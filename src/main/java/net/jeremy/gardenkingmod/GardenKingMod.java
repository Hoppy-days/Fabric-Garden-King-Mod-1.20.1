package net.jeremy.gardenkingmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.jeremy.gardenkingmod.armor.ModArmorSetEffects;
import net.jeremy.gardenkingmod.crop.BonusHarvestDropManager;
import net.jeremy.gardenkingmod.crop.CropDropModifier;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.registry.ModEntities;
import net.jeremy.gardenkingmod.registry.ModSoundEvents;
import net.jeremy.gardenkingmod.shop.GearShopOfferManager;

import net.minecraft.resource.ResourceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GardenKingMod implements ModInitializer {
        public static final String MOD_ID = "gardenkingmod";
        public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

        @Override
        public void onInitialize() {
                ModItems.registerModItems();
                ModBlocks.registerModBlocks();
                ModBlockEntities.registerBlockEntities();
                ModEntities.register();
                ModSoundEvents.register();
                ModScreenHandlers.registerScreenHandlers();
                ModScoreboards.registerScoreboards();
                ModArmorSetEffects.register();

                ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(BonusHarvestDropManager.getInstance());
                ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(GearShopOfferManager.getInstance());

                CropTierRegistry.init();
                CropDropModifier.register();

                LOGGER.info("Garden King Mod initialized");
        }
}
