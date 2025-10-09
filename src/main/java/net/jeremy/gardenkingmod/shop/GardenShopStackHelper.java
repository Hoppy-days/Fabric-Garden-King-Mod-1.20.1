package net.jeremy.gardenkingmod.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/**
 * Utility methods for working with Garden Shop item stacks that need to
 * represent costs larger than a single vanilla stack can hold.
 */
public final class GardenShopStackHelper {
    private static final String FULL_COUNT_KEY = "GardenShopFullCount";

    private GardenShopStackHelper() {
    }

    /**
     * Applies the requested count to the provided stack while preserving the
     * full requested amount for display or validation purposes.
     *
     * @param stack the stack to mutate
     * @param requestedCount the desired total count for the stack
     */
    public static void applyRequestedCount(ItemStack stack, int requestedCount) {
        if (stack.isEmpty()) {
            return;
        }

        int sanitized = Math.max(1, requestedCount);
        int displayCount = Math.min(sanitized, stack.getMaxCount());
        stack.setCount(displayCount);
        if (sanitized > displayCount) {
            stack.getOrCreateNbt().putInt(FULL_COUNT_KEY, sanitized);
        } else {
            removeFullCount(stack);
        }
    }

    /**
     * Returns the full requested count stored on the stack, or the vanilla
     * stack count if no custom count is present.
     *
     * @param stack the stack to read
     * @return the requested count encoded on the stack
     */
    public static int getRequestedCount(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(FULL_COUNT_KEY, NbtElement.NUMBER_TYPE)) {
            return Math.max(stack.getCount(), nbt.getInt(FULL_COUNT_KEY));
        }

        return stack.getCount();
    }

    /**
     * Returns a defensive copy of the stack without the helper metadata used to
     * encode large requested counts. This is useful when comparing cost stacks
     * against real inventory stacks which will not contain the helper data.
     *
     * @param stack the stack to copy
     * @return a copy of the provided stack without helper metadata
     */
    public static ItemStack copyWithoutRequestedCount(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack copy = stack.copy();
        removeFullCount(copy);
        return copy;
    }

    private static void removeFullCount(ItemStack stack) {
        if (!stack.hasNbt()) {
            return;
        }

        stack.removeSubNbt(FULL_COUNT_KEY);
        if (stack.getNbt() != null && stack.getNbt().isEmpty()) {
            stack.setNbt(null);
        }
    }
}
