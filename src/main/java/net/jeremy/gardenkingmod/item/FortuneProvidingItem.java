package net.jeremy.gardenkingmod.item;

import net.minecraft.item.ItemStack;

/**
 * Items that implement this interface can supply a built-in fortune level without an enchantment.
 */
public interface FortuneProvidingItem {
        int gardenkingmod$getFortuneLevel(ItemStack stack);
}
