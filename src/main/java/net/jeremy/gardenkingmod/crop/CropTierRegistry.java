package net.jeremy.gardenkingmod.crop;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.util.Identifier;

/**
 * Registry that exposes the hard-coded crop tiers used by Garden King. The
 * registry relies on block tags for tier lookups to allow datapacks to extend
 * the system without requiring an extra configuration file.
 */
public final class CropTierRegistry {
        private static final Identifier TIER_1_ID = new Identifier(GardenKingMod.MOD_ID, "crop_tiers/tier_1");
        private static final Identifier TIER_2_ID = new Identifier(GardenKingMod.MOD_ID, "crop_tiers/tier_2");
        private static final Identifier TIER_3_ID = new Identifier(GardenKingMod.MOD_ID, "crop_tiers/tier_3");
        private static final Identifier TIER_4_ID = new Identifier(GardenKingMod.MOD_ID, "crop_tiers/tier_4");
        private static final Identifier TIER_5_ID = new Identifier(GardenKingMod.MOD_ID, "crop_tiers/tier_5");

        public static final TagKey<Block> TIER_1 = TagKey.of(RegistryKeys.BLOCK, TIER_1_ID);
        public static final TagKey<Block> TIER_2 = TagKey.of(RegistryKeys.BLOCK, TIER_2_ID);
        public static final TagKey<Block> TIER_3 = TagKey.of(RegistryKeys.BLOCK, TIER_3_ID);
        public static final TagKey<Block> TIER_4 = TagKey.of(RegistryKeys.BLOCK, TIER_4_ID);
        public static final TagKey<Block> TIER_5 = TagKey.of(RegistryKeys.BLOCK, TIER_5_ID);

        private static final TagKey<Item> TIER_1_ITEM = TagKey.of(RegistryKeys.ITEM, TIER_1_ID);
        private static final TagKey<Item> TIER_2_ITEM = TagKey.of(RegistryKeys.ITEM, TIER_2_ID);
        private static final TagKey<Item> TIER_3_ITEM = TagKey.of(RegistryKeys.ITEM, TIER_3_ID);
        private static final TagKey<Item> TIER_4_ITEM = TagKey.of(RegistryKeys.ITEM, TIER_4_ID);
        private static final TagKey<Item> TIER_5_ITEM = TagKey.of(RegistryKeys.ITEM, TIER_5_ID);

        private static Map<Identifier, CropTier> tiers = Map.of();
        private static volatile Map<Block, CropTier> blockTierLookup = Map.of();
        private static volatile ResourceManager lastResourceManager;
        private static final Object LOOKUP_LOCK = new Object();
        private static final List<TagKey<Block>> TIER_BLOCK_TAGS = List.of(TIER_1, TIER_2, TIER_3, TIER_4, TIER_5);
        private static final Identifier BLOCK_LOOKUP_RELOAD_ID = new Identifier(GardenKingMod.MOD_ID,
                        "crop_tier_block_lookup");
        private static boolean initialized = false;

        private CropTierRegistry() {
        }

        public static void init() {
                if (initialized) {
                        return;
                }

                tiers = Map.ofEntries(
                                Map.entry(TIER_1_ID, new CropTier(TIER_1_ID, 0.5f, 1.0f, 0.25f, 0.25f, 0.01f)),
                                Map.entry(TIER_2_ID, new CropTier(TIER_2_ID, 0.3f, 1.15f, 0.20f, 0.20f, 0.02f)),
                                Map.entry(TIER_3_ID, new CropTier(TIER_3_ID, 0.1f, 1.3f, 0.15f, 0.15f, 0.03f)),
                                Map.entry(TIER_4_ID, new CropTier(TIER_4_ID, 0.05f, 1.5f, 0.10f, 0.10f, 0.04f)),
                                Map.entry(TIER_5_ID, new CropTier(TIER_5_ID, 0.02f, 1.75f, 0.05f, 0.05f, 0.05f)));

                ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                                .registerReloadListener(new BlockTierLookupReloader());

                initialized = true;
                GardenKingMod.LOGGER.debug("Registered {} crop tiers", tiers.size());
        }

        public static Optional<CropTier> get(Block block) {
                if (block == null) {
                        return Optional.empty();
                }

                Map<Block, CropTier> lookup = blockTierLookup;
                CropTier tier = lookup.get(block);
                if (tier != null) {
                        return Optional.of(tier);
                }

                if (lookup.isEmpty()) {
                        RegistryEntry<Block> entry = Registries.BLOCK.getEntry(block);
                        return resolveTier(entry::isIn);
                }

                return Optional.empty();
        }

        public static Optional<CropTier> get(BlockState state) {
                if (state == null) {
                        return Optional.empty();
                }

                Optional<CropTier> tier = get(state.getBlock());
                if (tier.isPresent()) {
                        return tier;
                }

                if (blockTierLookup.isEmpty()) {
                        Optional<CropTier> fallback = resolveTier(state::isIn);
                        if (fallback.isPresent()) {
                                return fallback;
                        }

                        RegistryEntry<Block> entry = Registries.BLOCK.getEntry(state.getBlock());
                        return resolveTier(entry::isIn);
                }

                return Optional.empty();
        }

        public static Optional<CropTier> get(Item item) {
                if (item == null) {
                        return Optional.empty();
                }

                if (item instanceof BlockItem blockItem) {
                        return get(blockItem.getBlock());
                }

                if (item instanceof AliasedBlockItem aliasedBlockItem) {
                        return get(aliasedBlockItem.getBlock());
                }

                RegistryEntry<Item> entry = Registries.ITEM.getEntry(item);
                if (entry.isIn(TIER_1_ITEM)) {
                        return get(TIER_1_ID);
                }
                if (entry.isIn(TIER_2_ITEM)) {
                        return get(TIER_2_ID);
                }
                if (entry.isIn(TIER_3_ITEM)) {
                        return get(TIER_3_ID);
                }
                if (entry.isIn(TIER_4_ITEM)) {
                        return get(TIER_4_ID);
                }
                if (entry.isIn(TIER_5_ITEM)) {
                        return get(TIER_5_ID);
                }

                return Optional.empty();
        }

        public static Optional<CropTier> get(Identifier id) {
                return Optional.ofNullable(tiers.get(id));
        }

        public static List<TagKey<Block>> getTierBlockTags() {
                return TIER_BLOCK_TAGS;
        }

        public static Optional<CropTier> getTierForTag(TagKey<Block> tag) {
                if (tag == null) {
                        return Optional.empty();
                }

                if (tag.equals(TIER_1)) {
                        return get(TIER_1_ID);
                }
                if (tag.equals(TIER_2)) {
                        return get(TIER_2_ID);
                }
                if (tag.equals(TIER_3)) {
                        return get(TIER_3_ID);
                }
                if (tag.equals(TIER_4)) {
                        return get(TIER_4_ID);
                }
                if (tag.equals(TIER_5)) {
                        return get(TIER_5_ID);
                }

                return Optional.empty();
        }

        public static void ensureBlockLookup(ResourceManager manager) {
                boolean needsRebuild = blockTierLookup.isEmpty();
                if (!needsRebuild && manager != null && manager != lastResourceManager) {
                        needsRebuild = true;
                }

                if (!needsRebuild) {
                        return;
                }

                synchronized (LOOKUP_LOCK) {
                        boolean rebuild = blockTierLookup.isEmpty();
                        if (!rebuild && manager != null && manager != lastResourceManager) {
                                rebuild = true;
                        }

                        if (!rebuild) {
                                return;
                        }

                        Map<Block, CropTier> rebuilt = rebuildBlockLookup(manager);
                        if (rebuilt != null) {
                                blockTierLookup = rebuilt;
                                if (manager != null) {
                                        lastResourceManager = manager;
                                }
                        }
                }
        }

        /**
         * Applies the configured growth multiplier for the provided block state to the
         * supplied base growth chance value. The multiplier is clamped to a sane range
         * so that crops always retain a minimum random tick frequency, which keeps
         * bonemeal and farmland hydration mechanics behaving as expected.
         *
         * @param state the block state being ticked
         * @param baseChance the vanilla moisture-based growth chance value
         * @return the scaled growth chance value respecting Garden King's crop tier
         *         configuration
         */
        public static float scaleGrowthChance(BlockState state, float baseChance) {
                if (baseChance <= 0.0f) {
                        return baseChance;
                }

                float multiplier = get(state)
                                .map(CropTier::growthMultiplier)
                                .orElse(1.0f);

                if (Float.isNaN(multiplier) || multiplier <= 0.0f) {
                        return baseChance;
                }

                float scaledChance = baseChance * multiplier;

                // Ensure the returned value still results in at least one random tick in
                // reasonable time to keep hydration/bonemeal behaviour intact.
                return Math.max(0.001f, scaledChance);
        }

        private static Optional<CropTier> resolveTier(TierMembership membership) {
                if (membership.isIn(TIER_1)) {
                        return get(TIER_1_ID);
                }
                if (membership.isIn(TIER_2)) {
                        return get(TIER_2_ID);
                }
                if (membership.isIn(TIER_3)) {
                        return get(TIER_3_ID);
                }
                if (membership.isIn(TIER_4)) {
                        return get(TIER_4_ID);
                }
                if (membership.isIn(TIER_5)) {
                        return get(TIER_5_ID);
                }

                return Optional.empty();
        }

        @FunctionalInterface
        private interface TierMembership {
                boolean isIn(TagKey<Block> tag);
        }

        private static Map<Block, CropTier> rebuildBlockLookup(ResourceManager manager) {
                if (manager == null) {
                        GardenKingMod.LOGGER.warn("Cannot rebuild crop tier lookup without a resource manager instance");
                        return null;
                }

                TagGroupLoader<RegistryEntry<Block>> loader = new TagGroupLoader<>(
                                id -> Registries.BLOCK.getEntry(RegistryKey.of(RegistryKeys.BLOCK, id)),
                                TagManagerLoader.getPath(RegistryKeys.BLOCK));

                Map<Identifier, Collection<RegistryEntry<Block>>> loadedTags;
                try {
                        loadedTags = loader.load(manager);
                } catch (Exception exception) {
                        GardenKingMod.LOGGER.error("Failed to build crop tier lookup from tag data", exception);
                        return null;
                }

                Map<Block, CropTier> resolved = new IdentityHashMap<>();
                int registered = 0;

                registered += registerTierMembers(loadedTags, TIER_1, get(TIER_1_ID), resolved);
                registered += registerTierMembers(loadedTags, TIER_2, get(TIER_2_ID), resolved);
                registered += registerTierMembers(loadedTags, TIER_3, get(TIER_3_ID), resolved);
                registered += registerTierMembers(loadedTags, TIER_4, get(TIER_4_ID), resolved);
                registered += registerTierMembers(loadedTags, TIER_5, get(TIER_5_ID), resolved);

                GardenKingMod.LOGGER.debug("Built crop tier lookup for {} blocks", registered);

                return resolved.isEmpty() ? Map.of() : Map.copyOf(resolved);
        }

        private static int registerTierMembers(Map<Identifier, Collection<RegistryEntry<Block>>> loadedTags, TagKey<Block> tag,
                        Optional<CropTier> tierOptional, Map<Block, CropTier> destination) {
                if (tierOptional.isEmpty()) {
                        return 0;
                }

                Collection<RegistryEntry<Block>> entries = loadedTags.getOrDefault(tag.id(), Collections.emptyList());
                if (entries.isEmpty()) {
                        return 0;
                }

                CropTier tier = tierOptional.get();
                int count = 0;

                for (RegistryEntry<Block> entry : entries) {
                        Block block = entry.value();
                        CropTier previous = destination.put(block, tier);
                        if (previous != null && previous != tier) {
                                Identifier blockId = Registries.BLOCK.getId(block);
                                GardenKingMod.LOGGER.warn(
                                                "Block {} appears in multiple crop tier tags ({} and {}); using the last value",
                                                blockId, previous.id(), tier.id());
                        }
                        count++;
                }

                return count;
        }

        private static final class BlockTierLookupReloader implements SimpleSynchronousResourceReloadListener {
                @Override
                public Identifier getFabricId() {
                        return BLOCK_LOOKUP_RELOAD_ID;
                }

                @Override
                public void reload(ResourceManager manager) {
                        synchronized (LOOKUP_LOCK) {
                                Map<Block, CropTier> rebuilt = rebuildBlockLookup(manager);
                                if (rebuilt != null) {
                                        blockTierLookup = rebuilt;
                                        lastResourceManager = manager;
                                }
                        }
                }

                @Override
                public Collection<Identifier> getFabricDependencies() {
                        return Collections.singleton(ResourceReloadListenerKeys.TAGS);
                }
        }
}
