package net.jeremy.gardenkingmod.screen.inventory;

import net.minecraft.inventory.SimpleInventory;

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
}
