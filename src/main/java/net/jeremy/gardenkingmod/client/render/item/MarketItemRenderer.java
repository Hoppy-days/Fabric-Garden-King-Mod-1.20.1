package net.jeremy.gardenkingmod.client.render.item;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.client.model.MarketBlockModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class MarketItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final Identifier TEXTURE = new Identifier(
            GardenKingMod.MOD_ID,
            "textures/entity/market/market.png"
    );

    private MarketBlockModel model;

    public MarketItemRenderer() {
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

        if (mode != null) {
            switch (mode) {
                case GUI -> {
                    matrices.scale(0.16f, 0.16f, 0.16f);
                    matrices.translate(0.0, 4.4, 0.0);
                }
                case GROUND -> {
                    matrices.scale(0.18f, 0.18f, 0.18f);
                    matrices.translate(0.0, 2.8, 0.0);
                }
                case FIXED -> {
                    matrices.scale(0.18f, 0.18f, 0.18f);
                    matrices.translate(0.0, 3.0, 0.0);
                }
                case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
                        THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                    matrices.scale(0.14f, 0.14f, 0.14f);
                    matrices.translate(0.0f, 3.2f, 0.0f);
                }
                default -> {
                }
            }
        }

        if (this.model == null) {
            this.model = new MarketBlockModel(MinecraftClient.getInstance().getEntityModelLoader()
                    .getModelPart(MarketBlockModel.LAYER_LOCATION));
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.render(matrices, vertexConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}
