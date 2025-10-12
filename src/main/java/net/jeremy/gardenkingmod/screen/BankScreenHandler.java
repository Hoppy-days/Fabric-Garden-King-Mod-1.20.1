package net.jeremy.gardenkingmod.screen;

import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.block.entity.BankBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class BankScreenHandler extends ScreenHandler {
    private final BankBlockEntity blockEntity;
    private final BlockPos bankPos;
    private int totalDollars;

    public BankScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, getBlockEntity(playerInventory, buf.readBlockPos()));
    }

    public BankScreenHandler(int syncId, PlayerInventory playerInventory, BankBlockEntity blockEntity) {
        super(ModScreenHandlers.BANK_SCREEN_HANDLER, syncId);
        this.blockEntity = blockEntity;
        this.bankPos = blockEntity != null ? blockEntity.getPos() : BlockPos.ORIGIN;
    }

    private static BankBlockEntity getBlockEntity(PlayerInventory playerInventory, BlockPos pos) {
        if (playerInventory.player.getWorld().getBlockEntity(pos) instanceof BankBlockEntity bankBlockEntity) {
            return bankBlockEntity;
        }
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (blockEntity != null) {
            return blockEntity.canPlayerUse(player);
        }
        return player.squaredDistanceTo((double) bankPos.getX() + 0.5D, (double) bankPos.getY() + 0.5D,
                (double) bankPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    public void updateBalance(int totalDollars) {
        this.totalDollars = totalDollars;
    }

    public BlockPos getBankPos() {
        return bankPos;
    }

    public int getTotalDollars() {
        return totalDollars;
    }
}
