package net.jeremy.gardenkingmod.client.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.jeremy.gardenkingmod.client.skill.SkillState;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.jeremy.gardenkingmod.skill.SkillProgressManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;

/**
 * Skill overview screen that presents the player's Garden King skill
 * progression and allows point allocation. Designed to scale with future skill
 * trees by using a scrollable list of nodes.
 */
public class SkillScreen extends Screen {
        private static final int INFO_PANEL_HEIGHT = 84;
        private static final int INFO_PANEL_PADDING = 12;
        private static final int TREE_MARGIN = 14;
        private static final int DONE_BUTTON_WIDTH = 90;
        private static final int DONE_BUTTON_HEIGHT = 20;
        private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

        private final SkillState skillState = SkillState.getInstance();

        @Nullable
        private SkillTreeWidget skillTreeWidget;
        @Nullable
        private ButtonWidget closeButton;

        private int infoPanelLeft;
        private int infoPanelTop;
        private int infoPanelWidth;

        @Nullable
        private SkillProgressManager.SkillDefinition hoveredDefinition;
        private int hoveredMouseX;
        private int hoveredMouseY;

        public SkillScreen() {
                super(Text.translatable("screen.gardenkingmod.skills.title"));
        }

        @Override
        protected void init() {
                super.init();

                this.infoPanelWidth = Math.min(320, this.width - 40);
                this.infoPanelLeft = (this.width - this.infoPanelWidth) / 2;
                this.infoPanelTop = 32;
                int infoPanelBottom = this.infoPanelTop + INFO_PANEL_HEIGHT;

                int treeWidth = Math.min(360, this.width - 40);
                int treeTop = infoPanelBottom + TREE_MARGIN;
                int treeBottom = this.height - (TREE_MARGIN + DONE_BUTTON_HEIGHT + 8);
                int treeHeight = Math.max(64, treeBottom - treeTop);

                this.skillTreeWidget = new SkillTreeWidget(this, this.client, treeWidth, treeHeight, treeTop, treeBottom);
                this.skillTreeWidget.setLeftPos((this.width - treeWidth) / 2);
                this.skillTreeWidget.rebuild(SkillProgressManager.getSkillDefinitions(), this.skillState);
                this.addSelectableChild(this.skillTreeWidget);
                this.addDrawableChild(this.skillTreeWidget);

                this.closeButton = ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
                                .dimensions((this.width - DONE_BUTTON_WIDTH) / 2,
                                                this.height - DONE_BUTTON_HEIGHT - TREE_MARGIN, DONE_BUTTON_WIDTH,
                                                DONE_BUTTON_HEIGHT)
                                .build();
                this.addDrawableChild(this.closeButton);
        }

        @Override
        public void tick() {
                if (this.skillTreeWidget != null) {
                        this.skillTreeWidget.refreshAllocations(this.skillState, this.skillState.getUnspentSkillPoints());
                }
        }

        @Override
        public boolean shouldPause() {
                return false;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                this.hoveredDefinition = null;
                super.render(context, mouseX, mouseY, delta);
                drawInfoPanel(context);

                if (this.hoveredDefinition != null) {
                        List<Text> tooltip = new ArrayList<>();
                        tooltip.add(Text.literal(this.hoveredDefinition.displayName()).formatted(Formatting.GOLD));
                        String description = this.hoveredDefinition.description();
                        if (!description.isBlank()) {
                                tooltip.add(Text.literal(description).formatted(Formatting.GRAY));
                        }
                        context.drawTooltip(this.textRenderer, tooltip, this.hoveredMouseX, this.hoveredMouseY);
                }
        }

        private void drawInfoPanel(DrawContext context) {
                int panelRight = this.infoPanelLeft + this.infoPanelWidth;
                int panelBottom = this.infoPanelTop + INFO_PANEL_HEIGHT;
                context.fill(this.infoPanelLeft - 2, this.infoPanelTop - 2, panelRight + 2, panelBottom + 2, 0x50000000);
                context.fill(this.infoPanelLeft, this.infoPanelTop, panelRight, panelBottom, 0xB0000000);

                int textX = this.infoPanelLeft + INFO_PANEL_PADDING;
                int textY = this.infoPanelTop + INFO_PANEL_PADDING;
                int lineHeight = this.textRenderer.fontHeight + 3;

                int level = this.skillState.getLevel();
                long totalExperience = this.skillState.getTotalExperience();
                long progress = this.skillState.getExperienceTowardsNextLevel();
                long required = this.skillState.getExperienceRequiredForNextLevel();
                int unspent = this.skillState.getUnspentSkillPoints();

                context.drawText(this.textRenderer,
                                Text.translatable("screen.gardenkingmod.skills.level", level),
                                textX, textY, 0xFFFFFF, false);
                textY += lineHeight;

                context.drawText(this.textRenderer,
                                Text.translatable("screen.gardenkingmod.skills.total_xp", NUMBER_FORMAT.format(totalExperience)),
                                textX, textY, 0xD0D0D0, false);
                textY += lineHeight;

                String progressValue = NUMBER_FORMAT.format(progress);
                String requiredValue = NUMBER_FORMAT.format(required);
                context.drawText(this.textRenderer,
                                Text.translatable("screen.gardenkingmod.skills.progress", progressValue, requiredValue),
                                textX, textY, 0xD0D0D0, false);
                textY += lineHeight;

                context.drawText(this.textRenderer,
                                Text.translatable("screen.gardenkingmod.skills.unspent_points", unspent),
                                textX, textY, 0xFFD784, false);

                int barWidth = this.infoPanelWidth - (INFO_PANEL_PADDING * 2);
                int barHeight = 8;
                int barX = this.infoPanelLeft + INFO_PANEL_PADDING;
                int barY = panelBottom - INFO_PANEL_PADDING - barHeight;
                context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF3A3A3A);
                if (required > 0L) {
                        float ratio = Math.min(1.0F, Math.max(0.0F, (float) progress / (float) required));
                        int fillWidth = Math.round((barWidth - 2) * ratio);
                        if (fillWidth > 0) {
                                context.fill(barX + 1, barY + 1, barX + 1 + fillWidth, barY + barHeight - 1, 0xFF66C24A);
                        }
                }
        }

        public void setHoveredSkill(@Nullable SkillProgressManager.SkillDefinition definition, int mouseX, int mouseY) {
                this.hoveredDefinition = definition;
                this.hoveredMouseX = mouseX;
                this.hoveredMouseY = mouseY;
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

        int getUnspentPoints() {
                return this.skillState.getUnspentSkillPoints();
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

        private static final class SkillTreeWidget extends ElementListWidget<SkillEntry> {
                private static final int ENTRY_HEIGHT = 56;

                private final SkillScreen parent;

                private SkillTreeWidget(SkillScreen parent, MinecraftClient client, int width, int height, int top,
                                int bottom) {
                        super(client, width, height, top, bottom, ENTRY_HEIGHT);
                        this.parent = parent;
                }

                void rebuild(Map<Identifier, SkillProgressManager.SkillDefinition> definitions, SkillState state) {
                        this.clearEntries();
                        definitions.forEach((id, definition) -> {
                                SkillEntry entry = new SkillEntry(this.parent, definition);
                                entry.updateAllocation(state.getAllocation(id));
                                entry.updateUnspent(this.parent.getUnspentPoints());
                                this.addEntry(entry);
                        });
                }

                void refreshAllocations(SkillState state, int unspent) {
                        for (SkillEntry entry : this.children()) {
                                entry.updateAllocation(state.getAllocation(entry.getSkillId()));
                                entry.updateUnspent(unspent);
                        }
                }

                @Override
                public int getRowWidth() {
                        return this.width - 12;
                }

                @Override
                protected int getScrollbarPositionX() {
                        return this.getRowLeft() + this.getRowWidth() + 6;
                }
        }

        private static final class SkillEntry extends ElementListWidget.Entry<SkillEntry> {
                private static final int BUTTON_WIDTH = 80;
                private static final int BUTTON_HEIGHT = 20;

                private final SkillScreen parent;
                private final SkillProgressManager.SkillDefinition definition;
                private final ButtonWidget allocateButton;
                private int allocation;

                private SkillEntry(SkillScreen parent, SkillProgressManager.SkillDefinition definition) {
                        this.parent = parent;
                        this.definition = definition;
                        this.allocateButton = ButtonWidget.builder(
                                        Text.translatable("screen.gardenkingmod.skills.allocate"),
                                        button -> this.parent.allocatePoint(this.definition.id()))
                                        .dimensions(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT).build();
                }

                Identifier getSkillId() {
                        return this.definition.id();
                }

                void updateAllocation(int value) {
                        this.allocation = Math.max(0, value);
                }

                void updateUnspent(int unspent) {
                        this.allocateButton.active = unspent > 0;
                }

                @Override
                public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                                int mouseX, int mouseY, boolean hovered, float delta) {
                        int backgroundColor = hovered ? 0x4032A852 : 0x40222222;
                        context.fill(x, y, x + entryWidth, y + entryHeight, backgroundColor);

                        Text nameText = Text.literal(this.definition.displayName()).formatted(Formatting.WHITE);
                        context.drawText(this.parent.textRenderer, nameText, x + 8, y + 8, 0xFFFFFF, false);

                        Text allocationText = Text.translatable("screen.gardenkingmod.skills.points_spent", this.allocation)
                                        .formatted(Formatting.YELLOW);
                        context.drawText(this.parent.textRenderer, allocationText, x + 8,
                                        y + 8 + this.parent.textRenderer.fontHeight + 2, 0xFFD784, false);

                        int descriptionMaxWidth = entryWidth - BUTTON_WIDTH - 20;
                        Text description = Text.literal(this.definition.description()).formatted(Formatting.GRAY);
                        List<OrderedText> descriptionLines = this.parent.textRenderer.wrapLines(description,
                                        Math.max(32, descriptionMaxWidth));
                        int descriptionBaseY = y + 8 + (this.parent.textRenderer.fontHeight + 2) * 2;
                        for (int i = 0; i < Math.min(2, descriptionLines.size()); i++) {
                                context.drawText(this.parent.textRenderer, descriptionLines.get(i), x + 8,
                                                descriptionBaseY + (i * (this.parent.textRenderer.fontHeight + 1)),
                                                0xC0C0C0, false);
                        }

                        int buttonX = x + entryWidth - BUTTON_WIDTH - 8;
                        int buttonY = y + (entryHeight / 2) - (BUTTON_HEIGHT / 2);
                        this.allocateButton.setPosition(buttonX, buttonY);
                        this.allocateButton.render(context, mouseX, mouseY, delta);

                        if (hovered || this.allocateButton.isHovered()) {
                                this.parent.setHoveredSkill(this.definition, mouseX, mouseY);
                        }
                }

                @Override
                public List<? extends Element> children() {
                        return List.of(this.allocateButton);
                }

                @Override
                public List<? extends Selectable> selectableChildren() {
                        return List.of(this.allocateButton);
                }
        }
}
