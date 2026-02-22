package net.jeremy.gardenkingmod.client.market;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import net.jeremy.gardenkingmod.crop.CropTier;

/**
 * Client-side cache for server-authoritative market sell values.
 */
public final class ServerMarketPricingState {
    private static final ServerMarketPricingState INSTANCE = new ServerMarketPricingState();

    private final Map<String, Integer> sellValuesByTierPath = new LinkedHashMap<>();

    private ServerMarketPricingState() {
    }

    public static ServerMarketPricingState getInstance() {
        return INSTANCE;
    }

    public synchronized void update(Map<String, Integer> values) {
        sellValuesByTierPath.clear();
        if (values == null || values.isEmpty()) {
            return;
        }

        values.forEach((path, value) -> {
            if (path == null || path.isBlank() || value == null || value <= 0) {
                return;
            }
            sellValuesByTierPath.put(path.toLowerCase(Locale.ROOT), value);
        });
    }

    public synchronized void reset() {
        sellValuesByTierPath.clear();
    }

    public synchronized int resolveSellValue(CropTier tier) {
        if (tier == null || tier.id() == null) {
            return 0;
        }

        String tierPath = tier.id().getPath();
        if (tierPath == null || tierPath.isBlank()) {
            return 0;
        }

        return Math.max(0, sellValuesByTierPath.getOrDefault(tierPath.toLowerCase(Locale.ROOT), 0));
    }
}
