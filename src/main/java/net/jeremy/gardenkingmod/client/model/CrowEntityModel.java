package net.jeremy.gardenkingmod.client.model;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.entity.crow.CrowEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.SinglePartEntityModel;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CrowEntityModel extends SinglePartEntityModel<CrowEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            new Identifier(GardenKingMod.MOD_ID, "crow"), "main");

    private final ModelPart root;
    private final ModelPart rightWing;
    private final ModelPart leftWing;

    public CrowEntityModel(ModelPart root) {
        this.root = root;
        this.rightWing = root.getChild("rightwing");
        this.leftWing = root.getChild("leftwing");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        root.addChild("rightwing", ModelPartBuilder.create()
                .uv(8, 25).cuboid(-8.0F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(14, 14).cuboid(-2.0F, -2.0F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(20, 20).cuboid(-4.0F, -2.0F, -2.0F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F))
                .uv(24, 0).cuboid(-6.0F, -2.0F, -2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-3.0F, 19.0F, 0.0F));

        root.addChild("leftwing", ModelPartBuilder.create()
                .uv(14, 8).cuboid(0.0F, -2.0F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(10, 20).cuboid(2.0F, -2.0F, -2.0F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F))
                .uv(0, 23).cuboid(4.0F, -2.0F, -2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(24, 4).cuboid(6.0F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.pivot(3.0F, 19.0F, 0.0F));

        root.addChild("bb_main", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-3.0F, -7.0F, -3.0F, 6.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(-2.0F, -7.0F, -6.0F, 4.0F, 2.0F, 3.0F, new Dilation(0.0F))
                .uv(0, 13).cuboid(-2.0F, -7.0F, 3.0F, 4.0F, 2.0F, 3.0F, new Dilation(0.0F))
                .uv(0, 18).cuboid(-1.0F, -7.0F, 6.0F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(CrowEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch) {
        this.rightWing.yaw = ageInTicks * 0.5F;
        this.leftWing.yaw = -this.rightWing.yaw;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green,
            float blue, float alpha) {
        root.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }
}
