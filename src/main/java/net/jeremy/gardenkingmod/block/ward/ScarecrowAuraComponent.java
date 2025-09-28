package net.jeremy.gardenkingmod.block.ward;

import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ScarecrowAuraComponent {
        public static final int BASE_HORIZONTAL_RADIUS = 12;
        public static final int BASE_VERTICAL_RADIUS = 8;
        public static final int PULSE_INTERVAL_TICKS = 40;
        public static final int PULSE_DURATION_TICKS = 45;

        private static final Map<ServerWorld, Set<ScarecrowBlockEntity>> ACTIVE = new WeakHashMap<>();

        private final ScarecrowBlockEntity owner;
        private int pulseCooldown;
        private long lastPulseTick;

        ScarecrowAuraComponent(ScarecrowBlockEntity owner) {
                this.owner = owner;
                this.pulseCooldown = 0;
                this.lastPulseTick = 0L;
        }

        static void register(ServerWorld world, ScarecrowBlockEntity entity) {
                ACTIVE.computeIfAbsent(world, key -> Collections.newSetFromMap(new IdentityHashMap<>())).add(entity);
        }

        static void unregister(ScarecrowBlockEntity entity) {
                if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
                        return;
                }
                Set<ScarecrowBlockEntity> entities = ACTIVE.get(serverWorld);
                if (entities == null) {
                        return;
                }
                entities.remove(entity);
                if (entities.isEmpty()) {
                        ACTIVE.remove(serverWorld);
                }
        }

        public static Optional<ScarecrowBlockEntity> findNearestActiveAura(ServerWorld world, Vec3d position) {
                Set<ScarecrowBlockEntity> entities = ACTIVE.get(world);
                if (entities == null || entities.isEmpty()) {
                        return Optional.empty();
                }
                long time = world.getTime();
                return entities.stream().filter(entity -> entity.isAuraActive())
                                .filter(entity -> entity.hasRecentPulse(time))
                                .filter(entity -> entity.isWithinAura(position))
                                .min(Comparator.comparingDouble(entity -> squaredDistance(entity.getPos(), position)));
        }

        private static double squaredDistance(BlockPos pos, Vec3d position) {
                double dx = position.x - (pos.getX() + 0.5);
                double dy = position.y - (pos.getY() + 0.5);
                double dz = position.z - (pos.getZ() + 0.5);
                return dx * dx + dy * dy + dz * dz;
        }

        void tick(ServerWorld world) {
                if (!owner.isAuraActive()) {
                        return;
                }

                ScarecrowBlockEntity.PitchforkAuraStats stats = owner.getPitchforkAuraStats();
                int interval = Math.max(1, stats.pulseIntervalTicks());

                if (this.pulseCooldown > interval) {
                        this.pulseCooldown = interval;
                }

                if (this.pulseCooldown > 0) {
                        this.pulseCooldown--;
                }

                if (this.pulseCooldown <= 0) {
                        this.pulseCooldown = interval;
                        this.lastPulseTick = world.getTime();
                }
        }

        double getHorizontalRadius() {
                return BASE_HORIZONTAL_RADIUS + owner.getPitchforkHorizontalBonus();
        }

        double getVerticalRadius() {
                return BASE_VERTICAL_RADIUS + owner.getPitchforkVerticalBonus();
        }

        boolean isPulseActive(long worldTime) {
                return worldTime - this.lastPulseTick <= owner.getPitchforkPulseDurationTicks();
        }

        void initialize(long worldTime) {
                this.lastPulseTick = worldTime;
        }

        void saveNbt(NbtCompound nbt) {
                nbt.putInt("AuraCooldown", this.pulseCooldown);
                nbt.putLong("AuraLastPulse", this.lastPulseTick);
        }

        void loadNbt(NbtCompound nbt) {
                this.pulseCooldown = nbt.getInt("AuraCooldown");
                this.lastPulseTick = nbt.getLong("AuraLastPulse");
        }

        void onAuraModifiersChanged(long worldTime) {
                this.pulseCooldown = 0;
                if (worldTime >= 0L) {
                        this.lastPulseTick = worldTime;
                }
        }
}
