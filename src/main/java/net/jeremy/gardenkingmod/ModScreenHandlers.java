package net.jeremy.gardenkingmod;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.jeremy.gardenkingmod.screen.BankScreenHandler;
import net.jeremy.gardenkingmod.screen.GardenOvenScreenHandler;
import net.jeremy.gardenkingmod.screen.GearShopScreenHandler;
import net.jeremy.gardenkingmod.screen.MarketScreenHandler;
import net.jeremy.gardenkingmod.screen.ScarecrowScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class ModScreenHandlers {
        public static final ScreenHandlerType<GearShopScreenHandler> GEAR_SHOP_SCREEN_HANDLER = Registry.register(
                        Registries.SCREEN_HANDLER, new Identifier(GardenKingMod.MOD_ID, "gear_shop"),
                        new ExtendedScreenHandlerType<>(GearShopScreenHandler::new));

        public static final ScreenHandlerType<MarketScreenHandler> MARKET_SCREEN_HANDLER = Registry.register(
                        Registries.SCREEN_HANDLER, new Identifier(GardenKingMod.MOD_ID, "market"),
                        new ExtendedScreenHandlerType<>(MarketScreenHandler::new));

        public static final ScreenHandlerType<ScarecrowScreenHandler> SCARECROW_SCREEN_HANDLER = Registry.register(
                        Registries.SCREEN_HANDLER, new Identifier(GardenKingMod.MOD_ID, "scarecrow"),
                        new ExtendedScreenHandlerType<>(ScarecrowScreenHandler::new));

        public static final ScreenHandlerType<BankScreenHandler> BANK_SCREEN_HANDLER = Registry.register(
                        Registries.SCREEN_HANDLER, new Identifier(GardenKingMod.MOD_ID, "bank"),
                        new ExtendedScreenHandlerType<>(BankScreenHandler::new));

        public static final ScreenHandlerType<GardenOvenScreenHandler> GARDEN_OVEN_SCREEN_HANDLER = Registry.register(
                        Registries.SCREEN_HANDLER, new Identifier(GardenKingMod.MOD_ID, "garden_oven"),
                        new ScreenHandlerType<>(GardenOvenScreenHandler::new, FeatureSet.empty()));

        private ModScreenHandlers() {
        }

        public static void registerScreenHandlers() {
                GardenKingMod.LOGGER.info("Registering screen handlers for {}", GardenKingMod.MOD_ID);
        }
}
