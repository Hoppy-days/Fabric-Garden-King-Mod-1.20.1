package net.jeremy.gardenkingmod.item;

import java.util.EnumMap;
import java.util.Map;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.ModItems;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public enum PearlArmorMaterial implements ArmorMaterial {
        INSTANCE;

        private static final int DURABILITY_MULTIPLIER = 39;
        private static final int ENCHANTABILITY = 25;
        private static final float TOUGHNESS = 3.5F;
        private static final float KNOCKBACK_RESISTANCE = 0.12F;

        private static final Map<ArmorItem.Type, Integer> BASE_DURABILITY = new EnumMap<>(ArmorItem.Type.class);
        private static final Map<ArmorItem.Type, Integer> PROTECTION_VALUES = new EnumMap<>(ArmorItem.Type.class);

        static {
                BASE_DURABILITY.put(ArmorItem.Type.HELMET, 13);
                BASE_DURABILITY.put(ArmorItem.Type.CHESTPLATE, 15);
                BASE_DURABILITY.put(ArmorItem.Type.LEGGINGS, 16);
                BASE_DURABILITY.put(ArmorItem.Type.BOOTS, 11);

                PROTECTION_VALUES.put(ArmorItem.Type.HELMET, 5);
                PROTECTION_VALUES.put(ArmorItem.Type.CHESTPLATE, 9);
                PROTECTION_VALUES.put(ArmorItem.Type.LEGGINGS, 7);
                PROTECTION_VALUES.put(ArmorItem.Type.BOOTS, 5);
        }

        @Override
        public int getDurability(ArmorItem.Type type) {
                return BASE_DURABILITY.get(type) * DURABILITY_MULTIPLIER;
        }

        @Override
        public int getProtection(ArmorItem.Type type) {
                return PROTECTION_VALUES.get(type);
        }

        @Override
        public int getEnchantability() {
                return ENCHANTABILITY;
        }

        @Override
        public SoundEvent getEquipSound() {
                return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
        }

        @Override
        public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(ModItems.PEARL);
        }

        @Override
        public String getName() {
                return GardenKingMod.MOD_ID + ":pearl";
        }

        @Override
        public float getToughness() {
                return TOUGHNESS;
        }

        @Override
        public float getKnockbackResistance() {
                return KNOCKBACK_RESISTANCE;
        }
}
