package net.jeremy.gardenkingmod.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.block.entity.GardenShopBlockEntity;
import net.jeremy.gardenkingmod.shop.GardenShopOffer;
import net.jeremy.gardenkingmod.shop.GardenShopStackHelper;
import net.jeremy.gardenkingmod.screen.inventory.GardenShopCostInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import net.jeremy.gardenkingmod.mixin.SlotAccessor;

public class GardenShopScreenHandler extends ScreenHandler {
        private static final int HOTBAR_SLOT_COUNT = 9;
        private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
        private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
        private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_ROW_COUNT * PLAYER_INVENTORY_COLUMN_COUNT;
        private static final int SLOT_SIZE = 18;
        private static final int DEFAULT_SLOT_SPACING = SLOT_SIZE * 2;
        private static final int SHOP_SLOT_START_X = 8;
        private static final int PLAYER_INVENTORY_START_Y = 123;
        private static final int PLAYER_INVENTORY_START_X = 132;
        private static final int PLAYER_HOTBAR_Y = 181;
        private static final int PLAYER_HOTBAR_X = 132;
        public static final int COST_SLOT_COUNT = 2;
        private static final int RESULT_SLOT_COUNT = 1;

        private static final PageSlotLayout DEFAULT_PAGE_SLOT_LAYOUT = new PageSlotLayout(160, 51, 244, 51,
                        DEFAULT_SLOT_SPACING - 10);
        private static final PageSlotLayout PAGE_ONE_SLOT_LAYOUT = new PageSlotLayout(145, 44, 250, 48,
                        DEFAULT_SLOT_SPACING);
        private static final PageSlotLayout PAGE_TWO_SLOT_LAYOUT = new PageSlotLayout(160, 51, 244, 51,
                        DEFAULT_SLOT_SPACING);
        private static final PageSlotLayout PAGE_THREE_SLOT_LAYOUT = new PageSlotLayout(160, 51, 244, 51,
                        DEFAULT_SLOT_SPACING);
        private static final PageSlotLayout PAGE_FOUR_SLOT_LAYOUT = new PageSlotLayout(160, 51, 244, 51,
                        DEFAULT_SLOT_SPACING);
        private static final PageSlotLayout[] PAGE_SLOT_LAYOUTS = { DEFAULT_PAGE_SLOT_LAYOUT, PAGE_ONE_SLOT_LAYOUT,
                        PAGE_TWO_SLOT_LAYOUT, PAGE_THREE_SLOT_LAYOUT, PAGE_FOUR_SLOT_LAYOUT };

        private static final int PURCHASE_BUTTON_FLAG = 1 << 30;
        private static final int SELECT_BUTTON_FLAG = 1 << 29;
        private static final int BUTTON_FLAG_MASK = PURCHASE_BUTTON_FLAG | SELECT_BUTTON_FLAG;
        private static final int PAGE_INDEX_SHIFT = 16;
        private static final int PAGE_INDEX_MASK = 0x7FFF;
        private static final int OFFER_INDEX_MASK = 0xFFFF;

        private final Inventory inventory;
        private final GardenShopBlockEntity blockEntity;
        private final SimpleInventory costInventory;
        private final SimpleInventory resultInventory;
        private final PlayerInventory playerInventory;
        private final List<List<GardenShopOffer>> offersByPage;
        private final Slot[] costSlots = new Slot[COST_SLOT_COUNT];
        private Slot resultSlot;
        private int selectedPageIndex = -1;
        private int selectedOfferIndex = -1;
        private int currentPageIndex = 0;

        private static int encodeOfferIndexValue(int offerIndex) {
                return offerIndex < 0 ? OFFER_INDEX_MASK : offerIndex & OFFER_INDEX_MASK;
        }

        public static int encodePurchaseButtonId(int pageIndex, int offerIndex) {
                return PURCHASE_BUTTON_FLAG | ((pageIndex & PAGE_INDEX_MASK) << PAGE_INDEX_SHIFT)
                                | encodeOfferIndexValue(offerIndex);
        }

        public static int encodeSelectButtonId(int pageIndex, int offerIndex) {
                return SELECT_BUTTON_FLAG | ((pageIndex & PAGE_INDEX_MASK) << PAGE_INDEX_SHIFT)
                                | encodeOfferIndexValue(offerIndex);
        }

        private static boolean isPurchaseButtonId(int id) {
                return (id & PURCHASE_BUTTON_FLAG) != 0;
        }

        private static boolean isSelectButtonId(int id) {
                return (id & SELECT_BUTTON_FLAG) != 0;
        }

        private static int decodePageIndex(int id) {
                return ((id & ~BUTTON_FLAG_MASK) >>> PAGE_INDEX_SHIFT) & PAGE_INDEX_MASK;
        }

        private static int decodeOfferIndex(int id) {
                int value = id & OFFER_INDEX_MASK;
                return value == OFFER_INDEX_MASK ? -1 : value;
        }

        public int getCostSlotX(int slotIndex) {
                int clampedIndex = MathHelper.clamp(slotIndex, 0, COST_SLOT_COUNT - 1);
                PageSlotLayout layout = getPageSlotLayout(currentPageIndex);
                return layout.costSlotStartX() + clampedIndex * layout.costSlotSpacing();
        }

        public int getCostSlotY() {
                PageSlotLayout layout = getPageSlotLayout(currentPageIndex);
                return layout.costSlotsY();
        }

        public int getResultSlotX() {
                PageSlotLayout layout = getPageSlotLayout(currentPageIndex);
                return layout.resultSlotX();
        }

        public int getResultSlotY() {
                PageSlotLayout layout = getPageSlotLayout(currentPageIndex);
                return layout.resultSlotY();
        }

        public boolean isCostSlot(Slot slot) {
                return slot != null && slot.inventory == this.costInventory;
        }

        public boolean isResultSlot(Slot slot) {
                return slot != null && slot.inventory == this.resultInventory;
        }

        public int getDisplayedPageIndex() {
                return this.currentPageIndex;
        }

        public GardenShopScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
                this(syncId, playerInventory, getBlockEntity(playerInventory, buf.readBlockPos()), buf);
        }

        public GardenShopScreenHandler(int syncId, PlayerInventory playerInventory, GardenShopBlockEntity blockEntity) {
                this(syncId, playerInventory, blockEntity, null);
        }

        private GardenShopScreenHandler(int syncId, PlayerInventory playerInventory, GardenShopBlockEntity blockEntity,
                        PacketByteBuf buf) {
                super(ModScreenHandlers.GARDEN_SHOP_SCREEN_HANDLER, syncId);
                this.playerInventory = playerInventory;
                this.blockEntity = blockEntity;
                this.inventory = blockEntity != null ? blockEntity : new SimpleInventory(GardenShopBlockEntity.INVENTORY_SIZE);
                this.costInventory = new GardenShopCostInventory(COST_SLOT_COUNT);
                this.resultInventory = new SimpleInventory(RESULT_SLOT_COUNT);
                this.costInventory.addListener(inventory -> onContentChanged(inventory));
                this.resultInventory.addListener(inventory -> onContentChanged(inventory));
                this.offersByPage = new ArrayList<>();

                checkSize(this.inventory, GardenShopBlockEntity.INVENTORY_SIZE);
                if (blockEntity != null) {
                        blockEntity.ensureOffers();
                        this.offersByPage.addAll(blockEntity.getOfferPages());
                        fillInventoryFromOffers();
                }

                if (buf != null) {
                        this.offersByPage.clear();
                        int pageCount = buf.readVarInt();
                        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                                int offerCount = buf.readVarInt();
                                List<GardenShopOffer> pageOffers = new ArrayList<>(offerCount);
                                for (int offerIndex = 0; offerIndex < offerCount; offerIndex++) {
                                        ItemStack result = buf.readItemStack();
                                        int costCount = buf.readVarInt();
                                        List<ItemStack> costs = new ArrayList<>(costCount);
                                        for (int costIndex = 0; costIndex < costCount; costIndex++) {
                                                ItemStack costStack = buf.readItemStack();
                                                int requestedCount = buf.readVarInt();
                                                if (!costStack.isEmpty()) {
                                                        GardenShopStackHelper.applyRequestedCount(costStack, requestedCount);
                                                        costs.add(costStack);
                                                }
                                        }
                                        if (!result.isEmpty()) {
                                                pageOffers.add(GardenShopOffer.of(result, costs));
                                        }
                                }
                                this.offersByPage.add(List.copyOf(pageOffers));
                        }
                        fillInventoryFromOffers();
                }

                this.inventory.onOpen(playerInventory.player);
                this.costInventory.onOpen(playerInventory.player);
                this.resultInventory.onOpen(playerInventory.player);

                addCostSlots();
                addResultSlot();
                addPlayerInventory(playerInventory);
                addPlayerHotbar(playerInventory);
                applyPageLayout(currentPageIndex);
        }

        private static GardenShopBlockEntity getBlockEntity(PlayerInventory playerInventory, BlockPos pos) {
                if (playerInventory.player.getWorld().getBlockEntity(pos) instanceof GardenShopBlockEntity shopBlockEntity) {
                        return shopBlockEntity;
                }

                return null;
        }

        @Override
        public boolean canUse(PlayerEntity player) {
                if (this.blockEntity != null) {
                        return this.blockEntity.canPlayerUse(player);
                }

                return this.inventory.canPlayerUse(player);
        }

        @Override
        public void onClosed(PlayerEntity player) {
                super.onClosed(player);
                if (this.blockEntity != null) {
                        this.blockEntity.onClose(player);
                } else {
                        this.inventory.onClose(player);
                }
                this.costInventory.onClose(player);
                this.resultInventory.onClose(player);
                if (!player.getWorld().isClient && player instanceof ServerPlayerEntity serverPlayer) {
                        returnCostItems(serverPlayer);
                }
                this.resultInventory.clear();
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int index) {
                Slot slot = this.slots.get(index);
                if (slot == null || !slot.hasStack()) {
                        return ItemStack.EMPTY;
                }

                ItemStack originalStack = slot.getStack();
                ItemStack copiedStack = originalStack.copy();

                int costSlotEnd = COST_SLOT_COUNT;
                int resultSlotStart = costSlotEnd;
                int resultSlotEnd = resultSlotStart + RESULT_SLOT_COUNT;
                int playerInventoryStart = resultSlotEnd;
                int playerInventoryEnd = playerInventoryStart + PLAYER_INVENTORY_SLOT_COUNT;
                int hotbarStart = playerInventoryEnd;
                int hotbarEnd = hotbarStart + HOTBAR_SLOT_COUNT;

                if (index < costSlotEnd) {
                        CostReturnResult result = returnCostSlot(player, index, originalStack.copy());
                        if (!result.slotChanged()) {
                                return ItemStack.EMPTY;
                        }

                        if (result.playerChanged()) {
                                player.getInventory().markDirty();
                        }
                        this.costInventory.markDirty();
                        ItemStack returned = result.returnedStack();
                        ItemStack callbackStack = returned.copy();
                        slot.onQuickTransfer(callbackStack, ItemStack.EMPTY);
                        slot.onTakeItem(player, callbackStack);
                        return returned.copy();
                } else if (index < resultSlotEnd) {
                        if (!this.insertItem(originalStack, playerInventoryStart, hotbarEnd, true)) {
                                return ItemStack.EMPTY;
                        }
                } else if (index < hotbarEnd) {
                        if (!this.insertItem(originalStack, 0, costSlotEnd, false)) {
                                if (index < playerInventoryEnd) {
                                        if (!this.insertItem(originalStack, hotbarStart, hotbarEnd, false)) {
                                                return ItemStack.EMPTY;
                                        }
                                } else if (!this.insertItem(originalStack, playerInventoryStart, playerInventoryEnd, false)) {
                                        return ItemStack.EMPTY;
                                }
                        }
                } else {
                        return ItemStack.EMPTY;
                }

                slot.onQuickTransfer(originalStack, copiedStack);

                if (originalStack.isEmpty()) {
                        slot.setStack(ItemStack.EMPTY);
                } else {
                        slot.markDirty();
                }

                if (originalStack.getCount() == copiedStack.getCount()) {
                        return ItemStack.EMPTY;
                }

                slot.onTakeItem(player, originalStack);
                return copiedStack;
        }

        @Override
        protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
                if (startIndex >= 0 && endIndex <= COST_SLOT_COUNT && stack != null && !stack.isEmpty()) {
                        boolean inserted = false;
                        int index = fromLast ? endIndex - 1 : startIndex;
                        int step = fromLast ? -1 : 1;
                        while (fromLast ? index >= startIndex : index < endIndex) {
                                Slot slot = this.slots.get(index);
                                ItemStack slotStack = slot.getStack();
                                if (slotStack.isEmpty()) {
                                        ItemStack moved = stack.copy();
                                        int movedCount = stack.getCount();
                                        stack.setCount(0);
                                        GardenShopStackHelper.applyRequestedCount(moved, movedCount);
                                        slot.setStack(moved);
                                        slot.markDirty();
                                        inserted = true;
                                        break;
                                }

                                if (canCombineIgnoringRequestedCount(slotStack, stack)) {
                                        int existing = GardenShopStackHelper.getRequestedCount(slotStack);
                                        int addition = stack.getCount();
                                        if (addition > 0) {
                                                GardenShopStackHelper.applyRequestedCount(slotStack, existing + addition);
                                                slot.markDirty();
                                                stack.setCount(0);
                                                inserted = true;
                                        }
                                        break;
                                }

                                index += step;
                        }

                        if (inserted) {
                                return true;
                        }
                }

                return super.insertItem(stack, startIndex, endIndex, fromLast);
        }

        @Override
        public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
                super.onSlotClick(slotIndex, button, actionType, player);
                if (slotIndex >= 0 && slotIndex < COST_SLOT_COUNT && actionType == SlotActionType.PICKUP) {
                        Slot slot = this.slots.get(slotIndex);
                        if (slot != null) {
                                ItemStack slotStack = slot.getStack();
                                ItemStack cursor = getCursorStack();
                                if (!slotStack.isEmpty() && !cursor.isEmpty()
                                                && canCombineIgnoringRequestedCount(slotStack, cursor)) {
                                        if (button == 0) {
                                                int total = GardenShopStackHelper.getRequestedCount(slotStack) + cursor.getCount();
                                                if (cursor.getCount() > 0) {
                                                        GardenShopStackHelper.applyRequestedCount(slotStack, total);
                                                        cursor.setCount(0);
                                                        slot.markDirty();
                                                }
                                        } else if (button == 1 && cursor.getCount() > 0) {
                                                int total = GardenShopStackHelper.getRequestedCount(slotStack) + 1;
                                                GardenShopStackHelper.applyRequestedCount(slotStack, total);
                                                cursor.decrement(1);
                                                slot.markDirty();
                                        }
                                }
                        }
                }
        }

        @Override
        public boolean onButtonClick(PlayerEntity player, int id) {
                if (isSelectButtonId(id) && player instanceof ServerPlayerEntity serverPlayer) {
                        int pageIndex = decodePageIndex(id);
                        int offerIndex = decodeOfferIndex(id);
                        if (offerIndex < 0) {
                                setCurrentPageIndex(pageIndex);
                                if (clearSelection(serverPlayer)) {
                                        sendContentUpdates();
                                }
                        } else if (setSelectedOffer(serverPlayer, pageIndex, offerIndex)) {
                                sendContentUpdates();
                        }
                        return true;
                }

                if (isPurchaseButtonId(id) && player instanceof ServerPlayerEntity serverPlayer) {
                        int pageIndex = decodePageIndex(id);
                        int offerIndex = decodeOfferIndex(id);
                        if (tryPurchase(serverPlayer, pageIndex, offerIndex)) {
                                sendContentUpdates();
                        }
                        return true;
                }

                return super.onButtonClick(player, id);
        }

        public Inventory getInventory() {
                return this.inventory;
        }

        public List<GardenShopOffer> getOffers(int pageIndex) {
                if (pageIndex < 0 || pageIndex >= this.offersByPage.size()) {
                        return List.of();
                }

                return this.offersByPage.get(pageIndex);
        }

        public int getPageCount() {
                return this.offersByPage.size();
        }

        private boolean setSelectedOffer(ServerPlayerEntity player, int pageIndex, int offerIndex) {
                if (pageIndex < 0 || pageIndex >= this.offersByPage.size()) {
                        return clearSelection(player);
                }

                List<GardenShopOffer> pageOffers = this.offersByPage.get(pageIndex);
                setCurrentPageIndex(pageIndex);
                if (offerIndex < 0 || offerIndex >= pageOffers.size()) {
                        return clearSelection(player);
                }

                GardenShopOffer offer = pageOffers.get(offerIndex);
                boolean inventoryChanged = populateSelectedOffer(player, offer, true, true);
                setCurrentPageIndex(pageIndex);
                boolean selectionChanged = this.selectedPageIndex != pageIndex || this.selectedOfferIndex != offerIndex;
                this.selectedPageIndex = pageIndex;
                this.selectedOfferIndex = offerIndex;
                return inventoryChanged || selectionChanged;
        }

        private boolean clearSelection(ServerPlayerEntity player) {
                boolean changed = returnCostItems(player);
                ItemStack current = this.resultInventory.getStack(0);
                if (!current.isEmpty()) {
                        this.resultInventory.setStack(0, ItemStack.EMPTY);
                        this.resultInventory.markDirty();
                        changed = true;
                }
                boolean selectionChanged = this.selectedPageIndex != -1 || this.selectedOfferIndex != -1;
                this.selectedPageIndex = -1;
                this.selectedOfferIndex = -1;
                return changed || selectionChanged;
        }

        private boolean populateSelectedOffer(ServerPlayerEntity player, GardenShopOffer offer, boolean returnExisting,
                        boolean refillFromPlayerInventory) {
                if (offer == null) {
                        return clearSelection(player);
                }

                PlayerInventory playerInv = player.getInventory();
                boolean changed = false;

                if (returnExisting) {
                        if (returnCostItems(player)) {
                                changed = true;
                        }
                }

                boolean shouldRefill = refillFromPlayerInventory;
                if (shouldRefill) {
                        shouldRefill = hasSufficientResources(playerInv, offer.costStacks());
                }

                if (shouldRefill) {
                        List<ItemStack> costs = offer.costStacks();
                        boolean playerChanged = false;
                        boolean slotChanged = false;
                        for (int slotIndex = 0; slotIndex < COST_SLOT_COUNT; slotIndex++) {
                                ItemStack template = slotIndex < costs.size() ? costs.get(slotIndex) : ItemStack.EMPTY;
                                ExtractResult result = fillCostSlotFromPlayer(playerInv, template, slotIndex);
                                if (result.playerChanged()) {
                                        playerChanged = true;
                                }
                                if (result.slotChanged()) {
                                        slotChanged = true;
                                }
                        }

                        if (playerChanged) {
                                playerInv.markDirty();
                                changed = true;
                        }

                        if (slotChanged) {
                                this.costInventory.markDirty();
                                changed = true;
                        }
                }

                if (!shouldRefill) {
                        if (clearCostSlots()) {
                                changed = true;
                        }
                }

                if (updateResultSlot(offer)) {
                        changed = true;
                }

                return changed;
        }

        private boolean clearCostSlots() {
                boolean cleared = false;
                for (int slot = 0; slot < this.costInventory.size(); slot++) {
                        if (!this.costInventory.getStack(slot).isEmpty()) {
                                this.costInventory.setStack(slot, ItemStack.EMPTY);
                                cleared = true;
                        }
                }
                if (cleared) {
                        this.costInventory.markDirty();
                }
                return cleared;
        }

        private boolean hasSufficientResources(PlayerInventory inventory, List<ItemStack> costs) {
                if (costs == null || costs.isEmpty()) {
                        return true;
                }

                Map<StackComparisonKey, Integer> available = countInventoryStacks(inventory);
                for (ItemStack cost : costs) {
                        if (cost == null || cost.isEmpty()) {
                                continue;
                        }

                        int required = GardenShopStackHelper.getRequestedCount(cost);
                        if (required <= 0) {
                                continue;
                        }

                        ItemStack comparison = GardenShopStackHelper.copyWithoutRequestedCount(cost);
                        if (comparison.isEmpty()) {
                                continue;
                        }

                        StackComparisonKey key = StackComparisonKey.of(comparison);
                        int availableCount = available.getOrDefault(key, 0);
                        if (availableCount < required) {
                                return false;
                        }

                        available.put(key, availableCount - required);
                }

                return true;
        }

        private Map<StackComparisonKey, Integer> countInventoryStacks(PlayerInventory inventory) {
                Map<StackComparisonKey, Integer> counts = new HashMap<>();
                for (int slot = 0; slot < inventory.size(); slot++) {
                        ItemStack stack = inventory.getStack(slot);
                        if (stack.isEmpty()) {
                                continue;
                        }

                        StackComparisonKey key = StackComparisonKey.of(stack);
                        if (key == null) {
                                continue;
                        }

                        counts.merge(key, stack.getCount(), Integer::sum);
                }
                return counts;
        }

        private boolean updateResultSlot(GardenShopOffer offer) {
                ItemStack previous = this.resultInventory.getStack(0);
                ItemStack next = ItemStack.EMPTY;
                if (offer != null && costSlotsMatchOffer(offer)) {
                        next = offer.copyResultStack();
                }

                boolean changed = !ItemStack.areEqual(previous, next) || previous.getCount() != next.getCount();
                if (changed) {
                        this.resultInventory.setStack(0, next);
                        this.resultInventory.markDirty();
                }
                return changed;
        }

        private boolean returnCostItems(ServerPlayerEntity player) {
                boolean playerChanged = false;
                boolean slotChanged = false;
                for (int slot = 0; slot < this.costInventory.size(); slot++) {
                        ItemStack original = this.costInventory.getStack(slot);
                        if (original.isEmpty()) {
                                continue;
                        }

                        CostReturnResult result = returnCostSlot(player, slot, original.copy());
                        if (result.playerChanged()) {
                                playerChanged = true;
                        }
                        if (result.slotChanged()) {
                                slotChanged = true;
                        }
                }

                if (playerChanged) {
                        player.getInventory().markDirty();
                }
                if (slotChanged) {
                        this.costInventory.markDirty();
                }
                return playerChanged || slotChanged;
        }

        private CostReturnResult returnCostSlot(PlayerEntity player, int slotIndex, ItemStack originalCopy) {
                if (originalCopy == null || originalCopy.isEmpty()) {
                        return CostReturnResult.NO_CHANGE;
                }

                ItemStack removed = this.costInventory.removeStack(slotIndex);
                if (removed.isEmpty()) {
                        return CostReturnResult.NO_CHANGE;
                }

                int provided = Math.max(GardenShopStackHelper.getRequestedCount(removed), removed.getCount());
                int requested = Math.max(GardenShopStackHelper.getRequestedCount(originalCopy), provided);
                if (requested <= 0) {
                        requested = provided;
                }

                ItemStack comparison = GardenShopStackHelper.copyWithoutRequestedCount(originalCopy);
                if (comparison.isEmpty()) {
                        comparison = GardenShopStackHelper.copyWithoutRequestedCount(removed);
                }

                if (comparison.isEmpty()) {
                        comparison = removed.copy();
                        comparison.setCount(Math.min(requested, comparison.getMaxCount()));
                }

                PlayerInventory playerInventory = player.getInventory();
                boolean playerChanged = false;

                if (requested <= 0 || comparison.isEmpty()) {
                        this.costInventory.setStack(slotIndex, ItemStack.EMPTY);
                        if (!removed.isEmpty()) {
                                ItemStack fallback = removed.copy();
                                if (playerInventory.insertStack(fallback)) {
                                        playerChanged = true;
                                } else {
                                        player.dropItem(fallback, false);
                                }
                        }
                        return new CostReturnResult(playerChanged, true, removed);
                }

                int remaining = requested;
                while (remaining > 0) {
                        ItemStack toInsert = comparison.copy();
                        int amount = Math.min(remaining, toInsert.getMaxCount());
                        toInsert.setCount(amount);

                        if (!player.giveItemStack(toInsert.copy())) {
                                player.dropItem(toInsert.copy(), false);
                        } else {
                                playerChanged = true;
                        }

                        remaining -= amount;
                }

                return new CostReturnResult(playerChanged, true, removed);
        }

        private ExtractResult fillCostSlotFromPlayer(PlayerInventory playerInventory, ItemStack template, int slotIndex) {
                ItemStack previous = this.costInventory.getStack(slotIndex);

                if (template == null || template.isEmpty()) {
                        boolean slotChanged = !previous.isEmpty();
                        if (slotChanged) {
                                this.costInventory.setStack(slotIndex, ItemStack.EMPTY);
                        }
                        return new ExtractResult(false, slotChanged);
                }

                int required = GardenShopStackHelper.getRequestedCount(template);
                if (required <= 0) {
                        boolean slotChanged = !previous.isEmpty();
                        if (slotChanged) {
                                this.costInventory.setStack(slotIndex, ItemStack.EMPTY);
                        }
                        return new ExtractResult(false, slotChanged);
                }

                ItemStack comparison = GardenShopStackHelper.copyWithoutRequestedCount(template);
                if (comparison.isEmpty()) {
                        boolean slotChanged = !previous.isEmpty();
                        if (slotChanged) {
                                this.costInventory.setStack(slotIndex, ItemStack.EMPTY);
                        }
                        return new ExtractResult(false, slotChanged);
                }

                ExtractionResult extracted = extractMatchingStacks(playerInventory, comparison, required);
                ItemStack collected = extracted.collected();

                if (collected.isEmpty()) {
                        boolean slotChanged = !previous.isEmpty();
                        if (slotChanged) {
                                this.costInventory.setStack(slotIndex, ItemStack.EMPTY);
                        }
                        return new ExtractResult(extracted.playerChanged(), slotChanged);
                }

                boolean slotChanged = !ItemStack.areEqual(previous, collected)
                                || previous.getCount() != collected.getCount()
                                || GardenShopStackHelper.getRequestedCount(previous) != GardenShopStackHelper
                                                .getRequestedCount(collected);
                this.costInventory.setStack(slotIndex, collected);
                return new ExtractResult(extracted.playerChanged(), slotChanged);
        }

        private ExtractionResult extractMatchingStacks(PlayerInventory playerInventory, ItemStack comparison,
                        int required) {
                if (required <= 0) {
                        return new ExtractionResult(ItemStack.EMPTY, false);
                }

                int remaining = required;
                int totalCollected = 0;
                ItemStack collected = ItemStack.EMPTY;
                boolean playerChanged = false;

                for (int inventorySlot = 0; inventorySlot < playerInventory.size() && remaining > 0; inventorySlot++) {
                        ItemStack playerStack = playerInventory.getStack(inventorySlot);
                        if (playerStack.isEmpty() || !ItemStack.canCombine(playerStack, comparison)) {
                                continue;
                        }

                        int taken = Math.min(playerStack.getCount(), remaining);
                        if (taken <= 0) {
                                continue;
                        }

                        if (collected.isEmpty()) {
                                collected = playerStack.copy();
                                collected.setCount(Math.min(taken, collected.getMaxCount()));
                        } else {
                                int newCount = Math.min(collected.getCount() + taken, collected.getMaxCount());
                                collected.setCount(newCount);
                        }

                        playerStack.decrement(taken);
                        remaining -= taken;
                        totalCollected += taken;
                        playerChanged = true;

                        if (playerStack.isEmpty()) {
                                playerInventory.setStack(inventorySlot, ItemStack.EMPTY);
                        }
                }

                if (!playerChanged) {
                        return new ExtractionResult(ItemStack.EMPTY, false);
                }

                GardenShopStackHelper.applyRequestedCount(collected, totalCollected);
                return new ExtractionResult(collected, true);
        }

        private boolean tryPurchase(ServerPlayerEntity player, int pageIndex, int offerIndex) {
                return processPurchase(player, pageIndex, offerIndex, false);
        }

        private boolean processPurchase(ServerPlayerEntity player, int pageIndex, int offerIndex,
                        boolean resultTakenFromSlot) {
                if (pageIndex < 0 || pageIndex >= offersByPage.size()) {
                        return false;
                }

                List<GardenShopOffer> pageOffers = offersByPage.get(pageIndex);
                if (offerIndex < 0 || offerIndex >= pageOffers.size()) {
                        return false;
                }

                GardenShopOffer offer = pageOffers.get(offerIndex);
                PlayerInventory playerInv = player.getInventory();
                if (!costSlotsMatchOffer(offer)) {
                        return false;
                }

                boolean costSlotsChanged = removeCostStacks(offer.costStacks(), playerInv);
                if (!resultTakenFromSlot) {
                        ItemStack result = offer.copyResultStack();
                        if (!result.isEmpty()) {
                                if (!player.giveItemStack(result)) {
                                        player.dropItem(result, false);
                                }
                        }
                }
                populateSelectedOffer(player, offer, false, true);
                playerInv.markDirty();
                if (costSlotsChanged) {
                        this.costInventory.markDirty();
                }
                return true;
        }

        @Override
        public void onContentChanged(Inventory inventory) {
                super.onContentChanged(inventory);
                if (inventory == this.costInventory) {
                        GardenShopOffer offer = getSelectedOffer();
                        if (updateResultSlot(offer) && !this.playerInventory.player.getWorld().isClient) {
                                sendContentUpdates();
                        }
                }
        }

        private GardenShopOffer getSelectedOffer() {
                if (this.selectedPageIndex < 0 || this.selectedPageIndex >= this.offersByPage.size()) {
                        return null;
                }

                List<GardenShopOffer> offers = this.offersByPage.get(this.selectedPageIndex);
                if (this.selectedOfferIndex < 0 || this.selectedOfferIndex >= offers.size()) {
                        return null;
                }

                return offers.get(this.selectedOfferIndex);
        }

        private boolean costSlotsMatchOffer(GardenShopOffer offer) {
                if (offer == null) {
                        return false;
                }

                List<ItemStack> costs = offer.costStacks();
                for (int slotIndex = 0; slotIndex < COST_SLOT_COUNT; slotIndex++) {
                        ItemStack required = slotIndex < costs.size() ? costs.get(slotIndex) : ItemStack.EMPTY;
                        ItemStack provided = this.costInventory.getStack(slotIndex);
                        if (!costMatches(required, provided)) {
                                return false;
                        }
                }

                return true;
        }

        private boolean costMatches(ItemStack required, ItemStack provided) {
                if (required == null || required.isEmpty()) {
                        return provided.isEmpty();
                }

                if (provided.isEmpty()) {
                        return false;
                }

                if (!canCombineIgnoringRequestedCount(provided, required)) {
                        return false;
                }

                int requiredCount = GardenShopStackHelper.getRequestedCount(required);
                if (requiredCount <= 0) {
                        return provided.isEmpty();
                }

                int providedCount = GardenShopStackHelper.getRequestedCount(provided);
                return providedCount >= requiredCount;
        }

        private static boolean canCombineIgnoringRequestedCount(ItemStack first, ItemStack second) {
                ItemStack firstComparison = GardenShopStackHelper.copyWithoutRequestedCount(first);
                ItemStack secondComparison = GardenShopStackHelper.copyWithoutRequestedCount(second);
                return ItemStack.canCombine(firstComparison, secondComparison);
        }

        private boolean removeCostStacks(List<ItemStack> costs, PlayerInventory playerInventory) {
                boolean costSlotsChanged = false;
                for (int index = 0; index < costs.size(); index++) {
                        ItemStack cost = costs.get(index);
                        if (cost.isEmpty()) {
                                continue;
                        }

                        int required = GardenShopStackHelper.getRequestedCount(cost);
                        if (required <= 0) {
                                continue;
                        }

                        ItemStack comparisonStack = GardenShopStackHelper.copyWithoutRequestedCount(cost);
                        int remaining = required;
                        if (index < COST_SLOT_COUNT) {
                                SlotConsumptionResult result = consumeCostSlot(index, comparisonStack, required);
                                remaining = result.remaining();
                                if (result.changed()) {
                                        costSlotsChanged = true;
                                }
                        }
                        if (remaining > 0) {
                                remaining = removeFromInventory(playerInventory, comparisonStack, remaining);
                        }
                }
                return costSlotsChanged;
        }

        private SlotConsumptionResult consumeCostSlot(int slotIndex, ItemStack comparisonStack, int required) {
                if (required <= 0) {
                        return new SlotConsumptionResult(0, false);
                }

                ItemStack slotStack = this.costInventory.getStack(slotIndex);
                if (slotStack.isEmpty()) {
                        return new SlotConsumptionResult(required, false);
                }

                ItemStack comparisonSource = GardenShopStackHelper.copyWithoutRequestedCount(slotStack);
                if (!ItemStack.canCombine(comparisonSource, comparisonStack)) {
                        return new SlotConsumptionResult(required, false);
                }

                int provided = GardenShopStackHelper.getRequestedCount(slotStack);
                if (provided <= 0) {
                        this.costInventory.setStack(slotIndex, ItemStack.EMPTY);
                        return new SlotConsumptionResult(required, true);
                }

                int consumed = Math.min(provided, required);
                int leftover = provided - consumed;
                if (leftover > 0) {
                        ItemStack replacement = GardenShopStackHelper.copyWithoutRequestedCount(slotStack);
                        GardenShopStackHelper.applyRequestedCount(replacement, leftover);
                        this.costInventory.setStack(slotIndex, replacement);
                } else {
                        this.costInventory.setStack(slotIndex, ItemStack.EMPTY);
                }

                return new SlotConsumptionResult(required - consumed, true);
        }

        private record SlotConsumptionResult(int remaining, boolean changed) {
        }

        private int removeFromInventory(Inventory inventory, ItemStack comparisonStack, int remaining) {
                if (remaining <= 0) {
                        return 0;
                }

                boolean changed = false;
                for (int slot = 0; slot < inventory.size() && remaining > 0; slot++) {
                        ItemStack stack = inventory.getStack(slot);
                        if (stack.isEmpty()) {
                                continue;
                        }
                        ItemStack comparisonSource = stack;
                        if (inventory == this.costInventory) {
                                comparisonSource = GardenShopStackHelper.copyWithoutRequestedCount(stack);
                        }
                        if (!ItemStack.canCombine(comparisonSource, comparisonStack)) {
                                continue;
                        }

                        if (inventory == this.costInventory) {
                                int requested = GardenShopStackHelper.getRequestedCount(stack);
                                if (requested <= 0) {
                                        inventory.setStack(slot, ItemStack.EMPTY);
                                        changed = true;
                                        continue;
                                }

                                int taken = Math.min(requested, remaining);
                                if (taken <= 0) {
                                        continue;
                                }

                                remaining -= taken;
                                changed = true;

                                int leftover = requested - taken;
                                if (leftover > 0) {
                                        ItemStack replacement = GardenShopStackHelper.copyWithoutRequestedCount(stack);
                                        GardenShopStackHelper.applyRequestedCount(replacement, leftover);
                                        inventory.setStack(slot, replacement);
                                } else {
                                        inventory.setStack(slot, ItemStack.EMPTY);
                                }
                                continue;
                        }

                        int taken = Math.min(stack.getCount(), remaining);
                        if (taken <= 0) {
                                continue;
                        }

                        stack.decrement(taken);
                        remaining -= taken;
                        changed = true;
                        if (stack.isEmpty()) {
                                inventory.setStack(slot, ItemStack.EMPTY);
                        }
                }

                if (changed) {
                        inventory.markDirty();
                }
                return remaining;
        }

        private void addPlayerInventory(PlayerInventory playerInventory) {
                for (int row = 0; row < PLAYER_INVENTORY_ROW_COUNT; ++row) {
                        for (int column = 0; column < PLAYER_INVENTORY_COLUMN_COUNT; ++column) {
                                int x = PLAYER_INVENTORY_START_X + column * SLOT_SIZE;
                                int y = PLAYER_INVENTORY_START_Y + row * SLOT_SIZE;
                                this.addSlot(new Slot(playerInventory, column + row * PLAYER_INVENTORY_COLUMN_COUNT + HOTBAR_SLOT_COUNT,
                                                x, y));
                        }
                }
        }

        private void addPlayerHotbar(PlayerInventory playerInventory) {
                for (int slot = 0; slot < HOTBAR_SLOT_COUNT; ++slot) {
                        int x = PLAYER_HOTBAR_X + slot * SLOT_SIZE;
                        this.addSlot(new Slot(playerInventory, slot, x, PLAYER_HOTBAR_Y));
                }
        }

        private void addCostSlots() {
                for (int index = 0; index < COST_SLOT_COUNT; index++) {
                        Slot slot = new Slot(this.costInventory, index, getCostSlotX(index), getCostSlotY());
                        this.costSlots[index] = this.addSlot(slot);
                }
        }

        private void addResultSlot() {
                this.resultSlot = this.addSlot(new ResultSlot(this, this.resultInventory, 0, getResultSlotX(), getResultSlotY()));
        }

        public void setDisplayedPage(int pageIndex) {
                setCurrentPageIndex(pageIndex);
        }

        private void setCurrentPageIndex(int pageIndex) {
                int clampedIndex = Math.max(pageIndex, 0);
                if (this.currentPageIndex != clampedIndex) {
                        this.currentPageIndex = clampedIndex;
                        applyPageLayout(clampedIndex);
                } else {
                        applyPageLayout(clampedIndex);
                }
        }

        public void applyPageLayout(int pageIndex) {
                PageSlotLayout layout = getPageSlotLayout(pageIndex);
                for (int index = 0; index < costSlots.length; index++) {
                        Slot slot = costSlots[index];
                        if (slot != null) {
                                updateSlotPosition(slot,
                                                layout.costSlotStartX() + index * layout.costSlotSpacing(),
                                                layout.costSlotsY());
                        }
                }

                if (resultSlot != null) {
                        updateSlotPosition(resultSlot, layout.resultSlotX(), layout.resultSlotY());
                }
        }

        private void updateSlotPosition(Slot slot, int x, int y) {
                if (slot instanceof SlotAccessor accessor) {
                        accessor.setX(x);
                        accessor.setY(y);
                }
        }

        private PageSlotLayout getPageSlotLayout(int pageIndex) {
                if (pageIndex >= 0 && pageIndex < PAGE_SLOT_LAYOUTS.length && PAGE_SLOT_LAYOUTS[pageIndex] != null) {
                        return PAGE_SLOT_LAYOUTS[pageIndex];
                }
                return DEFAULT_PAGE_SLOT_LAYOUT;
        }

        private record PageSlotLayout(int costSlotStartX, int costSlotsY, int resultSlotX, int resultSlotY,
                        int costSlotSpacing) {
        }

        private void fillInventoryFromOffers() {
                List<GardenShopOffer> flattened = flattenOffers();
                for (int index = 0; index < this.inventory.size(); index++) {
                        ItemStack stack = index < flattened.size() ? flattened.get(index).copyResultStack() : ItemStack.EMPTY;
                        this.inventory.setStack(index, stack);
                }
        }

        private List<GardenShopOffer> flattenOffers() {
                if (this.offersByPage.isEmpty()) {
                        return List.of();
                }

                List<GardenShopOffer> flattened = new ArrayList<>();
                for (List<GardenShopOffer> page : this.offersByPage) {
                        flattened.addAll(page);
                }
                return flattened;
        }

        private record ExtractResult(boolean playerChanged, boolean slotChanged) {
        }

        private record ExtractionResult(ItemStack collected, boolean playerChanged) {
        }

        private record CostReturnResult(boolean playerChanged, boolean slotChanged, ItemStack returnedStack) {
                static final CostReturnResult NO_CHANGE = new CostReturnResult(false, false, ItemStack.EMPTY);
        }

        private record StackComparisonKey(Item item, NbtCompound components) {
                static StackComparisonKey of(ItemStack stack) {
                        if (stack == null || stack.isEmpty()) {
                                return null;
                        }

                        ItemStack sanitized = GardenShopStackHelper.copyWithoutRequestedCount(stack);
                        if (sanitized.isEmpty()) {
                                return null;
                        }

                        Item item = sanitized.getItem();
                        NbtCompound nbt = sanitized.hasNbt() ? sanitized.getNbt().copy() : null;
                        return new StackComparisonKey(item, nbt);
                }
        }

        private static class ResultSlot extends Slot {
                private final GardenShopScreenHandler handler;

                public ResultSlot(GardenShopScreenHandler handler, Inventory inventory, int index, int x, int y) {
                        super(inventory, index, x, y);
                        this.handler = handler;
                }

                @Override
                public boolean canInsert(ItemStack stack) {
                        return false;
                }

                @Override
                public boolean canTakeItems(PlayerEntity playerEntity) {
                        return !getStack().isEmpty();
                }

                @Override
                public void onTakeItem(PlayerEntity player, ItemStack stack) {
                        boolean purchased = false;
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                                int pageIndex = handler.selectedPageIndex;
                                int offerIndex = handler.selectedOfferIndex;
                                purchased = handler.processPurchase(serverPlayer, pageIndex, offerIndex, true);
                        }
                        super.onTakeItem(player, stack);
                        if (purchased) {
                                handler.sendContentUpdates();
                        }
                }
        }
}
