package net.jeremy.gardenkingmod.skill;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import net.jeremy.gardenkingmod.GardenKingMod;

/**
 * Loads the harvest XP configuration stored in
 * <code>config/gardenkingmod/harvest_xp.json</code>. The configuration exposes
 * a map of crop tier identifier paths to the amount of skill experience earned
 * when successfully harvesting crops from that tier.
 */
public final class HarvestXpConfig {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
                        .resolve(GardenKingMod.MOD_ID)
                        .resolve("harvest_xp.json");

        private static final Map<String, Long> DEFAULT_XP = Map.of(
                        "crop_tiers/tier_1", 1L,
                        "crop_tiers/tier_2", 2L,
                        "crop_tiers/tier_3", 3L,
                        "crop_tiers/tier_4", 4L,
                        "crop_tiers/tier_5", 5L);

        private static volatile HarvestXpConfig instance = new HarvestXpConfig();

        private Map<String, Long> xpByTierPath = new LinkedHashMap<>(DEFAULT_XP);

        private HarvestXpConfig() {
        }

        /**
         * Ensures the harvest XP configuration exists on disk and refreshes the cached
         * copy used by the server when awarding skill experience.
         */
        public static void reload() {
                HarvestXpConfig defaults = new HarvestXpConfig();

                try {
                        Files.createDirectories(CONFIG_PATH.getParent());
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed to create harvest XP config directory", exception);
                }

                if (Files.notExists(CONFIG_PATH)) {
                        writeConfigFile(defaults);
                        instance = defaults;
                        return;
                }

                try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                        HarvestXpConfig loaded = GSON.fromJson(reader, HarvestXpConfig.class);
                        if (loaded == null) {
                                GardenKingMod.LOGGER.warn("Harvest XP config file was empty; using defaults");
                                instance = defaults;
                        } else {
                                boolean updated = loaded.validateAndApplyDefaults(defaults);
                                if (updated) {
                                        writeConfigFile(loaded);
                                }
                                instance = loaded;
                        }
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed to read harvest XP config; falling back to defaults", exception);
                        instance = defaults;
                }
        }

        private static void writeConfigFile(HarvestXpConfig config) {
                try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
                        GSON.toJson(config, writer);
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed to write harvest XP config", exception);
                }
        }

        private boolean validateAndApplyDefaults(HarvestXpConfig defaults) {
                boolean changed = false;

                if (xpByTierPath == null) {
                        xpByTierPath = new LinkedHashMap<>(defaults.xpByTierPath);
                        return true;
                }

                Map<String, Long> validated = new LinkedHashMap<>();

                for (Map.Entry<String, Long> entry : defaults.xpByTierPath.entrySet()) {
                        String key = entry.getKey();
                        Long value = xpByTierPath.get(key);

                        if (value == null || value.longValue() < 0L) {
                                validated.put(key, entry.getValue());
                                changed = true;
                        } else {
                                validated.put(key, value);
                        }
                }

                for (Map.Entry<String, Long> entry : xpByTierPath.entrySet()) {
                        String key = entry.getKey();
                        if (!validated.containsKey(key)) {
                                long value = Math.max(0L, entry.getValue() == null ? 0L : entry.getValue());
                                validated.put(key, value);
                        }
                }

                xpByTierPath = validated;
                return changed;
        }

        public static HarvestXpConfig get() {
                return instance;
        }

        /**
         * Returns the configured skill experience for the provided crop tier path.
         *
         * @param tierPath the {@link net.minecraft.util.Identifier#getPath() path}
         *                portion of the crop tier identifier
         * @return the configured skill experience, or {@code 0} when the tier does
         *         not award experience
         */
        public long experienceForTierPath(String tierPath) {
                if (tierPath == null || tierPath.isEmpty()) {
                        return 0L;
                }

                Long value = xpByTierPath.get(tierPath);
                if (value != null) {
                                return Math.max(0L, value);
                }

                String normalized = tierPath.toLowerCase(Locale.ROOT);
                for (Map.Entry<String, Long> entry : xpByTierPath.entrySet()) {
                        if (Objects.equals(entry.getKey().toLowerCase(Locale.ROOT), normalized)) {
                                return Math.max(0L, entry.getValue());
                        }
                }

                return 0L;
        }

        Map<String, Long> xpByTierPath() {
                return xpByTierPath;
        }
}
