package net.jeremy.gardenkingmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.jeremy.gardenkingmod.client.model.MarketBlockModel;
import net.jeremy.gardenkingmod.client.render.MarketBlockEntityRenderer;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.jeremy.gardenkingmod.screen.MarketScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class GardenKingModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.MARKET_SCREEN_HANDLER, MarketScreen::new);
        EntityModelLayerRegistry.registerModelLayer(MarketBlockModel.LAYER_LOCATION, MarketBlockModel::getTexturedModelData);
        BlockEntityRendererFactories.register(ModBlockEntities.MARKET_BLOCK_ENTITY, MarketBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.MARKET_SALE_RESULT_PACKET,
                (client, handler, buf, responseSender) -> {
                        buf.readBlockPos();
                        int itemsSold = buf.readVarInt();
                        int payout = buf.readVarInt();
                        int lifetimeTotal = buf.readVarInt();

                        client.execute(() -> {
                                if (client.currentScreen instanceof MarketScreen marketScreen) {
                                        marketScreen.updateSaleResult(itemsSold, payout, lifetimeTotal);
                                }
                        });
                });
    }
}
