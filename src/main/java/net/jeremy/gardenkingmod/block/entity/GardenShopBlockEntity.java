package net.jeremy.gardenkingmod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.screen.GardenShopScreenHandler;
import net.jeremy.gardenkingmod.shop.GardenShopOffer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class GardenShopBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, Inventory {
    public static final int INVENTORY_SIZE = 27;
    private static final String OFFERS_KEY = "Offers";
    private static final String OFFER_ITEM_KEY = "Item";
    private static final String OFFER_PRICE_KEY = "Price";
    private static final int TEST_OFFER_COUNT = 18;
    private static final int TEST_PRICE_PER_ITEM = 64;

    private DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final List<GardenShopOffer> offers = new ArrayList<>();

        public GardenShopBlockEntity(BlockPos pos, BlockState state) {
                super(ModBlockEntities.GARDEN_SHOP_BLOCK_ENTITY, pos, state);
        }

        @Override
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        ensureOffers();
        buf.writeBlockPos(getPos());
        buf.writeVarInt(offers.size());
        for (GardenShopOffer offer : offers) {
            buf.writeItemStack(offer.createDisplayStack());
            buf.writeVarInt(offer.price());
        }
    }

        @Override
        public Text getDisplayName() {
                return Text.translatable("container.gardenkingmod.garden_shop");
        }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        ensureOffers();
        return new GardenShopScreenHandler(syncId, playerInventory, this);
    }

        @Override
        public int size() {
                return items.size();
        }

        @Override
        public boolean isEmpty() {
                for (ItemStack stack : items) {
                        if (!stack.isEmpty()) {
                                return false;
                        }
                }

                return true;
        }

        @Override
        public ItemStack getStack(int slot) {
                return items.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
                ItemStack stack = Inventories.splitStack(items, slot, amount);
                if (!stack.isEmpty()) {
                        markDirty();
                }
                return stack;
        }

        @Override
        public ItemStack removeStack(int slot) {
                ItemStack stack = Inventories.removeStack(items, slot);
                if (!stack.isEmpty()) {
                        markDirty();
                }
                return stack;
        }

        @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
                if (stack.getCount() > getMaxCountPerStack()) {
                        stack.setCount(getMaxCountPerStack());
                }
                markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
                if (world == null || world.getBlockEntity(pos) != this) {
                        return false;
                }

                return player.squaredDistanceTo((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
                                (double) pos.getZ() + 0.5D) <= 64.0D;
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
                return true;
        }

        @Override
        public void clear() {
                for (int slot = 0; slot < items.size(); slot++) {
                        items.set(slot, ItemStack.EMPTY);
                }
        }

    @Override
    public void onClose(PlayerEntity player) {
        Inventory.super.onClose(player);
    }

        @Override
        public void markDirty() {
                super.markDirty();
                World world = getWorld();
                if (world != null) {
                        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
                        world.updateComparators(pos, getCachedState().getBlock());
                }
        }

        @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        syncItemsFromOffers();
        Inventories.writeNbt(nbt, items);
        NbtList offerList = new NbtList();
        for (GardenShopOffer offer : offers) {
            NbtCompound offerNbt = new NbtCompound();
            offerNbt.put(OFFER_ITEM_KEY, offer.createDisplayStack().writeNbt(new NbtCompound()));
            offerNbt.putInt(OFFER_PRICE_KEY, offer.price());
            offerList.add(offerNbt);
        }
        nbt.put(OFFERS_KEY, offerList);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(nbt, items);
        offers.clear();
        if (nbt.contains(OFFERS_KEY, NbtElement.LIST_TYPE)) {
            NbtList offerList = nbt.getList(OFFERS_KEY, NbtElement.COMPOUND_TYPE);
            for (int index = 0; index < offerList.size(); index++) {
                NbtCompound offerNbt = offerList.getCompound(index);
                ItemStack stack = ItemStack.fromNbt(offerNbt.getCompound(OFFER_ITEM_KEY));
                int price = offerNbt.getInt(OFFER_PRICE_KEY);
                if (!stack.isEmpty()) {
                    offers.add(new GardenShopOffer(stack, price));
                }
            }
        } else {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    offers.add(new GardenShopOffer(stack, TEST_PRICE_PER_ITEM));
                }
            }
        }
        syncItemsFromOffers();
    }

    public List<GardenShopOffer> getOffers() {
        return Collections.unmodifiableList(offers);
    }

    public void ensureOffers() {
        if (!offers.isEmpty()) {
            syncItemsFromOffers();
            return;
        }

        offers.clear();
        for (int index = 0; index < Math.min(TEST_OFFER_COUNT, INVENTORY_SIZE); index++) {
            ItemStack stack = new ItemStack(ModItems.RUBY_CHESTPLATE);
            offers.add(new GardenShopOffer(stack, TEST_PRICE_PER_ITEM));
        }
        syncItemsFromOffers();
        markDirty();
    }

    private void syncItemsFromOffers() {
        for (int index = 0; index < items.size(); index++) {
            items.set(index, ItemStack.EMPTY);
        }
        for (int index = 0; index < offers.size() && index < items.size(); index++) {
            items.set(index, offers.get(index).createDisplayStack());
        }
    }
}
