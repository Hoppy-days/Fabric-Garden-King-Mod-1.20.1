package net.jeremy.gardenkingmod.crop;

import java.util.ArrayList;
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
import net.jeremy.gardenkingmod.crop.RottenHarvestManager;
import net.jeremy.gardenkingmod.crop.RottenHarvestManager.RottenHarvestEntry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
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
                        float baseNoDropChance = MathHelper.clamp(tier.noDropChance(), 0.0f, 1.0f);
                        float baseRottenChance = MathHelper.clamp(tier.rottenChance(), 0.0f, 1.0f);

                        Optional<RottenHarvestEntry> rottenEntry = RottenHarvestManager.getInstance().getRottenHarvest(id);
                        float extraNoDropChance = rottenEntry.map(RottenHarvestEntry::extraNoDropChance).orElse(0.0f);
                        float extraRottenChance = rottenEntry.map(RottenHarvestEntry::extraRottenChance).orElse(0.0f);
                        float combinedNoDropChance = MathHelper.clamp(baseNoDropChance + extraNoDropChance, 0.0f, 1.0f);
                        float combinedRottenChance = MathHelper.clamp(baseRottenChance + extraRottenChance, 0.0f, 1.0f);

                        boolean applyScaling = multiplier > 1.0001f;
                        boolean applyNoDrop = combinedNoDropChance > 0.0f;
                        boolean applyRotten = rottenEntry.isPresent() && combinedRottenChance > 0.0f;

                        if (!applyScaling && !applyNoDrop && !applyRotten) {
                                return;
                        }

                        fabricBuilder.modifyPools(poolBuilder -> {
                                if (applyNoDrop) {
                                        poolBuilder.conditionally(RandomChanceLootCondition.builder(1.0f - combinedNoDropChance));
                                }

                                if (applyScaling) {
                                        poolBuilder.apply(CaptureStackFunction.builder());
                                        poolBuilder.apply(
                                                        SetCountLootFunction.builder(new TierScaledCountProvider(id, multiplier)));
                                }

                                if (applyRotten) {
                                        poolBuilder.apply(ApplyRottenHarvestFunction.builder(id, rottenEntry.get().rottenItem(),
                                                        combinedRottenChance));
                                }
                        });

                        List<String> appliedEffects = new ArrayList<>();
                        if (applyScaling) {
                                appliedEffects.add(String.format("drop scaling (x%.3f)", multiplier));
                        }
                        if (applyNoDrop) {
                                appliedEffects
                                                .add(String.format("no-drop chance (%.2f%%)", combinedNoDropChance * 100.0f));
                        }
                        if (applyRotten) {
                                Identifier rottenItemId = Registries.ITEM.getId(rottenEntry.get().rottenItem());
                                appliedEffects.add(String.format("rotten conversion (%.2f%% -> %s)",
                                                combinedRottenChance * 100.0f, rottenItemId));
                        }

                        if (!appliedEffects.isEmpty()) {
                                GardenKingMod.LOGGER.debug("Applied {} to loot table {} using tier {}",
                                                String.join(", ", appliedEffects), id, tier.id());
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
                List<Identifier> matchingBlocks = new ArrayList<>();

                for (Block block : Registries.BLOCK) {
                        Identifier blockLootTable = block.getLootTableId();
                        if (LootTables.EMPTY.equals(blockLootTable)) {
                                continue;
                        }

                        if (!blockLootTable.equals(lootTableId)) {
                                continue;
                        }

                        Identifier blockId = Registries.BLOCK.getId(block);
                        matchingBlocks.add(blockId);

                        Optional<CropTier> tier = CropTierRegistry.get(block.getDefaultState());

                        if (tier.isPresent()) {
                                return Optional.of(new TierScalingData(blockId, tier.get()));
                        }
                }

                if (!matchingBlocks.isEmpty()) {
                        GardenKingMod.LOGGER.debug(
                                        "Loot table {} is not assigned to a crop tier; using vanilla drop counts (owners: {})",
                                        lootTableId, matchingBlocks);
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

        private static final class ApplyRottenHarvestFunction extends ConditionalLootFunction {
                private final Identifier lootTableId;
                private final Item rottenItem;
                private final float rottenChance;

                ApplyRottenHarvestFunction(LootCondition[] conditions, Identifier lootTableId, Item rottenItem,
                                float rottenChance) {
                        super(conditions);
                        this.lootTableId = lootTableId;
                        this.rottenItem = rottenItem;
                        this.rottenChance = rottenChance;
                }

                static ConditionalLootFunction.Builder<?> builder(Identifier lootTableId, Item rottenItem,
                                float rottenChance) {
                        return ConditionalLootFunction.builder(
                                        conditions -> new ApplyRottenHarvestFunction(conditions, lootTableId, rottenItem,
                                                        rottenChance));
                }

                @Override
                public LootFunctionType getType() {
                        return LootFunctionTypes.SET_COUNT;
                }

                @Override
                protected ItemStack process(ItemStack stack, LootContext context) {
                        if (stack.isEmpty() || rottenChance <= 0.0f) {
                                return stack;
                        }

                        if (context.getRandom().nextFloat() >= rottenChance) {
                                return stack;
                        }

                        ItemStack rottenStack = new ItemStack(rottenItem, stack.getCount());
                        if (rottenStack.isEmpty()) {
                                GardenKingMod.LOGGER.debug(
                                                "Rotten harvest for loot table {} produced an empty stack; preserving original drop",
                                                lootTableId);
                                return stack;
                        }

                        return rottenStack;
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
