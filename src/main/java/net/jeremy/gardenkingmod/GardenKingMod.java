package net.jeremy.gardenkingmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.jeremy.gardenkingmod.armor.ModArmorSetEffects;
import net.jeremy.gardenkingmod.command.SkillDebugCommands;
import net.jeremy.gardenkingmod.crop.BonusHarvestDropManager;
import net.jeremy.gardenkingmod.crop.CropDropModifier;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.item.FertilizerBalanceConfig;
import net.jeremy.gardenkingmod.item.WalletItem;
import net.jeremy.gardenkingmod.network.ModServerNetworking;
import net.jeremy.gardenkingmod.registry.ModEntities;
import net.jeremy.gardenkingmod.registry.ModSoundEvents;
import net.jeremy.gardenkingmod.recipe.ModRecipes;
import net.jeremy.gardenkingmod.shop.GardenMarketOfferManager;
import net.jeremy.gardenkingmod.shop.GearShopOfferManager;
import net.jeremy.gardenkingmod.skill.HarvestXpConfig;
import net.jeremy.gardenkingmod.util.GardenOvenBalanceConfig;

import net.minecraft.resource.ResourceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GardenKingMod implements ModInitializer {
        public static final String MOD_ID = "gardenkingmod";
        public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

        @Override
        public void onInitialize() {
                FertilizerBalanceConfig.reload();
                GardenOvenBalanceConfig.reload();
                HarvestXpConfig.reload();
                ModItems.registerModItems();
                ModBlocks.registerModBlocks();
                ModBlockEntities.registerBlockEntities();
                ModRecipes.register();
                ModEntities.register();
                ModSoundEvents.register();
                ModScreenHandlers.registerScreenHandlers();
                ModScoreboards.registerScoreboards();
                ModArmorSetEffects.register();
                ModServerNetworking.register();
                SkillDebugCommands.register();

                ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(BonusHarvestDropManager.getInstance());
                ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(GearShopOfferManager.getInstance());
                ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(GardenMarketOfferManager.getInstance());

                CropTierRegistry.init();
                CropDropModifier.register();

                ServerTickEvents.END_SERVER_TICK.register(server -> {
                        for (var player : server.getPlayerManager().getPlayerList()) {
                                WalletItem.syncWalletBalances(player);
                        }
                });

                LOGGER.info("Garden King Mod initialized");
        }
}
