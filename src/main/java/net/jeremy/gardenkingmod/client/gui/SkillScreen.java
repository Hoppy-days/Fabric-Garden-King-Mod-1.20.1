package net.jeremy.gardenkingmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.jeremy.gardenkingmod.client.skill.SkillState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Minimal skill overview screen that currently displays the provided
 * background. Future iterations can build on this base to add interactive
 * elements.
 */
public class SkillScreen extends Screen {
        private static final Identifier BACKGROUND_TEXTURE = new Identifier("gardenkingmod",
                        "textures/gui/skill_screen_gui.png");
        private static final Identifier XP_BAR_TEXTURE = new Identifier("gardenkingmod",
                        "textures/gui/skill_xp_bar.png");

        private static final int TITLE_COLOR = 0x404040;
        private static final int TITLE_X = 8;
        private static final int TITLE_Y = 6;

        private static final int XP_BAR_WIDTH = 81;
        private static final int XP_BAR_HEIGHT = 5;
        private static final int XP_BAR_TEXTURE_WIDTH = 128;
        private static final int XP_BAR_TEXTURE_HEIGHT = 128;
        private static final int XP_BAR_BACKGROUND_V = 0;
        private static final int XP_BAR_FILL_V = XP_BAR_HEIGHT;
        private static final int XP_BAR_X_OFFSET_FROM_TITLE = 30;
        private static final int XP_BAR_Y_OFFSET = 2;

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
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                context.drawTexture(BACKGROUND_TEXTURE, this.backgroundX, this.backgroundY, 0, 0, BACKGROUND_WIDTH,
                                BACKGROUND_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

                RenderSystem.disableBlend();

                drawTitle(context);
                super.render(context, mouseX, mouseY, delta);
        }

        private void drawTitle(DrawContext context) {
                if (this.textRenderer == null) {
                        return;
                }

                context.drawText(this.textRenderer, this.title, this.backgroundX + TITLE_X, this.backgroundY + TITLE_Y,
                                TITLE_COLOR, false);
                drawXpBar(context);
        }

        private void drawXpBar(DrawContext context) {
                SkillState skillState = SkillState.getInstance();
                float progress = MathHelper.clamp(skillState.getProgressPercentage(), 0.0f, 1.0f);

                int titleWidth = this.textRenderer.getWidth(this.title);
                int barX = this.backgroundX + TITLE_X + titleWidth + XP_BAR_X_OFFSET_FROM_TITLE;
                int barY = this.backgroundY + TITLE_Y + XP_BAR_Y_OFFSET;

                context.drawTexture(XP_BAR_TEXTURE, barX, barY, 0, XP_BAR_BACKGROUND_V, XP_BAR_WIDTH, XP_BAR_HEIGHT,
                                XP_BAR_TEXTURE_WIDTH, XP_BAR_TEXTURE_HEIGHT);

                int filledWidth = MathHelper.ceil(progress * XP_BAR_WIDTH);
                if (filledWidth > 0) {
                        context.drawTexture(XP_BAR_TEXTURE, barX, barY, 0, XP_BAR_FILL_V, filledWidth, XP_BAR_HEIGHT,
                                        XP_BAR_TEXTURE_WIDTH, XP_BAR_TEXTURE_HEIGHT);
                }
        }

        @Override
        public void close() {
                if (this.client != null) {
                        this.client.setScreen(null);
                }
        }
}
