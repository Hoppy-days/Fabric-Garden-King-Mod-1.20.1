package net.jeremy.gardenkingmod.command;

import java.util.Optional;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.jeremy.gardenkingmod.crop.CropTier;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public final class CropDebugCommands {
        private CropDebugCommands() {
        }

        public static void register() {
                CommandRegistrationCallback.EVENT.register(CropDebugCommands::registerCommands);
        }

        private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                        CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
                dispatcher.register(CommandManager.literal("gkcrop")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.literal("inspect")
                                                .then(CommandManager.literal("block")
                                                                .then(CommandManager.argument("pos",
                                                                                BlockPosArgumentType.blockPos())
                                                                                .executes(context -> inspectBlock(context,
                                                                                                BlockPosArgumentType
                                                                                                                .getLoadedBlockPos(
                                                                                                                                context,
                                                                                                                                "pos")))))
                                                .then(CommandManager.literal("looking")
                                                                .executes(CropDebugCommands::inspectLookTarget))
                                                .then(CommandManager.literal("hand")
                                                                .executes(CropDebugCommands::inspectHeldItem))));
        }

        private static int inspectLookTarget(CommandContext<ServerCommandSource> context) {
                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                HitResult hit = player.raycast(8.0D, 0.0F, false);
                if (!(hit instanceof BlockHitResult blockHit)) {
                        context.getSource().sendError(Text.literal("No block target in range. Use /gkcrop inspect block <pos>."));
                        return 0;
                }

                return inspectBlock(context, blockHit.getBlockPos());
        }

        private static int inspectBlock(CommandContext<ServerCommandSource> context, BlockPos pos) {
                ServerCommandSource source = context.getSource();
                BlockState state = source.getWorld().getBlockState(pos);
                Block block = state.getBlock();
                Identifier blockId = Registries.BLOCK.getId(block);

                Optional<CropTier> tier = CropTierRegistry.get(state);
                String tierText = tier.map(value -> value.id().toString()).orElse("none");

                source.sendFeedback(() -> Text.literal("Crop debug @ " + pos.toShortString() + ":"), false);
                source.sendFeedback(() -> Text.literal("- block id: " + blockId), false);
                source.sendFeedback(() -> Text.literal("- loot table: " + block.getLootTableId()), false);
                source.sendFeedback(() -> Text.literal("- resolved tier: " + tierText), false);

                RegistryEntry<Block> entry = Registries.BLOCK.getEntry(block);
                for (TagKey<Block> tag : CropTierRegistry.getTierBlockTags()) {
                        boolean matches = entry.isIn(tag);
                        Optional<CropTier> tagTier = CropTierRegistry.getTierForTag(tag);
                        String tagTierId = tagTier.map(value -> value.id().toString()).orElse("unknown");
                        source.sendFeedback(
                                        () -> Text.literal("  * in " + tag.id() + " = " + matches + " (tier " + tagTierId + ")"),
                                        false);
                }

                return 1;
        }

        private static int inspectHeldItem(CommandContext<ServerCommandSource> context) {
                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                ItemStack stack = player.getMainHandStack();
                if (stack.isEmpty()) {
                        context.getSource().sendError(Text.literal("Your main hand is empty."));
                        return 0;
                }

                Item item = stack.getItem();
                Identifier itemId = Registries.ITEM.getId(item);
                Optional<CropTier> itemTier = CropTierRegistry.get(item);
                String tierText = itemTier.map(value -> value.id().toString()).orElse("none");

                context.getSource().sendFeedback(() -> Text.literal("Crop debug for held item:"), false);
                context.getSource().sendFeedback(() -> Text.literal("- item id: " + itemId), false);
                context.getSource().sendFeedback(() -> Text.literal("- resolved item tier: " + tierText), false);

                Block associatedBlock = null;
                if (item instanceof BlockItem blockItem) {
                        associatedBlock = blockItem.getBlock();
                } else if (item instanceof AliasedBlockItem aliasedBlockItem) {
                        associatedBlock = aliasedBlockItem.getBlock();
                }

                if (associatedBlock != null) {
                        Identifier blockId = Registries.BLOCK.getId(associatedBlock);
                        Optional<CropTier> blockTier = CropTierRegistry.get(associatedBlock);
                        context.getSource().sendFeedback(() -> Text.literal("- linked block id: " + blockId), false);
                        context.getSource().sendFeedback(
                                        () -> Text.literal("- linked block tier: " + blockTier.map(value -> value.id().toString()).orElse("none")),
                                        false);
                }

                return 1;
        }
}
