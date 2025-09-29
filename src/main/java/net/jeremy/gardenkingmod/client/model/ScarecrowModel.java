package net.jeremy.gardenkingmod.client.model;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class ScarecrowModel extends EntityModel<Entity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            new Identifier(GardenKingMod.MOD_ID, "scarecrow"),
            "main"
    );

    private final ModelPart scarecrowBase;

    public ScarecrowModel(ModelPart root) {
        this.scarecrowBase = root.getChild("scarecrow_base");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("scarecrow_base", ModelPartBuilder.create()
                        .uv(0, 12)
                        .cuboid(-8.0F, -4.0F, -8.0F, 16.0F, 4.0F, 16.0F, new Dilation(0.0F))
                        .uv(0, 28)
                        .cuboid(-1.0F, -16.0F, -1.0F, 2.0F, 12.0F, 2.0F, new Dilation(0.0F))
                        .uv(0, 24)
                        .cuboid(-8.0F, -16.0F, -1.0F, 16.0F, 2.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        scarecrowBase.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }
}
