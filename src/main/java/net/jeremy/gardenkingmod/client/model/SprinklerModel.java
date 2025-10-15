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
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class SprinklerModel extends EntityModel<Entity> {
        public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
                        new Identifier(GardenKingMod.MOD_ID, "sprinkler"), "main");
        /**
         * Place future animation keyframes at
         * {@code assets/gardenkingmod/animations/sprinkler.animation.json}.
         */

        private final ModelPart rotation;
        private final ModelPart cap4;
        private final ModelPart bbMain;

        private float rotationAngle;

        public SprinklerModel(ModelPart root) {
                this.rotation = root.getChild("rotation");
                this.cap4 = root.getChild("cap4");
                this.bbMain = root.getChild("bb_main");
        }

        public static TexturedModelData getTexturedModelData() {
                ModelData modelData = new ModelData();
                ModelPartData modelPartData = modelData.getRoot();
                ModelPartData rotation = modelPartData.addChild("rotation",
                                ModelPartBuilder.create().uv(8, 31).cuboid(-9.0F, -6.0F, -1.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                                                .uv(12, 31).cuboid(11.0F, -3.0F, -1.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                                                .uv(0, 27).cuboid(1.0F, -6.0F, -1.0F, 1.0F, 5.0F, 1.0F, new Dilation(0.0F)),
                                ModelTransform.pivot(-1.5F, -4.0F, 0.5F));

                rotation.addChild("cube_r1", ModelPartBuilder.create().uv(24, 11).cuboid(0.0F, -8.0F, 0.0F, 1.0F, 10.0F, 1.0F,
                                new Dilation(0.0F)), ModelTransform.of(10.0F, -3.0F, -1.0F, 0.0F, 0.0F, -1.5708F));

                rotation.addChild("cube_r2", ModelPartBuilder.create().uv(24, 0).cuboid(0.0F, -10.0F, 0.0F, 1.0F, 10.0F, 1.0F,
                                new Dilation(0.0F)), ModelTransform.of(1.0F, -3.0F, -1.0F, 0.0F, 0.0F, -1.5708F));

                rotation.addChild("cap3",
                                ModelPartBuilder.create().uv(22, 28).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 1.0F, 1.0F,
                                                new Dilation(0.0F))
                                                .uv(28, 10).cuboid(0.5F, -2.0F, 0.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                                                .uv(28, 28).cuboid(2.0F, -2.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                                                .uv(28, 13).cuboid(1.5F, -2.0F, 0.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                                                .uv(28, 16).cuboid(1.5F, -2.0F, -3.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                                                .uv(20, 33).cuboid(2.5F, -2.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                                                .uv(24, 33).cuboid(-0.5F, -2.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                                                .uv(28, 33).cuboid(2.5F, -2.0F, 0.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                                                .uv(32, 33).cuboid(-0.5F, -2.0F, 0.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                                                .uv(28, 19).cuboid(0.5F, -2.0F, -3.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F)),
                                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

                rotation.addChild("cap2",
                                ModelPartBuilder.create().uv(16, 31).cuboid(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F,
                                                new Dilation(0.0F))
                                                .uv(32, 22).cuboid(0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                                                .uv(32, 24).cuboid(2.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                                                .uv(32, 26).cuboid(1.5F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                                                .uv(0, 33).cuboid(1.5F, -1.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                                                .uv(16, 33).cuboid(0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)),
                                ModelTransform.pivot(0.0F, -5.0F, 0.0F));

                modelPartData.addChild("cap4",
                                ModelPartBuilder.create().uv(28, 4).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
                                                .uv(4, 27).cuboid(0.5F, -2.0F, 0.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
                                                .uv(28, 7).cuboid(2.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
                                                .uv(10, 27).cuboid(1.5F, -2.0F, 0.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
                                                .uv(16, 27).cuboid(1.5F, -2.0F, -3.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
                                                .uv(22, 30).cuboid(2.5F, -2.0F, -2.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                                                .uv(26, 30).cuboid(-0.5F, -2.0F, -2.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                                                .uv(30, 30).cuboid(2.5F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                                                .uv(4, 31).cuboid(-0.5F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                                                .uv(28, 0).cuboid(0.5F, -2.0F, -3.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F)),
                                ModelTransform.pivot(-1.5F, 1.0F, 0.5F));

                ModelPartData bbMain = modelPartData.addChild("bb_main",
                                ModelPartBuilder.create().uv(24, 22).cuboid(-1.0F, -29.0F, -1.0F, 2.0F, 4.0F, 2.0F,
                                                new Dilation(0.0F)),
                                ModelTransform.pivot(0.0F, 24.0F, 0.0F));

                bbMain.addChild("cube_r3",
                                ModelPartBuilder.create().uv(16, 0).cuboid(-1.0F, -24.0F, -1.0F, 2.0F, 25.0F, 2.0F,
                                                new Dilation(0.0F)),
                                ModelTransform.of(6.0F, 0.0F, -6.0F, -0.3054F, -0.7854F, 0.0F));

                bbMain.addChild("cube_r4",
                                ModelPartBuilder.create().uv(8, 0).cuboid(-1.0F, -24.0F, -1.0F, 2.0F, 25.0F, 2.0F,
                                                new Dilation(0.0F)),
                                ModelTransform.of(-6.0F, 0.0F, -6.0F, -0.3054F, 0.7854F, 0.0F));

                bbMain.addChild("cube_r5",
                                ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -24.0F, -1.0F, 2.0F, 25.0F, 2.0F,
                                                new Dilation(0.0F)),
                                ModelTransform.of(0.0F, 0.0F, 7.0F, 0.2618F, 0.0F, 0.0F));

                return TexturedModelData.of(modelData, 64, 64);
        }

        @Override
        public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                        float headPitch) {
        }

        public void setAnimationProgress(float animationProgress) {
                this.rotationAngle = animationProgress * ((float) Math.PI * 2.0F);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green,
                        float blue, float alpha) {
                this.rotation.yaw = this.rotationAngle;
                this.rotation.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
                this.cap4.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
                this.bbMain.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
}