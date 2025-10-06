package net.jeremy.gardenkingmod.crop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.util.JsonCommentHelper;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;

/**
 * Builds the list of {@link RottenCropDefinition definitions} that power both
 * runtime registration and data generation.
 */
public final class RottenCropDefinitions {
	private static List<RottenCropDefinition> allDefinitions = List.of();
	private static Map<Identifier, RottenCropDefinition> definitionsByTarget = Map.of();
	private static Map<Identifier, RottenCropDefinition> definitionsByCrop = Map.of();
	private static Map<Identifier, RottenCropDefinition> definitionsByLootTable = Map.of();
	private static Map<Identifier, RottenCropDefinition> definitionsByRottenItem = Map.of();
	private static boolean initialized;

	private RottenCropDefinitions() {
	}

	public static List<RottenCropDefinition> all() {
		ensureLoaded();
		return allDefinitions;
	}

	public static boolean hasDefinitions() {
		return initialized && !allDefinitions.isEmpty();
	}

	public static Optional<RottenCropDefinition> findByTargetId(Identifier identifier) {
		ensureLoaded();
		return Optional.ofNullable(definitionsByTarget.get(identifier));
	}

	public static Optional<RottenCropDefinition> findByCropId(Identifier identifier) {
		ensureLoaded();
		return Optional.ofNullable(definitionsByCrop.get(identifier));
	}

	public static Optional<RottenCropDefinition> findByLootTableId(Identifier identifier) {
		ensureLoaded();
		return Optional.ofNullable(definitionsByLootTable.get(identifier));
	}

	public static Optional<RottenCropDefinition> findByRottenItemId(Identifier identifier) {
		ensureLoaded();
		return Optional.ofNullable(definitionsByRottenItem.get(identifier));
	}

	public static synchronized void reload() {
		List<RottenCropDefinition> definitions = createDefinitions();
		allDefinitions = definitions;
		definitionsByTarget = indexBy(definitions, RottenCropDefinition::targetId);
		definitionsByCrop = indexBy(definitions, RottenCropDefinition::cropId);
		definitionsByLootTable = indexBy(definitions, RottenCropDefinition::lootTableId);
		definitionsByRottenItem = indexBy(definitions, RottenCropDefinition::rottenItemId);
		initialized = true;
	}

	private static void ensureLoaded() {
		if (!initialized) {
			reload();
		}
	}

	private static Map<Identifier, RottenCropDefinition> indexBy(List<RottenCropDefinition> definitions,
			Function<RottenCropDefinition, Identifier> keyExtractor) {
		Map<Identifier, RottenCropDefinition> map = new LinkedHashMap<>();
		for (RottenCropDefinition definition : definitions) {
			map.put(keyExtractor.apply(definition), definition);
		}

		return Collections.unmodifiableMap(map);
	}

        private static final String RESOURCE_PATH = "/data/" + GardenKingMod.MOD_ID + "/rotten_crops.json";

        private static List<RottenCropDefinition> createDefinitions() {
                List<RottenCropDefinition> manualList = manualDefinitions();
                if (manualList.isEmpty()) {
                        return List.of();
                }

                Map<Identifier, RottenCropDefinition> definitionsByTarget = new LinkedHashMap<>();
                Set<Identifier> usedRottenIds = new HashSet<>();

                for (RottenCropDefinition definition : manualList) {
                        Identifier targetId = definition.targetId();
                        Identifier rottenItemId = definition.rottenItemId();

                        if (definitionsByTarget.containsKey(targetId)) {
                                GardenKingMod.LOGGER.warn(
                                                "Duplicate rotten crop target {} found while reading {}. Skipping later entry.",
                                                targetId, RESOURCE_PATH);
                                continue;
                        }

                        if (usedRottenIds.contains(rottenItemId)) {
                                GardenKingMod.LOGGER.warn(
                                                "Duplicate rotten item {} found while reading {}. Skipping entry for {}.",
                                                rottenItemId, RESOURCE_PATH, targetId);
                                continue;
                        }

                        definitionsByTarget.put(targetId, definition);
                        usedRottenIds.add(rottenItemId);
                }

                List<RottenCropDefinition> definitions = new ArrayList<>(definitionsByTarget.values());
                definitions.sort(Comparator.comparing(definition -> definition.rottenItemId().toString()));
                return List.copyOf(definitions);
        }

        private static List<RottenCropDefinition> manualDefinitions() {
                // Textures for each rotten item should be placed in
                // src/main/resources/assets/gardenkingmod/textures/item/.
                return loadDefinitionsFromResource();
        }

        private static List<RottenCropDefinition> loadDefinitionsFromResource() {
                Optional<Collection<Path>> rootPaths = FabricLoader.getInstance()
                                .getModContainer(GardenKingMod.MOD_ID)
                                .map(ModContainer::getRootPaths);

                if (rootPaths.isPresent()) {
                        for (Path rootPath : rootPaths.get()) {
                                Path dataPath = rootPath.resolve("data").resolve(GardenKingMod.MOD_ID)
                                                .resolve("rotten_crops.json");
                                if (Files.isRegularFile(dataPath)) {
                                        try (BufferedReader reader = Files.newBufferedReader(dataPath,
                                                        StandardCharsets.UTF_8)) {
                                                return parseDefinitions(reader);
                                        } catch (JsonParseException | IOException exception) {
                                                GardenKingMod.LOGGER.error(
                                                                "Failed to read rotten crop definitions from {}",
                                                                RESOURCE_PATH, exception);
                                                return List.of();
                                        }
                                }
                        }
                }

                InputStream stream = RottenCropDefinitions.class.getResourceAsStream(RESOURCE_PATH);
                if (stream == null) {
                        GardenKingMod.LOGGER.info(
                                        "No rotten crop list found at {}. Add the file to register custom rotten items.",
                                        RESOURCE_PATH);
                        return List.of();
                }

                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        return parseDefinitions(reader);
                } catch (JsonParseException | IOException exception) {
                        GardenKingMod.LOGGER.error("Failed to read rotten crop definitions from {}", RESOURCE_PATH, exception);
                        return List.of();
                }
        }

        private static List<RottenCropDefinition> parseDefinitions(Reader reader) {
                List<RottenCropDefinition> definitions = new ArrayList<>();

                JsonElement root = JsonParser.parseReader(reader);
                JsonElement sanitized = JsonCommentHelper.sanitize(root);
                JsonArray entries = extractEntriesArray(sanitized);
                if (entries == null) {
                        GardenKingMod.LOGGER.warn("Expected an array of rotten crop entries in {}.", RESOURCE_PATH);
                        return List.of();
                }

                for (int index = 0; index < entries.size(); index++) {
                        JsonElement element = entries.get(index);
                        if (!element.isJsonObject()) {
                                GardenKingMod.LOGGER.warn("Ignoring non-object entry at index {} while reading {}.", index,
                                                RESOURCE_PATH);
                                continue;
                        }

                        RottenCropDefinition definition = parseDefinition(element.getAsJsonObject(), index);
                        if (definition != null) {
                                definitions.add(definition);
                        }
                }

                return List.copyOf(definitions);
        }

        private static JsonArray extractEntriesArray(JsonElement root) {
                if (root == null || root.isJsonNull()) {
                        return null;
                }

                if (root.isJsonArray()) {
                        return root.getAsJsonArray();
                }

                if (root.isJsonObject()) {
                        JsonObject object = root.getAsJsonObject();
                        if (object.has("entries")) {
                                JsonElement entries = object.get("entries");
                                if (entries.isJsonArray()) {
                                        return entries.getAsJsonArray();
                                }
                        }

                        if (object.has("crops")) {
                                JsonElement entries = object.get("crops");
                                if (entries.isJsonArray()) {
                                        return entries.getAsJsonArray();
                                }
                        }
                }

                return null;
        }

        private static RottenCropDefinition parseDefinition(JsonObject object, int index) {
                Identifier rottenItemId = parseIdentifier(object, "rotten_item", "entry " + index);
                if (rottenItemId == null) {
                        return null;
                }

                String entryLabel = rottenItemId.toString();

                if (!GardenKingMod.MOD_ID.equals(rottenItemId.getNamespace())) {
                        GardenKingMod.LOGGER.warn(
                                        "Skipping rotten crop entry {} because rotten_item {} must use the {} namespace.",
                                        entryLabel, rottenItemId, GardenKingMod.MOD_ID);
                        return null;
                }

                Identifier targetId = parseIdentifier(object, "target", entryLabel);
                if (targetId == null) {
                        return null;
                }

                Optional<Block> blockOptional = Registries.BLOCK.getOrEmpty(targetId);
                Block block = blockOptional.orElse(null);

                if (block == null && !object.has("crop")) {
                        GardenKingMod.LOGGER.warn(
                                        "Skipping rotten crop entry {} because target block {} is not registered.",
                                        entryLabel, targetId);
                        return null;
                }

                Identifier cropId;
                if (object.has("crop")) {
                        cropId = parseIdentifier(object, "crop", entryLabel);
                        if (cropId == null) {
                                return null;
                        }
                } else if (block != null) {
                        cropId = resolveCropIdentifier(block, targetId);
                        if (cropId == null) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping rotten crop entry {} because the crop item could not be determined.",
                                                entryLabel);
                                return null;
                        }
                } else {
                        GardenKingMod.LOGGER.warn(
                                        "Skipping rotten crop entry {} because the crop item could not be determined.",
                                        entryLabel);
                        return null;
                }

                if (Registries.ITEM.getOrEmpty(cropId).isEmpty()) {
                        GardenKingMod.LOGGER.warn(
                                        "Crop item {} referenced by rotten crop entry {} is not registered. The rotten item will be created regardless.",
                                        cropId, entryLabel);
                }

                Identifier lootTableId;
                if (object.has("loot_table")) {
                        lootTableId = parseIdentifier(object, "loot_table", entryLabel);
                        if (lootTableId == null) {
                                return null;
                        }
                } else if (block != null) {
                        lootTableId = block.getLootTableId();
                        if (LootTables.EMPTY.equals(lootTableId)) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping rotten crop entry {} because target {} does not have a loot table. Specify one with 'loot_table'.",
                                                entryLabel, targetId);
                                return null;
                        }
                } else {
                        lootTableId = createDefaultLootTableId(targetId);
                        if (lootTableId == null) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping rotten crop entry {} because target {} does not have a loot table. Specify one with 'loot_table'.",
                                                entryLabel, targetId);
                                return null;
                        }
                }

                float extraNoDropChance = readChance(object, "extra_no_drop_chance", entryLabel);
                float extraRottenChance = readChance(object, "extra_rotten_chance", entryLabel);

                return new RottenCropDefinition(cropId, targetId, lootTableId, rottenItemId.getPath(), extraNoDropChance,
                                extraRottenChance);
        }

        private static Identifier createDefaultLootTableId(Identifier targetId) {
                if (targetId == null) {
                        return null;
                }

                String path = targetId.getPath();
                if (path == null || path.isEmpty()) {
                        return null;
                }

                return new Identifier(targetId.getNamespace(), "blocks/" + path);
        }

        private static Identifier parseIdentifier(JsonObject object, String key, String entryLabel) {
                if (!object.has(key)) {
                        GardenKingMod.LOGGER.warn("Missing '{}' in rotten crop entry {}.", key, entryLabel);
                        return null;
                }

                try {
                        String raw = JsonHelper.getString(object, key);
                        Identifier identifier = Identifier.tryParse(raw);
                        if (identifier == null) {
                                GardenKingMod.LOGGER.warn(
                                                "Invalid identifier '{}' for '{}' in rotten crop entry {}.", raw, key,
                                                entryLabel);
                                return null;
                        }

                        return identifier;
                } catch (Exception exception) {
                        GardenKingMod.LOGGER.warn("Failed to parse '{}' in rotten crop entry {}.", key, entryLabel,
                                        exception);
                        return null;
                }
        }

        private static float readChance(JsonObject object, String key, String entryLabel) {
                if (!object.has(key)) {
                        return 0.0f;
                }

                try {
                        return MathHelper.clamp(JsonHelper.getFloat(object, key), 0.0f, 1.0f);
                } catch (Exception exception) {
                        GardenKingMod.LOGGER.warn(
                                        "Invalid value for '{}' in rotten crop entry {}. Defaulting to 0.", key,
                                        entryLabel, exception);
                        return 0.0f;
                }
        }

        private static Identifier resolveCropIdentifier(Block block, Identifier blockId) {
                Identifier pickStackId = resolvePickStackIdentifier(block);
                if (pickStackId != null) {
                        return pickStackId;
                }

                Identifier blockItemId = resolveItemIdentifier(block.asItem());
                if (blockItemId != null) {
                        return blockItemId;
                }

                return createCropIdentifier(blockId);
        }

        private static Identifier resolvePickStackIdentifier(Block block) {
                try {
                        ItemStack pickStack = block.getPickStack(null, null, block.getDefaultState());
                        return resolveItemIdentifier(pickStack);
                } catch (Exception exception) {
                        return null;
                }
        }

        private static Identifier resolveItemIdentifier(ItemStack stack) {
                if (stack == null || stack.isEmpty()) {
                        return null;
                }

                return resolveItemIdentifier(stack.getItem());
        }

        private static Identifier resolveItemIdentifier(Item item) {
                if (item == null || item == Items.AIR) {
                        return null;
                }

                Identifier itemId = Registries.ITEM.getId(item);
                if (itemId == null || itemId.equals(Registries.ITEM.getDefaultId())) {
                        return null;
                }

                return itemId;
        }

        private static Identifier createCropIdentifier(Identifier blockId) {
                String sanitizedPath = RottenCropDefinition.sanitizedPath(blockId.getPath());
                if (sanitizedPath.isEmpty()) {
                        sanitizedPath = blockId.getPath();
                }

                return new Identifier(blockId.getNamespace(), sanitizedPath);
        }
}
