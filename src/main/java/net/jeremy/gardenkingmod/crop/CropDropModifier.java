package net.jeremy.gardenkingmod.crop;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.crop.BonusHarvestDropManager.BonusDropEntry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Applies Garden King's crop drop multipliers to loot tables at runtime.
 * <p>
 * The modifier relies on {@link CropTierRegistry}'s block tags, so datapacks
 * can introduce new crops or rebalance tiers by tagging additional blocks
 * without code changes. Newly tagged crops will automatically inherit the tier
 * growth and drop modifiers the next time loot tables are reloaded.
 */
public final class CropDropModifier {
        private static final Map<Identifier, Optional<TierScalingData>> CACHE = new ConcurrentHashMap<>();
        private static final ThreadLocal<CapturedStackInfo> CAPTURED_STACK = new ThreadLocal<>();

        private CropDropModifier() {
        }

        public static void register() {
                LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
                        FabricLootTableBuilder fabricBuilder = (FabricLootTableBuilder) (Object) tableBuilder;

                        List<BonusDropEntry> bonusDrops = BonusHarvestDropManager.getInstance().getBonusDrops(id);
                        AtomicBoolean bonusApplied = new AtomicBoolean(false);
                        if (!bonusDrops.isEmpty()) {
                                fabricBuilder.modifyPools(poolBuilder -> {
                                        if (bonusApplied.compareAndSet(false, true)) {
                                                for (BonusDropEntry bonusDrop : bonusDrops) {
                                                        poolBuilder.with(ItemEntry.builder(bonusDrop.item())
                                                                        .conditionally(RandomChanceLootCondition.builder(bonusDrop.chance()))
                                                                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create((float) bonusDrop.minCount(), (float) bonusDrop.maxCount()))));
                                                }
                                        }
                                });

                                if (bonusApplied.get()) {
                                        GardenKingMod.LOGGER.debug("Applied {} bonus harvest drops to loot table {}", bonusDrops.size(), id);
                                } else {
                                        GardenKingMod.LOGGER.warn("Failed to attach bonus harvest drops to loot table {} because it has no pools", id);
                                }
                        }

                        Optional<TierScalingData> scalingData = resolveScaling(id);
                        if (scalingData.isEmpty()) {
                                return;
                        }

                        CropTier tier = scalingData.get().tier();
                        float multiplier = tier.dropMultiplier();
                        float noDropChance = MathHelper.clamp(tier.noDropChance(), 0.0f, 1.0f);
                        boolean applyScaling = multiplier > 1.0001f;
                        boolean applyNoDrop = noDropChance > 0.0f;

                        if (!applyScaling && !applyNoDrop) {
                                return;
                        }

                        fabricBuilder.modifyPools(poolBuilder -> {
                                if (applyNoDrop) {
                                        poolBuilder.conditionally(RandomChanceLootCondition.builder(1.0f - noDropChance));
                                }

                                if (applyScaling) {
                                        poolBuilder.apply(CaptureStackFunction.builder());
                                        poolBuilder.apply(
                                                        SetCountLootFunction.builder(new TierScaledCountProvider(id, multiplier)));
                                }
                        });

                        if (applyScaling && applyNoDrop) {
                                GardenKingMod.LOGGER.debug(
                                                "Applied drop scaling (x{}) and no-drop chance ({}%) to loot table {} using tier {}",
                                                multiplier, noDropChance * 100.0f, id, tier.id());
                        } else if (applyScaling) {
                                GardenKingMod.LOGGER.debug("Applied drop scaling (x{}) to loot table {} using tier {}",
                                                multiplier, id, tier.id());
                        } else {
                                GardenKingMod.LOGGER.debug("Applied no-drop chance ({}%) to loot table {} using tier {}",
                                                noDropChance * 100.0f, id, tier.id());
                        }
                });

                LootTableEvents.ALL_LOADED.register((resourceManager, lootManager) -> {
                        CACHE.clear();
                        GardenKingMod.LOGGER.debug("Reset crop drop scaling cache after loot table reload");
                });
        }

        private static Optional<TierScalingData> resolveScaling(Identifier lootTableId) {
                        return CACHE.computeIfAbsent(lootTableId, CropDropModifier::lookupScalingData);
        }

        private static Optional<TierScalingData> lookupScalingData(Identifier lootTableId) {
                for (Block block : Registries.BLOCK) {
                        Identifier blockLootTable = block.getLootTableId();
                        if (LootTables.EMPTY.equals(blockLootTable)) {
                                continue;
                        }

                        if (!blockLootTable.equals(lootTableId)) {
                                continue;
                        }

                        Identifier blockId = Registries.BLOCK.getId(block);
                        Optional<CropTier> tier = CropTierRegistry.get(block.getDefaultState());

                        if (tier.isPresent()) {
                                return Optional.of(new TierScalingData(blockId, tier.get()));
                        }

                        GardenKingMod.LOGGER.debug("Loot table {} (block {}) is not assigned to a crop tier; using vanilla drop counts", lootTableId, blockId);
                        return Optional.empty();
                }

                return Optional.empty();
        }

        private record TierScalingData(Identifier blockId, CropTier tier) {
        }

        private static final class CapturedStackInfo {
                private final int originalCount;
                private final int maxCount;
                private Integer scaledCount;

                CapturedStackInfo(int originalCount, int maxCount) {
                        this.originalCount = originalCount;
                        this.maxCount = maxCount;
                }

                int compute(float multiplier, LootContext context) {
                        if (scaledCount != null) {
                                return scaledCount.intValue();
                        }

                        float scaled = originalCount * multiplier;
                        int base = MathHelper.floor(scaled);
                        float fractional = scaled - base;
                        int result = base;

                        if (fractional > 0.0f && context.getRandom().nextFloat() < fractional) {
                                result++;
                        }

                        if (maxCount > 0) {
                                result = MathHelper.clamp(result, 0, maxCount);
                        } else {
                                result = Math.max(0, result);
                        }

                        scaledCount = result;
                        return result;
                }
        }

        private static final class CaptureStackFunction extends ConditionalLootFunction {
                CaptureStackFunction(LootCondition[] conditions) {
                        super(conditions);
                }

                static ConditionalLootFunction.Builder<?> builder() {
                        return ConditionalLootFunction.builder(CaptureStackFunction::new);
                }

                @Override
                public LootFunctionType getType() {
                        return LootFunctionTypes.SET_COUNT;
                }

                @Override
                protected ItemStack process(ItemStack stack, LootContext context) {
                        CAPTURED_STACK.set(new CapturedStackInfo(stack.getCount(), stack.getMaxCount()));
                        return stack;
                }
        }

        private static final class TierScaledCountProvider implements LootNumberProvider {
                private final Identifier lootTableId;
                private final float multiplier;

                TierScaledCountProvider(Identifier lootTableId, float multiplier) {
                        this.lootTableId = lootTableId;
                        this.multiplier = multiplier;
                }

                @Override
                public LootNumberProviderType getType() {
                        return LootNumberProviderTypes.CONSTANT;
                }

                @Override
                public int nextInt(LootContext context) {
                        return getScaledCount(context, true);
                }

                @Override
                public float nextFloat(LootContext context) {
                        return getScaledCount(context, false);
                }

                private int getScaledCount(LootContext context, boolean consume) {
                        CapturedStackInfo info = CAPTURED_STACK.get();
                        if (info == null) {
                                GardenKingMod.LOGGER.debug("Missing captured stack info while scaling loot table {}; falling back to base multiplier", lootTableId);
                                return Math.max(0, Math.round(multiplier));
                        }

                        int scaled = info.compute(multiplier, context);

                        if (consume) {
                                CAPTURED_STACK.remove();
                        }

                        return scaled;
                }

                @Override
                public Set<LootContextParameter<?>> getRequiredParameters() {
                        return Set.of();
                }
        }
}
