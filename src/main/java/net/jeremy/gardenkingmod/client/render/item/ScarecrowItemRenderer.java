package net.jeremy.gardenkingmod.client.render.item;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.client.model.ScarecrowModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class ScarecrowItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final Identifier TEXTURE = new Identifier(
            GardenKingMod.MOD_ID,
            "textures/entity/scarecrow/scarecrow.png"
    );

    private ScarecrowModel model;

    public ScarecrowItemRenderer() {
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

        switch (mode) {
            case GUI:
                matrices.scale(0.35f, 0.35f, 0.35f);
                matrices.translate(0.0, 2.4, 0.0);
                break;
            case GROUND:
                matrices.scale(0.35f, 0.35f, 0.35f);
                matrices.translate(0.0, 0.9, 0.0);
                break;
            case FIXED:
                matrices.scale(0.4f, 0.4f, 0.4f);
                matrices.translate(0.0, 1.8, 0.0);
                break;
            default:
                break;
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
                 THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                matrices.scale(0.25F, 0.25F, 0.25F);
                matrices.translate(0.0F, 2.0F, 0.0F);
            }
            default -> { /* keep block-sized scale for pedestal/world */ }
        }

        if (this.model == null) {
            this.model = new ScarecrowModel(MinecraftClient.getInstance().getEntityModelLoader()
                    .getModelPart(ScarecrowModel.LAYER_LOCATION));
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.render(matrices, vertexConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}