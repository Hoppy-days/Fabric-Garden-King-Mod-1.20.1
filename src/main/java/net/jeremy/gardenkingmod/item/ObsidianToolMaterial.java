package net.jeremy.gardenkingmod.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;

public enum ObsidianToolMaterial implements ToolMaterial {
        INSTANCE;

        private static final int DURABILITY = 2200;
        private static final float MINING_SPEED = 9.0F;
        private static final float ATTACK_DAMAGE = 5.0F;
        private static final int MINING_LEVEL = 4;
        private static final int ENCHANTABILITY = 12;

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
                return Ingredient.ofItems(Items.OBSIDIAN);
        }
}
