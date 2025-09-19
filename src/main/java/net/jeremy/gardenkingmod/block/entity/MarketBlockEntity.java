package net.jeremy.gardenkingmod.block.entity;

import java.util.Optional;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.ModScoreboards;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.jeremy.gardenkingmod.crop.CropTier;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.screen.MarketScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MarketBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, Inventory {
        public static final int INVENTORY_SIZE = 45;

        private DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

        public MarketBlockEntity(BlockPos pos, BlockState state) {
                super(ModBlockEntities.MARKET_BLOCK_ENTITY, pos, state);
        }

        public static boolean isSellable(ItemStack stack) {
                if (stack.isEmpty()) {
                        return false;
                }

                Identifier identifier = Registries.ITEM.getId(stack.getItem());
                if (identifier == null) {
                        return false;
                }

                String namespace = identifier.getNamespace();
                return "croptopia".equals(namespace) || "minecraft".equals(namespace);
        }

        @Override
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                buf.writeBlockPos(getPos());
        }

        @Override
        public Text getDisplayName() {
                return Text.translatable("container.gardenkingmod.market");
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new MarketScreenHandler(syncId, playerInventory, this);
        }

        @Override
        public int size() {
                return items.size();
        }

        @Override
        public boolean isEmpty() {
                for (ItemStack stack : items) {
                        if (!stack.isEmpty()) {
                                return false;
                        }
                }
                return true;
        }

        @Override
        public ItemStack getStack(int slot) {
                return items.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
                ItemStack stack = Inventories.splitStack(items, slot, amount);
                if (!stack.isEmpty()) {
                        markDirty();
                }
                return stack;
        }

        @Override
        public ItemStack removeStack(int slot) {
                ItemStack stack = Inventories.removeStack(items, slot);
                if (!stack.isEmpty()) {
                        markDirty();
                }
                return stack;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
                items.set(slot, stack);
                if (stack.getCount() > getMaxCountPerStack()) {
                        stack.setCount(getMaxCountPerStack());
                }
                markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
                if (world == null || world.getBlockEntity(pos) != this) {
                        return false;
                }

                return player.squaredDistanceTo((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
                                (double) pos.getZ() + 0.5D) <= 64.0D;
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
                return stack.isEmpty() || isSellable(stack);
        }

        @Override
        public void clear() {
                for (int slot = 0; slot < items.size(); slot++) {
                        items.set(slot, ItemStack.EMPTY);
                }
        }

        @Override
        public void onClose(PlayerEntity player) {
                Inventory.super.onClose(player);
                if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                        return;
                }

                for (int slot = 0; slot < size(); slot++) {
                        ItemStack remainingStack = removeStack(slot);
                        if (!remainingStack.isEmpty()) {
                                insertOrDrop(serverPlayer, remainingStack);
                        }
                }
        }

        @Override
        public void markDirty() {
                super.markDirty();
                World world = getWorld();
                if (world != null) {
                        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
                        world.updateComparators(pos, getCachedState().getBlock());
                }
        }

        @Override
        protected void writeNbt(NbtCompound nbt) {
                super.writeNbt(nbt);
                Inventories.writeNbt(nbt, items);
        }

        @Override
        public void readNbt(NbtCompound nbt) {
                super.readNbt(nbt);
                items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
                Inventories.readNbt(nbt, items);
        }

        public boolean sell(ServerPlayerEntity player) {
                if (isEmpty()) {
                        player.sendMessage(Text.translatable("message.gardenkingmod.market.empty"), true);
                        return false;
                }

                int inventorySize = items.size();
                int totalItemsSold = 0;
                int totalPayout = 0;
                boolean hasSellableStack = false;

                for (int slot = 0; slot < inventorySize; slot++) {
                        ItemStack stack = items.get(slot);
                        if (stack.isEmpty()) {
                                continue;
                        }

                        if (!isSellable(stack)) {
                                continue;
                        }

                        Optional<CropTier> optionalTier = CropTierRegistry.get(stack.getItem());
                        if (optionalTier.isEmpty()) {
                                continue;
                        }

                        int coinsPerStack = getCoinsPerStack(optionalTier.get());
                        if (coinsPerStack <= 0) {
                                continue;
                        }

                        int maxStackSize = stack.getMaxCount();
                        if (maxStackSize <= 0) {
                                continue;
                        }

                        hasSellableStack = true;

                        int fullStacks = stack.getCount() / maxStackSize;
                        if (fullStacks > 0) {
                                totalItemsSold += fullStacks * maxStackSize;
                                totalPayout += fullStacks * coinsPerStack;
                        }
                }

                if (!hasSellableStack) {
                        player.sendMessage(Text.translatable("message.gardenkingmod.market.invalid"), true);
                        return false;
                }

                boolean changed = false;

                for (int slot = 0; slot < inventorySize; slot++) {
                        ItemStack stack = items.get(slot);
                        if (stack.isEmpty()) {
                                continue;
                        }

                        if (!isSellable(stack)) {
                                continue;
                        }

                        Optional<CropTier> optionalTier = CropTierRegistry.get(stack.getItem());
                        if (optionalTier.isEmpty()) {
                                continue;
                        }

                        int coinsPerStack = getCoinsPerStack(optionalTier.get());
                        if (coinsPerStack <= 0) {
                                continue;
                        }

                        int maxStackSize = stack.getMaxCount();
                        if (maxStackSize <= 0) {
                                continue;
                        }

                        int fullStacks = stack.getCount() / maxStackSize;
                        int remainder = stack.getCount() - fullStacks * maxStackSize;

                        if (remainder > 0) {
                                ItemStack remainderStack = stack.copy();
                                remainderStack.setCount(remainder);
                                insertOrDrop(player, remainderStack);
                        }

                        items.set(slot, ItemStack.EMPTY);
                        changed = true;
                }

                if (!changed) {
                        player.sendMessage(Text.translatable("message.gardenkingmod.market.empty"), true);
                        return false;
                }

                markDirty();

                if (totalPayout > 0) {
                        ItemStack currencyStack = new ItemStack(ModItems.GARDEN_COIN, totalPayout);
                        boolean fullyInserted = player.getInventory().insertStack(currencyStack);
                        if (!fullyInserted && !currencyStack.isEmpty()) {
                                player.dropItem(currencyStack, false);
                        }
                }

                int lifetimeTotal = ModScoreboards.addCurrency(player, totalPayout);

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(pos);
                buf.writeVarInt(totalItemsSold);
                buf.writeVarInt(totalPayout);
                buf.writeVarInt(lifetimeTotal);
                ServerPlayNetworking.send(player, ModPackets.MARKET_SALE_RESULT_PACKET, buf);

                Text message = lifetimeTotal >= 0
                                ? Text.translatable("message.gardenkingmod.market.sold.lifetime", totalItemsSold,
                                                totalPayout, lifetimeTotal)
                                : Text.translatable("message.gardenkingmod.market.sold", totalItemsSold, totalPayout);
                player.sendMessage(message, true);

                World world = getWorld();
                if (world != null) {
                        world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.75f,
                                        1.0f);
                }

                return true;
        }

        private static int getCoinsPerStack(CropTier tier) {
                String path = tier.id().getPath();
                return switch (path) {
                case "crop_tiers/tier_1" -> 1;
                case "crop_tiers/tier_2" -> 2;
                case "crop_tiers/tier_3" -> 3;
                case "crop_tiers/tier_4" -> 4;
                case "crop_tiers/tier_5" -> 5;
                default -> 0;
                };
        }

        private static void insertOrDrop(PlayerEntity player, ItemStack stack) {
                if (stack.isEmpty()) {
                        return;
                }

                boolean fullyInserted = player.getInventory().insertStack(stack);
                if (!fullyInserted && !stack.isEmpty()) {
                        player.dropItem(stack, false);
                }
        }
}
