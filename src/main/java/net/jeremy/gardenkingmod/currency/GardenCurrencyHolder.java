package net.jeremy.gardenkingmod.currency;

/**
 * Simple capability-style interface injected into {@link net.minecraft.entity.player.PlayerEntity}
 * to persist lifetime Garden Coin earnings across play sessions.
 */
public interface GardenCurrencyHolder {
        String LIFETIME_CURRENCY_KEY = "GardenKingLifetimeCurrency";

        int gardenkingmod$getLifetimeCurrency();

        void gardenkingmod$setLifetimeCurrency(int amount);

        default int gardenkingmod$addToLifetimeCurrency(int amount) {
                if (amount == 0) {
                        return gardenkingmod$getLifetimeCurrency();
                }

                int updated = gardenkingmod$getLifetimeCurrency() + amount;
                if (updated < 0) {
                        updated = 0;
                }
                gardenkingmod$setLifetimeCurrency(updated);
                return updated;
        }
}
