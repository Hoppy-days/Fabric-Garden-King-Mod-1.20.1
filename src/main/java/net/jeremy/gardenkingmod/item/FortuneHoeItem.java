package net.jeremy.gardenkingmod.item;

import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;

/**
 * A hoe item that provides a built-in fortune level without requiring an enchantment.
 */
public class FortuneHoeItem extends HoeItem implements FortuneProvidingItem {
    private final int fortuneLevel;

    public FortuneHoeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings,
            int fortuneLevel) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.fortuneLevel = fortuneLevel;
    }
  
        @Override
        public int gardenkingmod$getFortuneLevel(ItemStack stack) {
                return fortuneLevel;
        }
}