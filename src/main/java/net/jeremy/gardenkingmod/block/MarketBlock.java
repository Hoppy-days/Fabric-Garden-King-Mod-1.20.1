package net.jeremy.gardenkingmod.block;

import net.jeremy.gardenkingmod.block.entity.MarketBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemScatterer;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MarketBlock extends BlockWithEntity {
        public MarketBlock(Settings settings) {
                super(settings);
        }

        @Override
        public BlockRenderType getRenderType(BlockState state) {
                return BlockRenderType.MODEL;
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
                if (!world.isClient) {
                        BlockEntity blockEntity = world.getBlockEntity(pos);
                        if (blockEntity instanceof MarketBlockEntity marketBlockEntity) {
                                player.openHandledScreen(marketBlockEntity);
                        }
                }

                return ActionResult.SUCCESS;
        }

        @Override
        public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
                if (state.getBlock() != newState.getBlock()) {
                        BlockEntity blockEntity = world.getBlockEntity(pos);
                        if (blockEntity instanceof Inventory inventory) {
                                ItemScatterer.spawn(world, pos, inventory);
                                world.updateComparators(pos, this);
                        }

                        super.onStateReplaced(state, world, pos, newState, moved);
                }
        }

        @Override
        public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
                return new MarketBlockEntity(pos, state);
        }

        @Override
        public boolean hasComparatorOutput(BlockState state) {
                return true;
        }

        @Override
        public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
                return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
        }
}
