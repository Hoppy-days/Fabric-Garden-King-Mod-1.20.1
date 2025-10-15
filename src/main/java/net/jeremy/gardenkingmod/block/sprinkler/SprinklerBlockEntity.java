package net.jeremy.gardenkingmod.block.sprinkler;

import net.jeremy.gardenkingmod.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SprinklerBlockEntity extends BlockEntity {
        private int animationTicks;
        private SprinklerTier tier;

        public SprinklerBlockEntity(BlockPos pos, BlockState state) {
                super(ModBlockEntities.SPRINKLER_BLOCK_ENTITY, pos, state);
                this.tier = determineTier(state);
        }

        public static void tick(World world, BlockPos pos, BlockState state, SprinklerBlockEntity sprinkler) {
                if (!world.isClient && world instanceof ServerWorld serverWorld) {
                        sprinkler.ensureRegistered(serverWorld);
                }
                sprinkler.animationTicks++;
        }

        public SprinklerTier getTier() {
                return this.tier;
        }

        public boolean isWithinRange(BlockPos target) {
                BlockPos origin = this.getPos();
                int dx = Math.abs(target.getX() - origin.getX());
                int dz = Math.abs(target.getZ() - origin.getZ());
                int dy = Math.abs(target.getY() - origin.getY());
                return Math.max(dx, dz) <= this.tier.getHorizontalRadius() && dy <= this.tier.getVerticalRadius();
        }

        public float getAnimationProgress(float tickDelta) {
                return (this.animationTicks + tickDelta) / 20.0F;
        }

        @Override
        public void setCachedState(BlockState state) {
                super.setCachedState(state);
                this.tier = determineTier(state);
        }

        @Override
        public void setWorld(World world) {
                super.setWorld(world);
                if (world instanceof ServerWorld serverWorld) {
                        SprinklerHydrationManager.register(serverWorld, this);
                }
        }

        @Override
        public void cancelRemoval() {
                super.cancelRemoval();
                if (this.world instanceof ServerWorld serverWorld) {
                        SprinklerHydrationManager.register(serverWorld, this);
                }
        }

        @Override
        public void markRemoved() {
                if (this.world instanceof ServerWorld) {
                        SprinklerHydrationManager.unregister(this);
                }
                super.markRemoved();
        }

        @Override
        protected void writeNbt(NbtCompound nbt) {
                super.writeNbt(nbt);
                nbt.putInt("AnimTicks", this.animationTicks);
        }

        @Override
        public void readNbt(NbtCompound nbt) {
                super.readNbt(nbt);
                this.animationTicks = nbt.getInt("AnimTicks");
                if (this.getCachedState() != null) {
                        this.tier = determineTier(this.getCachedState());
                }
        }

        private SprinklerTier determineTier(BlockState state) {
                if (state.getBlock() instanceof SprinklerBlock sprinklerBlock) {
                        return sprinklerBlock.getTier();
                }
                return SprinklerTier.IRON;
        }

        private void ensureRegistered(ServerWorld world) {
                SprinklerHydrationManager.register(world, this);
        }
}
