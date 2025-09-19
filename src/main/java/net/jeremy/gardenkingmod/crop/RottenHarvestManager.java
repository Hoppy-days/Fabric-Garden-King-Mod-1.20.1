package net.jeremy.gardenkingmod.crop;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;

import net.jeremy.gardenkingmod.GardenKingMod;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTables;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;

/**
 * Loads rotten harvest definitions from JSON files, allowing datapacks to
 * configure which crops can rot and how their drop chances are adjusted.
 */
public final class RottenHarvestManager extends JsonDataLoader implements IdentifiableResourceReloadListener {
        private static final Gson GSON = new GsonBuilder().setLenient().create();
        private static final String DIRECTORY = "rotten_harvest";
        private static final Identifier FABRIC_ID = new Identifier(GardenKingMod.MOD_ID, "rotten_harvest_manager");
        private static final RottenHarvestManager INSTANCE = new RottenHarvestManager();

        private final Map<Identifier, RottenHarvestEntry> rottenHarvests = new ConcurrentHashMap<>();

        private RottenHarvestManager() {
                super(GSON, DIRECTORY);
        }

        public static RottenHarvestManager getInstance() {
                return INSTANCE;
        }

        @Override
        public Identifier getFabricId() {
                return FABRIC_ID;
        }

        public Optional<RottenHarvestEntry> getRottenHarvest(Identifier lootTableId) {
                return Optional.ofNullable(rottenHarvests.get(lootTableId));
        }

        /**
         * Returns any additional rotten chance supplied by datapacks for the provided
         * loot table. The current schema does not expose extra rotten odds, so the
         * method always returns {@code 0.0f} but remains available for future
         * compatibility.
         */
        public float getExtraRottenChance(Identifier lootTableId) {
                return 0.0f;
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
                Map<Identifier, RottenHarvestEntry> parsed = new HashMap<>();

                for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
                        Identifier fileId = entry.getKey();
                        JsonElement json = entry.getValue();

                        if (!json.isJsonObject()) {
                                GardenKingMod.LOGGER.warn("Ignoring rotten harvest data at {} because it is not a JSON object", fileId);
                                continue;
                        }

                        JsonObject object = json.getAsJsonObject();
                        for (Map.Entry<String, JsonElement> definition : object.entrySet()) {
                                Identifier targetId = parseIdentifier(definition.getKey(), fileId);
                                if (targetId == null) {
                                        continue;
                                }

                                Identifier lootTableId = resolveTarget(targetId);
                                if (lootTableId == null) {
                                        continue;
                                }

                                JsonElement value = definition.getValue();
                                if (!value.isJsonObject()) {
                                        GardenKingMod.LOGGER.warn("Ignoring rotten harvest entry for {} in {} because the value is not an object",
                                                        targetId, fileId);
                                        continue;
                                }

                                RottenHarvestEntry parsedEntry = parseEntry(fileId, lootTableId, value.getAsJsonObject());
                                if (parsedEntry != null) {
                                        parsed.put(lootTableId, parsedEntry);
                                }
                        }
                }

                rottenHarvests.clear();
                rottenHarvests.putAll(parsed);

                if (!parsed.isEmpty()) {
                        GardenKingMod.LOGGER.info("Loaded rotten harvest definitions for {} loot tables", parsed.size());
                } else {
                        GardenKingMod.LOGGER.info("Cleared all rotten harvest definitions");
                }
        }

        private RottenHarvestEntry parseEntry(Identifier fileId, Identifier lootTableId, JsonObject object) {
                try {
                        String itemIdString = JsonHelper.getString(object, "rotten_item");
                        Identifier itemId = parseIdentifier(itemIdString, fileId);
                        if (itemId == null) {
                                return null;
                        }

                        Optional<Item> itemOptional = Registries.ITEM.getOrEmpty(itemId);
                        if (itemOptional.isEmpty()) {
                                GardenKingMod.LOGGER.warn("Unknown item {} referenced in {} for loot table {}", itemId, fileId, lootTableId);
                                return null;
                        }

                        float extraNoDropChance = 0.0f;
                        if (object.has("extra_no_drop_chance")) {
                                extraNoDropChance = MathHelper.clamp(JsonHelper.getFloat(object, "extra_no_drop_chance"), 0.0f, 1.0f);
                        }

                        return new RottenHarvestEntry(itemOptional.get(), extraNoDropChance);
                } catch (IllegalArgumentException exception) {
                        GardenKingMod.LOGGER.warn("Failed to parse rotten harvest entry for {} in {}", lootTableId, fileId, exception);
                        return null;
                }
        }

        private Identifier resolveTarget(Identifier candidate) {
                Optional<Block> block = Registries.BLOCK.getOrEmpty(candidate);
                if (block.isPresent()) {
                        Identifier lootTableId = block.get().getLootTableId();
                        if (LootTables.EMPTY.equals(lootTableId)) {
                                GardenKingMod.LOGGER.warn("Block {} does not have a loot table and cannot receive rotten harvest data",
                                                candidate);
                                return null;
                        }

                        return lootTableId;
                }

                return candidate;
        }

        private Identifier parseIdentifier(String raw, Identifier sourceFile) {
                if (raw == null || raw.isEmpty()) {
                        GardenKingMod.LOGGER.warn("Encountered empty identifier while parsing rotten harvest data in {}", sourceFile);
                        return null;
                }

                Identifier identifier = Identifier.tryParse(raw);
                if (identifier == null) {
                        GardenKingMod.LOGGER.warn("Invalid identifier '{}' in {}", raw, sourceFile);
                        return null;
                }

                return identifier;
        }

        public record RottenHarvestEntry(Item rottenItem, float extraNoDropChance) {
        }
}
