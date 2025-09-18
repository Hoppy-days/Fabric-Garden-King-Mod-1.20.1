package net.jeremy.gardenkingmod.network;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.util.Identifier;

public final class ModPackets {
    private ModPackets() {
    }

    public static final Identifier MARKET_SALE_RESULT_PACKET =
            new Identifier(GardenKingMod.MOD_ID, "market_sale_result");
}
