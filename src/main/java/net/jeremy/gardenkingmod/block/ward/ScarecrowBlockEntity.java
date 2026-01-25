package net.jeremy.gardenkingmod.block.ward;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.ModItemTags;
import net.jeremy.gardenkingmod.screen.ScarecrowScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


public class ScarecrowBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, Inventory {
        public static final int SLOT_HAT = 0;
        public static final int SLOT_HEAD = 1;
        public static final int SLOT_CHEST = 2;
        public static final int SLOT_PITCHFORK = 3;

        public static final int INVENTORY_SIZE = 4;
        public static final int MAX_DURABILITY = 64;

        private static final String NBT_PITCHFORK_AURA = "ScarecrowAura";
        private static final String NBT_HORIZONTAL_BONUS = "HorizontalBonus";
        private static final String NBT_VERTICAL_BONUS = "VerticalBonus";
        private static final String NBT_LEVEL = "Level";
        private static final String NBT_PULSE_INTERVAL = "PulseInterval";
        private static final String NBT_PULSE_DURATION = "PulseDuration";

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
                sanitizeInventory();
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
                        if (slot == SLOT_PITCHFORK) {
                                onPitchforkChanged();
                        }
                        markDirtyAndSync();
                }
                return stack;
        }

        @Override
        public ItemStack removeStack(int slot) {
                ItemStack stack = Inventories.removeStack(this.inventory, slot);
                if (!stack.isEmpty()) {
                        if (slot == SLOT_PITCHFORK) {
                                onPitchforkChanged();
                        }
                        markDirtyAndSync();
                }
                return stack;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
                if (!stack.isEmpty() && !this.isValid(slot, stack)) {
                        return;
                }
                if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
                        stack.setCount(this.getMaxCountPerStack());
                }
                ItemStack previous = this.inventory.get(slot);
                this.inventory.set(slot, stack);
                if (slot == SLOT_PITCHFORK && !ItemStack.areEqual(previous, stack)) {
                        onPitchforkChanged();
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
                boolean hadPitchfork = !getEquippedPitchfork().isEmpty();
                this.inventory.clear();
                if (hadPitchfork) {
                        onPitchforkChanged();
                }
                markDirtyAndSync();
        }

        private void sanitizeInventory() {
                for (int slot = 0; slot < this.inventory.size(); slot++) {
                        ItemStack stack = this.inventory.get(slot);
                        if (!stack.isEmpty() && !this.isValid(slot, stack)) {
                                this.inventory.set(slot, ItemStack.EMPTY);
                                if (slot == SLOT_PITCHFORK) {
                                        onPitchforkChanged();
                                }
                        }
                }
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
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                buf.writeBlockPos(getPos());
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

        public PitchforkAuraStats getPitchforkAuraStats() {
                return resolvePitchforkAuraStats(getEquippedPitchfork());
        }

        public double getPitchforkHorizontalBonus() {
                return getPitchforkAuraStats().horizontalBonus();
        }

        public double getPitchforkVerticalBonus() {
                return getPitchforkAuraStats().verticalBonus();
        }

        public int getPitchforkLevel() {
                return getPitchforkAuraStats().level();
        }

        public int getPitchforkPulseIntervalTicks() {
                return getPitchforkAuraStats().pulseIntervalTicks();
        }

        public int getPitchforkPulseDurationTicks() {
                return getPitchforkAuraStats().pulseDurationTicks();
        }

        public ItemStack getEquippedHat() {
                return this.inventory.get(SLOT_HAT);
        }

        public ItemStack getEquippedHead() {
                return this.inventory.get(SLOT_HEAD);
        }

        public ItemStack getEquippedChest() {
                return this.inventory.get(SLOT_CHEST);
        }

        public ItemStack getEquippedPitchfork() {
                return this.inventory.get(SLOT_PITCHFORK);
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
                if (stack.isEmpty()) {
                        return true;
                }
                return switch (slot) {
                        case SLOT_HAT -> isValidHatItem(stack);
                        case SLOT_HEAD -> isValidHeadItem(stack);
                        case SLOT_CHEST -> isValidChestItem(stack);
                        case SLOT_PITCHFORK -> isValidPitchforkItem(stack);
                        default -> false;
                };
        }

        @Override
        public int getMaxCountPerStack() {
                return 1;
        }

        public static boolean isValidHatItem(ItemStack stack) {
                return stack.isIn(ModItemTags.SCARECROW_HATS);
        }

        public static boolean isValidHeadItem(ItemStack stack) {
                return stack.isIn(ModItemTags.SCARECROW_HEADS);
        }

        public static boolean isValidChestItem(ItemStack stack) {
                return stack.isIn(ModItemTags.SCARECROW_SHIRTS);
        }

        public static boolean isValidPitchforkItem(ItemStack stack) {
                return stack.isIn(ModItemTags.SCARECROW_PITCHFORKS);
        }

        private PitchforkAuraStats resolvePitchforkAuraStats(ItemStack stack) {
                if (stack.isEmpty()) {
                        return PitchforkAuraStats.EMPTY;
                }
                if (!isValidPitchforkItem(stack)) {
                        return PitchforkAuraStats.EMPTY;
                }
                return PitchforkAuraStats.fromStack(stack);
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

        private void onPitchforkChanged() {
                if (this.world instanceof ServerWorld serverWorld) {
                        this.auraComponent.onAuraModifiersChanged(serverWorld.getTime());
                } else {
                        this.auraComponent.onAuraModifiersChanged(-1L);
                }
        }

        private static double readDouble(NbtCompound nbt, String key, double defaultValue) {
                if (!nbt.contains(key, NbtElement.NUMBER_TYPE)) {
                        return defaultValue;
                }
                return nbt.getDouble(key);
        }

        private static int readInt(NbtCompound nbt, String key, int defaultValue) {
                if (!nbt.contains(key, NbtElement.NUMBER_TYPE)) {
                        return defaultValue;
                }
                return nbt.getInt(key);
        }

        public record PitchforkAuraStats(double horizontalBonus, double verticalBonus, int level,
                        int pulseIntervalTicks, int pulseDurationTicks) {

                public static final PitchforkAuraStats EMPTY = new PitchforkAuraStats(0.0, 0.0, 0,
                                ScarecrowAuraComponent.PULSE_INTERVAL_TICKS,
                                ScarecrowAuraComponent.PULSE_DURATION_TICKS);

                static PitchforkAuraStats fromStack(ItemStack stack) {
                        NbtCompound nbt = stack.getNbt();
                        if (nbt == null || !nbt.contains(NBT_PITCHFORK_AURA, NbtElement.COMPOUND_TYPE)) {
                                return EMPTY;
                        }
                        NbtCompound aura = nbt.getCompound(NBT_PITCHFORK_AURA);
                        double horizontal = readDouble(aura, NBT_HORIZONTAL_BONUS, 0.0);
                        double vertical = readDouble(aura, NBT_VERTICAL_BONUS, 0.0);
                        int level = Math.max(0, readInt(aura, NBT_LEVEL, 0));
                        int pulseInterval = Math.max(1,
                                        readInt(aura, NBT_PULSE_INTERVAL, ScarecrowAuraComponent.PULSE_INTERVAL_TICKS));
                        int pulseDuration = Math.max(0,
                                        readInt(aura, NBT_PULSE_DURATION, ScarecrowAuraComponent.PULSE_DURATION_TICKS));
                        return new PitchforkAuraStats(horizontal, vertical, level, pulseInterval, pulseDuration);
                }
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
