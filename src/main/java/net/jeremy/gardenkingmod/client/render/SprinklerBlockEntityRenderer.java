package net.jeremy.gardenkingmod.client.render;

import net.jeremy.gardenkingmod.block.sprinkler.SprinklerBlockEntity;
import net.jeremy.gardenkingmod.client.model.SprinklerModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class SprinklerBlockEntityRenderer implements BlockEntityRenderer<SprinklerBlockEntity> {
        private final SprinklerModel model;

        public SprinklerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
                this.model = new SprinklerModel(context.getLayerModelPart(SprinklerModel.LAYER_LOCATION));
        }

        @Override
        public void render(SprinklerBlockEntity entity, float tickDelta, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light, int overlay) {
                matrices.push();
                matrices.translate(0.5D, 1.5D, 0.5D);
                matrices.scale(-1.0F, -1.0F, 1.0F);
                VertexConsumer vertexConsumer = vertexConsumers
                                .getBuffer(RenderLayer.getEntityCutoutNoCull(entity.getTier().getTexture()));
                this.model.render(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
                matrices.pop();
        }
}
