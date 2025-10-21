package net.jeremy.gardenkingmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.jeremy.gardenkingmod.client.skill.SkillState;
import net.jeremy.gardenkingmod.skill.SkillProgressManager;
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

        private static final int XP_BAR_TEXTURE_REGION_WIDTH = 81;
        private static final int XP_BAR_HEIGHT = 5;
        private static final int XP_BAR_TEXTURE_ATLAS_WIDTH = 128;
        private static final int XP_BAR_TEXTURE_ATLAS_HEIGHT = 128;
        private static final int XP_BAR_BACKGROUND_V = 0;
        private static final int XP_BAR_FILL_V = XP_BAR_HEIGHT;

        private static final int XP_BAR_DISPLAY_WIDTH = 240;
        private static final int XP_BAR_LEFT_CAP_WIDTH = 10;
        private static final int XP_BAR_RIGHT_CAP_WIDTH = 10;
        private static final int XP_BAR_REPEATABLE_WIDTH = XP_BAR_TEXTURE_REGION_WIDTH
                        - XP_BAR_LEFT_CAP_WIDTH - XP_BAR_RIGHT_CAP_WIDTH;

        private static final int HEADER_AREA_WIDTH = 258;
        private static final int HEADER_AREA_HEIGHT = 44;
        private static final int HEADER_AREA_OFFSET_X = 16;
        private static final int HEADER_AREA_OFFSET_Y = 19;
        private static final int HEADER_CONTENT_PADDING_LEFT = 0;
        private static final int HEADER_CONTENT_PADDING_RIGHT = 8;
        private static final int HEADER_CONTENT_PADDING_Y = 4;
        private static final int HEADER_LINE_SPACING = 1;
        private static final int HEADER_LABEL_VALUE_GAP = 4;
        private static final int HEADER_PROGRESS_BAR_VERTICAL_GAP = 2;
        private static final int HEADER_PROGRESS_BAR_Y_OFFSET = 2;

        private static final int UNSPENT_POINTS_X_OFFSET_FROM_TITLE = 100;
        private static final int UNSPENT_POINTS_Y_OFFSET = 0;

        private static final int LEVEL_LABEL_COLOR = 0x404040;
        private static final int LEVEL_VALUE_COLOR = 0x55FF55;
        private static final int TOTAL_XP_LABEL_COLOR = 0x404040;
        private static final int TOTAL_XP_VALUE_COLOR = 0xFFFF55;
        private static final int PROGRESS_LABEL_COLOR = 0x404040;
        private static final int PROGRESS_VALUE_COLOR = 0x55FFFF;
        private static final int UNSPENT_POINTS_LABEL_COLOR = 0x404040;
        private static final int UNSPENT_POINTS_VALUE_COLOR = 0x55FF55;

        private static final int FIRST_SKILL_AREA_OFFSET_X = 9;
        private static final int FIRST_SKILL_AREA_OFFSET_Y = 62;
        private static final int FIRST_SKILL_TITLE_BASE_X = FIRST_SKILL_AREA_OFFSET_X + 42;
        private static final int FIRST_SKILL_TITLE_BASE_Y = FIRST_SKILL_AREA_OFFSET_Y + 4;
        private static final int FIRST_SKILL_BAR_BASE_X = FIRST_SKILL_AREA_OFFSET_X + 42;
        private static final int FIRST_SKILL_BAR_BASE_Y = FIRST_SKILL_AREA_OFFSET_Y + 20;
        private static final int FIRST_SKILL_BAR_WIDTH = 208;
        private static final int FIRST_SKILL_LEVEL_BASE_X = FIRST_SKILL_AREA_OFFSET_X + 42;
        private static final int FIRST_SKILL_LEVEL_BASE_Y = FIRST_SKILL_AREA_OFFSET_Y + 27;
        private static final int FIRST_SKILL_MAX_LEVEL = 5;

        private static final int BACKGROUND_WIDTH = 428;
        private static final int BACKGROUND_HEIGHT = 246;

        private static final int TEXTURE_WIDTH = 512;
        private static final int TEXTURE_HEIGHT = 512;

        private int backgroundX;
        private int backgroundY;

        private final TextElementStyle chefSkillTitleStyle = new TextElementStyle(
                        FIRST_SKILL_TITLE_BASE_X, FIRST_SKILL_TITLE_BASE_Y, 0xFFFF55);
        private final BarElementStyle chefSkillBarStyle = new BarElementStyle(
                        FIRST_SKILL_BAR_BASE_X, FIRST_SKILL_BAR_BASE_Y, FIRST_SKILL_BAR_WIDTH);
        private final TextElementStyle chefSkillLevelStyle = new TextElementStyle(
                        FIRST_SKILL_LEVEL_BASE_X, FIRST_SKILL_LEVEL_BASE_Y, 0xFFFFFF);

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
                drawChefMasteryOverview(context, skillState);
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

                int contentX = headerX + HEADER_CONTENT_PADDING_LEFT;
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
                drawLabelAndValue(context, progressLabel, progressValue, contentX, progressY,
                                PROGRESS_LABEL_COLOR, PROGRESS_VALUE_COLOR);

                int availableContentWidth = HEADER_AREA_WIDTH - HEADER_CONTENT_PADDING_LEFT
                                - HEADER_CONTENT_PADDING_RIGHT;
                int xpBarWidth = Math.min(XP_BAR_DISPLAY_WIDTH, availableContentWidth);
                int xpBarX = contentX;
                int baseXpBarY = progressY + lineHeight + HEADER_PROGRESS_BAR_VERTICAL_GAP;
                int xpBarY = baseXpBarY + HEADER_PROGRESS_BAR_Y_OFFSET;

                int minXpBarY = contentY + Math.min(0, HEADER_PROGRESS_BAR_Y_OFFSET);
                int maxXpBarY = headerY + HEADER_AREA_HEIGHT - HEADER_CONTENT_PADDING_Y - XP_BAR_HEIGHT
                                + Math.max(0, HEADER_PROGRESS_BAR_Y_OFFSET);
                xpBarY = MathHelper.clamp(xpBarY, minXpBarY, Math.max(minXpBarY, maxXpBarY));

                drawXpBar(context, xpBarX, xpBarY, xpBarWidth, progress);

        }

        private void drawChefMasteryOverview(DrawContext context, SkillState skillState) {
                if (this.textRenderer == null) {
                        return;
                }

                int chefLevel = Math.max(0, skillState.getChefMasteryLevel());
                int maxChefLevel = Math.max(1, FIRST_SKILL_MAX_LEVEL);
                float chefProgress = MathHelper.clamp((float) chefLevel / (float) maxChefLevel, 0.0F, 1.0F);

                Text chefTitleText = getChefMasteryTitleText();
                int titleX = chefSkillTitleStyle.computeX(this.backgroundX);
                int titleY = chefSkillTitleStyle.computeY(this.backgroundY);
                context.drawText(this.textRenderer, chefTitleText, titleX, titleY, chefSkillTitleStyle.getColor(), false);

                int barX = chefSkillBarStyle.computeX(this.backgroundX);
                int barY = chefSkillBarStyle.computeY(this.backgroundY);
                int barWidth = chefSkillBarStyle.getWidth();
                if (barWidth > 0) {
                        RenderSystem.setShaderColor(chefSkillBarStyle.getRed(), chefSkillBarStyle.getGreen(),
                                        chefSkillBarStyle.getBlue(), chefSkillBarStyle.getAlpha());
                        drawXpBar(context, barX, barY, barWidth, chefProgress);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                }

                String chefLevelValue = MathHelper.clamp(chefLevel, 0, maxChefLevel) + "/" + maxChefLevel;
                Text chefLevelText = Text.literal(chefLevelValue);
                int levelX = chefSkillLevelStyle.computeX(this.backgroundX);
                int levelY = chefSkillLevelStyle.computeY(this.backgroundY);
                context.drawText(this.textRenderer, chefLevelText, levelX, levelY, chefSkillLevelStyle.getColor(), false);
        }

        private void drawXpBar(DrawContext context, int x, int y, int width, float progress) {
                if (width <= 0) {
                        return;
                }

                drawXpBarStrip(context, x, y, width, XP_BAR_BACKGROUND_V);

                int filledWidth = MathHelper.clamp(MathHelper.ceil(progress * width), 0, width);
                if (filledWidth > 0) {
                        drawXpBarStrip(context, x, y, filledWidth, XP_BAR_FILL_V);
                }
        }

        private void drawXpBarStrip(DrawContext context, int x, int y, int totalWidth, int textureV) {
                if (totalWidth <= 0) {
                        return;
                }

                int drawX = x;
                int remaining = totalWidth;

                int leftWidth = Math.min(XP_BAR_LEFT_CAP_WIDTH, remaining);
                if (leftWidth > 0) {
                        drawXpBarTexture(context, drawX, y, 0, textureV, leftWidth);
                        drawX += leftWidth;
                        remaining -= leftWidth;
                }

                if (remaining <= 0) {
                        return;
                }

                int rightWidth = 0;
                if (totalWidth > XP_BAR_LEFT_CAP_WIDTH) {
                        rightWidth = Math.min(XP_BAR_RIGHT_CAP_WIDTH, remaining);
                }

                int middleTarget = remaining - rightWidth;
                if (middleTarget > 0) {
                        if (XP_BAR_REPEATABLE_WIDTH > 0) {
                                drawTiledXpBarTexture(context, drawX, y, XP_BAR_LEFT_CAP_WIDTH, textureV, middleTarget);
                        } else {
                                drawXpBarTexture(context, drawX, y, XP_BAR_LEFT_CAP_WIDTH, textureV, middleTarget);
                        }
                        drawX += middleTarget;
                        remaining -= middleTarget;
                }

                if (remaining > 0 && rightWidth > 0) {
                        drawXpBarTexture(context, drawX, y,
                                        XP_BAR_TEXTURE_REGION_WIDTH - XP_BAR_RIGHT_CAP_WIDTH, textureV, remaining);
                }
        }

        private void drawTiledXpBarTexture(DrawContext context, int x, int y, int u, int v, int targetWidth) {
                if (targetWidth <= 0) {
                        return;
                }

                int drawX = x;
                int remaining = targetWidth;
                while (remaining > 0) {
                        int drawWidth = Math.min(XP_BAR_REPEATABLE_WIDTH, remaining);
                        drawXpBarTexture(context, drawX, y, u, v, drawWidth);
                        drawX += drawWidth;
                        remaining -= drawWidth;
                }
        }

        private void drawXpBarTexture(DrawContext context, int x, int y, int u, int v, int width) {
                if (width <= 0) {
                        return;
                }

                context.drawTexture(XP_BAR_TEXTURE, x, y, u, v, width, XP_BAR_HEIGHT, XP_BAR_TEXTURE_ATLAS_WIDTH,
                                XP_BAR_TEXTURE_ATLAS_HEIGHT);
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

        private Text getChefMasteryTitleText() {
                SkillProgressManager.SkillDefinition definition = SkillProgressManager.getSkillDefinitions()
                                .get(SkillProgressManager.CHEF_SKILL);
                if (definition != null) {
                        String displayName = definition.displayName();
                        if (displayName != null && !displayName.isBlank()) {
                                return Text.literal(displayName);
                        }
                }
                return Text.literal("Chef Mastery");
        }

        public void setChefSkillTitleOffset(int offsetX, int offsetY) {
                chefSkillTitleStyle.setOffset(offsetX, offsetY);
        }

        public void setChefSkillTitleColor(int color) {
                chefSkillTitleStyle.setColor(color);
        }

        public void setChefSkillBarOffset(int offsetX, int offsetY) {
                chefSkillBarStyle.setOffset(offsetX, offsetY);
        }

        public void setChefSkillBarColor(float red, float green, float blue, float alpha) {
                chefSkillBarStyle.setColor(red, green, blue, alpha);
        }

        public void setChefSkillBarColor(int rgb) {
                chefSkillBarStyle.setColor(rgb);
        }

        public void setChefSkillLevelOffset(int offsetX, int offsetY) {
                chefSkillLevelStyle.setOffset(offsetX, offsetY);
        }

        public void setChefSkillLevelColor(int color) {
                chefSkillLevelStyle.setColor(color);
        }

        @Override
        public void close() {
                if (this.client != null) {
                        this.client.setScreen(null);
                }
        }

        private static final class TextElementStyle {
                private final int baseX;
                private final int baseY;
                private int offsetX;
                private int offsetY;
                private int color;

                private TextElementStyle(int baseX, int baseY, int defaultColor) {
                        this.baseX = baseX;
                        this.baseY = baseY;
                        this.color = defaultColor;
                }

                private int computeX(int originX) {
                        return originX + this.baseX + this.offsetX;
                }

                private int computeY(int originY) {
                        return originY + this.baseY + this.offsetY;
                }

                private int getColor() {
                        return this.color;
                }

                private void setOffset(int offsetX, int offsetY) {
                        this.offsetX = offsetX;
                        this.offsetY = offsetY;
                }

                private void setColor(int color) {
                        this.color = color;
                }
        }

        private static final class BarElementStyle {
                private final int baseX;
                private final int baseY;
                private final int width;
                private int offsetX;
                private int offsetY;
                private float red = 1.0F;
                private float green = 1.0F;
                private float blue = 1.0F;
                private float alpha = 1.0F;

                private BarElementStyle(int baseX, int baseY, int width) {
                        this.baseX = baseX;
                        this.baseY = baseY;
                        this.width = width;
                }

                private int computeX(int originX) {
                        return originX + this.baseX + this.offsetX;
                }

                private int computeY(int originY) {
                        return originY + this.baseY + this.offsetY;
                }

                private int getWidth() {
                        return this.width;
                }

                private float getRed() {
                        return this.red;
                }

                private float getGreen() {
                        return this.green;
                }

                private float getBlue() {
                        return this.blue;
                }

                private float getAlpha() {
                        return this.alpha;
                }

                private void setOffset(int offsetX, int offsetY) {
                        this.offsetX = offsetX;
                        this.offsetY = offsetY;
                }

                private void setColor(float red, float green, float blue, float alpha) {
                        this.red = MathHelper.clamp(red, 0.0F, 1.0F);
                        this.green = MathHelper.clamp(green, 0.0F, 1.0F);
                        this.blue = MathHelper.clamp(blue, 0.0F, 1.0F);
                        this.alpha = MathHelper.clamp(alpha, 0.0F, 1.0F);
                }

                private void setColor(int rgb) {
                        float red = ((rgb >> 16) & 0xFF) / 255.0F;
                        float green = ((rgb >> 8) & 0xFF) / 255.0F;
                        float blue = (rgb & 0xFF) / 255.0F;
                        setColor(red, green, blue, 1.0F);
                }
        }
}
