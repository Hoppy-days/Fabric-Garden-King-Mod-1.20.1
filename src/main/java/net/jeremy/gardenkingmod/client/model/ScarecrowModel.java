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

    private final ModelPart bb_main;
    public ScarecrowModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 64).cuboid(-5.0F, -2.0F, -5.0F, 10.0F, 2.0F, 10.0F, new Dilation(0.0F))
                .uv(40, 66).cuboid(-4.0F, -4.0F, -4.0F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 28).cuboid(-1.0F, -38.0F, -1.0F, 2.0F, 34.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 60).cuboid(-19.0F, -28.0F, -1.0F, 37.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(16, 26).cuboid(-4.0F, -31.0F, -3.0F, 8.0F, 16.0F, 6.0F, new Dilation(0.0F))
                .uv(16, 39).cuboid(4.0F, -31.0F, -3.0F, 9.0F, 3.0F, 6.0F, new Dilation(0.0F))
                .uv(16, 39).cuboid(-13.0F, -31.0F, -3.0F, 9.0F, 3.0F, 6.0F, new Dilation(0.0F))
                .uv(48, 16).cuboid(-4.0F, -39.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F))
                .uv(48, 7).cuboid(-4.0F, -40.0F, -4.0F, 8.0F, 1.0F, 8.0F, new Dilation(0.0F))
                .uv(48, 35).cuboid(-3.0F, -42.0F, -3.0F, 6.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(48, 43).cuboid(-2.0F, -43.0F, -2.0F, 4.0F, 1.0F, 4.0F, new Dilation(0.0F))
                .uv(16, 45).cuboid(-1.0F, -15.0F, -3.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(17, 44).cuboid(3.0F, -15.0F, -3.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(26, 39).cuboid(-4.0F, -15.0F, -3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(22, 40).cuboid(4.0F, -15.0F, -3.0F, 0.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(20, 38).cuboid(4.0F, -15.0F, -2.0F, 0.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(28, 44).cuboid(2.0F, -15.0F, -3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(24, 41).cuboid(4.0F, -15.0F, 0.0F, 0.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(24, 38).cuboid(4.0F, -15.0F, 2.0F, 0.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 43).cuboid(3.0F, -15.0F, 3.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(19, 37).cuboid(2.0F, -15.0F, 3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(29, 37).cuboid(-1.0F, -15.0F, 3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(36, 37).cuboid(-3.0F, -15.0F, 3.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(35, 43).cuboid(-4.0F, -15.0F, 3.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(23, 43).cuboid(-4.0F, -15.0F, -3.0F, 0.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(18, 42).cuboid(-4.0F, -15.0F, -1.0F, 0.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(20, 41).cuboid(-4.0F, -15.0F, 2.0F, 0.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(16, 17).cuboid(4.0F, -28.0F, -3.0F, 9.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(28, 45).cuboid(-13.0F, -28.0F, -3.0F, 9.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(28, 45).cuboid(-13.0F, -28.0F, 3.0F, 9.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(28, 39).cuboid(4.0F, -28.0F, 3.0F, 9.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(21, 44).cuboid(4.0F, -26.0F, -3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(22, 34).cuboid(4.0F, -24.0F, -3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(21, 40).cuboid(-6.0F, -26.0F, -3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(21, 32).cuboid(-5.0F, -24.0F, -3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(25, 42).cuboid(11.0F, -26.0F, -3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(25, 38).cuboid(-13.0F, -26.0F, -3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(25, 39).cuboid(-6.0F, -26.0F, 3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(32, 41).cuboid(-5.0F, -24.0F, 3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(35, 41).cuboid(-13.0F, -26.0F, 3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(25, 41).cuboid(4.0F, -26.0F, 3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(22, 43).cuboid(4.0F, -24.0F, 3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(25, 41).cuboid(11.0F, -26.0F, 3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData cube_r1 = bb_main.addChild("cube_r1", ModelPartBuilder.create().uv(50, 18).cuboid(-1.0F, -2.0F, 1.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(1.2929F, -36.5F, 3.1F, 0.0F, 0.0F, 2.3562F));

        ModelPartData cube_r2 = bb_main.addChild("cube_r2", ModelPartBuilder.create().uv(51, 18).cuboid(-1.0F, -2.0F, 1.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(2.0F, -35.7929F, 3.1F, 0.0F, 0.0F, 0.7854F));

        ModelPartData cube_r3 = bb_main.addChild("cube_r3", ModelPartBuilder.create().uv(50, 18).cuboid(-1.0F, -2.0F, 1.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-2.7071F, -36.5F, 3.1F, 0.0F, 0.0F, 2.3562F));

        ModelPartData cube_r4 = bb_main.addChild("cube_r4", ModelPartBuilder.create().uv(51, 18).cuboid(-1.0F, -2.0F, 1.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-2.0F, -35.7929F, 3.1F, 0.0F, 0.0F, 0.7854F));

        ModelPartData cube_r5 = bb_main.addChild("cube_r5", ModelPartBuilder.create().uv(49, 8).cuboid(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(49, 9).cuboid(-4.0F, -1.0F, -2.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(49, 13).cuboid(-5.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -39.0F, 0.0F, 0.0F, 1.5708F, 0.2182F));

        ModelPartData cube_r6 = bb_main.addChild("cube_r6", ModelPartBuilder.create().uv(65, 14).cuboid(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(62, 13).cuboid(-4.0F, -1.0F, -2.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(58, 14).cuboid(-5.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.0F, -39.0F, 0.0F, 0.0F, -1.5708F, -0.2182F));

        ModelPartData cube_r7 = bb_main.addChild("cube_r7", ModelPartBuilder.create().uv(48, 12).cuboid(-3.0F, -1.0F, 1.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(48, 12).cuboid(-4.0F, -1.0F, 0.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(49, 11).cuboid(-5.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -39.0F, 5.0F, 0.2182F, 0.0F, 0.0F));

        ModelPartData cube_r8 = bb_main.addChild("cube_r8", ModelPartBuilder.create().uv(66, 13).cuboid(-3.0F, -1.0F, -1.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -39.0F, -6.0F, -0.2182F, 0.0F, 0.0F));

        ModelPartData cube_r9 = bb_main.addChild("cube_r9", ModelPartBuilder.create().uv(49, 12).cuboid(-4.0F, -1.0F, -1.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -39.0F, -5.0F, -0.2182F, 0.0F, 0.0F));

        ModelPartData cube_r10 = bb_main.addChild("cube_r10", ModelPartBuilder.create().uv(48, 12).cuboid(-5.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -39.0F, -4.0F, -0.2182F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }
    @Override
    public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }
}
