package net.jeremy.gardenkingmod.client.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Blockbench 4.10.0 export
 */
// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class market_block modded entity<T extends Entity> extends EntityModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "market_block- modded entity"), "main");
    private final ModelPart bone;
    private final ModelPart bb_main;

    public market_block- modded entity(ModelPart root) {
        this.bone = root.getChild("bone");
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(23.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 78).addBox(-1.0F, -2.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(34, 78).addBox(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 8.0F, 1.5708F, 0.0F, 3.1416F));

        PartDefinition cube_r3 = bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(34, 78).addBox(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, -6.0F, 1.5708F, 0.0F, 3.1416F));

        PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 78).addBox(-1.0F, -2.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 44).addBox(8.0F, -16.0F, -8.0F, 14.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(124, 0).addBox(-24.0F, -45.0F, 21.0F, 3.0F, 45.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(124, 0).addBox(21.0F, -45.0F, 21.0F, 3.0F, 45.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(104, 83).addBox(22.0F, -14.4F, -10.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(104, 87).addBox(7.0F, -10.0F, -10.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(103, 87).addBox(-9.0F, -10.0F, -10.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(104, 83).addBox(-24.0F, -14.4F, -10.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 85).addBox(22.0F, -9.0F, -24.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 85).addBox(7.0F, -9.0F, -24.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 85).addBox(-9.0F, -9.0F, -24.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 85).addBox(-24.0F, -9.0F, -24.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 88).addBox(9.0F, -7.0F, -23.0F, 13.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 124).addBox(-22.0F, -14.4F, -10.0F, 44.0F, 4.4F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(-24.0F, -48.0F, -8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(124, 0).addBox(-24.0F, -45.0F, -8.0F, 3.0F, 29.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(124, 0).addBox(21.0F, -45.0F, -8.0F, 3.0F, 29.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(-24.0F, -48.0F, 8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(-24.0F, -48.0F, -24.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(-8.0F, -48.0F, 8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(-8.0F, -48.0F, -8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(-8.0F, -48.0F, -24.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(8.0F, -48.0F, 8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(8.0F, -48.0F, -8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(8.0F, -48.0F, -24.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 77).addBox(-24.0F, -16.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 77).addBox(-24.0F, -2.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-21.0F, -19.0F, -8.0F, 42.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(64, 76).addBox(8.0F, -4.0F, -24.0F, 16.0F, 4.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(64, 76).addBox(-8.0F, -4.0F, -24.0F, 16.0F, 4.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(64, 76).addBox(-24.0F, -4.0F, -24.0F, 16.0F, 4.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(60, 44).addBox(-22.0F, -16.0F, -8.0F, 14.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 88).addBox(-7.0F, -7.0F, -23.0F, 14.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 88).addBox(-22.0F, -7.0F, -23.0F, 13.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(22.0F, -8.0F, -22.0F, 1.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 61).addBox(22.0F, -10.0F, -19.5F, 1.0F, 2.0F, 9.5F, new CubeDeformation(0.0F))
                .texOffs(1, 69).addBox(22.0F, -12.0F, -14.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(7.2F, -8.0F, -22.0F, 1.6F, 4.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 64).addBox(7.2F, -10.0F, -19.5F, 1.6F, 2.0F, 9.5F, new CubeDeformation(0.0F))
                .texOffs(0, 70).addBox(7.2F, -12.0F, -14.0F, 1.6F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 59).addBox(-8.8F, -8.0F, -22.0F, 1.6F, 4.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(-8.8F, -10.0F, -19.5F, 1.6F, 2.0F, 9.5F, new CubeDeformation(0.0F))
                .texOffs(0, 69).addBox(-8.8F, -12.0F, -14.0F, 1.6F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(-23.2F, -8.0F, -22.0F, 1.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(-23.2F, -10.0F, -19.5F, 1.0F, 2.0F, 9.5F, new CubeDeformation(0.0F))
                .texOffs(0, 70).addBox(-23.2F, -12.0F, -14.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition traycenter_r1 = bb_main.addOrReplaceChild("traycenter_r1", CubeListBuilder.create().texOffs(129, 54).addBox(-4.1F, -2.0F, -2.0F, 15.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(129, 54).addBox(26.9F, -2.0F, -2.0F, 15.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(129, 54).addBox(11.9F, -2.0F, -2.0F, 15.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-19.0F, -3.8F, -20.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r5 = bb_main.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 78).addBox(-1.9F, -2.0F, -2.0F, 1.8F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(29, 77).addBox(13.1F, -2.0F, -2.0F, 1.8F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(29, 77).addBox(29.1F, -2.0F, -2.0F, 1.8F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(28, 77).addBox(44.1F, -2.0F, -2.0F, 1.8F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.0F, -7.8F, -20.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r6 = bb_main.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(35, 77).addBox(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, -8.0F, -8.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r7 = bb_main.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(35, 78).addBox(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, -8.0F, 6.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}

public class MarketStandModel {
    private final ModelPart root;
    private final ModelPart marketStand;

    public MarketStandModel(ModelPart root) {
        this.root = root;
        this.marketStand = root.getChild("market_stand");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData marketStand = modelPartData.addChild("market_stand", ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        marketStand.addChild("base", ModelPartBuilder.create().uv(0, 34)
                        .cuboid(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("counter", ModelPartBuilder.create().uv(0, 16)
                        .cuboid(-7.0F, -12.0F, -6.0F, 14.0F, 2.0F, 12.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("shelf", ModelPartBuilder.create().uv(0, 0)
                        .cuboid(-6.0F, -9.0F, -5.0F, 12.0F, 1.0F, 10.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("front_trim", ModelPartBuilder.create().uv(64, 0)
                        .cuboid(-7.0F, -14.0F, -7.5F, 14.0F, 4.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("back_trim", ModelPartBuilder.create().uv(64, 5)
                        .cuboid(-7.0F, -14.0F, 6.5F, 14.0F, 4.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("left_trim", ModelPartBuilder.create().uv(64, 10)
                        .cuboid(-7.5F, -14.0F, -6.0F, 1.0F, 4.0F, 12.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("right_trim", ModelPartBuilder.create().uv(64, 26)
                        .cuboid(6.5F, -14.0F, -6.0F, 1.0F, 4.0F, 12.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("front_awning", ModelPartBuilder.create().uv(0, 40)
                        .cuboid(-8.0F, -20.0F, -10.0F, 16.0F, 2.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("rear_awning", ModelPartBuilder.create().uv(0, 46)
                        .cuboid(-8.0F, -20.0F, 6.0F, 16.0F, 2.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("left_awning", ModelPartBuilder.create().uv(0, 52)
                        .cuboid(-10.0F, -20.0F, -6.0F, 4.0F, 2.0F, 12.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("right_awning", ModelPartBuilder.create().uv(32, 52)
                        .cuboid(6.0F, -20.0F, -6.0F, 4.0F, 2.0F, 12.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("roof", ModelPartBuilder.create().uv(0, 56)
                        .cuboid(-9.0F, -24.0F, -9.0F, 18.0F, 2.0F, 18.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("front_banner", ModelPartBuilder.create().uv(68, 40)
                        .cuboid(-6.0F, -12.0F, -8.5F, 12.0F, 8.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, -8.0F, 0.0F));

        marketStand.addChild("left_post", ModelPartBuilder.create().uv(64, 40)
                        .cuboid(-1.0F, -24.0F, -1.0F, 2.0F, 24.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-6.0F, 0.0F, -6.0F));

        marketStand.addChild("right_post", ModelPartBuilder.create().uv(72, 40)
                        .cuboid(-1.0F, -24.0F, -1.0F, 2.0F, 24.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(6.0F, 0.0F, -6.0F));

        marketStand.addChild("rear_left_post", ModelPartBuilder.create().uv(80, 40)
                        .cuboid(-1.0F, -24.0F, -1.0F, 2.0F, 24.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-6.0F, 0.0F, 6.0F));

        marketStand.addChild("rear_right_post", ModelPartBuilder.create().uv(88, 40)
                        .cuboid(-1.0F, -24.0F, -1.0F, 2.0F, 24.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(6.0F, 0.0F, 6.0F));

        marketStand.addChild("lower_shelf", ModelPartBuilder.create().uv(48, 16)
                        .cuboid(-6.0F, -6.0F, -4.0F, 12.0F, 1.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("support_beam", ModelPartBuilder.create().uv(48, 25)
                        .cuboid(-8.0F, -18.0F, -1.0F, 16.0F, 2.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("side_beam_left", ModelPartBuilder.create().uv(48, 29)
                        .cuboid(-1.0F, -18.0F, -8.0F, 2.0F, 2.0F, 16.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-6.0F, 0.0F, 0.0F));

        marketStand.addChild("side_beam_right", ModelPartBuilder.create().uv(48, 33)
                        .cuboid(-1.0F, -18.0F, -8.0F, 2.0F, 2.0F, 16.0F, new Dilation(0.0F)),
                ModelTransform.pivot(6.0F, 0.0F, 0.0F));

        marketStand.addChild("counter_support_front", ModelPartBuilder.create().uv(48, 37)
                        .cuboid(-7.0F, -10.0F, -7.0F, 14.0F, 1.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        marketStand.addChild("counter_support_back", ModelPartBuilder.create().uv(48, 39)
                        .cuboid(-7.0F, -10.0F, 6.0F, 14.0F, 1.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        return TexturedModelData.of(modelData, 128, 128);
    }

    public ModelPart getRoot() {
        return this.root;
    }

    public ModelPart getMarketStand() {
        return this.marketStand;
    }

    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
                       float red, float green, float blue, float alpha) {
        this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}