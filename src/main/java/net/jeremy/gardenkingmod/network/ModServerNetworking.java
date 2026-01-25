package net.jeremy.gardenkingmod.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerPlayerEvents;
import net.jeremy.gardenkingmod.screen.BankScreenHandler;
import net.jeremy.gardenkingmod.skill.SkillProgressHolder;
import net.jeremy.gardenkingmod.skill.SkillProgressManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class ModServerNetworking {
    private ModServerNetworking() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.BANK_WITHDRAW_REQUEST_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    int syncId = buf.readVarInt();
                    long amount = buf.readVarLong();

                    server.execute(() -> {
                        if (player.currentScreenHandler instanceof BankScreenHandler bankHandler
                                && bankHandler.syncId == syncId) {
                            bankHandler.handleWithdrawRequest(player, amount);
                        }
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.SKILL_SPEND_REQUEST,
                (server, player, handler, buf, responseSender) -> {
                    Identifier skillId = buf.readIdentifier();
                    int pointsToSpend = buf.readVarInt();

                    server.execute(() -> {
                        if (!(player instanceof SkillProgressHolder)) {
                            return;
                        }
                        SkillProgressHolder skillHolder = (SkillProgressHolder) player;

                        if (pointsToSpend <= 0) {
                            SkillProgressNetworking.sync(player);
                            return;
                        }

                        if (!SkillProgressManager.getSkillDefinitions().containsKey(skillId)) {
                            SkillProgressNetworking.sync(player);
                            return;
                        }

                        if (!skillHolder.gardenkingmod$spendSkillPoints(pointsToSpend)) {
                            SkillProgressNetworking.sync(player);
                            return;
                        }

                        boolean applied = applySkillUpgrade(skillHolder, skillId, pointsToSpend);
                        if (!applied) {
                            refundSkillPoints(skillHolder, pointsToSpend);
                        }

                        SkillProgressNetworking.sync(player);
                    });
                });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server1) -> {
            SkillProgressNetworking.sync(handler.player);
            // Glow on join; respawn glow is handled in ServerPlayerEntityMixin.copyFrom.
            handler.player.setGlowing(true);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            applyPlayerGlow(newPlayer);
        });
    }

    private static void applyPlayerGlow(ServerPlayerEntity player) {
        if (player == null) {
            return;
        }
        player.setGlowing(true);
    }

    private static void applyPlayerGlow(ServerPlayerEntity player) {
        if (player == null) {
            return;
        }
        player.setGlowing(true);
    }

    private static boolean applySkillUpgrade(SkillProgressHolder skillHolder, Identifier skillId, int pointsToSpend) {
        int maxLevel = Math.max(0, SkillProgressManager.getMaxDefinedLevel());
        if (SkillProgressManager.CHEF_SKILL.equals(skillId)) {
            return applyBoundedAllocation(pointsToSpend, maxLevel, skillHolder.gardenkingmod$getChefMasteryLevel(),
                    skillHolder::gardenkingmod$setChefMasteryLevel, skillHolder);
        }
        if (SkillProgressManager.ENCHANTER_SKILL.equals(skillId)) {
            return applyBoundedAllocation(pointsToSpend, maxLevel, skillHolder.gardenkingmod$getEnchanterLevel(),
                    skillHolder::gardenkingmod$setEnchanterLevel, skillHolder);
        }
        return false;
    }

    private static boolean applyBoundedAllocation(int pointsToSpend, int maxLevel, int currentLevel,
            java.util.function.IntConsumer setter, SkillProgressHolder skillHolder) {
        int clampedCurrent = MathHelper.clamp(currentLevel, 0, maxLevel);
        int targetLevel = MathHelper.clamp(clampedCurrent + pointsToSpend, 0, maxLevel);
        int applied = targetLevel - clampedCurrent;
        if (applied <= 0) {
            return false;
        }
        setter.accept(targetLevel);
        int refund = pointsToSpend - applied;
        if (refund > 0) {
            refundSkillPoints(skillHolder, refund);
        }
        return true;
    }

    private static void refundSkillPoints(SkillProgressHolder skillHolder, int amount) {
        int refund = MathHelper.clamp(amount, 0, Integer.MAX_VALUE);
        if (refund <= 0) {
            return;
        }
        int updated = MathHelper.clamp(skillHolder.gardenkingmod$getUnspentSkillPoints() + refund, 0, Integer.MAX_VALUE);
        skillHolder.gardenkingmod$setUnspentSkillPoints(updated);
    }
}
