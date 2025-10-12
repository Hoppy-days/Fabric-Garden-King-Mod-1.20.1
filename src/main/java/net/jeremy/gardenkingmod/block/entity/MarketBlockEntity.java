package net.jeremy.gardenkingmod.block.entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.ModScoreboards;
import net.jeremy.gardenkingmod.network.ModPackets;
import net.jeremy.gardenkingmod.crop.CropTier;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.item.WalletItem;
import net.jeremy.gardenkingmod.screen.MarketScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
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
        public static final int INVENTORY_SIZE = 36;
        public static final TagKey<Item> MARKET_UNSELLABLE =
                        TagKey.of(RegistryKeys.ITEM,
                                        new Identifier(GardenKingMod.MOD_ID, "market_unsellable"));

        private DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

        public MarketBlockEntity(BlockPos pos, BlockState state) {
                super(ModBlockEntities.MARKET_BLOCK_ENTITY, pos, state);
        }

        public static boolean isSellable(ItemStack stack) {
                if (stack.isEmpty()) {
                        return false;
                }

                if (Registries.ITEM.getEntry(stack.getItem()).isIn(MARKET_UNSELLABLE)) {
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
                returnItemsToPlayer(player);
        }

        public boolean returnItemsToPlayer(PlayerEntity player) {
                if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                        return false;
                }

                boolean changed = false;

                for (int slot = 0; slot < size(); slot++) {
                        ItemStack remainingStack = removeStack(slot);
                        if (!remainingStack.isEmpty()) {
                                insertOrDrop(serverPlayer, remainingStack);
                                changed = true;
                        }
                }

                return changed;
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
                        sendSaleResult(player, false, 0, 0, -1,
                                        Text.translatable("message.gardenkingmod.market.empty"), Map.of());
                        return false;
                }

                int inventorySize = items.size();
                int totalItemsSold = 0;
                int totalPayout = 0;
                boolean hasSellableStack = false;
                Map<Item, Integer> soldItemCounts = new LinkedHashMap<>();

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
                                soldItemCounts.merge(stack.getItem(), fullStacks * maxStackSize, Integer::sum);
                        }
                }

                if (!hasSellableStack) {
                        sendSaleResult(player, false, 0, 0, -1,
                                        Text.translatable("message.gardenkingmod.market.invalid"), Map.of());
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
                        sendSaleResult(player, false, 0, 0, -1,
                                        Text.translatable("message.gardenkingmod.market.empty"), Map.of());
                        return false;
                }

                markDirty();

                if (totalPayout > 0) {
                        boolean deposited = WalletItem.depositToBank(player, totalPayout);
                        if (!deposited) {
                                ItemStack currencyStack = new ItemStack(ModItems.DOLLAR, totalPayout);
                                boolean fullyInserted = player.getInventory().insertStack(currencyStack);
                                if (!fullyInserted && !currencyStack.isEmpty()) {
                                        player.dropItem(currencyStack, false);
                                }
                        }
                }

                int lifetimeTotal = ModScoreboards.addCurrency(player, totalPayout);

                sendSaleResult(player, true, totalItemsSold, totalPayout, lifetimeTotal, Text.empty(), soldItemCounts);

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

        private void sendSaleResult(ServerPlayerEntity player, boolean success, int totalItemsSold, int totalPayout,
                        int lifetimeTotal, Text feedback, Map<Item, Integer> soldItemCounts) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(pos);
                buf.writeBoolean(success);
                buf.writeVarInt(totalItemsSold);
                buf.writeVarInt(totalPayout);
                buf.writeVarInt(lifetimeTotal);
                buf.writeText(feedback);
                buf.writeVarInt(soldItemCounts.size());
                soldItemCounts.forEach((item, count) -> {
                        Identifier itemId = Registries.ITEM.getId(item);
                        if (itemId != null) {
                                buf.writeIdentifier(itemId);
                                buf.writeVarInt(count);
                        }
                });
                ServerPlayNetworking.send(player, ModPackets.MARKET_SALE_RESULT_PACKET, buf);
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
