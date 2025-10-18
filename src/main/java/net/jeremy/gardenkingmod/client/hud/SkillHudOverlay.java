package net.jeremy.gardenkingmod.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.client.skill.SkillState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Renders the Garden King skill progress bar just above the player's hunger
 * bar in the HUD.
 */
public final class SkillHudOverlay implements HudRenderCallback {
    private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID,
            "textures/gui/skill_xp_bar.png");
    // The texture should contain three horizontal stripes, each BAR_WIDTH pixels wide:
    // 1) Background slice at V=0 with height BAR_HEIGHT
    // 2) Filled slice at V=BAR_HEIGHT with height BAR_HEIGHT
    // 3) Highlight slice at V=BAR_HEIGHT*2 with height BAR_HEIGHT
    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 5;
    // DrawContext#drawTexture requires the full dimensions of the underlying texture atlas
    // to avoid stretching the UVs across the image. The skill bar sprites occupy the top
    // left corner of a 128x128 texture file.
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final int BACKGROUND_V = 0;
    private static final int FILL_V = BAR_HEIGHT;
    private static final int HIGHLIGHT_V = BAR_HEIGHT * 2;

    public static final SkillHudOverlay INSTANCE = new SkillHudOverlay();

    private SkillHudOverlay() {
    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden) {
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player == null || player.isSpectator()) {
            return;
        }

        SkillState skillState = SkillState.getInstance();
        float progress = MathHelper.clamp(skillState.getProgressPercentage(), 0.0f, 1.0f);

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        int hungerBarAnchorX = scaledWidth / 2 + 91;
        int barX = hungerBarAnchorX - BAR_WIDTH;
        int hungerBarY = scaledHeight - 39;
        int barY = hungerBarY - BAR_HEIGHT - 3;

        context.drawTexture(TEXTURE, barX, barY, 0, BACKGROUND_V, BAR_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        int filledWidth = MathHelper.ceil(progress * BAR_WIDTH);
        if (filledWidth > 0) {
            context.drawTexture(TEXTURE, barX, barY, 0, FILL_V, filledWidth, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

        int unspentSkillPoints = skillState.getUnspentSkillPoints();
        if (unspentSkillPoints > 0) {
            float flashStrength = 0.55f + 0.45f
                    * MathHelper.sin((player.age + tickDelta) * 0.3f * MathHelper.PI);
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, MathHelper.clamp(flashStrength, 0.0f, 1.0f));
            context.drawTexture(TEXTURE, barX, barY, 0, HIGHLIGHT_V, BAR_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            context.drawTexture(TEXTURE, barX, barY, 0, BACKGROUND_V, BAR_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        int level = Math.max(0, skillState.getLevel());
        Text levelText = Text.translatable("hud." + GardenKingMod.MOD_ID + ".skill_level", level);
        int textWidth = client.textRenderer.getWidth(levelText);
        int textX = barX + BAR_WIDTH / 2 - textWidth / 2;
        int textY = barY - 10;

        context.drawText(client.textRenderer, levelText, textX, textY, 0x80FF20, true);
    }
}
