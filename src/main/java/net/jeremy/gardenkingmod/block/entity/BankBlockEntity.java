package net.jeremy.gardenkingmod.block.entity;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.ModScoreboards;
import net.jeremy.gardenkingmod.currency.GardenCurrencyHolder;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.jeremy.gardenkingmod.screen.BankScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class BankBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    private UUID ownerUuid;

    public BankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BANK_BLOCK_ENTITY, pos, state);
    }

    public void setOwner(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        markDirty();
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public boolean canAccess(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        if (world == null || world.getBlockEntity(pos) != this) {
            return false;
        }
        if (ownerUuid == null) {
            return true;
        }
        return ownerUuid.equals(player.getUuid());
    }

    public boolean canPlayerUse(PlayerEntity player) {
        if (!canAccess(player)) {
            return false;
        }
        return player.squaredDistanceTo((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
                (double) pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (ownerUuid != null) {
            nbt.putUuid("Owner", ownerUuid);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        ownerUuid = nbt.containsUuid("Owner") ? nbt.getUuid("Owner") : null;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(getPos());
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.gardenkingmod.bank");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            MinecraftServer server = serverPlayer.getServer();
            if (server != null) {
                BlockPos bankPos = getPos();
                server.execute(() -> {
                    if (serverPlayer.currentScreenHandler instanceof BankScreenHandler handler
                            && handler.getBankPos().equals(bankPos)) {
                        sendBalanceUpdate(serverPlayer);
                    }
                });
            }
        }
        return new BankScreenHandler(syncId, playerInventory, this);
    }

    public void sendBalanceUpdate(ServerPlayerEntity player) {
        if (player == null) {
            return;
        }
        int totalDollars = getBankBalance(player);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(getPos());
        buf.writeVarInt(totalDollars);
        ServerPlayNetworking.send(player, ModPackets.BANK_BALANCE_PACKET, buf);
    }

    private static int getBankBalance(ServerPlayerEntity player) {
        long balance;
        if (player instanceof GardenCurrencyHolder holder) {
            balance = holder.gardenkingmod$getBankBalance();
        } else {
            balance = ModScoreboards.getBankBalance(player);
        }

        if (balance <= 0L) {
            return 0;
        }

        return balance > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) balance;
    }
}
