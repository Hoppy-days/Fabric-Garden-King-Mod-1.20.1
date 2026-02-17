package net.jeremy.gardenkingmod.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jeremy.gardenkingmod.ModScoreboards;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.Text;

/**
 * Client HUD overlay for showing the top Garden Dollars leaderboard entries.
 */
public final class GardenLeaderboardHudOverlay implements HudRenderCallback {
    public static final GardenLeaderboardHudOverlay INSTANCE = new GardenLeaderboardHudOverlay();

    private static final int MAX_ENTRIES = 8;

    private boolean visible;

    private GardenLeaderboardHudOverlay() {
        this.visible = false;
    }

    public void toggleVisible() {
        this.visible = !this.visible;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        if (!visible) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null || client.options.hudHidden) {
            return;
        }

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjective(ModScoreboards.CURRENCY_OBJECTIVE);
        if (objective == null) {
            return;
        }

        List<ScoreboardPlayerScore> scores = new ArrayList<>(scoreboard.getAllPlayerScores(objective));
        scores.removeIf(score -> score.getPlayerName() == null || score.getPlayerName().startsWith("#"));
        scores.sort(Comparator.comparingInt(ScoreboardPlayerScore::getScore).reversed()
                .thenComparing(ScoreboardPlayerScore::getPlayerName, String.CASE_INSENSITIVE_ORDER));

        int x = 8;
        int y = 8;

        context.fill(x - 4, y - 4, x + 164, y + 16 + (MAX_ENTRIES * 10), 0x88000000);
        context.drawText(client.textRenderer, Text.translatable("hud.gardenkingmod.leaderboard_title"), x, y, 0xFFD85A,
                true);
        y += 12;

        int limit = Math.min(MAX_ENTRIES, scores.size());
        for (int index = 0; index < limit; index++) {
            ScoreboardPlayerScore score = scores.get(index);
            Text line = Text.translatable("hud.gardenkingmod.leaderboard_entry", index + 1, score.getPlayerName(),
                    score.getScore());
            context.drawText(client.textRenderer, line, x, y, 0xFFFFFF, false);
            y += 10;
        }

        if (scores.isEmpty()) {
            context.drawText(client.textRenderer, Text.translatable("hud.gardenkingmod.leaderboard_empty"), x, y,
                    0xAAAAAA, false);
        }
    }
}
