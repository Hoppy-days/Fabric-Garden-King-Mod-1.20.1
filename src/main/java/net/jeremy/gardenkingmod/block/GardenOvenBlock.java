package net.jeremy.gardenkingmod.block;

import net.jeremy.gardenkingmod.block.entity.GardenOvenBlockEntity;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class GardenOvenBlock extends AbstractFurnaceBlock {

        public GardenOvenBlock(Settings settings) {
                super(settings);
                this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH)
                                .with(LIT, Boolean.FALSE));
        }

        @Override
        public BlockState getPlacementState(ItemPlacementContext ctx) {
                Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
                if (ctx.getPlayer() == null) {
                        facing = ctx.getHorizontalFacing().getOpposite();
                }

                return this.getDefaultState().with(FACING, facing).with(LIT, Boolean.FALSE);
        }

	@Override
	protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
		if (world.isClient) {
			return;
		}

                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof GardenOvenBlockEntity oven) {
                        player.openHandledScreen((NamedScreenHandlerFactory) oven);
                }
        }

        @Override
        public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
                return new GardenOvenBlockEntity(pos, state);
        }
}
