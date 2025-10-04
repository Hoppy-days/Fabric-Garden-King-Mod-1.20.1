package net.jeremy.gardenkingmod.screen;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GardenShopScreen extends HandledScreen<GardenShopScreenHandler> {
        private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/garden_shop_gui.png");

        private static final int BACKGROUND_WIDTH = 274;
        private static final int BACKGROUND_HEIGHT = 198;
        private static final int PLAYER_INVENTORY_LABEL_Y = BACKGROUND_HEIGHT - 94;
        private static final int TITLE_X = 8;
        private static final int TITLE_Y = 6;

        public GardenShopScreen(GardenShopScreenHandler handler, PlayerInventory inventory, Text title) {
                super(handler, inventory, title);
                this.backgroundWidth = BACKGROUND_WIDTH;
                this.backgroundHeight = BACKGROUND_HEIGHT;
                this.playerInventoryTitleY = PLAYER_INVENTORY_LABEL_Y;
                this.titleX = TITLE_X;
                this.titleY = TITLE_Y;
        }

        @Override
        protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
                int x = (width - backgroundWidth) / 2;
                int y = (height - backgroundHeight) / 2;
                context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                renderBackground(context);
                super.render(context, mouseX, mouseY, delta);
                drawMouseoverTooltip(context, mouseX, mouseY);
        }
}
