package net.jeremy.gardenkingmod.crop;

import org.jetbrains.annotations.Nullable;

import net.jeremy.gardenkingmod.crop.CropTier;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.skill.HarvestXpService;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public final class RightClickHarvestHandler {

        private RightClickHarvestHandler() {
        }

        public static ActionResult tryHarvest(CropBlock crop, BlockState state, World world, BlockPos pos, PlayerEntity player,
                        Hand hand) {
                if (!crop.isMature(state)) {
                        return ActionResult.PASS;
                }

                if (!player.canModifyBlocks()) {
                        return ActionResult.PASS;
                }

                ItemStack heldStack = player.getStackInHand(hand);
                ItemStack toolForDrops = ItemStack.EMPTY;

                if (heldStack.getItem() instanceof HoeItem hoeItem) {
                        toolForDrops = heldStack.copy();
                }
                        
                if (world.isClient) {
                        return ActionResult.SUCCESS;
                }

                ServerWorld serverWorld = (ServerWorld) world;
                BlockEntity blockEntity = world.getBlockEntity(pos);
                dropStacksWithXp(state, serverWorld, pos, blockEntity, player, toolForDrops);

                BlockState resetState = crop.withAge(0);
                world.setBlockState(pos, resetState, Block.NOTIFY_ALL);
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                world.playSound(null, pos, SoundEvents.BLOCK_CROP_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);

                if (heldStack.getItem() instanceof HoeItem && !player.isCreative()) {
                        heldStack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                }

                return ActionResult.SUCCESS;
        }

        private static void dropStacksWithXp(BlockState state, ServerWorld world, BlockPos pos,
                        @Nullable BlockEntity blockEntity, PlayerEntity player, ItemStack toolForDrops) {
                Identifier lootTableId = state.getBlock().getLootTableId();
                LootTable lootTable = world.getServer().getLootManager().getLootTable(lootTableId);

                LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                                .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                                .add(LootContextParameters.TOOL, toolForDrops)
                                .addOptional(LootContextParameters.THIS_ENTITY, player);

                if (blockEntity != null) {
                        builder.addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity);
                }

                LootContextParameterSet parameters = builder.build(LootContextTypes.BLOCK);
                Identifier blockId = Registries.BLOCK.getId(state.getBlock());
                Identifier tierId = CropTierRegistry.get(state).map(CropTier::id).orElse(null);

                lootTable.generateLoot(parameters, stack -> {
                        if (stack.isEmpty()) {
                                return;
                        }

                        HarvestXpService.awardHarvestXp(player, blockId, tierId, stack);
                        Block.dropStack(world, pos, stack);
                });

                state.onStacksDropped(world, pos, toolForDrops, true);
        }

}
