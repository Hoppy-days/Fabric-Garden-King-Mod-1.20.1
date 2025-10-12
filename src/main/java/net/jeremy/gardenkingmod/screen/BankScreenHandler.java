package net.jeremy.gardenkingmod.screen;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.ModScoreboards;
import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.block.entity.BankBlockEntity;
import net.jeremy.gardenkingmod.currency.GardenCurrencyHolder;
import net.jeremy.gardenkingmod.item.WalletItem;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class BankScreenHandler extends ScreenHandler {
    public static final int GUI_WIDTH = 176;
    public static final int GUI_HEIGHT = 222;
    public static final int SLOT_SIZE = 18;
    public static final int DEPOSIT_SLOT_X = (GUI_WIDTH - SLOT_SIZE) / 2;
    public static final int DEPOSIT_SLOT_Y = 70;
    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_INVENTORY_Y = 128;
    public static final int PLAYER_INVENTORY_TITLE_Y = PLAYER_INVENTORY_Y - 10;
    public static final long MAX_WITHDRAW_AMOUNT = 64L * 36L;
    public static final int BUTTON_DEPOSIT = 0;

    private static final int DEPOSIT_SLOT_INDEX = 0;
    private static final int PLAYER_INVENTORY_START = 1;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final BankBlockEntity blockEntity;
    private final Inventory depositInventory = new SimpleInventory(1) {
        @Override
        public void markDirty() {
            super.markDirty();
            BankScreenHandler.this.sendContentUpdates();
        }
    };
    private final BlockPos bankPos;
    private int totalDollars;

    public BankScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, getBlockEntity(playerInventory, buf.readBlockPos()));
    }

    public BankScreenHandler(int syncId, PlayerInventory playerInventory, BankBlockEntity blockEntity) {
        super(ModScreenHandlers.BANK_SCREEN_HANDLER, syncId);
        this.blockEntity = blockEntity;
        this.bankPos = blockEntity != null ? blockEntity.getPos() : BlockPos.ORIGIN;

        this.addSlot(new Slot(this.depositInventory, 0, DEPOSIT_SLOT_X, DEPOSIT_SLOT_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return WalletItem.getCurrencyValuePerItem(stack.getItem()) > 0;
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
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
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = slot.getStack();
        ItemStack original = slotStack.copy();

        if (index == DEPOSIT_SLOT_INDEX) {
            if (!insertItem(slotStack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int valuePerItem = WalletItem.getCurrencyValuePerItem(slotStack.getItem());
            if (valuePerItem > 0) {
                if (!insertItem(slotStack, DEPOSIT_SLOT_INDEX, DEPOSIT_SLOT_INDEX + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END) {
                if (!insertItem(slotStack, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= HOTBAR_START && index < HOTBAR_END) {
                if (!insertItem(slotStack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return original;
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

    public boolean hasDepositItem() {
        Slot slot = this.slots.get(DEPOSIT_SLOT_INDEX);
        return slot != null && slot.hasStack();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == BUTTON_DEPOSIT && player instanceof ServerPlayerEntity serverPlayer) {
            if (deposit(serverPlayer)) {
                sendBalanceUpdate(serverPlayer);
            }
            return true;
        }

        return super.onButtonClick(player, id);
    }

    public void handleWithdrawRequest(ServerPlayerEntity player, long requestedAmount) {
        long amount = Math.min(Math.max(requestedAmount, 0L), MAX_WITHDRAW_AMOUNT);
        if (amount <= 0) {
            return;
        }

        long balance = getBankBalance(player);
        if (balance <= 0) {
            return;
        }

        if (amount > balance) {
            amount = balance;
        }

        boolean success = WalletItem.withdrawFromBank(player, amount);
        if (!success && player instanceof GardenCurrencyHolder holder) {
            holder.gardenkingmod$withdrawFromBank(amount);
            ModScoreboards.syncPlayerBalances(player);
            success = true;
        }

        if (!success) {
            return;
        }

        giveDollarsToPlayer(player, amount);
        sendBalanceUpdate(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        if (player instanceof ServerPlayerEntity serverPlayer) {
            ItemStack remaining = this.depositInventory.removeStack(0);
            if (!remaining.isEmpty()) {
                if (!serverPlayer.getInventory().insertStack(remaining)) {
                    serverPlayer.dropItem(remaining, false);
                }
            }
        }
    }

    private boolean deposit(ServerPlayerEntity player) {
        Slot slot = this.slots.get(DEPOSIT_SLOT_INDEX);
        if (slot == null) {
            return false;
        }

        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) {
            return false;
        }

        long value = WalletItem.getCurrencyValue(stack.getItem(), stack.getCount());
        if (value <= 0) {
            return false;
        }

        boolean deposited = WalletItem.depositToBank(player, value);
        if (!deposited && player instanceof GardenCurrencyHolder holder) {
            holder.gardenkingmod$depositToBank(value);
            ModScoreboards.syncPlayerBalances(player);
            deposited = true;
        }

        if (!deposited) {
            return false;
        }

        slot.takeStack(stack.getCount());
        slot.markDirty();
        this.depositInventory.markDirty();
        return true;
    }

    private void giveDollarsToPlayer(ServerPlayerEntity player, long amount) {
        int maxStackSize = ModItems.DOLLAR.getMaxCount();
        long remaining = amount;

        while (remaining > 0) {
            int stackSize = (int) Math.min(maxStackSize, remaining);
            if (stackSize <= 0) {
                break;
            }

            ItemStack payout = new ItemStack(ModItems.DOLLAR, stackSize);
            if (!player.getInventory().insertStack(payout)) {
                player.dropItem(payout, false);
            }
            remaining -= stackSize;
        }
    }

    private long getBankBalance(ServerPlayerEntity player) {
        if (player instanceof GardenCurrencyHolder holder) {
            return holder.gardenkingmod$getBankBalance();
        }

        return ModScoreboards.getBankBalance(player);
    }

    private void sendBalanceUpdate(ServerPlayerEntity player) {
        if (blockEntity != null) {
            blockEntity.sendBalanceUpdate(player);
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(this.bankPos);
        long balance = getBankBalance(player);
        int clamped = balance > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(0L, balance);
        buf.writeVarInt(clamped);
        ServerPlayNetworking.send(player, ModPackets.BANK_BALANCE_PACKET, buf);
    }

    private void addPlayerInventory(PlayerInventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9 + 9;
                int x = PLAYER_INVENTORY_X + column * SLOT_SIZE;
                int y = PLAYER_INVENTORY_Y + row * SLOT_SIZE;
                this.addSlot(new Slot(inventory, index, x, y));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory inventory) {
        for (int slot = 0; slot < 9; slot++) {
            int x = PLAYER_INVENTORY_X + slot * SLOT_SIZE;
            int y = PLAYER_INVENTORY_Y + 58;
            this.addSlot(new Slot(inventory, slot, x, y));
        }
    }
}
