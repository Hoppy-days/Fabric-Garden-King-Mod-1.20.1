package net.jeremy.gardenkingmod.currency;

/**
 * Simple capability-style interface injected into {@link net.minecraft.entity.player.PlayerEntity}
 * to persist lifetime Garden Coin earnings across play sessions.
 */
public interface GardenCurrencyHolder {
        String LIFETIME_CURRENCY_KEY = "GardenKingLifetimeCurrency";
        String BANK_CURRENCY_KEY = "GardenKingBankCurrency";

        int gardenkingmod$getLifetimeCurrency();

        void gardenkingmod$setLifetimeCurrency(int amount);

        long gardenkingmod$getBankBalance();

        void gardenkingmod$setBankBalance(long amount);

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

        default long gardenkingmod$depositToBank(long amount) {
                if (amount <= 0) {
                        return gardenkingmod$getBankBalance();
                }

                long balance = gardenkingmod$getBankBalance();
                long updated;
                try {
                        updated = Math.addExact(balance, amount);
                } catch (ArithmeticException overflow) {
                        updated = Long.MAX_VALUE;
                }
                gardenkingmod$setBankBalance(updated);
                return updated;
        }

        default long gardenkingmod$withdrawFromBank(long amount) {
                if (amount <= 0) {
                        return gardenkingmod$getBankBalance();
                }

                long balance = gardenkingmod$getBankBalance();
                long updated = balance - amount;
                if (updated < 0) {
                        updated = 0;
                }
                gardenkingmod$setBankBalance(updated);
                return updated;
        }
}
