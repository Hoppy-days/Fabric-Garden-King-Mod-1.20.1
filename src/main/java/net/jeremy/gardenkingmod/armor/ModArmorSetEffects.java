package net.jeremy.gardenkingmod.armor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.jeremy.gardenkingmod.item.AmethystArmorMaterial;
import net.jeremy.gardenkingmod.item.BlueSapphireArmorMaterial;
import net.jeremy.gardenkingmod.item.EmeraldArmorMaterial;
import net.jeremy.gardenkingmod.item.ObsidianArmorMaterial;
import net.jeremy.gardenkingmod.item.PearlArmorMaterial;
import net.jeremy.gardenkingmod.item.RubyArmorMaterial;
import net.jeremy.gardenkingmod.item.TopazArmorMaterial;
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
                EFFECTS_BY_MATERIAL.put(AmethystArmorMaterial.INSTANCE, new StatusEffectBonus(
                                effect(StatusEffects.NIGHT_VISION, 220, 0, true, false, true),
                                effect(StatusEffects.LUCK, 220, 0, true, false, true)));
                EFFECTS_BY_MATERIAL.put(BlueSapphireArmorMaterial.INSTANCE, new StatusEffectBonus(
                                effect(StatusEffects.SPEED, 220, 0, true, false, true),
                                effect(StatusEffects.JUMP_BOOST, 220, 0, true, false, true),
                                effect(StatusEffects.DOLPHINS_GRACE, 220, 0, true, false, true)));
                EFFECTS_BY_MATERIAL.put(EmeraldArmorMaterial.INSTANCE, new StatusEffectBonus(
                                effect(StatusEffects.LUCK, 220, 0, true, false, true),
                                effect(StatusEffects.HERO_OF_THE_VILLAGE, 220, 0, true, false, true),
                                effect(StatusEffects.REGENERATION, 220, 0, true, false, true)));
                EFFECTS_BY_MATERIAL.put(ObsidianArmorMaterial.INSTANCE, new StatusEffectBonus(
                                effect(StatusEffects.RESISTANCE, 220, 0, true, false, true),
                                effect(StatusEffects.SLOWNESS, 220, 0, true, false, true),
                                effect(StatusEffects.ABSORPTION, 220, 0, true, false, true)));
                EFFECTS_BY_MATERIAL.put(PearlArmorMaterial.INSTANCE, new StatusEffectBonus(
                                effect(StatusEffects.SLOW_FALLING, 220, 0, true, false, true),
                                effect(StatusEffects.SPEED, 220, 0, true, false, true),
                                effect(StatusEffects.RESISTANCE, 220, 0, true, false, true)));
                EFFECTS_BY_MATERIAL.put(RubyArmorMaterial.INSTANCE, new StatusEffectBonus(
                                effect(StatusEffects.STRENGTH, 220, 0, true, false, true),
                                effect(StatusEffects.REGENERATION, 220, 0, true, false, true),
                                effect(StatusEffects.FIRE_RESISTANCE, 220, 0, true, false, true)));
                EFFECTS_BY_MATERIAL.put(TopazArmorMaterial.INSTANCE, new StatusEffectBonus(
                                effect(StatusEffects.HASTE, 220, 0, true, false, true),
                                effect(StatusEffects.LUCK, 220, 0, true, false, true),
                                effect(StatusEffects.ABSORPTION, 220, 0, true, false, true)));
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

        private static StatusEffectInstance effect(StatusEffect statusEffect, int duration, int amplifier,
                        boolean ambient, boolean showParticles, boolean showIcon) {
                return new StatusEffectInstance(statusEffect, duration, amplifier, ambient, showParticles, showIcon);
        }

        private record StatusEffectBonus(List<StatusEffectInstance> templates) {
                private StatusEffectBonus(StatusEffectInstance... templates) {
                        this(List.of(templates));
                }

                void apply(ServerPlayerEntity player) {
                        for (StatusEffectInstance template : templates) {
                                StatusEffect effect = template.getEffectType();
                                StatusEffectInstance current = player.getStatusEffect(effect);
                                if (shouldRefresh(template, current)) {
                                        player.addStatusEffect(new StatusEffectInstance(template));
                                }
                        }
                }

                private boolean shouldRefresh(StatusEffectInstance template, StatusEffectInstance current) {
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
