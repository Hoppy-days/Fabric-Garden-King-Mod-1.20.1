package net.jeremy.gardenkingmod.screen;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.ModItems;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BankScreen extends HandledScreen<BankScreenHandler> {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(GardenKingMod.MOD_ID,
            "textures/gui/container/bank_gui.png");
    private static final int SLOT_SIZE = 18;
    private static final ItemStack DOLLAR_STACK = new ItemStack(ModItems.DOLLAR);

    public BankScreen(BankScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
        int slotOriginX = x + (backgroundWidth - SLOT_SIZE) / 2;
        int slotY = y + 34;

        drawSlot(context, slotOriginX, slotY, DOLLAR_STACK, handler.getTotalDollars(),
                Text.translatable("screen.gardenkingmod.bank.slot.dollar"));

        Text totalText = Text.translatable("screen.gardenkingmod.bank.total", handler.getTotalDollars());
        context.drawText(textRenderer, totalText, x + (backgroundWidth - textRenderer.getWidth(totalText)) / 2, slotY + 54,
                0x404040, false);
    }

    private void drawSlot(DrawContext context, int slotX, int slotY, ItemStack stack, int count, Text label) {
        context.drawTexture(BACKGROUND_TEXTURE, slotX, slotY, 7, 83, SLOT_SIZE, SLOT_SIZE, 256, 256);
        context.drawItem(stack, slotX + 1, slotY + 1);
        String countText = Integer.toString(count);
        int countWidth = textRenderer.getWidth(countText);
        context.drawText(textRenderer, countText, slotX + SLOT_SIZE - 4 - countWidth, slotY + SLOT_SIZE - 9, 0x404040, false);
        int labelX = slotX + SLOT_SIZE / 2 - textRenderer.getWidth(label) / 2;
        context.drawText(textRenderer, label, labelX, slotY + SLOT_SIZE + 4, 0x404040, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
