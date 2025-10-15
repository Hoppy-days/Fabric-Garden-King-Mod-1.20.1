package net.jeremy.gardenkingmod.block.sprinkler;

import net.jeremy.gardenkingmod.ModBlockEntities;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SprinklerBlock extends BlockWithEntity {
        private static final VoxelShape OUTLINE_SHAPE = createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

        private final SprinklerTier tier;

        public SprinklerBlock(Settings settings, SprinklerTier tier) {
                super(settings);
                this.tier = tier;
        }

        @Override
        public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
                return new SprinklerBlockEntity(pos, state);
        }

        public SprinklerTier getTier() {
                return this.tier;
        }

        @Override
        public BlockRenderType getRenderType(BlockState state) {
                return BlockRenderType.ENTITYBLOCK_ANIMATED;
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
                return OUTLINE_SHAPE;
        }

        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
                return checkType(type, ModBlockEntities.SPRINKLER_BLOCK_ENTITY, SprinklerBlockEntity::tick);
        }
}
