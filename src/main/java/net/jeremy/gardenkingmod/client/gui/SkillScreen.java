package net.jeremy.gardenkingmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.jeremy.gardenkingmod.client.skill.SkillState;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.jeremy.gardenkingmod.skill.SkillProgressManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
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
        private static final Identifier SUB_SKILL_XP_BAR_TEXTURE = new Identifier("gardenkingmod",
                        "textures/gui/sub_skill_xp_bar.png");

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
        private static final int HEADER_AREA_HEIGHT = 46;
        private static final int HEADER_AREA_OFFSET_X = 16;
        private static final int HEADER_AREA_OFFSET_Y = 19;
        private static final int HEADER_CONTENT_PADDING_LEFT = 0;
        private static final int HEADER_CONTENT_PADDING_RIGHT = 8;
        private static final int HEADER_CONTENT_PADDING_Y = 4;
        private static final int HEADER_LINE_SPACING = 1;
        private static final int HEADER_LABEL_VALUE_GAP = 4;
        private static final int HEADER_PROGRESS_BAR_VERTICAL_GAP = 2;
        private static final int HEADER_PROGRESS_BAR_Y_OFFSET = 1;

        private static final int UNSPENT_POINTS_X_OFFSET_FROM_TITLE = 140;
        private static final int UNSPENT_POINTS_Y_OFFSET = 0;

        private static final int LEVEL_LABEL_COLOR = 0x404040;
        private static final int LEVEL_VALUE_COLOR = 0x55FF55;
        private static final int TOTAL_XP_LABEL_COLOR = 0x404040;
        private static final int TOTAL_XP_VALUE_COLOR = 0xFFFF55;
        private static final int PROGRESS_LABEL_COLOR = 0x404040;
        private static final int PROGRESS_VALUE_COLOR = 0x55FFFF;
        private static final int UNSPENT_POINTS_LABEL_COLOR = 0x404040;
        private static final int UNSPENT_POINTS_VALUE_COLOR = 0xFFFF55;

        private static final int SKILL_LIST_OFFSET_X = 10;
        private static final int SKILL_LIST_OFFSET_Y = 65;
        private static final int SKILL_LIST_WIDTH = 256;
        private static final int SKILL_LIST_HEIGHT = 171;
        private static final int SKILL_SECTION_WIDTH = 256;
        private static final int SKILL_SECTION_HEIGHT = 44;
        private static final int SKILL_SECTION_TEXTURE_U = 0;
        private static final int SKILL_SECTION_TEXTURE_V = 248;
        private static final int SKILL_SECTION_HOVER_V = 290;
        private static final int SKILL_SECTION_TEXTURE_WIDTH = 251;
        private static final int SKILL_SECTION_TEXTURE_HEIGHT = 44;
        private static final int SKILL_SECTION_SPACING = 44;

        private static final int SKILL_TITLE_BASE_X = SKILL_LIST_OFFSET_X + 42;
        private static final int SKILL_TITLE_BASE_Y = SKILL_LIST_OFFSET_Y + 4;
        private static final int SKILL_BAR_BASE_X = SKILL_LIST_OFFSET_X + 42;
        private static final int SKILL_BAR_BASE_Y = SKILL_LIST_OFFSET_Y + 20;
        private static final int SKILL_BAR_WIDTH = 204;
        private static final int SKILL_LEVEL_BASE_X = SKILL_LIST_OFFSET_X + 42;
        private static final int SKILL_LEVEL_BASE_Y = SKILL_LIST_OFFSET_Y + 30;
        private static final int SKILL_LEVEL_LABEL_VALUE_GAP = 4;

        private static final int DESCRIPTION_AREA_OFFSET_X = 290;
        private static final int DESCRIPTION_AREA_OFFSET_Y = 19;
        private static final int DESCRIPTION_AREA_WIDTH = 127;
        private static final int DESCRIPTION_AREA_HEIGHT = 170;

        private static final int UPGRADE_BUTTON_OFFSET_X = 321;
        private static final int UPGRADE_BUTTON_OFFSET_Y = 213;
        private static final int UPGRADE_BUTTON_WIDTH = 70;
        private static final int UPGRADE_BUTTON_HEIGHT = 18;
        private static final int UPGRADE_BUTTON_TEXTURE_U = 430;
        private static final int UPGRADE_BUTTON_TEXTURE_V = 0;
        private static final int UPGRADE_BUTTON_HOVER_V = 18;

        private static final int SCROLLBAR_OFFSET_X = 262;
        private static final int SCROLLBAR_OFFSET_Y = 18;
        private static final int SCROLLBAR_WIDTH = 6;
        private static final int SCROLLBAR_HEIGHT = 219;
        private static final int SCROLLBAR_KNOB_TEXTURE_U = 261;
        private static final int SCROLLBAR_KNOB_TEXTURE_V = 247;
        private static final int SCROLLBAR_KNOB_WIDTH = 6;
        private static final int SCROLLBAR_KNOB_HEIGHT = 27;
        private static final int SCROLL_WHEEL_STEP = 10;

        private static final int BACKGROUND_WIDTH = 428;
        private static final int BACKGROUND_HEIGHT = 246;

        private static final int TEXTURE_WIDTH = 512;
        private static final int TEXTURE_HEIGHT = 512;

        private int backgroundX;
        private int backgroundY;

        private final TextElementStyle chefSkillTitleStyle = new TextElementStyle(
                        SKILL_TITLE_BASE_X, SKILL_TITLE_BASE_Y, 0xFFFFFF);
        private final BarElementStyle chefSkillBarStyle = new BarElementStyle(
                        SKILL_BAR_BASE_X, SKILL_BAR_BASE_Y, SKILL_BAR_WIDTH);
        private final TextElementStyle chefSkillLevelLabelStyle = new TextElementStyle(
                        SKILL_LEVEL_BASE_X, SKILL_LEVEL_BASE_Y, 0xFFFFAA);
        private final TextElementStyle chefSkillLevelValueStyle = new TextElementStyle(
                        SKILL_LEVEL_BASE_X, SKILL_LEVEL_BASE_Y, 0xFFFFFF);

        private final List<SkillEntry> skillEntries = new ArrayList<>();
        private Identifier selectedSkillId;
        private double scrollOffset;
        private boolean scrolling;
        private List<OrderedText> descriptionLines = Collections.emptyList();

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

                drawContents(context, mouseX, mouseY);
                super.render(context, mouseX, mouseY, delta);
        }

        private void drawContents(DrawContext context, int mouseX, int mouseY) {
                if (this.textRenderer == null) {
                        return;
                }

                SkillState skillState = SkillState.getInstance();
                updateSkillEntries(skillState);

                int titleX = this.backgroundX + TITLE_X;
                int titleY = this.backgroundY + TITLE_Y;
                context.drawText(this.textRenderer, this.title, titleX, titleY, TITLE_COLOR, false);

                drawUnspentPoints(context, skillState, titleX, titleY);
                drawHeader(context, skillState);
                drawSkillSections(context, skillState, mouseX, mouseY);
                drawDescription(context);
                drawUpgradeButton(context, skillState, mouseX, mouseY);
                drawScrollbar(context);
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

                drawXpBar(context, XP_BAR_TEXTURE, xpBarX, xpBarY, xpBarWidth, progress);

        }

        private void drawXpBar(DrawContext context, Identifier texture, int x, int y, int width, float progress) {
                if (width <= 0) {
                        return;
                }

                drawXpBarStrip(context, texture, x, y, width, XP_BAR_BACKGROUND_V);

                int filledWidth = MathHelper.clamp(MathHelper.ceil(progress * width), 0, width);
                if (filledWidth > 0) {
                        drawXpBarStrip(context, texture, x, y, filledWidth, XP_BAR_FILL_V);
                }
        }

        private void drawXpBar(DrawContext context, int x, int y, int width, float progress) {
                drawXpBar(context, XP_BAR_TEXTURE, x, y, width, progress);
        }

        private void drawXpBarStrip(DrawContext context, Identifier texture, int x, int y, int totalWidth,
                        int textureV) {
                if (totalWidth <= 0) {
                        return;
                }

                int drawX = x;
                int remaining = totalWidth;

                int leftWidth = Math.min(XP_BAR_LEFT_CAP_WIDTH, remaining);
                if (leftWidth > 0) {
                        drawXpBarTexture(context, texture, drawX, y, 0, textureV, leftWidth);
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
                                drawTiledXpBarTexture(context, texture, drawX, y, XP_BAR_LEFT_CAP_WIDTH, textureV,
                                                middleTarget);
                        } else {
                                drawXpBarTexture(context, texture, drawX, y, XP_BAR_LEFT_CAP_WIDTH, textureV,
                                                middleTarget);
                        }
                        drawX += middleTarget;
                        remaining -= middleTarget;
                }

                if (remaining > 0 && rightWidth > 0) {
                        drawXpBarTexture(context, texture, drawX, y,
                                        XP_BAR_TEXTURE_REGION_WIDTH - XP_BAR_RIGHT_CAP_WIDTH, textureV, remaining);
                }
        }

        private void drawTiledXpBarTexture(DrawContext context, Identifier texture, int x, int y, int u, int v,
                        int targetWidth) {
                if (targetWidth <= 0) {
                        return;
                }

                int drawX = x;
                int remaining = targetWidth;
                while (remaining > 0) {
                        int drawWidth = Math.min(XP_BAR_REPEATABLE_WIDTH, remaining);
                        drawXpBarTexture(context, texture, drawX, y, u, v, drawWidth);
                        drawX += drawWidth;
                        remaining -= drawWidth;
                }
        }

        private void drawXpBarTexture(DrawContext context, Identifier texture, int x, int y, int u, int v, int width) {
                if (width <= 0) {
                        return;
                }

                context.drawTexture(texture, x, y, u, v, width, XP_BAR_HEIGHT, XP_BAR_TEXTURE_ATLAS_WIDTH,
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

        private void drawSkillSections(DrawContext context, SkillState skillState, int mouseX, int mouseY) {
                if (this.textRenderer == null) {
                        return;
                }

                int listX = this.backgroundX + SKILL_LIST_OFFSET_X;
                int listY = this.backgroundY + SKILL_LIST_OFFSET_Y;
                int listBottom = listY + SKILL_LIST_HEIGHT;

                for (int index = 0; index < this.skillEntries.size(); index++) {
                        SkillEntry entry = this.skillEntries.get(index);
                        int entryTop = listY + getEntryOffset(index);
                        int entryBottom = entryTop + SKILL_SECTION_HEIGHT;

                        if (entryBottom < listY || entryTop > listBottom) {
                                continue;
                        }

                        boolean hovered = mouseX >= listX && mouseX <= listX + SKILL_SECTION_WIDTH && mouseY >= entryTop
                                        && mouseY <= entryBottom;

                        int textureV = hovered || entry.id.equals(this.selectedSkillId) ? SKILL_SECTION_HOVER_V
                                        : SKILL_SECTION_TEXTURE_V;
                        context.drawTexture(BACKGROUND_TEXTURE, listX, entryTop, SKILL_SECTION_TEXTURE_U, textureV,
                                        SKILL_SECTION_TEXTURE_WIDTH, SKILL_SECTION_TEXTURE_HEIGHT, TEXTURE_WIDTH,
                                        TEXTURE_HEIGHT);

                        drawSkillEntryContents(context, entry, index);
                }

        }

        private void drawSkillEntryContents(DrawContext context, SkillEntry entry, int index) {
                if (this.textRenderer == null) {
                        return;
                }

                int originY = this.backgroundY + getEntryOffset(index);

                chefSkillTitleStyle.draw(context, this.textRenderer, entry.displayName, this.backgroundX, originY);

                int barX = chefSkillBarStyle.computeX(this.backgroundX);
                int barY = chefSkillBarStyle.computeY(originY);
                int barWidth = chefSkillBarStyle.getWidth();
                if (barWidth > 0) {
                        RenderSystem.setShaderColor(chefSkillBarStyle.getRed(), chefSkillBarStyle.getGreen(),
                                        chefSkillBarStyle.getBlue(), chefSkillBarStyle.getAlpha());
                        drawXpBar(context, SUB_SKILL_XP_BAR_TEXTURE, barX, barY, barWidth, entry.progress);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                }

                chefSkillLevelLabelStyle.draw(context, this.textRenderer, entry.levelLabel, this.backgroundX, originY);

                int valueX = chefSkillLevelValueStyle.computeX(this.backgroundX)
                                + chefSkillLevelLabelStyle.getScaledTextWidth(this.textRenderer, entry.levelLabel)
                                + SKILL_LEVEL_LABEL_VALUE_GAP;
                int valueY = chefSkillLevelValueStyle.computeY(originY);
                chefSkillLevelValueStyle.drawAt(context, this.textRenderer, entry.levelValue, valueX, valueY);
        }

        private void drawDescription(DrawContext context) {
                if (this.textRenderer == null) {
                        return;
                }

                int descriptionX = this.backgroundX + DESCRIPTION_AREA_OFFSET_X;
                int descriptionY = this.backgroundY + DESCRIPTION_AREA_OFFSET_Y;

                int lineY = descriptionY;
                for (OrderedText line : this.descriptionLines) {
                        context.drawText(this.textRenderer, line, descriptionX, lineY, 0x404040, false);
                        lineY += this.textRenderer.fontHeight + 1;
                        if (lineY > descriptionY + DESCRIPTION_AREA_HEIGHT) {
                                break;
                        }
                }
        }

        private void drawUpgradeButton(DrawContext context, SkillState skillState, int mouseX, int mouseY) {
                if (this.textRenderer == null) {
                        return;
                }

                boolean buttonActive = this.selectedSkillId != null && skillState.getUnspentSkillPoints() > 0;
                boolean hovered = buttonActive && isUpgradeButtonHovered(mouseX, mouseY, skillState);

                int buttonX = this.backgroundX + UPGRADE_BUTTON_OFFSET_X;
                int buttonY = this.backgroundY + UPGRADE_BUTTON_OFFSET_Y;

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, buttonActive ? 1.0F : 0.5F);
                context.drawTexture(BACKGROUND_TEXTURE, buttonX, buttonY, UPGRADE_BUTTON_TEXTURE_U,
                                hovered ? UPGRADE_BUTTON_HOVER_V : UPGRADE_BUTTON_TEXTURE_V, UPGRADE_BUTTON_WIDTH,
                                UPGRADE_BUTTON_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                Text buttonText = Text.translatable("screen.gardenkingmod.skills.upgrade_button");
                int textWidth = this.textRenderer.getWidth(buttonText);
                int textX = buttonX + (UPGRADE_BUTTON_WIDTH - textWidth) / 2;
                int textY = buttonY + (UPGRADE_BUTTON_HEIGHT - this.textRenderer.fontHeight) / 2;
                context.drawText(this.textRenderer, buttonText, textX, textY, buttonActive ? 0xFFFFFF : 0xA0A0A0, false);
        }

        private void drawScrollbar(DrawContext context) {
                if (getMaxScroll() <= 0) {
                        return;
                }

                int scrollbarX = this.backgroundX + SCROLLBAR_OFFSET_X;
                int scrollbarY = this.backgroundY + SCROLLBAR_OFFSET_Y;
                int travel = SCROLLBAR_HEIGHT - SCROLLBAR_KNOB_HEIGHT;
                double scrollPercent = MathHelper.clamp(getScrollPercent(), 0.0D, 1.0D);
                int knobY = scrollbarY + MathHelper.floor(scrollPercent * travel);

                context.drawTexture(BACKGROUND_TEXTURE, scrollbarX, knobY, SCROLLBAR_KNOB_TEXTURE_U,
                                SCROLLBAR_KNOB_TEXTURE_V, SCROLLBAR_KNOB_WIDTH, SCROLLBAR_KNOB_HEIGHT, TEXTURE_WIDTH,
                                TEXTURE_HEIGHT);
        }

        private boolean isUpgradeButtonHovered(int mouseX, int mouseY, SkillState skillState) {
                int buttonX = this.backgroundX + UPGRADE_BUTTON_OFFSET_X;
                int buttonY = this.backgroundY + UPGRADE_BUTTON_OFFSET_Y;
                return mouseX >= buttonX && mouseX <= buttonX + UPGRADE_BUTTON_WIDTH && mouseY >= buttonY
                                && mouseY <= buttonY + UPGRADE_BUTTON_HEIGHT;
        }

        private void updateSkillEntries(SkillState skillState) {
                Map<Identifier, SkillProgressManager.SkillDefinition> definitions = SkillProgressManager
                                .getSkillDefinitions();

                Identifier previousSelection = this.selectedSkillId;
                this.skillEntries.clear();
                int maxLevel = Math.max(1, SkillProgressManager.getMaxDefinedLevel());

                SkillEntry matchedSelection = null;

                for (SkillProgressManager.SkillDefinition definition : definitions.values()) {
                        Identifier id = definition.id();
                        int level = Math.max(0, skillState.getAllocation(id));
                        float progress = MathHelper.clamp((float) level / (float) maxLevel, 0.0F, 1.0F);
                        String displayNameText = definition.displayName();
                        if (displayNameText == null || displayNameText.isBlank()) {
                                displayNameText = "Unknown Skill";
                        }
                        Text displayName = Text.literal(displayNameText);
                        Text levelLabel = Text.translatable("screen.gardenkingmod.skills.chef.current_level_label");
                        Text levelValue = Text.literal(level + "/" + maxLevel);
                        String descriptionText = definition.description();
                        if (descriptionText == null || descriptionText.isBlank()) {
                                descriptionText = "Sample description text.";
                        }
                        Text description = Text.literal(descriptionText);
                        SkillEntry entry = new SkillEntry(id, displayName, levelLabel, levelValue, progress, description);
                        this.skillEntries.add(entry);
                        if (matchedSelection == null && id.equals(previousSelection)) {
                                matchedSelection = entry;
                        }
                }

                SkillEntry selected = matchedSelection;
                if (selected == null && !this.skillEntries.isEmpty()) {
                        selected = this.skillEntries.get(0);
                }

                if (selected != null) {
                        this.selectedSkillId = selected.id;
                        updateDescriptionLines(selected.description);
                } else {
                        this.descriptionLines = wrapDescriptionText(
                                        Text.translatable("screen.gardenkingmod.skills.description_placeholder"));
                }

                clampScrollOffset();
        }

        private SkillEntry getSelectedEntry() {
                if (this.selectedSkillId == null) {
                        return null;
                }
                for (SkillEntry entry : this.skillEntries) {
                        if (entry.id.equals(this.selectedSkillId)) {
                                return entry;
                        }
                }
                return null;
        }

        private int getEntryOffset(int index) {
                double rawOffset = index * (double) SKILL_SECTION_SPACING - this.scrollOffset;
                return MathHelper.floor(rawOffset);
        }

        private void updateDescriptionLines(Text description) {
                this.descriptionLines = wrapDescriptionText(description);
        }

        private List<OrderedText> wrapDescriptionText(Text description) {
                if (this.textRenderer == null) {
                        return Collections.emptyList();
                }
                return this.textRenderer.wrapLines(description, DESCRIPTION_AREA_WIDTH);
        }

        private void clampScrollOffset() {
                double maxScroll = getMaxScroll();
                if (maxScroll <= 0) {
                        this.scrollOffset = 0.0D;
                        return;
                }
                this.scrollOffset = MathHelper.clamp(this.scrollOffset, 0.0D, maxScroll);
        }

        private double getMaxScroll() {
                int contentHeight = Math.max(0, this.skillEntries.size() * SKILL_SECTION_HEIGHT);
                int extra = Math.max(0, contentHeight - SKILL_LIST_HEIGHT);
                return extra;
        }

        private double getScrollPercent() {
                double maxScroll = getMaxScroll();
                if (maxScroll <= 0) {
                        return 0.0D;
                }
                return MathHelper.clamp(this.scrollOffset / maxScroll, 0.0D, 1.0D);
        }

        private void setScrollPercent(double percent) {
                double maxScroll = getMaxScroll();
                if (maxScroll <= 0) {
                        this.scrollOffset = 0.0D;
                        return;
                }
                this.scrollOffset = MathHelper.clamp(percent, 0.0D, 1.0D) * maxScroll;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                        if (handleSkillListClick(mouseX, mouseY)) {
                                return true;
                        }

                        if (handleUpgradeButtonClick(mouseX, mouseY)) {
                                return true;
                        }

                        if (handleScrollbarClick(mouseX, mouseY)) {
                                return true;
                        }
                }

                return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (button == 0) {
                        this.scrolling = false;
                }
                return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                if (this.scrolling && button == 0) {
                        updateScrollFromMouse(mouseY);
                        return true;
                }
                return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
                if (getMaxScroll() > 0) {
                        this.scrollOffset = MathHelper.clamp(this.scrollOffset - amount * SCROLL_WHEEL_STEP, 0.0D,
                                        getMaxScroll());
                        return true;
                }
                return super.mouseScrolled(mouseX, mouseY, amount);
        }

        private boolean handleSkillListClick(double mouseX, double mouseY) {
                int listX = this.backgroundX + SKILL_LIST_OFFSET_X;
                int listY = this.backgroundY + SKILL_LIST_OFFSET_Y;
                if (mouseX < listX || mouseX > listX + SKILL_SECTION_WIDTH || mouseY < listY
                                || mouseY > listY + SKILL_LIST_HEIGHT) {
                        return false;
                }

                for (int index = 0; index < this.skillEntries.size(); index++) {
                        int entryTop = listY + getEntryOffset(index);
                        int entryBottom = entryTop + SKILL_SECTION_HEIGHT;
                        if (mouseY >= entryTop && mouseY <= entryBottom) {
                                SkillEntry entry = this.skillEntries.get(index);
                                if (!entry.id.equals(this.selectedSkillId)) {
                                        this.selectedSkillId = entry.id;
                                        updateDescriptionLines(entry.description);
                                }
                                playClickSound();
                                return true;
                        }
                }

                return false;
        }

        private boolean handleUpgradeButtonClick(double mouseX, double mouseY) {
                SkillEntry selected = getSelectedEntry();
                if (selected == null) {
                        return false;
                }

                SkillState skillState = SkillState.getInstance();
                if (skillState.getUnspentSkillPoints() <= 0) {
                        return false;
                }

                if (!isUpgradeButtonHovered((int) mouseX, (int) mouseY, skillState)) {
                        return false;
                }

                sendUpgradeRequest(selected.id);
                playClickSound();
                return true;
        }

        private boolean handleScrollbarClick(double mouseX, double mouseY) {
                if (getMaxScroll() <= 0) {
                        return false;
                }

                int scrollbarX = this.backgroundX + SCROLLBAR_OFFSET_X;
                int scrollbarY = this.backgroundY + SCROLLBAR_OFFSET_Y;
                if (mouseX < scrollbarX || mouseX > scrollbarX + SCROLLBAR_WIDTH || mouseY < scrollbarY
                                || mouseY > scrollbarY + SCROLLBAR_HEIGHT) {
                        return false;
                }

                this.scrolling = true;
                updateScrollFromMouse(mouseY);
                return true;
        }

        private void updateScrollFromMouse(double mouseY) {
                int scrollbarY = this.backgroundY + SCROLLBAR_OFFSET_Y;
                int travel = SCROLLBAR_HEIGHT - SCROLLBAR_KNOB_HEIGHT;
                if (travel <= 0) {
                        return;
                }

                double knobCenter = mouseY - scrollbarY - SCROLLBAR_KNOB_HEIGHT / 2.0D;
                knobCenter = MathHelper.clamp(knobCenter, 0.0D, travel);
                double percent = knobCenter / travel;
                setScrollPercent(percent);
        }

        private void sendUpgradeRequest(Identifier skillId) {
                if (this.client == null) {
                        return;
                }

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeIdentifier(skillId);
                buf.writeVarInt(1);
                ClientPlayNetworking.send(ModPackets.SKILL_SPEND_REQUEST, buf);
        }

        private void playClickSound() {
                if (this.client != null) {
                        this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
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
                chefSkillLevelLabelStyle.setOffset(offsetX, offsetY);
                chefSkillLevelValueStyle.setOffset(offsetX, offsetY);
        }

        public void setChefSkillLevelColor(int color) {
                chefSkillLevelValueStyle.setColor(color);
        }

        public void setChefSkillLevelLabelColor(int color) {
                chefSkillLevelLabelStyle.setColor(color);
        }

        public void setChefSkillTitleScale(float scale) {
                chefSkillTitleStyle.setScale(scale);
        }

        public void setChefSkillLevelLabelScale(float scale) {
                chefSkillLevelLabelStyle.setScale(scale);
        }

        public void setChefSkillLevelValueScale(float scale) {
                chefSkillLevelValueStyle.setScale(scale);
        }

        @Override
        public void close() {
                if (this.client != null) {
                        this.client.setScreen(null);
                }
        }

        private static final class SkillEntry {
                private final Identifier id;
                private final Text displayName;
                private final Text levelLabel;
                private final Text levelValue;
                private final float progress;
                private final Text description;

                private SkillEntry(Identifier id, Text displayName, Text levelLabel, Text levelValue, float progress,
                                Text description) {
                        this.id = id;
                        this.displayName = displayName;
                        this.levelLabel = levelLabel;
                        this.levelValue = levelValue;
                        this.progress = progress;
                        this.description = description;
                }
        }

        private static final class TextElementStyle {
                private final int baseX;
                private final int baseY;
                private int offsetX;
                private int offsetY;
                private int color;
                private float scale = 1.0F;

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

                private void setOffset(int offsetX, int offsetY) {
                        this.offsetX = offsetX;
                        this.offsetY = offsetY;
                }

                private void setColor(int color) {
                        this.color = color;
                }

                private void setScale(float scale) {
                        this.scale = MathHelper.clamp(scale, 0.0F, 8.0F);
                }

                private int getScaledTextWidth(TextRenderer renderer, Text text) {
                        return MathHelper.ceil(renderer.getWidth(text) * this.scale);
                }

                private void draw(DrawContext context, TextRenderer renderer, Text text, int originX, int originY) {
                        int drawX = computeX(originX);
                        int drawY = computeY(originY);
                        drawAt(context, renderer, text, drawX, drawY);
                }

                private void drawAt(DrawContext context, TextRenderer renderer, Text text, int drawX, int drawY) {
                        float appliedScale = this.scale;
                        MatrixStack matrices = context.getMatrices();
                        matrices.push();
                        if (appliedScale != 1.0F) {
                                matrices.scale(appliedScale, appliedScale, 1.0F);
                                drawX = MathHelper.floor(drawX / appliedScale);
                                drawY = MathHelper.floor(drawY / appliedScale);
                        }
                        context.drawText(renderer, text, drawX, drawY, this.color, false);
                        matrices.pop();
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
