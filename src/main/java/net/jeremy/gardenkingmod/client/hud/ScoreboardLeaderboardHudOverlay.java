package net.jeremy.gardenkingmod.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jeremy.gardenkingmod.ModScoreboards;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ScoreboardLeaderboardHudOverlay implements HudRenderCallback {
    private static final int MAX_VISIBLE_ENTRIES = 8;
    private static final int REFRESH_INTERVAL_TICKS = 20;

    // Toggle / texture constants
    private static final boolean DRAW_BACKGROUND_TEXTURE = true;
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("gardenkingmod",
            "textures/gui/hud/leaderboard.png");
    private static final int BACKGROUND_X = 4;
    private static final int BACKGROUND_Y = 4;
    private static final int BACKGROUND_U = 0;
    private static final int BACKGROUND_V = 0;
    private static final int BACKGROUND_WIDTH = 144;
    private static final int BACKGROUND_HEIGHT = 196;
    private static final int BACKGROUND_TEXTURE_WIDTH = 256;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;

    // Position constants
    private static final int ENTRIES_U = 10;
    private static final int ENTRIES_V = 34;
    private static final int ENTRIES_X = BACKGROUND_X + ENTRIES_U;
    private static final int ENTRIES_START_Y = BACKGROUND_Y + ENTRIES_V;
    private static final int ENTRY_LINE_HEIGHT = 10;

    // Text style constants
    private static final int ENTRY_COLOR = 0xE0E0E0;
    private static final int EMPTY_STATE_COLOR = 0xE0E0E0;
    private static final boolean ENTRY_SHADOW = false;
    private static final boolean EMPTY_STATE_SHADOW = false;

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
        if (shouldRefresh(currentTick)) {
            refreshTopEntries(client.world.getScoreboard());
            lastRefreshTick = currentTick;
        }

        if (DRAW_BACKGROUND_TEXTURE) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            context.drawTexture(BACKGROUND_TEXTURE, BACKGROUND_X, BACKGROUND_Y, BACKGROUND_U, BACKGROUND_V,
                    BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
            RenderSystem.disableBlend();
        }

        int lineY = ENTRIES_START_Y;
        if (cachedTopEntries.isEmpty()) {
            context.drawText(client.textRenderer, Text.literal("No leaderboard data yet"), ENTRIES_X, lineY,
                    EMPTY_STATE_COLOR, EMPTY_STATE_SHADOW);
            return;
        }

        for (int i = 0; i < cachedTopEntries.size(); i++) {
            LeaderboardEntry entry = cachedTopEntries.get(i);
            Text line = Text.literal((i + 1) + ". " + entry.name() + " - " + entry.score());
            context.drawText(client.textRenderer, line, ENTRIES_X, lineY, ENTRY_COLOR, ENTRY_SHADOW);
            lineY += ENTRY_LINE_HEIGHT;
        }
    }

    private boolean shouldRefresh(int currentTick) {
        if (lastRefreshTick == Integer.MIN_VALUE) {
            return true;
        }

        long ticksSinceLastRefresh = (long) currentTick - lastRefreshTick;
        return ticksSinceLastRefresh >= REFRESH_INTERVAL_TICKS || currentTick < lastRefreshTick;
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
        for (ScoreboardPlayerScore scoreEntry : scoreboard.getAllPlayerScores(objective)) {
            int score = scoreEntry.getScore();
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
