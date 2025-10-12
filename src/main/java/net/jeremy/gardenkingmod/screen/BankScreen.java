package net.jeremy.gardenkingmod.screen;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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
    private static final ItemStack DOLLAR_STACK = new ItemStack(ModItems.DOLLAR);

    private static final int TEXTURE_WIDTH = 300;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int BASE_GUI_WIDTH = 176;
    private static final int BALANCE_SLOT_X_OFFSET = (BASE_GUI_WIDTH - BankScreenHandler.SLOT_SIZE) / 2;
    private static final int BALANCE_SLOT_Y_OFFSET = 24;
    private static final int BALANCE_TEXT_Y_OFFSET = BALANCE_SLOT_Y_OFFSET + BankScreenHandler.SLOT_SIZE + 20;
    private static final int WITHDRAW_FIELD_WIDTH = 72;
    private static final int WITHDRAW_FIELD_HEIGHT = 22;
    private static final int WITHDRAW_FIELD_X_OFFSET = 196;
    private static final int WITHDRAW_FIELD_Y_OFFSET = 24;
    private static final int WITHDRAW_BUTTON_WIDTH = 72;
    private static final int WITHDRAW_BUTTON_HEIGHT = 22;
    private static final int WITHDRAW_BUTTON_X_OFFSET = 205;
    private static final int WITHDRAW_BUTTON_Y_OFFSET = 52;
    private static final int DEPOSIT_BUTTON_WIDTH = 72;
    private static final int DEPOSIT_BUTTON_HEIGHT = 22;
    private static final int DEPOSIT_BUTTON_X_OFFSET = 205;
    private static final int DEPOSIT_BUTTON_Y_OFFSET = 140;
    private static final int BUTTON_HOVER_U = 0;
    private static final int BUTTON_HOVER_V = 223;

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
                Text.translatable("screen.gardenkingmod.bank.withdraw_placeholder"));
        this.withdrawField.setMaxLength(12);
        this.withdrawField.setTextPredicate(text -> text == null || text.chars().allMatch(Character::isDigit));
        this.withdrawField.setPlaceholder(Text.translatable("screen.gardenkingmod.bank.withdraw_placeholder"));
        this.withdrawField.setDrawsBackground(false);
        this.addDrawableChild(this.withdrawField);

        this.withdrawButton = new BankActionButton(
                left + WITHDRAW_BUTTON_X_OFFSET,
                top + WITHDRAW_BUTTON_Y_OFFSET,
                WITHDRAW_BUTTON_WIDTH,
                WITHDRAW_BUTTON_HEIGHT,
                Text.translatable("screen.gardenkingmod.bank.withdraw"),
                this::attemptWithdraw);
        this.addDrawableChild(this.withdrawButton);

        this.depositButton = new BankActionButton(
                left + DEPOSIT_BUTTON_X_OFFSET,
                top + DEPOSIT_BUTTON_Y_OFFSET,
                DEPOSIT_BUTTON_WIDTH,
                DEPOSIT_BUTTON_HEIGHT,
                Text.translatable("screen.gardenkingmod.bank.deposit"),
                this::attemptDeposit);
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

        drawSlot(context, slotOriginX, slotY, DOLLAR_STACK, handler.getTotalDollars(),
                Text.translatable("screen.gardenkingmod.bank.slot.dollar"));

        Text totalText = Text.translatable("screen.gardenkingmod.bank.total", handler.getTotalDollars());
        context.drawText(textRenderer, totalText, x + (BASE_GUI_WIDTH - textRenderer.getWidth(totalText)) / 2,
                y + BALANCE_TEXT_Y_OFFSET,
                0x404040, false);
    }

    private void drawSlot(DrawContext context, int slotX, int slotY, ItemStack stack, int count, Text label) {
        context.drawTexture(BACKGROUND_TEXTURE, slotX, slotY, 7, 83, BankScreenHandler.SLOT_SIZE, BankScreenHandler.SLOT_SIZE,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        context.drawItem(stack, slotX + 1, slotY + 1);
        String countText = Integer.toString(count);
        int countWidth = textRenderer.getWidth(countText);
        context.drawText(textRenderer, countText,
                slotX + BankScreenHandler.SLOT_SIZE - 4 - countWidth,
                slotY + BankScreenHandler.SLOT_SIZE - 9, 0x404040, false);
        int labelX = slotX + BankScreenHandler.SLOT_SIZE / 2 - textRenderer.getWidth(label) / 2;
        context.drawText(textRenderer, label, labelX, slotY + BankScreenHandler.SLOT_SIZE + 4, 0x404040, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
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
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
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

        BankActionButton(int x, int y, int width, int height, Text narration, Runnable onPressAction) {
            super(x, y, width, height, narration);
            this.onPressAction = onPressAction;
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
            int textX = this.getX() + (this.width - textWidth) / 2;
            int textY = this.getY() + (this.height - BankScreen.this.textRenderer.fontHeight) / 2;
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
