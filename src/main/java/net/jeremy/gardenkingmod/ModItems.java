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
import net.jeremy.gardenkingmod.item.RubyArmorMaterial;
import net.jeremy.gardenkingmod.item.RubyToolMaterial;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
        public static final Item GARDEN_COIN = registerItem("garden_coin", new Item(new FabricItemSettings()));
        public static final Item RUBY = registerItem("ruby", new Item(new FabricItemSettings()));
        public static final Item RUBY_SWORD = registerItem("ruby_sword",
                        new SwordItem(RubyToolMaterial.INSTANCE, 4, -2.2F, new FabricItemSettings()));
        public static final Item RUBY_PICKAXE = registerItem("ruby_pickaxe",
                        new PickaxeItem(RubyToolMaterial.INSTANCE, 2, -2.8F, new FabricItemSettings()));
        public static final Item RUBY_AXE = registerItem("ruby_axe",
                        new AxeItem(RubyToolMaterial.INSTANCE, 6.0F, -3.0F, new FabricItemSettings()));
        public static final Item RUBY_SHOVEL = registerItem("ruby_shovel",
                        new ShovelItem(RubyToolMaterial.INSTANCE, 2.5F, -3.0F, new FabricItemSettings()));
        public static final Item RUBY_HOE = registerItem("ruby_hoe",
                        new HoeItem(RubyToolMaterial.INSTANCE, -2, 0.0F, new FabricItemSettings()));
        public static final Item RUBY_HELMET = registerItem("ruby_helmet",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));
        public static final Item RUBY_CHESTPLATE = registerItem("ruby_chestplate",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
        public static final Item RUBY_LEGGINGS = registerItem("ruby_leggings",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
        public static final Item RUBY_BOOTS = registerItem("ruby_boots",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));

        private static Map<Identifier, Item> rottenItemsByCrop = Collections.emptyMap();
        private static Map<Identifier, Item> rottenItemsByTarget = Collections.emptyMap();
        private static List<Item> rottenItems = List.of();
        private static boolean rottenItemsRegistered;

        private ModItems() {
        }

        private static Item registerItem(String name, Item item) {
                return Registry.register(Registries.ITEM, new Identifier(GardenKingMod.MOD_ID, name), item);
        }

        public static synchronized boolean initializeRottenItems() {
                if (rottenItemsRegistered) {
                        return true;
                }

                RottenCropDefinitions.reload();
                List<RottenCropDefinition> definitions = RottenCropDefinitions.all();
                if (definitions.isEmpty()) {
                        return false;
                }

                Map<Identifier, Item> byCrop = new LinkedHashMap<>();
                Map<Identifier, Item> byTarget = new LinkedHashMap<>();
                List<Item> items = new ArrayList<>();

                for (RottenCropDefinition definition : definitions) {
                        Item item = registerItem(definition.rottenItemId().getPath(), new Item(new FabricItemSettings()));
                        byCrop.put(definition.cropId(), item);
                        byTarget.put(definition.targetId(), item);
                        items.add(item);
                }

                rottenItemsByCrop = Collections.unmodifiableMap(byCrop);
                rottenItemsByTarget = Collections.unmodifiableMap(byTarget);
                rottenItems = List.copyOf(items);
                rottenItemsRegistered = true;
                return true;
        }

        public static Item getRottenItemForCrop(Identifier cropId) {
                return initializeRottenItems() ? rottenItemsByCrop.get(cropId) : null;
        }

        public static Item getRottenItemForTarget(Identifier targetId) {
                return initializeRottenItems() ? rottenItemsByTarget.get(targetId) : null;
        }

        public static Collection<Item> getRottenItems() {
                initializeRottenItems();
                return rottenItems;
        }

        public static void registerModItems() {
                GardenKingMod.LOGGER.info("Registering mod items for {}", GardenKingMod.MOD_ID);
                if (!initializeRottenItems()) {
                        GardenKingMod.LOGGER.warn("Skipping rotten item registration until crop definitions are available");
                }
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                                .register(entries -> {
                                        entries.add(GARDEN_COIN);
                                        entries.add(RUBY);
                                        rottenItems.forEach(entries::add);
                                });
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                                .register(entries -> {
                                        entries.add(RUBY_PICKAXE);
                                        entries.add(RUBY_AXE);
                                        entries.add(RUBY_SHOVEL);
                                        entries.add(RUBY_HOE);
                                });
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                                .register(entries -> {
                                        entries.add(RUBY_SWORD);
                                        entries.add(RUBY_HELMET);
                                        entries.add(RUBY_CHESTPLATE);
                                        entries.add(RUBY_LEGGINGS);
                                        entries.add(RUBY_BOOTS);
                                });
        }
}
