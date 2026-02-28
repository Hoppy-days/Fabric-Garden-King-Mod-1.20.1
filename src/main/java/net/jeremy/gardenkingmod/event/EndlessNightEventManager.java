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
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public final class EndlessNightEventManager {
    private static final int EVENT_TRIGGER_TIME = 13000;
    private static final double EVENT_TRIGGER_CHANCE = 0.18D;
    private static final int NIGHT_LOCK_TIME = 18000;

    private static final List<TaskDefinition> TASK_POOL = List.of(
            new TaskDefinition(EntityType.ZOMBIE, 50,
                    Text.translatable("event.gardenkingmod.endless_night.task.kill_zombies", 50)),
            new TaskDefinition(EntityType.SKELETON, 35,
                    Text.translatable("event.gardenkingmod.endless_night.task.kill_skeletons", 35)),
            new TaskDefinition(EntityType.SPIDER, 40,
                    Text.translatable("event.gardenkingmod.endless_night.task.kill_spiders", 40)));

    private static final ServerBossBar BOSS_BAR = new ServerBossBar(
            Text.translatable("event.gardenkingmod.endless_night.bossbar.idle"),
            BossBar.Color.PURPLE,
            BossBar.Style.PROGRESS);

    private static MinecraftServer server;
    private static boolean active;
    private static TaskDefinition activeTask;
    private static int progress;
    private static long trackedNight = Long.MIN_VALUE;
    private static boolean triggerRolledForNight;

    private EndlessNightEventManager() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(startedServer -> server = startedServer);
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

        ServerPlayConnectionEvents.DISCONNECT.register((handler, minecraftServer) -> BOSS_BAR.removePlayer(handler.player));
    }

    private static void onServerTick(MinecraftServer minecraftServer) {
        ServerWorld overworld = minecraftServer.getWorld(World.OVERWORLD);
        if (overworld == null) {
            return;
        }

        long timeOfDay = overworld.getTimeOfDay();
        long day = Math.floorDiv(timeOfDay, 24000L);
        long dayTime = Math.floorMod(timeOfDay, 24000L);

        if (day != trackedNight) {
            trackedNight = day;
            triggerRolledForNight = false;
        }

        if (!active && !triggerRolledForNight && dayTime >= EVENT_TRIGGER_TIME && dayTime < 15000) {
            triggerRolledForNight = true;
            if (ThreadLocalRandom.current().nextDouble() <= EVENT_TRIGGER_CHANCE) {
                startEvent();
            }
        }

        if (active && dayTime < 12000) {
            overworld.setTimeOfDay(day * 24000L + NIGHT_LOCK_TIME);
        }
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
        if (server == null || TASK_POOL.isEmpty()) {
            return;
        }

        active = true;
        progress = 0;
        activeTask = TASK_POOL.get(ThreadLocalRandom.current().nextInt(TASK_POOL.size()));

        Text warning = Text.translatable("event.gardenkingmod.endless_night.approaching").formatted(Formatting.RED);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(warning, false);
        }

        refreshBossBar();
        syncToAllPlayers();
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
