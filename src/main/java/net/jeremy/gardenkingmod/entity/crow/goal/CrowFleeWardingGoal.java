package net.jeremy.gardenkingmod.entity.crow.goal;

import java.util.EnumSet;
import java.util.Optional;

import net.jeremy.gardenkingmod.block.ward.ScarecrowAuraComponent;
import net.jeremy.gardenkingmod.block.ward.ScarecrowBlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CrowFleeWardingGoal extends Goal {
        private static final int STATUS_EFFECT_DURATION = 60;
        private static final int DAMAGE_INTERVAL = 20;

        private final PathAwareEntity crow;
        private final double speed;

        private ScarecrowBlockEntity scarecrow;
        private Vec3d targetPos;
        private int ticksInAura;

        public CrowFleeWardingGoal(PathAwareEntity crow, double speed) {
                this.crow = crow;
                this.speed = speed;
                this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
                if (!(this.crow.getWorld() instanceof ServerWorld serverWorld)) {
                        return false;
                }

                Optional<ScarecrowBlockEntity> optionalScarecrow = ScarecrowAuraComponent
                                .findNearestActiveAura(serverWorld, this.crow.getPos());
                if (optionalScarecrow.isEmpty()) {
                        return false;
                }

                this.scarecrow = optionalScarecrow.get();
                this.targetPos = computeFleeTarget(this.scarecrow);
                return this.targetPos != null;
        }

        @Override
        public boolean shouldContinue() {
                if (this.scarecrow == null) {
                        return false;
                }

                if (!(this.crow.getWorld() instanceof ServerWorld serverWorld)) {
                        return false;
                }

                if (!this.scarecrow.hasRecentPulse(serverWorld.getTime())) {
                                return false;
                }

                boolean insideAura = this.scarecrow.isWithinAura(this.crow.getPos());
                return insideAura || !this.crow.getNavigation().isIdle();
        }

        @Override
        public void start() {
                if (this.targetPos != null) {
                        this.crow.getNavigation().startMovingTo(this.targetPos.x, this.targetPos.y, this.targetPos.z,
                                        this.speed);
                }
                this.ticksInAura = 0;
        }

        @Override
        public void stop() {
                this.scarecrow = null;
                this.targetPos = null;
                this.ticksInAura = 0;
        }

        @Override
        public void tick() {
                if (!(this.crow.getWorld() instanceof ServerWorld serverWorld)) {
                        return;
                }

                if (this.scarecrow == null) {
                        return;
                }

                if (this.scarecrow.isWithinAura(this.crow.getPos())) {
                        this.ticksInAura++;
                        applyRepelEffects(serverWorld);

                        if (this.crow.getNavigation().isIdle()) {
                                this.targetPos = computeFleeTarget(this.scarecrow);
                                if (this.targetPos != null) {
                                        this.crow.getNavigation().startMovingTo(this.targetPos.x, this.targetPos.y,
                                                        this.targetPos.z, this.speed);
                                }
                        }
                } else if (this.targetPos == null || this.crow.getNavigation().isIdle()) {
                        this.targetPos = computeFleeTarget(this.scarecrow);
                        if (this.targetPos != null) {
                                this.crow.getNavigation().startMovingTo(this.targetPos.x, this.targetPos.y,
                                                this.targetPos.z, this.speed);
                        }
                }
        }

        private void applyRepelEffects(ServerWorld world) {
                if (!this.crow.hasStatusEffect(StatusEffects.SLOWNESS)) {
                        this.crow.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, STATUS_EFFECT_DURATION,
                                        0, false, false, true));
                }

                if (this.ticksInAura % DAMAGE_INTERVAL == 0) {
                        this.scarecrow.onCrowRepelled(world);
                }
        }

        private Vec3d computeFleeTarget(ScarecrowBlockEntity scarecrow) {
                Vec3d scarecrowCenter = Vec3d.ofCenter(scarecrow.getPos());
                Vec3d current = this.crow.getPos();
                Vec3d away = current.subtract(scarecrowCenter);
                double horizontalRadius = scarecrow.getHorizontalAuraRadius() + 6.0;

                if (away.lengthSquared() < 1.0E-4) {
                        double yaw = this.crow.getRandom().nextDouble() * MathHelper.TAU;
                        away = new Vec3d(MathHelper.cos((float) yaw), 0.0, MathHelper.sin((float) yaw));
                }

                away = away.normalize().multiply(horizontalRadius);
                Vec3d target = current.add(away);
                double clampedY = MathHelper.clamp(target.y,
                                scarecrow.getPos().getY() - scarecrow.getVerticalAuraRadius(),
                                scarecrow.getPos().getY() + scarecrow.getVerticalAuraRadius());

                return new Vec3d(target.x, clampedY, target.z);
        }
}
