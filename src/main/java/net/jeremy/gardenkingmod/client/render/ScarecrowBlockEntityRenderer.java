package net.jeremy.gardenkingmod.client.render;

import net.jeremy.gardenkingmod.block.ward.ScarecrowBlockEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class ScarecrowBlockEntityRenderer implements BlockEntityRenderer<ScarecrowBlockEntity> {
    private final ScarecrowRenderHelper renderHelper;

    public ScarecrowBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.renderHelper = ScarecrowRenderHelper.createDefault(context);
    }

    @Override
    public void render(ScarecrowBlockEntity entity, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        float groundY = ScarecrowRenderHelper.DEFAULT_RENDER_SCALE * (24.0F / 16.0F);
        matrices.translate(0.5f, groundY, 0.5f);
        matrices.scale(ScarecrowRenderHelper.DEFAULT_RENDER_SCALE,
                ScarecrowRenderHelper.DEFAULT_RENDER_SCALE,
                ScarecrowRenderHelper.DEFAULT_RENDER_SCALE);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

        World world = entity.getWorld();
        int combinedLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        if (world != null) {
            BlockPos exposedPos = entity.getPos().up();
            combinedLight = WorldRenderer.getLightmapCoordinates(world, exposedPos);
        }

        this.renderHelper.setHatStack(entity.getEquippedHat());
        this.renderHelper.setHeadStack(entity.getEquippedHead());
        this.renderHelper.setChestStack(entity.getEquippedChest());
        this.renderHelper.setPantsStack(entity.getEquippedPants());
        this.renderHelper.setPitchforkStack(entity.getEquippedPitchfork());

        this.renderHelper.render(matrices, vertexConsumers, combinedLight, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }
}
