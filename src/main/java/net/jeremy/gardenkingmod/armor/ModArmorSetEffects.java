package net.jeremy.gardenkingmod.armor;

import java.util.LinkedHashMap;
import java.util.Map;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.jeremy.gardenkingmod.item.RubyArmorMaterial;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Manages full-armor-set bonuses for the mod.
 * <p>
 * Add or edit entries in {@link #EFFECTS_BY_MATERIAL} to tune existing bonuses or
 * register new ones for future armor materials.
 */
public final class ModArmorSetEffects {
        private static final int MINIMUM_REFRESH_THRESHOLD = 100;

        private static final Map<ArmorMaterial, StatusEffectBonus> EFFECTS_BY_MATERIAL = new LinkedHashMap<>();

        static {
                // Update the template below to adjust duration, amplifier, or visual settings for the
                // ruby armor bonus. Add new entries to this block for additional armor materials as
                // you introduce them.
                EFFECTS_BY_MATERIAL.put(RubyArmorMaterial.INSTANCE,
                                new StatusEffectBonus(new StatusEffectInstance(StatusEffects.SPEED, 220, 0, true, false, true)));
        }

        private ModArmorSetEffects() {
        }

        public static void register() {
                ServerTickEvents.END_WORLD_TICK.register(ModArmorSetEffects::handleWorldTick);
        }

        private static void handleWorldTick(ServerWorld world) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                        if (player.isSpectator()) {
                                continue;
                        }

                        for (Map.Entry<ArmorMaterial, StatusEffectBonus> entry : EFFECTS_BY_MATERIAL.entrySet()) {
                                ArmorMaterial material = entry.getKey();
                                StatusEffectBonus bonus = entry.getValue();

                                if (hasFullSet(player, material)) {
                                        bonus.apply(player);
                                }
                        }
                }
        }

        private static boolean hasFullSet(ServerPlayerEntity player, ArmorMaterial material) {
                return isMatchingArmor(player, EquipmentSlot.HEAD, material)
                                && isMatchingArmor(player, EquipmentSlot.CHEST, material)
                                && isMatchingArmor(player, EquipmentSlot.LEGS, material)
                                && isMatchingArmor(player, EquipmentSlot.FEET, material);
        }

        private static boolean isMatchingArmor(ServerPlayerEntity player, EquipmentSlot slot, ArmorMaterial material) {
                if (!slot.getType().equals(EquipmentSlot.Type.ARMOR)) {
                        return false;
                }

                if (!(player.getEquippedStack(slot).getItem() instanceof ArmorItem armorItem)) {
                        return false;
                }

                return armorItem.getMaterial() == material;
        }

        private record StatusEffectBonus(StatusEffectInstance template) {
                void apply(ServerPlayerEntity player) {
                        StatusEffect effect = template.getEffectType();
                        StatusEffectInstance current = player.getStatusEffect(effect);
                        if (shouldRefresh(current)) {
                                player.addStatusEffect(new StatusEffectInstance(template));
                        }
                }

                private boolean shouldRefresh(StatusEffectInstance current) {
                        if (current == null) {
                                return true;
                        }

                        if (current.getAmplifier() != template.getAmplifier()) {
                                return true;
                        }

                        if (current.isAmbient() != template.isAmbient()) {
                                return true;
                        }

                        if (current.shouldShowParticles() != template.shouldShowParticles()) {
                                return true;
                        }

                        if (current.shouldShowIcon() != template.shouldShowIcon()) {
                                return true;
                        }

                        return current.getDuration() <= Math.min(MINIMUM_REFRESH_THRESHOLD,
                                        Math.max(10, template.getDuration() / 2));
                }
        }
}
