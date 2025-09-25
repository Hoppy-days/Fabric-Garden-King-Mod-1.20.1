package net.jeremy.gardenkingmod.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.client.model.CrowEntityModel;
import net.jeremy.gardenkingmod.entity.crow.CrowEntity;

/**
 * Binds the crow model and texture to the renderer registry.
 */
public class CrowEntityRenderer extends MobEntityRenderer<CrowEntity, CrowEntityModel> {
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(
            new Identifier(GardenKingMod.MOD_ID, "crow"), "main");
    private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID, "textures/entity/crow.png");

    public CrowEntityRenderer(Context context) {
        super(context, new CrowEntityModel(context.getPart(MODEL_LAYER)), 0.3f);
    }

    @Override
    public Identifier getTexture(CrowEntity entity) {
        return TEXTURE;
    }
}
