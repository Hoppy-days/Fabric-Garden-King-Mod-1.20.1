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

public class MarketBlockModel extends EntityModel<Entity> {
        public static final EntityModelLayer LAYER_LOCATION =
                        new EntityModelLayer(new Identifier(GardenKingMod.MOD_ID, "market_block"), "main");

        private final ModelPart bone;
        private final ModelPart bbMain;

        public MarketBlockModel(ModelPart root) {
                this.bone = root.getChild("bone");
                this.bbMain = root.getChild("bb_main");
        }

        public static TexturedModelData getTexturedModelData() {
                ModelData modelData = new ModelData();
                ModelPartData root = modelData.getRoot();

                ModelPartData bone = root.addChild("bone", ModelPartBuilder.create(), ModelTransform.pivot(23.0F, 24.0F, 0.0F));

                bone.addChild("cube_r1", ModelPartBuilder.create().uv(0, 78).cuboid(-1.0F, -2.0F, -8.0F, 2.0F, 2.0F, 16.0F,
                                new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

                bone.addChild("cube_r2", ModelPartBuilder.create().uv(34, 78).cuboid(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 12.0F,
                                new Dilation(0.0F)), ModelTransform.of(0.0F, -8.0F, 8.0F, 1.5708F, 0.0F, 3.1416F));

                bone.addChild("cube_r3", ModelPartBuilder.create().uv(34, 78).cuboid(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 12.0F,
                                new Dilation(0.0F)), ModelTransform.of(0.0F, -8.0F, -6.0F, 1.5708F, 0.0F, 3.1416F));

                bone.addChild("cube_r4", ModelPartBuilder.create().uv(0, 78).cuboid(-1.0F, -2.0F, -8.0F, 2.0F, 2.0F, 16.0F,
                                new Dilation(0.0F)), ModelTransform.of(0.0F, -14.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

                ModelPartData bbMain = root.addChild("bb_main",
                                ModelPartBuilder.create().uv(0, 44).cuboid(8.0F, -16.0F, -8.0F, 14.0F, 16.0F, 16.0F,
                                                new Dilation(0.0F))
                                                .uv(124, 0).cuboid(-24.0F, -45.0F, 21.0F, 3.0F, 45.0F, 3.0F, new Dilation(0.0F))
                                                .uv(124, 0).cuboid(21.0F, -45.0F, 21.0F, 3.0F, 45.0F, 3.0F, new Dilation(0.0F))
                                                .uv(104, 83).cuboid(22.0F, -14.4F, -10.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                                                .uv(104, 87).cuboid(7.0F, -10.0F, -10.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
                                                .uv(103, 87).cuboid(-9.0F, -10.0F, -10.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
                                                .uv(104, 83).cuboid(-24.0F, -14.4F, -10.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                                                .uv(56, 85).cuboid(22.0F, -9.0F, -24.0F, 2.0F, 5.0F, 2.0F, new Dilation(0.0F))
                                                .uv(56, 85).cuboid(7.0F, -9.0F, -24.0F, 2.0F, 5.0F, 2.0F, new Dilation(0.0F))
                                                .uv(56, 85).cuboid(-9.0F, -9.0F, -24.0F, 2.0F, 5.0F, 2.0F, new Dilation(0.0F))
                                                .uv(56, 85).cuboid(-24.0F, -9.0F, -24.0F, 2.0F, 5.0F, 2.0F, new Dilation(0.0F))
                                                .uv(0, 88).cuboid(9.0F, -7.0F, -23.0F, 13.0F, 3.0F, 1.0F, new Dilation(0.0F))
                                                .uv(0, 124).cuboid(-22.0F, -14.4F, -10.0F, 44.0F, 4.4F, 2.0F, new Dilation(0.0F))
                                                .uv(0, 96).cuboid(-24.0F, -48.0F, -8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                                                .uv(124, 0).cuboid(-24.0F, -45.0F, -8.0F, 3.0F, 29.0F, 3.0F, new Dilation(0.0F))
                                                .uv(124, 0).cuboid(21.0F, -45.0F, -8.0F, 3.0F, 29.0F, 3.0F, new Dilation(0.0F))
                                                .uv(0, 96).cuboid(-24.0F, -48.0F, 8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 96).cuboid(-24.0F, -48.0F, -24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 96).cuboid(-8.0F, -48.0F, 8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 96).cuboid(-8.0F, -48.0F, -8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 96).cuboid(-8.0F, -48.0F, -24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 96).cuboid(8.0F, -48.0F, 8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 96).cuboid(8.0F, -48.0F, -8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 96).cuboid(8.0F, -48.0F, -24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 77).cuboid(-24.0F, -16.0F, -8.0F, 2.0F, 2.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 77).cuboid(-24.0F, -2.0F, -8.0F, 2.0F, 2.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 6).cuboid(-21.0F, -19.0F, -8.0F, 42.0F, 3.0F, 3.0F, new Dilation(0.0F))
                                                .uv(64, 76).cuboid(8.0F, -4.0F, -24.0F, 16.0F, 4.0F, 16.0F, new Dilation(0.0F))
                                                .uv(64, 76).cuboid(-8.0F, -4.0F, -24.0F, 16.0F, 4.0F, 16.0F, new Dilation(0.0F))
                                                .uv(64, 76).cuboid(-24.0F, -4.0F, -24.0F, 16.0F, 4.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 12).cuboid(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F))
                                                .uv(60, 44).cuboid(-22.0F, -16.0F, -8.0F, 14.0F, 16.0F, 16.0F, new Dilation(0.0F))
                                                .uv(0, 88).cuboid(-7.0F, -7.0F, -23.0F, 14.0F, 3.0F, 1.0F, new Dilation(0.0F))
                                                .uv(0, 88).cuboid(-22.0F, -7.0F, -23.0F, 13.0F, 3.0F, 1.0F, new Dilation(0.0F))
                                                .uv(0, 60).cuboid(22.0F, -8.0F, -22.0F, 1.0F, 4.0F, 12.0F, new Dilation(0.0F))
                                                .uv(0, 61).cuboid(22.0F, -10.0F, -19.5F, 1.0F, 2.0F, 9.5F, new Dilation(0.0F))
                                                .uv(1, 69).cuboid(22.0F, -12.0F, -14.0F, 1.0F, 2.0F, 4.0F, new Dilation(0.0F))
                                                .uv(0, 60).cuboid(7.2F, -8.0F, -22.0F, 1.6F, 4.0F, 12.0F, new Dilation(0.0F))
                                                .uv(0, 64).cuboid(7.2F, -10.0F, -19.5F, 1.6F, 2.0F, 9.5F, new Dilation(0.0F))
                                                .uv(0, 70).cuboid(7.2F, -12.0F, -14.0F, 1.6F, 2.0F, 4.0F, new Dilation(0.0F))
                                                .uv(0, 59).cuboid(-8.8F, -8.0F, -22.0F, 1.6F, 4.0F, 12.0F, new Dilation(0.0F))
                                                .uv(0, 60).cuboid(-8.8F, -10.0F, -19.5F, 1.6F, 2.0F, 9.5F, new Dilation(0.0F))
                                                .uv(0, 69).cuboid(-8.8F, -12.0F, -14.0F, 1.6F, 2.0F, 4.0F, new Dilation(0.0F))
                                                .uv(0, 60).cuboid(-23.2F, -8.0F, -22.0F, 1.0F, 4.0F, 12.0F, new Dilation(0.0F))
                                                .uv(0, 60).cuboid(-23.2F, -10.0F, -19.5F, 1.0F, 2.0F, 9.5F, new Dilation(0.0F))
                                                .uv(0, 70).cuboid(-23.2F, -12.0F, -14.0F, 1.0F, 2.0F, 4.0F, new Dilation(0.0F)),
                                ModelTransform.pivot(0.0F, 24.0F, 0.0F));

                bbMain.addChild("traycenter_r1",
                                ModelPartBuilder.create().uv(129, 54).cuboid(-4.1F, -2.0F, -2.0F, 15.0F, 2.0F, 16.0F,
                                                new Dilation(0.0F))
                                                .uv(129, 54).cuboid(26.9F, -2.0F, -2.0F, 15.0F, 2.0F, 16.0F, new Dilation(0.0F))
                                                .uv(129, 54).cuboid(11.9F, -2.0F, -2.0F, 15.0F, 2.0F, 16.0F, new Dilation(0.0F)),
                                ModelTransform.of(-19.0F, -3.8F, -20.0F, 0.3491F, 0.0F, 0.0F));

                bbMain.addChild("cube_r5", ModelPartBuilder.create().uv(0, 78).cuboid(-1.9F, -2.0F, -2.0F, 1.8F, 2.0F, 16.0F,
                                new Dilation(0.0F))
                                .uv(29, 77).cuboid(13.1F, -2.0F, -2.0F, 1.8F, 2.0F, 16.0F, new Dilation(0.0F))
                                .uv(29, 77).cuboid(29.1F, -2.0F, -2.0F, 1.8F, 2.0F, 16.0F, new Dilation(0.0F))
                                .uv(28, 77).cuboid(44.1F, -2.0F, -2.0F, 1.8F, 2.0F, 16.0F, new Dilation(0.0F)),
                                ModelTransform.of(-22.0F, -7.8F, -20.0F, 0.3491F, 0.0F, 0.0F));

                bbMain.addChild("cube_r6", ModelPartBuilder.create().uv(35, 77).cuboid(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 12.0F,
                                new Dilation(0.0F)), ModelTransform.of(-23.0F, -8.0F, -8.0F, -1.5708F, 0.0F, 0.0F));

                bbMain.addChild("cube_r7", ModelPartBuilder.create().uv(35, 78).cuboid(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 12.0F,
                                new Dilation(0.0F)), ModelTransform.of(-23.0F, -8.0F, 6.0F, -1.5708F, 0.0F, 0.0F));

                return TexturedModelData.of(modelData, 256, 256);
        }

        @Override
        public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw,
                        float headPitch) {
                // No animation for this static model
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green,
                        float blue, float alpha) {
                bone.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                bbMain.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }
}
