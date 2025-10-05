package net.jeremy.gardenkingmod.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.block.entity.GardenShopBlockEntity;
import net.jeremy.gardenkingmod.shop.GardenShopOffer;
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
        private static final int SLOT_SIZE = 18;
        private static final int SHOP_SLOT_START_X = 8;
        private static final int PLAYER_INVENTORY_START_Y = 116;
        private static final int PLAYER_INVENTORY_START_X = 108;
        private static final int PLAYER_HOTBAR_Y = 174;
        private static final int PLAYER_HOTBAR_X = 108;

        private final Inventory inventory;
        private final GardenShopBlockEntity blockEntity;
        private final List<GardenShopOffer> offers;

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
                this.offers = new ArrayList<>();

                checkSize(this.inventory, GardenShopBlockEntity.INVENTORY_SIZE);
                if (blockEntity != null) {
                        blockEntity.ensureOffers();
                        this.offers.addAll(blockEntity.getOffers());
                        fillInventoryFromOffers();
                }

                if (buf != null) {
                        this.offers.clear();
                        int offerCount = buf.readVarInt();
                        for (int index = 0; index < offerCount; index++) {
                                ItemStack stack = buf.readItemStack();
                                int price = buf.readVarInt();
                                if (!stack.isEmpty()) {
                                        this.offers.add(new GardenShopOffer(stack, price));
                                }
                        }
                        fillInventoryFromOffers();
                }

                this.inventory.onOpen(playerInventory.player);

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
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int index) {
                return ItemStack.EMPTY;
        }

        public Inventory getInventory() {
                return this.inventory;
        }

        public List<GardenShopOffer> getOffers() {
                return Collections.unmodifiableList(this.offers);
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

        private void fillInventoryFromOffers() {
                for (int index = 0; index < this.inventory.size(); index++) {
                        ItemStack stack = index < this.offers.size() ? this.offers.get(index).createDisplayStack() : ItemStack.EMPTY;
                        this.inventory.setStack(index, stack);
                }
        }
}
