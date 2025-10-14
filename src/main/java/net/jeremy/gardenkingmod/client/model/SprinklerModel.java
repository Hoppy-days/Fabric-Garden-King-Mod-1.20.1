package net.jeremy.gardenkingmod.client.model;

// Made with Blockbench 5.0.1
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class SprinklerModel extends EntityModel<Entity> {
	private final ModelPart rotation;
	private final ModelPart cap3;
	private final ModelPart cap2;
	private final ModelPart cap4;
	private final ModelPart bb_main;
	public SprinklerModel(ModelPart root) {
		this.rotation = root.getChild("rotation");
		this.cap3 = this.rotation.getChild("cap3");
		this.cap2 = this.rotation.getChild("cap2");
		this.cap4 = root.getChild("cap4");
		this.bb_main = root.getChild("bb_main");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData rotation = modelPartData.addChild("rotation", ModelPartBuilder.create().uv(8, 31).cuboid(-9.0F, -6.0F, -1.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(12, 31).cuboid(11.0F, -3.0F, -1.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 27).cuboid(1.0F, -6.0F, -1.0F, 1.0F, 5.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-1.5F, -4.0F, 0.5F));

		ModelPartData cube_r1 = rotation.addChild("cube_r1", ModelPartBuilder.create().uv(24, 11).cuboid(0.0F, -8.0F, 0.0F, 1.0F, 10.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(10.0F, -3.0F, -1.0F, 0.0F, 0.0F, -1.5708F));

		ModelPartData cube_r2 = rotation.addChild("cube_r2", ModelPartBuilder.create().uv(24, 0).cuboid(0.0F, -10.0F, 0.0F, 1.0F, 10.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, -3.0F, -1.0F, 0.0F, 0.0F, -1.5708F));

		ModelPartData cap3 = rotation.addChild("cap3", ModelPartBuilder.create().uv(22, 28).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(28, 10).cuboid(0.5F, -2.0F, 0.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
		.uv(28, 28).cuboid(2.0F, -2.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(28, 13).cuboid(1.5F, -2.0F, 0.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
		.uv(28, 16).cuboid(1.5F, -2.0F, -3.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
		.uv(20, 33).cuboid(2.5F, -2.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(24, 33).cuboid(-0.5F, -2.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(28, 33).cuboid(2.5F, -2.0F, 0.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(32, 33).cuboid(-0.5F, -2.0F, 0.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(28, 19).cuboid(0.5F, -2.0F, -3.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		ModelPartData cap2 = rotation.addChild("cap2", ModelPartBuilder.create().uv(16, 31).cuboid(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(32, 22).cuboid(0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(32, 24).cuboid(2.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(32, 26).cuboid(1.5F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 33).cuboid(1.5F, -1.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(16, 33).cuboid(0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -5.0F, 0.0F));

		ModelPartData cap4 = modelPartData.addChild("cap4", ModelPartBuilder.create().uv(28, 4).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(4, 27).cuboid(0.5F, -2.0F, 0.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
		.uv(28, 7).cuboid(2.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(10, 27).cuboid(1.5F, -2.0F, 0.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
		.uv(16, 27).cuboid(1.5F, -2.0F, -3.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
		.uv(22, 30).cuboid(2.5F, -2.0F, -2.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(26, 30).cuboid(-0.5F, -2.0F, -2.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(30, 30).cuboid(2.5F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(4, 31).cuboid(-0.5F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(28, 0).cuboid(0.5F, -2.0F, -3.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-1.5F, 1.0F, 0.5F));

		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(24, 22).cuboid(-1.0F, -29.0F, -1.0F, 2.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData cube_r3 = bb_main.addChild("cube_r3", ModelPartBuilder.create().uv(16, 0).cuboid(-1.0F, -24.0F, -1.0F, 2.0F, 25.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(6.0F, 0.0F, -6.0F, -0.3054F, -0.7854F, 0.0F));

		ModelPartData cube_r4 = bb_main.addChild("cube_r4", ModelPartBuilder.create().uv(8, 0).cuboid(-1.0F, -24.0F, -1.0F, 2.0F, 25.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-6.0F, 0.0F, -6.0F, -0.3054F, 0.7854F, 0.0F));

		ModelPartData cube_r5 = bb_main.addChild("cube_r5", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -24.0F, -1.0F, 2.0F, 25.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 7.0F, 0.2618F, 0.0F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		rotation.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		cap4.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}