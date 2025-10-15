package net.jeremy.gardenkingmod.client.render.item;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.jeremy.gardenkingmod.block.sprinkler.SprinklerBlock;
import net.jeremy.gardenkingmod.block.sprinkler.SprinklerTier;
import net.jeremy.gardenkingmod.client.model.SprinklerModel;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class SprinklerItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
        private SprinklerModel model;

        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light, int overlay) {
                if (this.model == null) {
                        this.model = new SprinklerModel(
                                        MinecraftClient.getInstance().getEntityModelLoader()
                                                        .getModelPart(SprinklerModel.LAYER_LOCATION));
                }

                matrices.push();
                matrices.translate(0.5D, 1.5D, 0.5D);
                matrices.scale(-1.0F, -1.0F, 1.0F);

                SprinklerTier tier = getTier(stack);
                VertexConsumer vertexConsumer = vertexConsumers
                                .getBuffer(RenderLayer.getEntityCutoutNoCull(tier.getTexture()));
                this.model.render(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
                matrices.pop();
        }

        private static SprinklerTier getTier(ItemStack stack) {
                Block block = Block.getBlockFromItem(stack.getItem());
                if (block instanceof SprinklerBlock sprinklerBlock) {
                        return sprinklerBlock.getTier();
                }
                if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SprinklerBlock sprinkler) {
                        return sprinkler.getTier();
                }
                return SprinklerTier.IRON;
        }
}
