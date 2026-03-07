package net.jeremy.gardenkingmod.event;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class EndlessNightEventManager {
    private static final int EVENT_TRIGGER_TIME = 13000;

    private static final ServerBossBar BOSS_BAR = new ServerBossBar(
            Text.translatable("event.gardenkingmod.endless_night.bossbar.idle"),
            BossBar.Color.PURPLE,
            BossBar.Style.PROGRESS);

    static {
        BOSS_BAR.setPercent(0.0F);
        BOSS_BAR.setVisible(false);
    }

    private static MinecraftServer server;
    private static boolean active;
    private static TaskDefinition activeTask;
    private static int progress;
    private static long trackedNight = Long.MIN_VALUE;
    private static boolean triggerRolledForNight;
    private static int boostSpawnTickCounter;

    private EndlessNightEventManager() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(startedServer -> {
            server = startedServer;
            resetState();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(stoppedServer -> {
            server = null;
            resetState();
        });

        ServerTickEvents.END_SERVER_TICK.register(EndlessNightEventManager::onServerTick);

        ServerLivingEntityEvents.AFTER_DEATH.register(EndlessNightEventManager::onEntityKilled);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, minecraftServer) -> {
            BOSS_BAR.addPlayer(handler.player);
            syncToPlayer(handler.player);
        });

        ServerPlayConnectionEvents.DISCONNECT
                .register((handler, minecraftServer) -> BOSS_BAR.removePlayer(handler.player));
    }

    public static boolean forceStart() {
        if (active) {
            return false;
        }
        startEvent();
        return active;
    }

    public static boolean forceEnd() {
        if (!active && !BOSS_BAR.isVisible()) {
            return false;
        }

        if (!active) {
            resetState();
            syncToAllPlayers();
            return true;
        }

        completeEvent();
        return true;
    }

    public static boolean isActive() {
        return active;
    }

    public static int getProgress() {
        return progress;
    }

    public static int getRequiredCount() {
        return activeTask == null ? 0 : activeTask.requiredCount();
    }

    public static Text getTaskDescription() {
        return activeTask == null
                ? Text.translatable("event.gardenkingmod.endless_night.bossbar.idle")
                : activeTask.description();
    }

    private static void onServerTick(MinecraftServer minecraftServer) {
        ServerWorld overworld = minecraftServer.getWorld(World.OVERWORLD);
        if (overworld == null) {
            return;
        }

        EndlessNightConfig config = EndlessNightConfig.get();

        long timeOfDay = overworld.getTimeOfDay();
        long day = Math.floorDiv(timeOfDay, 24000L);
        long dayTime = Math.floorMod(timeOfDay, 24000L);

        if (day != trackedNight) {
            trackedNight = day;
            triggerRolledForNight = false;
        }

        if (!active && !triggerRolledForNight && dayTime >= EVENT_TRIGGER_TIME && dayTime < 15000) {
            triggerRolledForNight = true;
            if (ThreadLocalRandom.current().nextDouble() <= config.triggerChance()) {
                startEvent();
            }
        }

        if (active) {
            if (dayTime < 13000L || dayTime > 22000L) {
                overworld.setTimeOfDay(day * 24000L + config.nightLockTime());
            }
            boostSpawnTickCounter++;
            if (boostSpawnTickCounter >= config.spawnBoostIntervalTicks()) {
                boostSpawnTickCounter = 0;
                applySpawnBoost(overworld, config);
            }
        }
    }

    private static void applySpawnBoost(ServerWorld world, EndlessNightConfig config) {
        if (!active || activeTask == null || activeTask.targetType() == null) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (ThreadLocalRandom.current().nextDouble() > config.spawnBoostChancePerPlayer()) {
                continue;
            }

            BlockPos spawnPos = findSpawnPosNearPlayer(player);
            if (spawnPos == null) {
                continue;
            }

            var entity = activeTask.targetType().create(world);
            if (!(entity instanceof MobEntity mob)) {
                continue;
            }

            mob.refreshPositionAndAngles(spawnPos, world.random.nextFloat() * 360.0F, 0.0F);
            mob.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.EVENT, null, null);
            world.spawnEntityAndPassengers(mob);
        }
    }

    private static BlockPos findSpawnPosNearPlayer(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        for (int attempts = 0; attempts < 10; attempts++) {
            int xOffset = ThreadLocalRandom.current().nextInt(-18, 19);
            int zOffset = ThreadLocalRandom.current().nextInt(-18, 19);
            BlockPos groundPos = world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    player.getBlockPos().add(xOffset, 0, zOffset));
            BlockPos spawnPos = groundPos.up();

            if (world.isAir(spawnPos) && world.isAir(spawnPos.up())
                    && world.getBlockState(groundPos).blocksMovement()) {
                return spawnPos;
            }
        }
        return null;
    }

    private static void onEntityKilled(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource) {
        if (!active || activeTask == null || server == null) {
            return;
        }

        if (!entity.getType().equals(activeTask.targetType())) {
            return;
        }

        if (!(damageSource.getAttacker() instanceof ServerPlayerEntity)) {
            return;
        }

        progress = Math.min(activeTask.requiredCount(), progress + 1);
        refreshBossBar();
        syncToAllPlayers();

        if (progress >= activeTask.requiredCount()) {
            completeEvent();
        }
    }

    private static void startEvent() {
        if (server == null) {
            return;
        }

        List<TaskDefinition> taskPool = resolveTaskPool(EndlessNightConfig.get());
        if (taskPool.isEmpty()) {
            return;
        }

        active = true;
        progress = 0;
        boostSpawnTickCounter = 0;
        activeTask = taskPool.get(ThreadLocalRandom.current().nextInt(taskPool.size()));

        Text warning = Text.translatable("event.gardenkingmod.endless_night.approaching").formatted(Formatting.RED);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(warning, false);
        }

        refreshBossBar();
        syncToAllPlayers();
    }

    private static List<TaskDefinition> resolveTaskPool(EndlessNightConfig config) {
        return config.activeTaskPool().stream()
                .map(EndlessNightEventManager::toTaskDefinition)
                .filter(task -> task != null)
                .toList();
    }

    private static TaskDefinition toTaskDefinition(EndlessNightConfig.TaskEntry entry) {
        Identifier id = Identifier.tryParse(entry.mobId());
        if (id == null) {
            return null;
        }
        if (!Registries.ENTITY_TYPE.containsId(id)) {
            return null;
        }
        EntityType<?> type = Registries.ENTITY_TYPE.get(id);
        if (type == EntityType.PLAYER || !type.isSummonable()) {
            return null;
        }

        Text description = Text.translatable("event.gardenkingmod.endless_night.task.kill_generic",
                entry.killCount(), type.getName());
        return new TaskDefinition(type, entry.killCount(), description);
    }

    private static void completeEvent() {
        if (server == null) {
            resetState();
            return;
        }

        Text completionMessage = Text.translatable("event.gardenkingmod.endless_night.completed")
                .formatted(Formatting.DARK_PURPLE);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(completionMessage, false);
        }

        resetState();
        syncToAllPlayers();
    }

    private static void resetState() {
        active = false;
        progress = 0;
        boostSpawnTickCounter = 0;
        activeTask = null;
        BOSS_BAR.setName(Text.translatable("event.gardenkingmod.endless_night.bossbar.idle"));
        BOSS_BAR.setPercent(0.0F);
        BOSS_BAR.setVisible(false);
    }

    private static void refreshBossBar() {
        if (!active || activeTask == null) {
            BOSS_BAR.setVisible(false);
            return;
        }

        BOSS_BAR.setVisible(true);
        BOSS_BAR.setName(Text.translatable("event.gardenkingmod.endless_night.bossbar.progress",
                activeTask.description(), progress, activeTask.requiredCount()));
        BOSS_BAR.setPercent(Math.min(1.0F, (float) progress / (float) activeTask.requiredCount()));
    }

    private static void syncToAllPlayers() {
        if (server == null) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            syncToPlayer(player);
        }
    }

    private static void syncToPlayer(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(active);
        if (active && activeTask != null) {
            buf.writeText(activeTask.description());
            buf.writeVarInt(progress);
            buf.writeVarInt(activeTask.requiredCount());
        }

        ServerPlayNetworking.send(player, ModPackets.ENDLESS_NIGHT_SYNC, buf);
    }

    private record TaskDefinition(EntityType<?> targetType, int requiredCount, Text description) {
    }
}
