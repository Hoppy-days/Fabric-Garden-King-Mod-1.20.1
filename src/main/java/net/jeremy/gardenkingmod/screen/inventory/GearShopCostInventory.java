package net.jeremy.gardenkingmod.screen.inventory;

import net.jeremy.gardenkingmod.shop.GearShopStackHelper;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

/**
 * Specialized inventory for Gear Shop cost slots that allows storing stack
 * counts beyond the vanilla stack limit so large offers can be displayed and
 * validated without splitting across multiple slots.
 */
public class GearShopCostInventory extends SimpleInventory {

    public GearShopCostInventory(int size) {
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

        int requestedCount = GearShopStackHelper.getRequestedCount(stack);
        if (requestedCount <= stack.getCount()) {
            return super.removeStack(slot, amount);
        }

        int removedCount = Math.min(amount, requestedCount);
        ItemStack removed = stack.copy();
        GearShopStackHelper.applyRequestedCount(removed, removedCount);

        int remaining = requestedCount - removedCount;
        if (remaining > 0) {
            ItemStack replacement = stack.copy();
            GearShopStackHelper.applyRequestedCount(replacement, remaining);
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

        int requestedCount = GearShopStackHelper.getRequestedCount(stack);
        if (requestedCount <= 0) {
            return ItemStack.EMPTY;
        }

        return removeStack(slot, requestedCount);
    }
}
