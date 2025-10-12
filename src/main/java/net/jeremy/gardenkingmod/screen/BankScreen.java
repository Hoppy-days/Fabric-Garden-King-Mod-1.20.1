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
    private static final int BALANCE_SLOT_LABEL_OFFSET = BankScreenHandler.SLOT_SIZE + 12;
    private static final int BALANCE_TEXT_Y_OFFSET = BALANCE_SLOT_Y_OFFSET + BankScreenHandler.SLOT_SIZE + 28;
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

        setInitialFocus(this.withdrawField);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        context.drawTexture(DOLLAR_ITEM_TEXTURE, x + DOLLAR_TEXTURE_X_OFFSET, y + DOLLAR_TEXTURE_Y_OFFSET, 0, 0,
                DOLLAR_TEXTURE_WIDTH, DOLLAR_TEXTURE_HEIGHT, DOLLAR_TEXTURE_WIDTH, DOLLAR_TEXTURE_HEIGHT);

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
        context.drawText(textRenderer, label, labelX, slotY + BALANCE_SLOT_LABEL_OFFSET, 0x404040, false);
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

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (isPointWithinBounds(WITHDRAW_BUTTON_X_OFFSET, WITHDRAW_BUTTON_Y_OFFSET, WITHDRAW_BUTTON_WIDTH,
                    WITHDRAW_BUTTON_HEIGHT, mouseX, mouseY)) {
                attemptWithdraw();
                return true;
            }

            if (isPointWithinBounds(DEPOSIT_BUTTON_X_OFFSET, DEPOSIT_BUTTON_Y_OFFSET, DEPOSIT_BUTTON_WIDTH,
                    DEPOSIT_BUTTON_HEIGHT, mouseX, mouseY)) {
                attemptDeposit();
                return true;
            }
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
}
