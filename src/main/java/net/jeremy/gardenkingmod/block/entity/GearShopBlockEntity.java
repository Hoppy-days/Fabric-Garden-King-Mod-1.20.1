package net.jeremy.gardenkingmod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.screen.GearShopScreenHandler;
import net.jeremy.gardenkingmod.shop.GearShopOffer;
import net.jeremy.gardenkingmod.shop.GearShopOfferManager;
import net.jeremy.gardenkingmod.shop.GearShopStackHelper;
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
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

public class GearShopBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, Inventory {
    public static final int INVENTORY_SIZE = 27;
    private static final String OFFERS_KEY = "Offers";
    private static final String OFFER_PAGES_KEY = "OfferPages";
    private static final String OFFER_RESULT_KEY = "Result";
    private static final String OFFER_COSTS_KEY = "Costs";
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final List<List<GearShopOffer>> offersByPage = new ArrayList<>();
    private final List<GearShopOffer> flattenedOffers = new ArrayList<>();

        public GearShopBlockEntity(BlockPos pos, BlockState state) {
                super(ModBlockEntities.GEAR_SHOP_BLOCK_ENTITY, pos, state);
        }

        @Override
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        ensureOffers();
        buf.writeBlockPos(getPos());
        buf.writeVarInt(offersByPage.size());
        for (List<GearShopOffer> page : offersByPage) {
            buf.writeVarInt(page.size());
            for (GearShopOffer offer : page) {
                buf.writeItemStack(offer.copyResultStack());
                List<ItemStack> costs = offer.copyCostStacks();
                buf.writeVarInt(costs.size());
                for (ItemStack cost : costs) {
                    buf.writeItemStack(cost);
                    buf.writeVarInt(GearShopStackHelper.getRequestedCount(cost));
                }
            }
        }
    }

        @Override
        public Text getDisplayName() {
                return Text.translatable("container.gardenkingmod.gear_shop");
        }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        ensureOffers();
        return new GearShopScreenHandler(syncId, playerInventory, this);
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
        NbtList pagesList = new NbtList();
        for (List<GearShopOffer> page : offersByPage) {
            NbtCompound pageNbt = new NbtCompound();
            NbtList pageOffers = new NbtList();
            for (GearShopOffer offer : page) {
                NbtCompound offerNbt = writeOfferNbt(offer);
                pageOffers.add(offerNbt.copy());
                offerList.add(offerNbt);
            }
            pageNbt.put(OFFERS_KEY, pageOffers);
            pagesList.add(pageNbt);
        }
        if (!pagesList.isEmpty()) {
            nbt.put(OFFER_PAGES_KEY, pagesList);
        }
        nbt.put(OFFERS_KEY, offerList);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(nbt, items);
        offersByPage.clear();
        if (nbt.contains(OFFER_PAGES_KEY, NbtElement.LIST_TYPE)) {
            NbtList pagesList = nbt.getList(OFFER_PAGES_KEY, NbtElement.COMPOUND_TYPE);
            for (int pageIndex = 0; pageIndex < pagesList.size(); pageIndex++) {
                NbtCompound pageNbt = pagesList.getCompound(pageIndex);
                List<GearShopOffer> pageOffers = readOffersFromNbt(pageNbt.getList(OFFERS_KEY, NbtElement.COMPOUND_TYPE));
                offersByPage.add(pageOffers);
            }
        }

        if (offersByPage.isEmpty() && nbt.contains(OFFERS_KEY, NbtElement.LIST_TYPE)) {
            List<GearShopOffer> legacyOffers = readOffersFromNbt(nbt.getList(OFFERS_KEY, NbtElement.COMPOUND_TYPE));
            if (!legacyOffers.isEmpty()) {
                offersByPage.add(legacyOffers);
            }
        }
        syncItemsFromOffers();
    }

    public List<GearShopOffer> getOffers() {
        return Collections.unmodifiableList(flattenedOffers);
    }

    public List<GearShopOffer> getOffersForPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= offersByPage.size()) {
            return List.of();
        }

        return Collections.unmodifiableList(offersByPage.get(pageIndex));
    }

    public List<List<GearShopOffer>> getOfferPages() {
        List<List<GearShopOffer>> snapshot = new ArrayList<>(offersByPage.size());
        for (List<GearShopOffer> page : offersByPage) {
            snapshot.add(List.copyOf(page));
        }
        return List.copyOf(snapshot);
    }

    public int getPageCount() {
        return offersByPage.size();
    }

    public void ensureOffers() {
        List<List<GearShopOffer>> configuredPages = GearShopOfferManager.getInstance().getOfferPages();
        if (offersMatch(configuredPages)) {
            syncItemsFromOffers();
            return;
        }

        offersByPage.clear();
        for (List<GearShopOffer> page : configuredPages) {
            offersByPage.add(new ArrayList<>(page));
        }
        syncItemsFromOffers();
        markDirty();
    }

    private void syncItemsFromOffers() {
        flattenedOffers.clear();
        for (List<GearShopOffer> page : offersByPage) {
            flattenedOffers.addAll(page);
        }

        for (int index = 0; index < items.size(); index++) {
            ItemStack stack = index < flattenedOffers.size() ? flattenedOffers.get(index).copyResultStack() : ItemStack.EMPTY;
            items.set(index, stack);
        }
    }

    private boolean offersMatch(List<List<GearShopOffer>> configuredPages) {
        if (offersByPage.size() != configuredPages.size()) {
            return false;
        }

        for (int index = 0; index < offersByPage.size(); index++) {
            if (!offersByPage.get(index).equals(configuredPages.get(index))) {
                return false;
            }
        }

        return true;
    }

    private NbtCompound writeOfferNbt(GearShopOffer offer) {
        NbtCompound offerNbt = new NbtCompound();
        offerNbt.put(OFFER_RESULT_KEY, offer.copyResultStack().writeNbt(new NbtCompound()));
        NbtList costsList = new NbtList();
        for (ItemStack cost : offer.copyCostStacks()) {
            costsList.add(cost.writeNbt(new NbtCompound()));
        }
        offerNbt.put(OFFER_COSTS_KEY, costsList);
        return offerNbt;
    }

    private List<GearShopOffer> readOffersFromNbt(NbtList list) {
        List<GearShopOffer> offers = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            NbtCompound offerNbt = list.getCompound(index);
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

            offers.add(GearShopOffer.of(result, costs));
        }
        return offers;
    }

    private ItemStack createStack(Identifier itemId, int count) {
        return Registries.ITEM.getOrEmpty(itemId)
                .map(item -> {
                    ItemStack stack = new ItemStack(item);
                    GearShopStackHelper.applyRequestedCount(stack, count);
                    return stack;
                })
                .orElse(ItemStack.EMPTY);
    }
}
