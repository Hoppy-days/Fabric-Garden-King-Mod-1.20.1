package net.jeremy.gardenkingmod.block.ward;

import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.screen.ScarecrowScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ScarecrowBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Inventory {
        public static final int INVENTORY_SIZE = 1;
        public static final int MAX_DURABILITY = 64;

        private static final Text TITLE = Text.translatable("container.gardenkingmod.scarecrow");

        private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
        private final PropertyDelegate propertyDelegate;
        private final ScarecrowAuraComponent auraComponent;
        private int durability;
        private long lastRepelTick;

        public ScarecrowBlockEntity(BlockPos pos, BlockState state) {
                super(ModBlockEntities.SCARECROW_BLOCK_ENTITY, pos, state);
                this.auraComponent = new ScarecrowAuraComponent(this);
                this.durability = MAX_DURABILITY;
                this.propertyDelegate = new PropertyDelegate() {
                        @Override
                        public int get(int index) {
                                return switch (index) {
                                        case 0 -> durability;
                                        case 1 -> MAX_DURABILITY;
                                        case 2 -> (int) Math.round(auraComponent.getHorizontalRadius());
                                        case 3 -> (int) Math.round(auraComponent.getVerticalRadius());
                                        default -> 0;
                                };
                        }

                        @Override
                        public void set(int index, int value) {
                                if (index == 0) {
                                        durability = Math.max(0, Math.min(value, MAX_DURABILITY));
                                }
                        }

                        @Override
                        public int size() {
                                return 4;
                        }
                };
        }

        public static void tick(World world, BlockPos pos, BlockState state, ScarecrowBlockEntity blockEntity) {
                if (!(world instanceof ServerWorld serverWorld)) {
                        return;
                }

                blockEntity.auraComponent.tick(serverWorld);
        }

        @Override
        public void readNbt(NbtCompound nbt) {
                super.readNbt(nbt);
                this.inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
                Inventories.readNbt(nbt, this.inventory);
                this.durability = Math.max(0, Math.min(nbt.getInt("Durability"), MAX_DURABILITY));
                this.auraComponent.loadNbt(nbt);
        }

        @Override
        protected void writeNbt(NbtCompound nbt) {
                super.writeNbt(nbt);
                Inventories.writeNbt(nbt, this.inventory);
                nbt.putInt("Durability", this.durability);
                this.auraComponent.saveNbt(nbt);
        }

        @Override
        public int size() {
                return INVENTORY_SIZE;
        }

        @Override
        public boolean isEmpty() {
                return this.inventory.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getStack(int slot) {
                return this.inventory.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
                ItemStack stack = Inventories.splitStack(this.inventory, slot, amount);
                if (!stack.isEmpty()) {
                        markDirtyAndSync();
                }
                return stack;
        }

        @Override
        public ItemStack removeStack(int slot) {
                ItemStack stack = Inventories.removeStack(this.inventory, slot);
                if (!stack.isEmpty()) {
                        markDirtyAndSync();
                }
                return stack;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
                if (!stack.isEmpty() && !this.isValid(slot, stack)) {
                        return;
                }
                this.inventory.set(slot, stack);
                if (stack.getCount() > this.getMaxCountPerStack()) {
                        stack.setCount(this.getMaxCountPerStack());
                }
                markDirtyAndSync();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
                if (this.world == null || this.world.getBlockEntity(this.pos) != this) {
                        return false;
                }
                return player.squaredDistanceTo(Vec3d.ofCenter(this.pos)) <= 64.0;
        }

        @Override
        public void clear() {
                this.inventory.clear();
                markDirtyAndSync();
        }

        @Override
        public void onOpen(PlayerEntity player) {
        }

        @Override
        public void onClose(PlayerEntity player) {
        }

        @Override
        public Text getDisplayName() {
                return TITLE;
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                if (!canPlayerUse(player)) {
                        return null;
                }
                return new ScarecrowScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
        }

        public PropertyDelegate getPropertyDelegate() {
                return this.propertyDelegate;
        }

        public ScarecrowAuraComponent getAuraComponent() {
                return this.auraComponent;
        }

        public boolean isValidUpgrade(ItemStack stack) {
                return stack.isIn(ItemTags.FLOWERS) || stack.isIn(ItemTags.WOOL_CARPETS);
        }

        public boolean isAuraActive() {
                return this.durability > 0 && this.world != null && !this.world.getBlockState(this.pos)
                                .get(ScarecrowBlock.POWERED);
        }

        public void onCrowRepelled(ServerWorld world) {
                if (world.getTime() == this.lastRepelTick) {
                        return;
                }
                this.lastRepelTick = world.getTime();
                if (this.durability <= 0) {
                        return;
                }
                this.durability = Math.max(0, this.durability - 1);
                markDirtyAndSync();
                if (this.durability <= 0) {
                        world.breakBlock(this.pos, true);
                }
        }

        public int getDurability() {
                return this.durability;
        }

        public void setDurability(int durability) {
                this.durability = Math.max(0, Math.min(durability, MAX_DURABILITY));
                markDirtyAndSync();
        }

        public double getUpgradeRadiusBonus() {
                ItemStack stack = this.inventory.get(0);
                if (stack.isEmpty()) {
                        return 0.0;
                }
                return Math.min(6.0, stack.getCount() * 0.5);
        }

        public double getUpgradeVerticalBonus() {
                ItemStack stack = this.inventory.get(0);
                if (stack.isEmpty()) {
                        return 0.0;
                }
                return Math.min(4.0, stack.getCount() * 0.25);
        }

        public int getUpgradeLevel() {
                ItemStack stack = this.inventory.get(0);
                return stack.isEmpty() ? 0 : Math.min(16, stack.getCount());
        }

        public double getHorizontalAuraRadius() {
                return this.auraComponent.getHorizontalRadius();
        }

        public double getVerticalAuraRadius() {
                return this.auraComponent.getVerticalRadius();
        }

        public void markDirtyAndSync() {
                markDirty();
                if (this.world instanceof ServerWorld serverWorld) {
                        serverWorld.getChunkManager().markForUpdate(this.pos);
                }
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
                return slot == 0 && this.isValidUpgrade(stack);
        }

        @Override
        public int getMaxCountPerStack() {
                return 16;
        }

        public boolean isWithinAura(Vec3d position) {
                double horizontalRadius = getHorizontalAuraRadius();
                double verticalRadius = getVerticalAuraRadius();
                Vec3d center = Vec3d.ofCenter(this.pos);
                double dx = position.x - center.x;
                double dz = position.z - center.z;
                double dy = Math.abs(position.y - center.y);
                return Math.sqrt(dx * dx + dz * dz) <= horizontalRadius && dy <= verticalRadius;
        }

        public boolean hasRecentPulse(long time) {
                return this.auraComponent.isPulseActive(time);
        }

        @Override
        public void setWorld(World world) {
                super.setWorld(world);
                if (world instanceof ServerWorld serverWorld) {
                        ScarecrowAuraComponent.register(serverWorld, this);
                        this.auraComponent.initialize(serverWorld.getTime());
                }
        }

        @Override
        public void markRemoved() {
                super.markRemoved();
                ScarecrowAuraComponent.unregister(this);
        }

        @Override
        public void cancelRemoval() {
                super.cancelRemoval();
                if (this.world instanceof ServerWorld serverWorld) {
                        ScarecrowAuraComponent.register(serverWorld, this);
                }
        }

        @Override
        public Packet<ClientPlayPacketListener> toUpdatePacket() {
                return BlockEntityUpdateS2CPacket.create(this);
        }

        @Override
        public NbtCompound toInitialChunkDataNbt() {
                return createNbt();
        }
}
