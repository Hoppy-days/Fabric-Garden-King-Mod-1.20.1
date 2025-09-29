package net.jeremy.gardenkingmod.client.render;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.client.model.ScarecrowModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public final class ScarecrowRenderHelper {
    public static final float DEFAULT_RENDER_SCALE = 0.9F;
    private static final Identifier BASE_TEXTURE = new Identifier(
            GardenKingMod.MOD_ID,
            "textures/entity/scarecrow/scarecrow.png"
    );

    private final ScarecrowModel baseModel;
    private ItemStack hatStack;
    private ItemStack headStack;
    private ItemStack chestStack;
    private ItemStack pantsStack;
    private ItemStack pitchforkStack;

    public ScarecrowRenderHelper(ModelPart baseModelPart) {
        this.baseModel = new ScarecrowModel(baseModelPart);
        this.hatStack = ItemStack.EMPTY;
        this.headStack = ItemStack.EMPTY;
        this.chestStack = ItemStack.EMPTY;
        this.pantsStack = ItemStack.EMPTY;
        this.pitchforkStack = ItemStack.EMPTY;
    }

    public void setHatStack(ItemStack stack) {
        this.hatStack = stack;
    }

    public void setHeadStack(ItemStack stack) {
        this.headStack = stack;
    }

    public void setChestStack(ItemStack stack) {
        this.chestStack = stack;
    }

    public void setPantsStack(ItemStack stack) {
        this.pantsStack = stack;
    }

    public void setPitchforkStack(ItemStack stack) {
        this.pitchforkStack = stack;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.baseModel.setBaseVisible(true);
        this.baseModel.setHatVisible(false);
        this.baseModel.setHeadVisible(false);
        this.baseModel.setChestVisible(false);
        this.baseModel.setPantsVisible(false);
        this.baseModel.setPitchforkVisible(false);

        VertexConsumer baseConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(BASE_TEXTURE));
        this.baseModel.render(matrices, baseConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

        ItemStack hat = this.hatStack;
        ItemStack head = this.headStack;
        ItemStack chest = this.chestStack;
        ItemStack pants = this.pantsStack;
        ItemStack pitchfork = this.pitchforkStack;

        this.hatStack = ItemStack.EMPTY;
        this.headStack = ItemStack.EMPTY;
        this.chestStack = ItemStack.EMPTY;
        this.pantsStack = ItemStack.EMPTY;
        this.pitchforkStack = ItemStack.EMPTY;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        if (!hat.isEmpty()) {
            renderHat(client, hat, matrices, vertexConsumers, light, overlay);
        }
        if (!head.isEmpty()) {
            renderHead(client, head, matrices, vertexConsumers, light, overlay);
        }
        if (!chest.isEmpty()) {
            renderChest(client, chest, matrices, vertexConsumers, light, overlay);
        }
        if (!pants.isEmpty()) {
            renderPants(client, pants, matrices, vertexConsumers, light, overlay);
        }
        if (!pitchfork.isEmpty()) {
            renderPitchfork(client, pitchfork, matrices, vertexConsumers, light, overlay);
        }
    }

    private void renderHat(MinecraftClient client, ItemStack stack, MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.0F, -2.05F, 0.0F);
        matrices.scale(0.85F, 0.85F, 0.85F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        renderStack(client, stack, matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }

    private void renderHead(MinecraftClient client, ItemStack stack, MatrixStack matrices,
                            VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.0F, -1.75F, 0.0F);
        matrices.scale(0.9F, 0.9F, 0.9F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        renderStack(client, stack, matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }

    private void renderChest(MinecraftClient client, ItemStack stack, MatrixStack matrices,
                             VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.0F, -1.05F, -0.2F);
        matrices.scale(0.9F, 0.9F, 0.9F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10.0F));
        renderStack(client, stack, matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }

    private void renderPants(MinecraftClient client, ItemStack stack, MatrixStack matrices,
                             VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.0F, -0.45F, -0.18F);
        matrices.scale(0.9F, 0.9F, 0.9F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(5.0F));
        renderStack(client, stack, matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }

    private void renderPitchfork(MinecraftClient client, ItemStack stack, MatrixStack matrices,
                                 VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(-0.75F, -0.6F, 0.0F);
        matrices.scale(0.85F, 0.85F, 0.85F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-15.0F));
        renderStack(client, stack, matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }

    private void renderStack(MinecraftClient client, ItemStack stack, MatrixStack matrices,
                             VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BakedModel model = client.getItemRenderer().getModel(stack, null, null, 0);
        client.getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, false,
                matrices, vertexConsumers, light, overlay, model);
    }

    public static ScarecrowRenderHelper createDefault(BlockEntityRendererFactory.Context context) {
        ModelPart base = context.getLayerModelPart(ScarecrowModel.LAYER_LOCATION);
        return new ScarecrowRenderHelper(base);
    }

    public static ScarecrowRenderHelper createDefault(MinecraftClient client) {
        ModelPart base = client.getEntityModelLoader().getModelPart(ScarecrowModel.LAYER_LOCATION);
        return new ScarecrowRenderHelper(base);
    }
}
