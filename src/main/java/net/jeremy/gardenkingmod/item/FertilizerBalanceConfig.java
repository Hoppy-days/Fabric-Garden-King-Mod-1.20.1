package net.jeremy.gardenkingmod.item;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import net.jeremy.gardenkingmod.GardenKingMod;

/**
 * Loads configurable fertilizer settings from
 * <code>config/gardenkingmod/fertilizer.json</code>. The JSON file is written on
 * first launch so that pack makers and administrators can balance the growth
 * and composting rates without recompiling the mod.
 */
public final class FertilizerBalanceConfig {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
                        .resolve(GardenKingMod.MOD_ID)
                        .resolve("fertilizer.json");

        private static volatile FertilizerBalanceConfig instance = new FertilizerBalanceConfig();

        private double fertilizerGrowthChance = 0.5;
        private double rottenCompostChance = 0.65;
        private int fertilizerOutputCount = 1;

        private FertilizerBalanceConfig() {
        }

        /**
         * Ensures the fertilizer config exists on disk and updates the cached values
         * used by the game.
         */
        public static void reload() {
                FertilizerBalanceConfig defaults = new FertilizerBalanceConfig();
                try {
                        Files.createDirectories(CONFIG_PATH.getParent());
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed to create fertilizer config directory", exception);
                }

                if (Files.notExists(CONFIG_PATH)) {
                        writeConfigFile(defaults);
                        instance = defaults;
                        return;
                }

                try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                        FertilizerBalanceConfig loaded = GSON.fromJson(reader, FertilizerBalanceConfig.class);
                        if (loaded == null) {
                                GardenKingMod.LOGGER.warn("Fertilizer config file was empty; using defaults");
                                instance = defaults;
                        } else {
                                boolean updated = loaded.validateAndApplyDefaults(defaults);
                                if (updated) {
                                        writeConfigFile(loaded);
                                }
                                instance = loaded;
                        }
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed to read fertilizer config; falling back to defaults", exception);
                        instance = defaults;
                }
        }

        private static void writeConfigFile(FertilizerBalanceConfig config) {
                try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
                        GSON.toJson(config, writer);
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed to write default fertilizer config", exception);
                }
        }

        private boolean validateAndApplyDefaults(FertilizerBalanceConfig defaults) {
                boolean changed = false;

                if (Double.isNaN(fertilizerGrowthChance) || fertilizerGrowthChance < 0.0
                                || fertilizerGrowthChance > 1.0) {
                        fertilizerGrowthChance = defaults.fertilizerGrowthChance;
                        changed = true;
                }

                if (Double.isNaN(rottenCompostChance) || rottenCompostChance < 0.0 || rottenCompostChance > 1.0) {
                        rottenCompostChance = defaults.rottenCompostChance;
                        changed = true;
                }

                if (fertilizerOutputCount <= 0) {
                        fertilizerOutputCount = defaults.fertilizerOutputCount;
                        changed = true;
                }

                return changed;
        }

        public static FertilizerBalanceConfig get() {
                return instance;
        }

        public double fertilizerGrowthChance() {
                return fertilizerGrowthChance;
        }

        public double rottenCompostChance() {
                return rottenCompostChance;
        }

        public int fertilizerOutputCount() {
                return fertilizerOutputCount;
        }
}
