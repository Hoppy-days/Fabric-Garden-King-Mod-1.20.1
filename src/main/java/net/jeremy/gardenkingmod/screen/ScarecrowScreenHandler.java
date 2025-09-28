package net.jeremy.gardenkingmod.screen;

import java.util.function.Predicate;

import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.block.ward.ScarecrowBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class ScarecrowScreenHandler extends ScreenHandler {
        public static final int EQUIPMENT_SLOT_COUNT = ScarecrowBlockEntity.INVENTORY_SIZE;
        public static final int HAT_SLOT_X = 44;
        public static final int HAT_SLOT_Y = 28;
        public static final int HEAD_SLOT_X = 98;
        public static final int HEAD_SLOT_Y = 28;
        public static final int CHEST_SLOT_X = 44;
        public static final int CHEST_SLOT_Y = 82;
        public static final int PITCHFORK_SLOT_X = 98;
        public static final int PITCHFORK_SLOT_Y = 82;
        public static final int PLAYER_INVENTORY_START_Y = 124;
        public static final int PLAYER_HOTBAR_Y = 182;

        private final Inventory inventory;
        private final PropertyDelegate properties;
        private final ScarecrowBlockEntity blockEntity;

        public ScarecrowScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
                this(syncId, playerInventory, getBlockEntity(playerInventory, buf.readBlockPos()));
        }

        public ScarecrowScreenHandler(int syncId, PlayerInventory playerInventory, ScarecrowBlockEntity blockEntity) {
                this(syncId, playerInventory, blockEntity,
                                blockEntity != null ? blockEntity.getPropertyDelegate() : new ArrayPropertyDelegate(4));
        }

        public ScarecrowScreenHandler(int syncId, PlayerInventory playerInventory, ScarecrowBlockEntity blockEntity,
                        PropertyDelegate properties) {
                super(ModScreenHandlers.SCARECROW_SCREEN_HANDLER, syncId);
                this.blockEntity = blockEntity;
                this.inventory = blockEntity != null ? blockEntity : new SimpleInventory(ScarecrowBlockEntity.INVENTORY_SIZE);
                this.properties = properties;

                checkSize(this.inventory, ScarecrowBlockEntity.INVENTORY_SIZE);
                addProperties(properties);

                this.inventory.onOpen(playerInventory.player);

                this.addSlot(createEquipmentSlot(ScarecrowBlockEntity.SLOT_HAT, HAT_SLOT_X, HAT_SLOT_Y,
                                ScarecrowBlockEntity::isValidHatItem));
                this.addSlot(createEquipmentSlot(ScarecrowBlockEntity.SLOT_HEAD, HEAD_SLOT_X, HEAD_SLOT_Y,
                                ScarecrowBlockEntity::isValidHeadItem));
                this.addSlot(createEquipmentSlot(ScarecrowBlockEntity.SLOT_CHEST, CHEST_SLOT_X, CHEST_SLOT_Y,
                                ScarecrowBlockEntity::isValidChestItem));
                this.addSlot(createEquipmentSlot(ScarecrowBlockEntity.SLOT_PITCHFORK, PITCHFORK_SLOT_X,
                                PITCHFORK_SLOT_Y, ScarecrowBlockEntity::isValidPitchforkItem));

                addPlayerInventory(playerInventory);
                addPlayerHotbar(playerInventory);
        }

        private Slot createEquipmentSlot(int slotIndex, int x, int y, Predicate<ItemStack> validator) {
                return new Slot(this.inventory, slotIndex, x, y) {
                        @Override
                        public boolean canInsert(ItemStack stack) {
                                return validator.test(stack);
                        }

                        @Override
                        public int getMaxItemCount() {
                                return 1;
                        }
                };
        }

        private static ScarecrowBlockEntity getBlockEntity(PlayerInventory playerInventory, BlockPos pos) {
                if (playerInventory.player.getWorld().getBlockEntity(pos) instanceof ScarecrowBlockEntity scarecrow) {
                        return scarecrow;
                }
                return null;
        }

        @Override
        public boolean canUse(PlayerEntity player) {
                return this.inventory.canPlayerUse(player);
        }

        @Override
        public void onClosed(PlayerEntity player) {
                super.onClosed(player);
                this.inventory.onClose(player);
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int slotIndex) {
                ItemStack newStack = ItemStack.EMPTY;
                Slot slot = this.slots.get(slotIndex);
                if (slot != null && slot.hasStack()) {
                        ItemStack original = slot.getStack();
                        newStack = original.copy();
                        if (slotIndex < EQUIPMENT_SLOT_COUNT) {
                                if (!this.insertItem(original, ScarecrowBlockEntity.INVENTORY_SIZE, this.slots.size(), true)) {
                                        return ItemStack.EMPTY;
                                }
                        } else if (!this.insertItem(original, ScarecrowBlockEntity.SLOT_HAT,
                                        ScarecrowBlockEntity.SLOT_HAT + 1, false)
                                        && !this.insertItem(original, ScarecrowBlockEntity.SLOT_HEAD,
                                                        ScarecrowBlockEntity.SLOT_HEAD + 1, false)
                                        && !this.insertItem(original, ScarecrowBlockEntity.SLOT_CHEST,
                                                        ScarecrowBlockEntity.SLOT_CHEST + 1, false)
                                        && !this.insertItem(original, ScarecrowBlockEntity.SLOT_PITCHFORK,
                                                        ScarecrowBlockEntity.SLOT_PITCHFORK + 1, false)) {
                                return ItemStack.EMPTY;
                        }

                        if (original.isEmpty()) {
                                slot.setStack(ItemStack.EMPTY);
                        } else {
                                slot.markDirty();
                        }
                }
                return newStack;
        }

        public Inventory getInventory() {
                return inventory;
        }

        private void addPlayerInventory(PlayerInventory playerInventory) {
                for (int row = 0; row < 3; ++row) {
                        for (int column = 0; column < 9; ++column) {
                                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18,
                                                PLAYER_INVENTORY_START_Y + row * 18));
                        }
                }
        }

        private void addPlayerHotbar(PlayerInventory playerInventory) {
                for (int slot = 0; slot < 9; ++slot) {
                        this.addSlot(new Slot(playerInventory, slot, 8 + slot * 18, PLAYER_HOTBAR_Y));
                }
        }

        public int getDurability() {
                return this.properties.get(0);
        }

        public int getMaxDurability() {
                return this.properties.get(1);
        }

        public int getHorizontalRadius() {
                return this.properties.get(2);
        }

        public int getVerticalRadius() {
                return this.properties.get(3);
        }
}
