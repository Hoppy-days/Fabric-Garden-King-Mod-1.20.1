package net.jeremy.gardenkingmod.client.render;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.block.ward.ScarecrowBlockEntity;
import net.jeremy.gardenkingmod.client.model.ScarecrowModel;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class ScarecrowBlockEntityRenderer implements BlockEntityRenderer<ScarecrowBlockEntity> {
    private static final Identifier TEXTURE = new Identifier(
            GardenKingMod.MOD_ID,
            "textures/entity/scarecrow/scarecrow.png"
    );

    private final ScarecrowModel model;

    public ScarecrowBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new ScarecrowModel(context.getLayerModelPart(ScarecrowModel.LAYER_LOCATION));
    }

    @Override
    public void render(ScarecrowBlockEntity entity, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

        World world = entity.getWorld();
        int combinedLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        if (world != null) {
            BlockPos exposedPos = entity.getPos().up();
            combinedLight = WorldRenderer.getLightmapCoordinates(world, exposedPos);
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.render(matrices, vertexConsumer, combinedLight, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}
