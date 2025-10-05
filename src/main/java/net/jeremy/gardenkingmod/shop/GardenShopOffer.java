package net.jeremy.gardenkingmod.shop;

import net.minecraft.item.ItemStack;

/**
 * Simple data holder representing a single item that the garden shop can sell
 * along with the Garden Coin price for that item.
 */
public record GardenShopOffer(ItemStack displayStack, int price) {

    public GardenShopOffer {
        displayStack = displayStack.copy();
    }

    public ItemStack createDisplayStack() {
        return displayStack.copy();
    }
}
