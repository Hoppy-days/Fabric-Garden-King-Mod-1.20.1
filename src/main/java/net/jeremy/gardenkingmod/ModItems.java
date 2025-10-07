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
import net.jeremy.gardenkingmod.item.EmeraldArmorMaterial;
import net.jeremy.gardenkingmod.item.EmeraldToolMaterial;
import net.jeremy.gardenkingmod.item.FortuneHoeItem;
import net.jeremy.gardenkingmod.item.ObsidianArmorMaterial;
import net.jeremy.gardenkingmod.item.ObsidianToolMaterial;
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
        public static final Item SCARECROW_SHIRT = registerItem("scarecrow_shirt",
                        new Item(new FabricItemSettings()));
        public static final Item SCARECROW_HEAD = registerItem("scarecrow_head",
                        new Item(new FabricItemSettings()));
        public static final Item SCARECROW_HAT = registerItem("scarecrow_hat",
                        new Item(new FabricItemSettings()));
        public static final Item RUBY_SWORD = registerItem("ruby_sword",
                        new SwordItem(RubyToolMaterial.INSTANCE, 4, -2.2F, new FabricItemSettings()));
        public static final Item RUBY_PICKAXE = registerItem("ruby_pickaxe",
                        new PickaxeItem(RubyToolMaterial.INSTANCE, 2, -2.8F, new FabricItemSettings()));
        public static final Item RUBY_AXE = registerItem("ruby_axe",
                        new AxeItem(RubyToolMaterial.INSTANCE, 6.0F, -3.0F, new FabricItemSettings()));
        public static final Item RUBY_SHOVEL = registerItem("ruby_shovel",
                        new ShovelItem(RubyToolMaterial.INSTANCE, 2.5F, -3.0F, new FabricItemSettings()));
        public static final Item RUBY_HOE = registerItem("ruby_hoe",
                        new FortuneHoeItem(RubyToolMaterial.INSTANCE, -2, 0.0F, new FabricItemSettings(), 5));
        public static final Item OBSIDIAN_SWORD = registerItem("obsidian_sword",
                        new SwordItem(ObsidianToolMaterial.INSTANCE, 5, -2.4F, new FabricItemSettings()));
        public static final Item OBSIDIAN_PICKAXE = registerItem("obsidian_pickaxe",
                        new PickaxeItem(ObsidianToolMaterial.INSTANCE, 3, -2.8F, new FabricItemSettings()));
        public static final Item OBSIDIAN_AXE = registerItem("obsidian_axe",
                        new AxeItem(ObsidianToolMaterial.INSTANCE, 7.0F, -3.1F, new FabricItemSettings()));
        public static final Item OBSIDIAN_SHOVEL = registerItem("obsidian_shovel",
                        new ShovelItem(ObsidianToolMaterial.INSTANCE, 3.5F, -3.0F, new FabricItemSettings()));
        public static final Item OBSIDIAN_HOE = registerItem("obsidian_hoe",
                        new HoeItem(ObsidianToolMaterial.INSTANCE, -4, 0.0F, new FabricItemSettings()));
        public static final Item EMERALD_SWORD = registerItem("emerald_sword",
                        new SwordItem(EmeraldToolMaterial.INSTANCE, 3, -2.2F, new FabricItemSettings()));
        public static final Item EMERALD_PICKAXE = registerItem("emerald_pickaxe",
                        new PickaxeItem(EmeraldToolMaterial.INSTANCE, 1, -2.8F, new FabricItemSettings()));
        public static final Item EMERALD_AXE = registerItem("emerald_axe",
                        new AxeItem(EmeraldToolMaterial.INSTANCE, 5.0F, -3.0F, new FabricItemSettings()));
        public static final Item EMERALD_SHOVEL = registerItem("emerald_shovel",
                        new ShovelItem(EmeraldToolMaterial.INSTANCE, 2.0F, -3.0F, new FabricItemSettings()));
        public static final Item EMERALD_HOE = registerItem("emerald_hoe",
                        new FortuneHoeItem(EmeraldToolMaterial.INSTANCE, -3, 0.0F, new FabricItemSettings(), 4));
        public static final Item RUBY_HELMET = registerItem("ruby_helmet",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));
        public static final Item RUBY_CHESTPLATE = registerItem("ruby_chestplate",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
        public static final Item RUBY_LEGGINGS = registerItem("ruby_leggings",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
        public static final Item RUBY_BOOTS = registerItem("ruby_boots",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));
        public static final Item OBSIDIAN_HELMET = registerItem("obsidian_helmet",
                        new ArmorItem(ObsidianArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));
        public static final Item OBSIDIAN_CHESTPLATE = registerItem("obsidian_chestplate",
                        new ArmorItem(ObsidianArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
        public static final Item OBSIDIAN_LEGGINGS = registerItem("obsidian_leggings",
                        new ArmorItem(ObsidianArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
        public static final Item OBSIDIAN_BOOTS = registerItem("obsidian_boots",
                        new ArmorItem(ObsidianArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));
        public static final Item EMERALD_HELMET = registerItem("emerald_helmet",
                        new ArmorItem(EmeraldArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));
        public static final Item EMERALD_CHESTPLATE = registerItem("emerald_chestplate",
                        new ArmorItem(EmeraldArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
        public static final Item EMERALD_LEGGINGS = registerItem("emerald_leggings",
                        new ArmorItem(EmeraldArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
        public static final Item EMERALD_BOOTS = registerItem("emerald_boots",
                        new ArmorItem(EmeraldArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));

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
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                                .register(entries -> {
                                            entries.add(SCARECROW_SHIRT);
                                            entries.add(SCARECROW_HEAD);
                                            entries.add(SCARECROW_HAT);
                                });
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                                .register(entries -> {
                                        entries.add(RUBY_PICKAXE);
                                        entries.add(RUBY_AXE);
                                        entries.add(RUBY_SHOVEL);
                                        entries.add(RUBY_HOE);
                                        entries.add(OBSIDIAN_PICKAXE);
                                        entries.add(OBSIDIAN_AXE);
                                        entries.add(OBSIDIAN_SHOVEL);
                                        entries.add(OBSIDIAN_HOE);
                                        entries.add(EMERALD_PICKAXE);
                                        entries.add(EMERALD_AXE);
                                        entries.add(EMERALD_SHOVEL);
                                        entries.add(EMERALD_HOE);
                                });
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                                .register(entries -> {
                                        entries.add(RUBY_SWORD);
                                        entries.add(RUBY_HELMET);
                                        entries.add(RUBY_CHESTPLATE);
                                        entries.add(RUBY_LEGGINGS);
                                        entries.add(RUBY_BOOTS);
                                        entries.add(OBSIDIAN_SWORD);
                                        entries.add(OBSIDIAN_HELMET);
                                        entries.add(OBSIDIAN_CHESTPLATE);
                                        entries.add(OBSIDIAN_LEGGINGS);
                                        entries.add(OBSIDIAN_BOOTS);
                                        entries.add(EMERALD_SWORD);
                                        entries.add(EMERALD_HELMET);
                                        entries.add(EMERALD_CHESTPLATE);
                                        entries.add(EMERALD_LEGGINGS);
                                        entries.add(EMERALD_BOOTS);
                                });
        }
}
