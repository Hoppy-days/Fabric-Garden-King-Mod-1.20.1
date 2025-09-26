package net.jeremy.gardenkingmod.client.model;

// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class garden_crow extends EntityModel<Entity> {
	private final ModelPart rightwing;
	private final ModelPart leftwing;
	private final ModelPart bb_main;
	public garden_crow(ModelPart root) {
		this.rightwing = root.getChild("rightwing");
		this.leftwing = root.getChild("leftwing");
		this.bb_main = root.getChild("bb_main");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData rightwing = modelPartData.addChild("rightwing", ModelPartBuilder.create().uv(8, 25).cuboid(-8.0F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(14, 14).cuboid(-2.0F, -2.0F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
		.uv(20, 20).cuboid(-4.0F, -2.0F, -2.0F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F))
		.uv(24, 0).cuboid(-6.0F, -2.0F, -2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.0F, 19.0F, 0.0F));

		ModelPartData leftwing = modelPartData.addChild("leftwing", ModelPartBuilder.create().uv(14, 8).cuboid(0.0F, -2.0F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
		.uv(10, 20).cuboid(2.0F, -2.0F, -2.0F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F))
		.uv(0, 23).cuboid(4.0F, -2.0F, -2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
		.uv(24, 4).cuboid(6.0F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(3.0F, 19.0F, 0.0F));

		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -7.0F, -3.0F, 6.0F, 2.0F, 6.0F, new Dilation(0.0F))
		.uv(0, 8).cuboid(-2.0F, -7.0F, -6.0F, 4.0F, 2.0F, 3.0F, new Dilation(0.0F))
		.uv(0, 13).cuboid(-2.0F, -7.0F, 3.0F, 4.0F, 2.0F, 3.0F, new Dilation(0.0F))
		.uv(0, 18).cuboid(-1.0F, -7.0F, 6.0F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 32, 32);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		rightwing.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		leftwing.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}