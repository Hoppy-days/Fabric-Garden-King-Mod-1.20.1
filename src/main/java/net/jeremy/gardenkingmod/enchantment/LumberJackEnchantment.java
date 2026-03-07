package net.jeremy.gardenkingmod.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class LumberJackEnchantment extends Enchantment {
        public LumberJackEnchantment() {
                super(Rarity.RARE, EnchantmentTarget.AXE, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
        }

        @Override
        public int getMinPower(int level) {
                return 15;
        }

        @Override
        public int getMaxPower(int level) {
                return 50;
        }

        @Override
        public int getMaxLevel() {
                return 1;
        }

        @Override
        public boolean isAvailableForRandomSelection() {
                return false;
        }

        @Override
        public boolean isAvailableForEnchantedBookOffer() {
                return false;
        }
}
