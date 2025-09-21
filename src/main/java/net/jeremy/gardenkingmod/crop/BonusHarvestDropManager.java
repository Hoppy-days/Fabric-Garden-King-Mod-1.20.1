package net.jeremy.gardenkingmod.crop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.Profiler;

/**
 * Loads bonus harvest drop definitions from JSON files so datapacks can
 * dynamically reconfigure seasonal events without code changes.
 */
public final class BonusHarvestDropManager extends JsonDataLoader implements IdentifiableResourceReloadListener {
        private static final Gson GSON = new GsonBuilder().setLenient().create();
        private static final String DIRECTORY = "bonus_harvest_drops";
        private static final Identifier FABRIC_ID = new Identifier(GardenKingMod.MOD_ID, "bonus_harvest_drop_manager");
        private static final BonusHarvestDropManager INSTANCE = new BonusHarvestDropManager();

        private final Map<Identifier, List<BonusDropEntry>> bonusDrops = new ConcurrentHashMap<>();
        private final Object reloadLock = new Object();
        private volatile ResourceManager lastSeenManager;
        private volatile boolean appliedForManager;

        private BonusHarvestDropManager() {
                super(GSON, DIRECTORY);
        }

        public static BonusHarvestDropManager getInstance() {
                return INSTANCE;
        }

        @Override
        public Identifier getFabricId() {
                return FABRIC_ID;
        }

        /**
         * Returns a snapshot of all bonus drops registered for a loot table.
         *
         * @param lootTableId The loot table identifier to inspect.
         * @return An immutable list of bonus entries.
         */
        public List<BonusDropEntry> getBonusDrops(Identifier lootTableId) {
                List<BonusDropEntry> entries = bonusDrops.get(lootTableId);
                return entries != null ? entries : Collections.emptyList();
        }

        public void ensureLoaded(ResourceManager resourceManager) {
                if (resourceManager == null) {
                        return;
                }

                boolean needsLoad;
                synchronized (reloadLock) {
                        if (resourceManager != lastSeenManager) {
                                lastSeenManager = resourceManager;
                                appliedForManager = false;
                        }

                        needsLoad = !appliedForManager;
                }

                if (!needsLoad) {
                        return;
                }

                Map<Identifier, JsonElement> prepared = prepare(resourceManager, DummyProfiler.INSTANCE);

                synchronized (reloadLock) {
                        if (resourceManager != lastSeenManager || appliedForManager) {
                                return;
                        }

                        loadFromPrepared(prepared);
                        appliedForManager = true;
                }
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
                synchronized (reloadLock) {
                        lastSeenManager = manager;
                        loadFromPrepared(prepared);
                        appliedForManager = true;
                }
        }

        private void loadFromPrepared(Map<Identifier, JsonElement> prepared) {
                Map<Identifier, List<BonusDropEntry>> parsed = new HashMap<>();

                for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
                        Identifier fileId = entry.getKey();
                        JsonElement json = entry.getValue();

                        if (!json.isJsonObject()) {
                                GardenKingMod.LOGGER.warn("Ignoring bonus harvest drops at {} because it is not a JSON object", fileId);
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
                                if (!value.isJsonArray()) {
                                        GardenKingMod.LOGGER.warn("Ignoring bonus entries for {} in {} because the value is not an array", targetId, fileId);
                                        continue;
                                }

                                parseEntries(fileId, lootTableId, value.getAsJsonArray(), parsed);
                        }
                }

                bonusDrops.clear();
                for (Map.Entry<Identifier, List<BonusDropEntry>> entry : parsed.entrySet()) {
                        bonusDrops.put(entry.getKey(), List.copyOf(entry.getValue()));
                }

                if (!parsed.isEmpty()) {
                        GardenKingMod.LOGGER.info("Loaded bonus harvest drop definitions for {} loot tables", parsed.size());
                } else {
                        GardenKingMod.LOGGER.info("Cleared all bonus harvest drop definitions");
                }
        }

        private void parseEntries(Identifier fileId, Identifier lootTableId, JsonArray array, Map<Identifier, List<BonusDropEntry>> parsed) {
                List<BonusDropEntry> entries = parsed.computeIfAbsent(lootTableId, id -> new ArrayList<>());

                for (int i = 0; i < array.size(); i++) {
                        JsonElement element = array.get(i);
                        if (!element.isJsonObject()) {
                                GardenKingMod.LOGGER.warn("Ignoring entry {} for {} in {} because it is not a JSON object", i, lootTableId, fileId);
                                continue;
                        }

                        JsonObject entryObject = element.getAsJsonObject();
                        try {
                                String itemIdString = JsonHelper.getString(entryObject, "item");
                                Identifier itemId = parseIdentifier(itemIdString, fileId);
                                if (itemId == null) {
                                        continue;
                                }

                                Optional<Item> itemOptional = Registries.ITEM.getOrEmpty(itemId);
                                if (itemOptional.isEmpty()) {
                                        GardenKingMod.LOGGER.warn("Unknown item {} referenced in {} for loot table {}", itemId, fileId, lootTableId);
                                        continue;
                                }

                                float chance = JsonHelper.getFloat(entryObject, "chance");
                                if (chance <= 0.0f) {
                                        GardenKingMod.LOGGER.debug("Skipping bonus drop {} from {} because chance {} is not positive", itemId, lootTableId, chance);
                                        continue;
                                }

                                JsonObject countObject = JsonHelper.getObject(entryObject, "count");
                                int min = JsonHelper.getInt(countObject, "min");
                                int max = JsonHelper.getInt(countObject, "max");
                                if (max < min) {
                                        int swap = min;
                                        min = max;
                                        max = swap;
                                }

                                entries.add(new BonusDropEntry(itemOptional.get(), chance, min, max));
                        } catch (IllegalArgumentException exception) {
                                GardenKingMod.LOGGER.warn("Failed to parse bonus entry {} for {} in {}", i, lootTableId, fileId, exception);
                        }
                }
        }

        private Identifier resolveTarget(Identifier candidate) {
                Optional<Block> block = Registries.BLOCK.getOrEmpty(candidate);
                if (block.isPresent()) {
                        Identifier lootTableId = block.get().getLootTableId();
                        if (LootTables.EMPTY.equals(lootTableId)) {
                                GardenKingMod.LOGGER.warn("Block {} does not have a loot table and cannot receive bonus harvest drops", candidate);
                                return null;
                        }

                        return lootTableId;
                }

                return candidate;
        }

        private Identifier parseIdentifier(String raw, Identifier sourceFile) {
                if (raw == null || raw.isEmpty()) {
                        GardenKingMod.LOGGER.warn("Encountered empty identifier while parsing bonus drops in {}", sourceFile);
                        return null;
                }

                Identifier identifier = Identifier.tryParse(raw);
                if (identifier == null) {
                        GardenKingMod.LOGGER.warn("Invalid identifier '{}' in {}", raw, sourceFile);
                        return null;
                }

                return identifier;
        }

        public record BonusDropEntry(Item item, float chance, int minCount, int maxCount) {
        }
}
