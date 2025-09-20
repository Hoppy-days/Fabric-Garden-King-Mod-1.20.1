package net.jeremy.gardenkingmod.crop;

import java.util.Map;
import java.util.Optional;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
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
        private static boolean initialized = false;

        private CropTierRegistry() {
        }

        public static void init() {
                if (initialized) {
                        return;
                }

		tiers = Map.ofEntries(
				Map.entry(TIER_1_ID, new CropTier(TIER_1_ID, 1.0f, 1.0f, 1.0f, 0.0f)),
				Map.entry(TIER_2_ID, new CropTier(TIER_2_ID, 0.9f, 1.15f, 0.05f, 0.05f)),
				Map.entry(TIER_3_ID, new CropTier(TIER_3_ID, 0.8f, 1.3f, 0.1f, 0.1f)),
				Map.entry(TIER_4_ID, new CropTier(TIER_4_ID, 0.7f, 1.5f, 0.15f, 0.15f)),
				Map.entry(TIER_5_ID, new CropTier(TIER_5_ID, 0.6f, 1.75f, 0.2f, 0.2f)));

                initialized = true;
                GardenKingMod.LOGGER.debug("Registered {} crop tiers", tiers.size());
        }

        public static Optional<CropTier> get(BlockState state) {
                if (state == null) {
                        return Optional.empty();
                }

                Optional<CropTier> tier = resolveTier(state::isIn);
                if (tier.isPresent()) {
                        return tier;
                }

                RegistryEntry<Block> entry = Registries.BLOCK.getEntry(state.getBlock());
                return resolveTier(entry::isIn);
        }

        public static Optional<CropTier> get(Item item) {
                if (item == null) {
                        return Optional.empty();
                }

                if (item instanceof BlockItem blockItem) {
                        return get(blockItem.getBlock().getDefaultState());
                }

                if (item instanceof AliasedBlockItem aliasedBlockItem) {
                        return get(aliasedBlockItem.getBlock().getDefaultState());
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
}
