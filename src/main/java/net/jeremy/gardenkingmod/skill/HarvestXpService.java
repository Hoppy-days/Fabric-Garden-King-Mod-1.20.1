package net.jeremy.gardenkingmod.skill;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.crop.CropTier;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.crop.EnchantedCropDefinition;
import net.jeremy.gardenkingmod.network.SkillProgressNetworking;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Awards Garden King skill experience when crops are harvested. The service
 * inspects the resulting loot to determine whether the harvest produced normal,
 * rotten, or enchanted items and applies the configured XP multipliers.
 */
public final class HarvestXpService {
        private HarvestXpService() {
        }

        public static void handleLootContext(LootContext context, @Nullable Identifier blockId,
                        @Nullable Identifier tierId, ItemStack stack) {
                Entity entity = context.get(LootContextParameters.THIS_ENTITY);
                if (!(entity instanceof PlayerEntity player)) {
                        return;
                }

                awardHarvestXp(player, blockId, tierId, stack);
        }

        public static void awardHarvestXp(@Nullable Entity harvester, @Nullable Identifier blockId,
                        @Nullable Identifier tierId, ItemStack stack) {
                if (!(harvester instanceof SkillProgressHolder skillHolder)) {
                        return;
                }

                if (stack == null || stack.isEmpty()) {
                        return;
                }

                Item item = stack.getItem();

                if (ModItems.isRottenItem(item)) {
                        return;
                }

                Identifier resolvedTierId = resolveTierId(tierId, blockId, item);
                if (resolvedTierId == null) {
                        return;
                }

                long baseExperience = HarvestXpConfig.get().experienceForTierPath(resolvedTierId.getPath());
                if (baseExperience <= 0L) {
                        return;
                }

                long awarded = baseExperience;
                if (ModItems.isEnchantedItem(item)) {
                        try {
                                awarded = Math.multiplyExact(baseExperience, 2L);
                        } catch (ArithmeticException overflow) {
                                awarded = Long.MAX_VALUE;
                        }
                }

                skillHolder.gardenkingmod$addSkillExperience(awarded);

                if (harvester instanceof ServerPlayerEntity serverPlayer) {
                        SkillProgressNetworking.sync(serverPlayer);
                }
        }

        private static Identifier resolveTierId(@Nullable Identifier tierId, @Nullable Identifier blockId, Item item) {
                if (tierId != null) {
                        return tierId;
                }

                Optional<CropTier> fromItem = CropTierRegistry.get(item);
                if (fromItem.isPresent()) {
                        return fromItem.get().id();
                }

                if (ModItems.isEnchantedItem(item)) {
                        Optional<EnchantedCropDefinition> definition = ModItems.getEnchantedDefinition(item);
                        if (definition.isPresent()) {
                                Identifier targetId = definition.get().targetId();
                                Identifier resolved = resolveTierFromBlock(targetId);
                                if (resolved != null) {
                                        return resolved;
                                }
                                Identifier cropId = definition.get().cropId();
                                Identifier cropTier = resolveTierFromBlock(cropId);
                                if (cropTier != null) {
                                        return cropTier;
                                }
                        }
                }

                if (blockId != null) {
                        Identifier resolved = resolveTierFromBlock(blockId);
                        if (resolved != null) {
                                return resolved;
                        }
                }

                return null;
        }

        private static Identifier resolveTierFromBlock(Identifier blockId) {
                if (blockId == null) {
                        return null;
                }

                Block block = Registries.BLOCK.get(blockId);
                Optional<CropTier> tier = CropTierRegistry.get(block);
                return tier.map(CropTier::id).orElse(null);
        }

}
