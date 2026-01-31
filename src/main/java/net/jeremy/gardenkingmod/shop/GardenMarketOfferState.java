package net.jeremy.gardenkingmod.shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PersistentState;

public final class GardenMarketOfferState extends PersistentState {
    private static final String DATA_NAME = GardenKingMod.MOD_ID + "_garden_market_offers";
    private static final String TAG_OFFERS = "OfferIndices";
    private static final String TAG_NEXT_REFRESH = "NextRefresh";

    private final List<Integer> offerIndices = new ArrayList<>();
    private long nextRefreshTime;

    public static GardenMarketOfferState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(GardenMarketOfferState::fromNbt,
                GardenMarketOfferState::new, DATA_NAME);
    }

    public static GardenMarketOfferState fromNbt(NbtCompound nbt) {
        GardenMarketOfferState state = new GardenMarketOfferState();
        state.readNbt(nbt);
        return state;
    }

    public List<Integer> getActiveOfferIndices(ServerWorld world) {
        ensureOffers(world);
        return List.copyOf(offerIndices);
    }

    public List<GearShopOffer> getActiveOffers(ServerWorld world) {
        ensureOffers(world);
        return GardenMarketOfferManager.getInstance().getOffersByIndices(offerIndices);
    }

    public long getNextRefreshTime(ServerWorld world) {
        ensureOffers(world);
        return nextRefreshTime;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (Integer index : offerIndices) {
            if (index != null) {
                list.add(NbtInt.of(index));
            }
        }
        nbt.put(TAG_OFFERS, list);
        nbt.putLong(TAG_NEXT_REFRESH, nextRefreshTime);
        return nbt;
    }

    private void readNbt(NbtCompound nbt) {
        offerIndices.clear();
        NbtList list = nbt.getList(TAG_OFFERS, NbtElement.INT_TYPE);
        for (int i = 0; i < list.size(); i++) {
            offerIndices.add(list.getInt(i));
        }
        nextRefreshTime = nbt.getLong(TAG_NEXT_REFRESH);
    }

    private void ensureOffers(ServerWorld world) {
        GardenMarketOfferManager manager = GardenMarketOfferManager.getInstance();
        List<GearShopOffer> master = manager.getMasterOffers();
        boolean showAllMismatch = manager.shouldShowAllOffers() && offerIndices.size() < master.size();
        if (needsRefresh(world) || showAllMismatch) {
            refreshOffers(world);
        }
    }

    public long forceRefresh(ServerWorld world) {
        refreshOffers(world);
        return nextRefreshTime;
    }

    private boolean needsRefresh(ServerWorld world) {
        return offerIndices.isEmpty() || nextRefreshTime <= 0L || world.getTime() >= nextRefreshTime;
    }

    private void refreshOffers(ServerWorld world) {
        GardenMarketOfferManager manager = GardenMarketOfferManager.getInstance();
        List<GearShopOffer> master = manager.getMasterOffers();
        offerIndices.clear();

        if (!master.isEmpty()) {
            int available = master.size();
            int count;
            if (manager.shouldShowAllOffers()) {
                count = available;
            } else {
                int minOffers = MathHelper.clamp(manager.getMinOffers(), 0, available);
                int maxOffers = MathHelper.clamp(manager.getMaxOffers(), minOffers, available);
                count = minOffers;
                if (maxOffers > minOffers) {
                    Random javaRandom = new Random(world.getRandom().nextLong());
                    count = minOffers + javaRandom.nextInt(maxOffers - minOffers + 1);
                }
            }

            if (count > 0) {
                List<Integer> indices = new ArrayList<>(available);
                for (int i = 0; i < available; i++) {
                    indices.add(i);
                }
                Collections.shuffle(indices, new Random(world.getRandom().nextLong()));
                offerIndices.addAll(indices.subList(0, Math.min(count, indices.size())));
            }
        }

        nextRefreshTime = world.getTime() + manager.getRefreshIntervalTicks();
        markDirty();
    }
}
