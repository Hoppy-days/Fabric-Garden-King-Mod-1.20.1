package net.jeremy.gardenkingmod.block;

import net.jeremy.gardenkingmod.ModBlocks;
import net.jeremy.gardenkingmod.block.entity.GearShopBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class GearShopBlock extends BlockWithEntity {
        public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

        public GearShopBlock(Settings settings) {
                super(settings);
                this.setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
                builder.add(FACING);
        }

        @Override
        public BlockState getPlacementState(ItemPlacementContext ctx) {
                Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
                BlockPos origin = ctx.getBlockPos();
                WorldView worldView = ctx.getWorld();

                for (GearShopBlockPart.Part part : GearShopBlockPart.Part.values()) {
                        if (part == GearShopBlockPart.Part.CENTER) {
                                continue;
                        }

                        BlockPos targetPos = origin.add(part.getOffset(facing));
                        if (!worldView.isAir(targetPos)) {
                                return null;
                        }
                }

                return getDefaultState().with(FACING, facing);
        }

        @Override
        public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
                super.onPlaced(world, pos, state, placer, itemStack);

                if (world.isClient) {
                        return;
                }

                Direction facing = state.get(FACING);
                for (GearShopBlockPart.Part part : GearShopBlockPart.Part.values()) {
                        if (part == GearShopBlockPart.Part.CENTER) {
                                continue;
                        }

                        BlockPos targetPos = pos.add(part.getOffset(facing));
                        BlockState partState = ModBlocks.GEAR_SHOP_BLOCK_PART.getDefaultState()
                                        .with(GearShopBlockPart.FACING, facing)
                                        .with(GearShopBlockPart.PART, part);
                        world.setBlockState(targetPos, partState, Block.NOTIFY_ALL | Block.FORCE_STATE);
                }
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
                return GearShopBlockPart.getShape(GearShopBlockPart.Part.CENTER, state.get(FACING));
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
                return GearShopBlockPart.getShape(GearShopBlockPart.Part.CENTER, state.get(FACING));
        }

        @Override
        public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
                return 1.0F;
        }

        @Override
        public BlockRenderType getRenderType(BlockState state) {
                return BlockRenderType.ENTITYBLOCK_ANIMATED;
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                        BlockHitResult hit) {
                if (!world.isClient) {
                        BlockEntity blockEntity = world.getBlockEntity(pos);
                        if (blockEntity instanceof GearShopBlockEntity gearShopBlockEntity) {
                                player.openHandledScreen(gearShopBlockEntity);
                        }
                }

                return ActionResult.SUCCESS;
        }

        @Override
        public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
                if (state.getBlock() != newState.getBlock()) {
                        if (!world.isClient) {
                                removePartBlocks(world, pos, state.get(FACING));
                        }

                        BlockEntity blockEntity = world.getBlockEntity(pos);
                        if (blockEntity instanceof Inventory inventory) {
                                ItemScatterer.spawn(world, pos, inventory);
                                world.updateComparators(pos, this);
                        }

                        super.onStateReplaced(state, world, pos, newState, moved);
                }
        }

        private void removePartBlocks(World world, BlockPos origin, Direction facing) {
                for (GearShopBlockPart.Part part : GearShopBlockPart.Part.values()) {
                        if (part == GearShopBlockPart.Part.CENTER) {
                                continue;
                        }

                        BlockPos targetPos = origin.add(part.getOffset(facing));
                        BlockState targetState = world.getBlockState(targetPos);
                        if (targetState.getBlock() instanceof GearShopBlockPart) {
                                world.removeBlock(targetPos, false);
                        }
                }
        }

        @Override
        public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
                return new GearShopBlockEntity(pos, state);
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
