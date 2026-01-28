package net.jeremy.gardenkingmod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.jeremy.gardenkingmod.crop.EnchantedCropDefinition;
import net.jeremy.gardenkingmod.crop.EnchantedCropDefinitions;
import net.jeremy.gardenkingmod.crop.RottenCropDefinition;
import net.jeremy.gardenkingmod.crop.RottenCropDefinitions;
import net.jeremy.gardenkingmod.item.AmethystArmorMaterial;
import net.jeremy.gardenkingmod.item.AmethystToolMaterial;
import net.jeremy.gardenkingmod.item.BuffedAxeItem;
import net.jeremy.gardenkingmod.item.BuffedHoeItem;
import net.jeremy.gardenkingmod.item.BuffedPickaxeItem;
import net.jeremy.gardenkingmod.item.BuffedShovelItem;
import net.jeremy.gardenkingmod.item.BuffedSwordItem;
import net.jeremy.gardenkingmod.item.BlueSapphireArmorMaterial;
import net.jeremy.gardenkingmod.item.BlueSapphireToolMaterial;
import net.jeremy.gardenkingmod.item.CompostFertilizerItem;
import net.jeremy.gardenkingmod.item.EmeraldArmorMaterial;
import net.jeremy.gardenkingmod.item.EmeraldToolMaterial;
import net.jeremy.gardenkingmod.item.EnchantedCropItem;
import net.jeremy.gardenkingmod.item.FertilizerBalanceConfig;
import net.jeremy.gardenkingmod.item.ObsidianArmorMaterial;
import net.jeremy.gardenkingmod.item.ObsidianToolMaterial;
import net.jeremy.gardenkingmod.item.PearlArmorMaterial;
import net.jeremy.gardenkingmod.item.PearlToolMaterial;
import net.jeremy.gardenkingmod.item.RubyArmorMaterial;
import net.jeremy.gardenkingmod.item.RubyToolMaterial;
import net.jeremy.gardenkingmod.item.TopazArmorMaterial;
import net.jeremy.gardenkingmod.item.TopazToolMaterial;
import net.jeremy.gardenkingmod.item.WalletItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Rarity;

public final class ModItems {
        public static final Item WALLET = registerItem("wallet", new WalletItem(new FabricItemSettings()));
        public static final Item DOLLAR = registerItem("dollar", new Item(new FabricItemSettings()));
        public static final Item RUBY = registerItem("ruby", new Item(new FabricItemSettings()));
        public static final Item BLUE_SAPPHIRE = registerItem("blue_sapphire", new Item(new FabricItemSettings()));
        public static final Item TOPAZ = registerItem("topaz", new Item(new FabricItemSettings()));
        public static final Item PEARL = registerItem("pearl", new Item(new FabricItemSettings()));
        public static final Item SCARECROW_SHIRT = registerItem("scarecrow_shirt",
                        new Item(new FabricItemSettings()));
        public static final Item SCARECROW_HEAD = registerItem("scarecrow_head",
                        new Item(new FabricItemSettings()));
        public static final Item SCARECROW_HAT = registerItem("scarecrow_hat",
                        new Item(new FabricItemSettings()));
        public static final Item RUBY_SWORD = registerItem("ruby_sword",
                        new BuffedSwordItem(RubyToolMaterial.INSTANCE, 4, -2.2F, new FabricItemSettings()));
        public static final Item BLUE_SAPPHIRE_SWORD = registerItem("blue_sapphire_sword",
                        new BuffedSwordItem(BlueSapphireToolMaterial.INSTANCE, 3, -2.0F, new FabricItemSettings()));
        public static final Item TOPAZ_SWORD = registerItem("topaz_sword",
                        new BuffedSwordItem(TopazToolMaterial.INSTANCE, 3, -2.0F, new FabricItemSettings()));
        public static final Item PEARL_SWORD = registerItem("pearl_sword",
                        new BuffedSwordItem(PearlToolMaterial.INSTANCE, 5, -2.2F, new FabricItemSettings()));
        public static final Item AMETHYST_SWORD = registerItem("amethyst_sword",
                        new BuffedSwordItem(AmethystToolMaterial.INSTANCE, 4, -2.1F, new FabricItemSettings()));
        public static final Item RUBY_PICKAXE = registerItem("ruby_pickaxe",
                        new BuffedPickaxeItem(RubyToolMaterial.INSTANCE, 2, -2.8F, new FabricItemSettings()));
        public static final Item BLUE_SAPPHIRE_PICKAXE = registerItem("blue_sapphire_pickaxe",
                        new BuffedPickaxeItem(BlueSapphireToolMaterial.INSTANCE, 1, -2.8F, new FabricItemSettings()));
        public static final Item TOPAZ_PICKAXE = registerItem("topaz_pickaxe",
                        new BuffedPickaxeItem(TopazToolMaterial.INSTANCE, 2, -2.8F, new FabricItemSettings()));
        public static final Item PEARL_PICKAXE = registerItem("pearl_pickaxe",
                        new BuffedPickaxeItem(PearlToolMaterial.INSTANCE, 3, -2.8F, new FabricItemSettings()));
        public static final Item AMETHYST_PICKAXE = registerItem("amethyst_pickaxe",
                        new BuffedPickaxeItem(AmethystToolMaterial.INSTANCE, 2, -2.8F, new FabricItemSettings()));
        public static final Item RUBY_AXE = registerItem("ruby_axe",
                        new BuffedAxeItem(RubyToolMaterial.INSTANCE, 6.0F, -3.0F, new FabricItemSettings()));
        public static final Item BLUE_SAPPHIRE_AXE = registerItem("blue_sapphire_axe",
                        new BuffedAxeItem(BlueSapphireToolMaterial.INSTANCE, 5.5F, -3.0F, new FabricItemSettings()));
        public static final Item TOPAZ_AXE = registerItem("topaz_axe",
                        new BuffedAxeItem(TopazToolMaterial.INSTANCE, 6.0F, -3.0F, new FabricItemSettings()));
        public static final Item PEARL_AXE = registerItem("pearl_axe",
                        new BuffedAxeItem(PearlToolMaterial.INSTANCE, 6.5F, -3.0F, new FabricItemSettings()));
        public static final Item AMETHYST_AXE = registerItem("amethyst_axe",
                        new BuffedAxeItem(AmethystToolMaterial.INSTANCE, 6.5F, -3.0F, new FabricItemSettings()));
        public static final Item RUBY_SHOVEL = registerItem("ruby_shovel",
                        new BuffedShovelItem(RubyToolMaterial.INSTANCE, 2.5F, -3.0F, new FabricItemSettings()));
        public static final Item BLUE_SAPPHIRE_SHOVEL = registerItem("blue_sapphire_shovel",
                        new BuffedShovelItem(BlueSapphireToolMaterial.INSTANCE, 2.5F, -3.0F, new FabricItemSettings()));
        public static final Item TOPAZ_SHOVEL = registerItem("topaz_shovel",
                        new BuffedShovelItem(TopazToolMaterial.INSTANCE, 2.5F, -3.0F, new FabricItemSettings()));
        public static final Item PEARL_SHOVEL = registerItem("pearl_shovel",
                        new BuffedShovelItem(PearlToolMaterial.INSTANCE, 3.0F, -3.0F, new FabricItemSettings()));
        public static final Item AMETHYST_SHOVEL = registerItem("amethyst_shovel",
                        new BuffedShovelItem(AmethystToolMaterial.INSTANCE, 3.0F, -3.0F, new FabricItemSettings()));
        public static final Item RUBY_HOE = registerItem("ruby_hoe",
                        new BuffedHoeItem(RubyToolMaterial.INSTANCE, -2, 0.0F, new FabricItemSettings()));
        public static final Item BLUE_SAPPHIRE_HOE = registerItem("blue_sapphire_hoe",
                        new BuffedHoeItem(BlueSapphireToolMaterial.INSTANCE, -2, 0.0F, new FabricItemSettings()));
        public static final Item TOPAZ_HOE = registerItem("topaz_hoe",
                        new BuffedHoeItem(TopazToolMaterial.INSTANCE, -2, 0.0F, new FabricItemSettings()));
        public static final Item PEARL_HOE = registerItem("pearl_hoe",
                        new BuffedHoeItem(PearlToolMaterial.INSTANCE, -2, 0.0F, new FabricItemSettings()));
        public static final Item AMETHYST_HOE = registerItem("amethyst_hoe",
                        new BuffedHoeItem(AmethystToolMaterial.INSTANCE, -2, 0.0F, new FabricItemSettings()));
        public static final Item OBSIDIAN_SWORD = registerItem("obsidian_sword",
                        new BuffedSwordItem(ObsidianToolMaterial.INSTANCE, 5, -2.4F, new FabricItemSettings()));
        public static final Item OBSIDIAN_PICKAXE = registerItem("obsidian_pickaxe",
                        new BuffedPickaxeItem(ObsidianToolMaterial.INSTANCE, 3, -2.8F, new FabricItemSettings()));
        public static final Item OBSIDIAN_AXE = registerItem("obsidian_axe",
                        new BuffedAxeItem(ObsidianToolMaterial.INSTANCE, 7.0F, -3.1F, new FabricItemSettings()));
        public static final Item OBSIDIAN_SHOVEL = registerItem("obsidian_shovel",
                        new BuffedShovelItem(ObsidianToolMaterial.INSTANCE, 3.5F, -3.0F, new FabricItemSettings()));
        public static final Item OBSIDIAN_HOE = registerItem("obsidian_hoe",
                        new BuffedHoeItem(ObsidianToolMaterial.INSTANCE, -4, 0.0F, new FabricItemSettings()));
        public static final Item EMERALD_SWORD = registerItem("emerald_sword",
                        new BuffedSwordItem(EmeraldToolMaterial.INSTANCE, 3, -2.2F, new FabricItemSettings()));
        public static final Item EMERALD_PICKAXE = registerItem("emerald_pickaxe",
                        new BuffedPickaxeItem(EmeraldToolMaterial.INSTANCE, 1, -2.8F, new FabricItemSettings()));
        public static final Item EMERALD_AXE = registerItem("emerald_axe",
                        new BuffedAxeItem(EmeraldToolMaterial.INSTANCE, 5.0F, -3.0F, new FabricItemSettings()));
        public static final Item EMERALD_SHOVEL = registerItem("emerald_shovel",
                        new BuffedShovelItem(EmeraldToolMaterial.INSTANCE, 2.0F, -3.0F, new FabricItemSettings()));
        public static final Item EMERALD_HOE = registerItem("emerald_hoe",
                        new BuffedHoeItem(EmeraldToolMaterial.INSTANCE, -3, 0.0F, new FabricItemSettings()));
        public static final Item RUBY_HELMET = registerItem("ruby_helmet",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));
        public static final Item BLUE_SAPPHIRE_HELMET = registerItem("blue_sapphire_helmet",
                        new ArmorItem(BlueSapphireArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));
        public static final Item TOPAZ_HELMET = registerItem("topaz_helmet",
                        new ArmorItem(TopazArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));
        public static final Item PEARL_HELMET = registerItem("pearl_helmet",
                        new ArmorItem(PearlArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));
        public static final Item RUBY_CHESTPLATE = registerItem("ruby_chestplate",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
        public static final Item BLUE_SAPPHIRE_CHESTPLATE = registerItem("blue_sapphire_chestplate",
                        new ArmorItem(BlueSapphireArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
        public static final Item TOPAZ_CHESTPLATE = registerItem("topaz_chestplate",
                        new ArmorItem(TopazArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
        public static final Item PEARL_CHESTPLATE = registerItem("pearl_chestplate",
                        new ArmorItem(PearlArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
        public static final Item RUBY_LEGGINGS = registerItem("ruby_leggings",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
        public static final Item BLUE_SAPPHIRE_LEGGINGS = registerItem("blue_sapphire_leggings",
                        new ArmorItem(BlueSapphireArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
        public static final Item TOPAZ_LEGGINGS = registerItem("topaz_leggings",
                        new ArmorItem(TopazArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
        public static final Item PEARL_LEGGINGS = registerItem("pearl_leggings",
                        new ArmorItem(PearlArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
        public static final Item RUBY_BOOTS = registerItem("ruby_boots",
                        new ArmorItem(RubyArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));
        public static final Item BLUE_SAPPHIRE_BOOTS = registerItem("blue_sapphire_boots",
                        new ArmorItem(BlueSapphireArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));
        public static final Item TOPAZ_BOOTS = registerItem("topaz_boots",
                        new ArmorItem(TopazArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));
        public static final Item PEARL_BOOTS = registerItem("pearl_boots",
                        new ArmorItem(PearlArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));
        public static final Item AMETHYST_HELMET = registerItem("amethyst_helmet",
                        new ArmorItem(AmethystArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));
        public static final Item AMETHYST_CHESTPLATE = registerItem("amethyst_chestplate",
                        new ArmorItem(AmethystArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
        public static final Item AMETHYST_LEGGINGS = registerItem("amethyst_leggings",
                        new ArmorItem(AmethystArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
        public static final Item AMETHYST_BOOTS = registerItem("amethyst_boots",
                        new ArmorItem(AmethystArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));
        public static final Item SPECIAL_FERTILIZER = registerItem("special_fertilizer",
                        new CompostFertilizerItem(new FabricItemSettings()));
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
    private static Map<Identifier, RottenCropDefinition> rottenDefinitionsByItem = Map.of();
    private static List<Item> rottenItems = List.of();
    private static boolean rottenItemsRegistered;

    private static Map<Identifier, Item> enchantedItemsByCrop = Collections.emptyMap();
    private static Map<Identifier, Item> enchantedItemsByTarget = Collections.emptyMap();
    private static Map<Identifier, EnchantedCropDefinition> enchantedDefinitionsByItem = Map.of();
    private static List<Item> enchantedItems = List.of();
    private static boolean enchantedItemsRegistered;

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
                Map<Identifier, RottenCropDefinition> byItemId = new LinkedHashMap<>();
                List<Item> items = new ArrayList<>();

                for (RottenCropDefinition definition : definitions) {
                        Identifier rottenId = definition.rottenItemId();
                        Item item = registerItem(rottenId.getPath(), new Item(new FabricItemSettings()));
                        byCrop.put(definition.cropId(), item);
                        byTarget.put(definition.targetId(), item);
                        byItemId.put(rottenId, definition);
                        items.add(item);
                }

                rottenItemsByCrop = Collections.unmodifiableMap(byCrop);
                rottenItemsByTarget = Collections.unmodifiableMap(byTarget);
                rottenDefinitionsByItem = Collections.unmodifiableMap(byItemId);
                rottenItems = List.copyOf(items);
                rottenItemsRegistered = true;
                return true;
        }

        public static synchronized boolean initializeEnchantedItems() {
                if (enchantedItemsRegistered) {
                        return true;
                }

                EnchantedCropDefinitions.reload();
                List<EnchantedCropDefinition> definitions = EnchantedCropDefinitions.all();
                if (definitions.isEmpty()) {
                        return false;
                }

                Map<Identifier, Item> byCrop = new LinkedHashMap<>();
                Map<Identifier, Item> byTarget = new LinkedHashMap<>();
                Map<Identifier, EnchantedCropDefinition> byItemId = new LinkedHashMap<>();
                List<Item> items = new ArrayList<>();

                for (EnchantedCropDefinition definition : definitions) {
                        Identifier enchantedId = definition.enchantedItemId();
                        Text name = Text.translatable(enchantedId.toTranslationKey("item"));
                        Item item = registerItem(enchantedId.getPath(),
                                        new EnchantedCropItem(new FabricItemSettings().rarity(Rarity.RARE),
                                                        definition.effectiveValueMultiplier(), name));

                        byCrop.put(definition.cropId(), item);
                        byTarget.put(definition.targetId(), item);
                        byItemId.put(enchantedId, definition);
                        items.add(item);
                }

                enchantedItemsByCrop = Collections.unmodifiableMap(byCrop);
                enchantedItemsByTarget = Collections.unmodifiableMap(byTarget);
                enchantedDefinitionsByItem = Collections.unmodifiableMap(byItemId);
                enchantedItems = List.copyOf(items);
                enchantedItemsRegistered = true;
                return true;
        }

        public static Item getRottenItemForCrop(Identifier cropId) {
                return initializeRottenItems() ? rottenItemsByCrop.get(cropId) : null;
        }

        public static Item getRottenItemForTarget(Identifier targetId) {
                return initializeRottenItems() ? rottenItemsByTarget.get(targetId) : null;
        }

        public static Optional<RottenCropDefinition> getRottenDefinition(Item item) {
                if (!initializeRottenItems()) {
                        return Optional.empty();
                }

                if (item == null) {
                        return Optional.empty();
                }

                Identifier id = Registries.ITEM.getId(item);
                if (id == null) {
                        return Optional.empty();
                }

                return Optional.ofNullable(rottenDefinitionsByItem.get(id));
        }

        public static boolean isRottenItem(Item item) {
                if (!initializeRottenItems()) {
                        return false;
                }

                if (item == null) {
                        return false;
                }

                Identifier id = Registries.ITEM.getId(item);
                return id != null && rottenDefinitionsByItem.containsKey(id);
        }

        public static Item getEnchantedItemForCrop(Identifier cropId) {
                return initializeEnchantedItems() ? enchantedItemsByCrop.get(cropId) : null;
        }

        public static Item getEnchantedItemForTarget(Identifier targetId) {
                return initializeEnchantedItems() ? enchantedItemsByTarget.get(targetId) : null;
        }

        public static Optional<EnchantedCropDefinition> getEnchantedDefinition(Item item) {
                if (!initializeEnchantedItems()) {
                        return Optional.empty();
                }

                if (item == null) {
                        return Optional.empty();
                }

                Identifier id = Registries.ITEM.getId(item);
                if (id == null) {
                        return Optional.empty();
                }

                return Optional.ofNullable(enchantedDefinitionsByItem.get(id));
        }

        public static boolean isEnchantedItem(Item item) {
                if (!initializeEnchantedItems()) {
                        return false;
                }

                if (item == null) {
                        return false;
                }

                Identifier id = Registries.ITEM.getId(item);
                return id != null && enchantedDefinitionsByItem.containsKey(id);
        }

        public static float getEnchantedValueMultiplier(Item item) {
                return getEnchantedDefinition(item).map(EnchantedCropDefinition::effectiveValueMultiplier).orElse(1.0f);
        }

        public static Collection<Item> getEnchantedItems() {
                initializeEnchantedItems();
                return enchantedItems;
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
                if (!initializeEnchantedItems()) {
                        GardenKingMod.LOGGER.warn(
                                        "Skipping enchanted item registration until crop definitions are available");
                }
                registerCompostables();
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                                .register(entries -> {
                                        entries.add(DOLLAR);
                                        entries.add(WALLET);
                                        entries.add(RUBY);
                                        entries.add(BLUE_SAPPHIRE);
                                        entries.add(TOPAZ);
                                        entries.add(PEARL);
                                        entries.add(SPECIAL_FERTILIZER);
                                        rottenItems.forEach(entries::add);
                                        enchantedItems.forEach(entries::add);
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
                                        entries.add(BLUE_SAPPHIRE_PICKAXE);
                                        entries.add(BLUE_SAPPHIRE_AXE);
                                        entries.add(BLUE_SAPPHIRE_SHOVEL);
                                        entries.add(BLUE_SAPPHIRE_HOE);
                                        entries.add(TOPAZ_PICKAXE);
                                        entries.add(TOPAZ_AXE);
                                        entries.add(TOPAZ_SHOVEL);
                                        entries.add(TOPAZ_HOE);
                                        entries.add(PEARL_PICKAXE);
                                        entries.add(PEARL_AXE);
                                        entries.add(PEARL_SHOVEL);
                                        entries.add(PEARL_HOE);
                                        entries.add(AMETHYST_PICKAXE);
                                        entries.add(AMETHYST_AXE);
                                        entries.add(AMETHYST_SHOVEL);
                                        entries.add(AMETHYST_HOE);
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
                                        entries.add(BLUE_SAPPHIRE_SWORD);
                                        entries.add(TOPAZ_SWORD);
                                        entries.add(PEARL_SWORD);
                                        entries.add(BLUE_SAPPHIRE_HELMET);
                                        entries.add(BLUE_SAPPHIRE_CHESTPLATE);
                                        entries.add(BLUE_SAPPHIRE_LEGGINGS);
                                        entries.add(BLUE_SAPPHIRE_BOOTS);
                                        entries.add(TOPAZ_HELMET);
                                        entries.add(TOPAZ_CHESTPLATE);
                                        entries.add(TOPAZ_LEGGINGS);
                                        entries.add(TOPAZ_BOOTS);
                                        entries.add(PEARL_HELMET);
                                        entries.add(PEARL_CHESTPLATE);
                                        entries.add(PEARL_LEGGINGS);
                                        entries.add(PEARL_BOOTS);
                                        entries.add(AMETHYST_SWORD);
                                        entries.add(AMETHYST_HELMET);
                                        entries.add(AMETHYST_CHESTPLATE);
                                        entries.add(AMETHYST_LEGGINGS);
                                        entries.add(AMETHYST_BOOTS);
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

        private static void registerCompostables() {
                double chanceValue = MathHelper.clamp(FertilizerBalanceConfig.get().rottenCompostChance(), 0.0, 1.0);
                if (chanceValue <= 0.0) {
                        return;
                }

                float chance = (float) chanceValue;
                for (Item item : rottenItems) {
                        CompostingChanceRegistry.INSTANCE.add(item, chance);
                }
        }
}
