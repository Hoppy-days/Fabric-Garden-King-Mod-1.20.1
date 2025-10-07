package net.jeremy.gardenkingmod.item;

import net.jeremy.gardenkingmod.ModItems;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public enum TopazToolMaterial implements ToolMaterial {
        INSTANCE;

        private static final int DURABILITY = 1958;
        private static final float MINING_SPEED = 9.25F;
        private static final float ATTACK_DAMAGE = 4.25F;
        private static final int MINING_LEVEL = 4;
        private static final int ENCHANTABILITY = 21;

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
                return Ingredient.ofItems(ModItems.TOPAZ);
        }
}
