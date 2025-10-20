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

        private static final int HEADER_AREA_WIDTH = 158;
        private static final int HEADER_AREA_HEIGHT = 42;
        private static final int HEADER_AREA_OFFSET_X = 60;
        private static final int HEADER_AREA_OFFSET_Y = 19;
        private static final int HEADER_CONTENT_PADDING_X = 8;
        private static final int HEADER_CONTENT_PADDING_Y = 4;
        private static final int HEADER_LINE_SPACING = 1;
        private static final int HEADER_LABEL_VALUE_GAP = 4;
        private static final int HEADER_PROGRESS_BAR_INLINE_GAP = 6;
        private static final int HEADER_PROGRESS_BAR_VERTICAL_GAP = 2;

        private static final int UNSPENT_POINTS_X_OFFSET_FROM_TITLE = 12;
        private static final int UNSPENT_POINTS_Y_OFFSET = 0;

        private static final int LEVEL_LABEL_COLOR = 0x404040;
        private static final int LEVEL_VALUE_COLOR = 0x55FF55;
        private static final int TOTAL_XP_LABEL_COLOR = 0x404040;
        private static final int TOTAL_XP_VALUE_COLOR = 0xFFFF55;
        private static final int PROGRESS_LABEL_COLOR = 0x404040;
        private static final int PROGRESS_VALUE_COLOR = 0x55FFFF;
        private static final int UNSPENT_POINTS_LABEL_COLOR = 0x404040;
        private static final int UNSPENT_POINTS_VALUE_COLOR = 0x55FF55;

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

                int titleX = this.backgroundX + TITLE_X;
                int titleY = this.backgroundY + TITLE_Y;
                context.drawText(this.textRenderer, this.title, titleX, titleY, TITLE_COLOR, false);

                SkillState skillState = SkillState.getInstance();
                drawUnspentPoints(context, skillState, titleX, titleY);
                drawHeader(context, skillState);
        }

        private void drawHeader(DrawContext context, SkillState skillState) {
                if (this.textRenderer == null) {
                        return;
                }
                float progress = MathHelper.clamp(skillState.getProgressPercentage(), 0.0f, 1.0f);
                int level = Math.max(0, skillState.getLevel());
                long totalExperience = Math.max(0L, skillState.getTotalExperience());
                long progressTowards = Math.max(0L, skillState.getExperienceTowardsNextLevel());
                long progressRequired = Math.max(0L, skillState.getExperienceRequiredForNextLevel());
                int headerX = this.backgroundX + HEADER_AREA_OFFSET_X;
                int headerY = this.backgroundY + HEADER_AREA_OFFSET_Y;

                int contentX = headerX + HEADER_CONTENT_PADDING_X;
                int contentY = headerY + HEADER_CONTENT_PADDING_Y;
                int lineHeight = this.textRenderer.fontHeight;

                Text levelLabel = Text.translatable("screen.gardenkingmod.skills.header.level_label");
                Text levelValue = Text.literal(Integer.toString(level));
                drawLabelAndValue(context, levelLabel, levelValue, contentX, contentY, LEVEL_LABEL_COLOR,
                                LEVEL_VALUE_COLOR);

                int totalXpY = contentY + lineHeight + HEADER_LINE_SPACING;
                Text totalXpLabel = Text.translatable("screen.gardenkingmod.skills.header.total_xp_label");
                Text totalXpValue = Text.literal(Long.toString(totalExperience));
                drawLabelAndValue(context, totalXpLabel, totalXpValue, contentX, totalXpY, TOTAL_XP_LABEL_COLOR,
                                TOTAL_XP_VALUE_COLOR);

                int progressY = totalXpY + lineHeight + HEADER_LINE_SPACING;
                Text progressLabel = Text.translatable("screen.gardenkingmod.skills.header.progress_label");
                String progressValueText = progressRequired <= 0L ? progressTowards + "/--"
                                : progressTowards + "/" + progressRequired;
                Text progressValue = Text.literal(progressValueText);
                int progressLineWidth = drawLabelAndValue(context, progressLabel, progressValue, contentX, progressY,
                                PROGRESS_LABEL_COLOR, PROGRESS_VALUE_COLOR);

                int availableContentWidth = HEADER_AREA_WIDTH - (2 * HEADER_CONTENT_PADDING_X);
                int xpBarX;
                int xpBarY;
                if (progressLineWidth + HEADER_PROGRESS_BAR_INLINE_GAP + XP_BAR_WIDTH <= availableContentWidth) {
                        xpBarX = contentX + progressLineWidth + HEADER_PROGRESS_BAR_INLINE_GAP;
                        xpBarY = progressY + Math.max(0, (lineHeight - XP_BAR_HEIGHT) / 2);
                } else {
                        xpBarX = contentX;
                        xpBarY = progressY + lineHeight + HEADER_PROGRESS_BAR_VERTICAL_GAP;
                }

                int minXpBarY = contentY;
                int maxXpBarY = headerY + HEADER_AREA_HEIGHT - HEADER_CONTENT_PADDING_Y - XP_BAR_HEIGHT;
                xpBarY = MathHelper.clamp(xpBarY, minXpBarY, Math.max(minXpBarY, maxXpBarY));

                context.drawTexture(XP_BAR_TEXTURE, xpBarX, xpBarY, 0, XP_BAR_BACKGROUND_V, XP_BAR_WIDTH,
                                XP_BAR_HEIGHT, XP_BAR_TEXTURE_WIDTH, XP_BAR_TEXTURE_HEIGHT);

                int filledWidth = MathHelper.ceil(progress * XP_BAR_WIDTH);
                if (filledWidth > 0) {
                        context.drawTexture(XP_BAR_TEXTURE, xpBarX, xpBarY, 0, XP_BAR_FILL_V, filledWidth,
                                        XP_BAR_HEIGHT, XP_BAR_TEXTURE_WIDTH, XP_BAR_TEXTURE_HEIGHT);
                }

        }

        private int drawLabelAndValue(DrawContext context, Text label, Text value, int x, int y, int labelColor,
                        int valueColor) {
                context.drawText(this.textRenderer, label, x, y, labelColor, false);
                int labelWidth = this.textRenderer.getWidth(label);
                int valueX = x + labelWidth + HEADER_LABEL_VALUE_GAP;
                context.drawText(this.textRenderer, value, valueX, y, valueColor, false);
                return labelWidth + HEADER_LABEL_VALUE_GAP + this.textRenderer.getWidth(value);
        }

        private void drawUnspentPoints(DrawContext context, SkillState skillState, int titleX, int titleY) {
                if (this.textRenderer == null) {
                        return;
                }

                int titleWidth = this.textRenderer.getWidth(this.title);
                int unspentLabelX = titleX + titleWidth + UNSPENT_POINTS_X_OFFSET_FROM_TITLE;
                int unspentLabelY = titleY + UNSPENT_POINTS_Y_OFFSET;

                Text unspentLabelText = Text.translatable("screen.gardenkingmod.skills.unspent_points_label");
                Text unspentValueText = Text.literal(Integer.toString(Math.max(0, skillState.getUnspentSkillPoints())));
                drawLabelAndValue(context, unspentLabelText, unspentValueText, unspentLabelX, unspentLabelY,
                                UNSPENT_POINTS_LABEL_COLOR, UNSPENT_POINTS_VALUE_COLOR);
        }

        @Override
        public void close() {
                if (this.client != null) {
                        this.client.setScreen(null);
                }
        }
}
