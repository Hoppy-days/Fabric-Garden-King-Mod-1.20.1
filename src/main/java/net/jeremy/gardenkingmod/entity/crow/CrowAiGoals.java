package net.jeremy.gardenkingmod.entity.crow;

import java.util.EnumSet;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import net.jeremy.gardenkingmod.registry.ModEntities;

/**
 * Collection of reusable AI goals that power the crow's behavior stack. These
 * goals are intentionally lightweight so future hostile wildlife can reuse
 * them.
 */
public final class CrowAiGoals {
    private CrowAiGoals() {
    }

    public static class CrowFleeWardingGoal extends Goal {
        private static final int RECALC_TICKS = 20;

        private final CrowEntity crow;
        private final double speed;

        private BlockPos wardPos;
        private Vec3d escapeTarget;
        private int ticksUntilRecalc;

        public CrowFleeWardingGoal(CrowEntity crow) {
            this(crow, 1.5);
        }

        public CrowFleeWardingGoal(CrowEntity crow, double speed) {
            this.crow = crow;
            this.speed = speed;
            setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (crow.isRemoved()) {
                return false;
            }

            if (crow.getWorld().isClient) {
                return false;
            }

            crow.findNearestWard().ifPresent(pos -> this.wardPos = pos);
            if (wardPos == null) {
                return false;
            }

            escapeTarget = computeEscapeTarget();
            if (escapeTarget == null) {
                wardPos = null;
                return false;
            }

            return true;
        }

        @Override
        public boolean shouldContinue() {
            if (crow.getWorld().isClient) {
                return false;
            }

            Optional<BlockPos> nearest = crow.findNearestWard();
            if (nearest.isEmpty()) {
                return false;
            }

            wardPos = nearest.get();
            double horizontal = CrowBalanceConfig.get().wardHorizontalRadius()
                    * CrowBalanceConfig.get().wardFearRadiusMultiplier();
            double horizontalSq = MathHelper.square(horizontal);
            return crow.squaredDistanceTo(Vec3d.ofCenter(wardPos)) <= horizontalSq * 1.5;
        }

        @Override
        public void start() {
            ticksUntilRecalc = 0;
            if (escapeTarget != null) {
                crow.getNavigation().startMovingTo(escapeTarget.x, escapeTarget.y, escapeTarget.z, speed);
            }
        }

        @Override
        public void stop() {
            wardPos = null;
            escapeTarget = null;
            crow.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (wardPos == null) {
                return;
            }

            if (--ticksUntilRecalc <= 0) {
                ticksUntilRecalc = RECALC_TICKS;
                escapeTarget = computeEscapeTarget();
                if (escapeTarget != null) {
                    crow.getNavigation().startMovingTo(escapeTarget.x, escapeTarget.y, escapeTarget.z, speed);
                }
            }
        }

        @Nullable
        private Vec3d computeEscapeTarget() {
            if (wardPos == null) {
                return null;
            }

            Vec3d wardCenter = Vec3d.ofCenter(wardPos);
            Vec3d direction = crow.getPos().subtract(wardCenter);
            if (direction.lengthSquared() < 1.0E-4) {
                direction = new Vec3d(crow.getRandom().nextDouble() - 0.5, 0.1,
                        crow.getRandom().nextDouble() - 0.5);
            }

            direction = direction.normalize();
            double horizontal = CrowBalanceConfig.get().wardHorizontalRadius()
                    * CrowBalanceConfig.get().wardFearRadiusMultiplier() * 1.5;
            Vec3d candidate = crow.getPos().add(direction.multiply(horizontal)).add(0.0, 1.0, 0.0);

            if (!crow.getWorld().isChunkLoaded(BlockPos.ofFloored(candidate))) {
                return null;
            }

            return candidate;
        }
    }

    public static class CrowBreakCropGoal extends Goal {
        private static final int BREAK_TIME = 20;

        private final CrowEntity crow;
        private final double speed;

        private BlockPos targetCrop;
        private int breakingTicks;

        public CrowBreakCropGoal(CrowEntity crow) {
            this(crow, 1.2);
        }

        public CrowBreakCropGoal(CrowEntity crow, double speed) {
            this.crow = crow;
            this.speed = speed;
            setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (!crow.isHungry()) {
                return false;
            }

            if (!crow.getWorld().getGameRules().getBoolean(ModEntities.CROW_GRIEFING_RULE)) {
                return false;
            }

            targetCrop = crow.findCropTarget().orElse(null);
            if (targetCrop == null) {
                return false;
            }

            return crow.getWorld().isChunkLoaded(targetCrop);
        }

        @Override
        public boolean shouldContinue() {
            if (!crow.isHungry()) {
                return false;
            }

            if (targetCrop == null) {
                return false;
            }

            if (!crow.getWorld().getGameRules().getBoolean(ModEntities.CROW_GRIEFING_RULE)) {
                return false;
            }

            return crow.getWorld().isChunkLoaded(targetCrop)
                    && crow.getWorld().getBlockState(targetCrop).isIn(CrowTags.CROW_TARGET_CROPS);
        }

        @Override
        public void start() {
            breakingTicks = 0;
            if (targetCrop != null) {
                crow.getNavigation().startMovingTo(targetCrop.getX() + 0.5, targetCrop.getY() + 0.5,
                        targetCrop.getZ() + 0.5, speed);
            }
        }

        @Override
        public void stop() {
            crow.getNavigation().stop();
            targetCrop = null;
            breakingTicks = 0;
        }

        @Override
        public void tick() {
            if (targetCrop == null) {
                return;
            }

            double distanceSq = crow.squaredDistanceTo(Vec3d.ofCenter(targetCrop));
            if (distanceSq > 3.0) {
                crow.getNavigation().startMovingTo(targetCrop.getX() + 0.5, targetCrop.getY() + 0.5,
                        targetCrop.getZ() + 0.5, speed);
                breakingTicks = 0;
                return;
            }

            crow.getNavigation().stop();
            crow.getLookControl().lookAt(targetCrop.getX() + 0.5, targetCrop.getY() + 0.5, targetCrop.getZ() + 0.5);
            breakingTicks++;
            if (breakingTicks >= BREAK_TIME) {
                if (crow.tryBreakCrop(targetCrop)) {
                    // Hunger will be reset in the crow's crop hook.
                }
                targetCrop = null;
            }
        }
    }

    public static class CrowRandomFlyGoal extends Goal {
        private final CrowEntity crow;
        private final double speed;

        @Nullable
        private Vec3d target;

        public CrowRandomFlyGoal(CrowEntity crow) {
            this(crow, 1.0);
        }

        public CrowRandomFlyGoal(CrowEntity crow, double speed) {
            this.crow = crow;
            this.speed = speed;
            setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (crow.isHungry()) {
                return false;
            }

            if (!crow.getNavigation().isIdle()) {
                return false;
            }

            target = crow.getRandomFlightTarget();
            return target != null;
        }

        @Override
        public boolean shouldContinue() {
            return target != null && !crow.isHungry() && !crow.getNavigation().isIdle();
        }

        @Override
        public void start() {
            if (target != null) {
                crow.getNavigation().startMovingTo(target.x, target.y, target.z, speed);
            }
        }

        @Override
        public void stop() {
            target = null;
            crow.getNavigation().stop();
        }
    }

    public static class CrowPerchGoal extends Goal {
        private final CrowEntity crow;
        private final double speed;

        private BlockPos perchPos;
        private int perchTicks;

        public CrowPerchGoal(CrowEntity crow) {
            this(crow, 1.0);
        }

        public CrowPerchGoal(CrowEntity crow, double speed) {
            this.crow = crow;
            this.speed = speed;
            setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (crow.isHungry()) {
                return false;
            }

            if (crow.getRandom().nextInt(5) != 0) {
                return false;
            }

            perchPos = crow.findPerchTarget().orElse(null);
            return perchPos != null;
        }

        @Override
        public boolean shouldContinue() {
            return perchPos != null && !crow.isHungry() && perchTicks < 100;
        }

        @Override
        public void start() {
            perchTicks = 0;
            if (perchPos != null) {
                crow.getNavigation().startMovingTo(perchPos.getX() + 0.5, perchPos.getY(), perchPos.getZ() + 0.5, speed);
            }
        }

        @Override
        public void stop() {
            crow.getNavigation().stop();
            perchPos = null;
            perchTicks = 0;
        }

        @Override
        public void tick() {
            if (perchPos == null) {
                return;
            }

            if (crow.getBlockPos().equals(perchPos) || crow.squaredDistanceTo(Vec3d.ofCenter(perchPos)) < 1.0) {
                perchTicks++;
                crow.setVelocity(Vec3d.ZERO);
                crow.teleport(perchPos.getX() + 0.5, perchPos.getY() + 0.1, perchPos.getZ() + 0.5);
            }
        }
    }
}
