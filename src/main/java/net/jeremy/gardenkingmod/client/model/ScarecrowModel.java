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

// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class ScarecrowModel extends EntityModel<Entity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            new Identifier(GardenKingMod.MOD_ID, "scarecrow"), "main");

    private final ModelPart base;
    private final ModelPart hat;
    private final ModelPart head;
    private final ModelPart chest;
    private final ModelPart pitchfork;

    public ScarecrowModel(ModelPart root) {
        this.base = root.getChild("base");
        this.hat = root.getChild("hat");
        this.head = root.getChild("head");
        this.chest = root.getChild("chest");
        this.pitchfork = root.getChild("pitchfork");

        this.base.visible = true;
        this.hat.visible = false;
        this.head.visible = false;
        this.chest.visible = false;
        this.pitchfork.visible = false;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild("base", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 22).cuboid(-1.0F, -38.0F, -1.0F, 2.0F, 36.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 18).cuboid(-19.0F, -28.0F, -1.0F, 37.0F, 2.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        modelPartData.addChild("hat", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        modelPartData.addChild("head", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        modelPartData.addChild("chest", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        modelPartData.addChild("pitchfork", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        base.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        if (hat.visible) {
            hat.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
        if (head.visible) {
            head.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
        if (chest.visible) {
            chest.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
        if (pitchfork.visible) {
            pitchfork.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
    }

    public void setBaseVisible(boolean visible) {
        this.base.visible = visible;
    }

    public boolean isBaseVisible() {
        return this.base.visible;
    }

    public void setHatVisible(boolean visible) {
        this.hat.visible = visible;
    }

    public boolean isHatVisible() {
        return this.hat.visible;
    }

    public void setHeadVisible(boolean visible) {
        this.head.visible = visible;
    }

    public boolean isHeadVisible() {
        return this.head.visible;
    }

    public void setChestVisible(boolean visible) {
        this.chest.visible = visible;
    }

    public boolean isChestVisible() {
        return this.chest.visible;
    }

    public void setPitchforkVisible(boolean visible) {
        this.pitchfork.visible = visible;
    }

    public boolean isPitchforkVisible() {
        return this.pitchfork.visible;
    }
}
