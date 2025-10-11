package net.jeremy.gardenkingmod.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.shop.GearShopOffer;
import net.jeremy.gardenkingmod.shop.GearShopOfferManager;
import net.jeremy.gardenkingmod.shop.GearShopStackHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class MarketScreen extends HandledScreen<MarketScreenHandler> {
        private static final Identifier SELL_TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/market_sell_gui.png");
        private static final Identifier BUY_TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/market_buy_gui.png");

        private static final int SELL_TEXTURE_WIDTH = 256;
        private static final int SELL_TEXTURE_HEIGHT = 256;

        private static final int SELL_BACKGROUND_U = 0;
        private static final int SELL_BACKGROUND_V = 0;
        private static final int SELL_BACKGROUND_WIDTH = 175;
        private static final int BACKGROUND_HEIGHT = 220;
        private static final int PLAYER_INVENTORY_LABEL_Y = BACKGROUND_HEIGHT - 94;
        private static final int TITLE_LABEL_Y = 22;
        private static final int SELL_BUTTON_WIDTH = 52;
        private static final int SELL_BUTTON_HEIGHT = 20;
        private static final int TAB_BUTTON_WIDTH = 35;
        private static final int TAB_BUTTON_HEIGHT = 15;
        private static final int TAB_BUTTON_Y_OFFSET = 3;
        private static final int TAB_BUTTON_TEXT_PADDING_X = 4;
        private static final int SELL_TAB_TEXT_X = 15;
        private static final int BUY_TAB_TEXT_X = 54;
        private static final int SCOREBOARD_BAND_TOP = 107;
        private static final int SCOREBOARD_BAND_BOTTOM = 138;
        private static final int RESULT_TEXT_TOP_OFFSET = -5;
        private static final int RESULT_TEXT_LINE_SPACING = 12;

        private static final int BUY_HEADER_COLOR = 0x404040;
        private static final int BUY_OFFERS_LABEL_X = 10;
        private static final int BUY_OFFERS_LABEL_Y = 24;
        private static final int BUY_COST_LABEL_X = 30;
        private static final int BUY_COST_LABEL_Y = 24;
        private static final int BUY_RESULT_LABEL_X = 118;
        private static final int BUY_RESULT_LABEL_Y = 24;

        private static final int BUY_OFFER_LIST_X = 10;
        private static final int BUY_OFFER_LIST_Y = 36;
        private static final int BUY_OFFER_ENTRY_WIDTH = 144;
        private static final int BUY_OFFER_ENTRY_HEIGHT = 20;
        private static final int BUY_MAX_VISIBLE_OFFERS = 6;
        private static final int BUY_OFFER_LIST_HEIGHT = BUY_MAX_VISIBLE_OFFERS * BUY_OFFER_ENTRY_HEIGHT;
        private static final int BUY_OFFER_ITEM_OFFSET_Y = 2;
        private static final int BUY_OFFER_COST_ITEM_OFFSET_X = 6;
        private static final int BUY_OFFER_COST_ITEM_SPACING = 22;
        private static final int BUY_OFFER_ARROW_OFFSET_X = 96;
        private static final int BUY_OFFER_RESULT_ITEM_OFFSET_X = 120;
        private static final int BUY_OFFER_BACKGROUND_U = 301;
        private static final int BUY_OFFER_BACKGROUND_V = 0;
        private static final int BUY_OFFER_HOVER_BACKGROUND_V = 21;
        private static final int BUY_OFFER_ARROW_U = 301;
        private static final int BUY_OFFER_ARROW_V = 42;
        private static final int BUY_OFFER_ARROW_WIDTH = 10;
        private static final int BUY_OFFER_ARROW_HEIGHT = 9;
        private static final int SELECTED_HIGHLIGHT_COLOR = 0x40FFFFFF;

        private static final class BuyBackground {
                private static final int TEXTURE_WIDTH = 512;
                private static final int TEXTURE_HEIGHT = 256;
                private static final int BACKGROUND_WIDTH = 276;
                private static final int BACKGROUND_HEIGHT = 220;
                private static final int U = 0;
                private static final int V = 0;

                private BuyBackground() {
                }
        }

        private static final int BUY_SCROLLBAR_OFFSET_X = BUY_OFFER_LIST_X + BUY_OFFER_ENTRY_WIDTH + 2;
        private static final int BUY_SCROLLBAR_OFFSET_Y = BUY_OFFER_LIST_Y;
        private static final int BUY_SCROLLBAR_TRACK_WIDTH = 6;
        private static final int BUY_SCROLLBAR_TRACK_HEIGHT = BUY_OFFER_LIST_HEIGHT;
        private static final int BUY_SCROLLBAR_KNOB_U = 24;
        private static final int BUY_SCROLLBAR_KNOB_V = 207;
        private static final int BUY_SCROLLBAR_KNOB_WIDTH = 6;
        private static final int BUY_SCROLLBAR_KNOB_HEIGHT = 27;

        private ButtonWidget sellButton;
        private TabButton sellTabButton;
        private TabButton buyTabButton;
        private int lastItemsSold;
        private int lastPayout;
        private int lastLifetimeTotal;
        private MutableText saleResultLine;
        private MutableText lifetimeResultLine;

        private Tab activeTab = Tab.SELL;
        private int maxScrollSteps;
        private int scrollOffset;
        private float scrollAmount;
        private boolean scrollbarDragging;
        private int selectedOffer = -1;
        private int lastOfferCount = -1;

        public MarketScreen(MarketScreenHandler handler, PlayerInventory inventory, Text title) {
                super(handler, inventory, title);
                this.backgroundWidth = SELL_BACKGROUND_WIDTH;
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
                addTabButtons();
                int sellButtonX = x + (backgroundWidth - SELL_BUTTON_WIDTH) / 2;
                int scoreboardBandHeight = SCOREBOARD_BAND_BOTTOM - SCOREBOARD_BAND_TOP + 1;
                int sellButtonY = y + SCOREBOARD_BAND_TOP + (scoreboardBandHeight - SELL_BUTTON_HEIGHT) / 2;
                sellButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.gardenkingmod.market.sell"),
                                button -> {
                                        if (client != null && client.interactionManager != null) {
                                                client.interactionManager.clickButton(handler.syncId, 0);
                                        }
                                }).dimensions(sellButtonX, sellButtonY, SELL_BUTTON_WIDTH, SELL_BUTTON_HEIGHT).build());
                updateTabButtonState();
                updateSellButtonVisibility();
                resetBuyTabState();
                updateTabButtonPositions();
        }

        @Override
        protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
                if (activeTab == Tab.BUY) {
                        int originX = getBuyBackgroundX();
                        int originY = getBuyBackgroundY();
                        context.drawTexture(BUY_TEXTURE, originX, originY, BuyBackground.U, BuyBackground.V,
                                        BuyBackground.BACKGROUND_WIDTH, BuyBackground.BACKGROUND_HEIGHT,
                                        BuyBackground.TEXTURE_WIDTH, BuyBackground.TEXTURE_HEIGHT);
                        drawBuyOfferList(context, originX, originY, mouseX, mouseY);
                        drawBuyScrollbar(context, originX, originY);
                } else {
                        context.drawTexture(SELL_TEXTURE, x, y, SELL_BACKGROUND_U, SELL_BACKGROUND_V, backgroundWidth, backgroundHeight,
                                        SELL_TEXTURE_WIDTH, SELL_TEXTURE_HEIGHT);
                }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                renderBackground(context);
                if (sellButton != null) {
                        sellButton.visible = activeTab == Tab.SELL;
                        sellButton.active = sellButton.visible && handler.hasSellableItem();
                }
                if (activeTab == Tab.BUY) {
                        updateBuyScrollLimits();
                }
                super.render(context, mouseX, mouseY, delta);
                drawMouseoverTooltip(context, mouseX, mouseY);
        }

        public void updateSaleResult(boolean success, int itemsSold, int payout, int lifetimeTotal, Text feedback,
                        Map<Item, Integer> soldItemCounts) {
                this.lastItemsSold = itemsSold;
                this.lastPayout = payout;
                this.lastLifetimeTotal = success && lifetimeTotal >= 0 ? lifetimeTotal : -1;

                if (success) {
                        MutableText payoutText = Text.literal(Integer.toString(payout)).formatted(Formatting.GREEN);
                        if (soldItemCounts != null && !soldItemCounts.isEmpty()) {
                                Text soldItemsText = buildSoldItemsText(soldItemCounts);
                                this.saleResultLine = Text.translatable(
                                                "screen.gardenkingmod.market.sale_result_detailed", soldItemsText,
                                                payoutText).formatted(Formatting.YELLOW);
                        } else {
                                this.saleResultLine = Text
                                                .translatable("screen.gardenkingmod.market.sale_result", itemsSold,
                                                                payoutText)
                                                .formatted(Formatting.YELLOW);
                        }

                        if (this.lastLifetimeTotal >= 0) {
                                MutableText lifetimeText = Text.literal(Integer.toString(this.lastLifetimeTotal))
                                                .formatted(Formatting.GREEN);
                                this.lifetimeResultLine = Text
                                                .translatable("screen.gardenkingmod.market.lifetime", lifetimeText)
                                                .formatted(Formatting.YELLOW);
                        } else {
                                this.lifetimeResultLine = Text.empty();
                        }
                } else {
                        MutableText feedbackLine = feedback.copy();
                        if (!feedbackLine.getString().isEmpty()) {
                                this.saleResultLine = feedbackLine.formatted(Formatting.RED);
                        } else {
                                this.saleResultLine = Text.empty();
                        }
                        this.lifetimeResultLine = Text.empty();
                }
        }

        private Text buildSoldItemsText(Map<Item, Integer> soldItemCounts) {
                List<Text> entries = new ArrayList<>();
                soldItemCounts.forEach((item, count) -> {
                        if (count <= 0) {
                                return;
                        }
                        Text itemName = Text.translatable(item.getTranslationKey());
                        entries.add(Text.translatable("screen.gardenkingmod.market.sale_result_item", count, itemName));
                });

                if (entries.isEmpty()) {
                        return Text.literal(Integer.toString(Math.max(this.lastItemsSold, 0)));
                }

                return Texts.join(entries, Text.literal(", "));
        }

        @Override
        protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
                super.drawForeground(context, mouseX, mouseY);

                if (activeTab == Tab.BUY) {
                        context.getMatrices().push();
                        context.getMatrices().translate(getBuyBackgroundX() - this.x,
                                        getBuyBackgroundY() - this.y, 0);
                        drawBuyLabels(context);
                        context.getMatrices().pop();
                        return;
                }

                if (lastItemsSold < 0 || saleResultLine == null || saleResultLine.getString().isEmpty()) {
                        return;
                }

                int firstLineY = RESULT_TEXT_TOP_OFFSET;
                int firstLineX = (backgroundWidth - textRenderer.getWidth(saleResultLine)) / 2;
                context.drawText(textRenderer, saleResultLine, firstLineX, firstLineY, 0xFFFFFF, false);

                if (lastLifetimeTotal >= 0 && lifetimeResultLine != null && !lifetimeResultLine.getString().isEmpty()) {
                        int secondLineY = firstLineY + RESULT_TEXT_LINE_SPACING;
                        int secondLineX = (backgroundWidth - textRenderer.getWidth(lifetimeResultLine)) / 2;
                        context.drawText(textRenderer, lifetimeResultLine, secondLineX, secondLineY, 0xFFFFFF, false);
                }
        }

        @Override
        protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
                super.drawMouseoverTooltip(context, mouseX, mouseY);
                if (activeTab != Tab.BUY) {
                        return;
                }

                getHoveredOfferStack(mouseX, mouseY).ifPresent(hovered -> {
                        ItemStack stack = hovered.stack();
                        if (hovered.isCostStack()) {
                                drawCostTooltip(context, stack, mouseX, mouseY);
                        } else {
                                context.drawTooltip(textRenderer, getTooltipFromItem(stack), mouseX, mouseY);
                        }
                });
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (activeTab == Tab.BUY && button == 0) {
                        if (isPointWithinBuyScrollbar(mouseX, mouseY)) {
                                scrollbarDragging = true;
                                updateScrollFromMouse(mouseY);
                                return true;
                        }

                        int offerIndex = getOfferIndexAt(mouseX, mouseY);
                        if (offerIndex >= 0) {
                                selectOffer(offerIndex);
                                playClickSound();
                                return true;
                        }
                }

                return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                if (activeTab == Tab.BUY && scrollbarDragging) {
                        updateScrollFromMouse(mouseY);
                        return true;
                }
                return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
                scrollbarDragging = false;
                return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
                if (activeTab != Tab.BUY || !canScroll()) {
                        return super.mouseScrolled(mouseX, mouseY, amount);
                }

                float scrollDelta = (float) (amount / (double) Math.max(maxScrollSteps, 1));
                setScrollAmount(scrollAmount - scrollDelta);
                return true;
        }

        private void addTabButtons() {
                int buttonY = y + TAB_BUTTON_Y_OFFSET;
                int sellButtonX = x + SELL_TAB_TEXT_X - TAB_BUTTON_TEXT_PADDING_X;
                int buyButtonX = x + BUY_TAB_TEXT_X - TAB_BUTTON_TEXT_PADDING_X;
                sellTabButton = addDrawableChild(new TabButton(sellButtonX, buttonY, Tab.SELL, getTabLabel(Tab.SELL),
                                () -> setActiveTab(Tab.SELL)));
                buyTabButton = addDrawableChild(new TabButton(buyButtonX, buttonY, Tab.BUY, getTabLabel(Tab.BUY),
                                () -> setActiveTab(Tab.BUY)));
        }

        private void setActiveTab(Tab tab) {
                if (this.activeTab == tab) {
                        return;
                }
                this.activeTab = tab;
                if (this.handler != null) {
                        this.handler.setMarketSlotsEnabled(tab == Tab.SELL);
                        sendTabChangeToServer(tab);
                }
                updateTabButtonState();
                updateSellButtonVisibility();
                if (tab == Tab.BUY) {
                        resetBuyTabState();
                }
                updateTabButtonPositions();
        }

        private void updateSellButtonVisibility() {
                if (sellButton != null) {
                        sellButton.visible = activeTab == Tab.SELL;
                }
        }

        private void sendTabChangeToServer(Tab tab) {
                if (client == null || client.interactionManager == null) {
                        return;
                }

                int buttonId = tab == Tab.SELL ? MarketScreenHandler.BUTTON_SELECT_SELL_TAB
                                : MarketScreenHandler.BUTTON_SELECT_BUY_TAB;
                client.interactionManager.clickButton(handler.syncId, buttonId);
        }

        private class TabButton extends ClickableWidget {
                private final Tab tab;
                private final Runnable pressAction;

                private TabButton(int x, int y, Tab tab, Text message, Runnable pressAction) {
                        super(x, y, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT, message);
                        this.tab = tab;
                        this.pressAction = pressAction;
                }

                private void reposition(int x, int y) {
                        setPosition(x, y);
                }

                @Override
                public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
                        int textX = getX() + TAB_BUTTON_TEXT_PADDING_X;
                        int textY = getY() + (TAB_BUTTON_HEIGHT - textRenderer.fontHeight) / 2;
                        boolean selected = tab == activeTab;
                        int baseColor = getMessage().getStyle().getColor() != null
                                        ? getMessage().getStyle().getColor().getRgb()
                                        : 0xFFFFFF;
                        int color = selected || active ? baseColor : 0xA0A0A0;
                        context.drawText(textRenderer, getMessage(), textX, textY, color, false);
                }

                @Override
                public void onClick(double mouseX, double mouseY) {
                        if (pressAction != null && active) {
                                pressAction.run();
                        }
                }

                @Override
                protected void appendClickableNarrations(NarrationMessageBuilder builder) {
                        builder.put(NarrationPart.TITLE, getMessage());
                }
        }

        private void updateTabButtonState() {
                if (sellTabButton != null) {
                        sellTabButton.setMessage(getTabLabel(Tab.SELL));
                        sellTabButton.active = activeTab != Tab.SELL;
                }
                if (buyTabButton != null) {
                        buyTabButton.setMessage(getTabLabel(Tab.BUY));
                        buyTabButton.active = activeTab != Tab.BUY;
                }
        }

        private MutableText getTabLabel(Tab tab) {
                Text base = Text.translatable(tab.translationKey);
                return base.copy().formatted(tab == activeTab ? Formatting.GOLD : Formatting.WHITE);
        }

        private void updateTabButtonPositions() {
                int baseX = activeTab == Tab.BUY ? getBuyBackgroundX() : this.x;
                int baseY = activeTab == Tab.BUY ? getBuyBackgroundY() : this.y;
                int buttonY = baseY + TAB_BUTTON_Y_OFFSET;
                if (sellTabButton != null) {
                        sellTabButton.reposition(baseX + SELL_TAB_TEXT_X - TAB_BUTTON_TEXT_PADDING_X, buttonY);
                }
                if (buyTabButton != null) {
                        buyTabButton.reposition(baseX + BUY_TAB_TEXT_X - TAB_BUTTON_TEXT_PADDING_X, buttonY);
                }
        }

        private int getBuyBackgroundX() {
                int extraWidth = Math.max(0, BuyBackground.BACKGROUND_WIDTH - this.backgroundWidth);
                return this.x - extraWidth;
        }

        private int getBuyBackgroundY() {
                return this.y;
        }

        private void resetBuyTabState() {
                selectedOffer = -1;
                setScrollAmount(0.0F);
                lastOfferCount = -1;
                updateBuyScrollLimits();
        }

        private void drawBuyLabels(DrawContext context) {
                context.drawText(textRenderer, Text.translatable("screen.gardenkingmod.market.offers"),
                                BUY_OFFERS_LABEL_X, BUY_OFFERS_LABEL_Y, BUY_HEADER_COLOR, false);
                context.drawText(textRenderer, Text.translatable("screen.gardenkingmod.market.cost_label"),
                                BUY_COST_LABEL_X, BUY_COST_LABEL_Y, BUY_HEADER_COLOR, false);
                context.drawText(textRenderer, Text.translatable("screen.gardenkingmod.market.buy_label"),
                                BUY_RESULT_LABEL_X, BUY_RESULT_LABEL_Y, BUY_HEADER_COLOR, false);
        }

        private void drawBuyOfferList(DrawContext context, int originX, int originY, int mouseX, int mouseY) {
                List<GearShopOffer> offers = getBuyOffers();
                if (offers.isEmpty()) {
                        return;
                }

                int listLeft = originX + BUY_OFFER_LIST_X;
                int listTop = originY + BUY_OFFER_LIST_Y;
                int hoveredOffer = getOfferIndexAt(mouseX, mouseY);
                int visibleOffers = Math.min(BUY_MAX_VISIBLE_OFFERS, Math.max(offers.size() - scrollOffset, 0));
                int scissorHeight = Math.min(BUY_MAX_VISIBLE_OFFERS, offers.size()) * BUY_OFFER_ENTRY_HEIGHT;
                if (scissorHeight <= 0) {
                        return;
                }

                context.enableScissor(listLeft, listTop, listLeft + BUY_OFFER_ENTRY_WIDTH, listTop + scissorHeight);
                for (int visibleRow = 0; visibleRow < visibleOffers; visibleRow++) {
                        int offerIndex = scrollOffset + visibleRow;
                        int entryY = listTop + visibleRow * BUY_OFFER_ENTRY_HEIGHT;
                        int backgroundV = offerIndex == hoveredOffer ? BUY_OFFER_HOVER_BACKGROUND_V
                                        : BUY_OFFER_BACKGROUND_V;
                        context.drawTexture(BUY_TEXTURE, listLeft, entryY, BUY_OFFER_BACKGROUND_U, backgroundV,
                                        BUY_OFFER_ENTRY_WIDTH, BUY_OFFER_ENTRY_HEIGHT, BuyBackground.TEXTURE_WIDTH,
                                        BuyBackground.TEXTURE_HEIGHT);

                        if (offerIndex == selectedOffer) {
                                context.fill(listLeft, entryY, listLeft + BUY_OFFER_ENTRY_WIDTH,
                                                entryY + BUY_OFFER_ENTRY_HEIGHT, SELECTED_HIGHLIGHT_COLOR);
                        }

                        GearShopOffer offer = offers.get(offerIndex);
                        int itemY = entryY + BUY_OFFER_ITEM_OFFSET_Y;
                        int costStartX = listLeft + BUY_OFFER_COST_ITEM_OFFSET_X;
                        int arrowX = listLeft + BUY_OFFER_ARROW_OFFSET_X;
                        int maxCostRight = arrowX - 2;
                        List<ItemStack> costStacks = offer.costStacks();
                        for (int costIndex = 0; costIndex < costStacks.size(); costIndex++) {
                                int costX = costStartX + costIndex * BUY_OFFER_COST_ITEM_SPACING;
                                if (costX + 16 > maxCostRight) {
                                        break;
                                }
                                ItemStack costStack = costStacks.get(costIndex);
                                drawCostStack(context, costStack, costX, itemY);
                        }

                        int arrowY = entryY + BUY_OFFER_ITEM_OFFSET_Y + 4;
                        context.drawTexture(BUY_TEXTURE, arrowX, arrowY, BUY_OFFER_ARROW_U, BUY_OFFER_ARROW_V,
                                        BUY_OFFER_ARROW_WIDTH, BUY_OFFER_ARROW_HEIGHT, BuyBackground.TEXTURE_WIDTH,
                                        BuyBackground.TEXTURE_HEIGHT);

                        ItemStack displayStack = offer.copyResultStack();
                        int resultX = listLeft + BUY_OFFER_RESULT_ITEM_OFFSET_X;
                        context.drawItem(displayStack, resultX, itemY);
                        context.drawItemInSlot(textRenderer, displayStack, resultX, itemY);
                }
                context.disableScissor();
        }

        private void drawCostStack(DrawContext context, ItemStack stack, int x, int y) {
                context.drawItem(stack, x, y);
                int requestedCount = GearShopStackHelper.getRequestedCount(stack);
                if (requestedCount > stack.getCount()) {
                        String label = formatRequestedCount(requestedCount);
                        context.drawItemInSlot(textRenderer, stack, x, y, label);
                } else {
                        context.drawItemInSlot(textRenderer, stack, x, y);
                }
        }

        private void drawBuyScrollbar(DrawContext context, int originX, int originY) {
                int scrollbarX = originX + BUY_SCROLLBAR_OFFSET_X;
                int scrollbarY = originY + BUY_SCROLLBAR_OFFSET_Y;
                int knobTravel = BUY_SCROLLBAR_TRACK_HEIGHT - BUY_SCROLLBAR_KNOB_HEIGHT;
                int knobY;

                if (!canScroll()) {
                        knobY = scrollbarY;
                } else {
                        knobY = scrollbarY + Math.round(scrollAmount * knobTravel);
                        knobY = MathHelper.clamp(knobY, scrollbarY, scrollbarY + knobTravel);
                }

                context.drawTexture(BUY_TEXTURE, scrollbarX, knobY, BUY_SCROLLBAR_KNOB_U, BUY_SCROLLBAR_KNOB_V,
                                BUY_SCROLLBAR_KNOB_WIDTH, BUY_SCROLLBAR_KNOB_HEIGHT, BuyBackground.TEXTURE_WIDTH,
                                BuyBackground.TEXTURE_HEIGHT);
        }

        private void updateBuyScrollLimits() {
                List<GearShopOffer> offers = getBuyOffers();
                int offerCount = offers.size();
                if (offerCount == lastOfferCount) {
                        return;
                }
                lastOfferCount = offerCount;
                maxScrollSteps = Math.max(offerCount - BUY_MAX_VISIBLE_OFFERS, 0);
                setScrollAmount(scrollAmount);
                if (selectedOffer >= offerCount) {
                        selectedOffer = offerCount - 1;
                }
        }

        private void updateScrollFromMouse(double mouseY) {
                int scrollbarY = getBuyBackgroundY() + BUY_SCROLLBAR_OFFSET_Y;
                double relativeY = mouseY - scrollbarY - (BUY_SCROLLBAR_KNOB_HEIGHT / 2.0);
                double available = BUY_SCROLLBAR_TRACK_HEIGHT - BUY_SCROLLBAR_KNOB_HEIGHT;
                if (available <= 0) {
                        return;
                }
                setScrollAmount((float) (relativeY / available));
        }

        private void setScrollAmount(float amount) {
                if (!canScroll()) {
                        scrollAmount = 0.0F;
                        scrollOffset = 0;
                        return;
                }

                float clampedAmount = MathHelper.clamp(amount, 0.0F, 1.0F);
                int calculatedOffset = MathHelper.floor(clampedAmount * maxScrollSteps + 0.5F);
                scrollOffset = MathHelper.clamp(calculatedOffset, 0, maxScrollSteps);
                scrollAmount = maxScrollSteps == 0 ? 0.0F : (float) scrollOffset / (float) maxScrollSteps;
        }

        private boolean canScroll() {
                return activeTab == Tab.BUY && maxScrollSteps > 0;
        }

        private boolean isPointWithinBuyScrollbar(double mouseX, double mouseY) {
                int scrollbarX = getBuyBackgroundX() + BUY_SCROLLBAR_OFFSET_X;
                int scrollbarY = getBuyBackgroundY() + BUY_SCROLLBAR_OFFSET_Y;
                return mouseX >= scrollbarX && mouseX < scrollbarX + BUY_SCROLLBAR_TRACK_WIDTH && mouseY >= scrollbarY
                                && mouseY < scrollbarY + BUY_SCROLLBAR_TRACK_HEIGHT;
        }

        private int getOfferIndexAt(double mouseX, double mouseY) {
                if (activeTab != Tab.BUY) {
                        return -1;
                }

                int listLeft = getBuyBackgroundX() + BUY_OFFER_LIST_X;
                int listTop = getBuyBackgroundY() + BUY_OFFER_LIST_Y;

                if (mouseX < listLeft || mouseX >= listLeft + BUY_OFFER_ENTRY_WIDTH) {
                        return -1;
                }

                double localY = mouseY - listTop;
                if (localY < 0.0D) {
                        return -1;
                }

                int row = (int) (localY / BUY_OFFER_ENTRY_HEIGHT);
                if (row >= BUY_MAX_VISIBLE_OFFERS) {
                        return -1;
                }

                int offerIndex = scrollOffset + row;
                return offerIndex < getBuyOffers().size() ? offerIndex : -1;
        }

        private Optional<HoveredStack> getHoveredOfferStack(int mouseX, int mouseY) {
                int offerIndex = getOfferIndexAt(mouseX, mouseY);
                List<GearShopOffer> offers = getBuyOffers();
                if (offerIndex < 0 || offerIndex >= offers.size()) {
                        return Optional.empty();
                }

                int listLeft = getBuyBackgroundX() + BUY_OFFER_LIST_X;
                int listTop = getBuyBackgroundY() + BUY_OFFER_LIST_Y;
                int relativeMouseY = mouseY - listTop;
                if (relativeMouseY < 0) {
                        return Optional.empty();
                }

                int row = relativeMouseY / BUY_OFFER_ENTRY_HEIGHT;
                if (row >= BUY_MAX_VISIBLE_OFFERS) {
                        return Optional.empty();
                }

                int entryTop = listTop + row * BUY_OFFER_ENTRY_HEIGHT;
                int itemTop = entryTop + BUY_OFFER_ITEM_OFFSET_Y;
                if (mouseY < itemTop || mouseY >= itemTop + 16) {
                        return Optional.empty();
                }

                GearShopOffer offer = offers.get(offerIndex);
                int costStart = listLeft + BUY_OFFER_COST_ITEM_OFFSET_X;
                int arrowLeft = listLeft + BUY_OFFER_ARROW_OFFSET_X;
                int maxCostRight = arrowLeft - 2;
                List<ItemStack> costStacks = offer.costStacks();
                for (int costIndex = 0; costIndex < costStacks.size(); costIndex++) {
                        int costX = costStart + costIndex * BUY_OFFER_COST_ITEM_SPACING;
                        if (costX + 16 > maxCostRight) {
                                break;
                        }
                        if (mouseX >= costX && mouseX < costX + 16) {
                                return Optional.of(new HoveredStack(costStacks.get(costIndex).copy(), true));
                        }
                }

                int resultLeft = listLeft + BUY_OFFER_RESULT_ITEM_OFFSET_X;
                if (mouseX >= resultLeft && mouseX < resultLeft + 16) {
                        return Optional.of(new HoveredStack(offer.copyResultStack(), false));
                }

                return Optional.empty();
        }

        private void selectOffer(int offerIndex) {
                selectedOffer = MathHelper.clamp(offerIndex, -1, getBuyOffers().size() - 1);
        }

        private void drawCostTooltip(DrawContext context, ItemStack stack, int mouseX, int mouseY) {
                List<Text> tooltip = new ArrayList<>(getTooltipFromItem(stack));
                int requested = GearShopStackHelper.getRequestedCount(stack);
                if (requested > stack.getCount()) {
                        tooltip.add(Text.translatable("screen.gardenkingmod.market.cost_count", requested));
                }
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
        }

        private static String formatRequestedCount(int count) {
                if (count < 1000) {
                        return Integer.toString(count);
                }

                double value = count;
                char[] suffixes = { 'k', 'M', 'B', 'T' };
                int suffixIndex = -1;
                while (value >= 1000.0 && suffixIndex + 1 < suffixes.length) {
                        value /= 1000.0;
                        suffixIndex++;
                }

                if (suffixIndex < 0) {
                        return Integer.toString(count);
                }

                String format = value >= 100.0 ? "%.0f" : "%.1f";
                String number = String.format(java.util.Locale.ROOT, format, value);
                if (number.endsWith(".0")) {
                        number = number.substring(0, number.length() - 2);
                }
                return number + suffixes[suffixIndex];
        }

        private List<GearShopOffer> getBuyOffers() {
                return GearShopOfferManager.getInstance().getOffers();
        }

        private void playClickSound() {
                if (client == null) {
                        return;
                }
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        private record HoveredStack(ItemStack stack, boolean isCostStack) {
        }

        private enum Tab {
                SELL("screen.gardenkingmod.market.tab.sell"),
                BUY("screen.gardenkingmod.market.tab.buy");

                private final String translationKey;

                Tab(String translationKey) {
                        this.translationKey = translationKey;
                }
        }
}
