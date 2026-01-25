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
public class BankBlockModel extends EntityModel<Entity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            new Identifier(GardenKingMod.MOD_ID, "bank_block"),
            "main"
    );

    private final ModelPart leftCase;
    private final ModelPart rightCase;
    private final ModelPart bbMain;

    public BankBlockModel(ModelPart root) {
        this.leftCase = root.getChild("left_case");
        this.rightCase = root.getChild("right_case");
        this.bbMain = root.getChild("bb_main");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild("left_case", ModelPartBuilder.create().uv(64, 0).cuboid(-1.0F, 0.0F, -3.0F, 2.0F, 5.0F, 8.0F,
                        new Dilation(0.0F))
                .uv(66, 59).cuboid(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 7.0F, new Dilation(0.0F))
                .uv(18, 68).cuboid(-1.0F, -4.0F, -3.0F, 2.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(64, 26).cuboid(-1.0F, -6.0F, -3.0F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F)),
                ModelTransform.pivot(7.0F, -2.0F, 3.0F));

        modelPartData.addChild("right_case",
                ModelPartBuilder.create().uv(32, 73).cuboid(-1.0F, 4.0F, -3.0F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F))
                        .uv(66, 68).cuboid(-1.0F, 6.0F, -3.0F, 2.0F, 2.0F, 5.0F, new Dilation(0.0F))
                        .uv(0, 68).cuboid(-1.0F, 8.0F, -3.0F, 2.0F, 2.0F, 7.0F, new Dilation(0.0F))
                        .uv(64, 13).cuboid(-1.0F, 10.0F, -3.0F, 2.0F, 5.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-7.0F, -12.0F, 3.0F));

        ModelPartData bbMain = modelPartData.addChild("bb_main",
                ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F))
                        .uv(0, 32).cuboid(-8.0F, -32.0F, -8.0F, 16.0F, 16.0F, 8.0F, new Dilation(0.0F))
                        .uv(48, 46).cuboid(-8.0F, -21.0F, 0.0F, 16.0F, 5.0F, 8.0F, new Dilation(0.0F))
                        .uv(48, 32).cuboid(-8.0F, -35.0F, -8.0F, 16.0F, 3.0F, 11.0F, new Dilation(0.0F))
                        .uv(0, 56).cuboid(-8.0F, -44.0F, -4.0F, 16.0F, 9.0F, 3.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        bbMain.addChild("screen_r1",
                ModelPartBuilder.create().uv(38, 59).cuboid(-6.0F, -9.0F, -1.0F, 12.0F, 12.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, -23.0F, 3.0F, 0.48F, 0.0F, 0.0F));

        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green,
            float blue, float alpha) {
        leftCase.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        rightCase.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        bbMain.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }
}
