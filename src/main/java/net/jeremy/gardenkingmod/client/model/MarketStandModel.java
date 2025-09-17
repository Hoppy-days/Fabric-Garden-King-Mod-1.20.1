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
