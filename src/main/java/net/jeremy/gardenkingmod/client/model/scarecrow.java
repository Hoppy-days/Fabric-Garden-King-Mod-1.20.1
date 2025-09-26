package net.jeremy.gardenkingmod.client.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class scarecrow extends EntityModel<Entity> {
	private final ModelPart hatbrim;
	private final ModelPart lefteye;
	private final ModelPart righteye;
	private final ModelPart bb_main;
	public scarecrow(ModelPart root) {
		this.hatbrim = root.getChild("hatbrim");
		this.lefteye = root.getChild("lefteye");
		this.righteye = root.getChild("righteye");
		this.bb_main = root.getChild("bb_main");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData hatbrim = modelPartData.addChild("hatbrim", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData brim2_r1 = hatbrim.addChild("brim2_r1", ModelPartBuilder.create().uv(50, 73).cuboid(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(60, 75).cuboid(-4.0F, -1.0F, -2.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(49, 67).cuboid(-5.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.0F, -39.0F, 0.0F, 0.0F, -1.5708F, -0.2182F));

		ModelPartData brim1_r1 = hatbrim.addChild("brim1_r1", ModelPartBuilder.create().uv(49, 69).cuboid(-5.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(59, 70).cuboid(-4.0F, -1.0F, -2.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(63, 76).cuboid(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -39.0F, 0.0F, 0.0F, 1.5708F, 0.2182F));

		ModelPartData brim2_r2 = hatbrim.addChild("brim2_r2", ModelPartBuilder.create().uv(65, 66).cuboid(-3.0F, -1.0F, -1.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -39.0F, -6.0F, -0.2182F, 0.0F, 0.0F));

		ModelPartData brim2_r3 = hatbrim.addChild("brim2_r3", ModelPartBuilder.create().uv(49, 76).cuboid(-4.0F, -1.0F, -1.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -39.0F, -5.0F, -0.2182F, 0.0F, 0.0F));

		ModelPartData brim1_r2 = hatbrim.addChild("brim1_r2", ModelPartBuilder.create().uv(49, 65).cuboid(-5.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(49, 78).cuboid(-4.0F, -1.0F, 0.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(54, 69).cuboid(-3.0F, -1.0F, 1.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -39.0F, 5.0F, 0.2182F, 0.0F, 0.0F));

		ModelPartData brim1_r3 = hatbrim.addChild("brim1_r3", ModelPartBuilder.create().uv(57, 78).cuboid(-5.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -39.0F, -4.0F, -0.2182F, 0.0F, 0.0F));

		ModelPartData lefteye = modelPartData.addChild("lefteye", ModelPartBuilder.create().uv(2, 2).cuboid(-0.5F, -1.5F, 1.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-2.0F, -12.5F, -5.2F, 0.0F, 0.0F, 0.7854F));

		ModelPartData cube_r1 = lefteye.addChild("cube_r1", ModelPartBuilder.create().uv(2, 2).cuboid(-1.0F, -2.0F, 1.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 1.5708F));

		ModelPartData righteye = modelPartData.addChild("righteye", ModelPartBuilder.create().uv(2, 2).cuboid(-0.5F, -1.5F, 1.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(2.0F, -12.5F, -5.2F, 0.0F, 0.0F, 0.7854F));

		ModelPartData cube_r2 = righteye.addChild("cube_r2", ModelPartBuilder.create().uv(2, 2).cuboid(-1.0F, -2.0F, 1.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 1.5708F));

		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 4).cuboid(-5.0F, -2.0F, -5.0F, 10.0F, 2.0F, 10.0F, new Dilation(0.0F))
		.uv(36, 32).cuboid(-4.0F, -4.0F, -4.0F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
		.uv(0, 88).cuboid(-1.0F, -38.0F, -1.0F, 2.0F, 34.0F, 2.0F, new Dilation(0.0F))
		.uv(0, 124).cuboid(-19.0F, -28.0F, -1.0F, 37.0F, 2.0F, 2.0F, new Dilation(0.0F))
		.uv(17, 89).cuboid(-4.0F, -31.0F, -3.0F, 8.0F, 16.0F, 6.0F, new Dilation(0.0F))
		.uv(17, 103).cuboid(4.0F, -31.0F, -3.0F, 9.0F, 3.0F, 6.0F, new Dilation(0.0F))
		.uv(17, 102).cuboid(-13.0F, -31.0F, -3.0F, 9.0F, 3.0F, 6.0F, new Dilation(0.0F))
		.uv(28, 16).cuboid(-4.0F, -39.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F))
		.uv(40, 4).cuboid(-4.0F, -40.0F, -4.0F, 8.0F, 1.0F, 8.0F, new Dilation(0.0F))
		.uv(0, 38).cuboid(-3.0F, -42.0F, -3.0F, 6.0F, 2.0F, 6.0F, new Dilation(0.0F))
		.uv(0, 52).cuboid(-2.0F, -43.0F, -2.0F, 4.0F, 1.0F, 4.0F, new Dilation(0.0F))
		.uv(21, 69).cuboid(-1.0F, -15.0F, -3.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
		.uv(27, 73).cuboid(3.0F, -15.0F, -3.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
		.uv(36, 71).cuboid(-4.0F, -15.0F, -3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(22, 67).cuboid(4.0F, -15.0F, -3.0F, 0.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(19, 70).cuboid(4.0F, -15.0F, -2.0F, 0.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(22, 82).cuboid(2.0F, -15.0F, -3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(25, 68).cuboid(4.0F, -15.0F, 0.0F, 0.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(25, 68).cuboid(4.0F, -15.0F, 2.0F, 0.0F, 3.0F, 1.0F, new Dilation(0.0F))
		.uv(38, 74).cuboid(3.0F, -15.0F, 3.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
		.uv(23, 80).cuboid(2.0F, -15.0F, 3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(38, 73).cuboid(-1.0F, -15.0F, 3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(41, 77).cuboid(-3.0F, -15.0F, 3.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
		.uv(25, 82).cuboid(-4.0F, -15.0F, 3.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
		.uv(24, 72).cuboid(-4.0F, -15.0F, -3.0F, 0.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(25, 70).cuboid(-4.0F, -15.0F, -1.0F, 0.0F, 3.0F, 2.0F, new Dilation(0.0F))
		.uv(26, 71).cuboid(-4.0F, -15.0F, 2.0F, 0.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(17, 85).cuboid(4.0F, -28.0F, -3.0F, 9.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(17, 80).cuboid(-13.0F, -28.0F, -3.0F, 9.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(28, 95).cuboid(-13.0F, -28.0F, 3.0F, 9.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(29, 97).cuboid(4.0F, -28.0F, 3.0F, 9.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(15, 66).cuboid(4.0F, -26.0F, -3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(28, 69).cuboid(4.0F, -24.0F, -3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(3, 73).cuboid(-6.0F, -26.0F, -3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(0, 68).cuboid(-5.0F, -24.0F, -3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(24, 71).cuboid(11.0F, -26.0F, -3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(5, 69).cuboid(-13.0F, -26.0F, -3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(26, 69).cuboid(-6.0F, -26.0F, 3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(5, 71).cuboid(-5.0F, -24.0F, 3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(19, 67).cuboid(-13.0F, -26.0F, 3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(23, 72).cuboid(4.0F, -26.0F, 3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(5, 70).cuboid(4.0F, -24.0F, 3.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
		.uv(25, 71).cuboid(11.0F, -26.0F, 3.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 128, 128);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		hatbrim.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		lefteye.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		righteye.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}