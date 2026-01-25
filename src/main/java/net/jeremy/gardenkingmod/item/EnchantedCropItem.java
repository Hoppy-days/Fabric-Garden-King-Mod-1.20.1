package net.jeremy.gardenkingmod.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
/**
 * Simple wrapper item that always renders with the enchantment glint and marks
 * the stack as rare.
 */
public class EnchantedCropItem extends Item {
        private final float valueMultiplier;
        private final Text customName;

        public EnchantedCropItem(Settings settings, float valueMultiplier, Text customName) {
                super(settings);
                this.valueMultiplier = Math.max(1.0f, valueMultiplier);
                this.customName = customName;
        }

        public float getValueMultiplier() {
                return valueMultiplier;
        }

        @Override
        public boolean hasGlint(ItemStack stack) {
                return true;
        }

        @Override
        public Text getName(ItemStack stack) {
                return customName == null ? super.getName(stack) : customName;
        }

        @Override
        public Text getName() {
                return customName == null ? super.getName() : customName;
        }
}
