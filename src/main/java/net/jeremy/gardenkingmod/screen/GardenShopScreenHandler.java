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
        private static final int COST_SLOT_ONE_X = 144;
        private static final int COST_SLOT_TWO_X = 180;
        private static final int COST_SLOTS_Y = 45;

        private static final int PURCHASE_BUTTON_FLAG = 1 << 30;
        private static final int PAGE_INDEX_SHIFT = 16;
        private static final int PAGE_INDEX_MASK = 0x7FFF;
        private static final int OFFER_INDEX_MASK = 0xFFFF;

        private final Inventory inventory;
        private final GardenShopBlockEntity blockEntity;
        private final SimpleInventory costInventory;
        private final List<List<GardenShopOffer>> offersByPage;

        public static int encodePurchaseButtonId(int pageIndex, int offerIndex) {
                return PURCHASE_BUTTON_FLAG | ((pageIndex & PAGE_INDEX_MASK) << PAGE_INDEX_SHIFT)
                                | (offerIndex & OFFER_INDEX_MASK);
        }

        private static boolean isPurchaseButtonId(int id) {
                return (id & PURCHASE_BUTTON_FLAG) != 0;
        }

        private static int decodePageIndex(int id) {
                return (id >>> PAGE_INDEX_SHIFT) & PAGE_INDEX_MASK;
        }

        private static int decodeOfferIndex(int id) {
                return id & OFFER_INDEX_MASK;
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

                addCostSlots();
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
                if (!player.getWorld().isClient) {
                        this.dropInventory(player, this.costInventory);
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

                int costSlotEnd = COST_SLOT_COUNT;
                int playerInventoryStart = costSlotEnd;
                int playerInventoryEnd = playerInventoryStart + PLAYER_INVENTORY_SLOT_COUNT;
                int hotbarStart = playerInventoryEnd;
                int hotbarEnd = hotbarStart + HOTBAR_SLOT_COUNT;

                if (index < costSlotEnd) {
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

        private boolean tryPurchase(ServerPlayerEntity player, int pageIndex, int offerIndex) {
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
                ItemStack result = offer.copyResultStack();
                boolean inserted = playerInventory.insertStack(result);
                if (!inserted && !result.isEmpty()) {
                        player.dropItem(result, false);
                }
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
                                simulatedCounts[index][slot] = source.getStack(slot).getCount();
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
}
