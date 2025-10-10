package net.jeremy.gardenkingmod.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.shop.GardenShopOffer;
import net.jeremy.gardenkingmod.shop.GardenShopStackHelper;
import net.jeremy.gardenkingmod.screen.inventory.GardenShopCostInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class GardenShopScreen extends HandledScreen<GardenShopScreenHandler> {
        private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/garden_shop_gui.png");
        private static final Identifier[] PAGE_TEXTURES = {
                        TEXTURE,
                        new Identifier(GardenKingMod.MOD_ID, "textures/gui/container/garden_shop_gui2.png"),
                        new Identifier(GardenKingMod.MOD_ID, "textures/gui/container/garden_shop_gui3.png"),
                        new Identifier(GardenKingMod.MOD_ID, "textures/gui/container/garden_shop_gui4.png"),
                        new Identifier(GardenKingMod.MOD_ID, "textures/gui/container/garden_shop_gui5.png") };
        private static final int TEXTURE_WIDTH = 512;
        private static final int TEXTURE_HEIGHT = 256;

        private static final int BACKGROUND_WIDTH = 300;
        private static final int BACKGROUND_HEIGHT = 204;
        private static final int PLAYER_INVENTORY_LABEL_Y = 112;
        private static final int PLAYER_INVENTORY_LABEL_X = 134;
        private static final int TITLE_X = 180;
        private static final int TITLE_Y = 6;

        private static final int OFFERS_LABEL_X = 32;
        private static final int OFFERS_LABEL_Y = 6;
        private static final int BUY_LABEL_X = 203;
        private static final int BUY_LABEL_Y = 91;
        private static final int BUY_BUTTON_OFFSET_X = 188;
        private static final int BUY_BUTTON_OFFSET_Y = 88;
        private static final int BUY_BUTTON_U = 301;
        private static final int BUY_BUTTON_V = 81;
        private static final int BUY_BUTTON_HOVER_V = 96;
        private static final int BUY_BUTTON_WIDTH = 46;
        private static final int BUY_BUTTON_HEIGHT = 14;
        private static final float BUY_LABEL_SCALE = 0.9F;

        private static final int OFFER_LIST_X = 29;
        private static final int OFFER_LIST_Y = 17;
        private static final int OFFER_ENTRY_WIDTH = 88;
        private static final int OFFER_ENTRY_HEIGHT = 20;
        private static final int OFFER_LIST_HEIGHT = 180;
        private static final int MAX_VISIBLE_OFFERS = OFFER_LIST_HEIGHT / OFFER_ENTRY_HEIGHT;
        private static final int OFFER_ITEM_OFFSET_Y = 2;
        private static final int OFFER_COST_ITEM_OFFSET_X = 6;
        private static final int OFFER_COST_ITEM_SPACING = 22;
        private static final int OFFER_RESULT_ITEM_OFFSET_X = 68;
        private static final int OFFER_BACKGROUND_U = 301;
        private static final int OFFER_BACKGROUND_V = 0;
        private static final int OFFER_HOVER_BACKGROUND_V = 21;
        private static final int OFFER_ARROW_U = 301;
        private static final int OFFER_ARROW_V = 42;
        private static final int OFFER_ARROW_WIDTH = 10;
        private static final int OFFER_ARROW_HEIGHT = 9;
        private static final int OFFER_ARROW_OFFSET_X = 53;
        private static final int OFFER_ARROW_OFFSET_Y = 6;
        private static final int COST_TEXT_COLOR = 0xFF0000;
        private static final String COST_LABEL_TRANSLATION_KEY = "screen.gardenkingmod.garden_shop.cost_label";
            //DEFAULT_COST_SLOT_LABEL is the word "COST"
        private static final int DEFAULT_COST_SLOT_LABEL_ANCHOR_X = 8;
        private static final int DEFAULT_COST_SLOT_LABEL_OFFSET_Y = 20;
            //DEFAULT_COST_SLOT_VALUE is the cost item count
        private static final int DEFAULT_COST_SLOT_VALUE_ANCHOR_X = 8;
        private static final int DEFAULT_COST_SLOT_VALUE_OFFSET_Y = 30;
        private static final float DEFAULT_COST_SLOT_TEXT_SCALE = 0.6F;

        private static final PageLayout DEFAULT_PAGE_LAYOUT = buildLayout(builder -> {
        });
        /**
         * Layout configuration for page 1 (garden_shop_gui.png). Adjust the builder calls
         * in this lambda to reposition elements for that page without impacting the
         * others.
         */
        private static final PageLayout PAGE1_LAYOUT = buildLayout(builder -> {
            /* Offer list placement */
                builder.offerList(OFFER_LIST_X, OFFER_LIST_Y);
                /* Cost stack spacing */
                builder.costStacks(OFFER_COST_ITEM_OFFSET_X, OFFER_COST_ITEM_SPACING);
                /* Cost text placement */
                builder.costSlotText(DEFAULT_COST_SLOT_LABEL_ANCHOR_X, DEFAULT_COST_SLOT_LABEL_OFFSET_Y,
                                DEFAULT_COST_SLOT_VALUE_ANCHOR_X, DEFAULT_COST_SLOT_VALUE_OFFSET_Y,
                                DEFAULT_COST_SLOT_TEXT_SCALE);
                /* Result slot position */
                builder.resultItem(OFFER_RESULT_ITEM_OFFSET_X);
                /* Arrow position */
                builder.arrow(OFFER_ARROW_OFFSET_X, OFFER_ARROW_OFFSET_Y);
                /* Buy button placement & size */
                builder.buyButton(BUY_BUTTON_OFFSET_X, BUY_BUTTON_OFFSET_Y, BUY_BUTTON_WIDTH, BUY_BUTTON_HEIGHT);
                /* Buy label position */
                builder.buyLabel(BUY_LABEL_X, BUY_LABEL_Y);
        });
        /**
         * Layout configuration for page 2 (garden_shop_gui2.png). Start with the same
         * defaults as page 1, then tweak any of the values inside this lambda to match
         * that texture's custom layout.
         */
        private static final PageLayout PAGE2_LAYOUT = buildLayout(builder -> {
                int BUY_BUTTON_OFFSET_X = 148;
                int BUY_BUTTON_OFFSET_Y = 94;
                int BUY_LABEL_X = 163;
                int BUY_LABEL_Y = 97;
                /* Offer list placement */
                builder.offerList(OFFER_LIST_X, OFFER_LIST_Y);
                /* Cost stack spacing */
                builder.costStacks(OFFER_COST_ITEM_OFFSET_X, OFFER_COST_ITEM_SPACING);
                /* Cost text placement */
                builder.costSlotText(DEFAULT_COST_SLOT_LABEL_ANCHOR_X, DEFAULT_COST_SLOT_LABEL_OFFSET_Y,
                                DEFAULT_COST_SLOT_VALUE_ANCHOR_X, DEFAULT_COST_SLOT_VALUE_OFFSET_Y,
                                DEFAULT_COST_SLOT_TEXT_SCALE);
                /* Result slot position */
                builder.resultItem(OFFER_RESULT_ITEM_OFFSET_X);
                /* Arrow position */
                builder.arrow(OFFER_ARROW_OFFSET_X, OFFER_ARROW_OFFSET_Y);
                /* Buy button placement & size */
                builder.buyButton(BUY_BUTTON_OFFSET_X, BUY_BUTTON_OFFSET_Y, BUY_BUTTON_WIDTH, BUY_BUTTON_HEIGHT);
                /* Buy label position */
                builder.buyLabel(BUY_LABEL_X, BUY_LABEL_Y);

        });
        /**
         * Page-specific layout overrides for offer list item positions. Update the
         * existing layout lambdas or add new {@code buildLayout(...)} constants (e.g.
         * PAGE3_LAYOUT) and append them here to move slots for a given page without
         * affecting the others.
         */
        private static final PageLayout[] PAGE_LAYOUTS = { PAGE1_LAYOUT, PAGE2_LAYOUT, DEFAULT_PAGE_LAYOUT,
                        DEFAULT_PAGE_LAYOUT, DEFAULT_PAGE_LAYOUT };

        private static PageLayout buildLayout(Consumer<PageLayout.Builder> configurer) {
                PageLayout.Builder builder = PageLayout.defaults();
                configurer.accept(builder);
                return builder.build();
        }

        private static final float OFFER_DISPLAY_SCALE = 3.25F;
        private static final float OFFER_ROTATION_SPEED = 30.0F;
        private static final float OFFER_ROTATION_PERIOD_TICKS = 20.0F * (360.0F / OFFER_ROTATION_SPEED);

        private static final float RESULT_SLOT_ANIMATION_SCALE = 1.35F;
        private static final float RESULT_SLOT_ANIMATION_OFFSET_X = 0.0F;
        private static final float RESULT_SLOT_ANIMATION_OFFSET_Y = -1.5F;
        private static final float RESULT_SLOT_ANIMATION_OFFSET_Z = 0.0F;
        private static final float RESULT_SLOT_BASE_Z = 200.0F;
        private static final float RESULT_SLOT_ROTATION_PERIOD_TICKS = 60.0F;
        private static final float RESULT_SLOT_ROTATION_PHASE_TICKS = 0.0F;
        private static final RotationAxis RESULT_SLOT_ROTATION_AXIS = RotationAxis.POSITIVE_Y;
        private static final float RESULT_SLOT_STATIC_PITCH = 20.0F;
        private static final float RESULT_SLOT_STATIC_YAW = 0.0F;
        private static final float RESULT_SLOT_STATIC_ROLL = 0.0F;
        private static final float RESULT_SLOT_BOB_AMPLITUDE = 1.0F;
        private static final float RESULT_SLOT_BOB_OFFSET = 0.0F;
        private static final float RESULT_SLOT_BOB_PERIOD_TICKS = 40.0F;
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

        private static final int TAB_X = 0;
        private static final int TAB_WIDTH = 24;
        private static final int TAB_HEIGHT = 28;
        private static final int TAB_ICON_SIZE = 16;
        private static final int TAB_ICON_OFFSET_X = (TAB_WIDTH - TAB_ICON_SIZE) / 2 + 1;
        private static final int TAB_ICON_OFFSET_Y = (TAB_HEIGHT - TAB_ICON_SIZE) / 2;
        private static final int TAB_HOVER_U = 301;
        private static final int TAB_HOVER_V = 52;
        private static final TabDefinition[] TAB_DEFINITIONS = {
                        new TabDefinition(16, 30, 205),
                        new TabDefinition(46, 46, 205),
                        new TabDefinition(76, 62, 205),
                        new TabDefinition(106, 78, 205),
                        new TabDefinition(136, 94, 205) };

        private static final int SCROLLBAR_OFFSET_X = 118;
        private static final int SCROLLBAR_OFFSET_Y = 17;
        private static final int SCROLLBAR_TRACK_WIDTH = 6;
        private static final int SCROLLBAR_TRACK_HEIGHT = OFFER_LIST_HEIGHT;
        private static final int SCROLLBAR_KNOB_U = 24;
        private static final int SCROLLBAR_KNOB_V = 207;
        private static final int SCROLLBAR_KNOB_WIDTH = 6;
        private static final int SCROLLBAR_KNOB_HEIGHT = 27;

        private static final int SELECTED_HIGHLIGHT_COLOR = 0x40FFFFFF;

        private int maxScrollSteps;
        private int scrollOffset;
        private float scrollAmount;
        private boolean scrollbarDragging;
        private int selectedOffer = -1;
        private int lastOfferCount = -1;
        private int activeTab = 0;
        private float resultSlotAnimationStartTicks = Float.NaN;
        private ItemStack lastAnimatedResultSlotStack = ItemStack.EMPTY;

        public GardenShopScreen(GardenShopScreenHandler handler, PlayerInventory inventory, Text title) {
                super(handler, inventory, title);
                this.backgroundWidth = BACKGROUND_WIDTH;
                this.backgroundHeight = BACKGROUND_HEIGHT;
                this.playerInventoryTitleY = PLAYER_INVENTORY_LABEL_Y;
                this.playerInventoryTitleX = PLAYER_INVENTORY_LABEL_X;
                this.titleX = TITLE_X;
                this.titleY = TITLE_Y;
        }

        @Override
        protected void init() {
                super.init();
                handler.setDisplayedPage(activeTab);
                updateScrollLimits();
                lastOfferCount = getOffersForActiveTab().size();
        }

        @Override
        protected void handledScreenTick() {
                super.handledScreenTick();

                int currentOfferCount = getOffersForActiveTab().size();
                if (currentOfferCount != lastOfferCount) {
                        updateScrollLimits();
                        lastOfferCount = currentOfferCount;
                }
        }

        @Override
        protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
                int originX = x;
                int originY = y;
                Identifier backgroundTexture = getBackgroundTexture();
                context.drawTexture(backgroundTexture, originX, originY, 0, 0, backgroundWidth, backgroundHeight,
                                TEXTURE_WIDTH,
                                TEXTURE_HEIGHT);

                drawBuyButton(context, originX, originY, mouseX, mouseY);
                drawTabs(context, originX, originY, mouseX, mouseY);
                drawOfferList(context, originX, originY, mouseX, mouseY);
                drawScrollbar(context, originX, originY);
        }

        @Override
        protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
                super.drawForeground(context, mouseX, mouseY);
                PageLayout layout = getPageLayout();
                context.drawText(textRenderer, Text.translatable("screen.gardenkingmod.garden_shop.offers"), OFFERS_LABEL_X,
                                OFFERS_LABEL_Y, 0x404040, false);
                if (isBuyButtonVisible()) {
                        Text buyText = Text.translatable("screen.gardenkingmod.garden_shop.buy_button");
                        MatrixStack matrices = context.getMatrices();
                        matrices.push();
                        float scale = BUY_LABEL_SCALE;
                        double labelX = layout.buyLabelX();
                        double labelY = layout.buyLabelY();
                        if (scale != 1.0F) {
                                float textWidth = textRenderer.getWidth(buyText);
                                float textHeight = textRenderer.fontHeight;
                                labelX += (1.0F - scale) * textWidth / 2.0F;
                                labelY += (1.0F - scale) * textHeight / 2.0F;
                                matrices.translate(labelX, labelY, 0.0F);
                                matrices.scale(scale, scale, 1.0F);
                                context.drawText(textRenderer, buyText, 0, 0, 0xFFFFFF, false);
                        } else {
                                context.drawText(textRenderer, buyText, layout.buyLabelX(), layout.buyLabelY(), 0xFFFFFF,
                                                false);
                        }
                        matrices.pop();
                }
        }

        private float lastRenderDelta;

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                lastRenderDelta = delta;
                renderBackground(context);
                ResultSlotSnapshot suppressedResult = suppressVanillaResultSlot();
                List<CostSlotSnapshot> suppressedCounts = suppressVanillaCostCounts();
                try {
                        super.render(context, mouseX, mouseY, delta);
                } finally {
                        restoreVanillaCostCounts(suppressedCounts);
                        restoreVanillaResultSlot(suppressedResult);
                }
                drawAnimatedResultSlot(context);
                drawCostSlotOverlays(context);
                drawMouseoverTooltip(context, mouseX, mouseY);
        }


        private void drawCostSlotOverlays(DrawContext context) {
                PageLayout layout = getPageLayout();
                String label = Text.translatable(COST_LABEL_TRANSLATION_KEY).getString();
                for (Slot slot : handler.slots) {
                        if (slot.inventory instanceof GardenShopCostInventory && slot.hasStack()) {
                                int slotX = this.x + slot.x;
                                int slotY = this.y + slot.y;
                                ItemStack stack = slot.getStack();
                                context.drawItem(stack, slotX, slotY);
                                drawCostSlotText(context, label, stack, slotX, slotY, layout);
                        }
                }
        }

        @Override
        protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
                super.drawMouseoverTooltip(context, mouseX, mouseY);
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
                if (button == 0) {
                        int tabIndex = getTabIndexAt(mouseX, mouseY);
                        if (tabIndex >= 0) {
                                setActiveTab(tabIndex);
                                return true;
                        }

                        if (isPointWithinScrollbar(mouseX, mouseY)) {
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
                if (scrollbarDragging) {
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
                if (!canScroll()) {
                        return super.mouseScrolled(mouseX, mouseY, amount);
                }

                float scrollDelta = (float) (amount / (double) Math.max(maxScrollSteps, 1));
                setScrollAmount(scrollAmount - scrollDelta);
                return true;
        }

        private void drawOfferList(DrawContext context, int originX, int originY, int mouseX, int mouseY) {
                PageLayout layout = getPageLayout();
                List<GardenShopOffer> offers = getOffersForActiveTab();
                int listLeft = originX + layout.offerListX();
                int listTop = originY + layout.offerListY();
                int hoveredOffer = getOfferIndexAt(mouseX, mouseY);
                int clampedVisibleOffers = Math.min(MAX_VISIBLE_OFFERS, Math.max(offers.size() - scrollOffset, 0));
                int scissorHeight = Math.min(MAX_VISIBLE_OFFERS, offers.size()) * OFFER_ENTRY_HEIGHT;

                if (scissorHeight <= 0) {
                        return;
                }

                context.enableScissor(listLeft, listTop, listLeft + OFFER_ENTRY_WIDTH, listTop + scissorHeight);
                for (int visibleRow = 0; visibleRow < clampedVisibleOffers; visibleRow++) {
                        int offerIndex = scrollOffset + visibleRow;

                        int entryY = listTop + visibleRow * OFFER_ENTRY_HEIGHT;
                        int backgroundV = offerIndex == hoveredOffer ? OFFER_HOVER_BACKGROUND_V : OFFER_BACKGROUND_V;
                        context.drawTexture(TEXTURE, listLeft, entryY, OFFER_BACKGROUND_U, backgroundV, OFFER_ENTRY_WIDTH,
                                        OFFER_ENTRY_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

                        if (offerIndex == selectedOffer) {
                                context.fill(listLeft, entryY, listLeft + OFFER_ENTRY_WIDTH, entryY + OFFER_ENTRY_HEIGHT,
                                                SELECTED_HIGHLIGHT_COLOR);
                        }

                        GardenShopOffer offer = offers.get(offerIndex);
                        int itemY = entryY + OFFER_ITEM_OFFSET_Y;
                        int costStartX = listLeft + layout.costItemOffsetX();
                        int arrowX = listLeft + layout.arrowOffsetX();
                        int maxCostRight = arrowX - 2;
                        List<ItemStack> costStacks = offer.costStacks();
                        for (int costIndex = 0; costIndex < costStacks.size(); costIndex++) {
                                int costX = costStartX + costIndex * layout.costItemSpacing();
                                if (costX + 16 > maxCostRight) {
                                        break;
                                }
                                ItemStack costStack = costStacks.get(costIndex);
                                drawCostStack(context, costStack, costX, itemY);
                        }

                        int arrowY = entryY + layout.arrowOffsetY();
                        context.drawTexture(TEXTURE, arrowX, arrowY, OFFER_ARROW_U, OFFER_ARROW_V, OFFER_ARROW_WIDTH,
                                        OFFER_ARROW_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

                        ItemStack displayStack = offer.copyResultStack();
                        int resultX = listLeft + layout.resultItemOffsetX();
                        context.drawItem(displayStack, resultX, itemY);
                        context.drawItemInSlot(textRenderer, displayStack, resultX, itemY);
                }
                context.disableScissor();
        }

        private void drawCostStack(DrawContext context, ItemStack stack, int x, int y) {
                context.drawItem(stack, x, y);

                int requestedCount = GardenShopStackHelper.getRequestedCount(stack);
                if (requestedCount > stack.getCount()) {
                        String label = formatRequestedCount(requestedCount);
                        context.drawItemInSlot(textRenderer, stack, x, y, label);
                } else {
                        context.drawItemInSlot(textRenderer, stack, x, y);
                }
        }

        private void drawCostSlotText(DrawContext context, String label, ItemStack stack, int slotX, int slotY,
                        PageLayout layout) {
                int requiredCount = GardenShopStackHelper.getRequestedCount(stack);
                if (requiredCount <= 0) {
                        return;
                }

                String valueText = formatRequestedCount(requiredCount);

                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                drawCostTextLine(context, label, slotX + layout.costSlotLabelAnchorOffsetX(),
                                slotY + layout.costSlotLabelOffsetY(), layout.costSlotTextScale());
                drawCostTextLine(context, valueText, slotX + layout.costSlotValueAnchorOffsetX(),
                                slotY + layout.costSlotValueOffsetY(), layout.costSlotTextScale());

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
                List<CostSlotSnapshot> modified = new ArrayList<>();
                for (Slot slot : handler.slots) {
                        if (!(slot.inventory instanceof GardenShopCostInventory)) {
                                continue;
                        }

                        ItemStack stack = slot.getStack();
                        if (stack.isEmpty()) {
                                continue;
                        }

                        int originalCount = stack.getCount();
                        if (originalCount > 1) {
                                modified.add(new CostSlotSnapshot(slot, originalCount));
                                stack.setCount(1);
                        }
                }
                return modified;
        }

        private void restoreVanillaCostCounts(List<CostSlotSnapshot> suppressed) {
                for (CostSlotSnapshot snapshot : suppressed) {
                        ItemStack stack = snapshot.slot().getStack();
                        if (!stack.isEmpty()) {
                                stack.setCount(snapshot.originalCount());
                        }
                }
        }

        private record ResultSlotSnapshot(Slot slot, ItemStack stack, int originalCount) {
        }

        private record CostSlotSnapshot(Slot slot, int originalCount) {
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
                String number = String.format(Locale.ROOT, format, value);
                if (number.endsWith(".0")) {
                        number = number.substring(0, number.length() - 2);
                }
                return number + suffixes[suffixIndex];
        }

        private void drawCostTooltip(DrawContext context, ItemStack stack, int mouseX, int mouseY) {
                List<Text> tooltip = new ArrayList<>(getTooltipFromItem(stack));
                int requested = GardenShopStackHelper.getRequestedCount(stack);
                if (requested > stack.getCount()) {
                        tooltip.add(Text.translatable("screen.gardenkingmod.garden_shop.cost_count", requested));
                }
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
        }

        private void drawScrollbar(DrawContext context, int originX, int originY) {
                int scrollbarX = originX + SCROLLBAR_OFFSET_X;
                int scrollbarY = originY + SCROLLBAR_OFFSET_Y;
                int knobTravel = SCROLLBAR_TRACK_HEIGHT - SCROLLBAR_KNOB_HEIGHT;
                int knobY;

                if (!canScroll()) {
                        knobY = scrollbarY;
                } else {
                        knobY = scrollbarY + Math.round(scrollAmount * knobTravel);
                        knobY = MathHelper.clamp(knobY, scrollbarY, scrollbarY + knobTravel);
                }

                context.drawTexture(TEXTURE, scrollbarX, knobY, SCROLLBAR_KNOB_U, SCROLLBAR_KNOB_V, SCROLLBAR_KNOB_WIDTH,
                                SCROLLBAR_KNOB_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        private void drawBuyButton(DrawContext context, int originX, int originY, int mouseX, int mouseY) {
                if (!isBuyButtonVisible()) {
                        return;
                }

                PageLayout layout = getPageLayout();
                int buttonX = originX + layout.buyButtonOffsetX();
                int buttonY = originY + layout.buyButtonOffsetY();
                int v = isPointWithinBuyButton(mouseX, mouseY) ? BUY_BUTTON_HOVER_V : BUY_BUTTON_V;
                context.drawTexture(TEXTURE, buttonX, buttonY, BUY_BUTTON_U, v, layout.buyButtonWidth(),
                                layout.buyButtonHeight(),
                                TEXTURE_WIDTH, TEXTURE_HEIGHT);
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

                float animationScale = animation.scale();
                matrices.scale(animationScale, animationScale, animationScale);
                matrices.scale(16.0F, -16.0F, 16.0F);

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
                renderer.renderItem(stack, ModelTransformationMode.GUI, false, context.getMatrices(),
                                context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE,
                                OverlayTexture.DEFAULT_UV, model);
                context.draw();
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

        private boolean isPointWithinScrollbar(double mouseX, double mouseY) {
                int scrollbarX = x + SCROLLBAR_OFFSET_X;
                int scrollbarY = y + SCROLLBAR_OFFSET_Y;
                return mouseX >= scrollbarX && mouseX < scrollbarX + SCROLLBAR_TRACK_WIDTH && mouseY >= scrollbarY
                                && mouseY < scrollbarY + SCROLLBAR_TRACK_HEIGHT;
        }

        private boolean isPointWithinBuyButton(double mouseX, double mouseY) {
                if (!isBuyButtonVisible()) {
                        return false;
                }

                PageLayout layout = getPageLayout();
                int buttonX = x + layout.buyButtonOffsetX();
                int buttonY = y + layout.buyButtonOffsetY();
                return mouseX >= buttonX && mouseX < buttonX + layout.buyButtonWidth() && mouseY >= buttonY
                                && mouseY < buttonY + layout.buyButtonHeight();
        }

        private void updateScrollLimits() {
                int offerCount = getOffersForActiveTab().size();
                maxScrollSteps = Math.max(offerCount - MAX_VISIBLE_OFFERS, 0);
                setScrollAmount(scrollAmount);
                if (offerCount <= 0) {
                        if (selectedOffer != -1) {
                                clearSelectedOffer();
                        }
                } else if (selectedOffer >= offerCount) {
                        selectOffer(offerCount - 1);
                }
        }

        private void updateScrollFromMouse(double mouseY) {
                int scrollbarY = y + SCROLLBAR_OFFSET_Y;
                double relativeY = mouseY - scrollbarY - (SCROLLBAR_KNOB_HEIGHT / 2.0);
                double available = SCROLLBAR_TRACK_HEIGHT - SCROLLBAR_KNOB_HEIGHT;
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
                scrollAmount = maxScrollSteps > 0 ? (float) scrollOffset / (float) maxScrollSteps : 0.0F;
        }

        private boolean canScroll() {
                return maxScrollSteps > 0;
        }

        private Identifier getBackgroundTexture() {
                int index = MathHelper.clamp(activeTab, 0, PAGE_TEXTURES.length - 1);
                return PAGE_TEXTURES[index];
        }

        private void drawTabs(DrawContext context, int originX, int originY, int mouseX, int mouseY) {
                int tabX = originX + TAB_X;
                for (int i = 0; i < TAB_DEFINITIONS.length; i++) {
                        TabDefinition definition = TAB_DEFINITIONS[i];
                        int tabY = originY + definition.yOffset();
                        boolean hovered = isPointWithinTab(mouseX, mouseY, tabX, tabY);
                        boolean selected = i == activeTab;

                        if (hovered || selected) {
                                context.drawTexture(TEXTURE, tabX, tabY, TAB_HOVER_U, TAB_HOVER_V, TAB_WIDTH, TAB_HEIGHT,
                                                TEXTURE_WIDTH, TEXTURE_HEIGHT);
                        }

                        int iconX = tabX + TAB_ICON_OFFSET_X;
                        int iconY = tabY + TAB_ICON_OFFSET_Y;
                        context.drawTexture(TEXTURE, iconX, iconY, definition.iconU(), definition.iconV(), TAB_ICON_SIZE,
                                        TAB_ICON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                }
        }

        private boolean isPointWithinTab(double mouseX, double mouseY, int tabX, int tabY) {
                return mouseX >= tabX && mouseX < tabX + TAB_WIDTH && mouseY >= tabY && mouseY < tabY + TAB_HEIGHT;
        }

        private int getTabIndexAt(double mouseX, double mouseY) {
                int tabX = x + TAB_X;
                for (int i = 0; i < TAB_DEFINITIONS.length; i++) {
                        TabDefinition definition = TAB_DEFINITIONS[i];
                        int tabY = y + definition.yOffset();
                        if (isPointWithinTab(mouseX, mouseY, tabX, tabY)) {
                                return i;
                        }
                }
                return -1;
        }

        private void setActiveTab(int tabIndex) {
                int clampedIndex = MathHelper.clamp(tabIndex, 0, TAB_DEFINITIONS.length - 1);
                if (activeTab != clampedIndex) {
                        activeTab = clampedIndex;
                        handler.setDisplayedPage(activeTab);
                        clearSelectedOffer();
                        setScrollAmount(0.0F);
                        updateScrollLimits();
                        lastOfferCount = getOffersForActiveTab().size();
                }
        }

        private PageLayout getPageLayout() {
                int index = MathHelper.clamp(activeTab, 0, PAGE_LAYOUTS.length - 1);
                return PAGE_LAYOUTS[index];
        }

        private record TabDefinition(int yOffset, int iconU, int iconV) {
        }

        private record PageLayout(int offerListX, int offerListY, int costItemOffsetX, int costItemSpacing,
                        int resultItemOffsetX, int arrowOffsetX, int arrowOffsetY, int buyButtonOffsetX,
                        int buyButtonOffsetY, int buyButtonWidth, int buyButtonHeight, int buyLabelX, int buyLabelY,
                        int costSlotLabelAnchorOffsetX, int costSlotLabelOffsetY, int costSlotValueAnchorOffsetX,
                        int costSlotValueOffsetY, float costSlotTextScale) {
                static Builder defaults() {
                        return new Builder()
                                        .offerList(OFFER_LIST_X, OFFER_LIST_Y)
                                        .costStacks(OFFER_COST_ITEM_OFFSET_X, OFFER_COST_ITEM_SPACING)
                                        .costSlotText(DEFAULT_COST_SLOT_LABEL_ANCHOR_X, DEFAULT_COST_SLOT_LABEL_OFFSET_Y,
                                                        DEFAULT_COST_SLOT_VALUE_ANCHOR_X, DEFAULT_COST_SLOT_VALUE_OFFSET_Y,
                                                        DEFAULT_COST_SLOT_TEXT_SCALE)
                                        .resultItem(OFFER_RESULT_ITEM_OFFSET_X)
                                        .arrow(OFFER_ARROW_OFFSET_X, OFFER_ARROW_OFFSET_Y)
                                        .buyButton(BUY_BUTTON_OFFSET_X, BUY_BUTTON_OFFSET_Y, BUY_BUTTON_WIDTH,
                                                        BUY_BUTTON_HEIGHT)
                                        .buyLabel(BUY_LABEL_X, BUY_LABEL_Y);
                }

                static final class Builder {
                        private int offerListX;
                        private int offerListY;
                        private int costItemOffsetX;
                        private int costItemSpacing;
                        private int resultItemOffsetX;
                        private int arrowOffsetX;
                        private int arrowOffsetY;
                        private int buyButtonOffsetX;
                        private int buyButtonOffsetY;
                        private int buyButtonWidth;
                        private int buyButtonHeight;
                        private int buyLabelX;
                        private int buyLabelY;
                        private int costSlotLabelAnchorOffsetX;
                        private int costSlotLabelOffsetY;
                        private int costSlotValueAnchorOffsetX;
                        private int costSlotValueOffsetY;
                        private float costSlotTextScale;

                        Builder offerList(int x, int y) {
                                this.offerListX = x;
                                this.offerListY = y;
                                return this;
                        }

                        Builder costStacks(int offsetX, int spacing) {
                                this.costItemOffsetX = offsetX;
                                this.costItemSpacing = spacing;
                                return this;
                        }

                        Builder costSlotText(int labelAnchorOffsetX, int labelOffsetY, int valueAnchorOffsetX,
                                        int valueOffsetY, float scale) {
                                this.costSlotLabelAnchorOffsetX = labelAnchorOffsetX;
                                this.costSlotLabelOffsetY = labelOffsetY;
                                this.costSlotValueAnchorOffsetX = valueAnchorOffsetX;
                                this.costSlotValueOffsetY = valueOffsetY;
                                this.costSlotTextScale = scale;
                                return this;
                        }

                        Builder resultItem(int offsetX) {
                                this.resultItemOffsetX = offsetX;
                                return this;
                        }

                        Builder arrow(int offsetX, int offsetY) {
                                this.arrowOffsetX = offsetX;
                                this.arrowOffsetY = offsetY;
                                return this;
                        }

                        Builder buyButton(int offsetX, int offsetY, int width, int height) {
                                this.buyButtonOffsetX = offsetX;
                                this.buyButtonOffsetY = offsetY;
                                this.buyButtonWidth = width;
                                this.buyButtonHeight = height;
                                return this;
                        }

                        Builder buyLabel(int x, int y) {
                                this.buyLabelX = x;
                                this.buyLabelY = y;
                                return this;
                        }

                        PageLayout build() {
                                return new PageLayout(offerListX, offerListY, costItemOffsetX, costItemSpacing,
                                                resultItemOffsetX, arrowOffsetX, arrowOffsetY, buyButtonOffsetX,
                                                buyButtonOffsetY, buyButtonWidth, buyButtonHeight, buyLabelX, buyLabelY,
                                                costSlotLabelAnchorOffsetX, costSlotLabelOffsetY,
                                                costSlotValueAnchorOffsetX, costSlotValueOffsetY, costSlotTextScale);
                        }
                }
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

        private int getOfferIndexAt(double mouseX, double mouseY) {
                PageLayout layout = getPageLayout();
                int listLeft = x + layout.offerListX();
                int listTop = y + layout.offerListY();

                if (mouseX < listLeft || mouseX >= listLeft + OFFER_ENTRY_WIDTH) {
                        return -1;
                }

                double localY = mouseY - listTop;
                if (localY < 0.0D) {
                        return -1;
                }

                int row = (int) (localY / OFFER_ENTRY_HEIGHT);
                if (row < 0 || row >= MAX_VISIBLE_OFFERS) {
                        return -1;
                }

                int offerIndex = scrollOffset + row;
                return offerIndex < getOffersForActiveTab().size() ? offerIndex : -1;
        }

        private Optional<HoveredStack> getHoveredOfferStack(int mouseX, int mouseY) {
                int offerIndex = getOfferIndexAt(mouseX, mouseY);
                List<GardenShopOffer> offers = getOffersForActiveTab();
                if (offerIndex < 0 || offerIndex >= offers.size()) {
                        return Optional.empty();
                }

                PageLayout layout = getPageLayout();
                int listLeft = x + layout.offerListX();
                int listTop = y + layout.offerListY();
                int relativeMouseY = mouseY - listTop;
                if (relativeMouseY < 0) {
                        return Optional.empty();
                }

                int row = relativeMouseY / OFFER_ENTRY_HEIGHT;
                if (row < 0 || row >= MAX_VISIBLE_OFFERS) {
                        return Optional.empty();
                }

                int entryTop = listTop + row * OFFER_ENTRY_HEIGHT;
                int itemTop = entryTop + OFFER_ITEM_OFFSET_Y;
                if (mouseY < itemTop || mouseY >= itemTop + 16) {
                        return Optional.empty();
                }

                GardenShopOffer offer = offers.get(offerIndex);
                int costStart = listLeft + layout.costItemOffsetX();
                int arrowLeft = listLeft + layout.arrowOffsetX();
                int maxCostRight = arrowLeft - 2;
                List<ItemStack> costStacks = offer.costStacks();
                for (int costIndex = 0; costIndex < costStacks.size(); costIndex++) {
                        int costX = costStart + costIndex * layout.costItemSpacing();
                        if (costX + 16 > maxCostRight) {
                                break;
                        }
                        if (mouseX >= costX && mouseX < costX + 16) {
                                return Optional.of(new HoveredStack(costStacks.get(costIndex).copy(), true));
                        }
                }

                int resultLeft = listLeft + layout.resultItemOffsetX();
                if (mouseX >= resultLeft && mouseX < resultLeft + 16) {
                        return Optional.of(new HoveredStack(offer.copyResultStack(), false));
                }

                return Optional.empty();
        }

        private List<GardenShopOffer> getOffersForActiveTab() {
                return handler.getOffers(activeTab);
        }

        private record HoveredStack(ItemStack stack, boolean isCostStack) {
        }

        private void selectOffer(int offerIndex) {
                selectedOffer = offerIndex;
                sendOfferSelectionUpdate(offerIndex);
                resetResultSlotAnimation();
        }

        private void clearSelectedOffer() {
                selectedOffer = -1;
                resetResultSlotAnimation();
                sendOfferSelectionUpdate(-1);
        }

        private void sendOfferSelectionUpdate(int offerIndex) {
                if (client == null || client.interactionManager == null) {
                        return;
                }

                int buttonId = GardenShopScreenHandler.encodeSelectButtonId(activeTab, offerIndex);
                client.interactionManager.clickButton(handler.syncId, buttonId);
        }

        private void attemptPurchase() {
                if (client == null || client.interactionManager == null) {
                        return;
                }

                List<GardenShopOffer> offers = getOffersForActiveTab();
                if (selectedOffer < 0 || selectedOffer >= offers.size()) {
                        return;
                }

                int buttonId = GardenShopScreenHandler.encodePurchaseButtonId(activeTab, selectedOffer);
                client.interactionManager.clickButton(handler.syncId, buttonId);
        }

        private boolean isBuyButtonVisible() {
                return !getOffersForActiveTab().isEmpty();
        }

        private void playClickSound() {
                if (client == null) {
                        return;
                }

                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
}
