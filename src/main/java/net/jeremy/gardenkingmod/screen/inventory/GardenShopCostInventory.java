package net.jeremy.gardenkingmod.screen.inventory;

import net.jeremy.gardenkingmod.shop.GardenShopStackHelper;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

/**
 * Specialized inventory for Garden Shop cost slots that allows storing stack
 * counts beyond the vanilla stack limit so large offers can be displayed and
 * validated without splitting across multiple slots.
 */
public class GardenShopCostInventory extends SimpleInventory {

    public GardenShopCostInventory(int size) {
        super(size);
    }

    @Override
    public int getMaxCountPerStack() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = getStack(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int requestedCount = GardenShopStackHelper.getRequestedCount(stack);
        if (requestedCount <= stack.getCount()) {
            return super.removeStack(slot, amount);
        }

        ItemStack removed = GardenShopStackHelper.copyWithoutRequestedCount(stack);
        int removedCount = Math.min(Math.min(amount, requestedCount), removed.getMaxCount());
        removed.setCount(removedCount);

        int remaining = requestedCount - removedCount;
        if (remaining > 0) {
            ItemStack replacement = GardenShopStackHelper.copyWithoutRequestedCount(stack);
            GardenShopStackHelper.applyRequestedCount(replacement, remaining);
            setStack(slot, replacement);
        } else {
            setStack(slot, ItemStack.EMPTY);
        }

        markDirty();
        return removed;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = getStack(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int requestedCount = GardenShopStackHelper.getRequestedCount(stack);
        if (requestedCount <= stack.getCount()) {
            return super.removeStack(slot);
        }

        ItemStack removed = GardenShopStackHelper.copyWithoutRequestedCount(stack);
        int removedCount = Math.min(requestedCount, removed.getMaxCount());
        removed.setCount(removedCount);

        int remaining = requestedCount - removedCount;
        if (remaining > 0) {
            ItemStack replacement = GardenShopStackHelper.copyWithoutRequestedCount(stack);
            GardenShopStackHelper.applyRequestedCount(replacement, remaining);
            setStack(slot, replacement);
        } else {
            setStack(slot, ItemStack.EMPTY);
        }

        markDirty();
        return removed;
    }
}
