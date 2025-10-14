package net.jeremy.gardenkingmod.client.render.item;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.client.config.BankItemDisplayConfig;
import net.jeremy.gardenkingmod.client.model.BankBlockModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class BankItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final Identifier TEXTURE = new Identifier(
            GardenKingMod.MOD_ID,
            "textures/entity/bank/bank.png"
    );

    private BankBlockModel model;

    public BankItemRenderer() {
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

        if (mode != null) {
            BankItemDisplayConfig config = BankItemDisplayConfig.get();
            BankItemDisplayConfig.DisplayTransform transform = switch (mode) {
                case GUI -> config.gui();
                case GROUND -> config.ground();
                case FIXED -> config.fixed();
                case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
                        THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> config.hand();
                default -> null;
            };

            if (transform != null) {
                matrices.scale(transform.scaleX(), transform.scaleY(), transform.scaleZ());
                matrices.translate(transform.translateX(), transform.translateY(), transform.translateZ());
            }
        }

        if (this.model == null) {
            this.model = new BankBlockModel(MinecraftClient.getInstance().getEntityModelLoader()
                    .getModelPart(BankBlockModel.LAYER_LOCATION));
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.render(matrices, vertexConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}
