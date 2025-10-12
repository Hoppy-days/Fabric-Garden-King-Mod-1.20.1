package net.jeremy.gardenkingmod.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.block.entity.MarketBlockEntity;
import net.jeremy.gardenkingmod.item.WalletItem;
import net.jeremy.gardenkingmod.screen.inventory.GearShopCostInventory;
import net.jeremy.gardenkingmod.shop.GardenMarketOfferManager;
import net.jeremy.gardenkingmod.shop.GearShopOffer;
import net.jeremy.gardenkingmod.shop.GearShopStackHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
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

public class MarketScreenHandler extends ScreenHandler {
        public static final int BUTTON_SELL = 0;
        public static final int BUTTON_SELECT_SELL_TAB = 1;
        public static final int BUTTON_SELECT_BUY_TAB = 2;
        public static final int BUTTON_BUY = 3;
        private static final int BUTTON_SELECT_BUY_OFFER_BASE = 1000;

        private static final int SLOT_SIZE = 18;
        private static final int COST_SLOT_COUNT = 2;
        private static final int RESULT_SLOT_COUNT = 1;
        /**
         * The buy tab background is 100 pixels wider than the sell tab texture. The
         * wider texture extends equally to the left of the default GUI origin, so we
         * shift the slot anchors by that amount to keep them centered on the slot
         * frames drawn in {@link MarketScreen}.
         */
        private static final int BUY_BACKGROUND_X_OFFSET = 100;
        private static final int COST_SLOT_FIRST_X = 121 - BUY_BACKGROUND_X_OFFSET;
        private static final int COST_SLOT_Y = 49;
        private static final int COST_SLOT_SPACING = 36;
        private static final int RESULT_SLOT_X = 226 - BUY_BACKGROUND_X_OFFSET;
        private static final int RESULT_SLOT_Y = 56;

        private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
        private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
        private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_ROW_COUNT * PLAYER_INVENTORY_COLUMN_COUNT;
        private static final int HOTBAR_SLOT_COUNT = 9;

        private final Inventory inventory;
        private final MarketBlockEntity blockEntity;
        private final List<MarketSellSlot> marketSlots;
        private final PlayerInventory playerInventory;
        private final GearShopCostInventory costInventory;
        private final SimpleInventory resultInventory;
        private final Slot[] costSlots = new Slot[COST_SLOT_COUNT];
        private Slot resultSlot;
        private boolean marketSlotsEnabled;
        private boolean buySlotsEnabled;
        private final List<GearShopOffer> buyOffers;
        private int selectedOfferIndex;
        private final int marketInventoryEnd;
        private final int costSlotStartIndex;
        private final int costSlotEndIndex;
        private final int resultSlotStartIndex;
        private final int playerInventoryStartIndex;
        private final int hotbarStartIndex;

        public MarketScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
                this(syncId, playerInventory, getBlockEntity(playerInventory, buf.readBlockPos()));
        }

        public MarketScreenHandler(int syncId, PlayerInventory playerInventory, MarketBlockEntity blockEntity) {
                super(ModScreenHandlers.MARKET_SCREEN_HANDLER, syncId);
                this.blockEntity = blockEntity;
                this.playerInventory = playerInventory;
                this.inventory = blockEntity != null ? blockEntity : new SimpleInventory(MarketBlockEntity.INVENTORY_SIZE);
                this.marketSlots = new ArrayList<>();
                this.marketSlotsEnabled = true;
                this.buySlotsEnabled = false;
                this.costInventory = new GearShopCostInventory(COST_SLOT_COUNT);
                this.resultInventory = new SimpleInventory(RESULT_SLOT_COUNT);
                this.costInventory.addListener(this::onInventoryChanged);
                this.resultInventory.addListener(this::onInventoryChanged);
                this.costInventory.onOpen(playerInventory.player);
                this.resultInventory.onOpen(playerInventory.player);
                this.buyOffers = new ArrayList<>(GardenMarketOfferManager.getInstance().getOffers());
                this.selectedOfferIndex = -1;

                checkSize(this.inventory, MarketBlockEntity.INVENTORY_SIZE);
                this.inventory.onOpen(playerInventory.player);

                addMarketInventory();

                this.marketInventoryEnd = this.slots.size();
                this.costSlotStartIndex = this.marketInventoryEnd;
                addCostSlots();
                this.costSlotEndIndex = this.slots.size();
                this.resultSlotStartIndex = this.costSlotEndIndex;
                addResultSlot();
                int resultSlotEndIndex = this.slots.size();
                this.playerInventoryStartIndex = resultSlotEndIndex;
                addPlayerInventory(playerInventory);
                int playerInventoryEndIndex = this.slots.size();
                this.hotbarStartIndex = playerInventoryEndIndex;
                addPlayerHotbar(playerInventory);

                setMarketSlotsEnabled(true);
                setBuySlotsEnabled(false);
        }
        private void onInventoryChanged(Inventory inventory) {
                onContentChanged(inventory);
        }

        private void addMarketInventory() {
                final int slotsPerRow = 9;
                final int startX = 8;
                final int startY = 34;

                for (int slotIndex = 0; slotIndex < MarketBlockEntity.INVENTORY_SIZE; ++slotIndex) {
                        int column = slotIndex % slotsPerRow;
                        int row = slotIndex / slotsPerRow;
                        int x = startX + column * SLOT_SIZE;
                        int y = startY + row * SLOT_SIZE;
                        MarketSellSlot slot = new MarketSellSlot(this.inventory, slotIndex, x, y);
                        this.addSlot(slot);
                        this.marketSlots.add(slot);
                }
        }

        private void addCostSlots() {
                for (int index = 0; index < COST_SLOT_COUNT; index++) {
                        Slot slot = new MarketCostSlot(this.costInventory, index, getCostSlotX(index), COST_SLOT_Y);
                        this.costSlots[index] = this.addSlot(slot);
                }
        }

        private void addResultSlot() {
                this.resultSlot = this.addSlot(new MarketResultSlot(this, this.resultInventory, 0, RESULT_SLOT_X, RESULT_SLOT_Y));
        }

        private int getCostSlotX(int index) {
                int clamped = Math.max(0, Math.min(index, COST_SLOT_COUNT - 1));
                return COST_SLOT_FIRST_X + clamped * COST_SLOT_SPACING;
        }

        public void setMarketSlotsEnabled(boolean enabled) {
                if (this.marketSlotsEnabled == enabled) {
                        return;
                }

                this.marketSlotsEnabled = enabled;

                for (MarketSellSlot slot : this.marketSlots) {
                        slot.setEnabled(enabled);
                }
        }

        public void setBuySlotsEnabled(boolean enabled) {
                if (this.buySlotsEnabled == enabled) {
                        return;
                }

                this.buySlotsEnabled = enabled;
                for (Slot slot : this.costSlots) {
                        if (slot instanceof MarketCostSlot costSlot) {
                                costSlot.setEnabled(enabled);
                        }
                }
                if (this.resultSlot instanceof MarketResultSlot resultSlot) {
                        resultSlot.setEnabled(enabled);
                }
        }

        public boolean areMarketSlotsEnabled() {
                return this.marketSlotsEnabled;
        }

        public boolean areBuySlotsEnabled() {
                return this.buySlotsEnabled;
        }
        public boolean isCostSlot(Slot slot) {
                return slot != null && slot.inventory == this.costInventory;
        }

        public boolean isResultSlot(Slot slot) {
                return slot == this.resultSlot;
        }
        public static int encodeSelectBuyOfferButtonId(int offerIndex) {
                return BUTTON_SELECT_BUY_OFFER_BASE + (offerIndex + 1);
        }

        private static boolean isSelectBuyOfferButtonId(int id) {
                return id >= BUTTON_SELECT_BUY_OFFER_BASE;
        }

        private static int decodeSelectBuyOfferIndex(int id) {
                return id - BUTTON_SELECT_BUY_OFFER_BASE - 1;
        }
        private static MarketBlockEntity getBlockEntity(PlayerInventory playerInventory, BlockPos pos) {
                if (playerInventory.player.getWorld().getBlockEntity(pos) instanceof MarketBlockEntity marketBlockEntity) {
                        return marketBlockEntity;
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
                if (player instanceof ServerPlayerEntity serverPlayer) {
                        returnCostItems(serverPlayer);
                }
        }
        @Override
        public ItemStack quickMove(PlayerEntity player, int index) {
                Slot slot = this.slots.get(index);
                if (slot == null || !slot.hasStack()) {
                        return ItemStack.EMPTY;
                }

                ItemStack originalStack = slot.getStack();
                ItemStack copiedStack = originalStack.copy();

                int marketEnd = this.marketInventoryEnd;
                int costSlotStart = this.costSlotStartIndex;
                int costSlotEnd = this.costSlotEndIndex;
                int resultSlotStart = this.resultSlotStartIndex;
                int resultSlotEnd = resultSlotStart + RESULT_SLOT_COUNT;
                int playerInventoryStart = this.playerInventoryStartIndex;
                int playerInventoryEnd = playerInventoryStart + PLAYER_INVENTORY_SLOT_COUNT;
                int hotbarStart = this.hotbarStartIndex;
                int hotbarEnd = hotbarStart + HOTBAR_SLOT_COUNT;

                if (index < marketEnd) {
                        if (!this.insertItem(originalStack, playerInventoryStart, hotbarEnd, true)) {
                                return ItemStack.EMPTY;
                        }
                } else if (index >= costSlotStart && index < costSlotEnd) {
                        CostReturnResult result = returnCostSlot(player, index - costSlotStart, originalStack.copy());
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
                } else if (index >= resultSlotStart && index < resultSlotEnd) {
                        if (!this.insertItem(originalStack, playerInventoryStart, hotbarEnd, true)) {
                                return ItemStack.EMPTY;
                        }
                } else if (index < hotbarEnd) {
                        boolean inserted = false;

                        if (this.areMarketSlotsEnabled()) {
                                inserted = this.insertItem(originalStack, 0, marketEnd, false);
                        }

                        if (!inserted && this.areBuySlotsEnabled()) {
                                inserted = this.insertItem(originalStack, costSlotStart, costSlotEnd, false);
                        }

                        if (!inserted) {
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
                if (startIndex >= this.costSlotStartIndex && endIndex <= this.costSlotEndIndex && stack != null && !stack.isEmpty()) {
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
                                        GearShopStackHelper.applyRequestedCount(moved, movedCount);
                                        slot.setStack(moved);
                                        slot.markDirty();
                                        inserted = true;
                                        break;
                                }

                                if (canCombineIgnoringRequestedCount(slotStack, stack)) {
                                        int existing = GearShopStackHelper.getRequestedCount(slotStack);
                                        int addition = stack.getCount();
                                        if (addition > 0) {
                                                GearShopStackHelper.applyRequestedCount(slotStack, existing + addition);
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
                if (slotIndex >= this.costSlotStartIndex && slotIndex < this.costSlotEndIndex && actionType == SlotActionType.PICKUP) {
                        Slot slot = this.slots.get(slotIndex);
                        if (slot != null) {
                                ItemStack slotStack = slot.getStack();
                                ItemStack cursor = getCursorStack();
                                if (!slotStack.isEmpty() && !cursor.isEmpty() && canCombineIgnoringRequestedCount(slotStack, cursor)) {
                                        if (button == 0) {
                                                int total = GearShopStackHelper.getRequestedCount(slotStack) + cursor.getCount();
                                                if (cursor.getCount() > 0) {
                                                        GearShopStackHelper.applyRequestedCount(slotStack, total);
                                                        cursor.setCount(0);
                                                        slot.markDirty();
                                                }
                                        } else if (button == 1 && cursor.getCount() > 0) {
                                                int total = GearShopStackHelper.getRequestedCount(slotStack) + 1;
                                                GearShopStackHelper.applyRequestedCount(slotStack, total);
                                                cursor.decrement(1);
                                                slot.markDirty();
                                        }
                                }
                        }
                }
        }
        @Override
        public boolean onButtonClick(PlayerEntity player, int id) {
                if (isSelectBuyOfferButtonId(id) && player instanceof ServerPlayerEntity serverPlayer) {
                        int offerIndex = decodeSelectBuyOfferIndex(id);
                        if (offerIndex < 0) {
                                if (clearSelection(serverPlayer)) {
                                        sendContentUpdates();
                                }
                        } else if (setSelectedOffer(serverPlayer, offerIndex)) {
                                sendContentUpdates();
                        }
                        return true;
                }

                if (this.blockEntity != null) {
                        if (id == BUTTON_SELL && player instanceof ServerPlayerEntity serverPlayer) {
                                if (this.blockEntity.sell(serverPlayer)) {
                                        sendContentUpdates();
                                }
                                return true;
                        }

                        if (id == BUTTON_SELECT_SELL_TAB) {
                                if (player instanceof ServerPlayerEntity serverPlayer) {
                                        if (clearSelection(serverPlayer)) {
                                                sendContentUpdates();
                                        }
                                }
                                setMarketSlotsEnabled(true);
                                setBuySlotsEnabled(false);
                                return true;
                        }

                        if (id == BUTTON_SELECT_BUY_TAB) {
                                boolean returnedItems = this.blockEntity.returnItemsToPlayer(player);
                                setMarketSlotsEnabled(false);
                                setBuySlotsEnabled(true);
                                if (returnedItems) {
                                        sendContentUpdates();
                                }
                                return true;
                        }

                        if (id == BUTTON_BUY && player instanceof ServerPlayerEntity serverPlayer) {
                                if (processPurchase(serverPlayer, this.selectedOfferIndex, false)) {
                                        sendContentUpdates();
                                }
                                return true;
                        }
                }

                return super.onButtonClick(player, id);
        }
        public boolean hasSellableItem() {
                for (int slot = 0; slot < MarketBlockEntity.INVENTORY_SIZE; slot++) {
                        if (MarketBlockEntity.isSellable(this.inventory.getStack(slot))) {
                                return true;
                        }
                }
                return false;
        }
        @Override
        public void onContentChanged(Inventory inventory) {
                super.onContentChanged(inventory);
                if (inventory == this.costInventory) {
                        GearShopOffer offer = getSelectedOffer();
                        if (updateResultSlot(offer) && !this.playerInventory.player.getWorld().isClient) {
                                sendContentUpdates();
                        }
                }
        }
        private boolean setSelectedOffer(ServerPlayerEntity player, int offerIndex) {
                if (offerIndex < 0 || offerIndex >= this.buyOffers.size()) {
                        return clearSelection(player);
                }

                GearShopOffer offer = this.buyOffers.get(offerIndex);
                boolean inventoryChanged = populateSelectedOffer(player, offer, true, true);
                boolean selectionChanged = this.selectedOfferIndex != offerIndex;
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
                boolean selectionChanged = this.selectedOfferIndex != -1;
                this.selectedOfferIndex = -1;
                return changed || selectionChanged;
        }

        private boolean populateSelectedOffer(ServerPlayerEntity player, GearShopOffer offer, boolean returnExisting,
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
                        shouldRefill = hasSufficientResources(player, playerInv, offer.costStacks());
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
        private boolean hasSufficientResources(PlayerEntity player, PlayerInventory inventory, List<ItemStack> costs) {
                if (costs == null || costs.isEmpty()) {
                        return true;
                }

                PlayerEntity owner = player != null ? player : inventory.player;
                long availableBankCoins = WalletItem.getAccessibleBankBalance(owner);
                boolean walletAvailable = WalletItem.hasUsableWallet(owner);
                long coinsNeededFromBank = 0L;

                Map<StackComparisonKey, Integer> available = countInventoryStacks(inventory);
                for (ItemStack cost : costs) {
                        if (cost == null || cost.isEmpty()) {
                                continue;
                        }

                        int required = GearShopStackHelper.getRequestedCount(cost);
                        if (required <= 0) {
                                continue;
                        }

                        ItemStack comparison = GearShopStackHelper.copyWithoutRequestedCount(cost);
                        if (comparison.isEmpty()) {
                                continue;
                        }

                        int valuePerItem = WalletItem.getCurrencyValuePerItem(comparison.getItem());
                        if (valuePerItem > 0) {
                                long coinsRequired = WalletItem.getCurrencyValue(comparison.getItem(), required);
                                StackComparisonKey key = StackComparisonKey.of(comparison);
                                int availableCount = available.getOrDefault(key, 0);
                                if (availableCount > 0) {
                                        int used = Math.min(availableCount, required);
                                        available.put(key, availableCount - used);
                                        long coinsFromInventory = WalletItem.getCurrencyValue(comparison.getItem(), used);
                                        coinsRequired = Math.max(0L, coinsRequired - coinsFromInventory);
                                }

                                if (coinsRequired <= 0) {
                                        continue;
                                }

                                if (!walletAvailable) {
                                        return false;
                                }

                                coinsNeededFromBank = addClamped(coinsNeededFromBank, coinsRequired);
                                if (coinsNeededFromBank > availableBankCoins) {
                                        return false;
                                }
                                continue;
                        }

                        StackComparisonKey key = StackComparisonKey.of(comparison);
                        int availableCount = available.getOrDefault(key, 0);
                        if (availableCount < required) {
                                return false;
                        }

                        available.put(key, availableCount - required);
                }

                return coinsNeededFromBank <= availableBankCoins;
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

        private boolean updateResultSlot(GearShopOffer offer) {
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

                int provided = Math.max(GearShopStackHelper.getRequestedCount(removed), removed.getCount());
                int requested = Math.max(GearShopStackHelper.getRequestedCount(originalCopy), provided);
                if (requested <= 0) {
                        requested = provided;
                }

                ItemStack comparison = GearShopStackHelper.copyWithoutRequestedCount(originalCopy);
                if (comparison.isEmpty()) {
                        comparison = GearShopStackHelper.copyWithoutRequestedCount(removed);
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

                int required = GearShopStackHelper.getRequestedCount(template);
                if (required <= 0) {
                        boolean slotChanged = !previous.isEmpty();
                        if (slotChanged) {
                                this.costInventory.setStack(slotIndex, ItemStack.EMPTY);
                        }
                        return new ExtractResult(false, slotChanged);
                }

                ItemStack comparison = GearShopStackHelper.copyWithoutRequestedCount(template);
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
                                || GearShopStackHelper.getRequestedCount(previous) != GearShopStackHelper
                                                .getRequestedCount(collected);
                this.costInventory.setStack(slotIndex, collected);
                return new ExtractResult(extracted.playerChanged(), slotChanged);
        }

        private ExtractionResult extractMatchingStacks(PlayerInventory playerInventory, ItemStack comparison, int required) {
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

                GearShopStackHelper.applyRequestedCount(collected, totalCollected);
                return new ExtractionResult(collected, true);
        }
        private boolean costSlotsMatchOffer(GearShopOffer offer) {
                if (offer == null) {
                        return false;
                }

                PlayerEntity player = this.playerInventory.player;
                long availableBankCoins = WalletItem.getAccessibleBankBalance(player);
                boolean walletAvailable = WalletItem.hasUsableWallet(player);

                List<ItemStack> costs = offer.costStacks();
                for (int slotIndex = 0; slotIndex < COST_SLOT_COUNT; slotIndex++) {
                        ItemStack required = slotIndex < costs.size() ? costs.get(slotIndex) : ItemStack.EMPTY;
                        ItemStack provided = this.costInventory.getStack(slotIndex);

                        if (required == null || required.isEmpty()) {
                                if (!provided.isEmpty()) {
                                        return false;
                                }
                                continue;
                        }

                        ItemStack comparison = GearShopStackHelper.copyWithoutRequestedCount(required);
                        int requiredCount = GearShopStackHelper.getRequestedCount(required);
                        if (requiredCount <= 0) {
                                if (!provided.isEmpty()) {
                                        return false;
                                }
                                continue;
                        }

                        int valuePerItem = WalletItem.getCurrencyValuePerItem(comparison.getItem());
                        if (valuePerItem > 0) {
                                long coinsRequired = WalletItem.getCurrencyValue(comparison.getItem(), requiredCount);
                                long coinsProvided = 0L;
                                if (!provided.isEmpty()) {
                                        if (!canCombineIgnoringRequestedCount(provided, required)) {
                                                return false;
                                        }
                                        int providedCount = GearShopStackHelper.getRequestedCount(provided);
                                        coinsProvided = WalletItem.getCurrencyValue(comparison.getItem(), providedCount);
                                }

                                if (coinsProvided >= coinsRequired) {
                                        continue;
                                }

                                long coinsNeeded = coinsRequired - coinsProvided;
                                if (!walletAvailable || availableBankCoins < coinsNeeded) {
                                        return false;
                                }

                                availableBankCoins -= coinsNeeded;
                                continue;
                        }

                        if (provided.isEmpty() || !canCombineIgnoringRequestedCount(provided, required)) {
                                return false;
                        }

                        int providedCount = GearShopStackHelper.getRequestedCount(provided);
                        if (providedCount < requiredCount) {
                                return false;
                        }
                }

                return true;
        }

        private CostRemovalResult removeCostStacks(ServerPlayerEntity player, List<ItemStack> costs,
                        PlayerInventory playerInventory) {
                boolean costSlotsChanged = false;
                Map<Integer, ItemStack> originalCostSlots = new HashMap<>();
                Map<Integer, ItemStack> originalPlayerSlots = new HashMap<>();
                long withdrawnCoins = 0L;
                boolean success = true;

                for (int index = 0; index < costs.size(); index++) {
                        ItemStack cost = costs.get(index);
                        if (cost.isEmpty()) {
                                continue;
                        }

                        int required = GearShopStackHelper.getRequestedCount(cost);
                        if (required <= 0) {
                                continue;
                        }

                        ItemStack comparisonStack = GearShopStackHelper.copyWithoutRequestedCount(cost);
                        int valuePerItem = WalletItem.getCurrencyValuePerItem(comparisonStack.getItem());
                        if (valuePerItem > 0) {
                                long coinsRequired = WalletItem.getCurrencyValue(comparisonStack.getItem(), required);
                                int remainingItems = required;
                                if (index < COST_SLOT_COUNT) {
                                        originalCostSlots.putIfAbsent(index,
                                                        this.costInventory.getStack(index).copy());
                                        SlotConsumptionResult result = consumeCostSlot(index, comparisonStack, required);
                                        remainingItems = result.remaining();
                                        if (result.changed()) {
                                                costSlotsChanged = true;
                                        }
                                        int consumed = required - remainingItems;
                                        if (consumed > 0) {
                                                long coinsFromSlot = WalletItem.getCurrencyValue(comparisonStack.getItem(), consumed);
                                                coinsRequired = Math.max(0L, coinsRequired - coinsFromSlot);
                                        }
                                }

                                if (remainingItems > 0) {
                                        int remainingAfterInventory = removeFromInventory(playerInventory, comparisonStack,
                                                        remainingItems, originalPlayerSlots);
                                        int consumed = remainingItems - remainingAfterInventory;
                                        if (consumed > 0) {
                                                long coinsFromInventory = WalletItem.getCurrencyValue(comparisonStack.getItem(), consumed);
                                                coinsRequired = Math.max(0L, coinsRequired - coinsFromInventory);
                                        }
                                        remainingItems = remainingAfterInventory;
                                }

                                if (coinsRequired > 0 && player != null) {
                                        if (!WalletItem.withdrawFromBank(player, coinsRequired)) {
                                                success = false;
                                                break;
                                        }
                                        withdrawnCoins += coinsRequired;
                                }

                                continue;
                        }

                        int remaining = required;
                        if (index < COST_SLOT_COUNT) {
                                originalCostSlots.putIfAbsent(index, this.costInventory.getStack(index).copy());
                                SlotConsumptionResult result = consumeCostSlot(index, comparisonStack, required);
                                remaining = result.remaining();
                                if (result.changed()) {
                                        costSlotsChanged = true;
                                }
                        }
                        if (remaining > 0) {
                                remaining = removeFromInventory(playerInventory, comparisonStack, remaining, originalPlayerSlots);
                        }
                        if (remaining > 0) {
                                success = false;
                                break;
                        }
                }
                if (!success) {
                        boolean touchedCostSlots = costSlotsChanged || !originalCostSlots.isEmpty();
                        restoreInventoryStacks(this.costInventory, originalCostSlots);
                        restoreInventoryStacks(playerInventory, originalPlayerSlots);
                        if (withdrawnCoins > 0 && player != null) {
                                WalletItem.depositToBank(player, withdrawnCoins);
                        }
                        return new CostRemovalResult(false, touchedCostSlots);
                }
                return new CostRemovalResult(true, costSlotsChanged);
        }

        private SlotConsumptionResult consumeCostSlot(int slotIndex, ItemStack comparisonStack, int required) {
                if (required <= 0) {
                        return new SlotConsumptionResult(0, false);
                }

                ItemStack slotStack = this.costInventory.getStack(slotIndex);
                if (slotStack.isEmpty()) {
                        return new SlotConsumptionResult(required, false);
                }

                ItemStack comparisonSource = GearShopStackHelper.copyWithoutRequestedCount(slotStack);
                if (!ItemStack.canCombine(comparisonSource, comparisonStack)) {
                        return new SlotConsumptionResult(required, false);
                }

                int provided = GearShopStackHelper.getRequestedCount(slotStack);
                if (provided <= 0) {
                        this.costInventory.setStack(slotIndex, ItemStack.EMPTY);
                        return new SlotConsumptionResult(required, true);
                }

                int consumed = Math.min(provided, required);
                int leftover = provided - consumed;
                if (leftover > 0) {
                        ItemStack replacement = GearShopStackHelper.copyWithoutRequestedCount(slotStack);
                        GearShopStackHelper.applyRequestedCount(replacement, leftover);
                        this.costInventory.setStack(slotIndex, replacement);
                } else {
                        this.costInventory.setStack(slotIndex, ItemStack.EMPTY);
                }

                return new SlotConsumptionResult(required - consumed, true);
        }

        private int removeFromInventory(Inventory inventory, ItemStack comparisonStack, int remaining,
                        Map<Integer, ItemStack> originalStacks) {
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
                                comparisonSource = GearShopStackHelper.copyWithoutRequestedCount(stack);
                        }
                        if (!ItemStack.canCombine(comparisonSource, comparisonStack)) {
                                continue;
                        }

                        if (inventory == this.costInventory) {
                                int requested = GearShopStackHelper.getRequestedCount(stack);
                                if (requested <= 0) {
                                        inventory.setStack(slot, ItemStack.EMPTY);
                                        changed = true;
                                        continue;
                                }

                                int taken = Math.min(requested, remaining);
                                if (taken <= 0) {
                                        continue;
                                }

                                if (originalStacks != null) {
                                        originalStacks.putIfAbsent(slot, stack.copy());
                                }
                                remaining -= taken;
                                changed = true;

                                int leftover = requested - taken;
                                if (leftover > 0) {
                                        ItemStack replacement = GearShopStackHelper.copyWithoutRequestedCount(stack);
                                        GearShopStackHelper.applyRequestedCount(replacement, leftover);
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

                        if (originalStacks != null) {
                                originalStacks.putIfAbsent(slot, stack.copy());
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

        private void restoreInventoryStacks(Inventory inventory, Map<Integer, ItemStack> originalStacks) {
                if (originalStacks == null || originalStacks.isEmpty()) {
                        return;
                }

                for (Map.Entry<Integer, ItemStack> entry : originalStacks.entrySet()) {
                        ItemStack original = entry.getValue();
                        if (original == null) {
                                inventory.setStack(entry.getKey(), ItemStack.EMPTY);
                        } else {
                                inventory.setStack(entry.getKey(), original.copy());
                        }
                }

                inventory.markDirty();
        }
        private boolean processPurchase(ServerPlayerEntity player, int offerIndex, boolean resultTakenFromSlot) {
                if (offerIndex < 0 || offerIndex >= this.buyOffers.size()) {
                        return false;
                }

                GearShopOffer offer = this.buyOffers.get(offerIndex);
                PlayerInventory playerInv = player.getInventory();
                if (!costSlotsMatchOffer(offer)) {
                        return false;
                }

                CostRemovalResult removalResult = removeCostStacks(player, offer.costStacks(), playerInv);
                if (!removalResult.success()) {
                        playerInv.markDirty();
                        if (removalResult.costSlotsChanged()) {
                                this.costInventory.markDirty();
                        }
                        return false;
                }

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
                if (removalResult.costSlotsChanged()) {
                        this.costInventory.markDirty();
                }
                return true;
        }

        private GearShopOffer getSelectedOffer() {
                if (this.selectedOfferIndex < 0 || this.selectedOfferIndex >= this.buyOffers.size()) {
                        return null;
                }
                return this.buyOffers.get(this.selectedOfferIndex);
        }

        private static boolean canCombineIgnoringRequestedCount(ItemStack first, ItemStack second) {
                ItemStack firstComparison = GearShopStackHelper.copyWithoutRequestedCount(first);
                ItemStack secondComparison = GearShopStackHelper.copyWithoutRequestedCount(second);
                return ItemStack.canCombine(firstComparison, secondComparison);
        }

        private record ExtractResult(boolean playerChanged, boolean slotChanged) {
        }

        private record ExtractionResult(ItemStack collected, boolean playerChanged) {
        }

        private record CostReturnResult(boolean playerChanged, boolean slotChanged, ItemStack returnedStack) {
                static final CostReturnResult NO_CHANGE = new CostReturnResult(false, false, ItemStack.EMPTY);
        }

        private record CostRemovalResult(boolean success, boolean costSlotsChanged) {
        }

        private record SlotConsumptionResult(int remaining, boolean changed) {
        }

        private record StackComparisonKey(Item item, NbtCompound components) {
                static StackComparisonKey of(ItemStack stack) {
                        if (stack == null || stack.isEmpty()) {
                                return null;
                        }

                        ItemStack sanitized = GearShopStackHelper.copyWithoutRequestedCount(stack);
                        if (sanitized.isEmpty()) {
                                return null;
                        }

                        Item item = sanitized.getItem();
                        NbtCompound nbt = sanitized.hasNbt() ? sanitized.getNbt().copy() : null;
                        return new StackComparisonKey(item, nbt);
                }
        }

        private static long addClamped(long first, long second) {
                try {
                        return Math.addExact(first, second);
                } catch (ArithmeticException overflow) {
                        return Long.MAX_VALUE;
                }
        }
        private void addPlayerInventory(PlayerInventory playerInventory) {
                final int baseY = 138;
                for (int row = 0; row < PLAYER_INVENTORY_ROW_COUNT; ++row) {
                        for (int column = 0; column < PLAYER_INVENTORY_COLUMN_COUNT; ++column) {
                                this.addSlot(new Slot(playerInventory, column + row * PLAYER_INVENTORY_COLUMN_COUNT + HOTBAR_SLOT_COUNT,
                                                8 + column * SLOT_SIZE, baseY + row * SLOT_SIZE));
                        }
                }
        }

        private void addPlayerHotbar(PlayerInventory playerInventory) {
                final int hotbarY = 196;
                for (int slot = 0; slot < HOTBAR_SLOT_COUNT; ++slot) {
                        this.addSlot(new Slot(playerInventory, slot, 8 + slot * SLOT_SIZE, hotbarY));
                }
        }
        private static class MarketSellSlot extends Slot {
                private boolean enabled = true;

                protected MarketSellSlot(Inventory inventory, int index, int x, int y) {
                        super(inventory, index, x, y);
                }

                @Override
                public boolean canInsert(ItemStack stack) {
                        return this.enabled && MarketBlockEntity.isSellable(stack);
                }

                @Override
                public boolean canTakeItems(PlayerEntity player) {
                        return this.enabled && super.canTakeItems(player);
                }

                @Override
                public boolean isEnabled() {
                        return this.enabled;
                }

                public void setEnabled(boolean enabled) {
                        this.enabled = enabled;
                }
        }

        private static class MarketCostSlot extends Slot {
                private boolean enabled = false;

                protected MarketCostSlot(Inventory inventory, int index, int x, int y) {
                        super(inventory, index, x, y);
                }

                public void setEnabled(boolean enabled) {
                        this.enabled = enabled;
                }

                @Override
                public boolean canInsert(ItemStack stack) {
                        return this.enabled;
                }

                @Override
                public boolean canTakeItems(PlayerEntity player) {
                        return this.enabled;
                }

                @Override
                public boolean isEnabled() {
                        return this.enabled;
                }
        }

        private static class MarketResultSlot extends Slot {
                private final MarketScreenHandler handler;
                private boolean enabled;

                public MarketResultSlot(MarketScreenHandler handler, Inventory inventory, int index, int x, int y) {
                        super(inventory, index, x, y);
                        this.handler = handler;
                        this.enabled = false;
                }

                public void setEnabled(boolean enabled) {
                        this.enabled = enabled;
                }

                @Override
                public boolean canInsert(ItemStack stack) {
                        return false;
                }

                @Override
                public boolean canTakeItems(PlayerEntity player) {
                        return this.enabled && !getStack().isEmpty();
                }

                @Override
                public boolean isEnabled() {
                        return this.enabled;
                }

                @Override
                public void onTakeItem(PlayerEntity player, ItemStack stack) {
                        boolean purchased = false;
                        if (this.enabled && player instanceof ServerPlayerEntity serverPlayer) {
                                purchased = this.handler.processPurchase(serverPlayer, this.handler.selectedOfferIndex, true);
                        }
                        super.onTakeItem(player, stack);
                        if (purchased) {
                                this.handler.sendContentUpdates();
                        }
                }
        }
}
