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
        matrices.translate(0.5f, 1.9f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

        World world = entity.getWorld();
        int combinedLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        if (world != null) {
            BlockPos exposedPos = entity.getPos().up();
            combinedLight = WorldRenderer.getLightmapCoordinates(world, exposedPos);
        }

        this.renderHelper.setHatVisible(!entity.getEquippedHat().isEmpty());
        this.renderHelper.setHeadVisible(!entity.getEquippedHead().isEmpty());
        this.renderHelper.setChestVisible(!entity.getEquippedChest().isEmpty());
        this.renderHelper.setPantsVisible(!entity.getEquippedPants().isEmpty());
        this.renderHelper.setPitchforkVisible(!entity.getEquippedPitchfork().isEmpty());

        this.renderHelper.render(matrices, vertexConsumers, combinedLight, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }
}
