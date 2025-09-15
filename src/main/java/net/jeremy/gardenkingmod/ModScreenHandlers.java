package net.jeremy.gardenkingmod;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.jeremy.gardenkingmod.screen.MarketScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class ModScreenHandlers {
        public static final ScreenHandlerType<MarketScreenHandler> MARKET_SCREEN_HANDLER = Registry.register(
                        Registries.SCREEN_HANDLER, new Identifier(GardenKingMod.MOD_ID, "market"),
                        new ExtendedScreenHandlerType<>(MarketScreenHandler::new));

        private ModScreenHandlers() {
        }

        public static void registerScreenHandlers() {
                GardenKingMod.LOGGER.info("Registering screen handlers for {}", GardenKingMod.MOD_ID);
        }
}
