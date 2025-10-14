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
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(facing.asRotation()));
        }

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

        World world = entity.getWorld();
        int combinedLight = light;
        if (world != null) {
            BlockPos basePos = entity.getPos();
            int lowerLight = WorldRenderer.getLightmapCoordinates(world, entity.getCachedState(), basePos);
            int upperLight = WorldRenderer.getLightmapCoordinates(world, world.getBlockState(basePos.up()), basePos.up());
            int blockLight = Math.max(lowerLight & 0xFFFF, upperLight & 0xFFFF);
            int skyLight = Math.max((lowerLight >> 16) & 0xFFFF, (upperLight >> 16) & 0xFFFF);
            combinedLight = LightmapTextureManager.pack(blockLight, skyLight);
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.render(matrices, vertexConsumer, combinedLight, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}
