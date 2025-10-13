package net.jeremy.gardenkingmod.client.render;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.block.BankBlock;
import net.jeremy.gardenkingmod.block.entity.BankBlockEntity;
import net.jeremy.gardenkingmod.client.model.BankBlockModel;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class BankBlockEntityRenderer implements BlockEntityRenderer<BankBlockEntity> {
    private static final Identifier TEXTURE = new Identifier(
            GardenKingMod.MOD_ID,
            "textures/entity/bank/bank.png"
    );

    private final BankBlockModel model;

    public BankBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new BankBlockModel(context.getLayerModelPart(BankBlockModel.LAYER_LOCATION));
    }

    @Override
    public void render(BankBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);

        Direction facing = entity.getCachedState() != null ? entity.getCachedState().get(BankBlock.FACING) : null;
        if (facing != null) {
            float yRotation;
            switch (facing) {
                case NORTH -> yRotation = 0.0f;
                case EAST -> yRotation = 90.0f;
                case SOUTH -> yRotation = 180.0f;
                case WEST -> yRotation = 270.0f;
                default -> yRotation = 0.0f;
            }
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation));
        }

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

        World world = entity.getWorld();
        int combinedLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        if (world != null) {
            BlockPos posAbove = entity.getPos().up();
            combinedLight = WorldRenderer.getLightmapCoordinates(world, posAbove);
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.render(matrices, vertexConsumer, combinedLight, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}
