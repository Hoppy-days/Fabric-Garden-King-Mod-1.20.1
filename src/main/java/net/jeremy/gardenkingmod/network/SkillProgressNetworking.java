package net.jeremy.gardenkingmod.network;

import java.util.LinkedHashMap;
import java.util.Map;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeremy.gardenkingmod.skill.SkillProgressHolder;
import net.jeremy.gardenkingmod.skill.SkillProgressManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class SkillProgressNetworking {
        private SkillProgressNetworking() {
        }

        public static void sync(ServerPlayerEntity player) {
                if (player == null || !(player instanceof SkillProgressHolder skillHolder)) {
                        return;
                }

                long totalExperience = Math.max(0L, skillHolder.gardenkingmod$getSkillExperience());
                int level = Math.max(0, skillHolder.gardenkingmod$getSkillLevel());
                long currentLevelFloor = Math.max(0L, SkillProgressManager.getExperienceForLevel(level));
                long nextLevelRequirement = Math.max(currentLevelFloor,
                                SkillProgressManager.getExperienceForLevel(level + 1));
                long progressIntoLevel = Math.max(0L, totalExperience - currentLevelFloor);
                long experienceToNextLevel = Math.max(0L, nextLevelRequirement - currentLevelFloor);
                int unspentPoints = Math.max(0, skillHolder.gardenkingmod$getUnspentSkillPoints());

                Map<Identifier, Integer> allocations = new LinkedHashMap<>();
                allocations.put(SkillProgressManager.CHEF_SKILL,
                                Math.max(0, skillHolder.gardenkingmod$getChefMasteryLevel()));

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeVarLong(totalExperience);
                buf.writeVarInt(level);
                buf.writeVarLong(progressIntoLevel);
                buf.writeVarLong(experienceToNextLevel);
                buf.writeVarInt(unspentPoints);
                buf.writeVarInt(allocations.size());
                allocations.forEach((id, allocation) -> {
                        buf.writeIdentifier(id);
                        buf.writeVarInt(Math.max(0, allocation));
                });
                ServerPlayNetworking.send(player, ModPackets.SKILL_PROGRESS_SYNC, buf);
        }
}
