package net.jeremy.gardenkingmod.entity.crow;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.jeremy.gardenkingmod.registry.ModEntities;
import net.jeremy.gardenkingmod.registry.ModSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

/**
 * Server-side implementation of the crow mob. Handles configuration-driven
 * hunger timers, crop hunting, and ward awareness.
 */
public class CrowEntity extends PathAwareEntity {
    private static final TrackedData<Integer> HUNGER_TICKS = DataTracker.registerData(CrowEntity.class,
            TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> HUNGRY = DataTracker.registerData(CrowEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);

    private static final int MIN_DESPERATION_TICKS = 200;

    private int timeSinceCropBreak;
    private boolean desperateForFood;

    public CrowEntity(EntityType<? extends CrowEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 8, true);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 0.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, 0.0f);
        this.setPathfindingPenalty(PathNodeType.WATER, -1.0f);
        this.experiencePoints = 3;
    }

    /**
     * Defines the base attribute container using the values supplied by the
     * configuration.
     */
    public static DefaultAttributeContainer.Builder createCrowAttributes() {
        CrowBalanceConfig config = CrowBalanceConfig.get();
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, config.baseHealth())
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, config.movementSpeed())
                .add(EntityAttributes.GENERIC_FLYING_SPEED, config.flyingSpeed())
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
    }

    public static boolean canSpawn(EntityType<CrowEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos,
            Random random) {
        if (reason != SpawnReason.NATURAL && reason != SpawnReason.CHUNK_GENERATION) {
            return true;
        }

        int surfaceY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if (pos.getY() < surfaceY) {
            return false;
        }

        return world.getBaseLightLevel(pos, 0) > 7;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new CrowAiGoals.CrowFleeWardingGoal(this));
        this.goalSelector.add(1, new CrowAiGoals.CrowBreakCropGoal(this));
        this.goalSelector.add(2, new CrowAiGoals.CrowRandomFlyGoal(this));
        this.goalSelector.add(3, new CrowAiGoals.CrowPerchGoal(this));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation navigation = new BirdNavigation(this, world);
        navigation.setCanEnterOpenDoors(true);
        navigation.setCanSwim(false);
        return navigation;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HUNGER_TICKS, CrowBalanceConfig.get().chooseHungerDuration());
        this.dataTracker.startTracking(HUNGRY, false);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient) {
            tickHunger();
        }
    }

    private void tickHunger() {
        if (isHungry()) {
            return;
        }

        int hunger = getHungerTicks();
        if (hunger > 0) {
            setHungerTicks(hunger - 1);
        } else {
            setHungry(true);
        }
    }

    @Override
    public void mobTick() {
        super.mobTick();
        if (timeSinceCropBreak < Integer.MAX_VALUE) {
            timeSinceCropBreak++;
        }

        updateDesperationState();
    }

    @Override
    public boolean isFlappingWings() {
        return !this.isOnGround() && this.getVelocity().lengthSquared() > 1.0E-4;
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isTouchingWater()) {
            this.updateVelocity(0.02f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.8f));
            return;
        }

        if (this.isInLava()) {
            this.updateVelocity(0.02f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.5));
            return;
        }

        float speed = 0.1f;
        this.updateVelocity(speed, movementInput);
        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.91));
    }

    @Override
    public void setNoGravity(boolean noGravity) {
        super.setNoGravity(true);
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    @Override
    public void playAmbientSound() {
        SoundEvent soundEvent = getAmbientSound();
        if (soundEvent != null) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, SoundCategory.NEUTRAL,
                    0.8f, 0.9f + this.random.nextFloat() * 0.2f);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.CROW_CAW;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.CROW_CAW;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.CROW_CAW;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (damageSource == this.getDamageSources().fall()) {
            return true;
        }
        return super.isInvulnerableTo(damageSource);
    }

    public int getHungerTicks() {
        return this.dataTracker.get(HUNGER_TICKS);
    }

    public void setHungerTicks(int ticks) {
        this.dataTracker.set(HUNGER_TICKS, Math.max(0, ticks));
    }

    public boolean isHungry() {
        return this.dataTracker.get(HUNGRY);
    }

    public void setHungry(boolean hungry) {
        this.dataTracker.set(HUNGRY, hungry);
        if (!hungry) {
            desperateForFood = false;
        }
    }

    public void resetHunger() {
        setHungry(false);
        setHungerTicks(CrowBalanceConfig.get().chooseHungerDuration());
        timeSinceCropBreak = 0;
        desperateForFood = false;
    }

    public int getTimeSinceCropBreak() {
        return timeSinceCropBreak;
    }

    public boolean isDesperateForFood() {
        return desperateForFood;
    }

    public boolean canIgnoreHungerLockout() {
        return !isHungry() || isDesperateForFood();
    }

    public Optional<BlockPos> findCropTarget() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return Optional.empty();
        }

        CrowBalanceConfig config = CrowBalanceConfig.get();
        BlockPos origin = this.getBlockPos();
        int horizontal = config.cropSearchHorizontal();
        int vertical = config.cropSearchVertical();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int y = -vertical; y <= vertical; y++) {
            int actualY = origin.getY() + y;
            if (actualY < serverWorld.getBottomY() || actualY > serverWorld.getTopY() - 1) {
                continue;
            }

            for (int dx = -horizontal; dx <= horizontal; dx++) {
                for (int dz = -horizontal; dz <= horizontal; dz++) {
                    mutable.set(origin.getX() + dx, actualY, origin.getZ() + dz);
                    if (!serverWorld.isChunkLoaded(mutable)) {
                        continue;
                    }

                    BlockState state = serverWorld.getBlockState(mutable);
                    if (!state.isIn(CrowTags.CROW_TARGET_CROPS)) {
                        continue;
                    }

                    if (!isMatureCrop(state)) {
                        continue;
                    }

                    double distance = mutable.getSquaredDistance(origin);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = mutable.toImmutable();
                    }
                }
            }
        }

        return Optional.ofNullable(best);
    }

    public boolean isMatureCrop(BlockState state) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            return cropBlock.isMature(state);
        }
        return true;
    }

    public boolean tryBreakCrop(BlockPos pos) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }

        if (!serverWorld.getGameRules().getBoolean(ModEntities.CROW_GRIEFING_RULE)) {
            return false;
        }

        BlockState state = serverWorld.getBlockState(pos);
        if (!state.isIn(CrowTags.CROW_TARGET_CROPS)) {
            return false;
        }

        if (!isMatureCrop(state)) {
            return false;
        }

        boolean dropLoot = CrowBalanceConfig.get().dropLootOnCropBreak();
        boolean broke = serverWorld.breakBlock(pos, dropLoot, this);
        if (broke) {
            onCropBroken(state, pos);
        }
        return broke;
    }

    protected void onCropBroken(BlockState state, BlockPos pos) {
        this.resetHunger();
        this.playSound(ModSoundEvents.CROW_CROP_BREAK, 0.9f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    public Optional<BlockPos> findNearestWard() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return Optional.empty();
        }

        CrowBalanceConfig config = CrowBalanceConfig.get();
        double multiplier = config.wardFearRadiusMultiplier();
        int horizontal = MathHelper.ceil(config.wardHorizontalRadius() * multiplier);
        int vertical = MathHelper.ceil(config.wardVerticalRadius() * multiplier);
        BlockPos origin = this.getBlockPos();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (int y = -vertical; y <= vertical; y++) {
            int actualY = origin.getY() + y;
            if (actualY < serverWorld.getBottomY() || actualY > serverWorld.getTopY() - 1) {
                continue;
            }

            for (int dx = -horizontal; dx <= horizontal; dx++) {
                for (int dz = -horizontal; dz <= horizontal; dz++) {
                    mutable.set(origin.getX() + dx, actualY, origin.getZ() + dz);
                    if (!serverWorld.isChunkLoaded(mutable)) {
                        continue;
                    }

                    BlockState state = serverWorld.getBlockState(mutable);
                    if (!state.isIn(CrowTags.CROW_WARD_BLOCKS)) {
                        continue;
                    }

                    double distance = mutable.getSquaredDistance(origin);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closest = mutable.toImmutable();
                    }
                }
            }
        }

        return Optional.ofNullable(closest);
    }

    public Optional<BlockPos> findPerchTarget() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return Optional.empty();
        }

        CrowBalanceConfig config = CrowBalanceConfig.get();
        int range = MathHelper.ceil(config.perchSearchRange());
        BlockPos origin = this.getBlockPos();
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int attempt = 0; attempt < 12; attempt++) {
            int dx = MathHelper.nextInt(this.random, -range, range);
            int dz = MathHelper.nextInt(this.random, -range, range);
            int dy = MathHelper.nextInt(this.random, -2, 2);
            mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
            if (!serverWorld.isChunkLoaded(mutable)) {
                continue;
            }

            BlockState state = serverWorld.getBlockState(mutable);
            if (!state.isIn(CrowTags.CROW_PERCH_BLOCKS)) {
                continue;
            }

            BlockPos above = mutable.up();
            if (!serverWorld.isAir(above)) {
                continue;
            }

            return Optional.of(above);
        }

        return Optional.empty();
    }

    @Nullable
    public Vec3d getRandomFlightTarget() {
        CrowBalanceConfig config = CrowBalanceConfig.get();
        Vec3d base = this.getPos();
        Vec3d wardCenter = findNearestWard().map(Vec3d::ofCenter).orElse(null);
        double range = Math.max(4.0, config.randomFlightRange());

        double minY = this.getWorld().getBottomY() + 1;
        double maxY = this.getWorld().getTopY() - 1;
        double baseY = MathHelper.clamp(base.y, minY, maxY);
        double verticalRange = Math.max(range, 4.0);

        for (int attempt = 0; attempt < 12; attempt++) {
            double dx = (this.random.nextDouble() * 2.0 - 1.0) * range;
            double dz = (this.random.nextDouble() * 2.0 - 1.0) * range;
            double dy = (this.random.nextDouble() * 2.0 - 1.0) * verticalRange;

            if (baseY - minY < 4.0) {
                dy = Math.abs(dy) + this.random.nextDouble() * verticalRange * 0.25;
            }

            double targetY = MathHelper.clamp(baseY + dy, minY, maxY);
            Vec3d candidate = new Vec3d(base.x + dx, targetY, base.z + dz);
            if (wardCenter != null) {
                double currentDistance = base.squaredDistanceTo(wardCenter);
                double candidateDistance = candidate.squaredDistanceTo(wardCenter);
                if (candidateDistance <= currentDistance) {
                    continue;
                }
            }

            if (this.getWorld().isChunkLoaded(BlockPos.ofFloored(candidate))) {
                return candidate;
            }
        }

        if (wardCenter != null) {
            Vec3d direction = base.subtract(wardCenter);
            if (direction.lengthSquared() > 1.0E-4) {
                Vec3d normalized = direction.normalize();
                double targetY = MathHelper.clamp(baseY + Math.max(1.0, range * 0.25), minY, maxY);
                return new Vec3d(base.x + normalized.x, targetY, base.z + normalized.z);
            }
        }

        double fallbackY = MathHelper.clamp(baseY + Math.max(1.0, range * 0.3), minY, maxY);
        return new Vec3d(base.x, fallbackY, base.z);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("HungerTicks", getHungerTicks());
        nbt.putBoolean("Hungry", isHungry());
        nbt.putInt("TimeSinceCropBreak", timeSinceCropBreak);
        nbt.putBoolean("Desperate", desperateForFood);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setHungerTicks(nbt.getInt("HungerTicks"));
        setHungry(nbt.getBoolean("Hungry"));
        timeSinceCropBreak = nbt.getInt("TimeSinceCropBreak");
        desperateForFood = nbt.getBoolean("Desperate") && isHungry();
    }

    private void updateDesperationState() {
        if (!isHungry()) {
            desperateForFood = false;
            return;
        }

        if (!desperateForFood && timeSinceCropBreak >= getDesperationDelay()) {
            desperateForFood = true;
        }
    }

    private int getDesperationDelay() {
        CrowBalanceConfig config = CrowBalanceConfig.get();
        return Math.max(MIN_DESPERATION_TICKS, config.minHungerTicks());
    }
}
