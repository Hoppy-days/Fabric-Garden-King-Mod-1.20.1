package net.jeremy.gardenkingmod;

import java.util.LinkedHashMap;
import java.util.Map;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.jeremy.gardenkingmod.client.model.CrowEntityModel;
import net.jeremy.gardenkingmod.client.model.MarketBlockModel;
import net.jeremy.gardenkingmod.client.model.ScarecrowModel;
import net.jeremy.gardenkingmod.client.render.CrowEntityRenderer;
import net.jeremy.gardenkingmod.client.render.MarketBlockEntityRenderer;
import net.jeremy.gardenkingmod.client.render.ScarecrowBlockEntityRenderer;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.item.FortuneProvidingItem;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.jeremy.gardenkingmod.screen.MarketScreen;
import net.jeremy.gardenkingmod.screen.ScarecrowScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.jeremy.gardenkingmod.registry.ModEntities;

public class GardenKingModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.MARKET_SCREEN_HANDLER, MarketScreen::new);
        HandledScreens.register(ModScreenHandlers.SCARECROW_SCREEN_HANDLER, ScarecrowScreen::new);
        EntityModelLayerRegistry.registerModelLayer(MarketBlockModel.LAYER_LOCATION, MarketBlockModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ScarecrowModel.LAYER_LOCATION, ScarecrowModel::getTexturedModelData);

        EntityModelLayerRegistry.registerModelLayer(CrowEntityModel.LAYER_LOCATION, CrowEntityModel::getTexturedModelData);
        BlockEntityRendererFactories.register(ModBlockEntities.MARKET_BLOCK_ENTITY, MarketBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.SCARECROW_BLOCK_ENTITY, ScarecrowBlockEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CROW, CrowEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SCARECROW_BLOCK, RenderLayer.getCutout());

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.MARKET_SALE_RESULT_PACKET,
                (client, handler, buf, responseSender) -> {
                        buf.readBlockPos();
                        boolean success = buf.readBoolean();
                        int itemsSold = buf.readVarInt();
                        int payout = buf.readVarInt();
                        int lifetimeTotal = buf.readVarInt();
                        Text feedback = buf.readText();
                        int soldItemEntries = buf.readVarInt();
                        Map<Item, Integer> soldItemCounts = new LinkedHashMap<>();
                        for (int index = 0; index < soldItemEntries; index++) {
                                Identifier itemId = buf.readIdentifier();
                                int count = buf.readVarInt();
                                if (count <= 0) {
                                        continue;
                                }
                                Registries.ITEM.getOrEmpty(itemId)
                                                .ifPresent(item -> soldItemCounts.merge(item, count, Integer::sum));
                        }

                        client.execute(() -> {
                                if (client.currentScreen instanceof MarketScreen marketScreen) {
                                        marketScreen.updateSaleResult(success, itemsSold, payout, lifetimeTotal, feedback,
                                                        soldItemCounts);
                                }
                        });
                });

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
                if (stack.getItem() instanceof FortuneProvidingItem fortuneItem) {
                        int fortuneLevel = fortuneItem.gardenkingmod$getFortuneLevel(stack);
                        if (fortuneLevel > 0) {
                                lines.add(Text.translatable("tooltip." + GardenKingMod.MOD_ID + ".built_in_fortune",
                                                Text.translatable("enchantment.minecraft.fortune"),
                                                Text.translatable("enchantment.level." + fortuneLevel))
                                                .formatted(Formatting.GRAY));
                        }
                }

                CropTierRegistry.get(stack.getItem()).ifPresent(tier -> {
                        String path = tier.id().getPath();
                        String suffix = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
                        Text tierName = Text
                                        .translatable("tooltip." + tier.id().getNamespace() + ".crop_tier." + suffix);
                        lines.add(Text.translatable("tooltip." + GardenKingMod.MOD_ID + ".crop_tier", tierName)
                                        .formatted(Formatting.GREEN));
                });
        });
    }
}
