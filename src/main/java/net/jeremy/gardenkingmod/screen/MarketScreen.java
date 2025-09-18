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
        private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID, "textures/gui/container/market_gui.png");

        private ButtonWidget sellButton;
        private int lastItemsSold;
        private int lastPayout;
        private int lastLifetimeTotal;
        private MutableText saleResultLine;
        private MutableText lifetimeResultLine;

        public MarketScreen(MarketScreenHandler handler, PlayerInventory inventory, Text title) {
                super(handler, inventory, title);
                this.backgroundWidth = 176;
                this.backgroundHeight = 166;
                this.playerInventoryTitleY = this.backgroundHeight - 88;
                this.lastItemsSold = -1;
                this.lastPayout = 0;
                this.lastLifetimeTotal = -1;
                this.saleResultLine = Text.empty();
                this.lifetimeResultLine = Text.empty();
        }

        @Override
        protected void init() {
                super.init();
                sellButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.gardenkingmod.market.sell"),
                                button -> {
                                        if (client != null && client.interactionManager != null) {
                                                client.interactionManager.clickButton(handler.syncId, 0);
                                        }
                                }).dimensions(x + 62, y + 44, 52, 20).build());
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

                int firstLineY = 70;
                int firstLineX = (backgroundWidth - textRenderer.getWidth(saleResultLine)) / 2;
                context.drawText(textRenderer, saleResultLine, firstLineX, firstLineY, 0xFFFFFF, false);

                if (lastLifetimeTotal >= 0 && lifetimeResultLine != null && !lifetimeResultLine.getString().isEmpty()) {
                        int secondLineY = firstLineY + 12;
                        int secondLineX = (backgroundWidth - textRenderer.getWidth(lifetimeResultLine)) / 2;
                        context.drawText(textRenderer, lifetimeResultLine, secondLineX, secondLineY, 0xFFFFFF, false);
                }
        }
}
