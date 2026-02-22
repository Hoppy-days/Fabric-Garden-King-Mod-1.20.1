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

    private Map<String, Double> enchantedTierSellMultipliers = new LinkedHashMap<>(Map.of(
            "crop_tiers/tier_1", 1.5D,
            "crop_tiers/tier_2", 2.0D,
            "crop_tiers/tier_3", 2.5D,
            "crop_tiers/tier_4", 3.0D,
            "crop_tiers/tier_5", 3.5D));

    private MarketEconomyConfig() {
    }

    private static MarketEconomyConfig defaults() {
        return new MarketEconomyConfig();
    }

    public static void reload() {
        MarketEconomyConfig defaults = defaults();

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
        } catch (IOException exception) {
            GardenKingMod.LOGGER.warn("Failed to create market economy config directory", exception);
        }

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

        if (enchantedTierSellMultipliers == null) {
            enchantedTierSellMultipliers = new LinkedHashMap<>(defaults.enchantedTierSellMultipliers);
            changed = true;
        } else {
            Map<String, Double> validated = new LinkedHashMap<>();
            for (Map.Entry<String, Double> entry : defaults.enchantedTierSellMultipliers.entrySet()) {
                Double configured = enchantedTierSellMultipliers.get(entry.getKey());
                if (configured == null || !Double.isFinite(configured) || configured < 1.0D) {
                    validated.put(entry.getKey(), entry.getValue());
                    changed = true;
                } else {
                    validated.put(entry.getKey(), configured);
                }
            }
            for (Map.Entry<String, Double> entry : enchantedTierSellMultipliers.entrySet()) {
                if (!validated.containsKey(entry.getKey())) {
                    double configured = entry.getValue() == null ? 1.0D : entry.getValue();
                    validated.put(entry.getKey(), !Double.isFinite(configured) ? 1.0D : Math.max(1.0D, configured));
                }
            }
            enchantedTierSellMultipliers = validated;
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


    public Map<String, Integer> getEffectiveTierSellValues() {
        Map<String, Integer> effective = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : tierBaseSellValues.entrySet()) {
            String normalizedTierPath = normalizeTierPathKey(entry.getKey());
            if (normalizedTierPath == null || effective.containsKey(normalizedTierPath)) {
                continue;
            }

            int resolved = resolveSellValueForTierPath(normalizedTierPath);
            if (resolved > 0) {
                effective.put(normalizedTierPath, resolved);
            }
        }
        return effective;
    }

    public int resolveSellValue(ItemStack stack, CropTier tier) {
        if (stack == null || stack.isEmpty() || tier == null) {
            return 0;
        }

        return resolveSellValueForTierPath(tier.id().getPath());
    }

    public int resolveSellValueForTierPath(String tierPath) {
        Integer tierValue = findTierValue(tierPath);
        if (tierValue == null || tierValue <= 0) {
            return 0;
        }

        return applySellMultiplier(tierValue);
    }

    public float resolveEnchantedSellMultiplier(CropTier tier, float fallbackMultiplier) {
        if (tier == null || tier.id() == null) {
            return sanitizeEnchantedMultiplier(fallbackMultiplier);
        }

        Double configured = findDoubleValue(enchantedTierSellMultipliers, tier.id().getPath());
        if (configured == null) {
            return sanitizeEnchantedMultiplier(fallbackMultiplier);
        }

        return sanitizeEnchantedMultiplier(configured.floatValue());
    }

    private Integer findTierValue(String tierPath) {
        return findIntegerValue(tierBaseSellValues, tierPath);
    }

    private static String normalizeTierPathKey(String tierPath) {
        if (tierPath == null) {
            return null;
        }

        String trimmed = tierPath.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.toLowerCase(Locale.ROOT);
    }

    private static Integer findIntegerValue(Map<String, Integer> values, String key) {
        if (values == null || key == null || key.isBlank()) {
            return null;
        }

        Integer direct = values.get(key);
        if (direct != null) {
            return direct;
        }

        String normalized = key.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            if (Objects.equals(entry.getKey().toLowerCase(Locale.ROOT), normalized)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private static Double findDoubleValue(Map<String, Double> values, String key) {
        if (values == null || key == null || key.isBlank()) {
            return null;
        }

        Double direct = values.get(key);
        if (direct != null) {
            return direct;
        }

        String normalized = key.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            if (Objects.equals(entry.getKey().toLowerCase(Locale.ROOT), normalized)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private static float sanitizeEnchantedMultiplier(float value) {
        if (!Float.isFinite(value) || value < 1.0f) {
            return 1.0f;
        }
        return value;
    }

    private int applySellMultiplier(int baseValue) {
        if (baseValue <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(baseValue * marketSellMultiplier));
    }
}
