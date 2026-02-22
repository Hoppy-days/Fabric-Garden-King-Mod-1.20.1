package net.jeremy.gardenkingmod.shop;

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
import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.crop.CropTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Market economy configuration with simple global multipliers.
 *
 * Buy pricing: {@code marketBuyDollarMultiplier} scales dollar costs loaded from
 * {@code garden_market_offers.json}.
 *
 * Sell pricing: crop/cooked item must be in a crop tier tag; the tier's base
 * value is read from {@code tierBaseSellValues}, then multiplied by
 * {@code marketSellMultiplier}.
 */
public final class MarketEconomyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("market_economy.json");
    private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(GardenKingMod.MOD_ID)
            .resolve("market_economy.json");

    private static volatile MarketEconomyConfig instance = defaults();

    private double marketBuyDollarMultiplier = 1.0D;
    private double marketSellMultiplier = 1.0D;

    private Map<String, Integer> tierBaseSellValues = new LinkedHashMap<>(Map.of(
            "crop_tiers/tier_1", 1,
            "crop_tiers/tier_2", 2,
            "crop_tiers/tier_3", 4,
            "crop_tiers/tier_4", 7,
            "crop_tiers/tier_5", 11));

    private MarketEconomyConfig() {
    }

    private static MarketEconomyConfig defaults() {
        return new MarketEconomyConfig();
    }

    public static void reload() {
        MarketEconomyConfig defaults = defaults();

        migrateLegacyConfigIfNeeded();

        if (Files.notExists(CONFIG_PATH)) {
            writeConfigFile(defaults);
            instance = defaults;
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            MarketEconomyConfig loaded = GSON.fromJson(reader, MarketEconomyConfig.class);
            if (loaded == null) {
                GardenKingMod.LOGGER.warn("Market economy config file was empty; using defaults");
                instance = defaults;
            } else {
                boolean updated = loaded.validateAndApplyDefaults(defaults);
                if (updated) {
                    writeConfigFile(loaded);
                }
                instance = loaded;
            }
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to read market economy config; falling back to defaults", exception);
            instance = defaults;
        }
    }

    private static void writeConfigFile(MarketEconomyConfig config) {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to write market economy config", exception);
        }
    }

    private static void migrateLegacyConfigIfNeeded() {
        if (Files.exists(CONFIG_PATH) || Files.notExists(LEGACY_CONFIG_PATH)) {
            return;
        }

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.copy(LEGACY_CONFIG_PATH, CONFIG_PATH);
            GardenKingMod.LOGGER.info("Migrated market economy config from {} to {}",
                    LEGACY_CONFIG_PATH, CONFIG_PATH);
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to migrate legacy market economy config", exception);
        }
    }

    public static Path configPath() {
        return CONFIG_PATH;
    }

    private boolean validateAndApplyDefaults(MarketEconomyConfig defaults) {
        boolean changed = false;

        if (!Double.isFinite(marketBuyDollarMultiplier) || marketBuyDollarMultiplier <= 0.0D) {
            marketBuyDollarMultiplier = defaults.marketBuyDollarMultiplier;
            changed = true;
        }

        if (!Double.isFinite(marketSellMultiplier) || marketSellMultiplier <= 0.0D) {
            marketSellMultiplier = defaults.marketSellMultiplier;
            changed = true;
        }

        if (tierBaseSellValues == null) {
            tierBaseSellValues = new LinkedHashMap<>(defaults.tierBaseSellValues);
            changed = true;
        } else {
            Map<String, Integer> validated = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : defaults.tierBaseSellValues.entrySet()) {
                Integer configured = tierBaseSellValues.get(entry.getKey());
                if (configured == null || configured < 0) {
                    validated.put(entry.getKey(), entry.getValue());
                    changed = true;
                } else {
                    validated.put(entry.getKey(), configured);
                }
            }
            for (Map.Entry<String, Integer> entry : tierBaseSellValues.entrySet()) {
                if (!validated.containsKey(entry.getKey())) {
                    validated.put(entry.getKey(), Math.max(0, entry.getValue() == null ? 0 : entry.getValue()));
                }
            }
            tierBaseSellValues = validated;
        }

        return changed;
    }

    public static MarketEconomyConfig get() {
        return instance;
    }

    public int applyBuyMultiplier(ItemStack originalCost) {
        if (originalCost == null || originalCost.isEmpty()) {
            return 0;
        }

        Item item = originalCost.getItem();
        if (item != ModItems.DOLLAR) {
            return originalCost.getCount();
        }

        int base = GearShopStackHelper.getRequestedCount(originalCost);
        return Math.max(1, (int) Math.round(base * marketBuyDollarMultiplier));
    }

    public int resolveSellValue(ItemStack stack, CropTier tier) {
        if (stack == null || stack.isEmpty() || tier == null) {
            return 0;
        }

        Integer tierValue = findTierValue(tier.id().getPath());
        if (tierValue == null || tierValue <= 0) {
            return 0;
        }

        return applySellMultiplier(tierValue);
    }

    private Integer findTierValue(String tierPath) {
        if (tierPath == null || tierPath.isBlank()) {
            return null;
        }

        Integer direct = tierBaseSellValues.get(tierPath);
        if (direct != null) {
            return direct;
        }

        String normalized = tierPath.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Integer> entry : tierBaseSellValues.entrySet()) {
            if (Objects.equals(entry.getKey().toLowerCase(Locale.ROOT), normalized)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private int applySellMultiplier(int baseValue) {
        if (baseValue <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(baseValue * marketSellMultiplier));
    }
}
