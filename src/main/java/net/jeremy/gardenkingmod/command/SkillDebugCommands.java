package net.jeremy.gardenkingmod.command;

import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.jeremy.gardenkingmod.network.SkillProgressNetworking;
import net.jeremy.gardenkingmod.skill.SkillProgressHolder;
import net.jeremy.gardenkingmod.skill.SkillProgressManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class SkillDebugCommands {
        private SkillDebugCommands() {
        }

        public static void register() {
                CommandRegistrationCallback.EVENT.register(SkillDebugCommands::registerCommands);
        }

        private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                        CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
                dispatcher.register(CommandManager.literal("gkskill")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.literal("add_xp")
                                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                                .then(CommandManager.argument("amount",
                                                                                LongArgumentType.longArg(1L, Long.MAX_VALUE))
                                                                                .executes(context -> addExperience(context,
                                                                                                EntityArgumentType.getPlayers(
                                                                                                                context,
                                                                                                                "targets"),
                                                                                                LongArgumentType.getLong(context,
                                                                                                                "amount")))))
                                                .then(CommandManager.argument("amount", LongArgumentType.longArg(1L,
                                                                Long.MAX_VALUE))
                                                                .executes(context -> addExperience(context,
                                                                                context.getSource().getPlayerOrThrow(),
                                                                                LongArgumentType.getLong(context,
                                                                                                "amount")))))
                                .then(CommandManager.literal("remove_xp")
                                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                                .then(CommandManager.argument("amount",
                                                                                LongArgumentType.longArg(1L, Long.MAX_VALUE))
                                                                                .executes(context -> removeExperience(context,
                                                                                                EntityArgumentType.getPlayers(
                                                                                                                context,
                                                                                                                "targets"),
                                                                                                LongArgumentType.getLong(context,
                                                                                                                "amount")))))
                                                .then(CommandManager.argument("amount", LongArgumentType.longArg(1L,
                                                                Long.MAX_VALUE))
                                                                .executes(context -> removeExperience(context,
                                                                                context.getSource().getPlayerOrThrow(),
                                                                                LongArgumentType.getLong(context,
                                                                                                "amount")))))
                                .then(CommandManager.literal("add_levels")
                                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                                .then(CommandManager.argument("levels",
                                                                                IntegerArgumentType.integer(1))
                                                                                .executes(context -> addLevels(context,
                                                                                                EntityArgumentType.getPlayers(
                                                                                                                context,
                                                                                                                "targets"),
                                                                                                IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "levels")))))
                                                .then(CommandManager.argument("levels", IntegerArgumentType.integer(1))
                                                                .executes(context -> addLevels(context,
                                                                                context.getSource().getPlayerOrThrow(),
                                                                                IntegerArgumentType.getInteger(context,
                                                                                                "levels")))))
                                .then(CommandManager.literal("remove_levels")
                                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                                .then(CommandManager.argument("levels",
                                                                                IntegerArgumentType.integer(1))
                                                                                .executes(context -> removeLevels(context,
                                                                                                EntityArgumentType.getPlayers(
                                                                                                                context,
                                                                                                                "targets"),
                                                                                                IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "levels")))))
                                                .then(CommandManager.argument("levels", IntegerArgumentType.integer(1))
                                                                .executes(context -> removeLevels(context,
                                                                                context.getSource().getPlayerOrThrow(),
                                                                                IntegerArgumentType.getInteger(context,
                                                                                                "levels"))))));
        }

        private static int addExperience(CommandContext<ServerCommandSource> context,
                        ServerPlayerEntity target, long amount) {
                return addExperience(context, List.of(target), amount);
        }

        private static int addExperience(CommandContext<ServerCommandSource> context,
                        Collection<ServerPlayerEntity> targets, long amount) {
                int affected = 0;
                for (ServerPlayerEntity player : targets) {
                        if (!(player instanceof SkillProgressHolder skillHolder)) {
                                continue;
                        }

                        skillHolder.gardenkingmod$addSkillExperience(amount);
                        SkillProgressNetworking.sync(player);
                        affected++;
                        context.getSource().sendFeedback(
                                        () -> Text.literal("Awarded " + amount + " Garden King XP to "
                                                        + player.getName().getString() + "."),
                                        false);
                }
                return affected;
        }

        private static int removeExperience(CommandContext<ServerCommandSource> context,
                        ServerPlayerEntity target, long amount) {
                return removeExperience(context, List.of(target), amount);
        }

        private static int removeExperience(CommandContext<ServerCommandSource> context,
                        Collection<ServerPlayerEntity> targets, long amount) {
                int affected = 0;
                for (ServerPlayerEntity player : targets) {
                        if (!(player instanceof SkillProgressHolder skillHolder)) {
                                continue;
                        }

                        long currentExperience = Math.max(0L, skillHolder.gardenkingmod$getSkillExperience());
                        long updatedExperience = Math.max(0L, currentExperience - amount);
                        if (updatedExperience == currentExperience) {
                                continue;
                        }

                        skillHolder.gardenkingmod$setSkillExperience(updatedExperience);
                        int previousLevel = Math.max(0, skillHolder.gardenkingmod$getSkillLevel());
                        int newLevel = SkillProgressManager.getLevelForExperience(updatedExperience);
                        if (newLevel != previousLevel) {
                                skillHolder.gardenkingmod$setSkillLevel(newLevel);
                                if (skillHolder.gardenkingmod$getUnspentSkillPoints() > newLevel) {
                                        skillHolder.gardenkingmod$setUnspentSkillPoints(newLevel);
                                }
                        }

                        SkillProgressNetworking.sync(player);
                        affected++;
                        long removed = currentExperience - updatedExperience;
                        context.getSource().sendFeedback(() -> Text.literal("Removed " + removed
                                        + " Garden King XP from " + player.getName().getString() + "."), false);
                }
                return affected;
        }

        private static int addLevels(CommandContext<ServerCommandSource> context,
                        ServerPlayerEntity target, int levels) {
                return addLevels(context, List.of(target), levels);
        }

        private static int addLevels(CommandContext<ServerCommandSource> context,
                        Collection<ServerPlayerEntity> targets, int levels) {
                int affected = 0;
                for (ServerPlayerEntity player : targets) {
                        if (!(player instanceof SkillProgressHolder skillHolder)) {
                                continue;
                        }

                        int startingLevel = Math.max(0, skillHolder.gardenkingmod$getSkillLevel());
                        int maxLevel = SkillProgressManager.getMaxDefinedLevel();
                        int targetLevel = Math.min(maxLevel, startingLevel + levels);
                        if (targetLevel <= startingLevel) {
                                continue;
                        }

                        long currentExperience = Math.max(0L, skillHolder.gardenkingmod$getSkillExperience());
                        long targetExperience = Math.max(currentExperience,
                                        SkillProgressManager.getExperienceForLevel(targetLevel));
                        long experienceToAward = Math.max(0L, targetExperience - currentExperience);
                        if (experienceToAward <= 0L) {
                                continue;
                        }

                        skillHolder.gardenkingmod$addSkillExperience(experienceToAward);
                        SkillProgressNetworking.sync(player);
                        affected++;
                        context.getSource().sendFeedback(() -> Text.literal("Advanced "
                                        + player.getName().getString() + " by " + (targetLevel - startingLevel)
                                        + " level(s)."), false);
                }
                return affected;
        }

        private static int removeLevels(CommandContext<ServerCommandSource> context,
                        ServerPlayerEntity target, int levels) {
                return removeLevels(context, List.of(target), levels);
        }

        private static int removeLevels(CommandContext<ServerCommandSource> context,
                        Collection<ServerPlayerEntity> targets, int levels) {
                int affected = 0;
                for (ServerPlayerEntity player : targets) {
                        if (!(player instanceof SkillProgressHolder skillHolder)) {
                                continue;
                        }

                        int startingLevel = Math.max(0, skillHolder.gardenkingmod$getSkillLevel());
                        int targetLevel = Math.max(0, startingLevel - levels);
                        if (targetLevel >= startingLevel) {
                                continue;
                        }

                        long currentExperience = Math.max(0L, skillHolder.gardenkingmod$getSkillExperience());
                        long targetExperience = SkillProgressManager.getExperienceForLevel(targetLevel);
                        if (currentExperience < targetExperience) {
                                targetExperience = currentExperience;
                        }

                        skillHolder.gardenkingmod$setSkillLevel(targetLevel);
                        skillHolder.gardenkingmod$setSkillExperience(targetExperience);
                        if (skillHolder.gardenkingmod$getUnspentSkillPoints() > targetLevel) {
                                skillHolder.gardenkingmod$setUnspentSkillPoints(targetLevel);
                        }

                        SkillProgressNetworking.sync(player);
                        affected++;
                        context.getSource().sendFeedback(() -> Text.literal("Reduced "
                                        + player.getName().getString() + " to level " + targetLevel + "."), false);
                }
                return affected;
        }
}
