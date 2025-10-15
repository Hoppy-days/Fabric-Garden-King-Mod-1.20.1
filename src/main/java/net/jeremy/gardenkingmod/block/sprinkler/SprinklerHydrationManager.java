package net.jeremy.gardenkingmod.block.sprinkler;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class SprinklerHydrationManager {
        private static final WeakHashMap<ServerWorld, Set<SprinklerBlockEntity>> ACTIVE = new WeakHashMap<>();

        private SprinklerHydrationManager() {
        }

        public static void register(ServerWorld world, SprinklerBlockEntity entity) {
                ACTIVE.compute(world, (ignored, entities) -> {
                        Set<SprinklerBlockEntity> result = entities;
                        if (result == null) {
                                result = Collections.newSetFromMap(new WeakHashMap<>());
                        }
                        result.add(entity);
                        return result;
                });
        }

        public static void unregister(SprinklerBlockEntity entity) {
                if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
                        return;
                }
                Set<SprinklerBlockEntity> entities = ACTIVE.get(serverWorld);
                if (entities != null) {
                        entities.remove(entity);
                        if (entities.isEmpty()) {
                                ACTIVE.remove(serverWorld);
                        }
                }
        }

        public static boolean keepsFarmlandWet(ServerWorld world, BlockPos farmland) {
                Set<SprinklerBlockEntity> entities = ACTIVE.get(world);
                if (entities == null || entities.isEmpty()) {
                        return false;
                }
                for (SprinklerBlockEntity entity : entities) {
                        if (!entity.isRemoved() && entity.isWithinRange(farmland)) {
                                return true;
                        }
                }
                return false;
        }
}
