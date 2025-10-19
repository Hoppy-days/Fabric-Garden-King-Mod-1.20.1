package net.jeremy.gardenkingmod.client.gui;

import java.text.NumberFormat;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.jeremy.gardenkingmod.client.skill.SkillState;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.jeremy.gardenkingmod.skill.SkillProgressManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;

/**
 * Skill overview screen that presents the player's Garden King skill
 * progression and allows point allocation. Designed to scale with future skill
 * trees by using a scrollable list of nodes.
 */
public class SkillScreen extends Screen {
        private static final Identifier TEXTURE = new Identifier("gardenkingmod", "textures/gui/skill_screen_gui.png");
        private static final Identifier XP_BAR_TEXTURE = new Identifier("gardenkingmod", "textures/gui/skill_xp_bar.png");
        private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

        private static final class Layout {
                private static final int TEXTURE_WIDTH = 512;
                private static final int TEXTURE_HEIGHT = 512;

                private static final int BACKGROUND_WIDTH = 404;
                private static final int BACKGROUND_HEIGHT = 280;

                private static final int TITLE_X_OFFSET = 16;
                private static final int TITLE_Y_OFFSET = 14;

                private static final int SKILL_NAME_X_OFFSET = 32;
                private static final int SKILL_NAME_Y_OFFSET = 54;

                private static final int CURRENT_LEVEL_X_OFFSET = 36;
                private static final int CURRENT_LEVEL_Y_OFFSET = 86;

                private static final int NEXT_LEVEL_X_OFFSET = 36;
                private static final int NEXT_LEVEL_Y_OFFSET = 102;

                private static final int UPGRADE_BUTTON_X_OFFSET = 36;
                private static final int UPGRADE_BUTTON_Y_OFFSET = 134;
                private static final int UPGRADE_BUTTON_WIDTH = 90;
                private static final int UPGRADE_BUTTON_HEIGHT = 20;

                private static final int XP_BAR_X_OFFSET = 136;
                private static final int XP_BAR_Y_OFFSET = 24;
                private static final int XP_BAR_WIDTH = 81;
                private static final int XP_BAR_HEIGHT = 5;
                private static final int XP_BAR_TEXTURE_WIDTH = 128;
                private static final int XP_BAR_TEXTURE_HEIGHT = 128;
                private static final int XP_BAR_BACKGROUND_V = 0;
                private static final int XP_BAR_FILL_V = XP_BAR_HEIGHT;
                private static final int XP_TEXT_Y_OFFSET = XP_BAR_Y_OFFSET + XP_BAR_HEIGHT + 6;

                private static final int LEVEL_LABEL_PADDING = 8;

                private static final int SKILL_POINTS_TEXT_X_OFFSET = 220;
                private static final int SKILL_POINTS_TEXT_Y_OFFSET = 20;

                private static final int DESCRIPTION_TITLE_X_OFFSET = 284;
                private static final int DESCRIPTION_TITLE_Y_OFFSET = 36;

                private static final int DESCRIPTION_TEXT_X_OFFSET = DESCRIPTION_TITLE_X_OFFSET;
                private static final int DESCRIPTION_TEXT_Y_OFFSET = 60;
                private static final int DESCRIPTION_TEXT_WRAP_WIDTH = 108;
                private static final int DESCRIPTION_LINE_SPACING = 2;

                private Layout() {
                }
        }

        private static final class Colors {
                private static final int TITLE = 0xFFAA5500;
                private static final int HIGHLIGHT = 0xFFAA5500;
                private static final int PRIMARY_TEXT = 0xFFFFD200;
                private static final int SECONDARY_TEXT = 0xFFC0C0C0;

                private Colors() {
                }
        }

        private final SkillState skillState = SkillState.getInstance();

        private int backgroundX;
        private int backgroundY;

        @Nullable
        private ButtonWidget upgradeButton;

        public SkillScreen() {
                super(Text.translatable("screen.gardenkingmod.skills.title"));
        }

        @Override
        protected void init() {
                super.init();

                this.backgroundX = (this.width - Layout.BACKGROUND_WIDTH) / 2;
                this.backgroundY = (this.height - Layout.BACKGROUND_HEIGHT) / 2;

                int buttonX = this.backgroundX + Layout.UPGRADE_BUTTON_X_OFFSET;
                int buttonY = this.backgroundY + Layout.UPGRADE_BUTTON_Y_OFFSET;
                this.upgradeButton = ButtonWidget.builder(Text.literal("Upgrade"),
                                button -> this.allocatePoint(SkillProgressManager.CHEF_SKILL))
                                .dimensions(buttonX, buttonY, Layout.UPGRADE_BUTTON_WIDTH,
                                                Layout.UPGRADE_BUTTON_HEIGHT)
                                .build();
                this.addDrawableChild(this.upgradeButton);

                updateUpgradeButtonState();
        }

        @Override
        public void tick() {
                updateUpgradeButtonState();
        }

        @Override
        public boolean shouldPause() {
                return false;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                this.renderBackground(context);
                drawBackgroundLayer(context);
                super.render(context, mouseX, mouseY, delta);
                drawForegroundLayer(context);
        }

        private void drawBackgroundLayer(DrawContext context) {
                context.drawTexture(TEXTURE, this.backgroundX, this.backgroundY, 0, 0, Layout.BACKGROUND_WIDTH,
                                Layout.BACKGROUND_HEIGHT, Layout.TEXTURE_WIDTH, Layout.TEXTURE_HEIGHT);
        }

        private void drawForegroundLayer(DrawContext context) {
                context.drawText(this.textRenderer, Text.literal("Skills"),
                                this.backgroundX + Layout.TITLE_X_OFFSET,
                                this.backgroundY + Layout.TITLE_Y_OFFSET, Colors.TITLE, false);

                context.drawText(this.textRenderer, Text.literal("Chef Master"),
                                this.backgroundX + Layout.SKILL_NAME_X_OFFSET,
                                this.backgroundY + Layout.SKILL_NAME_Y_OFFSET, Colors.HIGHLIGHT, false);

                int chefLevel = this.skillState.getChefMasteryLevel();
                int nextLevel = Math.min(chefLevel + 1, SkillProgressManager.getMaxDefinedLevel());
                boolean isMax = chefLevel >= SkillProgressManager.getMaxDefinedLevel();

                context.drawText(this.textRenderer,
                                Text.literal("Current Level: " + NUMBER_FORMAT.format(chefLevel)),
                                this.backgroundX + Layout.CURRENT_LEVEL_X_OFFSET,
                                this.backgroundY + Layout.CURRENT_LEVEL_Y_OFFSET, Colors.PRIMARY_TEXT, false);

                String nextLevelLabel = isMax ? "Next Level: MAX"
                                : "Next Level: " + NUMBER_FORMAT.format(nextLevel);
                context.drawText(this.textRenderer, Text.literal(nextLevelLabel),
                                this.backgroundX + Layout.NEXT_LEVEL_X_OFFSET,
                                this.backgroundY + Layout.NEXT_LEVEL_Y_OFFSET, Colors.SECONDARY_TEXT, false);

                int xpBarX = this.backgroundX + Layout.XP_BAR_X_OFFSET;
                int xpBarY = this.backgroundY + Layout.XP_BAR_Y_OFFSET;

                int levelTextWidth = this.textRenderer.getWidth("Lv " + this.skillState.getLevel());
                context.drawText(this.textRenderer,
                                Text.literal("Lv " + NUMBER_FORMAT.format(this.skillState.getLevel())),
                                xpBarX - levelTextWidth - Layout.LEVEL_LABEL_PADDING,
                                xpBarY + (Layout.XP_BAR_HEIGHT / 2) - (this.textRenderer.fontHeight / 2),
                                Colors.HIGHLIGHT, false);

                context.drawTexture(XP_BAR_TEXTURE, xpBarX, xpBarY, 0, Layout.XP_BAR_BACKGROUND_V,
                                Layout.XP_BAR_WIDTH, Layout.XP_BAR_HEIGHT, Layout.XP_BAR_TEXTURE_WIDTH,
                                Layout.XP_BAR_TEXTURE_HEIGHT);

                long required = this.skillState.getExperienceRequiredForNextLevel();
                long progress = this.skillState.getExperienceTowardsNextLevel();
                String progressLabel;
                if (required > 0L) {
                        float ratio = Math.min(1.0F, Math.max(0.0F, (float) progress / (float) required));
                        int fillWidth = Math.max(1, Math.round(Layout.XP_BAR_WIDTH * ratio));
                        if (fillWidth > 0) {
                                context.drawTexture(XP_BAR_TEXTURE, xpBarX, xpBarY, 0, Layout.XP_BAR_FILL_V, fillWidth,
                                                Layout.XP_BAR_HEIGHT, Layout.XP_BAR_TEXTURE_WIDTH,
                                                Layout.XP_BAR_TEXTURE_HEIGHT);
                        }
                        progressLabel = NUMBER_FORMAT.format(progress) + " / " + NUMBER_FORMAT.format(required);
                } else {
                        context.drawTexture(XP_BAR_TEXTURE, xpBarX, xpBarY, 0, Layout.XP_BAR_FILL_V,
                                        Layout.XP_BAR_WIDTH, Layout.XP_BAR_HEIGHT, Layout.XP_BAR_TEXTURE_WIDTH,
                                        Layout.XP_BAR_TEXTURE_HEIGHT);
                        progressLabel = "Max Level";
                }

                int progressLabelWidth = this.textRenderer.getWidth(progressLabel);
                context.drawText(this.textRenderer, Text.literal(progressLabel),
                                xpBarX + (Layout.XP_BAR_WIDTH / 2) - (progressLabelWidth / 2),
                                this.backgroundY + Layout.XP_TEXT_Y_OFFSET, Colors.SECONDARY_TEXT, false);

                context.drawText(this.textRenderer,
                                Text.literal("Skill Points: " + NUMBER_FORMAT.format(this.skillState.getUnspentSkillPoints())),
                                this.backgroundX + Layout.SKILL_POINTS_TEXT_X_OFFSET,
                                this.backgroundY + Layout.SKILL_POINTS_TEXT_Y_OFFSET, Colors.PRIMARY_TEXT, false);

                context.drawText(this.textRenderer, Text.literal("Description"),
                                this.backgroundX + Layout.DESCRIPTION_TITLE_X_OFFSET,
                                this.backgroundY + Layout.DESCRIPTION_TITLE_Y_OFFSET, Colors.HIGHLIGHT, false);

                String description = SkillProgressManager.getSkillDefinitions()
                                .getOrDefault(SkillProgressManager.CHEF_SKILL,
                                                new SkillProgressManager.SkillDefinition(SkillProgressManager.CHEF_SKILL,
                                                                "Chef Master", ""))
                                .description();
                if (description.isBlank()) {
                        description = "No description available.";
                }

                int descriptionTextX = this.backgroundX + Layout.DESCRIPTION_TEXT_X_OFFSET;
                int descriptionTextY = this.backgroundY + Layout.DESCRIPTION_TEXT_Y_OFFSET;
                for (OrderedText line : this.textRenderer.wrapLines(Text.literal(description),
                                Layout.DESCRIPTION_TEXT_WRAP_WIDTH)) {
                        context.drawText(this.textRenderer, line, descriptionTextX, descriptionTextY,
                                        Colors.SECONDARY_TEXT, false);
                        descriptionTextY += this.textRenderer.fontHeight + Layout.DESCRIPTION_LINE_SPACING;
                }
        }

        private void updateUpgradeButtonState() {
                if (this.upgradeButton == null) {
                        return;
                }

                boolean canUpgrade = this.skillState.getUnspentSkillPoints() > 0
                                && this.skillState.getChefMasteryLevel() < SkillProgressManager.getMaxDefinedLevel();
                this.upgradeButton.active = canUpgrade;
        }

        private void sendSpendRequest(Identifier skillId, int points) {
                if (this.client == null || this.client.getNetworkHandler() == null || points <= 0
                                || this.skillState.getUnspentSkillPoints() <= 0) {
                        return;
                }

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeIdentifier(skillId);
                buf.writeVarInt(points);
                ClientPlayNetworking.send(ModPackets.SKILL_SPEND_REQUEST, buf);
        }

        void allocatePoint(Identifier skillId) {
                sendSpendRequest(skillId, 1);
        }

        @Override
        public void close() {
                if (this.client != null) {
                        this.client.setScreen(null);
                }
        }
}
