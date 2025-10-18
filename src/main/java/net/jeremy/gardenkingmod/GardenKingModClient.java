package net.jeremy.gardenkingmod;

import java.util.LinkedHashMap;
import java.util.Map;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jeremy.gardenkingmod.client.gui.SkillScreen;
import net.jeremy.gardenkingmod.client.hud.SkillHudOverlay;
import net.jeremy.gardenkingmod.client.model.BankBlockModel;
import net.jeremy.gardenkingmod.client.model.CrowEntityModel;
import net.jeremy.gardenkingmod.client.model.GearShopModel;
import net.jeremy.gardenkingmod.client.model.MarketBlockModel;
import net.jeremy.gardenkingmod.client.model.ScarecrowModel;
import net.jeremy.gardenkingmod.client.model.SprinklerModel;
import net.jeremy.gardenkingmod.client.render.BankBlockEntityRenderer;
import net.jeremy.gardenkingmod.client.render.CrowEntityRenderer;
import net.jeremy.gardenkingmod.client.render.GearShopBlockEntityRenderer;
import net.jeremy.gardenkingmod.client.render.MarketBlockEntityRenderer;
import net.jeremy.gardenkingmod.client.render.ScarecrowBlockEntityRenderer;
import net.jeremy.gardenkingmod.client.render.SprinklerBlockEntityRenderer;
import net.jeremy.gardenkingmod.client.skill.SkillState;
import net.jeremy.gardenkingmod.client.render.item.BankItemRenderer;
import net.jeremy.gardenkingmod.client.render.item.GearShopItemRenderer;
import net.jeremy.gardenkingmod.client.render.item.MarketItemRenderer;
import net.jeremy.gardenkingmod.client.render.item.ScarecrowItemRenderer;
import net.jeremy.gardenkingmod.client.render.item.SprinklerItemRenderer;
import net.jeremy.gardenkingmod.registry.ModEntities;
import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.ModBlocks;
import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.item.FortuneProvidingItem;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.jeremy.gardenkingmod.screen.BankScreen;
import net.jeremy.gardenkingmod.screen.BankScreenHandler;
import net.jeremy.gardenkingmod.screen.GearShopScreen;
import net.jeremy.gardenkingmod.screen.MarketScreen;
import net.jeremy.gardenkingmod.screen.ScarecrowScreen;
import net.jeremy.gardenkingmod.skill.SkillProgressHolder;
import net.jeremy.gardenkingmod.skill.SkillProgressManager;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.util.InputUtil;

import org.lwjgl.glfw.GLFW;

public class GardenKingModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBinding openSkillScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gardenkingmod.open_skills",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.gardenkingmod"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSkillScreenKey.wasPressed()) {
                if (client != null && client.player != null && client.currentScreen == null) {
                    client.setScreen(new SkillScreen());
                }
            }
        });

        HandledScreens.register(ModScreenHandlers.GEAR_SHOP_SCREEN_HANDLER, GearShopScreen::new);
        HandledScreens.register(ModScreenHandlers.MARKET_SCREEN_HANDLER, MarketScreen::new);
        HandledScreens.register(ModScreenHandlers.SCARECROW_SCREEN_HANDLER, ScarecrowScreen::new);
        HandledScreens.register(ModScreenHandlers.BANK_SCREEN_HANDLER, BankScreen::new);
        EntityModelLayerRegistry.registerModelLayer(BankBlockModel.LAYER_LOCATION, BankBlockModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(MarketBlockModel.LAYER_LOCATION, MarketBlockModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(GearShopModel.LAYER_LOCATION,
                        GearShopModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ScarecrowModel.LAYER_LOCATION, ScarecrowModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(SprinklerModel.LAYER_LOCATION, SprinklerModel::getTexturedModelData);

        EntityModelLayerRegistry.registerModelLayer(CrowEntityModel.LAYER_LOCATION, CrowEntityModel::getTexturedModelData);
        BlockEntityRendererFactories.register(ModBlockEntities.BANK_BLOCK_ENTITY, BankBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.MARKET_BLOCK_ENTITY, MarketBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.GEAR_SHOP_BLOCK_ENTITY,
                        GearShopBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.SCARECROW_BLOCK_ENTITY, ScarecrowBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.SPRINKLER_BLOCK_ENTITY, SprinklerBlockEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CROW, CrowEntityRenderer::new);
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.BANK_BLOCK, new BankItemRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.MARKET_BLOCK, new MarketItemRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.GEAR_SHOP_BLOCK, new GearShopItemRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.SCARECROW_BLOCK, new ScarecrowItemRenderer());
        SprinklerItemRenderer sprinklerItemRenderer = new SprinklerItemRenderer();
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.IRON_SPRINKLER_BLOCK, sprinklerItemRenderer);
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.GOLD_SPRINKLER_BLOCK, sprinklerItemRenderer);
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.DIAMOND_SPRINKLER_BLOCK, sprinklerItemRenderer);
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.EMERALD_SPRINKLER_BLOCK, sprinklerItemRenderer);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SCARECROW_BLOCK, RenderLayer.getCutout());
        HudRenderCallback.EVENT.register(SkillHudOverlay.INSTANCE);

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.MARKET_SALE_RESULT_PACKET,
                (client, handler, buf, responseSender) -> {
                        buf.readBlockPos();
                        boolean success = buf.readBoolean();
                        int itemsSold = buf.readVarInt();
                        int dollarsEarned = buf.readVarInt();
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
                                        marketScreen.updateSaleResult(success, itemsSold, dollarsEarned, lifetimeTotal, feedback,
                                                        soldItemCounts);
                                }
                        });
                });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.BANK_BALANCE_PACKET,
                (client, handler, buf, responseSender) -> {
                        BlockPos bankPos = buf.readBlockPos();
                        int totalDollars = buf.readVarInt();

                        client.execute(() -> {
                                if (client.player != null
                                                && client.player.currentScreenHandler instanceof BankScreenHandler bankHandler
                                                && bankHandler.getBankPos().equals(bankPos)) {
                                        bankHandler.updateBalance(totalDollars);
                                }
                        });
                });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SKILL_PROGRESS_SYNC,
                (client, handler, buf, responseSender) -> {
                        long experience = buf.readVarLong();
                        int level = buf.readVarInt();
                        long progressIntoLevel = buf.readVarLong();
                        long experienceToNextLevel = buf.readVarLong();
                        int unspentPoints = buf.readVarInt();
                        int allocationEntries = buf.readVarInt();
                        Map<Identifier, Integer> allocations = new LinkedHashMap<>();
                        for (int index = 0; index < allocationEntries; index++) {
                                Identifier skillId = buf.readIdentifier();
                                int allocation = buf.readVarInt();
                                allocations.put(skillId, allocation);
                        }

                        client.execute(() -> {
                                SkillState skillState = SkillState.getInstance();
                                int previousLevel = skillState.getLevel();
                                skillState.update(experience, level, progressIntoLevel, experienceToNextLevel,
                                                unspentPoints, allocations);

                                if (client.player != null && level > previousLevel) {
                                        client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS,
                                                        1.0f, 1.0f);
                                }

                                if (client.player instanceof SkillProgressHolder skillHolder) {
                                        skillHolder.gardenkingmod$setSkillExperience(experience);
                                        skillHolder.gardenkingmod$setSkillLevel(level);
                                        skillHolder.gardenkingmod$setUnspentSkillPoints(unspentPoints);
                                        int chefMastery = allocations.getOrDefault(SkillProgressManager.CHEF_SKILL, 0);
                                        skillHolder.gardenkingmod$setChefMasteryLevel(chefMastery);
                                }
                        });
                });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SkillState.getInstance().reset());

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
