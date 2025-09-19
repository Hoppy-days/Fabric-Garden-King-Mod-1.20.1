package net.jeremy.gardenkingmod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.jeremy.gardenkingmod.crop.RottenCropDefinition;
import net.jeremy.gardenkingmod.crop.RottenCropDefinitions;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
        public static final Item GARDEN_COIN = registerItem("garden_coin", new Item(new FabricItemSettings()));

        private static final Map<Identifier, Item> ROTTEN_ITEMS_BY_CROP;
        private static final Map<Identifier, Item> ROTTEN_ITEMS_BY_TARGET;
        private static final List<Item> ROTTEN_ITEMS;

        static {
                Map<Identifier, Item> byCrop = new LinkedHashMap<>();
                Map<Identifier, Item> byTarget = new LinkedHashMap<>();
                List<Item> items = new ArrayList<>();

                for (RottenCropDefinition definition : RottenCropDefinitions.all()) {
                        Item item = registerItem(definition.rottenItemId().getPath(), new Item(new FabricItemSettings()));
                        byCrop.put(definition.cropId(), item);
                        byTarget.put(definition.targetId(), item);
                        items.add(item);
                }

                ROTTEN_ITEMS_BY_CROP = Collections.unmodifiableMap(byCrop);
                ROTTEN_ITEMS_BY_TARGET = Collections.unmodifiableMap(byTarget);
                ROTTEN_ITEMS = List.copyOf(items);
        }

        private ModItems() {
        }

        private static Item registerItem(String name, Item item) {
                return Registry.register(Registries.ITEM, new Identifier(GardenKingMod.MOD_ID, name), item);
        }

        public static Item getRottenItemForCrop(Identifier cropId) {
                return ROTTEN_ITEMS_BY_CROP.get(cropId);
        }

        public static Item getRottenItemForTarget(Identifier targetId) {
                return ROTTEN_ITEMS_BY_TARGET.get(targetId);
        }

        public static Collection<Item> getRottenItems() {
                return ROTTEN_ITEMS;
        }

        public static void registerModItems() {
                GardenKingMod.LOGGER.info("Registering mod items for {}", GardenKingMod.MOD_ID);
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                                .register(entries -> {
                                        entries.add(GARDEN_COIN);
                                        ROTTEN_ITEMS.forEach(entries::add);
                                });
        }
}
