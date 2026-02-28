package net.jeremy.gardenkingmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.jeremy.gardenkingmod.event.EndlessNightEventManager;

public final class EndlessNightCommands {
    private EndlessNightCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(EndlessNightCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("gkendlessnight")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("start")
                        .executes(EndlessNightCommands::start))
                .then(CommandManager.literal("stop")
                        .executes(EndlessNightCommands::stop))
                .then(CommandManager.literal("status")
                        .executes(EndlessNightCommands::status)));
    }

    private static int start(CommandContext<ServerCommandSource> context) {
        if (!EndlessNightEventManager.forceStart()) {
            context.getSource().sendError(Text.literal("Endless Night is already active."));
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal("Forced Endless Night event start."), true);
        return 1;
    }

    private static int stop(CommandContext<ServerCommandSource> context) {
        if (!EndlessNightEventManager.forceEnd()) {
            context.getSource().sendError(Text.literal("Endless Night is not active."));
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal("Forced Endless Night event end."), true);
        return 1;
    }

    private static int status(CommandContext<ServerCommandSource> context) {
        if (!EndlessNightEventManager.isActive()) {
            context.getSource().sendFeedback(() -> Text.literal("Endless Night status: inactive."), false);
            return 1;
        }

        context.getSource().sendFeedback(
                () -> Text.literal("Endless Night status: active - " + EndlessNightEventManager.getProgress() + "/"
                        + EndlessNightEventManager.getRequiredCount() + " ("
                        + EndlessNightEventManager.getTaskDescription().getString() + ")"),
                false);
        return 1;
    }
}
