package net.jeremy.gardenkingmod.shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.item.ItemStack;

/**
 * Represents a single shop offer consisting of the resulting stack and a list
 * of ItemStacks required to purchase it.
 */
public record GearShopOffer(ItemStack resultStack, List<ItemStack> costStacks) {

    public GearShopOffer {
        Objects.requireNonNull(resultStack, "resultStack");
        Objects.requireNonNull(costStacks, "costStacks");
        resultStack = resultStack.copy();
        costStacks = toImmutableCostList(costStacks);
    }

    public static GearShopOffer of(ItemStack result, ItemStack... costs) {
        return new GearShopOffer(result, Arrays.asList(costs));
    }

    public static GearShopOffer of(ItemStack result, List<ItemStack> costs) {
        return new GearShopOffer(result, costs);
    }

    private static List<ItemStack> toImmutableCostList(List<ItemStack> source) {
        List<ItemStack> copy = new ArrayList<>(source.size());
        for (ItemStack stack : source) {
            copy.add(stack.copy());
        }
        return List.copyOf(copy);
    }

    @Override
    public ItemStack resultStack() {
        return this.resultStack.copy();
    }

    public ItemStack copyResultStack() {
        return this.resultStack.copy();
    }

    @Override
    public List<ItemStack> costStacks() {
        return this.costStacks;
    }

    public List<ItemStack> copyCostStacks() {
        List<ItemStack> copy = new ArrayList<>(this.costStacks.size());
        for (ItemStack stack : this.costStacks) {
            copy.add(stack.copy());
        }
        return copy;
    }
}
