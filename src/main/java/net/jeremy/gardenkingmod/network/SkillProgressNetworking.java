package net.jeremy.gardenkingmod.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeremy.gardenkingmod.skill.SkillProgressHolder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SkillProgressNetworking {
        private SkillProgressNetworking() {
        }

        public static void sync(ServerPlayerEntity player) {
                if (player == null || !(player instanceof SkillProgressHolder skillHolder)) {
                        return;
                }

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeVarLong(skillHolder.gardenkingmod$getSkillExperience());
                buf.writeVarInt(skillHolder.gardenkingmod$getSkillLevel());
                buf.writeVarInt(skillHolder.gardenkingmod$getUnspentSkillPoints());
                buf.writeVarInt(skillHolder.gardenkingmod$getChefMasteryLevel());
                ServerPlayNetworking.send(player, ModPackets.SKILL_PROGRESS_SYNC_PACKET, buf);
        }
}
