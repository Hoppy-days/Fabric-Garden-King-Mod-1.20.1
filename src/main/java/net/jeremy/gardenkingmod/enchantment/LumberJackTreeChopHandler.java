package net.jeremy.gardenkingmod.enchantment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.AxeItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class LumberJackTreeChopHandler {
        // Adjust this value to control max connected log blocks the enchantment can fell.
        // This count includes the first log mined by the player.
        public static final int MAX_CONNECTED_LOG_BLOCKS = 32;

        private static final ThreadLocal<Boolean> BREAKING_TREE = ThreadLocal.withInitial(() -> Boolean.FALSE);

        private LumberJackTreeChopHandler() {
        }

        public static void register() {
                PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
                        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                                return;
                        }

                        if (world.isClient() || BREAKING_TREE.get()) {
                                return;
                        }

                        if (!state.isIn(BlockTags.LOGS)) {
                                return;
                        }

                        if (!(serverPlayer.getMainHandStack().getItem() instanceof AxeItem)) {
                                return;
                        }

                        if (EnchantmentHelper.getLevel(ModEnchantments.LUMBER_JACK, serverPlayer.getMainHandStack()) <= 0) {
                                return;
                        }

                        if (world.getBlockState(pos.down()).isIn(BlockTags.LOGS)) {
                                return;
                        }

                        List<BlockPos> connectedLogs = findConnectedLogs(world, pos, MAX_CONNECTED_LOG_BLOCKS - 1);
                        if (connectedLogs.isEmpty()) {
                                return;
                        }

                        BREAKING_TREE.set(Boolean.TRUE);
                        try {
                                for (BlockPos logPos : connectedLogs) {
                                        if (!isHoldingEnchantedAxe(serverPlayer)) {
                                                break;
                                        }

                                        if (world.getBlockState(logPos).isIn(BlockTags.LOGS)) {
                                                serverPlayer.interactionManager.tryBreakBlock(logPos);
                                        }
                                }
                        } finally {
                                BREAKING_TREE.set(Boolean.FALSE);
                        }
                });
        }

        private static boolean isHoldingEnchantedAxe(ServerPlayerEntity player) {
                if (!(player.getMainHandStack().getItem() instanceof AxeItem)) {
                        return false;
                }

                return EnchantmentHelper.getLevel(ModEnchantments.LUMBER_JACK, player.getMainHandStack()) > 0;
        }

        private static List<BlockPos> findConnectedLogs(World world, BlockPos origin, int maxAdditionalLogs) {
                List<BlockPos> collected = new ArrayList<>();
                if (maxAdditionalLogs <= 0) {
                        return collected;
                }

                Set<BlockPos> visited = new HashSet<>();
                ArrayDeque<BlockPos> queue = new ArrayDeque<>();
                visited.add(origin);
                queue.add(origin);

                while (!queue.isEmpty() && collected.size() < maxAdditionalLogs) {
                        BlockPos current = queue.poll();
                        for (BlockPos neighbor : BlockPos.iterate(current.add(-1, -1, -1), current.add(1, 1, 1))) {
                                BlockPos immutableNeighbor = neighbor.toImmutable();
                                if (immutableNeighbor.equals(current) || visited.contains(immutableNeighbor)) {
                                        continue;
                                }

                                visited.add(immutableNeighbor);

                                BlockState neighborState = world.getBlockState(immutableNeighbor);
                                if (!neighborState.isIn(BlockTags.LOGS)) {
                                        continue;
                                }

                                collected.add(immutableNeighbor);
                                queue.add(immutableNeighbor);

                                if (collected.size() >= maxAdditionalLogs) {
                                        break;
                                }
                        }
                }

                return collected;
        }
}
