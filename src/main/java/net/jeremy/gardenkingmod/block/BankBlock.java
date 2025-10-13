package net.jeremy.gardenkingmod.block;

import net.jeremy.gardenkingmod.block.entity.BankBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class BankBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape SHAPE = VoxelShapes.fullCube();

    public BankBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        WorldView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        if (pos.getY() >= world.getTopY() - 1) {
            return null;
        }

        BlockPos abovePos = pos.up();
        if (!world.getBlockState(abovePos).canReplace(ctx)) {
            return null;
        }

        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.isClient) {
            return;
        }

        BlockPos abovePos = pos.up();
        BlockState upperState = state.with(HALF, DoubleBlockHalf.UPPER);
        world.setBlockState(abovePos, upperState, Block.NOTIFY_ALL);

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BankBlockEntity bankBlockEntity && placer instanceof PlayerEntity player) {
            bankBlockEntity.setOwner(player.getUuid());
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        DoubleBlockHalf half = state.get(HALF);
        BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
        BlockState otherState = world.getBlockState(otherPos);
        if (otherState.isOf(this) && otherState.get(HALF) != half) {
            world.setBlockState(otherPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() == newState.getBlock()) {
            super.onStateReplaced(state, world, pos, newState, moved);
            return;
        }

        DoubleBlockHalf half = state.get(HALF);
        BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
        BlockState otherState = world.getBlockState(otherPos);
        if (otherState.isOf(this) && otherState.get(HALF) != half) {
            world.removeBlock(otherPos, false);
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
            WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.get(HALF);
        if (direction.getAxis() == Direction.Axis.Y) {
            if (half == DoubleBlockHalf.LOWER && direction == Direction.UP) {
                return neighborState.isOf(this) && neighborState.get(HALF) == DoubleBlockHalf.UPPER
                        ? state
                        : Blocks.AIR.getDefaultState();
            }
            if (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN) {
                return neighborState.isOf(this) && neighborState.get(HALF) == DoubleBlockHalf.LOWER
                        ? state
                        : Blocks.AIR.getDefaultState();
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
            BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockPos lowerPos = state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
        BlockEntity blockEntity = world.getBlockEntity(lowerPos);
        if (blockEntity instanceof BankBlockEntity bankBlockEntity) {
            if (bankBlockEntity.canAccess(player)) {
                player.openHandledScreen(bankBlockEntity);
            } else {
                player.sendMessage(Text.translatable("message.gardenkingmod.bank.not_owner"), true);
            }
        }

        return ActionResult.CONSUME;
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
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return new BankBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.util.BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.util.BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}
