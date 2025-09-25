package net.jeremy.gardenkingmod.entity.crow;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
public class CrowEntity extends PathAwareEntity {
        public CrowEntity(EntityType<? extends CrowEntity> entityType, World world) {
                super(entityType, world);
                this.moveControl = new FlightMoveControl(this, 20, true);
        }

        @Override
        protected void initGoals() {
                this.goalSelector.add(0, new SwimGoal(this));
                this.goalSelector.add(1, new FlyGoal(this, 1.0));
                this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0));
                this.goalSelector.add(3, new LookAroundGoal(this));
        }

        @Override
        protected EntityNavigation createNavigation(World world) {
                BirdNavigation navigation = new BirdNavigation(this, world);
                navigation.setCanPathThroughDoors(false);
                navigation.setCanEnterOpenDoors(true);
                navigation.setCanSwim(false);
                return navigation;
        }

        @Override
        public boolean isInAir() {
                return !this.isOnGround();
        }

        @Override
        public boolean isClimbing() {
                return false;
        }

        @Override
        public boolean isPushable() {
                return false;
        }

        @Override
        public void tickMovement() {
                super.tickMovement();
                if (this.world.isClient) {
                        return;
                }

                if (this.isOnGround()) {
                        this.setVelocity(this.getVelocity().multiply(0.6));
                }
        }

        public static DefaultAttributeContainer.Builder createCrowAttributes() {
                return MobEntity.createMobAttributes()
                                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
                                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
        }
}
