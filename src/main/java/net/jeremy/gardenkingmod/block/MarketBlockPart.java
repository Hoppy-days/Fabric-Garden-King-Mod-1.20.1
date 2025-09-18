package net.jeremy.gardenkingmod.block;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class MarketBlockPart extends Block {
        public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
        public static final EnumProperty<Part> PART = EnumProperty.of("part", Part.class);

        private static final Map<Part, VoxelShape> BASE_SHAPES = createBaseShapes();

        public MarketBlockPart(Settings settings) {
                super(settings);
                this.setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(PART,
                                Part.CENTER));
        }

        private static Map<Part, VoxelShape> createBaseShapes() {
                EnumMap<Part, VoxelShape> shapes = new EnumMap<>(Part.class);
                for (Part part : Part.values()) {
                        shapes.put(part, VoxelShapes.fullCube());
                }
                return shapes;
        }

        public static VoxelShape getShape(Part part, Direction facing) {
                return BASE_SHAPES.getOrDefault(part, VoxelShapes.fullCube());
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
                builder.add(FACING, PART);
        }

        @Override
        public BlockRenderType getRenderType(BlockState state) {
                return BlockRenderType.INVISIBLE;
        }

        public static BlockPos getOrigin(BlockPos partPos, BlockState state) {
                BlockPos offset = state.get(PART).getOffset(state.get(FACING));
                return partPos.subtract(offset);
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                        BlockHitResult hit) {
                BlockPos origin = getOrigin(pos, state);
                BlockState originState = world.getBlockState(origin);
                if (!(originState.getBlock() instanceof MarketBlock)) {
                        return ActionResult.PASS;
                }

                BlockHitResult translatedHit = new BlockHitResult(hit.getPos(), hit.getSide(), origin,
                                hit.isInsideBlock());
                return originState.onUse(world, player, hand, translatedHit);
        }

        @Override
        public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
                if (state.getBlock() != newState.getBlock()) {
                        if (!world.isClient) {
                                BlockPos origin = getOrigin(pos, state);
                                BlockState originState = world.getBlockState(origin);
                                if (originState.getBlock() instanceof MarketBlock) {
                                        world.breakBlock(origin, true);
                                }
                        }

                        super.onStateReplaced(state, world, pos, newState, moved);
                }
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
                return getShape(state.get(PART), state.get(FACING));
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
                return getShape(state.get(PART), state.get(FACING));
        }

        @Override

        public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
                return 1.0F;
        }

        @Override


        public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
                return ItemStack.EMPTY;
        }

        @Override
        public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
                return Collections.emptyList();
        }

        public enum Part implements StringIdentifiable {
                NORTH_WEST("north_west", new BlockPos(-1, 0, -1)),
                NORTH("north", new BlockPos(0, 0, -1)),
                NORTH_EAST("north_east", new BlockPos(1, 0, -1)),
                WEST("west", new BlockPos(-1, 0, 0)),
                CENTER("center", BlockPos.ORIGIN),
                EAST("east", new BlockPos(1, 0, 0)),
                SOUTH_WEST("south_west", new BlockPos(-1, 0, 1)),
                SOUTH("south", new BlockPos(0, 0, 1)),
                SOUTH_EAST("south_east", new BlockPos(1, 0, 1));

                private final String name;
                private final BlockPos offset;

                Part(String name, BlockPos offset) {
                        this.name = name;
                        this.offset = offset;
                }

                public BlockPos getOffset(Direction facing) {
                        return offset.rotate(rotationFromFacing(facing));
                }

                @Override
                public String asString() {
                        return name;
                }
        }

        private static BlockRotation rotationFromFacing(Direction facing) {
                return switch (facing) {
                        case NORTH -> BlockRotation.NONE;
                        case SOUTH -> BlockRotation.CLOCKWISE_180;
                        case WEST -> BlockRotation.COUNTERCLOCKWISE_90;
                        case EAST -> BlockRotation.CLOCKWISE_90;
                        default -> BlockRotation.NONE;
                };
        }
}
