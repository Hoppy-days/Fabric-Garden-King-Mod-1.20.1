package net.jeremy.gardenkingmod.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.jeremy.gardenkingmod.GardenKingMod;

public final class GardenOvenBalanceConfig {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
                        .resolve(GardenKingMod.MOD_ID)
                        .resolve("garden_oven.json");

        private static volatile GardenOvenBalanceConfig instance = new GardenOvenBalanceConfig();

        private int cookTime = 200;

        private GardenOvenBalanceConfig() {
        }

        public static void reload() {
                GardenOvenBalanceConfig defaults = new GardenOvenBalanceConfig();

                try {
                        Files.createDirectories(CONFIG_PATH.getParent());
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed to create oven config directory", exception);
                }

                if (Files.notExists(CONFIG_PATH)) {
                        writeConfigFile(defaults);
                        instance = defaults;
                        return;
                }

                try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                        GardenOvenBalanceConfig loaded = GSON.fromJson(reader, GardenOvenBalanceConfig.class);
                        if (loaded == null) {
                                GardenKingMod.LOGGER.warn("Garden oven config file was empty; using defaults");
                                instance = defaults;
                        } else {
                                boolean updated = loaded.validateAndApplyDefaults(defaults);
                                if (updated) {
                                        writeConfigFile(loaded);
                                }
                                instance = loaded;
                        }
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed to read garden oven config; falling back to defaults", exception);
                        instance = defaults;
                }
        }

        private static void writeConfigFile(GardenOvenBalanceConfig config) {
                try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
                        GSON.toJson(config, writer);
                } catch (IOException exception) {
                        GardenKingMod.LOGGER.warn("Failed to write garden oven config", exception);
                }
        }

        private boolean validateAndApplyDefaults(GardenOvenBalanceConfig defaults) {
                boolean changed = false;

                if (cookTime <= 0) {
                        cookTime = defaults.cookTime;
                        changed = true;
                }

                return changed;
        }

        public static GardenOvenBalanceConfig get() {
                return instance;
        }

        public int cookTime() {
                return cookTime;
        }
}
