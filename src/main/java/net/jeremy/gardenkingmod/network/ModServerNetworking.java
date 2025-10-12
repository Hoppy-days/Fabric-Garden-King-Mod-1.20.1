package net.jeremy.gardenkingmod.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeremy.gardenkingmod.screen.BankScreenHandler;

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
    }
}
