package net.jeremy.gardenkingmod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.screen.GardenShopScreenHandler;
import net.jeremy.gardenkingmod.shop.GardenShopOffer;
import net.jeremy.gardenkingmod.shop.GardenShopOfferManager;
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
    private static final String OFFER_RESULT_KEY = "Result";
    private static final String OFFER_COSTS_KEY = "Costs";
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
            buf.writeItemStack(offer.copyResultStack());
            List<ItemStack> costs = offer.copyCostStacks();
            buf.writeVarInt(costs.size());
            for (ItemStack cost : costs) {
                buf.writeItemStack(cost);
            }
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
            offerNbt.put(OFFER_RESULT_KEY, offer.copyResultStack().writeNbt(new NbtCompound()));
            NbtList costsList = new NbtList();
            for (ItemStack cost : offer.copyCostStacks()) {
                costsList.add(cost.writeNbt(new NbtCompound()));
            }
            offerNbt.put(OFFER_COSTS_KEY, costsList);
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
                if (!offerNbt.contains(OFFER_RESULT_KEY, NbtElement.COMPOUND_TYPE)) {
                    continue;
                }

                ItemStack result = ItemStack.fromNbt(offerNbt.getCompound(OFFER_RESULT_KEY));
                if (result.isEmpty()) {
                    continue;
                }

                List<ItemStack> costs = new ArrayList<>();
                if (offerNbt.contains(OFFER_COSTS_KEY, NbtElement.LIST_TYPE)) {
                    NbtList costsList = offerNbt.getList(OFFER_COSTS_KEY, NbtElement.COMPOUND_TYPE);
                    for (int costIndex = 0; costIndex < costsList.size(); costIndex++) {
                        ItemStack costStack = ItemStack.fromNbt(costsList.getCompound(costIndex));
                        if (!costStack.isEmpty()) {
                            costs.add(costStack);
                        }
                    }
                }

                offers.add(GardenShopOffer.of(result, costs));
            }
        }
        syncItemsFromOffers();
    }

    public List<GardenShopOffer> getOffers() {
        return Collections.unmodifiableList(offers);
    }

    public void ensureOffers() {
        List<GardenShopOffer> configuredOffers = GardenShopOfferManager.getInstance().getOffers();
        if (offers.equals(configuredOffers)) {
            syncItemsFromOffers();
            return;
        }

        offers.clear();
        offers.addAll(configuredOffers);
        syncItemsFromOffers();
        markDirty();
    }

    private void syncItemsFromOffers() {
        for (int index = 0; index < items.size(); index++) {
            items.set(index, ItemStack.EMPTY);
        }
        for (int index = 0; index < offers.size() && index < items.size(); index++) {
            items.set(index, offers.get(index).copyResultStack());
        }
    }
}
