package net.jeremy.gardenkingmod.screen;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class MarketScreen extends HandledScreen<MarketScreenHandler> {
        private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/market_gui.png");

        private static final int BACKGROUND_WIDTH = 176;
        private static final int BACKGROUND_HEIGHT = 222;
        private static final int PLAYER_INVENTORY_LABEL_Y = BACKGROUND_HEIGHT - 94;
        private static final int TITLE_LABEL_Y = 42;
        private static final int SELL_BUTTON_WIDTH = 52;
        private static final int SELL_BUTTON_HEIGHT = 20;
        private static final int SCOREBOARD_BAND_TOP = 107;
        private static final int SCOREBOARD_BAND_BOTTOM = 138;
        private static final int SCOREBOARD_TEXT_PADDING = 7;
        private static final int SCOREBOARD_TEXT_LINE_SPACING = 12;

        private ButtonWidget sellButton;
        private int lastItemsSold;
        private int lastPayout;
        private int lastLifetimeTotal;
        private MutableText saleResultLine;
        private MutableText lifetimeResultLine;

        public MarketScreen(MarketScreenHandler handler, PlayerInventory inventory, Text title) {
                super(handler, inventory, title);
                this.backgroundWidth = BACKGROUND_WIDTH;
                this.backgroundHeight = BACKGROUND_HEIGHT;
                this.playerInventoryTitleY = PLAYER_INVENTORY_LABEL_Y;
                this.titleY = TITLE_LABEL_Y;
                this.lastItemsSold = -1;
                this.lastPayout = 0;
                this.lastLifetimeTotal = -1;
                this.saleResultLine = Text.empty();
                this.lifetimeResultLine = Text.empty();
        }

        @Override
        protected void init() {
                super.init();
                int sellButtonX = x + (backgroundWidth - SELL_BUTTON_WIDTH) / 2;
                int scoreboardBandHeight = SCOREBOARD_BAND_BOTTOM - SCOREBOARD_BAND_TOP + 1;
                int sellButtonY = y + SCOREBOARD_BAND_TOP + (scoreboardBandHeight - SELL_BUTTON_HEIGHT) / 2;
                sellButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.gardenkingmod.market.sell"),
                                button -> {
                                        if (client != null && client.interactionManager != null) {
                                                client.interactionManager.clickButton(handler.syncId, 0);
                                        }
                                }).dimensions(sellButtonX, sellButtonY, SELL_BUTTON_WIDTH, SELL_BUTTON_HEIGHT).build());
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

        public void updateSaleResult(int itemsSold, int payout, int lifetimeTotal) {
                this.lastItemsSold = itemsSold;
                this.lastPayout = payout;
                this.lastLifetimeTotal = lifetimeTotal >= 0 ? lifetimeTotal : -1;

                MutableText payoutText = Text.literal(Integer.toString(payout)).formatted(Formatting.GREEN);
                this.saleResultLine = Text
                                .translatable("screen.gardenkingmod.market.sale_result", itemsSold, payoutText)
                                .formatted(Formatting.YELLOW);

                if (this.lastLifetimeTotal >= 0) {
                        MutableText lifetimeText = Text.literal(Integer.toString(this.lastLifetimeTotal))
                                        .formatted(Formatting.GREEN);
                        this.lifetimeResultLine = Text
                                        .translatable("screen.gardenkingmod.market.lifetime", lifetimeText)
                                        .formatted(Formatting.YELLOW);
                } else {
                        this.lifetimeResultLine = Text.empty();
                }
        }

        @Override
        protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
                super.drawForeground(context, mouseX, mouseY);

                if (lastItemsSold < 0 || saleResultLine == null || saleResultLine.getString().isEmpty()) {
                        return;
                }

                if (sellButton == null) {
                        return;
                }

                int firstLineY = sellButton.getY() - this.y + SELL_BUTTON_HEIGHT + SCOREBOARD_TEXT_PADDING;
                int firstLineX = (backgroundWidth - textRenderer.getWidth(saleResultLine)) / 2;
                context.drawText(textRenderer, saleResultLine, firstLineX, firstLineY, 0xFFFFFF, false);

                if (lastLifetimeTotal >= 0 && lifetimeResultLine != null && !lifetimeResultLine.getString().isEmpty()) {
                        int secondLineY = firstLineY + SCOREBOARD_TEXT_LINE_SPACING;
                        int secondLineX = (backgroundWidth - textRenderer.getWidth(lifetimeResultLine)) / 2;
                        context.drawText(textRenderer, lifetimeResultLine, secondLineX, secondLineY, 0xFFFFFF, false);
                }
        }
}
