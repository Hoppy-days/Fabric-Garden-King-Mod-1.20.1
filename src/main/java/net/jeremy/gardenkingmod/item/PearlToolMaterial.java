package net.jeremy.gardenkingmod.item;

import net.jeremy.gardenkingmod.ModItems;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public enum PearlToolMaterial implements ToolMaterial {
        INSTANCE;

        private static final int DURABILITY = 2100;
        private static final float MINING_SPEED = 10.0F;
        private static final float ATTACK_DAMAGE = 5.0F;
        private static final int MINING_LEVEL = 4;
        private static final int ENCHANTABILITY = 24;

        @Override
        public int getDurability() {
                return DURABILITY;
        }

        @Override
        public float getMiningSpeedMultiplier() {
                return MINING_SPEED;
        }

        @Override
        public float getAttackDamage() {
                return ATTACK_DAMAGE;
        }

        @Override
        public int getMiningLevel() {
                return MINING_LEVEL;
        }

        @Override
        public int getEnchantability() {
                return ENCHANTABILITY;
        }

        @Override
        public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(ModItems.PEARL);
        }
}
