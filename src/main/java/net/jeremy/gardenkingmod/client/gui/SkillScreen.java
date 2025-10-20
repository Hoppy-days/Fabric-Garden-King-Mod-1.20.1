package net.jeremy.gardenkingmod.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Minimal skill overview screen that currently displays the provided
 * background. Future iterations can build on this base to add interactive
 * elements.
 */
public class SkillScreen extends Screen {
        private static final Identifier BACKGROUND_TEXTURE = new Identifier("gardenkingmod",
                        "textures/gui/skill_screen_gui.png");

        private static final int TITLE_COLOR = 0x404040;
        private static final int TITLE_X = 8;
        private static final int TITLE_Y = 6;

        private static final int BACKGROUND_WIDTH = 428;
        private static final int BACKGROUND_HEIGHT = 246;

        private static final int TEXTURE_WIDTH = 512;
        private static final int TEXTURE_HEIGHT = 512;

        private int backgroundX;
        private int backgroundY;

        public SkillScreen() {
                super(Text.translatable("screen.gardenkingmod.skills.title"));
        }

        @Override
        protected void init() {
                super.init();

                this.backgroundX = (this.width - BACKGROUND_WIDTH) / 2;
                this.backgroundY = (this.height - BACKGROUND_HEIGHT) / 2;
        }

        @Override
        public boolean shouldPause() {
                return false;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                context.drawTexture(BACKGROUND_TEXTURE, this.backgroundX, this.backgroundY, 0, 0, BACKGROUND_WIDTH,
                                BACKGROUND_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                drawTitle(context);
                super.render(context, mouseX, mouseY, delta);
        }

        private void drawTitle(DrawContext context) {
                if (this.textRenderer == null) {
                        return;
                }

                context.drawText(this.textRenderer, this.title, this.backgroundX + TITLE_X, this.backgroundY + TITLE_Y,
                                TITLE_COLOR, false);
        }

        @Override
        public void close() {
                if (this.client != null) {
                        this.client.setScreen(null);
                }
        }
}
