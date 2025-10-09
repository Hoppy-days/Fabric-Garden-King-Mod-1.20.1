package net.jeremy.gardenkingmod.screen;

import java.util.ArrayList;
import java.util.List;

import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.block.entity.GardenShopBlockEntity;
import net.jeremy.gardenkingmod.shop.GardenShopOffer;
import net.jeremy.gardenkingmod.shop.GardenShopStackHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class GardenShopScreenHandler extends ScreenHandler {
        private static final int HOTBAR_SLOT_COUNT = 9;
        private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
        private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
        private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_ROW_COUNT * PLAYER_INVENTORY_COLUMN_COUNT;
        private static final int SLOT_SIZE = 18;
        private static final int SHOP_SLOT_START_X = 8;
        private static final int PLAYER_INVENTORY_START_Y = 123;
        private static final int PLAYER_INVENTORY_START_X = 132;
        private static final int PLAYER_HOTBAR_Y = 181;
        private static final int PLAYER_HOTBAR_X = 132;
        private static final int COST_SLOT_COUNT = 2;
        private static final int RESULT_SLOT_COUNT = 1;
        private static final int COST_SLOT_ONE_X = 144;
        private static final int COST_SLOT_TWO_X = 180;
        private static final int COST_SLOTS_Y = 45;
        private static final int RESULT_SLOT_X = 244;
        private static final int RESULT_SLOT_Y = 52;

        private static final int PURCHASE_BUTTON_FLAG = 1 << 30;
        private static final int SELECT_BUTTON_FLAG = 1 << 29;
        private static final int PAGE_INDEX_SHIFT = 16;
        private static final int PAGE_INDEX_MASK = 0x7FFF;
        private static final int OFFER_INDEX_MASK = 0xFFFF;

        private final Inventory inventory;
        private final GardenShopBlockEntity blockEntity;
        private final SimpleInventory costInventory;
        private final SimpleInventory resultInventory;
        private final List<List<GardenShopOffer>> offersByPage;
        private int selectedPageIndex = -1;
        private int selectedOfferIndex = -1;

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
                return (id >>> PAGE_INDEX_SHIFT) & PAGE_INDEX_MASK;
        }

        private static int decodeOfferIndex(int id) {
                int value = id & OFFER_INDEX_MASK;
                return value == OFFER_INDEX_MASK ? -1 : value;
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
                this.blockEntity = blockEntity;
                this.inventory = blockEntity != null ? blockEntity : new SimpleInventory(GardenShopBlockEntity.INVENTORY_SIZE);
                this.costInventory = new SimpleInventory(COST_SLOT_COUNT);
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
                                                if (!costStack.isEmpty()) {
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
                if (!player.getWorld().isClient) {
                        this.dropInventory(player, this.costInventory);
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
                        if (!this.insertItem(originalStack, playerInventoryStart, hotbarEnd, true)) {
                                return ItemStack.EMPTY;
                        }
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
        public boolean onButtonClick(PlayerEntity player, int id) {
                if (isSelectButtonId(id) && player instanceof ServerPlayerEntity serverPlayer) {
                        int pageIndex = decodePageIndex(id);
                        int offerIndex = decodeOfferIndex(id);
                        if (offerIndex < 0) {
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
                if (offerIndex < 0 || offerIndex >= pageOffers.size()) {
                        return clearSelection(player);
                }

                GardenShopOffer offer = pageOffers.get(offerIndex);
                boolean inventoryChanged = populateSelectedOffer(player, offer, true);
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

        private boolean populateSelectedOffer(ServerPlayerEntity player, GardenShopOffer offer, boolean returnExisting) {
                if (offer == null) {
                        return clearSelection(player);
                }

                PlayerInventory playerInventory = player.getInventory();
                boolean changed = false;

                if (returnExisting) {
                        if (returnCostItems(player)) {
                                changed = true;
                        }
                }

                List<ItemStack> costs = offer.costStacks();
                boolean playerChanged = false;
                boolean slotChanged = false;
                for (int slotIndex = 0; slotIndex < COST_SLOT_COUNT; slotIndex++) {
                        ItemStack template = slotIndex < costs.size() ? costs.get(slotIndex) : ItemStack.EMPTY;
                        ExtractResult result = fillCostSlotFromPlayer(playerInventory, template, slotIndex);
                        if (result.playerChanged()) {
                                playerChanged = true;
                        }
                        if (result.slotChanged()) {
                                slotChanged = true;
                        }
                }

                if (playerChanged) {
                        playerInventory.markDirty();
                        changed = true;
                }

                if (slotChanged) {
                        this.costInventory.markDirty();
                        changed = true;
                }

                if (updateResultSlot(offer, playerInventory)) {
                        changed = true;
                }

                return changed;
        }

        private boolean updateResultSlot(GardenShopOffer offer, PlayerInventory playerInventory) {
                ItemStack previous = this.resultInventory.getStack(0);
                ItemStack next = ItemStack.EMPTY;
                if (offer != null && canAfford(offer.costStacks(), this.costInventory, playerInventory)) {
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
                boolean changed = false;
                PlayerInventory playerInventory = player.getInventory();
                for (int slot = 0; slot < this.costInventory.size(); slot++) {
                        ItemStack stack = this.costInventory.removeStack(slot);
                        if (stack.isEmpty()) {
                                continue;
                        }

                        int requested = Math.max(GardenShopStackHelper.getRequestedCount(stack), stack.getCount());
                        ItemStack comparison = GardenShopStackHelper.copyWithoutRequestedCount(stack);
                        if (comparison.isEmpty()) {
                                comparison = stack.copy();
                                comparison.setCount(Math.min(requested, comparison.getMaxCount()));
                        }

                        int remaining = requested;
                        while (remaining > 0) {
                                ItemStack toInsert = comparison.copy();
                                int amount = Math.min(remaining, toInsert.getMaxCount());
                                toInsert.setCount(amount);
                                if (!playerInventory.insertStack(toInsert)) {
                                        player.dropItem(toInsert, false);
                                }
                                remaining -= amount;
                        }
                        changed = true;
                }

                if (changed) {
                        playerInventory.markDirty();
                        this.costInventory.markDirty();
                }
                return changed;
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
                PlayerInventory playerInventory = player.getInventory();
                if (!canAfford(offer.costStacks(), this.costInventory, playerInventory)) {
                        return false;
                }

                removeCostStacks(offer.costStacks(), playerInventory);
                if (!resultTakenFromSlot) {
                        ItemStack result = offer.copyResultStack();
                        if (!result.isEmpty()) {
                                if (!player.giveItemStack(result)) {
                                        player.dropItem(result, false);
                                }
                        }
                }
                populateSelectedOffer(player, offer, false);
                playerInventory.markDirty();
                this.costInventory.markDirty();
                return true;
        }

        private boolean canAfford(List<ItemStack> costs, Inventory... sources) {
                if (costs.isEmpty()) {
                        return true;
                }

                int[][] simulatedCounts = new int[sources.length][];
                for (int index = 0; index < sources.length; index++) {
                        Inventory source = sources[index];
                        simulatedCounts[index] = new int[source.size()];
                        for (int slot = 0; slot < source.size(); slot++) {
                                ItemStack stack = source.getStack(slot);
                                int available = stack.getCount();
                                if (source == this.costInventory) {
                                        available = GardenShopStackHelper.getRequestedCount(stack);
                                }
                                simulatedCounts[index][slot] = available;
                        }
                }

                for (ItemStack cost : costs) {
                        if (cost.isEmpty()) {
                                continue;
                        }

                        int required = GardenShopStackHelper.getRequestedCount(cost);
                        if (required <= 0) {
                                continue;
                        }

                        ItemStack comparisonStack = GardenShopStackHelper.copyWithoutRequestedCount(cost);
                        int remaining = required;
                        for (int sourceIndex = 0; sourceIndex < sources.length && remaining > 0; sourceIndex++) {
                                Inventory source = sources[sourceIndex];
                                int[] counts = simulatedCounts[sourceIndex];
                                for (int slot = 0; slot < source.size() && remaining > 0; slot++) {
                                        ItemStack stack = source.getStack(slot);
                                        if (stack.isEmpty()) {
                                                continue;
                                        }
                                        if (!ItemStack.canCombine(stack, comparisonStack)) {
                                                continue;
                                        }

                                        int available = counts[slot];
                                        if (available <= 0) {
                                                continue;
                                        }

                                        int taken = Math.min(available, remaining);
                                        counts[slot] -= taken;
                                        remaining -= taken;
                                }
                        }

                        if (remaining > 0) {
                                return false;
                        }
                }

                return true;
        }

        private void removeCostStacks(List<ItemStack> costs, PlayerInventory playerInventory) {
                for (ItemStack cost : costs) {
                        if (cost.isEmpty()) {
                                continue;
                        }

                        int required = GardenShopStackHelper.getRequestedCount(cost);
                        if (required <= 0) {
                                continue;
                        }

                        ItemStack comparisonStack = GardenShopStackHelper.copyWithoutRequestedCount(cost);
                        int remaining = required;
                        remaining = removeFromInventory(this.costInventory, comparisonStack, remaining);
                        if (remaining > 0) {
                                remaining = removeFromInventory(playerInventory, comparisonStack, remaining);
                        }
                }
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
                        if (!ItemStack.canCombine(stack, comparisonStack)) {
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
                this.addSlot(new Slot(this.costInventory, 0, COST_SLOT_ONE_X, COST_SLOTS_Y));
                this.addSlot(new Slot(this.costInventory, 1, COST_SLOT_TWO_X, COST_SLOTS_Y));
        }

        private void addResultSlot() {
                this.addSlot(new ResultSlot(this, this.resultInventory, 0, RESULT_SLOT_X, RESULT_SLOT_Y));
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
