package net.jeremy.gardenkingmod.screen;

import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.block.entity.GardenShopBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class GardenShopScreenHandler extends ScreenHandler {
        private static final int HOTBAR_SLOT_COUNT = 9;
        private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
        private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
        private static final int SHOP_SLOTS_PER_ROW = 9;
        private static final int SLOT_SIZE = 18;
        private static final int SHOP_SLOT_START_X = 8;
        private static final int SHOP_SLOT_START_Y = 18;
        private static final int PLAYER_INVENTORY_START_Y = 84;
        private static final int PLAYER_HOTBAR_Y = 142;

        private final Inventory inventory;
        private final GardenShopBlockEntity blockEntity;

        public GardenShopScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
                this(syncId, playerInventory, getBlockEntity(playerInventory, buf.readBlockPos()));
        }

        public GardenShopScreenHandler(int syncId, PlayerInventory playerInventory, GardenShopBlockEntity blockEntity) {
                super(ModScreenHandlers.GARDEN_SHOP_SCREEN_HANDLER, syncId);
                this.blockEntity = blockEntity;
                this.inventory = blockEntity != null ? blockEntity : new SimpleInventory(GardenShopBlockEntity.INVENTORY_SIZE);

                checkSize(this.inventory, GardenShopBlockEntity.INVENTORY_SIZE);
                this.inventory.onOpen(playerInventory.player);

                addGardenShopInventory();
                addPlayerInventory(playerInventory);
                addPlayerHotbar(playerInventory);
        }

        private void addGardenShopInventory() {
                for (int slotIndex = 0; slotIndex < GardenShopBlockEntity.INVENTORY_SIZE; ++slotIndex) {
                        int column = slotIndex % SHOP_SLOTS_PER_ROW;
                        int row = slotIndex / SHOP_SLOTS_PER_ROW;
                        int x = SHOP_SLOT_START_X + column * SLOT_SIZE;
                        int y = SHOP_SLOT_START_Y + row * SLOT_SIZE;
                        this.addSlot(new Slot(this.inventory, slotIndex, x, y));
                }
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
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int index) {
                ItemStack newStack = ItemStack.EMPTY;
                Slot slot = this.slots.get(index);
                if (slot != null && slot.hasStack()) {
                        ItemStack originalStack = slot.getStack();
                        newStack = originalStack.copy();
                        if (index < GardenShopBlockEntity.INVENTORY_SIZE) {
                                if (!this.insertItem(originalStack, GardenShopBlockEntity.INVENTORY_SIZE, this.slots.size(), true)) {
                                        return ItemStack.EMPTY;
                                }
                        } else if (!this.insertItem(originalStack, 0, GardenShopBlockEntity.INVENTORY_SIZE, false)) {
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

        public Inventory getInventory() {
                return this.inventory;
        }

        private void addPlayerInventory(PlayerInventory playerInventory) {
                for (int row = 0; row < PLAYER_INVENTORY_ROW_COUNT; ++row) {
                        for (int column = 0; column < PLAYER_INVENTORY_COLUMN_COUNT; ++column) {
                                int x = SHOP_SLOT_START_X + column * SLOT_SIZE;
                                int y = PLAYER_INVENTORY_START_Y + row * SLOT_SIZE;
                                this.addSlot(new Slot(playerInventory, column + row * PLAYER_INVENTORY_COLUMN_COUNT + HOTBAR_SLOT_COUNT,
                                                x, y));
                        }
                }
        }

        private void addPlayerHotbar(PlayerInventory playerInventory) {
                for (int slot = 0; slot < HOTBAR_SLOT_COUNT; ++slot) {
                        int x = SHOP_SLOT_START_X + slot * SLOT_SIZE;
                        this.addSlot(new Slot(playerInventory, slot, x, PLAYER_HOTBAR_Y));
                }
        }
}
