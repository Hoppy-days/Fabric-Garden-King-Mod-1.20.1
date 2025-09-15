package net.jeremy.gardenkingmod;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
        }

        public static void addCurrency(ServerPlayerEntity player, int amount) {
                if (amount <= 0) {
                        return;
                }

                MinecraftServer server = player.getServer();
                if (server == null) {
                        return;
                }

                Scoreboard scoreboard = server.getScoreboard();
                ScoreboardObjective objective = ensureObjective(scoreboard);
                if (objective == null) {
                        return;
                }

                ScoreboardPlayerScore score = scoreboard.getPlayerScore(player.getEntityName(), objective);
                score.add(amount);
        }

        private static ScoreboardObjective ensureObjective(Scoreboard scoreboard) {
                ScoreboardObjective objective = scoreboard.getObjective(CURRENCY_OBJECTIVE);
                if (objective == null) {
                        objective = scoreboard.addObjective(CURRENCY_OBJECTIVE, ScoreboardCriterion.DUMMY, Text.literal("Garden Coins"), ScoreboardCriterion.RenderType.INTEGER);
                        GardenKingMod.LOGGER.info("Created scoreboard objective {}", CURRENCY_OBJECTIVE);
                }

                return objective;
        }
}
