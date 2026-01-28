package net.jeremy.gardenkingmod.item;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.util.JsonCommentHelper;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public final class ToolBuffDefinitions {
        private static final String RESOURCE_PATH = "/data/" + GardenKingMod.MOD_ID + "/tool_buffs.json";
        private static Map<Identifier, ToolBuffDefinition> definitionsByItem = Map.of();
        private static boolean initialized;

        private ToolBuffDefinitions() {
        }

        public static Optional<ToolBuffDefinition> findByItemId(Identifier itemId) {
                ensureLoaded();
                return Optional.ofNullable(definitionsByItem.get(itemId));
        }

        public static synchronized void reload() {
                List<ToolBuffDefinition> definitions = loadDefinitionsFromResource();
                Map<Identifier, ToolBuffDefinition> byItem = new LinkedHashMap<>();
                for (ToolBuffDefinition definition : definitions) {
                        byItem.put(definition.itemId(), definition);
                }
                definitionsByItem = Collections.unmodifiableMap(byItem);
                initialized = true;
        }

        private static void ensureLoaded() {
                if (!initialized) {
                        reload();
                }
        }

        private static List<ToolBuffDefinition> loadDefinitionsFromResource() {
                Optional<Collection<Path>> rootPaths = FabricLoader.getInstance()
                                .getModContainer(GardenKingMod.MOD_ID)
                                .map(ModContainer::getRootPaths);

                if (rootPaths.isPresent()) {
                        for (Path rootPath : rootPaths.get()) {
                                Path dataPath = rootPath.resolve("data").resolve(GardenKingMod.MOD_ID)
                                                .resolve("tool_buffs.json");
                                if (Files.isRegularFile(dataPath)) {
                                        try (BufferedReader reader = Files.newBufferedReader(dataPath,
                                                        StandardCharsets.UTF_8)) {
                                                return parseDefinitions(reader);
                                        } catch (JsonParseException | IOException exception) {
                                                GardenKingMod.LOGGER.error(
                                                                "Failed to read tool buff definitions from {}",
                                                                RESOURCE_PATH, exception);
                                                return List.of();
                                        }
                                }
                        }
                }

                InputStream stream = ToolBuffDefinitions.class.getResourceAsStream(RESOURCE_PATH);
                if (stream == null) {
                        return List.of();
                }

                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        return parseDefinitions(reader);
                } catch (JsonParseException | IOException exception) {
                        GardenKingMod.LOGGER.error("Failed to read tool buff definitions from {}", RESOURCE_PATH,
                                        exception);
                        return List.of();
                }
        }

        private static List<ToolBuffDefinition> parseDefinitions(Reader reader) {
                JsonElement root = JsonParser.parseReader(reader);
                JsonElement sanitized = JsonCommentHelper.sanitize(root);
                JsonArray entries = extractEntriesArray(sanitized);
                if (entries == null) {
                        GardenKingMod.LOGGER.warn("Expected an array of tool buff entries in {}.", RESOURCE_PATH);
                        return List.of();
                }

                List<ToolBuffDefinition> definitions = new ArrayList<>();
                for (int index = 0; index < entries.size(); index++) {
                        JsonElement element = entries.get(index);
                        if (!element.isJsonObject()) {
                                GardenKingMod.LOGGER.warn("Ignoring non-object entry at index {} while reading {}.", index,
                                                RESOURCE_PATH);
                                continue;
                        }

                        ToolBuffDefinition definition = parseDefinition(element.getAsJsonObject(), index);
                        if (definition != null) {
                                definitions.add(definition);
                        }
                }

                return List.copyOf(definitions);
        }

        private static JsonArray extractEntriesArray(JsonElement element) {
                if (element == null || element.isJsonNull()) {
                        return null;
                }
                if (element.isJsonArray()) {
                        return element.getAsJsonArray();
                }
                if (element.isJsonObject()) {
                        JsonObject object = element.getAsJsonObject();
                        if (object.has("entries") && object.get("entries").isJsonArray()) {
                                return object.getAsJsonArray("entries");
                        }
                }
                return null;
        }

        private static ToolBuffDefinition parseDefinition(JsonObject object, int index) {
                Identifier itemId = parseIdentifier(object, "item", "entry " + index);
                if (itemId == null) {
                        return null;
                }

                OptionalInt durabilityOverride = OptionalInt.empty();
                if (object.has("durability_override")) {
                        int durability = JsonHelper.getInt(object, "durability_override", -1);
                        if (durability > 0) {
                                durabilityOverride = OptionalInt.of(durability);
                        }
                }

                List<ToolBuffDefinition.EnchantmentEntry> enchantments = parseEnchantments(
                                object.has("enchantments") ? object.get("enchantments") : null, itemId);
                List<ToolBuffDefinition.AttributeEntry> attributes = parseAttributeModifiers(
                                object.has("attribute_modifiers") ? object.get("attribute_modifiers") : null, itemId);

                return new ToolBuffDefinition(itemId, durabilityOverride, enchantments, attributes);
        }

        private static List<ToolBuffDefinition.EnchantmentEntry> parseEnchantments(JsonElement element,
                        Identifier itemId) {
                if (element == null || !element.isJsonArray()) {
                        return List.of();
                }

                List<ToolBuffDefinition.EnchantmentEntry> entries = new ArrayList<>();
                JsonArray array = element.getAsJsonArray();
                for (int index = 0; index < array.size(); index++) {
                        JsonElement entry = array.get(index);
                        if (!entry.isJsonObject()) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping non-object enchantment entry {} for tool buff {}.", index, itemId);
                                continue;
                        }
                        JsonObject enchantmentObject = entry.getAsJsonObject();
                        Identifier enchantmentId = parseIdentifier(enchantmentObject, "id",
                                        "enchantment " + index + " for " + itemId);
                        if (enchantmentId == null) {
                                continue;
                        }
                        int level = JsonHelper.getInt(enchantmentObject, "level", 1);
                        if (level <= 0) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping enchantment {} for {} because level {} was invalid.",
                                                enchantmentId, itemId, level);
                                continue;
                        }
                        if (!Registries.ENCHANTMENT.containsId(enchantmentId)) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping enchantment {} for {} because it is not registered.",
                                                enchantmentId, itemId);
                                continue;
                        }
                        entries.add(new ToolBuffDefinition.EnchantmentEntry(enchantmentId, level));
                }

                return List.copyOf(entries);
        }

        private static List<ToolBuffDefinition.AttributeEntry> parseAttributeModifiers(JsonElement element,
                        Identifier itemId) {
                if (element == null || !element.isJsonArray()) {
                        return List.of();
                }

                List<ToolBuffDefinition.AttributeEntry> entries = new ArrayList<>();
                JsonArray array = element.getAsJsonArray();
                for (int index = 0; index < array.size(); index++) {
                        JsonElement entry = array.get(index);
                        if (!entry.isJsonObject()) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping non-object attribute entry {} for tool buff {}.", index, itemId);
                                continue;
                        }
                        JsonObject attributeObject = entry.getAsJsonObject();
                        Identifier attributeId = parseIdentifier(attributeObject, "attribute",
                                        "attribute " + index + " for " + itemId);
                        if (attributeId == null) {
                                continue;
                        }
                        if (!Registries.ATTRIBUTE.containsId(attributeId)) {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping attribute modifier {} for {} because it is not registered.",
                                                attributeId, itemId);
                                continue;
                        }
                        double amount = JsonHelper.getDouble(attributeObject, "amount", 0.0);
                        String operationName = JsonHelper.getString(attributeObject, "operation", "add");
                        EntityAttributeModifier.Operation operation = parseOperation(operationName, itemId, attributeId);
                        if (operation == null) {
                                continue;
                        }
                        String slotName = JsonHelper.getString(attributeObject, "slot", "mainhand");
                        EquipmentSlot slot = parseSlot(slotName, itemId, attributeId);
                        if (slot == null) {
                                continue;
                        }
                        String name = JsonHelper.getString(attributeObject, "name", itemId + " " + attributeId.getPath());
                        entries.add(new ToolBuffDefinition.AttributeEntry(attributeId, amount, operation, slot, name));
                }

                return List.copyOf(entries);
        }

        private static EntityAttributeModifier.Operation parseOperation(String raw, Identifier itemId,
                        Identifier attributeId) {
                String normalized = raw.toLowerCase();
                return switch (normalized) {
                        case "add", "addition" -> EntityAttributeModifier.Operation.ADDITION;
                        case "multiply_base" -> EntityAttributeModifier.Operation.MULTIPLY_BASE;
                        case "multiply_total" -> EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
                        default -> {
                                GardenKingMod.LOGGER.warn(
                                                "Skipping attribute modifier {} for {} because operation '{}' is invalid.",
                                                attributeId, itemId, raw);
                                yield null;
                        }
                };
        }

        private static EquipmentSlot parseSlot(String raw, Identifier itemId, Identifier attributeId) {
                EquipmentSlot slot = EquipmentSlot.byName(raw);
                if (slot == null) {
                        GardenKingMod.LOGGER.warn(
                                        "Skipping attribute modifier {} for {} because slot '{}' is invalid.",
                                        attributeId, itemId, raw);
                }
                return slot;
        }

        private static Identifier parseIdentifier(JsonObject object, String key, String entryLabel) {
                if (!object.has(key)) {
                        GardenKingMod.LOGGER.warn("Missing '{}' in tool buff entry {}.", key, entryLabel);
                        return null;
                }
                try {
                        Identifier identifier = new Identifier(object.get(key).getAsString());
                        if (identifier.getNamespace().isBlank() || identifier.getPath().isBlank()) {
                                return null;
                        }
                        return identifier;
                } catch (Exception exception) {
                        GardenKingMod.LOGGER.warn("Invalid identifier '{}' for '{}' in tool buff entry {}.",
                                        object.get(key), key, entryLabel);
                        return null;
                }
        }
}
