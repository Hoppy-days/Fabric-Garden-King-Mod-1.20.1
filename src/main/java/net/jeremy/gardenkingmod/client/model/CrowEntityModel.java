package net.jeremy.gardenkingmod.client.model;


import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.entity.crow.CrowEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CrowEntityModel extends SinglePartEntityModel<CrowEntity> {
        public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
                        new Identifier(GardenKingMod.MOD_ID, "crow"),
                        "main");

        private final ModelPart root;
        private final ModelPart body;
        private final ModelPart leftWing;
        private final ModelPart rightWing;
        private final ModelPart head;

        public CrowEntityModel(ModelPart root) {
                this.root = root;
                this.body = root.getChild("body");
                this.leftWing = body.getChild("left_wing");
                this.rightWing = body.getChild("right_wing");
                this.head = body.getChild("head");
        }

        public static TexturedModelData getTexturedModelData() {
                ModelData modelData = new ModelData();
                ModelPartData root = modelData.getRoot();
                ModelPartData body = root.addChild("body",
                                ModelPartBuilder.create()
                                                .uv(0, 0)
                                                .cuboid(-3.0F, -3.0F, -4.0F, 6.0F, 6.0F, 8.0F),
                                ModelTransform.pivot(0.0F, 16.0F, 0.0F));

                body.addChild("tail",
                                ModelPartBuilder.create()
                                                .uv(0, 14)
                                                .cuboid(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 4.0F),
                                ModelTransform.of(0.0F, 1.0F, 4.0F, -0.1745F, 0.0F, 0.0F));

                ModelPartData head = body.addChild("head",
                                ModelPartBuilder.create()
                                                .uv(24, 0)
                                                .cuboid(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 3.0F)
                                                .uv(24, 7)
                                                .cuboid(-1.0F, -1.0F, -5.0F, 2.0F, 2.0F, 2.0F),
                                ModelTransform.pivot(0.0F, -1.0F, -4.0F));

                head.addChild("beak",
                                ModelPartBuilder.create()
                                                .uv(34, 7)
                                                .cuboid(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 3.0F),
                                ModelTransform.pivot(0.0F, 0.0F, -5.0F));

                body.addChild("left_wing",
                                ModelPartBuilder.create()
                                                .uv(0, 20)
                                                .cuboid(0.0F, -1.0F, -3.0F, 1.0F, 4.0F, 6.0F),
                                ModelTransform.pivot(3.0F, -1.0F, 0.0F));

                body.addChild("right_wing",
                                ModelPartBuilder.create()
                                                .uv(14, 20)
                                                .cuboid(-1.0F, -1.0F, -3.0F, 1.0F, 4.0F, 6.0F),
                                ModelTransform.pivot(-3.0F, -1.0F, 0.0F));

                body.addChild("legs",
                                ModelPartBuilder.create()
                                                .uv(28, 12)
                                                .cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                                ModelTransform.pivot(0.0F, 3.0F, -1.0F));

                return TexturedModelData.of(modelData, 64, 32);
        }

        @Override
        public void setAngles(CrowEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                        float headPitch) {
                this.head.yaw = netHeadYaw * ((float) Math.PI / 180F);
                this.head.pitch = headPitch * ((float) Math.PI / 180F);

                float flap = (float) Math.cos(ageInTicks * 0.6F) * 0.6F * limbSwingAmount + 0.3F;
                this.leftWing.roll = flap;
                this.rightWing.roll = -flap;
        }

        @Override
        public ModelPart getPart() {
                return this.root;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green,
                        float blue, float alpha) {
                this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import net.jeremy.gardenkingmod.entity.crow.CrowEntity;

/**
 * Simple crow model built around a single textured cube body with animated
 * wings. The geometry intentionally mirrors vanilla-style bird models so
 * resource packs can reuse textures without a custom loader.
 */
public class CrowEntityModel extends SinglePartEntityModel<CrowEntity> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;

    public CrowEntityModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.tail = body.getChild("tail");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        ModelPartData body = root.addChild("body",
                ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -2.0f, -4.0f, 6.0f, 4.0f, 8.0f),
                ModelTransform.pivot(0.0f, 16.0f, 0.0f));

        body.addChild("head",
                ModelPartBuilder.create().uv(0, 12).cuboid(-2.0f, -4.0f, -3.0f, 4.0f, 4.0f, 3.0f)
                        .uv(14, 12).cuboid(-1.0f, -2.0f, -5.0f, 2.0f, 1.0f, 2.0f),
                ModelTransform.pivot(0.0f, -1.0f, -4.0f));

        body.addChild("left_wing",
                ModelPartBuilder.create().uv(20, 0).cuboid(0.0f, -1.0f, -4.0f, 1.0f, 3.0f, 8.0f),
                ModelTransform.pivot(3.0f, -1.0f, 0.0f));

        body.addChild("right_wing",
                ModelPartBuilder.create().uv(20, 0).mirrored().cuboid(-1.0f, -1.0f, -4.0f, 1.0f, 3.0f, 8.0f),
                ModelTransform.pivot(-3.0f, -1.0f, 0.0f));

        body.addChild("tail",
                ModelPartBuilder.create().uv(0, 19).cuboid(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 4.0f),
                ModelTransform.pivot(0.0f, 1.5f, 4.0f));

        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(CrowEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw,
            float headPitch) {
        head.yaw = headYaw * (MathHelper.PI / 180.0f);
        head.pitch = headPitch * (MathHelper.PI / 180.0f);

        float flap = MathHelper.cos(animationProgress * 0.6f) * (float) Math.PI * 0.25f;
        if (entity.isOnGround() || entity.getVelocity().lengthSquared() < 0.01f) {
            flap = 0.05f;
        }

        leftWing.roll = flap;
        rightWing.roll = -flap;
        tail.pitch = -0.35f + MathHelper.cos(animationProgress * 0.2f) * 0.05f;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green,
            float blue, float alpha) {
        root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }
}