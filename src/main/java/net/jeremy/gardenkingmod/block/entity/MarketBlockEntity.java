package net.jeremy.gardenkingmod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.ModScoreboards;
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
        public static final int INVENTORY_SIZE = 1;
        public static final int INPUT_SLOT = 0;

        private final DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

        public MarketBlockEntity(BlockPos pos, BlockState state) {
                super(ModBlockEntities.MARKET_BLOCK_ENTITY, pos, state);
        }

        public static boolean isSellable(ItemStack stack) {
                if (stack.isEmpty()) {
                        return false;
                }

                Identifier identifier = Registries.ITEM.getId(stack.getItem());
                return identifier != null && "croptopia".equals(identifier.getNamespace());
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
                return slot == INPUT_SLOT && (stack.isEmpty() || isSellable(stack));
        }

        @Override
        public void clear() {
                items.clear();
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
                Inventories.readNbt(nbt, items);
        }

        public boolean sell(ServerPlayerEntity player) {
                ItemStack stack = getStack(INPUT_SLOT);
                if (stack.isEmpty()) {
                        player.sendMessage(Text.translatable("message.gardenkingmod.market.empty"), true);
                        return false;
                }

                if (!isSellable(stack)) {
                        player.sendMessage(Text.translatable("message.gardenkingmod.market.invalid"), true);
                        return false;
                }

                int soldCount = stack.getCount();
                items.set(INPUT_SLOT, ItemStack.EMPTY);
                markDirty();

                ItemStack currencyStack = new ItemStack(ModItems.GARDEN_COIN, soldCount);
                boolean fullyInserted = player.getInventory().insertStack(currencyStack);
                if (!fullyInserted && !currencyStack.isEmpty()) {
                        player.dropItem(currencyStack, false);
                }

                int lifetimeTotal = ModScoreboards.addCurrency(player, soldCount);
                Text message = lifetimeTotal >= 0
                                ? Text.translatable("message.gardenkingmod.market.sold.lifetime", soldCount, soldCount,
                                                lifetimeTotal)
                                : Text.translatable("message.gardenkingmod.market.sold", soldCount, soldCount);
                player.sendMessage(message, true);

                World world = getWorld();
                if (world != null) {
                        world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.75f,
                                        1.0f);
                }

                return true;
        }
}
