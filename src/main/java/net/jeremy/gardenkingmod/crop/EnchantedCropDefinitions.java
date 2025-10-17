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
 * Loads {@link EnchantedCropDefinition enchanted crop definitions} from the
 * {@code enchanted_crops.json} data file. When no explicit definitions are
 * supplied the loader derives defaults from the rotten crop list to keep the
 * enchanted system in sync.
 */
public final class EnchantedCropDefinitions {
        private static List<EnchantedCropDefinition> allDefinitions = List.of();
        private static Map<Identifier, EnchantedCropDefinition> definitionsByTarget = Map.of();
        private static Map<Identifier, EnchantedCropDefinition> definitionsByCrop = Map.of();
        private static Map<Identifier, EnchantedCropDefinition> definitionsByLootTable = Map.of();
        private static Map<Identifier, EnchantedCropDefinition> definitionsByEnchantedItem = Map.of();
        private static boolean initialized;

        private EnchantedCropDefinitions() {
        }

        public static List<EnchantedCropDefinition> all() {
                ensureLoaded();
                return allDefinitions;
        }

        public static boolean hasDefinitions() {
                return initialized && !allDefinitions.isEmpty();
        }

        public static Optional<EnchantedCropDefinition> findByTargetId(Identifier identifier) {
                ensureLoaded();
                return Optional.ofNullable(definitionsByTarget.get(identifier));
        }

        public static Optional<EnchantedCropDefinition> findByCropId(Identifier identifier) {
                ensureLoaded();
                return Optional.ofNullable(definitionsByCrop.get(identifier));
        }

        public static Optional<EnchantedCropDefinition> findByLootTableId(Identifier identifier) {
                ensureLoaded();
                return Optional.ofNullable(definitionsByLootTable.get(identifier));
        }

        public static Optional<EnchantedCropDefinition> findByEnchantedItemId(Identifier identifier) {
                ensureLoaded();
                return Optional.ofNullable(definitionsByEnchantedItem.get(identifier));
        }

        public static synchronized void reload() {
                List<EnchantedCropDefinition> definitions = createDefinitions();
                allDefinitions = definitions;
                definitionsByTarget = indexBy(definitions, EnchantedCropDefinition::targetId);
                definitionsByCrop = indexBy(definitions, EnchantedCropDefinition::cropId);
                definitionsByLootTable = indexBy(definitions, EnchantedCropDefinition::lootTableId);
                definitionsByEnchantedItem = indexBy(definitions, EnchantedCropDefinition::enchantedItemId);
                initialized = true;
        }

        private static void ensureLoaded() {
                if (!initialized) {
                        reload();
                }
        }

        private static Map<Identifier, EnchantedCropDefinition> indexBy(List<EnchantedCropDefinition> definitions,
                        Function<EnchantedCropDefinition, Identifier> keyExtractor) {
                Map<Identifier, EnchantedCropDefinition> map = new LinkedHashMap<>();
                for (EnchantedCropDefinition definition : definitions) {
                        map.put(keyExtractor.apply(definition), definition);
                }

                return Collections.unmodifiableMap(map);
        }

        private static final String RESOURCE_PATH = "/data/" + GardenKingMod.MOD_ID + "/enchanted_crops.json";

        private static List<EnchantedCropDefinition> createDefinitions() {
                List<EnchantedCropDefinition> manualList = manualDefinitions();
                if (!manualList.isEmpty()) {
                        return sort(manualList);
                }

                GardenKingMod.LOGGER.info(
                                "No enchanted crop list found at {}. Deriving defaults from rotten crop definitions.",
                                RESOURCE_PATH);
                RottenCropDefinitions.reload();
                List<RottenCropDefinition> rotten = RottenCropDefinitions.all();
                if (rotten.isEmpty()) {
                        return List.of();
                }

                List<EnchantedCropDefinition> derived = new ArrayList<>();
                for (RottenCropDefinition definition : rotten) {
                        derived.add(new EnchantedCropDefinition(definition.cropId(), definition.targetId(),
                                        definition.lootTableId()));
                }

                return sort(derived);
        }

        private static List<EnchantedCropDefinition> sort(List<EnchantedCropDefinition> definitions) {
                List<EnchantedCropDefinition> copy = new ArrayList<>(definitions);
                copy.sort(Comparator.comparing(definition -> definition.enchantedItemId().toString()));
                return List.copyOf(copy);
        }

        private static List<EnchantedCropDefinition> manualDefinitions() {
                // Enchanted crops reuse the base crop textures; reference them from the
                // generated item models instead of supplying duplicate PNGs.
                return loadDefinitionsFromResource();
        }

        private static List<EnchantedCropDefinition> loadDefinitionsFromResource() {
                Optional<Collection<Path>> rootPaths = FabricLoader.getInstance()
                                .getModContainer(GardenKingMod.MOD_ID)
                                .map(ModContainer::getRootPaths);

                if (rootPaths.isPresent()) {
                        for (Path rootPath : rootPaths.get()) {
                                Path dataPath = rootPath.resolve("data").resolve(GardenKingMod.MOD_ID)
                                                .resolve("enchanted_crops.json");
                                if (Files.isRegularFile(dataPath)) {
                                        try (BufferedReader reader = Files.newBufferedReader(dataPath,
                                                        StandardCharsets.UTF_8)) {
                                                return parseDefinitions(reader);
                                        } catch (JsonParseException | IOException exception) {
                                                GardenKingMod.LOGGER.error(
                                                                "Failed to read enchanted crop definitions from {}",
                                                                RESOURCE_PATH, exception);
                                                return List.of();
                                        }
                                }
                        }
                }

                InputStream stream = EnchantedCropDefinitions.class.getResourceAsStream(RESOURCE_PATH);
                if (stream == null) {
                        return List.of();
                }

                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        return parseDefinitions(reader);
                } catch (JsonParseException | IOException exception) {
                        GardenKingMod.LOGGER.error("Failed to read enchanted crop definitions from {}", RESOURCE_PATH,
                                        exception);
                        return List.of();
                }
        }

        private static List<EnchantedCropDefinition> parseDefinitions(Reader reader) {
                List<EnchantedCropDefinition> definitions = new ArrayList<>();

                JsonElement root = JsonParser.parseReader(reader);
                JsonElement sanitized = JsonCommentHelper.sanitize(root);
                JsonArray entries = extractEntriesArray(sanitized);
                if (entries == null) {
                        GardenKingMod.LOGGER.warn("Expected an array of enchanted crop entries in {}.", RESOURCE_PATH);
                        return List.of();
                }

                Map<Identifier, EnchantedCropDefinition> byTarget = new LinkedHashMap<>();
                Set<Identifier> enchantedIds = new HashSet<>();

                for (int index = 0; index < entries.size(); index++) {
                        JsonElement element = entries.get(index);
                        if (!element.isJsonObject()) {
                                GardenKingMod.LOGGER.warn("Ignoring non-object entry at index {} while reading {}.", index,
                                                RESOURCE_PATH);
                                continue;
                        }

                        EnchantedCropDefinition definition = parseDefinition(element.getAsJsonObject(), index);
                        if (definition == null) {
                                continue;
                        }

                        Identifier targetId = definition.targetId();
                        Identifier enchantedId = definition.enchantedItemId();

                        if (byTarget.containsKey(targetId)) {
                                GardenKingMod.LOGGER.warn(
                                                "Duplicate enchanted crop target {} found while reading {}. Skipping later entry.",
                                                targetId, RESOURCE_PATH);
                                continue;
                        }

                        if (enchantedIds.contains(enchantedId)) {
                                                GardenKingMod.LOGGER.warn(
                                                                "Duplicate enchanted item {} found while reading {}. Skipping entry for {}.",
                                                                enchantedId, RESOURCE_PATH, targetId);
                                continue;
                        }

                        byTarget.put(targetId, definition);
                        enchantedIds.add(enchantedId);
                }

                definitions.addAll(byTarget.values());
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

        private static EnchantedCropDefinition parseDefinition(JsonObject object, int index) {
                Identifier enchantedItemId = null;
                if (object.has("enchanted_item")) {
                        enchantedItemId = parseIdentifier(object, "enchanted_item", "entry " + index);
                        if (enchantedItemId == null) {
                                return null;
                        }

                        if (!GardenKingMod.MOD_ID.equals(enchantedItemId.getNamespace())) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping enchanted crop entry {} because enchanted_item {} must use the {} namespace.",
                                                index, enchantedItemId, GardenKingMod.MOD_ID);
                                return null;
                        }
                }

                Identifier targetId = parseIdentifier(object, "target", enchantedItemId != null ? enchantedItemId.toString()
                                : "entry " + index);
                if (targetId == null) {
                        return null;
                }

                Optional<Block> blockOptional = Registries.BLOCK.getOrEmpty(targetId);
                Block block = blockOptional.orElse(null);

                if (block == null && !object.has("crop")) {
                        GardenKingMod.LOGGER.warn(
                                        "Skipping enchanted crop entry {} because target block {} is not registered.",
                                        targetId, targetId);
                        return null;
                }

                Identifier cropId;
                if (object.has("crop")) {
                        cropId = parseIdentifier(object, "crop", targetId.toString());
                        if (cropId == null) {
                                return null;
                        }
                } else if (block != null) {
                        cropId = resolveCropIdentifier(block, targetId);
                        if (cropId == null) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping enchanted crop entry {} because the crop item could not be determined.",
                                                targetId);
                                return null;
                        }
                } else {
                        GardenKingMod.LOGGER.warn(
                                        "Skipping enchanted crop entry {} because the crop item could not be determined.",
                                        targetId);
                        return null;
                }

                if (enchantedItemId == null) {
                        String sanitized = EnchantedCropDefinition.sanitizedPath(cropId.getPath());
                        if (sanitized.isEmpty()) {
                                sanitized = cropId.getPath();
                        }
                        enchantedItemId = new Identifier(GardenKingMod.MOD_ID, "enchanted_" + sanitized);
                }

                Identifier lootTableId;
                if (object.has("loot_table")) {
                        lootTableId = parseIdentifier(object, "loot_table", enchantedItemId.toString());
                        if (lootTableId == null) {
                                return null;
                        }
                } else if (block != null) {
                        lootTableId = block.getLootTableId();
                        if (LootTables.EMPTY.equals(lootTableId)) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping enchanted crop entry {} because target {} does not have a loot table. Specify one with 'loot_table'.",
                                                enchantedItemId, targetId);
                                return null;
                        }
                } else {
                        lootTableId = createDefaultLootTableId(targetId);
                        if (lootTableId == null) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping enchanted crop entry {} because target {} does not have a loot table. Specify one with 'loot_table'.",
                                                enchantedItemId, targetId);
                                return null;
                        }
                }

                float dropChance = readChance(object, "drop_chance", enchantedItemId.toString(),
                                EnchantedCropDefinition.DEFAULT_DROP_CHANCE);
                float valueMultiplier = readMultiplier(object, "value_multiplier", enchantedItemId.toString(),
                                EnchantedCropDefinition.DEFAULT_VALUE_MULTIPLIER);

                return new EnchantedCropDefinition(cropId, targetId, lootTableId, enchantedItemId.getPath(), dropChance,
                                valueMultiplier);
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
                        GardenKingMod.LOGGER.warn("Missing '{}' in enchanted crop entry {}.", key, entryLabel);
                        return null;
                }

                try {
                        String raw = JsonHelper.getString(object, key);
                        Identifier identifier = Identifier.tryParse(raw);
                        if (identifier == null) {
                                GardenKingMod.LOGGER.warn(
                                                "Invalid identifier '{}' for '{}' in enchanted crop entry {}.", raw, key,
                                                entryLabel);
                                return null;
                        }

                        return identifier;
                } catch (Exception exception) {
                        GardenKingMod.LOGGER.warn("Failed to parse '{}' in enchanted crop entry {}.", key, entryLabel,
                                        exception);
                        return null;
                }
        }

        private static float readChance(JsonObject object, String key, String entryLabel, float defaultValue) {
                if (!object.has(key)) {
                        return defaultValue;
                }

                try {
                        return MathHelper.clamp(JsonHelper.getFloat(object, key), 0.0f, 1.0f);
                } catch (Exception exception) {
                        GardenKingMod.LOGGER.warn(
                                        "Invalid value for '{}' in enchanted crop entry {}. Defaulting to {}.", key,
                                        entryLabel, defaultValue, exception);
                        return defaultValue;
                }
        }

        private static float readMultiplier(JsonObject object, String key, String entryLabel, float defaultValue) {
                if (!object.has(key)) {
                        return defaultValue;
                }

                try {
                        float value = JsonHelper.getFloat(object, key);
                        if (!Float.isFinite(value) || value < 1.0f) {
                                return defaultValue;
                        }
                        return value;
                } catch (Exception exception) {
                        GardenKingMod.LOGGER.warn(
                                        "Invalid value for '{}' in enchanted crop entry {}. Defaulting to {}.", key,
                                        entryLabel, defaultValue, exception);
                        return defaultValue;
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
                String sanitizedPath = EnchantedCropDefinition.sanitizedPath(blockId.getPath());
                if (sanitizedPath.isEmpty()) {
                        sanitizedPath = blockId.getPath();
                }

                return new Identifier(blockId.getNamespace(), sanitizedPath);
        }
}
