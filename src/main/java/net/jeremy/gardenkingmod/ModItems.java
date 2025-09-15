package net.jeremy.gardenkingmod;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
        public static final Item GARDEN_COIN = registerItem("garden_coin", new Item(new FabricItemSettings()));

        private ModItems() {
        }

        private static Item registerItem(String name, Item item) {
                return Registry.register(Registries.ITEM, new Identifier(GardenKingMod.MOD_ID, name), item);
        }

        public static void registerModItems() {
                GardenKingMod.LOGGER.info("Registering mod items for {}", GardenKingMod.MOD_ID);
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(GARDEN_COIN));
        }
}
