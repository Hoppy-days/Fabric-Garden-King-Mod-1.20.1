package net.jeremy.gardenkingmod.client.gui.toast;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

public class SaleResultToast implements Toast {
        private static final long DISPLAY_DURATION_MS = 5000L;
        private static final int WIDTH = 160;
        private static final int HEIGHT = 32;

        private final Text primaryLine;
        private final Text secondaryLine;

        public SaleResultToast(Text primaryLine, Text secondaryLine) {
                this.primaryLine = primaryLine == null ? Text.empty() : primaryLine;
                this.secondaryLine = secondaryLine == null ? Text.empty() : secondaryLine;
        }

        @Override
        public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
                context.drawTexture(TEXTURE, 0, 0, 0, 0, WIDTH, HEIGHT);

                TextRenderer textRenderer = manager.getClient().textRenderer;
                if (!primaryLine.getString().isEmpty()) {
                        context.drawTextWithShadow(textRenderer, primaryLine, 30, 7, 0xFFFFFF);
                }
                if (!secondaryLine.getString().isEmpty()) {
                        context.drawTextWithShadow(textRenderer, secondaryLine, 30, 18, 0xFFFFFF);
                }

                return startTime >= DISPLAY_DURATION_MS ? Visibility.HIDE : Visibility.SHOW;
        }
}
