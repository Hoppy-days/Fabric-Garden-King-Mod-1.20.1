package net.jeremy.gardenkingmod.network;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.util.Identifier;

public final class ModPackets {
    private ModPackets() {
    }

    public static final Identifier MARKET_SALE_RESULT_PACKET =
            new Identifier(GardenKingMod.MOD_ID, "market_sale_result");

    public static final Identifier BANK_BALANCE_PACKET =
            new Identifier(GardenKingMod.MOD_ID, "bank_balance");

    public static final Identifier BANK_WITHDRAW_REQUEST_PACKET =
            new Identifier(GardenKingMod.MOD_ID, "bank_withdraw_request");

    public static final Identifier SKILL_PROGRESS_SYNC =
            new Identifier(GardenKingMod.MOD_ID, "skill_progress_sync");

    public static final Identifier SKILL_SPEND_REQUEST =
            new Identifier(GardenKingMod.MOD_ID, "skill_spend_request");

    public static final Identifier MARKET_SELL_VALUES_SYNC =
            new Identifier(GardenKingMod.MOD_ID, "market_sell_values_sync");
}
