package net.jeremy.gardenkingmod.event;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class EndlessNightConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(GardenKingMod.MOD_ID)
            .resolve("endless_night.json");

    private static volatile EndlessNightConfig instance = defaults();

    private double triggerChance = 0.18D;
    private boolean hardcoreEnabled = false;
    private long nightLockTime = 18000L;

    private int spawnBoostIntervalTicks = 120;
    private double spawnBoostChancePerPlayer = 0.22D;

    private List<TaskEntry> normalTaskPool = new ArrayList<>(List.of(
            new TaskEntry("minecraft:zombie", 50),
            new TaskEntry("minecraft:skeleton", 35),
            new TaskEntry("minecraft:spider", 40),
            new TaskEntry("minecraft:creeper", 20),
            new TaskEntry("minecraft:husk", 35),
            new TaskEntry("minecraft:drowned", 30)));

    private List<TaskEntry> hardcoreTaskPool = new ArrayList<>(List.of(
            new TaskEntry("minecraft:witch", 20),
            new TaskEntry("minecraft:enderman", 30),
            new TaskEntry("minecraft:blaze", 28),
            new TaskEntry("minecraft:pillager", 40),
            new TaskEntry("minecraft:ravager", 8),
            new TaskEntry("minecraft:stray", 45)));

    private EndlessNightConfig() {
    }

    private static EndlessNightConfig defaults() {
        return new EndlessNightConfig();
    }

    public static void reload() {
        EndlessNightConfig defaults = defaults();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to create Endless Night config directory", exception);
        }

        if (Files.notExists(CONFIG_PATH)) {
            writeConfigFile(defaults);
            instance = defaults;
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            EndlessNightConfig loaded = GSON.fromJson(reader, EndlessNightConfig.class);
            if (loaded == null) {
                instance = defaults;
                return;
            }

            boolean changed = loaded.validateAndApplyDefaults(defaults);
            if (changed) {
                writeConfigFile(loaded);
            }
            instance = loaded;
        } catch (JsonParseException exception) {
            GardenKingMod.LOGGER.warn("Malformed Endless Night config; using defaults", exception);
            instance = defaults;
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to load Endless Night config; using defaults", exception);
            instance = defaults;
        }
    }

    private static void writeConfigFile(EndlessNightConfig config) {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to write Endless Night config", exception);
        }
    }

    private boolean validateAndApplyDefaults(EndlessNightConfig defaults) {
        boolean changed = false;

        if (!Double.isFinite(triggerChance) || triggerChance < 0.0D || triggerChance > 1.0D) {
            triggerChance = defaults.triggerChance;
            changed = true;
        }

        if (nightLockTime < 13000L || nightLockTime > 22000L) {
            nightLockTime = defaults.nightLockTime;
            changed = true;
        }

        if (spawnBoostIntervalTicks < 20) {
            spawnBoostIntervalTicks = defaults.spawnBoostIntervalTicks;
            changed = true;
        }

        if (!Double.isFinite(spawnBoostChancePerPlayer)
                || spawnBoostChancePerPlayer < 0.0D
                || spawnBoostChancePerPlayer > 1.0D) {
            spawnBoostChancePerPlayer = defaults.spawnBoostChancePerPlayer;
            changed = true;
        }

        List<TaskEntry> validatedNormal = validateTaskPool(normalTaskPool, defaults.normalTaskPool);
        if (!validatedNormal.equals(normalTaskPool)) {
            normalTaskPool = validatedNormal;
            changed = true;
        }

        List<TaskEntry> validatedHardcore = validateTaskPool(hardcoreTaskPool, defaults.hardcoreTaskPool);
        if (!validatedHardcore.equals(hardcoreTaskPool)) {
            hardcoreTaskPool = validatedHardcore;
            changed = true;
        }

        return changed;
    }

    private static List<TaskEntry> validateTaskPool(List<TaskEntry> configured, List<TaskEntry> fallback) {
        if (configured == null || configured.isEmpty()) {
            return new ArrayList<>(fallback);
        }

        List<TaskEntry> validated = new ArrayList<>();
        Set<String> dedupe = new LinkedHashSet<>();

        for (TaskEntry entry : configured) {
            if (entry == null || entry.mobId == null || entry.mobId.isBlank() || entry.killCount <= 0) {
                continue;
            }
            Identifier id = Identifier.tryParse(entry.mobId);
            if (id == null || !Registries.ENTITY_TYPE.containsId(id)) {
                continue;
            }
            EntityType<?> type = Registries.ENTITY_TYPE.get(id);
            if (type == EntityType.PLAYER || !type.isSpawnable()) {
                continue;
            }
            if (!dedupe.add(entry.mobId)) {
                continue;
            }
            validated.add(new TaskEntry(entry.mobId, entry.killCount));
        }

        return validated.isEmpty() ? new ArrayList<>(fallback) : validated;
    }

    public static EndlessNightConfig get() {
        return instance;
    }

    public static Path configPath() {
        return CONFIG_PATH;
    }

    public double triggerChance() {
        return triggerChance;
    }

    public boolean hardcoreEnabled() {
        return hardcoreEnabled;
    }

    public long nightLockTime() {
        return nightLockTime;
    }

    public int spawnBoostIntervalTicks() {
        return spawnBoostIntervalTicks;
    }

    public double spawnBoostChancePerPlayer() {
        return spawnBoostChancePerPlayer;
    }

    public List<TaskEntry> activeTaskPool() {
        return hardcoreEnabled ? hardcoreTaskPool : normalTaskPool;
    }

    public static final class TaskEntry {
        private String mobId;
        private int killCount;

        public TaskEntry(String mobId, int killCount) {
            this.mobId = mobId;
            this.killCount = killCount;
        }

        public String mobId() {
            return mobId;
        }

        public int killCount() {
            return killCount;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof TaskEntry entry)) {
                return false;
            }
            return mobId.equals(entry.mobId) && killCount == entry.killCount;
        }

        @Override
        public int hashCode() {
            return mobId.hashCode() * 31 + killCount;
        }
    }
}
