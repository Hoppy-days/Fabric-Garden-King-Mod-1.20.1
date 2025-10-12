package net.jeremy.gardenkingmod.screen;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BankScreen extends HandledScreen<BankScreenHandler> {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(GardenKingMod.MOD_ID,
            "textures/gui/container/bank_gui.png");
    private static final int BACKGROUND_TEXTURE_WIDTH = 300;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
    private static final int WITHDRAW_TITLE_X_OFFSET = 188;
    private static final int WITHDRAW_TITLE_Y_OFFSET = 6;
    private static final int DEPOSIT_TITLE_X_OFFSET = 188;
    private static final int DEPOSIT_TITLE_Y_OFFSET = 94;
    private static final Identifier DOLLAR_ITEM_TEXTURE = new Identifier(GardenKingMod.MOD_ID,
            "textures/item/dollar.png");
    private static final int DOLLAR_TEXTURE_WIDTH = 16;
    private static final int DOLLAR_TEXTURE_HEIGHT = 16;
    private static final int DOLLAR_TEXTURE_X_OFFSET = 80;
    private static final int DOLLAR_TEXTURE_Y_OFFSET = 36;
    private static final int TEXTURE_WIDTH = 300;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int BASE_GUI_WIDTH = 176;
    private static final int BALANCE_SLOT_X_OFFSET = (BASE_GUI_WIDTH - BankScreenHandler.SLOT_SIZE) / 2;
    private static final int BALANCE_SLOT_Y_OFFSET = 24;
    private static final int BALANCE_SLOT_LABEL_OFFSET = BankScreenHandler.SLOT_SIZE + 22;
    private static final int BALANCE_TEXT_Y_OFFSET = BALANCE_SLOT_Y_OFFSET + BankScreenHandler.SLOT_SIZE + 38;
    private static final int WITHDRAW_FIELD_WIDTH = 72;
    private static final int WITHDRAW_FIELD_HEIGHT = 22;
    private static final int WITHDRAW_FIELD_X_OFFSET = 220;
    private static final int WITHDRAW_FIELD_Y_OFFSET = 31;
    private static final int WITHDRAW_BUTTON_WIDTH = 72;
    private static final int WITHDRAW_BUTTON_HEIGHT = 22;
    private static final int WITHDRAW_BUTTON_X_OFFSET = 205;
    private static final int WITHDRAW_BUTTON_Y_OFFSET = 52;
    private static final int WITHDRAW_BUTTON_TEXT_X_ADJUST = -9;
    private static final int WITHDRAW_BUTTON_TEXT_Y_ADJUST = -1;
    private static final int DEPOSIT_BUTTON_WIDTH = 72;
    private static final int DEPOSIT_BUTTON_HEIGHT = 22;
    private static final int DEPOSIT_BUTTON_X_OFFSET = 205;
    private static final int DEPOSIT_BUTTON_Y_OFFSET = 140;
    private static final int DEPOSIT_BUTTON_TEXT_X_ADJUST = -9;
    private static final int DEPOSIT_BUTTON_TEXT_Y_ADJUST = -1;
    private static final int BUTTON_HOVER_U = 0;
    private static final int BUTTON_HOVER_V = 223;

    private static final Text WITHDRAW_PLACEHOLDER = Text
            .translatable("screen.gardenkingmod.bank.withdraw_placeholder");
    private static final int PLACEHOLDER_COLOR = 0xA0A0A0;

    private TextFieldWidget withdrawField;
    private BankActionButton withdrawButton;
    private BankActionButton depositButton;

    public BankScreen(BankScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = BankScreenHandler.GUI_WIDTH;
        this.backgroundHeight = BankScreenHandler.GUI_HEIGHT;
        this.playerInventoryTitleY = BankScreenHandler.PLAYER_INVENTORY_TITLE_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - BASE_GUI_WIDTH) / 2;
        this.titleX = (BASE_GUI_WIDTH - this.textRenderer.getWidth(title)) / 2;

        int left = this.x;
        int top = this.y;

        this.withdrawField = new TextFieldWidget(this.textRenderer,
                left + WITHDRAW_FIELD_X_OFFSET,
                top + WITHDRAW_FIELD_Y_OFFSET,
                WITHDRAW_FIELD_WIDTH,
                WITHDRAW_FIELD_HEIGHT,
                Text.empty());
        this.withdrawField.setMaxLength(12);
        this.withdrawField.setTextPredicate(text -> text == null || text.chars().allMatch(Character::isDigit));
        this.withdrawField.setDrawsBackground(false);
        this.addDrawableChild(this.withdrawField);
        this.setFocused(this.withdrawField);
        this.withdrawField.setFocused(true);

        this.withdrawButton = new BankActionButton(
                left + WITHDRAW_BUTTON_X_OFFSET,
                top + WITHDRAW_BUTTON_Y_OFFSET,
                WITHDRAW_BUTTON_WIDTH,
                WITHDRAW_BUTTON_HEIGHT,
                Text.translatable("screen.gardenkingmod.bank.withdraw"),
                this::attemptWithdraw,
                WITHDRAW_BUTTON_TEXT_X_ADJUST,
                WITHDRAW_BUTTON_TEXT_Y_ADJUST);
        this.addDrawableChild(this.withdrawButton);

        this.depositButton = new BankActionButton(
                left + DEPOSIT_BUTTON_X_OFFSET,
                top + DEPOSIT_BUTTON_Y_OFFSET,
                DEPOSIT_BUTTON_WIDTH,
                DEPOSIT_BUTTON_HEIGHT,
                Text.translatable("screen.gardenkingmod.bank.deposit"),
                this::attemptDeposit,
                DEPOSIT_BUTTON_TEXT_X_ADJUST,
                DEPOSIT_BUTTON_TEXT_Y_ADJUST);
        this.addDrawableChild(this.depositButton);

        updateButtonStates();

        setInitialFocus(this.withdrawField);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        context.drawText(this.textRenderer,
                Text.translatable("screen.gardenkingmod.bank.withdraw"),
                x + WITHDRAW_TITLE_X_OFFSET,
                y + WITHDRAW_TITLE_Y_OFFSET,
                0x404040,
                false);
        context.drawText(this.textRenderer,
                Text.translatable("screen.gardenkingmod.bank.deposit"),
                x + DEPOSIT_TITLE_X_OFFSET,
                y + DEPOSIT_TITLE_Y_OFFSET,
                0x404040,
                false);
        int slotOriginX = x + BALANCE_SLOT_X_OFFSET;
        int slotY = y + BALANCE_SLOT_Y_OFFSET;

        Text slotLabel = Text.translatable("screen.gardenkingmod.bank.slot.dollar");
        int labelX = slotOriginX + BankScreenHandler.SLOT_SIZE / 2 - textRenderer.getWidth(slotLabel) / 2;
        context.drawText(textRenderer, slotLabel, labelX, slotY + BALANCE_SLOT_LABEL_OFFSET, 0x404040, false);

        context.drawTexture(DOLLAR_ITEM_TEXTURE,
                x + DOLLAR_TEXTURE_X_OFFSET,
                y + DOLLAR_TEXTURE_Y_OFFSET,
                0,
                0,
                DOLLAR_TEXTURE_WIDTH,
                DOLLAR_TEXTURE_HEIGHT,
                DOLLAR_TEXTURE_WIDTH,
                DOLLAR_TEXTURE_HEIGHT);

        Text totalText = Text.translatable("screen.gardenkingmod.bank.total", handler.getTotalDollars());
        context.drawText(textRenderer, totalText, x + (BASE_GUI_WIDTH - textRenderer.getWidth(totalText)) / 2,
                y + BALANCE_TEXT_Y_OFFSET,
                0x404040, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
        renderWithdrawPlaceholder(context);
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if (this.withdrawField != null) {
            this.withdrawField.tick();
        }
        updateButtonStates();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.withdrawField != null && this.withdrawField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (this.withdrawField != null && this.withdrawField.isFocused()
                && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            attemptWithdraw();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.withdrawField != null && this.withdrawField.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.withdrawField != null && this.withdrawField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.withdrawField);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderWithdrawPlaceholder(DrawContext context) {
        if (this.withdrawField == null || !this.withdrawField.getText().isEmpty()
                || this.withdrawField.isFocused()) {
            return;
        }

        Text placeholder = WITHDRAW_PLACEHOLDER;
        int placeholderWidth = this.textRenderer.getWidth(placeholder);
        int placeholderX = this.withdrawField.getX() + (this.withdrawField.getWidth() - placeholderWidth) / 2;
        int placeholderY = this.withdrawField.getY()
                + (this.withdrawField.getHeight() - this.textRenderer.fontHeight) / 2;

        context.drawText(this.textRenderer, placeholder, placeholderX, placeholderY, PLACEHOLDER_COLOR, false);
    }

    private void attemptDeposit() {
        MinecraftClient client = this.client;
        if (client == null || client.interactionManager == null) {
            return;
        }

        if (!this.handler.hasDepositItem()) {
            return;
        }

        client.interactionManager.clickButton(this.handler.syncId, BankScreenHandler.BUTTON_DEPOSIT);
    }

    private void attemptWithdraw() {
        long amount = getWithdrawAmount();
        if (amount <= 0 || amount > this.handler.getTotalDollars()) {
            return;
        }

        MinecraftClient client = this.client;
        if (client == null || client.player == null) {
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(this.handler.syncId);
        buf.writeVarLong(amount);
        ClientPlayNetworking.send(ModPackets.BANK_WITHDRAW_REQUEST_PACKET, buf);
        this.withdrawField.setText("");
    }

    private long getWithdrawAmount() {
        if (this.withdrawField == null) {
            return 0L;
        }

        String text = this.withdrawField.getText();
        if (text == null || text.isEmpty()) {
            return 0L;
        }

        long value;
        try {
            value = Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return 0L;
        }

        long clamped = Math.min(value, BankScreenHandler.MAX_WITHDRAW_AMOUNT);
        if (clamped != value) {
            this.withdrawField.setText(Long.toString(clamped));
            value = clamped;
        }

        return Math.max(value, 0L);
    }

    private void updateButtonStates() {
        if (this.depositButton != null) {
            this.depositButton.setActive(this.handler.hasDepositItem());
        }

        if (this.withdrawButton != null) {
            long amount = getWithdrawAmount();
            boolean canWithdraw = amount > 0 && amount <= this.handler.getTotalDollars();
            this.withdrawButton.setActive(canWithdraw);
        }
    }

    private class BankActionButton extends ClickableWidget {
        private final Runnable onPressAction;
        private final int textXAdjust;
        private final int textYAdjust;

        BankActionButton(int x, int y, int width, int height, Text narration, Runnable onPressAction,
                int textXAdjust, int textYAdjust) {
            super(x, y, width, height, narration);
            this.onPressAction = onPressAction;
            this.textXAdjust = textXAdjust;
            this.textYAdjust = textYAdjust;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.onPressAction.run();
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            if (!this.visible) {
                return;
            }

            if ((this.isHovered() || this.isFocused()) && this.active) {
                context.drawTexture(BACKGROUND_TEXTURE, this.getX(), this.getY(), BUTTON_HOVER_U, BUTTON_HOVER_V,
                        this.width, this.height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }

            Text message = this.getMessage();
            int textWidth = BankScreen.this.textRenderer.getWidth(message);
            int textX = this.getX() + (this.width - textWidth) / 2 + this.textXAdjust;
            int textY = this.getY() + (this.height - BankScreen.this.textRenderer.fontHeight) / 2 + this.textYAdjust;
            int color = this.active ? 0xFFFFFF : 0xA0A0A0;
            context.drawText(BankScreen.this.textRenderer, message, textX, textY, color, false);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            builder.put(NarrationPart.TITLE, this.getMessage());
        }

        void setActive(boolean active) {
            this.active = active;
        }
    }
}
