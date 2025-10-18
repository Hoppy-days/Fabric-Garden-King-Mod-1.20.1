package net.jeremy.gardenkingmod.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeremy.gardenkingmod.screen.BankScreenHandler;
import net.jeremy.gardenkingmod.skill.SkillProgressHolder;
import net.jeremy.gardenkingmod.skill.SkillProgressManager;
import net.minecraft.server.network.ServerPlayerEntity;
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
                        if (!(player instanceof ServerPlayerEntity)) {
                            return;
                        }
                        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                        if (!(serverPlayer instanceof SkillProgressHolder)) {
                            return;
                        }
                        SkillProgressHolder skillHolder = (SkillProgressHolder) serverPlayer;

                        if (pointsToSpend <= 0) {
                            SkillProgressNetworking.sync(serverPlayer);
                            return;
                        }

                        if (!SkillProgressManager.getSkillDefinitions().containsKey(skillId)) {
                            SkillProgressNetworking.sync(serverPlayer);
                            return;
                        }

                        if (!skillHolder.gardenkingmod$spendSkillPoints(pointsToSpend)) {
                            SkillProgressNetworking.sync(serverPlayer);
                            return;
                        }

                        if (SkillProgressManager.CHEF_SKILL.equals(skillId)) {
                            int updatedLevel = MathHelper.clamp(
                                    skillHolder.gardenkingmod$getChefMasteryLevel() + pointsToSpend, 0, Integer.MAX_VALUE);
                            skillHolder.gardenkingmod$setChefMasteryLevel(updatedLevel);
                        } else {
                            int refunded = MathHelper.clamp(pointsToSpend, 0, Integer.MAX_VALUE);
                            skillHolder.gardenkingmod$setUnspentSkillPoints(MathHelper.clamp(
                                    skillHolder.gardenkingmod$getUnspentSkillPoints() + refunded, 0, Integer.MAX_VALUE));
                        }

                        SkillProgressNetworking.sync(serverPlayer);
                    });
                });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server1) -> {
            SkillProgressNetworking.sync(handler.player);
        });
    }
}
