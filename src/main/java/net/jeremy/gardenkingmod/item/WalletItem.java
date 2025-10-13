package net.jeremy.gardenkingmod.item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.ModScoreboards;
import net.jeremy.gardenkingmod.currency.GardenCurrencyHolder;
import net.jeremy.gardenkingmod.screen.BankScreenHandler;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Wallet item that links to a specific player and exposes their Garden Dollar bank
 * balance to both client and server logic.
 */
public class WalletItem extends Item {
        private static final String OWNER_KEY = "Owner";
        private static final String OWNER_NAME_KEY = "OwnerName";
        private static final String BALANCE_KEY = "WalletBalance";

        public WalletItem(Settings settings) {
                super(settings.maxCount(1));
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
                ItemStack stack = user.getStackInHand(hand);

                if (world.isClient) {
                        return TypedActionResult.success(stack);
                }

                if (!(user instanceof ServerPlayerEntity serverPlayer)) {
                        return TypedActionResult.pass(stack);
                }

                boolean changed = ensureOwner(stack, serverPlayer);
                if (!isOwnedBy(stack, serverPlayer)) {
                        serverPlayer.sendMessage(Text.translatable("message.gardenkingmod.bank.not_owner"), true);
                        return TypedActionResult.fail(stack);
                }

                if (serverPlayer instanceof GardenCurrencyHolder holder) {
                        long balance = holder.gardenkingmod$getBankBalance();
                        if (updateBalanceSnapshot(stack, balance)) {
                                changed = true;
                        }
                }

                if (changed) {
                        markInventoryDirty(serverPlayer);
                }

                serverPlayer.openHandledScreen(new RemoteBankScreenFactory());
                return TypedActionResult.consume(stack);
        }

        @Override
        public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
                if (!world.isClient && entity instanceof ServerPlayerEntity serverPlayer) {
                        boolean changed = ensureOwner(stack, serverPlayer);

                        if (isOwnedBy(stack, serverPlayer) && serverPlayer instanceof GardenCurrencyHolder holder) {
                                long balance = holder.gardenkingmod$getBankBalance();
                                if (updateBalanceSnapshot(stack, balance)) {
                                        changed = true;
                                }
                        }

                        if (changed) {
                                markInventoryDirty(serverPlayer);
                        }
                }

                super.inventoryTick(stack, world, entity, slot, selected);
        }

        @Override
        public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
                super.appendTooltip(stack, world, tooltip, context);

                NbtCompound nbt = stack.getNbt();
                if (nbt == null) {
                        tooltip.add(Text.translatable("tooltip.gardenkingmod.wallet.unbound").formatted(Formatting.GRAY));
                        tooltip.add(Text.translatable("tooltip.gardenkingmod.wallet.balance", formatDollars(0))
                                        .formatted(Formatting.GOLD));
                        return;
                }

                if (nbt.contains(OWNER_NAME_KEY, NbtElement.STRING_TYPE)) {
                        String ownerName = nbt.getString(OWNER_NAME_KEY);
                        if (!ownerName.isEmpty()) {
                                tooltip.add(Text.translatable("tooltip.gardenkingmod.wallet.owner", ownerName)
                                                .formatted(Formatting.GRAY));
                        } else if (!nbt.containsUuid(OWNER_KEY)) {
                                tooltip.add(Text.translatable("tooltip.gardenkingmod.wallet.unbound")
                                                .formatted(Formatting.GRAY));
                        }
                } else if (!nbt.containsUuid(OWNER_KEY)) {
                        tooltip.add(Text.translatable("tooltip.gardenkingmod.wallet.unbound").formatted(Formatting.GRAY));
                }

                long balance = getBalanceSnapshot(stack);
                tooltip.add(Text.translatable("tooltip.gardenkingmod.wallet.balance", formatDollars(balance))
                                .formatted(Formatting.GOLD));
        }

        public static boolean hasUsableWallet(PlayerEntity player) {
                return !findUsableWalletStack(player).isEmpty();
        }

        public static long getAccessibleBankBalance(PlayerEntity player) {
                if (player == null) {
                        return 0L;
                }

                ItemStack wallet = findUsableWalletStack(player);
                if (wallet.isEmpty()) {
                        return 0L;
                }

                if (player instanceof GardenCurrencyHolder holder) {
                        return holder.gardenkingmod$getBankBalance();
                }

                return getBalanceSnapshot(wallet);
        }

        public static boolean depositToBank(ServerPlayerEntity player, long amount) {
                if (player == null || amount <= 0) {
                        return amount <= 0;
                }

                if (!(player instanceof GardenCurrencyHolder holder)) {
                        return false;
                }

                Optional<WalletContext> contextOptional = locateWallet(player);
                if (contextOptional.isEmpty()) {
                        return false;
                }

                WalletContext context = contextOptional.get();
                context.ensureOwner();
                long newBalance = holder.gardenkingmod$depositToBank(amount);
                ModScoreboards.syncPlayerBalances(player);
                context.updateSnapshot(newBalance);
                return true;
        }

        public static boolean withdrawFromBank(ServerPlayerEntity player, long amount) {
                if (player == null || amount <= 0) {
                        return true;
                }

                if (!(player instanceof GardenCurrencyHolder holder)) {
                        return false;
                }

                Optional<WalletContext> contextOptional = locateWallet(player);
                if (contextOptional.isEmpty()) {
                        return false;
                }

                long balance = holder.gardenkingmod$getBankBalance();
                if (balance < amount) {
                        return false;
                }

                WalletContext context = contextOptional.get();
                context.ensureOwner();
                long newBalance = holder.gardenkingmod$withdrawFromBank(amount);
                ModScoreboards.syncPlayerBalances(player);
                context.updateSnapshot(newBalance);
                return true;
        }

        public static int getCurrencyValuePerItem(Item item) {
                return item == ModItems.DOLLAR ? 1 : 0;
        }

        public static long getCurrencyValue(Item item, long count) {
                if (item != ModItems.DOLLAR || count <= 0) {
                        return 0L;
                }

                return count;
        }

        private static Optional<WalletContext> locateWallet(ServerPlayerEntity player) {
                if (player == null) {
                        return Optional.empty();
                }

                PlayerInventory inventory = player.getInventory();
                for (int slot = 0; slot < inventory.size(); slot++) {
                        ItemStack stack = inventory.getStack(slot);
                        if (stack.getItem() instanceof WalletItem && isOwnedBy(stack, player)) {
                                return Optional.of(new WalletContext(player, inventory, stack));
                        }
                }

                return Optional.empty();
        }

        private static ItemStack findUsableWalletStack(PlayerEntity player) {
                if (player == null) {
                        return ItemStack.EMPTY;
                }

                PlayerInventory inventory = player.getInventory();
                for (int slot = 0; slot < inventory.size(); slot++) {
                        ItemStack stack = inventory.getStack(slot);
                        if (stack.getItem() instanceof WalletItem && isOwnedBy(stack, player)) {
                                return stack;
                        }
                }

                return ItemStack.EMPTY;
        }

        private static boolean isOwnedBy(ItemStack stack, PlayerEntity player) {
                if (stack == null || player == null) {
                        return false;
                }

                NbtCompound nbt = stack.getNbt();
                if (nbt == null || !nbt.containsUuid(OWNER_KEY)) {
                        return false;
                }

                return player.getUuid().equals(nbt.getUuid(OWNER_KEY));
        }

        private boolean ensureOwner(ItemStack stack, ServerPlayerEntity player) {
                if (stack == null || player == null) {
                        return false;
                }

                NbtCompound nbt = stack.getOrCreateNbt();
                boolean changed = false;

                if (!nbt.containsUuid(OWNER_KEY)) {
                        nbt.putUuid(OWNER_KEY, player.getUuid());
                        changed = true;
                } else if (!player.getUuid().equals(nbt.getUuid(OWNER_KEY))) {
                        return changed;
                }

                String name = player.getName().getString();
                if (!name.equals(nbt.getString(OWNER_NAME_KEY))) {
                        nbt.putString(OWNER_NAME_KEY, name);
                        changed = true;
                }

                return changed;
        }

        private static boolean updateBalanceSnapshot(ItemStack stack, long balance) {
                if (stack == null) {
                        return false;
                }

                long sanitized = Math.max(0L, balance);
                NbtCompound nbt = stack.getOrCreateNbt();
                if (nbt.contains(BALANCE_KEY, NbtElement.NUMBER_TYPE) && nbt.getLong(BALANCE_KEY) == sanitized) {
                        return false;
                }

                nbt.putLong(BALANCE_KEY, sanitized);
                return true;
        }

        private static long getBalanceSnapshot(ItemStack stack) {
                if (stack == null) {
                        return 0L;
                }

                NbtCompound nbt = stack.getNbt();
                if (nbt != null && nbt.contains(BALANCE_KEY, NbtElement.NUMBER_TYPE)) {
                        return Math.max(0L, nbt.getLong(BALANCE_KEY));
                }

                return 0L;
        }

        private static String formatDollars(long amount) {
                NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
                format.setGroupingUsed(true);
                return format.format(Math.max(0L, amount));
        }

        private static void markInventoryDirty(ServerPlayerEntity player) {
                if (player == null) {
                        return;
                }

                player.getInventory().markDirty();
                if (player.playerScreenHandler != null) {
                        player.playerScreenHandler.sendContentUpdates();
                }
        }

        private record WalletContext(ServerPlayerEntity player, PlayerInventory inventory, ItemStack stack) {
                void ensureOwner() {
                        if (stack.getItem() instanceof WalletItem wallet) {
                                if (wallet.ensureOwner(stack, player)) {
                                        markDirty();
                                }
                        }
                }

                void updateSnapshot(long balance) {
                        if (updateBalanceSnapshot(stack, balance)) {
                                markDirty();
                        }
                }

                private void markDirty() {
                        inventory.markDirty();
                        if (player.playerScreenHandler != null) {
                                player.playerScreenHandler.sendContentUpdates();
                        }
                }
        }

        private static class RemoteBankScreenFactory implements ExtendedScreenHandlerFactory {
                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                        buf.writeBlockPos(BankScreenHandler.REMOTE_BANK_POS);
                }

                @Override
                public Text getDisplayName() {
                        return Text.translatable("container.gardenkingmod.bank");
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                        BankScreenHandler handler = BankScreenHandler.createRemote(syncId, playerInventory);
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                                handler.sendBalanceUpdate(serverPlayer);
                        }
                        return handler;
                }
        }
}
