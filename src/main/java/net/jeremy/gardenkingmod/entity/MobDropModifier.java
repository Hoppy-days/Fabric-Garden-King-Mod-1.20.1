package net.jeremy.gardenkingmod.entity;

import java.util.Set;

import net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.entity.MobDropConfig.ResolvedBonusDrop;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.LootPool;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Applies configured mob loot multipliers and bonus drop entries.
 */
public final class MobDropModifier {
        private static final String ENTITY_TABLE_PREFIX = "entities/";
        private static final ThreadLocal<CapturedStackInfo> CAPTURED_STACK = new ThreadLocal<>();

        private MobDropModifier() {
        }

        public static void register() {
                LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
                        if (!id.getPath().startsWith(ENTITY_TABLE_PREFIX)) {
                                return;
                        }

                        Identifier mobId = Identifier.of(id.getNamespace(), id.getPath().substring(ENTITY_TABLE_PREFIX.length()));
                        MobDropConfig config = MobDropConfig.get();

                        float multiplier = Math.max(0.0f, config.multiplierForMob(mobId));
                        boolean applyMultiplier = Math.abs(multiplier - 1.0f) > 0.0001f;

                        FabricLootTableBuilder fabricBuilder = (FabricLootTableBuilder) (Object) tableBuilder;
                        if (applyMultiplier) {
                                fabricBuilder.modifyPools(pool -> {
                                        pool.apply(CaptureStackFunction.builder());
                                        pool.apply(SetCountLootFunction.builder(new MobScaledCountProvider(multiplier, mobId)));
                                });
                        }

                        for (ResolvedBonusDrop drop : config.bonusDropsForMob(mobId)) {
                                fabricBuilder.pool(LootPool.builder()
                                                .with(ItemEntry.builder(drop.item())
                                                                .conditionally(RandomChanceLootCondition.builder(drop.chance()))
                                                                .apply(SetCountLootFunction.builder(
                                                                                UniformLootNumberProvider.create((float) drop.minCount(),
                                                                                                (float) drop.maxCount()))))
                                                .build());
                        }

                        if (applyMultiplier || !config.bonusDropsForMob(mobId).isEmpty()) {
                                GardenKingMod.LOGGER.debug("Applied mob loot config to {} (multiplier={}, bonusDrops={})",
                                                mobId, multiplier, config.bonusDropsForMob(mobId).size());
                        }
                });
        }

        private record CapturedStackInfo(int count, int maxCount) {
                int compute(float multiplier, LootContext context) {
                        float scaled = count * multiplier;
                        int base = MathHelper.floor(scaled);
                        float fractional = scaled - base;
                        int result = base;

                        if (fractional > 0.0f && context.getRandom().nextFloat() < fractional) {
                                result++;
                        }

                        if (maxCount > 0) {
                                return MathHelper.clamp(result, 0, maxCount);
                        }
                        return Math.max(0, result);
                }
        }

        private static final class CaptureStackFunction extends ConditionalLootFunction {
                protected CaptureStackFunction(LootCondition[] conditions) {
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

        private record MobScaledCountProvider(float multiplier, Identifier mobId) implements LootNumberProvider {
                @Override
                public LootNumberProviderType getType() {
                        return LootNumberProviderTypes.CONSTANT;
                }

                @Override
                public int nextInt(LootContext context) {
                        return getScaled(context, true);
                }

                @Override
                public float nextFloat(LootContext context) {
                        return getScaled(context, false);
                }

                private int getScaled(LootContext context, boolean consume) {
                        CapturedStackInfo info = CAPTURED_STACK.get();
                        if (info == null) {
                                GardenKingMod.LOGGER.debug("Missing captured stack while scaling mob drops for {}; using rounded multiplier", mobId);
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
