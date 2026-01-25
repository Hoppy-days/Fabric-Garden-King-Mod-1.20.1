package net.jeremy.gardenkingmod.entity.crow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import net.jeremy.gardenkingmod.GardenKingMod;

/**
 * Loads configurable balance settings for the crow entity from
 * <code>config/gardenkingmod/crow.json</code>. The JSON file is written the
 * first time the game runs so packs or administrators can adjust values
 * without recompiling the mod.
 */
public final class CrowBalanceConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(GardenKingMod.MOD_ID)
            .resolve("crow.json");

    private static volatile CrowBalanceConfig instance = new CrowBalanceConfig();

    private int minHungerTicks = 600;
    private int maxHungerTicks = 1200;
    private int cropSearchHorizontal = 12;
    private int cropSearchVertical = 6;
    private double randomFlightRange = 8.0;
    private double perchSearchRange = 6.0;
    private double wardHorizontalRadius = 12.0;
    private double wardVerticalRadius = 8.0;
    private double wardFearRadiusMultiplier = 1.0;
    private boolean dropLootOnCropBreak = true;
    private double baseHealth = 10.0;
    private double flyingSpeed = 0.6;
    private double movementSpeed = 0.25;
    private int spawnWeight = 6;
    private int minSpawnGroupSize = 1;
    private int maxSpawnGroupSize = 3;

    private CrowBalanceConfig() {
    }

    /**
     * Ensures the config file is written to disk and updates the cached
     * configuration values.
     */
    public static void reload() {
        CrowBalanceConfig defaults = new CrowBalanceConfig();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to create crow config directory", exception);
        }

        if (Files.notExists(CONFIG_PATH)) {
            writeConfigFile(defaults);
            instance = defaults;
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            CrowBalanceConfig loaded = GSON.fromJson(reader, CrowBalanceConfig.class);
            if (loaded == null) {
                GardenKingMod.LOGGER.warn("Crow config file was empty; using defaults");
                instance = defaults;
            } else {
                boolean updated = loaded.validateAndApplyDefaults(defaults);
                if (updated) {
                    writeConfigFile(loaded);
                }
                instance = loaded;
            }
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to read crow config; falling back to defaults", exception);
            instance = defaults;
        }
    }

    private static void writeConfigFile(CrowBalanceConfig config) {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to write default crow config", exception);
        }
    }

    private boolean validateAndApplyDefaults(CrowBalanceConfig defaults) {
        boolean changed = false;
        if (minHungerTicks <= 0) {
            minHungerTicks = defaults.minHungerTicks;
            changed = true;
        }

        if (maxHungerTicks < minHungerTicks) {
            maxHungerTicks = Math.max(minHungerTicks, defaults.maxHungerTicks);
            changed = true;
        }

        if (cropSearchHorizontal <= 0) {
            cropSearchHorizontal = defaults.cropSearchHorizontal;
            changed = true;
        }

        if (cropSearchVertical <= 0) {
            cropSearchVertical = defaults.cropSearchVertical;
            changed = true;
        }

        if (randomFlightRange <= 0.0) {
            randomFlightRange = defaults.randomFlightRange;
            changed = true;
        }

        if (perchSearchRange <= 0.0) {
            perchSearchRange = defaults.perchSearchRange;
            changed = true;
        }

        if (wardHorizontalRadius <= 0.0) {
            wardHorizontalRadius = defaults.wardHorizontalRadius;
            changed = true;
        }

        if (wardVerticalRadius <= 0.0) {
            wardVerticalRadius = defaults.wardVerticalRadius;
            changed = true;
        }

        if (wardFearRadiusMultiplier <= 0.0) {
            wardFearRadiusMultiplier = defaults.wardFearRadiusMultiplier;
            changed = true;
        }

        if (baseHealth <= 0.0) {
            baseHealth = defaults.baseHealth;
            changed = true;
        }

        if (flyingSpeed <= 0.0) {
            flyingSpeed = defaults.flyingSpeed;
            changed = true;
        }

        if (movementSpeed <= 0.0) {
            movementSpeed = defaults.movementSpeed;
            changed = true;
        }

        if (spawnWeight < 0) {
            spawnWeight = defaults.spawnWeight;
            changed = true;
        }

        if (minSpawnGroupSize <= 0) {
            minSpawnGroupSize = defaults.minSpawnGroupSize;
            changed = true;
        }

        if (maxSpawnGroupSize < minSpawnGroupSize) {
            maxSpawnGroupSize = Math.max(minSpawnGroupSize, defaults.maxSpawnGroupSize);
            changed = true;
        }

        return changed;
    }

    public static CrowBalanceConfig get() {
        return instance;
    }

    public int chooseHungerDuration() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int min = Math.min(minHungerTicks, maxHungerTicks);
        int max = Math.max(minHungerTicks, maxHungerTicks);
        if (min == max) {
            return min;
        }
        return random.nextInt(min, max + 1);
    }

    public int cropSearchHorizontal() {
        return cropSearchHorizontal;
    }

    public int cropSearchVertical() {
        return cropSearchVertical;
    }

    public double randomFlightRange() {
        return randomFlightRange;
    }

    public double perchSearchRange() {
        return perchSearchRange;
    }

    public double wardHorizontalRadius() {
        return wardHorizontalRadius;
    }

    public double wardVerticalRadius() {
        return wardVerticalRadius;
    }

    public double wardFearRadiusMultiplier() {
        return wardFearRadiusMultiplier;
    }

    public boolean dropLootOnCropBreak() {
        return dropLootOnCropBreak;
    }

    public int minHungerTicks() {
        return minHungerTicks;
    }

    public int maxHungerTicks() {
        return maxHungerTicks;
    }

    public double baseHealth() {
        return baseHealth;
    }

    public double flyingSpeed() {
        return flyingSpeed;
    }

    public double movementSpeed() {
        return movementSpeed;
    }

    public int spawnWeight() {
        return spawnWeight;
    }

    public int minSpawnGroupSize() {
        return minSpawnGroupSize;
    }

    public int maxSpawnGroupSize() {
        return maxSpawnGroupSize;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "CrowBalanceConfig{hunger=%d-%d,cropRadius=%d,%d,wardRadius=%.2f/%.2f,loot=%s,spawn=%d:%d-%d}",
                minHungerTicks, maxHungerTicks, cropSearchHorizontal, cropSearchVertical, wardHorizontalRadius,
                wardVerticalRadius, dropLootOnCropBreak, spawnWeight, minSpawnGroupSize, maxSpawnGroupSize);
    }
}
