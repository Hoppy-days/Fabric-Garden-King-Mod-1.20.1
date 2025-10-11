package net.jeremy.gardenkingmod.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.client.gui.toast.SaleResultToast;
import net.jeremy.gardenkingmod.shop.GearShopOffer;
import net.jeremy.gardenkingmod.shop.GardenMarketOfferManager;
import net.jeremy.gardenkingmod.shop.GearShopStackHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
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
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;

public class MarketScreen extends HandledScreen<MarketScreenHandler> {
        private static final Identifier SELL_TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/market_sell_gui.png");
        private static final Identifier BUY_TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/market_buy_gui.png");

        private static final int SELL_TEXTURE_WIDTH = 256;
        private static final int SELL_TEXTURE_HEIGHT = 256;

        private static final int SELL_BACKGROUND_U = 0;
        private static final int SELL_BACKGROUND_V = 0;
        private static final int SELL_BACKGROUND_WIDTH = 176;
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

        private static final int BUY_HEADER_COLOR = 0x404040;
        private static final int BUY_OFFERS_LABEL_X = 6;
        private static final int BUY_OFFERS_LABEL_Y = 22;

        private static final int BUY_OFFER_LIST_X = 5;
        private static final int BUY_OFFER_LIST_Y = 32;
        private static final int BUY_OFFER_ENTRY_WIDTH = 88;
        private static final int BUY_OFFER_ENTRY_HEIGHT = 20;
        private static final int BUY_OFFER_LIST_HEIGHT = 180;
        private static final int BUY_MAX_VISIBLE_OFFERS = BUY_OFFER_LIST_HEIGHT / BUY_OFFER_ENTRY_HEIGHT;
        private static final int BUY_OFFER_ITEM_OFFSET_Y = 2;
        private static final int BUY_OFFER_COST_ITEM_OFFSET_X = 6;
        private static final int BUY_OFFER_COST_ITEM_SPACING = 22;
        private static final int BUY_OFFER_ARROW_OFFSET_X = 53;
        private static final int BUY_OFFER_ARROW_OFFSET_Y = 6;
        private static final int BUY_OFFER_RESULT_ITEM_OFFSET_X = 68;
        private static final int BUY_OFFER_BACKGROUND_U = 277;
        private static final int BUY_OFFER_BACKGROUND_V = 15;
        private static final int BUY_OFFER_HOVER_BACKGROUND_V = 36;
        private static final int BUY_OFFER_ARROW_U = 301;
        private static final int BUY_OFFER_ARROW_V = 42;
        private static final int BUY_OFFER_ARROW_WIDTH = 10;
        private static final int BUY_OFFER_ARROW_HEIGHT = 9;
        private static final int SELECTED_HIGHLIGHT_COLOR = 0x40FFFFFF;

        private static final int SLOT_ICON_SIZE = 16;
        private static final int BUY_BUTTON_WIDTH = 46;
        private static final int BUY_BUTTON_HEIGHT = 14;
        private static final int BUY_BUTTON_U = 277;
        private static final int BUY_BUTTON_V = 67;
        private static final int BUY_BUTTON_HOVER_V = 82;
        private static final int BUY_BUTTON_CENTER_OFFSET_Y = 8;
        private static final int BUY_BUTTON_LABEL_COLOR = 0xFFFFFF;
        private static final Text BUY_BUTTON_TEXT = Text.literal("BUY");

        private static final int COST_TEXT_COLOR = 0xFFFFFF;
        private static final String COST_LABEL_TRANSLATION_KEY = "screen.gardenkingmod.gear_shop.cost_label";
        private static final int COST_SLOT_LABEL_ANCHOR_X = 9;
        private static final int COST_SLOT_LABEL_OFFSET_Y = 20;
        private static final int COST_SLOT_VALUE_ANCHOR_X = 9;
        private static final int COST_SLOT_VALUE_OFFSET_Y = 29;
        private static final float COST_SLOT_TEXT_SCALE = 0.8F;

        private static final float OFFER_DISPLAY_SCALE = 3.25F;
        private static final float OFFER_ROTATION_SPEED = 30.0F;
        private static final float OFFER_ROTATION_PERIOD_TICKS = 20.0F * (360.0F / OFFER_ROTATION_SPEED);
        private static final float RESULT_SLOT_BASE_Z = 200.0F;
        private static final float RESULT_SLOT_ANIMATION_SCALE = 1.0F;
        private static final float RESULT_SLOT_ANIMATION_OFFSET_X = 0.0F;
        private static final float RESULT_SLOT_ANIMATION_OFFSET_Y = -1.5F;
        private static final float RESULT_SLOT_ANIMATION_OFFSET_Z = 0.0F;
        private static final float RESULT_SLOT_ROTATION_PERIOD_TICKS = 90.0F;
        private static final float RESULT_SLOT_ROTATION_PHASE_TICKS = 0.0F;
        private static final RotationAxis RESULT_SLOT_ROTATION_AXIS = RotationAxis.POSITIVE_Y;
        private static final float RESULT_SLOT_STATIC_PITCH = 0.0F;
        private static final float RESULT_SLOT_STATIC_YAW = 0.0F;
        private static final float RESULT_SLOT_STATIC_ROLL = 0.0F;
        private static final float RESULT_SLOT_BOB_AMPLITUDE = 1.0F;
        private static final float RESULT_SLOT_BOB_OFFSET = 0.0F;
        private static final float RESULT_SLOT_BOB_PERIOD_TICKS = 20.0F;
        private static final float RESULT_SLOT_BOB_PHASE_TICKS = 0.0F;

        private static final OfferDisplayAnimation RESULT_SLOT_ANIMATION = buildAnimation(builder -> {
                builder.scale(RESULT_SLOT_ANIMATION_SCALE);
                builder.offset(RESULT_SLOT_ANIMATION_OFFSET_X, RESULT_SLOT_ANIMATION_OFFSET_Y,
                                RESULT_SLOT_ANIMATION_OFFSET_Z);
                builder.rotationAxis(RESULT_SLOT_ROTATION_AXIS);
                builder.rotationPeriodTicks(RESULT_SLOT_ROTATION_PERIOD_TICKS);
                builder.rotationPhaseTicks(RESULT_SLOT_ROTATION_PHASE_TICKS);
                builder.staticPitch(RESULT_SLOT_STATIC_PITCH);
                builder.staticYaw(RESULT_SLOT_STATIC_YAW);
                builder.staticRoll(RESULT_SLOT_STATIC_ROLL);
                builder.bobAmplitude(RESULT_SLOT_BOB_AMPLITUDE);
                builder.bobOffset(RESULT_SLOT_BOB_OFFSET);
                builder.bobPeriodTicks(RESULT_SLOT_BOB_PERIOD_TICKS);
                builder.bobPhaseTicks(RESULT_SLOT_BOB_PHASE_TICKS);
        });

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

        private static final int BUY_SCROLLBAR_OFFSET_X = 94;
        private static final int BUY_SCROLLBAR_OFFSET_Y = 32;
        private static final int BUY_SCROLLBAR_TRACK_WIDTH = 6;
        private static final int BUY_SCROLLBAR_TRACK_HEIGHT = BUY_OFFER_LIST_HEIGHT;
        private static final int BUY_SCROLLBAR_KNOB_U = 277;
        private static final int BUY_SCROLLBAR_KNOB_V = 97;
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
        private float lastRenderDelta;
        private float resultSlotAnimationStartTicks = Float.NaN;
        private ItemStack lastAnimatedResultSlotStack = ItemStack.EMPTY;

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
                        drawBuyButton(context, mouseX, mouseY);
                } else {
                        context.drawTexture(SELL_TEXTURE, x, y, SELL_BACKGROUND_U, SELL_BACKGROUND_V, backgroundWidth, backgroundHeight,
                                        SELL_TEXTURE_WIDTH, SELL_TEXTURE_HEIGHT);
                }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                lastRenderDelta = delta;
                renderBackground(context);
                if (sellButton != null) {
                        sellButton.visible = activeTab == Tab.SELL;
                        sellButton.active = sellButton.visible && handler.hasSellableItem();
                }
                if (activeTab == Tab.BUY) {
                        updateBuyScrollLimits();
                }
                ResultSlotSnapshot suppressedResult = activeTab == Tab.BUY ? suppressVanillaResultSlot() : null;
                List<CostSlotSnapshot> suppressedCounts = activeTab == Tab.BUY ? suppressVanillaCostCounts() : List.of();
                try {
                        super.render(context, mouseX, mouseY, delta);
                } finally {
                        if (activeTab == Tab.BUY) {
                                restoreVanillaCostCounts(suppressedCounts);
                                restoreVanillaResultSlot(suppressedResult);
                        }
                }
                if (activeTab == Tab.BUY) {
                        drawAnimatedResultSlot(context);
                        drawCostSlotOverlays(context);
                }
                drawMouseoverTooltip(context, mouseX, mouseY);
        }

        public void updateSaleResult(boolean success, int itemsSold, int payout, int lifetimeTotal, Text feedback,
                        Map<Item, Integer> soldItemCounts) {
                this.lastItemsSold = itemsSold;
                this.lastPayout = payout;
                this.lastLifetimeTotal = success && lifetimeTotal >= 0 ? lifetimeTotal : -1;

                if (success) {
                        MutableText payoutText = Text.literal(Integer.toString(payout)).formatted(Formatting.GREEN);
                        MutableText soldLine;
                        if (soldItemCounts != null && !soldItemCounts.isEmpty()) {
                                Text soldItemsText = buildSoldItemsText(soldItemCounts);
                                soldLine = Text.translatable("screen.gardenkingmod.market.sale_result_sold_detailed",
                                                soldItemsText);
                        } else {
                                soldLine = Text.translatable("screen.gardenkingmod.market.sale_result_sold", itemsSold);
                        }

                        MutableText earnedLine = Text
                                        .translatable("screen.gardenkingmod.market.sale_result_earned", payoutText);
                        MutableText saleResult = soldLine.copy();
                        if (!earnedLine.getString().isEmpty()) {
                                if (!saleResult.getString().isEmpty()) {
                                        saleResult.append(Text.literal("\n"));
                                }
                                saleResult.append(earnedLine);
                        }
                        this.saleResultLine = saleResult.formatted(Formatting.YELLOW);

                        if (this.lastLifetimeTotal >= 0) {
                                MutableText lifetimeText = Text.literal(Integer.toString(this.lastLifetimeTotal))
                                                .formatted(Formatting.GREEN);
                                this.lifetimeResultLine = Text.translatable(
                                                "screen.gardenkingmod.market.sale_result_lifetime", lifetimeText)
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

                showSaleResultToast();
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
                        if (isBuyButtonVisible()) {
                                getBuyButtonArea().ifPresent(area -> drawBuyButtonLabel(context,
                                                area.left() - getBuyBackgroundX(),
                                                area.top() - getBuyBackgroundY()));
                        }
                        context.getMatrices().pop();
                        return;
                }
        }

        private void showSaleResultToast() {
                if (client == null || client.getToastManager() == null) {
                        return;
                }

                if (saleResultLine == null || saleResultLine.getString().isEmpty()) {
                        return;
                }

                Text secondaryLine = lifetimeResultLine != null && !lifetimeResultLine.getString().isEmpty()
                                ? lifetimeResultLine
                                : Text.empty();
                client.getToastManager().add(new SaleResultToast(saleResultLine, secondaryLine));
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

                        if (isPointWithinBuyButton(mouseX, mouseY)) {
                                playClickSound();
                                attemptPurchase();
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
                        this.handler.setBuySlotsEnabled(tab == Tab.BUY);
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
                int buttonY = this.y + TAB_BUTTON_Y_OFFSET;
                if (sellTabButton != null) {
                        sellTabButton.reposition(this.x + SELL_TAB_TEXT_X - TAB_BUTTON_TEXT_PADDING_X, buttonY);
                }
                if (buyTabButton != null) {
                        buyTabButton.reposition(this.x + BUY_TAB_TEXT_X - TAB_BUTTON_TEXT_PADDING_X, buttonY);
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
                sendOfferSelectionToServer(-1);
        }

        private void drawBuyLabels(DrawContext context) {
                context.drawText(textRenderer, Text.translatable("screen.gardenkingmod.market.offers"),
                                BUY_OFFERS_LABEL_X, BUY_OFFERS_LABEL_Y, BUY_HEADER_COLOR, false);
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

                        int arrowY = entryY + BUY_OFFER_ARROW_OFFSET_Y;
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

        private boolean isBuyButtonVisible() {
                return activeTab == Tab.BUY && handler != null && handler.areBuySlotsEnabled()
                                && !getBuyOffers().isEmpty();
        }

        private Optional<ButtonArea> getBuyButtonArea() {
                if (handler == null) {
                        return Optional.empty();
                }

                double sumCenterX = 0.0;
                double centerY = Double.NaN;
                int count = 0;
                for (Slot slot : handler.slots) {
                        if (handler.isCostSlot(slot)) {
                                double slotCenterX = this.x + slot.x + SLOT_ICON_SIZE / 2.0;
                                double slotCenterY = this.y + slot.y + SLOT_ICON_SIZE / 2.0;
                                sumCenterX += slotCenterX;
                                centerY = slotCenterY;
                                count++;
                        }
                }

                if (count <= 0 || Double.isNaN(centerY)) {
                        return Optional.empty();
                }

                double centerX = sumCenterX / count;
                double buttonCenterY = centerY + BUY_BUTTON_CENTER_OFFSET_Y;
                int left = (int) Math.round(centerX - BUY_BUTTON_WIDTH / 2.0);
                int top = (int) Math.round(buttonCenterY - BUY_BUTTON_HEIGHT / 2.0);
                return Optional.of(new ButtonArea(left, top, BUY_BUTTON_WIDTH, BUY_BUTTON_HEIGHT));
        }

        private void attemptPurchase() {
                if (!isBuyButtonVisible() || client == null || client.interactionManager == null) {
                        return;
                }

                List<GearShopOffer> offers = getBuyOffers();
                if (selectedOffer < 0 || selectedOffer >= offers.size()) {
                        return;
                }

                client.interactionManager.clickButton(handler.syncId, MarketScreenHandler.BUTTON_BUY);
        }

        private void drawBuyButton(DrawContext context, int mouseX, int mouseY) {
                if (!isBuyButtonVisible()) {
                        return;
                }

                Optional<ButtonArea> area = getBuyButtonArea();
                if (area.isEmpty()) {
                        return;
                }

                ButtonArea button = area.get();
                int v = button.contains(mouseX, mouseY) ? BUY_BUTTON_HOVER_V : BUY_BUTTON_V;
                context.drawTexture(BUY_TEXTURE, button.left(), button.top(), BUY_BUTTON_U, v, BUY_BUTTON_WIDTH,
                                BUY_BUTTON_HEIGHT, BuyBackground.TEXTURE_WIDTH, BuyBackground.TEXTURE_HEIGHT);
        }

        private boolean isPointWithinBuyButton(double mouseX, double mouseY) {
                if (!isBuyButtonVisible()) {
                        return false;
                }

                return getBuyButtonArea().map(area -> area.contains(mouseX, mouseY)).orElse(false);
        }

        private void drawBuyButtonLabel(DrawContext context, int relativeLeft, int relativeTop) {
                int textWidth = textRenderer.getWidth(BUY_BUTTON_TEXT);
                int textHeight = textRenderer.fontHeight;
                int textX = relativeLeft + (BUY_BUTTON_WIDTH - textWidth) / 2;
                int textY = relativeTop + (BUY_BUTTON_HEIGHT - textHeight) / 2;
                context.drawText(textRenderer, BUY_BUTTON_TEXT, textX, textY, BUY_BUTTON_LABEL_COLOR, false);
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
                int clamped = MathHelper.clamp(offerIndex, -1, getBuyOffers().size() - 1);
                if (this.selectedOffer != clamped) {
                        this.selectedOffer = clamped;
                        sendOfferSelectionToServer(clamped);
                } else if (clamped >= 0) {
                        sendOfferSelectionToServer(clamped);
                }
        }

        private void sendOfferSelectionToServer(int offerIndex) {
                if (handler == null || client == null || client.interactionManager == null) {
                        return;
                }

                int buttonId = MarketScreenHandler.encodeSelectBuyOfferButtonId(offerIndex);
                client.interactionManager.clickButton(handler.syncId, buttonId);
        }

        private void drawCostSlotOverlays(DrawContext context) {
                if (handler == null) {
                        return;
                }

                String label = Text.translatable(COST_LABEL_TRANSLATION_KEY).getString();
                for (Slot slot : handler.slots) {
                        if (handler.isCostSlot(slot) && slot.isEnabled() && slot.hasStack()) {
                                int slotX = this.x + slot.x;
                                int slotY = this.y + slot.y;
                                ItemStack stack = slot.getStack();
                                context.drawItem(stack, slotX, slotY);
                                drawCostSlotText(context, label, stack, slotX, slotY);
                        }
                }
        }

        private void drawCostSlotText(DrawContext context, String label, ItemStack stack, int slotX, int slotY) {
                int requiredCount = GearShopStackHelper.getRequestedCount(stack);
                if (requiredCount <= 0) {
                        return;
                }

                String valueText = formatRequestedCount(requiredCount);

                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                drawCostTextLine(context, label, slotX + COST_SLOT_LABEL_ANCHOR_X,
                                slotY + COST_SLOT_LABEL_OFFSET_Y, COST_SLOT_TEXT_SCALE);
                drawCostTextLine(context, valueText, slotX + COST_SLOT_VALUE_ANCHOR_X,
                                slotY + COST_SLOT_VALUE_OFFSET_Y, COST_SLOT_TEXT_SCALE);

                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
        }

        private void drawCostTextLine(DrawContext context, String text, int anchorX, int baselineY, float scale) {
                if (text == null || text.isEmpty()) {
                        return;
                }

                float scaledWidth = textRenderer.getWidth(text) * scale;
                float drawX = anchorX - scaledWidth / 2.0F;
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.translate(drawX, baselineY, 300.0F);
                matrices.scale(scale, scale, 1.0F);
                context.drawText(textRenderer, text, 0, 0, COST_TEXT_COLOR, false);
                matrices.pop();
        }

        private void drawAnimatedResultSlot(DrawContext context) {
                Slot resultSlot = getResultSlot();
                if (resultSlot == null || !resultSlot.isEnabled()) {
                        return;
                }

                ItemStack stack = resultSlot.getStack();
                if (stack.isEmpty()) {
                        resetResultSlotAnimation();
                        return;
                }

                if (!ItemStack.areEqual(lastAnimatedResultSlotStack, stack)
                                || stack.getCount() != lastAnimatedResultSlotStack.getCount()) {
                        lastAnimatedResultSlotStack = stack.copy();
                        resultSlotAnimationStartTicks = Float.NaN;
                }

                if (Float.isNaN(resultSlotAnimationStartTicks)) {
                        resultSlotAnimationStartTicks = getAnimationTicks(0.0F);
                }

                float animationTicks = getAnimationTicks(lastRenderDelta) - resultSlotAnimationStartTicks;
                if (animationTicks < 0.0F) {
                        animationTicks = 0.0F;
                }

                int slotLeft = this.x + resultSlot.x;
                int slotTop = this.y + resultSlot.y;
                float slotCenterX = slotLeft + 8.0F;
                float slotCenterY = slotTop + 8.0F;

                OfferDisplayAnimation animation = RESULT_SLOT_ANIMATION;
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.translate(slotCenterX + animation.offsetX(), slotCenterY + animation.offsetY(),
                                RESULT_SLOT_BASE_Z + animation.offsetZ());

                float bobTranslation = animation.bobOffset();
                if (animation.bobPeriodTicks() > 0.0F && animation.bobAmplitude() != 0.0F) {
                        float bobAngle = (animationTicks + animation.bobPhaseTicks()) / animation.bobPeriodTicks();
                        bobTranslation += MathHelper.sin(bobAngle) * animation.bobAmplitude();
                }
                if (bobTranslation != 0.0F) {
                        matrices.translate(0.0F, bobTranslation, 0.0F);
                }

                float scale = animation.scale();
                if (scale != 1.0F) {
                        matrices.scale(scale, scale, scale);
                }

                if (animation.rotationPeriodTicks() > 0.0F) {
                        float rotationDegrees = ((animationTicks + animation.rotationPhaseTicks())
                                        / animation.rotationPeriodTicks()) * 360.0F;
                        matrices.multiply(animation.rotationAxis().rotationDegrees(rotationDegrees));
                }

                if (animation.staticYaw() != 0.0F) {
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(animation.staticYaw()));
                }
                if (animation.staticPitch() != 0.0F) {
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(animation.staticPitch()));
                }
                if (animation.staticRoll() != 0.0F) {
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(animation.staticRoll()));
                }

                drawResultStack(context, stack);
                matrices.pop();

                context.drawItemInSlot(textRenderer, stack, slotLeft, slotTop);
        }

        private void drawResultStack(DrawContext context, ItemStack stack) {
                MinecraftClient minecraftClient = client;
                if (minecraftClient == null) {
                        return;
                }

                ItemRenderer renderer = minecraftClient.getItemRenderer();
                BakedModel model = renderer.getModel(stack, null, null, 0);
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.scale(16.0F, -16.0F, 16.0F);
                boolean disableLighting = !model.isSideLit();
                if (disableLighting) {
                        DiffuseLighting.disableGuiDepthLighting();
                }
                renderer.renderItem(stack, ModelTransformationMode.GUI, false, matrices, context.getVertexConsumers(),
                                LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, model);
                context.draw();
                if (disableLighting) {
                        DiffuseLighting.enableGuiDepthLighting();
                }
                matrices.pop();
        }

        private Slot getResultSlot() {
                for (Slot slot : handler.slots) {
                        if (handler.isResultSlot(slot)) {
                                return slot;
                        }
                }
                return null;
        }

        private void resetResultSlotAnimation() {
                resultSlotAnimationStartTicks = Float.NaN;
                lastAnimatedResultSlotStack = ItemStack.EMPTY;
        }

        private float getAnimationTicks(float delta) {
                MinecraftClient minecraftClient = client;
                if (minecraftClient != null && minecraftClient.world != null) {
                        return minecraftClient.world.getTime() + delta;
                }
                return Util.getMeasuringTimeMs() / 50.0F;
        }

        private ResultSlotSnapshot suppressVanillaResultSlot() {
                Slot resultSlot = getResultSlot();
                if (resultSlot == null) {
                        return null;
                }

                ItemStack stack = resultSlot.getStack();
                if (stack.isEmpty()) {
                        return null;
                }

                int originalCount = stack.getCount();
                stack.setCount(0);
                return new ResultSlotSnapshot(resultSlot, stack, originalCount);
        }

        private void restoreVanillaResultSlot(ResultSlotSnapshot snapshot) {
                if (snapshot == null) {
                        return;
                }

                if (snapshot.slot().getStack() == snapshot.stack()) {
                        snapshot.stack().setCount(snapshot.originalCount());
                }
        }

        private List<CostSlotSnapshot> suppressVanillaCostCounts() {
                if (handler == null) {
                        return List.of();
                }

                List<CostSlotSnapshot> modified = new ArrayList<>();
                for (Slot slot : handler.slots) {
                        if (!handler.isCostSlot(slot)) {
                                continue;
                        }

                        ItemStack stack = slot.getStack();
                        if (stack.isEmpty()) {
                                continue;
                        }

                        int originalCount = stack.getCount();
                        int requested = GearShopStackHelper.getRequestedCount(stack);
                        if (requested <= stack.getCount()) {
                                continue;
                        }

                        stack.setCount(Math.min(requested, stack.getMaxCount()));
                        modified.add(new CostSlotSnapshot(slot, stack, originalCount));
                }
                return modified;
        }

        private void restoreVanillaCostCounts(List<CostSlotSnapshot> snapshots) {
                if (snapshots == null) {
                        return;
                }

                for (CostSlotSnapshot snapshot : snapshots) {
                        if (snapshot.slot().getStack() == snapshot.stack()) {
                                snapshot.stack().setCount(snapshot.originalCount());
                        }
                }
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
                return GardenMarketOfferManager.getInstance().getOffers();
        }

        private void playClickSound() {
                if (client == null) {
                        return;
                }
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        private record ButtonArea(int left, int top, int width, int height) {
                boolean contains(double x, double y) {
                        return x >= left && x < left + width && y >= top && y < top + height;
                }
        }

        private record ResultSlotSnapshot(Slot slot, ItemStack stack, int originalCount) {
        }

        private record CostSlotSnapshot(Slot slot, ItemStack stack, int originalCount) {
        }

        private record OfferDisplayAnimation(float scale, float offsetX, float offsetY, float offsetZ,
                        float rotationPeriodTicks, float rotationPhaseTicks, RotationAxis rotationAxis,
                        float staticPitch, float staticYaw, float staticRoll, float bobAmplitude, float bobOffset,
                        float bobPeriodTicks, float bobPhaseTicks) {
                static Builder builder() {
                        return new Builder();
                }

                static final class Builder {
                        private float scale = OFFER_DISPLAY_SCALE;
                        private float offsetX;
                        private float offsetY;
                        private float offsetZ;
                        private float rotationPeriodTicks = OFFER_ROTATION_PERIOD_TICKS;
                        private float rotationPhaseTicks;
                        private RotationAxis rotationAxis = RotationAxis.POSITIVE_Y;
                        private float staticPitch = 25.0F;
                        private float staticYaw;
                        private float staticRoll;
                        private float bobAmplitude;
                        private float bobOffset;
                        private float bobPeriodTicks;
                        private float bobPhaseTicks;

                        Builder scale(float scale) {
                                this.scale = scale;
                                return this;
                        }

                        Builder offset(float x, float y, float z) {
                                this.offsetX = x;
                                this.offsetY = y;
                                this.offsetZ = z;
                                return this;
                        }

                        Builder offsetX(float x) {
                                this.offsetX = x;
                                return this;
                        }

                        Builder offsetY(float y) {
                                this.offsetY = y;
                                return this;
                        }

                        Builder offsetZ(float z) {
                                this.offsetZ = z;
                                return this;
                        }

                        Builder rotationPeriodTicks(float periodTicks) {
                                this.rotationPeriodTicks = periodTicks;
                                return this;
                        }

                        Builder rotationPhaseTicks(float phaseTicks) {
                                this.rotationPhaseTicks = phaseTicks;
                                return this;
                        }

                        Builder rotationAxis(RotationAxis axis) {
                                this.rotationAxis = axis;
                                return this;
                        }

                        Builder staticPitch(float pitch) {
                                this.staticPitch = pitch;
                                return this;
                        }

                        Builder staticYaw(float yaw) {
                                this.staticYaw = yaw;
                                return this;
                        }

                        Builder staticRoll(float roll) {
                                this.staticRoll = roll;
                                return this;
                        }

                        Builder bobAmplitude(float amplitude) {
                                this.bobAmplitude = amplitude;
                                return this;
                        }

                        Builder bobOffset(float offset) {
                                this.bobOffset = offset;
                                return this;
                        }

                        Builder bobPeriodTicks(float periodTicks) {
                                this.bobPeriodTicks = periodTicks;
                                return this;
                        }

                        Builder bobPhaseTicks(float phaseTicks) {
                                this.bobPhaseTicks = phaseTicks;
                                return this;
                        }

                        OfferDisplayAnimation build() {
                                return new OfferDisplayAnimation(scale, offsetX, offsetY, offsetZ, rotationPeriodTicks,
                                                rotationPhaseTicks, rotationAxis, staticPitch, staticYaw, staticRoll,
                                                bobAmplitude, bobOffset, bobPeriodTicks, bobPhaseTicks);
                        }
                }
        }

        private static OfferDisplayAnimation buildAnimation(Consumer<OfferDisplayAnimation.Builder> configurer) {
                OfferDisplayAnimation.Builder builder = OfferDisplayAnimation.builder();
                configurer.accept(builder);
                return builder.build();
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
