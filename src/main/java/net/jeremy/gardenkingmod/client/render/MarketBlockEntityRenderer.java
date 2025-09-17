package net.jeremy.gardenkingmod.client.render;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.block.entity.MarketBlockEntity;
import net.jeremy.gardenkingmod.client.model.MarketBlockModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class MarketBlockEntityRenderer implements BlockEntityRenderer<MarketBlockEntity> {
        private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/entity/market/market.png");

        private final MarketBlockModel model;

        public MarketBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
                this.model = new MarketBlockModel(context.getLayerModelPart(MarketBlockModel.LAYER_LOCATION));
        }

        @Override
        public void render(MarketBlockEntity entity, float tickDelta, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light, int overlay) {
                matrices.push();
                matrices.translate(0.5f, 1.5f, 0.5f);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
                this.model.render(matrices, vertexConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

                matrices.pop();
        }
}
