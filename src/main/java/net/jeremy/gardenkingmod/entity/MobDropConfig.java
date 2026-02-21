package net.jeremy.gardenkingmod.entity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import net.jeremy.gardenkingmod.GardenKingMod;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * JSON-backed configuration for entity loot tuning and bonus mob drops.
 */
public final class MobDropConfig {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
                        .resolve(GardenKingMod.MOD_ID)
                        .resolve("mob_drops.json");

        private static volatile MobDropConfig instance = defaults();

        private float globalMultiplier = 1.0f;
        private Map<String, MobGroupConfig> groups = new LinkedHashMap<>();
        private List<BonusMobDropConfig> bonusDrops = new ArrayList<>();

        private final transient Map<Identifier, Float> multiplierByMobId = new LinkedHashMap<>();
        private final transient Map<Identifier, List<ResolvedBonusDrop>> bonusDropsByMobId = new LinkedHashMap<>();

        private MobDropConfig() {
        }

        public static void reload() {
                MobDropConfig defaults = defaults();

                try {
                        Files.createDirectories(CONFIG_PATH.getParent());
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed to create mob drop config directory", exception);
                }

                if (Files.notExists(CONFIG_PATH)) {
                        writeConfig(defaults);
                        defaults.rebuildRuntimeCaches();
                        instance = defaults;
                        return;
                }

                try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                        MobDropConfig loaded = GSON.fromJson(reader, MobDropConfig.class);
                        if (loaded == null) {
                                GardenKingMod.LOGGER.warn("Mob drop config is empty; using defaults");
                                defaults.rebuildRuntimeCaches();
                                instance = defaults;
                                return;
                        }

                        boolean updated = loaded.validateAndApplyDefaults(defaults);
                        loaded.rebuildRuntimeCaches();
                        instance = loaded;

                        if (updated) {
                                writeConfig(loaded);
                        }
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed reading mob drop config; using defaults", exception);
                        defaults.rebuildRuntimeCaches();
                        instance = defaults;
                }
        }

        public static MobDropConfig get() {
                return instance;
        }

        public float multiplierForMob(Identifier mobId) {
                return multiplierByMobId.getOrDefault(mobId, globalMultiplier);
        }

        public List<ResolvedBonusDrop> bonusDropsForMob(Identifier mobId) {
                return bonusDropsByMobId.getOrDefault(mobId, List.of());
        }

        private static void writeConfig(MobDropConfig config) {
                try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
                        GSON.toJson(config, writer);
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed writing mob drop config", exception);
                }
        }

        private boolean validateAndApplyDefaults(MobDropConfig defaults) {
                boolean changed = false;

                if (!Float.isFinite(globalMultiplier) || globalMultiplier < 0.0f) {
                        globalMultiplier = defaults.globalMultiplier;
                        changed = true;
                }

                if (groups == null) {
                        groups = new LinkedHashMap<>(defaults.groups);
                        changed = true;
                }

                if (bonusDrops == null) {
                        bonusDrops = new ArrayList<>(defaults.bonusDrops);
                        changed = true;
                }

                return changed;
        }

        private void rebuildRuntimeCaches() {
                multiplierByMobId.clear();
                bonusDropsByMobId.clear();

                float clampedGlobal = Math.max(0.0f, globalMultiplier);

                Set<String> trackedMobIds = new LinkedHashSet<>();
                for (MobGroupConfig group : groups.values()) {
                        if (group == null || group.mobs == null) {
                                continue;
                        }
                        trackedMobIds.addAll(group.mobs);
                }
                for (BonusMobDropConfig bonusDrop : bonusDrops) {
                        if (bonusDrop == null || bonusDrop.mobs == null) {
                                continue;
                        }
                        trackedMobIds.addAll(bonusDrop.mobs);
                }

                for (String rawMobId : trackedMobIds) {
                        Identifier mobId = Identifier.tryParse(normalizeId(rawMobId));
                        if (mobId == null) {
                                GardenKingMod.LOGGER.warn("Ignoring invalid mob id '{}' in mob drop config", rawMobId);
                                continue;
                        }

                        float groupMultiplier = 1.0f;
                        for (MobGroupConfig group : groups.values()) {
                                if (group == null || group.mobs == null || !group.mobs.contains(rawMobId)
                                                && !group.mobs.contains(mobId.toString())) {
                                        continue;
                                }

                                float value = group.multiplier;
                                if (!Float.isFinite(value) || value < 0.0f) {
                                        value = 1.0f;
                                }
                                groupMultiplier *= value;
                        }

                        multiplierByMobId.put(mobId, clampedGlobal * groupMultiplier);
                }

                for (BonusMobDropConfig configuredDrop : bonusDrops) {
                        if (configuredDrop == null || configuredDrop.mobs == null || configuredDrop.mobs.isEmpty()) {
                                continue;
                        }

                        Identifier itemId = Identifier.tryParse(normalizeId(configuredDrop.item));
                        if (itemId == null) {
                                GardenKingMod.LOGGER.warn("Ignoring bonus drop with invalid item id '{}'", configuredDrop.item);
                                continue;
                        }

                        Item item = Registries.ITEM.get(itemId);
                        if (item == null || item == net.minecraft.item.Items.AIR) {
                                GardenKingMod.LOGGER.warn("Ignoring bonus drop because item '{}' does not exist", itemId);
                                continue;
                        }

                        float chance = Math.max(0.0f, Math.min(1.0f, configuredDrop.chance));
                        int minCount = Math.max(1, configuredDrop.minCount);
                        int maxCount = Math.max(minCount, configuredDrop.maxCount);
                        ResolvedBonusDrop resolvedDrop = new ResolvedBonusDrop(item, chance, minCount, maxCount);

                        for (String rawMobId : configuredDrop.mobs) {
                                Identifier mobId = Identifier.tryParse(normalizeId(rawMobId));
                                if (mobId == null) {
                                        GardenKingMod.LOGGER.warn("Ignoring invalid mob id '{}' in bonus drop entry", rawMobId);
                                        continue;
                                }
                                bonusDropsByMobId.computeIfAbsent(mobId, unused -> new ArrayList<>()).add(resolvedDrop);
                        }
                }
        }

        private static String normalizeId(String value) {
                if (value == null || value.isBlank()) {
                        return "";
                }
                String normalized = value.toLowerCase(Locale.ROOT).trim();
                return normalized.contains(":") ? normalized : "minecraft:" + normalized;
        }

        private static MobDropConfig defaults() {
                MobDropConfig config = new MobDropConfig();
                config.globalMultiplier = 1.0f;

                MobGroupConfig hostiles = new MobGroupConfig();
                hostiles.multiplier = 1.0f;
                hostiles.mobs = List.of("minecraft:zombie", "minecraft:skeleton", "minecraft:spider");

                MobGroupConfig passive = new MobGroupConfig();
                passive.multiplier = 1.0f;
                passive.mobs = List.of("minecraft:cow", "minecraft:pig", "minecraft:sheep", "minecraft:chicken");

                config.groups.put("hostile", hostiles);
                config.groups.put("passive", passive);

                BonusMobDropConfig rubyDrop = new BonusMobDropConfig();
                rubyDrop.mobs = List.of("minecraft:zombie", "minecraft:skeleton", "minecraft:spider");
                rubyDrop.item = "gardenkingmod:ruby";
                rubyDrop.chance = 0.05f;
                rubyDrop.minCount = 1;
                rubyDrop.maxCount = 1;
                config.bonusDrops.add(rubyDrop);

                return config;
        }

        private static final class MobGroupConfig {
                private float multiplier = 1.0f;
                private List<String> mobs = List.of();
        }

        private static final class BonusMobDropConfig {
                private List<String> mobs = List.of();
                private String item = "minecraft:rotten_flesh";
                private float chance = 0.0f;
                private int minCount = 1;
                private int maxCount = 1;
        }

        public record ResolvedBonusDrop(Item item, float chance, int minCount, int maxCount) {
        }
}
