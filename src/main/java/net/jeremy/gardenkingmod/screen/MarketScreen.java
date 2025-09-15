package net.jeremy.gardenkingmod.screen;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MarketScreen extends HandledScreen<MarketScreenHandler> {
        private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID, "textures/gui/container/market.png");

        private ButtonWidget sellButton;

        public MarketScreen(MarketScreenHandler handler, PlayerInventory inventory, Text title) {
                super(handler, inventory, title);
                this.backgroundWidth = 176;
                this.backgroundHeight = 166;
                this.playerInventoryTitleY = this.backgroundHeight - 94;
        }

        @Override
        protected void init() {
                super.init();
                sellButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.gardenkingmod.market.sell"),
                                button -> {
                                        if (client != null && client.interactionManager != null) {
                                                client.interactionManager.clickButton(handler.syncId, 0);
                                        }
                                }).dimensions(x + 62, y + 60, 52, 20).build());
        }

        @Override
        protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
                context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                renderBackground(context);
                if (sellButton != null) {
                        sellButton.active = handler.hasSellableItem();
                }
                super.render(context, mouseX, mouseY, delta);
                drawMouseoverTooltip(context, mouseX, mouseY);
        }
}
