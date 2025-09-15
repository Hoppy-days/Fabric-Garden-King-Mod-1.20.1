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

        private ModScoreboards() {
        }

        public static void registerScoreboards() {
                ServerLifecycleEvents.SERVER_STARTED.register(server -> ensureObjective(server.getScoreboard()));
                ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncPlayerScore(handler.player));
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
                ScoreboardObjective objective = ensureObjective(scoreboard);
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

        private static void syncPlayerScore(ServerPlayerEntity player) {
                MinecraftServer server = player.getServer();
                if (server == null) {
                        return;
                }

                Scoreboard scoreboard = server.getScoreboard();
                ScoreboardObjective objective = ensureObjective(scoreboard);
                ScoreboardPlayerScore score = scoreboard.getPlayerScore(player.getEntityName(), objective);

                if (player instanceof GardenCurrencyHolder holder) {
                        score.setScore(holder.gardenkingmod$getLifetimeCurrency());
                }
        }

        private static ScoreboardObjective ensureObjective(Scoreboard scoreboard) {
                ScoreboardObjective objective = scoreboard.getObjective(CURRENCY_OBJECTIVE);
                if (objective == null) {
                        objective = scoreboard.addObjective(CURRENCY_OBJECTIVE, ScoreboardCriterion.DUMMY, Text.translatable("scoreboard.gardenkingmod.garden_currency"), ScoreboardCriterion.RenderType.INTEGER);
                        GardenKingMod.LOGGER.info("Created scoreboard objective {}", CURRENCY_OBJECTIVE);
                }

                return objective;
        }
}
