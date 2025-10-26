package net.jeremy.gardenkingmod.screen;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GardenOvenScreen extends HandledScreen<GardenOvenScreenHandler> {
        private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/oven_gui.png");

        public GardenOvenScreen(GardenOvenScreenHandler handler, PlayerInventory inventory, Text title) {
                super(handler, inventory, title);
                this.backgroundWidth = 176;
                this.backgroundHeight = 166;
        }

        @Override
        protected void init() {
                super.init();
                this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
                this.titleY = 6;
        }

        @Override
        protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
                int x = this.x;
                int y = this.y;
                context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

                int progress = this.handler.getCookProgress();
                if (progress > 0) {
                        context.drawTexture(TEXTURE, x + 90, y + 35, 176, 0, progress + 1, 17);
                }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                this.renderBackground(context);
                super.render(context, mouseX, mouseY, delta);
                this.drawMouseoverTooltip(context, mouseX, mouseY);
        }

        @Override
        protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
                context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
                context.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.backgroundHeight - 94, 0x404040, false);
        }
}
