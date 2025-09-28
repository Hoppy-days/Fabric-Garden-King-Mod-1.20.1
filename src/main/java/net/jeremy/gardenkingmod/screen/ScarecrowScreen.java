package net.jeremy.gardenkingmod.screen;

import java.util.ArrayList;
import java.util.List;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.block.ward.ScarecrowBlockEntity;
import net.jeremy.gardenkingmod.client.render.ScarecrowRenderHelper;
import net.jeremy.gardenkingmod.client.render.ScarecrowRenderHelper.ScarecrowEquipment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class ScarecrowScreen extends HandledScreen<ScarecrowScreenHandler> {
        private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/scarecrow_gui.png");
        private static final int BACKGROUND_WIDTH = 176;
        private static final int BACKGROUND_HEIGHT = 206;
        private static final int PLAYER_LABEL_Y = 108;
        private static final int TITLE_X = 8;
        private static final int TITLE_Y = 8;
        private static final int OVERLAY_SIZE = 18;
        private static final int SLOT_OVERLAY_U = 176;
        private static final int SLOT_OVERLAY_V = 0;
        private static final int RADIUS_TEXT_X = 8;
        private static final int RADIUS_TEXT_Y = 28;
        private static final int RADIUS_INFO_X = 8;
        private static final int RADIUS_INFO_Y = 24;
        private static final int RADIUS_INFO_WIDTH = 160;
        private static final int RADIUS_INFO_HEIGHT = 14;

        private static final Text HAT_TOOLTIP = Text.translatable("screen.gardenkingmod.scarecrow.slot.hat");
        private static final Text HEAD_TOOLTIP = Text.translatable("screen.gardenkingmod.scarecrow.slot.head");
        private static final Text CHEST_TOOLTIP = Text.translatable("screen.gardenkingmod.scarecrow.slot.chest");
        private static final Text HAND_TOOLTIP = Text.translatable("screen.gardenkingmod.scarecrow.slot.hand");

        private static final float PREVIEW_Z_OFFSET = 150.0F;
        private static final int PREVIEW_CENTER_X = 51;
        private static final int PREVIEW_CENTER_Y = 75;
        private static final float PREVIEW_SCALE = 28.0F;

        private ScarecrowRenderHelper renderHelper;

        public ScarecrowScreen(ScarecrowScreenHandler handler, PlayerInventory inventory, Text title) {
                super(handler, inventory, title);
                this.backgroundWidth = BACKGROUND_WIDTH;
                this.backgroundHeight = BACKGROUND_HEIGHT;
                this.playerInventoryTitleY = PLAYER_LABEL_Y;
                this.titleX = TITLE_X;
                this.titleY = TITLE_Y;
        }

        @Override
        protected void init() {
                super.init();
                this.titleX = TITLE_X;
                this.titleY = TITLE_Y;
                if (this.renderHelper == null) {
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client != null) {
                                this.renderHelper = ScarecrowRenderHelper.createDefault(client);
                        }
                }
        }

        @Override
        protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
                int x = (width - backgroundWidth) / 2;
                int y = (height - backgroundHeight) / 2;
                context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

                drawSlotOverlay(context, x + ScarecrowScreenHandler.HAT_SLOT_X - 1,
                                y + ScarecrowScreenHandler.HAT_SLOT_Y - 1);
                drawSlotOverlay(context, x + ScarecrowScreenHandler.HEAD_SLOT_X - 1,
                                y + ScarecrowScreenHandler.HEAD_SLOT_Y - 1);
                drawSlotOverlay(context, x + ScarecrowScreenHandler.CHEST_SLOT_X - 1,
                                y + ScarecrowScreenHandler.CHEST_SLOT_Y - 1);
                drawSlotOverlay(context, x + ScarecrowScreenHandler.PITCHFORK_SLOT_X - 1,
                                y + ScarecrowScreenHandler.PITCHFORK_SLOT_Y - 1);
        }

        @Override
        protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
                super.drawForeground(context, mouseX, mouseY);
                int horizontalRadius = handler.getHorizontalRadius();
                int verticalRadius = handler.getVerticalRadius();
                Text radiusSummary = Text.translatable("screen.gardenkingmod.scarecrow.radius.summary",
                                horizontalRadius, verticalRadius);
                context.drawText(textRenderer, radiusSummary, RADIUS_TEXT_X, RADIUS_TEXT_Y, 0x404040, false);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                renderBackground(context);
                super.render(context, mouseX, mouseY, delta);
                renderScarecrowModel(context, mouseX, mouseY);
                drawCustomTooltips(context, mouseX, mouseY);
                drawMouseoverTooltip(context, mouseX, mouseY);
        }

        private void renderScarecrowModel(DrawContext context, int mouseX, int mouseY) {
                if (this.renderHelper == null) {
                        return;
                }

                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null) {
                        return;
                }

                Inventory inventory = handler.getInventory();
                ItemStack hat = inventory.getStack(ScarecrowBlockEntity.SLOT_HAT);
                ItemStack head = inventory.getStack(ScarecrowBlockEntity.SLOT_HEAD);
                ItemStack chest = inventory.getStack(ScarecrowBlockEntity.SLOT_CHEST);
                ItemStack pitchfork = inventory.getStack(ScarecrowBlockEntity.SLOT_PITCHFORK);
                ScarecrowEquipment equipment = new ScarecrowEquipment(hat, head, chest, pitchfork);

                VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

                MatrixStack matrices = context.getMatrices();
                matrices.push();

                int originX = (width - backgroundWidth) / 2;
                int originY = (height - backgroundHeight) / 2;
                float centerX = originX + PREVIEW_CENTER_X;
                float centerY = originY + PREVIEW_CENTER_Y;

                float mouseDeltaX = centerX - mouseX;
                float mouseDeltaY = centerY - mouseY;
                float yaw = (float) Math.atan(mouseDeltaX / 40.0F);
                float pitch = (float) Math.atan(mouseDeltaY / 40.0F);

                matrices.translate(centerX, centerY, PREVIEW_Z_OFFSET);
                matrices.scale(1.0F, -1.0F, 1.0F);
                matrices.scale(PREVIEW_SCALE, PREVIEW_SCALE, PREVIEW_SCALE);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch * 20.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw * 40.0F));

                this.renderHelper.render(matrices, immediate, 0xF000F0, OverlayTexture.DEFAULT_UV, equipment,
                                client.world);

                matrices.pop();
                immediate.draw();
        }

        private void drawSlotOverlay(DrawContext context, int x, int y) {
                context.drawTexture(TEXTURE, x, y, SLOT_OVERLAY_U, SLOT_OVERLAY_V, OVERLAY_SIZE, OVERLAY_SIZE);
        }

        private void drawCustomTooltips(DrawContext context, int mouseX, int mouseY) {
                Slot hoveredSlot = findHoveredEquipmentSlot(mouseX, mouseY);
                if (hoveredSlot != null && !hoveredSlot.hasStack()) {
                        Text tooltip = getEquipmentTooltip(hoveredSlot);
                        if (tooltip != null) {
                                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                                return;
                        }
                }

                if (isPointWithinBounds(RADIUS_INFO_X, RADIUS_INFO_Y, RADIUS_INFO_WIDTH, RADIUS_INFO_HEIGHT, mouseX,
                                mouseY)) {
                        int horizontalRadius = handler.getHorizontalRadius();
                        int verticalRadius = handler.getVerticalRadius();
                        List<Text> radiusTooltip = new ArrayList<>();
                        radiusTooltip.add(Text.translatable("screen.gardenkingmod.scarecrow.radius.horizontal",
                                        horizontalRadius));
                        radiusTooltip.add(Text.translatable("screen.gardenkingmod.scarecrow.radius.vertical",
                                        verticalRadius));
                        radiusTooltip.add(Text.translatable("tooltip.gardenkingmod.scarecrow.ward_radius",
                                        Math.max(horizontalRadius, verticalRadius)));
                        context.drawTooltip(textRenderer, radiusTooltip, mouseX, mouseY);
                }
        }

        private Text getEquipmentTooltip(Slot slot) {
                if (slot.inventory == handler.getInventory()) {
                        return switch (slot.getIndex()) {
                        case ScarecrowBlockEntity.SLOT_HAT -> HAT_TOOLTIP;
                        case ScarecrowBlockEntity.SLOT_HEAD -> HEAD_TOOLTIP;
                        case ScarecrowBlockEntity.SLOT_CHEST -> CHEST_TOOLTIP;
                        case ScarecrowBlockEntity.SLOT_PITCHFORK -> HAND_TOOLTIP;
                        default -> null;
                        };
                }
                return null;
        }

        private Slot findHoveredEquipmentSlot(int mouseX, int mouseY) {
                for (Slot slot : handler.slots) {
                        if (slot.inventory == handler.getInventory()
                                        && isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                                return slot;
                        }
                }
                return null;
        }
}
