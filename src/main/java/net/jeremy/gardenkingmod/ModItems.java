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
				rottenItems.forEach(entries::add);
			});
	}
}
