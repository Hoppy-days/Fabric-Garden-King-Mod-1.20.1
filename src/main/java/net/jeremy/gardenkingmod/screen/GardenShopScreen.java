package net.jeremy.gardenkingmod.screen;

import java.util.List;
import java.util.Optional;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.shop.GardenShopOffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class GardenShopScreen extends HandledScreen<GardenShopScreenHandler> {
        private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/garden_shop_gui.png");
        private static final int TEXTURE_WIDTH = 512;
        private static final int TEXTURE_HEIGHT = 256;

        private static final int BACKGROUND_WIDTH = 276;
        private static final int BACKGROUND_HEIGHT = 198;
        private static final int PLAYER_INVENTORY_LABEL_Y = 104;
        private static final int PLAYER_INVENTORY_LABEL_X = 110;
        private static final int TITLE_X = 156;
        private static final int TITLE_Y = 6;

        private static final int OFFERS_LABEL_X = 8;
        private static final int OFFERS_LABEL_Y = 6;
        private static final int BUY_LABEL_X = 180;
        private static final int BUY_LABEL_Y = 87;

        private static final int OFFER_LIST_X = 5;
        private static final int OFFER_LIST_Y = 17;
        private static final int OFFER_ENTRY_WIDTH = 88;
        private static final int OFFER_ENTRY_HEIGHT = 20;
        private static final int MAX_VISIBLE_OFFERS = (PLAYER_INVENTORY_LABEL_Y - OFFER_LIST_Y) / OFFER_ENTRY_HEIGHT;
        private static final int OFFER_ITEM_OFFSET_Y = 2;
        private static final int OFFER_COST_ITEM_OFFSET_X = 6;
        private static final int OFFER_COST_ITEM_SPACING = 18;
        private static final int OFFER_RESULT_ITEM_OFFSET_X = 68;
        private static final int OFFER_BACKGROUND_U = 277;
        private static final int OFFER_BACKGROUND_V = 0;
        private static final int OFFER_HOVER_BACKGROUND_V = 21;
        private static final int OFFER_ARROW_U = 277;
        private static final int OFFER_ARROW_V = 42;
        private static final int OFFER_ARROW_WIDTH = 10;
        private static final int OFFER_ARROW_HEIGHT = 9;
        private static final int OFFER_ARROW_OFFSET_X = 53;
        private static final int OFFER_ARROW_OFFSET_Y = 6;

        private static final int SCROLLBAR_OFFSET_X = 94;
        private static final int SCROLLBAR_OFFSET_Y = 16;
        private static final int SCROLLBAR_TRACK_U = 0;
        private static final int SCROLLBAR_TRACK_V = 199;
        private static final int SCROLLBAR_TRACK_WIDTH = 6;
        private static final int SCROLLBAR_TRACK_HEIGHT = 78;
        private static final int SCROLLBAR_KNOB_U = 6;
        private static final int SCROLLBAR_KNOB_V = 199;
        private static final int SCROLLBAR_KNOB_WIDTH = 6;
        private static final int SCROLLBAR_KNOB_HEIGHT = 27;

        private static final int SELECTED_HIGHLIGHT_COLOR = 0x40FFFFFF;

        private int maxScrollSteps;
        private int scrollOffset;
        private float scrollAmount;
        private boolean scrollbarDragging;
        private int selectedOffer = -1;
        private int lastOfferCount = -1;

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
                updateScrollLimits();
                lastOfferCount = handler.getOffers().size();
        }

        @Override
        protected void handledScreenTick() {
                super.handledScreenTick();

                int currentOfferCount = handler.getOffers().size();
                if (currentOfferCount != lastOfferCount) {
                        updateScrollLimits();
                        lastOfferCount = currentOfferCount;
                }
        }

        @Override
        protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
                int originX = (width - backgroundWidth) / 2;
                int originY = (height - backgroundHeight) / 2;
                context.drawTexture(TEXTURE, originX, originY, 0, 0, backgroundWidth, backgroundHeight, TEXTURE_WIDTH,
                                TEXTURE_HEIGHT);

                drawOfferList(context, originX, originY, mouseX, mouseY);
                drawScrollbar(context, originX, originY);
        }

        @Override
        protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
                super.drawForeground(context, mouseX, mouseY);
                context.drawText(textRenderer, Text.translatable("screen.gardenkingmod.garden_shop.offers"), OFFERS_LABEL_X,
                                OFFERS_LABEL_Y, 0x404040, false);
                context.drawText(textRenderer, Text.translatable("screen.gardenkingmod.garden_shop.buy_button"), BUY_LABEL_X,
                                BUY_LABEL_Y, 0xFFFFFF, false);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                renderBackground(context);
                super.render(context, mouseX, mouseY, delta);
                drawMouseoverTooltip(context, mouseX, mouseY);
        }

        @Override
        protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
                super.drawMouseoverTooltip(context, mouseX, mouseY);
                getHoveredOfferStack(mouseX, mouseY)
                                .ifPresent(stack -> context.drawItemTooltip(textRenderer, stack, mouseX, mouseY));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                        if (isPointWithinScrollbar(mouseX, mouseY)) {
                                scrollbarDragging = true;
                                updateScrollFromMouse(mouseY);
                                return true;
                        }

                        int offerIndex = getOfferIndexAt(mouseX, mouseY);
                        if (offerIndex >= 0) {
                                selectedOffer = offerIndex;
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
                List<GardenShopOffer> offers = handler.getOffers();
                int listLeft = originX + OFFER_LIST_X;
                int listTop = originY + OFFER_LIST_Y;
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
                        int costStartX = listLeft + OFFER_COST_ITEM_OFFSET_X;
                        int arrowX = listLeft + OFFER_ARROW_OFFSET_X;
                        int maxCostRight = arrowX - 2;
                        List<ItemStack> costStacks = offer.costStacks();
                        for (int costIndex = 0; costIndex < costStacks.size(); costIndex++) {
                                int costX = costStartX + costIndex * OFFER_COST_ITEM_SPACING;
                                if (costX + 16 > maxCostRight) {
                                        break;
                                }
                                ItemStack costStack = costStacks.get(costIndex);
                                context.drawItem(costStack, costX, itemY);
                                context.drawItemInSlot(textRenderer, costStack, costX, itemY);
                        }

                        int arrowY = entryY + OFFER_ARROW_OFFSET_Y;
                        context.drawTexture(TEXTURE, arrowX, arrowY, OFFER_ARROW_U, OFFER_ARROW_V, OFFER_ARROW_WIDTH,
                                        OFFER_ARROW_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

                        ItemStack displayStack = offer.copyResultStack();
                        int resultX = listLeft + OFFER_RESULT_ITEM_OFFSET_X;
                        context.drawItem(displayStack, resultX, itemY);
                        context.drawItemInSlot(textRenderer, displayStack, resultX, itemY);
                }
                context.disableScissor();
        }

        private void drawScrollbar(DrawContext context, int originX, int originY) {
                int scrollbarX = originX + SCROLLBAR_OFFSET_X;
                int scrollbarY = originY + SCROLLBAR_OFFSET_Y;
                context.drawTexture(TEXTURE, scrollbarX, scrollbarY, SCROLLBAR_TRACK_U, SCROLLBAR_TRACK_V, SCROLLBAR_TRACK_WIDTH,
                                SCROLLBAR_TRACK_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

                if (!canScroll()) {
                        int centeredY = scrollbarY + (SCROLLBAR_TRACK_HEIGHT - SCROLLBAR_KNOB_HEIGHT) / 2;
                        context.drawTexture(TEXTURE, scrollbarX, centeredY, SCROLLBAR_KNOB_U, SCROLLBAR_KNOB_V,
                                        SCROLLBAR_KNOB_WIDTH, SCROLLBAR_KNOB_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                        return;
                }

                int knobTravel = SCROLLBAR_TRACK_HEIGHT - SCROLLBAR_KNOB_HEIGHT;
                int knobY = scrollbarY + Math.round(scrollAmount * knobTravel);
                knobY = MathHelper.clamp(knobY, scrollbarY, scrollbarY + knobTravel);
                context.drawTexture(TEXTURE, scrollbarX, knobY, SCROLLBAR_KNOB_U, SCROLLBAR_KNOB_V, SCROLLBAR_KNOB_WIDTH,
                                SCROLLBAR_KNOB_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        private boolean isPointWithinScrollbar(double mouseX, double mouseY) {
                int originX = (width - backgroundWidth) / 2;
                int originY = (height - backgroundHeight) / 2;
                int scrollbarX = originX + SCROLLBAR_OFFSET_X;
                int scrollbarY = originY + SCROLLBAR_OFFSET_Y;
                return mouseX >= scrollbarX && mouseX < scrollbarX + SCROLLBAR_TRACK_WIDTH && mouseY >= scrollbarY
                                && mouseY < scrollbarY + SCROLLBAR_TRACK_HEIGHT;
        }

        private void updateScrollLimits() {
                int offerCount = handler.getOffers().size();
                maxScrollSteps = Math.max(offerCount - MAX_VISIBLE_OFFERS, 0);
                setScrollAmount(scrollAmount);
                if (selectedOffer >= offerCount) {
                        selectedOffer = offerCount - 1;
                }
        }

        private void updateScrollFromMouse(double mouseY) {
                int originY = (height - backgroundHeight) / 2;
                int scrollbarY = originY + SCROLLBAR_OFFSET_Y;
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

                scrollAmount = MathHelper.clamp(amount, 0.0F, 1.0F);
                scrollOffset = MathHelper.floor(scrollAmount * maxScrollSteps + 0.5F);
                scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScrollSteps);
        }

        private boolean canScroll() {
                return maxScrollSteps > 0;
        }

        private int getOfferIndexAt(double mouseX, double mouseY) {
                int originX = (width - backgroundWidth) / 2;
                int originY = (height - backgroundHeight) / 2;
                int listLeft = originX + OFFER_LIST_X;
                int listTop = originY + OFFER_LIST_Y;

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
                return offerIndex < handler.getOffers().size() ? offerIndex : -1;
        }

        private Optional<ItemStack> getHoveredOfferStack(int mouseX, int mouseY) {
                int offerIndex = getOfferIndexAt(mouseX, mouseY);
                if (offerIndex < 0 || offerIndex >= handler.getOffers().size()) {
                        return Optional.empty();
                }

                int originX = (width - backgroundWidth) / 2;
                int originY = (height - backgroundHeight) / 2;
                int listLeft = originX + OFFER_LIST_X;
                int listTop = originY + OFFER_LIST_Y;
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

                GardenShopOffer offer = handler.getOffers().get(offerIndex);
                int costStart = listLeft + OFFER_COST_ITEM_OFFSET_X;
                int arrowLeft = listLeft + OFFER_ARROW_OFFSET_X;
                int maxCostRight = arrowLeft - 2;
                List<ItemStack> costStacks = offer.costStacks();
                for (int costIndex = 0; costIndex < costStacks.size(); costIndex++) {
                        int costX = costStart + costIndex * OFFER_COST_ITEM_SPACING;
                        if (costX + 16 > maxCostRight) {
                                break;
                        }
                        if (mouseX >= costX && mouseX < costX + 16) {
                                return Optional.of(costStacks.get(costIndex).copy());
                        }
                }

                int resultLeft = listLeft + OFFER_RESULT_ITEM_OFFSET_X;
                if (mouseX >= resultLeft && mouseX < resultLeft + 16) {
                        return Optional.of(offer.copyResultStack());
                }

                return Optional.empty();
        }
}
