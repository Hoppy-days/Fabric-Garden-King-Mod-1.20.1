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
public class GardenShopModel extends EntityModel<Entity> {
        public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
                        new Identifier(GardenKingMod.MOD_ID, "garden_shop"),
                        "main");
    private final ModelPart fencegate1;
    private final ModelPart fencegate2;
    private final ModelPart fencegate3;
    private final ModelPart fencegate4;
    private final ModelPart fencegate5;
    private final ModelPart fencegate6;
    private final ModelPart fencegate7;
    private final ModelPart fencegate8;
    private final ModelPart fencegate9;
    private final ModelPart fencegate10;
    private final ModelPart bb_main;
    public GardenShopModel(ModelPart root) {
        this.fencegate1 = root.getChild("fencegate1");
        this.fencegate2 = root.getChild("fencegate2");
        this.fencegate3 = root.getChild("fencegate3");
        this.fencegate4 = root.getChild("fencegate4");
        this.fencegate5 = root.getChild("fencegate5");
        this.fencegate6 = root.getChild("fencegate6");
        this.fencegate7 = root.getChild("fencegate7");
        this.fencegate8 = root.getChild("fencegate8");
        this.fencegate9 = root.getChild("fencegate9");
        this.fencegate10 = root.getChild("fencegate10");
        this.bb_main = root.getChild("bb_main");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData fencegate1 = modelPartData.addChild("fencegate1", ModelPartBuilder.create().uv(132, 0).cuboid(-2.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(140, 0).cuboid(-16.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(184, 232).cuboid(-8.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(144, 246).cuboid(-10.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 66).cuboid(-6.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 71).cuboid(-6.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 76).cuboid(-14.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 81).cuboid(-14.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-32.0F, -38.0F, -14.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData fencegate2 = modelPartData.addChild("fencegate2", ModelPartBuilder.create().uv(152, 246).cuboid(-8.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(160, 246).cuboid(-10.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 86).cuboid(-6.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 91).cuboid(-6.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 96).cuboid(-14.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 101).cuboid(-14.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-32.0F, -38.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData fencegate3 = modelPartData.addChild("fencegate3", ModelPartBuilder.create().uv(148, 0).cuboid(-2.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(156, 0).cuboid(-16.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(240, 248).cuboid(-8.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(248, 248).cuboid(-10.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 106).cuboid(-6.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 111).cuboid(-6.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 116).cuboid(-14.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 121).cuboid(-14.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-32.0F, -38.0F, 14.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData Left_handpost_r1 = fencegate3.addChild("Left_handpost_r1", ModelPartBuilder.create().uv(48, 246).cuboid(9.3137F, -2.6863F, 0.0F, 2.0F, 17.0F, 2.0F, new Dilation(0.0F))
                .uv(232, 244).cuboid(9.3137F, -2.6863F, 64.0F, 2.0F, 17.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(6.0F, 7.0F, -65.0F, 0.0F, 0.0F, 0.7854F));

        ModelPartData fencegate4 = modelPartData.addChild("fencegate4", ModelPartBuilder.create().uv(164, 0).cuboid(-2.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(172, 0).cuboid(-16.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(168, 252).cuboid(-8.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(176, 252).cuboid(-10.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 126).cuboid(-6.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 131).cuboid(-6.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 136).cuboid(-14.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 141).cuboid(-14.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(32.0F, -38.0F, 14.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData fencegate5 = modelPartData.addChild("fencegate5", ModelPartBuilder.create().uv(64, 254).cuboid(-8.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(72, 254).cuboid(-10.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 146).cuboid(-6.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 151).cuboid(-6.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 156).cuboid(-14.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 161).cuboid(-14.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(32.0F, -38.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData fencegate6 = modelPartData.addChild("fencegate6", ModelPartBuilder.create().uv(180, 0).cuboid(-2.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(112, 218).cuboid(-16.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(80, 254).cuboid(-8.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(88, 254).cuboid(-10.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 166).cuboid(-6.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 171).cuboid(-6.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 176).cuboid(-14.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 181).cuboid(-14.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(32.0F, -38.0F, -14.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData fencegate7 = modelPartData.addChild("fencegate7", ModelPartBuilder.create().uv(120, 218).cuboid(-2.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(56, 246).cuboid(-16.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(96, 254).cuboid(-8.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(104, 254).cuboid(-10.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 186).cuboid(-6.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 191).cuboid(-6.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 196).cuboid(-14.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 201).cuboid(-14.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(30.0F, -38.0F, -32.0F));

        ModelPartData fencegate8 = modelPartData.addChild("fencegate8", ModelPartBuilder.create().uv(128, 246).cuboid(-2.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 22).cuboid(-8.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 33).cuboid(-10.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 226).cuboid(-6.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 231).cuboid(-6.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 236).cuboid(-14.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 241).cuboid(-14.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -38.0F, -32.0F));

        ModelPartData fencegate9 = modelPartData.addChild("fencegate9", ModelPartBuilder.create().uv(112, 246).cuboid(-2.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(120, 246).cuboid(-16.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 0).cuboid(-8.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 11).cuboid(-10.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 206).cuboid(-6.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 211).cuboid(-6.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 216).cuboid(-14.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 221).cuboid(-14.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-14.0F, -38.0F, -32.0F));

        ModelPartData fencegate10 = modelPartData.addChild("fencegate10", ModelPartBuilder.create().uv(136, 246).cuboid(-16.0F, 15.0F, -1.0F, 2.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 44).cuboid(-8.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 55).cuboid(-10.0F, 16.0F, -1.0F, 2.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 246).cuboid(-6.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 251).cuboid(-6.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(256, 256).cuboid(-14.0F, 22.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(144, 257).cuboid(-14.0F, 16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(16.0F, -38.0F, -32.0F));

        ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(64, 118).cuboid(-24.0F, -16.0F, 8.0F, 16.0F, 8.0F, 16.0F, new Dilation(0.0F))
                .uv(128, 13).cuboid(-8.0F, -16.0F, 8.0F, 16.0F, 8.0F, 16.0F, new Dilation(0.0F))
                .uv(128, 37).cuboid(8.0F, -16.0F, 8.0F, 16.0F, 8.0F, 16.0F, new Dilation(0.0F))
                .uv(64, 218).cuboid(-24.0F, -8.0F, 8.0F, 16.0F, 8.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 226).cuboid(-8.0F, -8.0F, 8.0F, 16.0F, 8.0F, 8.0F, new Dilation(0.0F))
                .uv(192, 228).cuboid(8.0F, -8.0F, 8.0F, 16.0F, 8.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 22).cuboid(-40.0F, -16.0F, 8.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 54).cuboid(24.0F, -16.0F, 8.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F))
                .uv(48, 226).cuboid(-34.0F, -32.0F, 14.0F, 4.0F, 16.0F, 4.0F, new Dilation(0.0F))
                .uv(80, 234).cuboid(30.0F, -32.0F, 14.0F, 4.0F, 16.0F, 4.0F, new Dilation(0.0F))
                .uv(64, 22).cuboid(-40.0F, -16.0F, -40.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F))
                .uv(64, 54).cuboid(-24.0F, -16.0F, -40.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 86).cuboid(-8.0F, -16.0F, -40.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F))
                .uv(64, 86).cuboid(8.0F, -16.0F, -40.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F))
                .uv(96, 234).cuboid(-34.0F, -32.0F, -34.0F, 4.0F, 16.0F, 4.0F, new Dilation(0.0F))
                .uv(0, 118).cuboid(24.0F, -16.0F, -40.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 242).cuboid(30.0F, -32.0F, -34.0F, 4.0F, 16.0F, 4.0F, new Dilation(0.0F))
                .uv(128, 61).cuboid(-40.0F, -51.0F, 8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(128, 80).cuboid(-24.0F, -51.0F, 8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(128, 99).cuboid(-8.0F, -51.0F, 8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(128, 118).cuboid(8.0F, -51.0F, 8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(128, 137).cuboid(24.0F, -51.0F, 8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(64, 142).cuboid(24.0F, -51.0F, -8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 150).cuboid(24.0F, -51.0F, -24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(128, 156).cuboid(24.0F, -51.0F, -40.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(64, 161).cuboid(8.0F, -51.0F, -40.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 169).cuboid(8.0F, -51.0F, -24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(128, 175).cuboid(8.0F, -51.0F, -8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(64, 180).cuboid(-8.0F, -51.0F, -8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 188).cuboid(-8.0F, -51.0F, -24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 0).cuboid(-8.0F, -51.0F, -40.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 19).cuboid(-24.0F, -51.0F, -40.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 38).cuboid(-24.0F, -51.0F, -24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 57).cuboid(-24.0F, -51.0F, -8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 76).cuboid(-40.0F, -51.0F, -8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 95).cuboid(-40.0F, -51.0F, -24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 114).cuboid(-40.0F, -51.0F, -40.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 133).cuboid(-40.0F, -16.0F, -8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 152).cuboid(-40.0F, -16.0F, -24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 171).cuboid(24.0F, -16.0F, -8.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 190).cuboid(24.0F, -16.0F, -24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(32, 242).cuboid(-34.0F, -48.0F, 14.0F, 4.0F, 16.0F, 4.0F, new Dilation(0.0F))
                .uv(184, 244).cuboid(-34.0F, -48.0F, -34.0F, 4.0F, 16.0F, 4.0F, new Dilation(0.0F))
                .uv(200, 244).cuboid(30.0F, -48.0F, -34.0F, 4.0F, 16.0F, 4.0F, new Dilation(0.0F))
                .uv(216, 244).cuboid(30.0F, -48.0F, 14.0F, 4.0F, 16.0F, 4.0F, new Dilation(0.0F))
                .uv(128, 194).cuboid(24.0F, -51.0F, 24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(64, 199).cuboid(8.0F, -51.0F, 24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 207).cuboid(-8.0F, -51.0F, 24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(192, 209).cuboid(-24.0F, -51.0F, 24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(128, 213).cuboid(-40.0F, -51.0F, 24.0F, 16.0F, 3.0F, 16.0F, new Dilation(0.0F))
                .uv(36, 262).cuboid(-40.0F, -48.0F, 40.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(260, 261).cuboid(-38.0F, -48.0F, 40.0F, 2.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(124, 13).cuboid(-36.0F, -48.0F, 40.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(188, 0).cuboid(-34.0F, -48.0F, 40.0F, 2.0F, 11.0F, 0.0F, new Dilation(0.0F))
                .uv(156, 257).cuboid(-32.0F, -48.0F, 40.0F, 2.0F, 11.0F, 0.0F, new Dilation(0.0F))
                .uv(128, 259).cuboid(-30.0F, -48.0F, 40.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(0, 262).cuboid(-28.0F, -48.0F, 40.0F, 2.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(40, 262).cuboid(-26.0F, -48.0F, 40.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(44, 262).cuboid(-10.0F, -48.0F, 40.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(144, 262).cuboid(-24.0F, -48.0F, 40.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(4, 262).cuboid(-22.0F, -48.0F, 40.0F, 2.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(132, 259).cuboid(-20.0F, -48.0F, 40.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(160, 257).cuboid(-18.0F, -48.0F, 40.0F, 2.0F, 11.0F, 0.0F, new Dilation(0.0F))
                .uv(164, 257).cuboid(-16.0F, -48.0F, 40.0F, 2.0F, 11.0F, 0.0F, new Dilation(0.0F))
                .uv(136, 259).cuboid(-14.0F, -48.0F, 40.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(8, 262).cuboid(-12.0F, -48.0F, 40.0F, 2.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(148, 262).cuboid(6.0F, -48.0F, 40.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(152, 262).cuboid(-8.0F, -48.0F, 40.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(12, 262).cuboid(-6.0F, -48.0F, 40.0F, 2.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(140, 259).cuboid(-4.0F, -48.0F, 40.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(56, 259).cuboid(-2.0F, -48.0F, 40.0F, 2.0F, 11.0F, 0.0F, new Dilation(0.0F))
                .uv(60, 259).cuboid(0.0F, -48.0F, 40.0F, 2.0F, 11.0F, 0.0F, new Dilation(0.0F))
                .uv(240, 259).cuboid(2.0F, -48.0F, 40.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(16, 262).cuboid(4.0F, -48.0F, 40.0F, 2.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(168, 263).cuboid(22.0F, -48.0F, 40.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(172, 263).cuboid(8.0F, -48.0F, 40.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(20, 262).cuboid(10.0F, -48.0F, 40.0F, 2.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(244, 259).cuboid(12.0F, -48.0F, 40.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(112, 259).cuboid(14.0F, -48.0F, 40.0F, 2.0F, 11.0F, 0.0F, new Dilation(0.0F))
                .uv(116, 259).cuboid(16.0F, -48.0F, 40.0F, 2.0F, 11.0F, 0.0F, new Dilation(0.0F))
                .uv(248, 259).cuboid(18.0F, -48.0F, 40.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(24, 262).cuboid(20.0F, -48.0F, 40.0F, 2.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(176, 263).cuboid(38.0F, -48.0F, 40.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(180, 263).cuboid(24.0F, -48.0F, 40.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(28, 262).cuboid(26.0F, -48.0F, 40.0F, 2.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(252, 259).cuboid(28.0F, -48.0F, 40.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(120, 259).cuboid(30.0F, -48.0F, 40.0F, 2.0F, 11.0F, 0.0F, new Dilation(0.0F))
                .uv(124, 259).cuboid(32.0F, -48.0F, 40.0F, 2.0F, 11.0F, 0.0F, new Dilation(0.0F))
                .uv(256, 261).cuboid(34.0F, -48.0F, 40.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(32, 262).cuboid(36.0F, -48.0F, 40.0F, 2.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-32.0F, -62.0F, 32.0F, 64.0F, 11.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 13).cuboid(-31.0F, -61.0F, 34.4F, 62.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData cube_r1 = bb_main.addChild("cube_r1", ModelPartBuilder.create().uv(140, 232).cuboid(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(112, 232).cuboid(-59.0F, -2.0F, -2.0F, 2.0F, 2.0F, 12.0F, new Dilation(0.0F)), ModelTransform.of(29.0F, -51.0F, 27.0F, 0.7854F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 512, 512);
    }
    @Override
    public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        fencegate1.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        fencegate2.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        fencegate3.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        fencegate4.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        fencegate5.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        fencegate6.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        fencegate7.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        fencegate8.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        fencegate9.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        fencegate10.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }
}
