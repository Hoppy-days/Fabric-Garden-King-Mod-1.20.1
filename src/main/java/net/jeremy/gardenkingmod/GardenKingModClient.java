package net.jeremy.gardenkingmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.jeremy.gardenkingmod.client.model.MarketBlockModel;
import net.jeremy.gardenkingmod.client.render.MarketBlockEntityRenderer;
import net.jeremy.gardenkingmod.screen.MarketScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class GardenKingModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.MARKET_SCREEN_HANDLER, MarketScreen::new);
        EntityModelLayerRegistry.registerModelLayer(MarketBlockModel.LAYER_LOCATION, MarketBlockModel::getTexturedModelData);
        BlockEntityRendererFactories.register(ModBlockEntities.MARKET_BLOCK_ENTITY, MarketBlockEntityRenderer::new);
    }
}
