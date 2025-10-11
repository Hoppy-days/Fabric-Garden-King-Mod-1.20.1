package net.jeremy.gardenkingmod.client.gui.toast;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

public class SaleResultToast implements Toast {
        private static final long DISPLAY_DURATION_MS = 5000L;
        private static final int WIDTH = 160;
        private static final int HEIGHT = 32;
        private static final int TEXT_START_X = 30;
        private static final int TEXT_START_Y = 7;
        private static final int BOTTOM_MARGIN = 7;
        private static final int LINE_SPACING = 2;
        private static final int MAX_LINES = 3;
        private static final int BACKGROUND_TOP_SLICE_HEIGHT = 4;
        private static final int BACKGROUND_BOTTOM_SLICE_HEIGHT = 4;
        private static final int BACKGROUND_MIDDLE_SLICE_HEIGHT = HEIGHT - BACKGROUND_TOP_SLICE_HEIGHT - BACKGROUND_BOTTOM_SLICE_HEIGHT;

        private final List<OrderedText> wrappedPrimaryLines;
        private final List<OrderedText> wrappedSecondaryLines;
        private final int backgroundHeight;

        public SaleResultToast(Text primaryLine, Text secondaryLine) {
                Text sanitizedPrimary = primaryLine == null ? Text.empty() : primaryLine;
                Text sanitizedSecondary = secondaryLine == null ? Text.empty() : secondaryLine;

                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                if (textRenderer != null) {
                        this.wrappedPrimaryLines = wrapText(textRenderer, sanitizedPrimary);
                        this.wrappedSecondaryLines = wrapText(textRenderer, sanitizedSecondary);
                        this.backgroundHeight = calculateBackgroundHeight(textRenderer);
                } else {
                        this.wrappedPrimaryLines = Collections.emptyList();
                        this.wrappedSecondaryLines = Collections.emptyList();
                        this.backgroundHeight = HEIGHT;
                }
        }

        @Override
        public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
                drawBackground(context);

                TextRenderer textRenderer = manager.getClient().textRenderer;
                int currentY = TEXT_START_Y;
                int renderedLines = 0;

                int primaryRendered = drawWrappedLines(context, textRenderer, wrappedPrimaryLines, currentY, renderedLines);
                renderedLines += primaryRendered;
                currentY += (textRenderer.fontHeight + LINE_SPACING) * primaryRendered;

                if (renderedLines < MAX_LINES) {
                        // Provide a small gap between the primary and secondary sections when both are present.
                        if (!wrappedPrimaryLines.isEmpty() && !wrappedSecondaryLines.isEmpty()) {
                                currentY += LINE_SPACING;
                        }

                        int secondaryRendered = drawWrappedLines(context, textRenderer, wrappedSecondaryLines, currentY, renderedLines);
                        renderedLines += secondaryRendered;
                }

                return startTime >= DISPLAY_DURATION_MS ? Visibility.HIDE : Visibility.SHOW;
        }

        @Override
        public int getHeight() {
                return backgroundHeight;
        }

        private int drawWrappedLines(DrawContext context, TextRenderer textRenderer, List<OrderedText> lines, int startY, int alreadyRendered) {
                if (lines.isEmpty()) {
                        return 0;
                }

                int y = startY;
                int rendered = 0;
                for (OrderedText orderedText : lines) {
                        if (alreadyRendered + rendered >= MAX_LINES) {
                                break;
                        }

                        context.drawTextWithShadow(textRenderer, orderedText, TEXT_START_X, y, 0xFFFFFF);
                        y += textRenderer.fontHeight + LINE_SPACING;
                        rendered++;
                }

                return rendered;
        }

        private List<OrderedText> wrapText(TextRenderer textRenderer, Text text) {
                if (text == null || text.getString().isEmpty()) {
                        return Collections.emptyList();
                }

                int availableWidth = WIDTH - TEXT_START_X - 8;
                return textRenderer.wrapLines(text, availableWidth);
        }

        private int calculateBackgroundHeight(TextRenderer textRenderer) {
                int primaryLines = Math.min(MAX_LINES, wrappedPrimaryLines.size());
                int linesRemaining = MAX_LINES - primaryLines;
                int secondaryLines = Math.min(linesRemaining, wrappedSecondaryLines.size());
                if (primaryLines == 0 && secondaryLines == 0) {
                        return HEIGHT;
                }

                int totalLines = primaryLines + secondaryLines;
                int contentHeight = totalLines * textRenderer.fontHeight
                                + Math.max(0, totalLines - 1) * LINE_SPACING
                                + (primaryLines > 0 && secondaryLines > 0 ? LINE_SPACING : 0);
                int calculatedHeight = TEXT_START_Y + contentHeight + BOTTOM_MARGIN;
                return Math.max(HEIGHT, calculatedHeight);
        }

        private void drawBackground(DrawContext context) {
                context.drawTexture(TEXTURE, 0, 0, 0, 0, WIDTH, BACKGROUND_TOP_SLICE_HEIGHT);

                int remainingHeight = backgroundHeight - BACKGROUND_TOP_SLICE_HEIGHT - BACKGROUND_BOTTOM_SLICE_HEIGHT;
                int drawY = BACKGROUND_TOP_SLICE_HEIGHT;
                while (remainingHeight > 0) {
                        int drawHeight = Math.min(BACKGROUND_MIDDLE_SLICE_HEIGHT, remainingHeight);
                        context.drawTexture(TEXTURE, 0, drawY, 0, BACKGROUND_TOP_SLICE_HEIGHT, WIDTH, drawHeight);
                        drawY += drawHeight;
                        remainingHeight -= drawHeight;
                }

                context.drawTexture(TEXTURE, 0, drawY, 0, HEIGHT - BACKGROUND_BOTTOM_SLICE_HEIGHT, WIDTH, BACKGROUND_BOTTOM_SLICE_HEIGHT);
        }
}
