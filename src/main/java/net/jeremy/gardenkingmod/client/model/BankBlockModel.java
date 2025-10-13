package net.jeremy.gardenkingmod.client.model;

// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class bank_block extends EntityModel<Entity> {
	private final ModelPart left_case;
	private final ModelPart right_case;
	private final ModelPart bb_main;
	public bank_block(ModelPart root) {
		this.left_case = root.getChild("left_case");
		this.right_case = root.getChild("right_case");
		this.bb_main = root.getChild("bb_main");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData left_case = modelPartData.addChild("left_case", ModelPartBuilder.create().uv(64, 0).cuboid(-1.0F, 0.0F, -3.0F, 2.0F, 5.0F, 8.0F, new Dilation(0.0F))
		.uv(66, 59).cuboid(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 7.0F, new Dilation(0.0F))
		.uv(18, 68).cuboid(-1.0F, -4.0F, -3.0F, 2.0F, 2.0F, 5.0F, new Dilation(0.0F))
		.uv(64, 26).cuboid(-1.0F, -6.0F, -3.0F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(7.0F, -2.0F, 3.0F));

		ModelPartData right_case = modelPartData.addChild("right_case", ModelPartBuilder.create().uv(32, 73).cuboid(-1.0F, 4.0F, -3.0F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F))
		.uv(66, 68).cuboid(-1.0F, 6.0F, -3.0F, 2.0F, 2.0F, 5.0F, new Dilation(0.0F))
		.uv(0, 68).cuboid(-1.0F, 8.0F, -3.0F, 2.0F, 2.0F, 7.0F, new Dilation(0.0F))
		.uv(64, 13).cuboid(-1.0F, 10.0F, -3.0F, 2.0F, 5.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(-7.0F, -12.0F, 3.0F));

		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 32).cuboid(-8.0F, -32.0F, -8.0F, 16.0F, 16.0F, 8.0F, new Dilation(0.0F))
		.uv(48, 46).cuboid(-8.0F, -21.0F, 0.0F, 16.0F, 5.0F, 8.0F, new Dilation(0.0F))
		.uv(48, 32).cuboid(-8.0F, -35.0F, -8.0F, 16.0F, 3.0F, 11.0F, new Dilation(0.0F))
		.uv(0, 56).cuboid(-8.0F, -44.0F, -4.0F, 16.0F, 9.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData screen_r1 = bb_main.addChild("screen_r1", ModelPartBuilder.create().uv(38, 59).cuboid(-6.0F, -9.0F, -1.0F, 12.0F, 12.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -23.0F, 3.0F, 0.48F, 0.0F, 0.0F));
		return TexturedModelData.of(modelData, 128, 128);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		left_case.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		right_case.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}