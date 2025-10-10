package net.jeremy.gardenkingmod.screen;

import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.block.entity.MarketBlockEntity;
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

public class MarketScreenHandler extends ScreenHandler {
        private final Inventory inventory;
        private final MarketBlockEntity blockEntity;

        public MarketScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
                this(syncId, playerInventory, getBlockEntity(playerInventory, buf.readBlockPos()));
        }

        public MarketScreenHandler(int syncId, PlayerInventory playerInventory, MarketBlockEntity blockEntity) {
                super(ModScreenHandlers.MARKET_SCREEN_HANDLER, syncId);
                this.blockEntity = blockEntity;
                this.inventory = blockEntity != null ? blockEntity : new SimpleInventory(MarketBlockEntity.INVENTORY_SIZE);

                checkSize(this.inventory, MarketBlockEntity.INVENTORY_SIZE);
                this.inventory.onOpen(playerInventory.player);

                addMarketInventory();

                addPlayerInventory(playerInventory);
                addPlayerHotbar(playerInventory);
        }

        private void addMarketInventory() {
                final int slotsPerRow = 9;
                final int slotSize = 18;
                final int startX = 8;
                final int startY = 34;

                for (int slotIndex = 0; slotIndex < MarketBlockEntity.INVENTORY_SIZE; ++slotIndex) {
                        int column = slotIndex % slotsPerRow;
                        int row = slotIndex / slotsPerRow;
                        int x = startX + column * slotSize;
                        int y = startY + row * slotSize;
                        this.addSlot(new Slot(this.inventory, slotIndex, x, y) {
                                @Override
                                public boolean canInsert(ItemStack stack) {
                                        return MarketBlockEntity.isSellable(stack);
                                }
                        });
                }
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
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int index) {
                ItemStack newStack = ItemStack.EMPTY;
                Slot slot = this.slots.get(index);
                if (slot != null && slot.hasStack()) {
                        ItemStack originalStack = slot.getStack();
                        newStack = originalStack.copy();
                        if (index < MarketBlockEntity.INVENTORY_SIZE) {
                                if (!this.insertItem(originalStack, MarketBlockEntity.INVENTORY_SIZE, this.slots.size(),
                                                true)) {
                                        return ItemStack.EMPTY;
                                }
                        } else if (!MarketBlockEntity.isSellable(originalStack)
                                        || !this.insertItem(originalStack, 0, MarketBlockEntity.INVENTORY_SIZE, false)) {
                                return ItemStack.EMPTY;
                        }

                        if (originalStack.isEmpty()) {
                                slot.setStack(ItemStack.EMPTY);
                        } else {
                                slot.markDirty();
                        }
                }

                return newStack;
        }

        @Override
        public boolean onButtonClick(PlayerEntity player, int id) {
                if (id == 0 && blockEntity != null && player instanceof ServerPlayerEntity serverPlayer) {
                        if (blockEntity.sell(serverPlayer)) {
                                sendContentUpdates();
                        }
                        return true;
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

        private void addPlayerInventory(PlayerInventory playerInventory) {
                final int baseY = 138;
                for (int row = 0; row < 3; ++row) {
                        for (int column = 0; column < 9; ++column) {
                                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 7 + column * 18,
                                                baseY + row * 18));
                        }
                }
        }

        private void addPlayerHotbar(PlayerInventory playerInventory) {
                final int hotbarY = 196;
                for (int slot = 0; slot < 9; ++slot) {
                        this.addSlot(new Slot(playerInventory, slot, 7 + slot * 18, hotbarY));
                }
        }
}
