package net.jeremy.gardenkingmod.client.render;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.client.model.ScarecrowModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class ScarecrowRenderHelper {
    private static final Identifier BASE_TEXTURE = new Identifier(
            GardenKingMod.MOD_ID,
            "textures/entity/scarecrow/scarecrow.png"
    );

    private final ScarecrowModel baseModel;
    private boolean hatVisible;
    private boolean headVisible;
    private boolean chestVisible;
    private boolean pantsVisible;
    private boolean pitchforkVisible;

    public ScarecrowRenderHelper(ModelPart baseModelPart) {
        this.baseModel = new ScarecrowModel(baseModelPart);
        this.hatVisible = false;
        this.headVisible = false;
        this.chestVisible = false;
        this.pantsVisible = false;
        this.pitchforkVisible = false;
    }

    public void setHatVisible(boolean visible) {
        this.hatVisible = visible;
    }

    public void setHeadVisible(boolean visible) {
        this.headVisible = visible;
    }

    public void setChestVisible(boolean visible) {
        this.chestVisible = visible;
    }

    public void setPantsVisible(boolean visible) {
        this.pantsVisible = visible;
    }

    public void setPitchforkVisible(boolean visible) {
        this.pitchforkVisible = visible;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.baseModel.setBaseVisible(true);
        this.baseModel.setHatVisible(this.hatVisible);
        this.baseModel.setHeadVisible(this.headVisible);
        this.baseModel.setChestVisible(this.chestVisible);
        this.baseModel.setPantsVisible(this.pantsVisible);
        this.baseModel.setPitchforkVisible(this.pitchforkVisible);

        VertexConsumer baseConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(BASE_TEXTURE));
        this.baseModel.render(matrices, baseConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
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
