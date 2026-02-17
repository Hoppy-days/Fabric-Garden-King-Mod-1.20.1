package net.jeremy.gardenkingmod.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jeremy.gardenkingmod.ModScoreboards;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

public final class ScoreboardLeaderboardHudOverlay implements HudRenderCallback {
    private static final int MAX_VISIBLE_ENTRIES = 8;
    private static final int REFRESH_INTERVAL_TICKS = 20;

    public static final ScoreboardLeaderboardHudOverlay INSTANCE = new ScoreboardLeaderboardHudOverlay();

    private final List<LeaderboardEntry> cachedTopEntries = new ArrayList<>();
    private int lastRefreshTick = Integer.MIN_VALUE;
    private boolean visible;

    private ScoreboardLeaderboardHudOverlay() {
        visible = false;
    }

    public boolean toggleVisible() {
        visible = !visible;
        return visible;
    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!visible || client == null || client.options.hudHidden || client.player == null || client.world == null) {
            return;
        }

        int currentTick = client.player.age;
        if (currentTick - lastRefreshTick >= REFRESH_INTERVAL_TICKS || currentTick < lastRefreshTick) {
            refreshTopEntries(client.world.getScoreboard());
            lastRefreshTick = currentTick;
        }

        if (cachedTopEntries.isEmpty()) {
            return;
        }

        int x = 8;
        int y = 8;
        context.drawText(client.textRenderer, Text.literal("Garden Dollars Leaderboard"), x, y, 0xFFFFFF, true);

        int lineY = y + 12;
        for (int i = 0; i < cachedTopEntries.size(); i++) {
            LeaderboardEntry entry = cachedTopEntries.get(i);
            Text line = Text.literal((i + 1) + ". " + entry.name() + " - " + entry.score());
            context.drawText(client.textRenderer, line, x, lineY, 0xE0E0E0, false);
            lineY += 10;
        }
    }

    private void refreshTopEntries(Scoreboard scoreboard) {
        ScoreboardObjective objective = scoreboard.getObjective(ModScoreboards.CURRENCY_OBJECTIVE);
        cachedTopEntries.clear();
        if (objective == null) {
            return;
        }

        Comparator<LeaderboardEntry> byLowestScoreThenReverseName = Comparator
                .comparingInt(LeaderboardEntry::score)
                .thenComparing(LeaderboardEntry::name, Comparator.reverseOrder());

        PriorityQueue<LeaderboardEntry> topEntries = new PriorityQueue<>(MAX_VISIBLE_ENTRIES, byLowestScoreThenReverseName);
        for (ReadableScoreboardScore scoreEntry : scoreboard.getAllPlayerScores(objective)) {
            int score = scoreEntry.getScore();
            if (score <= 0) {
                continue;
            }

            LeaderboardEntry candidate = new LeaderboardEntry(scoreEntry.getPlayerName(), score);
            if (topEntries.size() < MAX_VISIBLE_ENTRIES) {
                topEntries.offer(candidate);
                continue;
            }

            LeaderboardEntry lowestTopEntry = topEntries.peek();
            if (lowestTopEntry != null && byLowestScoreThenReverseName.compare(candidate, lowestTopEntry) > 0) {
                topEntries.poll();
                topEntries.offer(candidate);
            }
        }

        cachedTopEntries.addAll(topEntries);
        cachedTopEntries.sort(Comparator
                .comparingInt(LeaderboardEntry::score)
                .reversed()
                .thenComparing(LeaderboardEntry::name));
    }

    private record LeaderboardEntry(String name, int score) {
    }
}
