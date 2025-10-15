package net.jeremy.gardenkingmod.client.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;
import net.jeremy.gardenkingmod.GardenKingMod;

/**
 * Allows pack makers to configure how the bank block item is displayed when it
 * is rendered in inventories or held by the player. The settings are written to
 * {@code config/gardenkingmod/bank_item_display.json} on first launch so they
 * can be tweaked without recompiling the mod.
 */
public final class BankItemDisplayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(GardenKingMod.MOD_ID)
            .resolve("bank_item_display.json");

    private static volatile BankItemDisplayConfig instance = new BankItemDisplayConfig();

    private DisplayTransform gui = DisplayTransform.guiDefaults();
    private DisplayTransform ground = DisplayTransform.groundDefaults();
    private DisplayTransform fixed = DisplayTransform.fixedDefaults();
    private DisplayTransform hand = DisplayTransform.handDefaults();

    private BankItemDisplayConfig() {
    }

    /**
     * Ensures the bank item display config exists on disk and updates the cached
     * values used by the renderer.
     */
    public static void reload() {
        BankItemDisplayConfig defaults = new BankItemDisplayConfig();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to create bank item config directory", exception);
        }

        if (Files.notExists(CONFIG_PATH)) {
            writeConfigFile(defaults);
            instance = defaults;
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            BankItemDisplayConfig loaded = GSON.fromJson(reader, BankItemDisplayConfig.class);
            if (loaded == null) {
                GardenKingMod.LOGGER.warn("Bank item config file was empty; using defaults");
                instance = defaults;
            } else {
                boolean updated = loaded.validateAndApplyDefaults(defaults);
                if (updated) {
                    writeConfigFile(loaded);
                }
                instance = loaded;
            }
        } catch (JsonParseException exception) {
            GardenKingMod.LOGGER.warn("Failed to parse bank item config; falling back to defaults", exception);
            instance = defaults;
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to read bank item config; falling back to defaults", exception);
            instance = defaults;
        }
    }

    private static void writeConfigFile(BankItemDisplayConfig config) {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to write default bank item config", exception);
        }
    }

    private boolean validateAndApplyDefaults(BankItemDisplayConfig defaults) {
        boolean changed = false;

        if (gui == null) {
            gui = DisplayTransform.copyOf(defaults.gui);
            changed = true;
        } else {
            changed |= gui.applyDefaults(defaults.gui);
        }

        if (ground == null) {
            ground = DisplayTransform.copyOf(defaults.ground);
            changed = true;
        } else {
            changed |= ground.applyDefaults(defaults.ground);
        }

        if (fixed == null) {
            fixed = DisplayTransform.copyOf(defaults.fixed);
            changed = true;
        } else {
            changed |= fixed.applyDefaults(defaults.fixed);
        }

        if (hand == null) {
            hand = DisplayTransform.copyOf(defaults.hand);
            changed = true;
        } else {
            changed |= hand.applyDefaults(defaults.hand);
        }

        return changed;
    }

    public static BankItemDisplayConfig get() {
        return instance;
    }

    public DisplayTransform gui() {
        return gui;
    }

    public DisplayTransform ground() {
        return ground;
    }

    public DisplayTransform fixed() {
        return fixed;
    }

    public DisplayTransform hand() {
        return hand;
    }

    public static final class DisplayTransform {
        private double scaleX;
        private double scaleY;
        private double scaleZ;
        private double translateX;
        private double translateY;
        private double translateZ;

        public DisplayTransform() {
        }

        private DisplayTransform(double scaleX, double scaleY, double scaleZ,
                double translateX, double translateY, double translateZ) {
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
            this.translateX = translateX;
            this.translateY = translateY;
            this.translateZ = translateZ;
        }

        private static DisplayTransform guiDefaults() {
            return new DisplayTransform(0.18, 0.18, 0.18, 0.0, 4.0, 0.0);
        }

        private static DisplayTransform groundDefaults() {
            return new DisplayTransform(0.18, 0.18, 0.18, 0.0, 2.8, 0.0);
        }

        private static DisplayTransform fixedDefaults() {
            return new DisplayTransform(0.18, 0.18, 0.18, 0.0, 3.0, 0.0);
        }

        private static DisplayTransform handDefaults() {
            return new DisplayTransform(0.16, 0.16, 0.16, 0.0, 3.0, 0.0);
        }

        private static DisplayTransform copyOf(DisplayTransform transform) {
            return new DisplayTransform(transform.scaleX, transform.scaleY, transform.scaleZ,
                    transform.translateX, transform.translateY, transform.translateZ);
        }

        private boolean applyDefaults(DisplayTransform defaults) {
            boolean changed = false;

            if (!isFinite(scaleX) || scaleX == 0.0) {
                scaleX = defaults.scaleX;
                changed = true;
            }
            if (!isFinite(scaleY) || scaleY == 0.0) {
                scaleY = defaults.scaleY;
                changed = true;
            }
            if (!isFinite(scaleZ) || scaleZ == 0.0) {
                scaleZ = defaults.scaleZ;
                changed = true;
            }
            if (!isFinite(translateX)) {
                translateX = defaults.translateX;
                changed = true;
            }
            if (!isFinite(translateY)) {
                translateY = defaults.translateY;
                changed = true;
            }
            if (!isFinite(translateZ)) {
                translateZ = defaults.translateZ;
                changed = true;
            }

            return changed;
        }

        private static boolean isFinite(double value) {
            return !Double.isNaN(value) && !Double.isInfinite(value);
        }

        public float scaleX() {
            return (float) scaleX;
        }

        public float scaleY() {
            return (float) scaleY;
        }

        public float scaleZ() {
            return (float) scaleZ;
        }

        public float translateX() {
            return (float) translateX;
        }

        public float translateY() {
            return (float) translateY;
        }

        public float translateZ() {
            return (float) translateZ;
        }
    }
}
