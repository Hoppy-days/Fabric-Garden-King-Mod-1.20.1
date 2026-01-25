package net.jeremy.gardenkingmod.block.ward;

import net.jeremy.gardenkingmod.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class ScarecrowBlock extends BlockWithEntity {
        private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
        public static final BooleanProperty POWERED = BooleanProperty.of("powered");

        public ScarecrowBlock(Settings settings) {
                super(settings);
                this.setDefaultState(this.stateManager.getDefaultState().with(POWERED, false));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
                builder.add(POWERED);
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
                return SHAPE;
        }

        @Override
        public BlockRenderType getRenderType(BlockState state) {
                return BlockRenderType.ENTITYBLOCK_ANIMATED;
        }

        @Override
        public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
                BlockState below = world.getBlockState(pos.down());
                boolean hasHeadroom = world.getBlockState(pos.up()).isAir();
                boolean sturdy = below.isSideSolidFullSquare(world, pos.down(), Direction.UP)
                                || below.isOf(Blocks.FARMLAND) || below.isOf(Blocks.DIRT) || below.isOf(Blocks.COARSE_DIRT)
                                || below.isOf(Blocks.GRASS_BLOCK) || below.isOf(Blocks.ROOTED_DIRT);
                return hasHeadroom && sturdy;
        }

        @Override
        public BlockState getPlacementState(ItemPlacementContext ctx) {
                WorldView worldView = ctx.getWorld();
                BlockPos pos = ctx.getBlockPos();
                if (!worldView.getBlockState(pos.up()).canReplace(ctx)) {
                        return null;
                }
                boolean powered = worldView.isReceivingRedstonePower(pos) || worldView.isReceivingRedstonePower(pos.up());
                BlockState baseState = super.getPlacementState(ctx);
                if (baseState == null) {
                        return null;
                }
                return baseState.with(POWERED, powered);
        }

        @Override
        public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                        WorldAccess world, BlockPos pos, BlockPos neighborPos) {
                if (!state.canPlaceAt(world, pos)) {
                        world.scheduleBlockTick(pos, this, 1);
                }
                boolean powered = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
                if (powered != state.get(POWERED)) {
                        return state.with(POWERED, powered);
                }
                return state;
        }

        @Override
        public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
                if (!state.canPlaceAt(world, pos)) {
                        dropStack(world, pos, new ItemStack(this));
                        world.breakBlock(pos, false);
                }
        }

        @Override
        public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
                if (state.isOf(newState.getBlock())) {
                        super.onStateReplaced(state, world, pos, newState, moved);
                        return;
                }

                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof ScarecrowBlockEntity scarecrow) {
                        ItemScatterer.spawn(world, pos, scarecrow);
                        world.updateComparators(pos, this);
                }

                super.onStateReplaced(state, world, pos, newState, moved);
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                        BlockHitResult hit) {
                if (world.isClient) {
                        return ActionResult.SUCCESS;
                }

                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof ScarecrowBlockEntity scarecrow) {
                        NamedScreenHandlerFactory factory = scarecrow;
                        player.openHandledScreen(factory);
                        return ActionResult.CONSUME;
                }
                return ActionResult.PASS;
        }

        @Override
        public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
                return new ScarecrowBlockEntity(pos, state);
        }

        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                        BlockEntityType<T> type) {
                if (world.isClient) {
                        return null;
                }
                return checkType(type, ModBlockEntities.SCARECROW_BLOCK_ENTITY, ScarecrowBlockEntity::tick);
        }
}
