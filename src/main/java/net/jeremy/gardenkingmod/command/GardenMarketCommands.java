package net.jeremy.gardenkingmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.jeremy.gardenkingmod.shop.GardenMarketOfferState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class GardenMarketCommands {
    private GardenMarketCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(GardenMarketCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("gkmarket")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("refresh")
                        .executes(GardenMarketCommands::refreshMarket)));
    }

    private static int refreshMarket(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        int refreshed = 0;
        long nextRefreshTime = 0L;
        for (ServerWorld world : source.getServer().getWorlds()) {
            GardenMarketOfferState state = GardenMarketOfferState.get(world);
            nextRefreshTime = Math.max(nextRefreshTime, state.forceRefresh(world));
            refreshed++;
        }
        source.sendFeedback(() -> Text.literal("Refreshed garden market offers and reset the timer in "
                + refreshed + " world(s). Next refresh tick: " + nextRefreshTime + "."), false);
        return refreshed;
    }
}
