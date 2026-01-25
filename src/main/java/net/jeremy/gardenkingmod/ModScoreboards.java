package net.jeremy.gardenkingmod;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.jeremy.gardenkingmod.currency.GardenCurrencyHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class ModScoreboards {
        public static final String CURRENCY_OBJECTIVE = "garden_currency";
        public static final String BANK_CURRENCY_OBJECTIVE = "garden_currency_bank";

        private ModScoreboards() {
        }

        public static void registerScoreboards() {
                ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                        Scoreboard scoreboard = server.getScoreboard();
                        ensureLifetimeObjective(scoreboard);
                        ensureBankObjective(scoreboard);
                });
                ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncPlayerBalances(handler.player));
        }

        public static int addCurrency(ServerPlayerEntity player, int amount) {
                if (amount <= 0) {
                        return player instanceof GardenCurrencyHolder holder ? holder.gardenkingmod$getLifetimeCurrency() : -1;
                }

                MinecraftServer server = player.getServer();
                if (server == null) {
                        return -1;
                }

                Scoreboard scoreboard = server.getScoreboard();
                ScoreboardObjective objective = ensureLifetimeObjective(scoreboard);
                ScoreboardPlayerScore score = scoreboard.getPlayerScore(player.getEntityName(), objective);

                int updatedScore;
                if (player instanceof GardenCurrencyHolder holder) {
                        updatedScore = holder.gardenkingmod$addToLifetimeCurrency(amount);
                } else {
                        updatedScore = score.getScore() + amount;
                }

                score.setScore(updatedScore);
                return updatedScore;
        }

        public static void syncPlayerBalances(ServerPlayerEntity player) {
                MinecraftServer server = player.getServer();
                if (server == null) {
                        return;
                }

                Scoreboard scoreboard = server.getScoreboard();
                ScoreboardObjective lifetimeObjective = ensureLifetimeObjective(scoreboard);
                ScoreboardObjective bankObjective = ensureBankObjective(scoreboard);
                ScoreboardPlayerScore lifetimeScore = scoreboard.getPlayerScore(player.getEntityName(), lifetimeObjective);
                ScoreboardPlayerScore bankScore = scoreboard.getPlayerScore(player.getEntityName(), bankObjective);

                if (player instanceof GardenCurrencyHolder holder) {
                        lifetimeScore.setScore(holder.gardenkingmod$getLifetimeCurrency());

                        long bankBalance = holder.gardenkingmod$getBankBalance();
                        int clamped = bankBalance > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(0L, bankBalance);
                        bankScore.setScore(clamped);
                } else {
                        lifetimeScore.setScore(0);
                        bankScore.setScore(0);
                }
        }

        public static int getLifetimeCurrency(ServerPlayerEntity player) {
                if (player instanceof GardenCurrencyHolder holder) {
                        return holder.gardenkingmod$getLifetimeCurrency();
                }

                MinecraftServer server = player.getServer();
                if (server == null) {
                        return 0;
                }

                Scoreboard scoreboard = server.getScoreboard();
                ScoreboardObjective objective = ensureLifetimeObjective(scoreboard);
                return scoreboard.getPlayerScore(player.getEntityName(), objective).getScore();
        }

        public static long getBankBalance(ServerPlayerEntity player) {
                if (player instanceof GardenCurrencyHolder holder) {
                        return holder.gardenkingmod$getBankBalance();
                }

                MinecraftServer server = player.getServer();
                if (server == null) {
                        return 0L;
                }

                Scoreboard scoreboard = server.getScoreboard();
                ScoreboardObjective objective = ensureBankObjective(scoreboard);
                return scoreboard.getPlayerScore(player.getEntityName(), objective).getScore();
        }

        private static ScoreboardObjective ensureLifetimeObjective(Scoreboard scoreboard) {
                return ensureObjective(scoreboard, CURRENCY_OBJECTIVE, "scoreboard.gardenkingmod.garden_currency");
        }

        private static ScoreboardObjective ensureBankObjective(Scoreboard scoreboard) {
                return ensureObjective(scoreboard, BANK_CURRENCY_OBJECTIVE, "scoreboard.gardenkingmod.garden_currency_bank");
        }

        private static ScoreboardObjective ensureObjective(Scoreboard scoreboard, String name, String translationKey) {
                ScoreboardObjective objective = scoreboard.getObjective(name);
                if (objective == null) {
                        objective = scoreboard.addObjective(name, ScoreboardCriterion.DUMMY, Text.translatable(translationKey), ScoreboardCriterion.RenderType.INTEGER);
                        GardenKingMod.LOGGER.info("Created scoreboard objective {}", name);
                }

                return objective;
        }
}
