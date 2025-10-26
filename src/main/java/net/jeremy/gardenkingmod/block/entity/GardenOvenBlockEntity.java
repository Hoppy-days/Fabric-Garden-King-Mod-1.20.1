package net.jeremy.gardenkingmod.block.entity;

import java.util.List;

import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.block.GardenOvenBlock;
import net.jeremy.gardenkingmod.recipe.GardenOvenRecipe;
import net.jeremy.gardenkingmod.recipe.ModRecipes;
import net.jeremy.gardenkingmod.screen.GardenOvenScreenHandler;
import net.jeremy.gardenkingmod.util.GardenOvenBalanceConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class GardenOvenBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
        public static final int INPUT_SLOT_COUNT = 9;
        public static final int OUTPUT_SLOT_INDEX = 9;
        private static final int INVENTORY_SIZE = OUTPUT_SLOT_INDEX + 1;
        private static final int[] TOP_SLOTS = createRange(0, INPUT_SLOT_COUNT);
        private static final int[] SIDE_SLOTS = createRange(0, INPUT_SLOT_COUNT);
        private static final int[] BOTTOM_SLOTS = new int[] { OUTPUT_SLOT_INDEX };

        private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
        private final PropertyDelegate propertyDelegate;
        private int cookTime;
        private int cookTimeTotal;
        private float storedExperience;

        public GardenOvenBlockEntity(BlockPos pos, BlockState state) {
                super(ModBlockEntities.GARDEN_OVEN_BLOCK_ENTITY, pos, state);
                this.propertyDelegate = new PropertyDelegate() {
                        @Override
                        public int get(int index) {
                                return switch (index) {
                                case 0 -> cookTime;
                                case 1 -> cookTimeTotal;
                                default -> 0;
                                };
                        }

                        @Override
                        public void set(int index, int value) {
                                switch (index) {
                                case 0 -> cookTime = value;
                                case 1 -> cookTimeTotal = value;
                                default -> {
                                }
                                }
                        }

                        @Override
                        public int size() {
                                return 2;
                        }
                };
        }

        private static int[] createRange(int startInclusive, int length) {
                int[] slots = new int[length];
                for (int i = 0; i < length; i++) {
                        slots[i] = startInclusive + i;
                }
                return slots;
        }

        public static void tick(World world, BlockPos pos, BlockState state, GardenOvenBlockEntity blockEntity) {
                if (world.isClient) {
                        return;
                }

                boolean wasCooking = blockEntity.isCooking();
                boolean dirty = false;

                SimpleInventory input = blockEntity.createInputInventory();
                GardenOvenRecipe recipe = blockEntity.getMatchingRecipe(world, input);

                if (recipe != null && blockEntity.canAcceptRecipe(world, recipe)) {
                        blockEntity.cookTimeTotal = recipe.getCookingTime();
                        blockEntity.cookTime++;
                        if (blockEntity.cookTime >= blockEntity.cookTimeTotal) {
                                blockEntity.cookTime = 0;
                                blockEntity.cookTimeTotal = recipe.getCookingTime();
                                blockEntity.craftRecipe(world, pos, recipe, input);
                                dirty = true;
                        }
                } else {
                        if (blockEntity.cookTime > 0) {
                                blockEntity.cookTime = Math.max(blockEntity.cookTime - 2, 0);
                        }
                        blockEntity.cookTimeTotal = GardenOvenBalanceConfig.get().cookTime();
                }

                boolean isCooking = blockEntity.isCooking();
                if (wasCooking != isCooking) {
                        dirty = true;
                        state = state.with(GardenOvenBlock.LIT, isCooking);
                        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
                }

                if (dirty) {
                        blockEntity.markDirty();
                }
        }

        private GardenOvenRecipe getMatchingRecipe(World world, SimpleInventory input) {
                RecipeManager manager = world.getRecipeManager();
                List<GardenOvenRecipe> matches = manager.getAllMatches(ModRecipes.GARDEN_OVEN_RECIPE_TYPE, input, world);
                return matches.isEmpty() ? null : matches.get(0);
        }

        private boolean canAcceptRecipe(World world, GardenOvenRecipe recipe) {
                ItemStack result = recipe.getOutput(world.getRegistryManager());
                if (result.isEmpty()) {
                        return false;
                }

                ItemStack output = this.inventory.get(OUTPUT_SLOT_INDEX);
                if (output.isEmpty()) {
                        return true;
                }

                if (!ItemStack.canCombine(output, result)) {
                        return false;
                }

                return output.getCount() + result.getCount() <= output.getMaxCount();
        }

        private void craftRecipe(World world, BlockPos pos, GardenOvenRecipe recipe, SimpleInventory input) {
                if (world.isClient) {
                        return;
                }

                ItemStack result = recipe.craft(input, world.getRegistryManager());
                if (result.isEmpty()) {
                        return;
                }

                ItemStack output = this.inventory.get(OUTPUT_SLOT_INDEX);
                if (output.isEmpty()) {
                        this.inventory.set(OUTPUT_SLOT_INDEX, result);
                } else if (ItemStack.canCombine(output, result)) {
                                output.increment(result.getCount());
                }

                consumeIngredients(recipe, input);
                applyRemainders(world, pos, recipe, input);
                this.storedExperience += recipe.getExperience();
        }

        private void consumeIngredients(GardenOvenRecipe recipe, SimpleInventory input) {
                boolean[] consumed = new boolean[INPUT_SLOT_COUNT];

                for (int index = 0; index < recipe.getIngredients().size(); index++) {
                        var ingredient = recipe.getIngredients().get(index);
                        if (ingredient == null || ingredient.isEmpty()) {
                                continue;
                        }

                        for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) {
                                if (consumed[slot]) {
                                        continue;
                                }

                                ItemStack stack = input.getStack(slot);
                                if (!stack.isEmpty() && ingredient.test(stack)) {
                                        consumed[slot] = true;
                                        ItemStack current = this.inventory.get(slot);
                                        current.decrement(1);
                                        if (current.isEmpty()) {
                                                this.inventory.set(slot, ItemStack.EMPTY);
                                        }
                                        break;
                                }
                        }
                }
        }

        private void applyRemainders(World world, BlockPos pos, GardenOvenRecipe recipe, SimpleInventory input) {
                DefaultedList<ItemStack> remainders = recipe.getRemainder(input);
                for (int slot = 0; slot < INPUT_SLOT_COUNT && slot < remainders.size(); slot++) {
                        ItemStack remainder = remainders.get(slot);
                        if (remainder.isEmpty()) {
                                continue;
                        }

                        ItemStack current = this.inventory.get(slot);
                        if (current.isEmpty()) {
                                this.inventory.set(slot, remainder);
                        } else if (ItemStack.canCombine(current, remainder)) {
                                current.increment(remainder.getCount());
                        } else if (world instanceof ServerWorld serverWorld) {
                                ItemScatterer.spawn(serverWorld, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, remainder);
                        }
                }
        }

        public void dropContents(World world, BlockPos pos) {
                ItemScatterer.spawn(world, pos, this.inventory);
                this.inventory.clear();
        }

        public void onOutputTaken(ServerPlayerEntity player) {
                if (player == null || this.world == null) {
                        return;
                }

                ServerWorld serverWorld = (ServerWorld) this.world;
                int experience = MathHelper.floor(this.storedExperience);
                float remainder = this.storedExperience - experience;
                if (remainder > 0.0F && serverWorld.random.nextFloat() < remainder) {
                        experience++;
                }
                if (experience > 0) {
                        ExperienceOrbEntity.spawn(serverWorld, Vec3d.ofCenter(this.pos), experience);
                }
                this.storedExperience = 0.0F;
        }

        private SimpleInventory createInputInventory() {
                SimpleInventory input = new SimpleInventory(INPUT_SLOT_COUNT);
                for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) {
                        input.setStack(slot, this.inventory.get(slot).copy());
                }
                return input;
        }

        private boolean isCooking() {
                return this.cookTime > 0;
        }

        public PropertyDelegate getPropertyDelegate() {
                return this.propertyDelegate;
        }

        @Override
        public Text getDisplayName() {
                return Text.translatable("container.gardenkingmod.garden_oven");
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new GardenOvenScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
        }

        @Override
        public void readNbt(NbtCompound nbt) {
                super.readNbt(nbt);
                this.inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
                Inventories.readNbt(nbt, this.inventory);
                this.cookTime = nbt.getInt("CookTime");
                this.cookTimeTotal = nbt.getInt("CookTimeTotal");
                this.storedExperience = nbt.getFloat("StoredExperience");
        }

        @Override
        protected void writeNbt(NbtCompound nbt) {
                super.writeNbt(nbt);
                Inventories.writeNbt(nbt, this.inventory);
                nbt.putInt("CookTime", this.cookTime);
                nbt.putInt("CookTimeTotal", this.cookTimeTotal);
                nbt.putFloat("StoredExperience", this.storedExperience);
        }

        @Override
        public int size() {
                return this.inventory.size();
        }

        @Override
        public boolean isEmpty() {
                for (ItemStack stack : this.inventory) {
                        if (!stack.isEmpty()) {
                                return false;
                        }
                }
                return true;
        }

        @Override
        public ItemStack getStack(int slot) {
                return this.inventory.get(slot);
        }

        public ItemStack removeStack(int slot, int amount) {
                ItemStack result = Inventories.splitStack(this.inventory, slot, amount);
                if (!result.isEmpty()) {
                        this.markDirty();
                }
                return result;
        }

        public ItemStack removeStack(int slot) {
                ItemStack result = Inventories.removeStack(this.inventory, slot);
                if (!result.isEmpty()) {
                        this.markDirty();
                }
                return result;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
                ItemStack existing = this.inventory.get(slot);
                boolean same = !stack.isEmpty() && ItemStack.canCombine(existing, stack);
                this.inventory.set(slot, stack);
                if (stack.getCount() > this.getMaxCountPerStack()) {
                        stack.setCount(this.getMaxCountPerStack());
                }
                if (slot < INPUT_SLOT_COUNT && !same) {
                        this.cookTime = 0;
                }
                this.markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
                if (this.world == null) {
                        return false;
                }
                if (this.world.getBlockEntity(this.pos) != this) {
                        return false;
                }
                return player.squaredDistanceTo(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
        }

        @Override
        public void clear() {
                this.inventory.clear();
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
                if (side == Direction.DOWN) {
                        return BOTTOM_SLOTS;
                }
                return side == Direction.UP ? TOP_SLOTS : SIDE_SLOTS;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
                return slot < INPUT_SLOT_COUNT;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
                if (dir == Direction.DOWN) {
                        return slot == OUTPUT_SLOT_INDEX;
                }
                return slot < INPUT_SLOT_COUNT;
        }
}
