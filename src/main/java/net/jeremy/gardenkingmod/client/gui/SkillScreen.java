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
        private static final int BACKGROUND_WIDTH = 404;
        private static final int BACKGROUND_HEIGHT = 196;
        private static final int XP_BAR_WIDTH = 200;
        private static final int XP_BAR_HEIGHT = 10;
        private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

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

                this.backgroundX = (this.width - BACKGROUND_WIDTH) / 2;
                this.backgroundY = (this.height - BACKGROUND_HEIGHT) / 2;

                int buttonX = this.backgroundX + 23;
                int buttonY = this.backgroundY + 59;
                this.upgradeButton = ButtonWidget.builder(Text.literal("Upgrade"),
                                button -> this.allocatePoint(SkillProgressManager.CHEF_SKILL))
                                .dimensions(buttonX, buttonY, 80, 20).build();
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
                context.drawTexture(TEXTURE, this.backgroundX, this.backgroundY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

                int descriptionBoxX = this.backgroundX + 284;
                int descriptionBoxY = this.backgroundY;
                int descriptionBoxWidth = Math.max(32, BACKGROUND_WIDTH - 284 - 12);
                int descriptionBoxHeight = Math.max(32, BACKGROUND_HEIGHT - 12);

                int borderColor = 0x80404040;
                int backgroundColor = 0xC0101010;
                context.fill(descriptionBoxX, descriptionBoxY, descriptionBoxX + descriptionBoxWidth,
                                descriptionBoxY + descriptionBoxHeight, borderColor);
                context.fill(descriptionBoxX + 2, descriptionBoxY + 2, descriptionBoxX + descriptionBoxWidth - 2,
                                descriptionBoxY + descriptionBoxHeight - 2, backgroundColor);
        }

        private void drawForegroundLayer(DrawContext context) {
                int titleColor = 0xFFAA5500;
                int highlightColor = 0xFFAA5500;
                int yellowColor = 0xFFFFD200;
                int grayColor = 0xFFC0C0C0;

                int descriptionBoxX = this.backgroundX + 284;
                int descriptionBoxY = this.backgroundY;
                int descriptionBoxWidth = Math.max(32, BACKGROUND_WIDTH - 284 - 12);

                context.drawText(this.textRenderer, Text.literal("Skills"), this.backgroundX + 8,
                                this.backgroundY + 8, titleColor, false);

                context.drawText(this.textRenderer, Text.literal("Chef Master"), this.backgroundX + 18,
                                this.backgroundY + 26, highlightColor, false);

                int chefLevel = this.skillState.getChefMasteryLevel();
                int nextLevel = Math.min(chefLevel + 1, SkillProgressManager.getMaxDefinedLevel());
                boolean isMax = chefLevel >= SkillProgressManager.getMaxDefinedLevel();

                context.drawText(this.textRenderer,
                                Text.literal("Current Level: " + NUMBER_FORMAT.format(chefLevel)),
                                this.backgroundX + 22, this.backgroundY + 38, yellowColor, false);

                String nextLevelLabel = isMax ? "Next Level: MAX"
                                : "Next Level: " + NUMBER_FORMAT.format(nextLevel);
                context.drawText(this.textRenderer, Text.literal(nextLevelLabel),
                                this.backgroundX + 22, this.backgroundY + 48, grayColor, false);

                int xpBarX = this.backgroundX + 96;
                int xpBarY = this.backgroundY + 8;

                int levelTextWidth = this.textRenderer.getWidth("Lv " + this.skillState.getLevel());
                context.drawText(this.textRenderer,
                                Text.literal("Lv " + NUMBER_FORMAT.format(this.skillState.getLevel())),
                                xpBarX - levelTextWidth - 6, xpBarY + (XP_BAR_HEIGHT / 2) - (this.textRenderer.fontHeight / 2),
                                highlightColor, false);

                context.fill(xpBarX, xpBarY, xpBarX + XP_BAR_WIDTH, xpBarY + XP_BAR_HEIGHT, 0xFF3A3A3A);

                long required = this.skillState.getExperienceRequiredForNextLevel();
                long progress = this.skillState.getExperienceTowardsNextLevel();
                String progressLabel;
                if (required > 0L) {
                        float ratio = Math.min(1.0F, Math.max(0.0F, (float) progress / (float) required));
                        int fillWidth = Math.round((XP_BAR_WIDTH - 2) * ratio);
                        if (fillWidth > 0) {
                                context.fill(xpBarX + 1, xpBarY + 1, xpBarX + 1 + fillWidth, xpBarY + XP_BAR_HEIGHT - 1,
                                                0xFF66C24A);
                        }
                        progressLabel = NUMBER_FORMAT.format(progress) + " / " + NUMBER_FORMAT.format(required);
                } else {
                        context.fill(xpBarX + 1, xpBarY + 1, xpBarX + XP_BAR_WIDTH - 1, xpBarY + XP_BAR_HEIGHT - 1,
                                        0xFF66C24A);
                        progressLabel = "Max Level";
                }

                int progressLabelWidth = this.textRenderer.getWidth(progressLabel);
                context.drawText(this.textRenderer, Text.literal(progressLabel),
                                xpBarX + (XP_BAR_WIDTH / 2) - (progressLabelWidth / 2),
                                xpBarY + XP_BAR_HEIGHT + 3, grayColor, false);

                context.drawText(this.textRenderer,
                                Text.literal("Skill Points: " + NUMBER_FORMAT.format(this.skillState.getUnspentSkillPoints())),
                                this.backgroundX + 198, this.backgroundY + 4, yellowColor, false);

                context.drawText(this.textRenderer, Text.literal("Description"), descriptionBoxX + 4,
                                descriptionBoxY + 8, highlightColor, false);

                String description = SkillProgressManager.getSkillDefinitions()
                                .getOrDefault(SkillProgressManager.CHEF_SKILL,
                                                new SkillProgressManager.SkillDefinition(SkillProgressManager.CHEF_SKILL,
                                                                "Chef Master", ""))
                                .description();
                if (description.isBlank()) {
                        description = "No description available.";
                }

                int descriptionTextX = descriptionBoxX + 4;
                int descriptionTextY = descriptionBoxY + 22;
                int wrapWidth = descriptionBoxWidth - 8;
                for (OrderedText line : this.textRenderer.wrapLines(Text.literal(description), wrapWidth)) {
                        context.drawText(this.textRenderer, line, descriptionTextX, descriptionTextY, grayColor, false);
                        descriptionTextY += this.textRenderer.fontHeight + 2;
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
