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
import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.crop.EnchantedCropDefinition;
import net.jeremy.gardenkingmod.crop.EnchantedCropDefinitions;
import net.jeremy.gardenkingmod.crop.BonusHarvestDropManager.BonusDropEntry;

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
                        CropTierRegistry.ensureBlockLookup(resourceManager);

                        BonusHarvestDropManager bonusManager = BonusHarvestDropManager.getInstance();

                        bonusManager.ensureLoaded(resourceManager);

                        FabricLootTableBuilder fabricBuilder = (FabricLootTableBuilder) (Object) tableBuilder;

                        List<BonusDropEntry> bonusDrops = bonusManager.getBonusDrops(id);
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
                        Optional<CropTier> tier = scalingData.map(TierScalingData::tier);
                        float multiplier = tier.map(CropTier::dropMultiplier).orElse(1.0f);
                        float baseNoDropChance = MathHelper.clamp(tier.map(CropTier::noDropChance).orElse(0.0f), 0.0f, 1.0f);
                        float baseRottenChance = MathHelper.clamp(tier.map(CropTier::rottenChance).orElse(0.0f), 0.0f, 1.0f);
                        float baseEnchantedChance = MathHelper
                                        .clamp(tier.map(CropTier::enchantedChance).orElse(0.0f), 0.0f, 1.0f);
                        Item baselineRottenItem = scalingData
                                        .map(TierScalingData::blockId)
                                        .map(ModItems::getRottenItemForTarget)
                                        .orElse(null);
                        Item baselineEnchantedItem = scalingData
                                        .map(TierScalingData::blockId)
                                        .map(ModItems::getEnchantedItemForTarget)
                                        .orElse(null);

                        Optional<RottenCropDefinition> rottenDefinition = RottenCropDefinitions.findByLootTableId(id);
                        float extraNoDropChance = rottenDefinition.map(RottenCropDefinition::extraNoDropChance).orElse(0.0f);
                        float extraRottenChance = rottenDefinition.map(RottenCropDefinition::extraRottenChance).orElse(0.0f);
                        Item definitionRottenItem = rottenDefinition
                                        .map(RottenCropDefinition::targetId)
                                        .map(ModItems::getRottenItemForTarget)
                                        .orElse(null);

                        Optional<EnchantedCropDefinition> enchantedDefinition = EnchantedCropDefinitions
                                        .findByLootTableId(id);
                        if (enchantedDefinition.isEmpty()) {
                                enchantedDefinition = scalingData
                                                .map(TierScalingData::blockId)
                                                .flatMap(EnchantedCropDefinitions::findByTargetId);
                        }

                        boolean definitionUsesTierChance = enchantedDefinition
                                        .map(EnchantedCropDefinition::usesTierChance)
                                        .orElse(false);
                        float enchantedChance = baseEnchantedChance;
                        if (enchantedDefinition.isPresent() && !definitionUsesTierChance) {
                                enchantedChance = enchantedDefinition.get().effectiveDropChance();
                        }
                        float enchantedMultiplier = enchantedDefinition
                                        .map(EnchantedCropDefinition::effectiveValueMultiplier)
                                        .orElse(EnchantedCropDefinition.DEFAULT_VALUE_MULTIPLIER);
                        Item definitionEnchantedItem = enchantedDefinition
                                        .map(EnchantedCropDefinition::targetId)
                                        .map(ModItems::getEnchantedItemForTarget)
                                        .orElse(null);
                        if (definitionEnchantedItem == null) {
                                definitionEnchantedItem = enchantedDefinition
                                                .map(EnchantedCropDefinition::cropId)
                                                .map(ModItems::getEnchantedItemForCrop)
                                                .orElse(null);
                        }

                        if (enchantedChance <= 0.0f && baselineEnchantedItem != null
                                        && (enchantedDefinition.isEmpty() || definitionUsesTierChance)) {
                                enchantedChance = baseEnchantedChance > 0.0f ? baseEnchantedChance
                                                : EnchantedCropDefinition.DEFAULT_DROP_CHANCE;
                        }

                        Item enchantedItem = definitionEnchantedItem != null ? definitionEnchantedItem : baselineEnchantedItem;
                        float combinedNoDropChance = MathHelper.clamp(baseNoDropChance + extraNoDropChance, 0.0f, 1.0f);
                        float combinedRottenChance = MathHelper.clamp(baseRottenChance + extraRottenChance, 0.0f, 1.0f);
                        Item rottenItem = definitionRottenItem != null ? definitionRottenItem : baselineRottenItem;

                        boolean applyScaling = multiplier > 1.0001f;
                        boolean applyNoDrop = combinedNoDropChance > 0.0f;
                        boolean applyEnchanted = enchantedItem != null && enchantedChance > 0.0f;
                        boolean applyRotten = rottenItem != null && combinedRottenChance > 0.0f;

                        if (!applyScaling && !applyNoDrop && !applyRotten && !applyEnchanted) {
                                return;
                        }

                        if (!applyEnchanted && enchantedChance > 0.0f) {
                                String baselineLabel = baselineEnchantedItem == null ? "none"
                                                : Optional.ofNullable(Registries.ITEM.getId(baselineEnchantedItem))
                                                                .map(Identifier::toString)
                                                                .orElse("unknown");
                                String definitionLabel = enchantedDefinition
                                                .map(EnchantedCropDefinition::enchantedItemId)
                                                .map(Identifier::toString)
                                                .orElse("none");
                                GardenKingMod.LOGGER.debug(
                                                "Skipping enchanted harvest for loot table {} because no enchanted item is available (definition candidate: {}, tier fallback: {})",
                                                id, definitionLabel, baselineLabel);
                        }

                        if (!applyRotten && combinedRottenChance > 0.0f) {
                                String baselineLabel = baselineRottenItem == null ? "none"
                                                : Registries.ITEM.getId(baselineRottenItem).toString();
                                String definitionLabel = rottenDefinition
                                                .map(RottenCropDefinition::rottenItemId)
                                                .map(Identifier::toString)
                                                .orElse("none");
                                GardenKingMod.LOGGER.debug(
                                                "Skipping rotten conversion for loot table {} because no rotten item is available (definition candidate: {}, tier fallback: {})",
                                                id, definitionLabel, baselineLabel);
                        }

                        final Item finalEnchantedItem = enchantedItem;
                        final float finalEnchantedChance = enchantedChance;
                        final float finalEnchantedMultiplier = enchantedMultiplier;
                        fabricBuilder.modifyPools(poolBuilder -> {
                                if (applyNoDrop) {
                                        poolBuilder.conditionally(RandomChanceLootCondition.builder(1.0f - combinedNoDropChance));
                                }

                                if (applyScaling) {
                                        poolBuilder.apply(CaptureStackFunction.builder());
                                        poolBuilder.apply(
                                                        SetCountLootFunction.builder(new TierScaledCountProvider(id, multiplier)));
                                }

                                if (applyEnchanted) {
                                        poolBuilder.apply(ApplyEnchantedHarvestFunction.builder(id, finalEnchantedItem,
                                                        finalEnchantedChance, finalEnchantedMultiplier));
                                }

                                if (applyRotten) {
                                        poolBuilder.apply(ApplyRottenHarvestFunction.builder(id, rottenItem,
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
                        if (applyEnchanted) {
                                Identifier enchantedItemId = Registries.ITEM.getId(enchantedItem);
                                appliedEffects.add(String.format("enchanted harvest (%.2f%% -> %s, x%.2f)",
                                                enchantedChance * 100.0f,
                                                enchantedItemId == null ? "unknown" : enchantedItemId.toString(),
                                                enchantedMultiplier));
                        }
                        if (applyRotten) {
                                Identifier rottenItemId = Registries.ITEM.getId(rottenItem);
                                String rottenOrigin = definitionRottenItem != null ? "definition" : "tier fallback";
                                appliedEffects.add(String.format("rotten conversion (%.2f%% -> %s, %s)",
                                                combinedRottenChance * 100.0f, rottenItemId, rottenOrigin));
                        }

                        if (!appliedEffects.isEmpty()) {
                                if (tier.isPresent()) {
                                        GardenKingMod.LOGGER.debug("Applied {} to loot table {} using tier {}",
                                                        String.join(", ", appliedEffects), id, tier.get().id());
                                } else {
                                        GardenKingMod.LOGGER.debug("Applied {} to loot table {} without tier scaling",
                                                        String.join(", ", appliedEffects), id);
                                }
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

                        Optional<CropTier> tier = CropTierRegistry.get(block);

                        if (tier.isPresent()) {
                                return Optional.of(new TierScalingData(blockId, tier.get()));
                        }
                }

                if (!matchingBlocks.isEmpty()) {
                        GardenKingMod.LOGGER.debug(
                                        "Loot table {} is not assigned to a crop tier; using vanilla drop counts (owners: {})",
                                        lootTableId, matchingBlocks);
                } else {
                        GardenKingMod.LOGGER.debug(
                                        "Loot table {} is not associated with a block owner; skipping crop tier scaling",
                                        lootTableId);
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

        private static final class ApplyEnchantedHarvestFunction extends ConditionalLootFunction {
                private final Identifier lootTableId;
                private final Item enchantedItem;
                private final float enchantedChance;
                private final float valueMultiplier;

                ApplyEnchantedHarvestFunction(LootCondition[] conditions, Identifier lootTableId, Item enchantedItem,
                                float enchantedChance, float valueMultiplier) {
                        super(conditions);
                        this.lootTableId = lootTableId;
                        this.enchantedItem = enchantedItem;
                        this.enchantedChance = enchantedChance;
                        this.valueMultiplier = Math.max(1.0f, valueMultiplier);
                }

                static ConditionalLootFunction.Builder<?> builder(Identifier lootTableId, Item enchantedItem,
                                float enchantedChance, float valueMultiplier) {
                        return ConditionalLootFunction.builder(conditions -> new ApplyEnchantedHarvestFunction(conditions,
                                        lootTableId, enchantedItem, enchantedChance, valueMultiplier));
                }

                @Override
                public LootFunctionType getType() {
                        return LootFunctionTypes.SET_COUNT;
                }

                @Override
                protected ItemStack process(ItemStack stack, LootContext context) {
                        if (stack.isEmpty() || enchantedChance <= 0.0f) {
                                return stack;
                        }

                        if (context.getRandom().nextFloat() >= enchantedChance) {
                                return stack;
                        }

                        int originalCount = stack.getCount();
                        float scaled = originalCount * valueMultiplier;
                        int baseCount = MathHelper.floor(scaled);
                        float fractional = scaled - baseCount;
                        int result = baseCount;

                        if (fractional > 0.0f && context.getRandom().nextFloat() < fractional) {
                                result++;
                        }

                        int maxCount = stack.getMaxCount();
                        if (maxCount > 0) {
                                result = MathHelper.clamp(result, 1, maxCount);
                        } else {
                                result = Math.max(1, result);
                        }

                        ItemStack enchantedStack = new ItemStack(enchantedItem, result);
                        if (enchantedStack.isEmpty()) {
                                GardenKingMod.LOGGER.debug(
                                                "Enchanted harvest for loot table {} produced an empty stack; preserving original drop",
                                                lootTableId);
                                return stack;
                        }

                        return enchantedStack;
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

                        if (ModItems.isEnchantedItem(stack.getItem())) {
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
