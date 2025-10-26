package net.jeremy.gardenkingmod.screen;

import java.util.Optional;

import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.block.entity.GardenOvenBlockEntity;
import net.jeremy.gardenkingmod.crop.CropTier;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.network.SkillProgressNetworking;
import net.jeremy.gardenkingmod.skill.HarvestXpConfig;
import net.jeremy.gardenkingmod.skill.SkillProgressHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class GardenOvenScreenHandler extends ScreenHandler {
        private static final int INPUT_SLOT_COUNT = GardenOvenBlockEntity.INPUT_SLOT_COUNT;
        private static final int OUTPUT_SLOT_INDEX = GardenOvenBlockEntity.OUTPUT_SLOT_INDEX;
        private static final int INVENTORY_SLOT_COUNT = OUTPUT_SLOT_INDEX + 1;
        private static final int PLAYER_INVENTORY_SLOT_COUNT = 27;
        private static final int HOTBAR_SLOT_COUNT = 9;
        private static final int PLAYER_SLOT_START = INVENTORY_SLOT_COUNT;
        private static final int PLAYER_SLOT_END = PLAYER_SLOT_START + PLAYER_INVENTORY_SLOT_COUNT;
        private static final int HOTBAR_SLOT_START = PLAYER_SLOT_END;
        private static final int HOTBAR_SLOT_END = HOTBAR_SLOT_START + HOTBAR_SLOT_COUNT;

        private final Inventory inventory;
        private final PropertyDelegate propertyDelegate;
        private final PlayerEntity player;

        public GardenOvenScreenHandler(int syncId, PlayerInventory playerInventory) {
                this(syncId, playerInventory, new SimpleInventory(INVENTORY_SLOT_COUNT), new ArrayPropertyDelegate(2));
        }

        public GardenOvenScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory,
                        PropertyDelegate propertyDelegate) {
                super(ModScreenHandlers.GARDEN_OVEN_SCREEN_HANDLER, syncId);
                checkSize(inventory, INVENTORY_SLOT_COUNT);
                this.inventory = inventory;
                this.propertyDelegate = propertyDelegate;
                this.player = playerInventory.player;
                this.inventory.onOpen(this.player);

                this.addProperties(propertyDelegate);
                addOvenSlots();
                addPlayerInventory(playerInventory);
                addPlayerHotbar(playerInventory);
        }

        private void addOvenSlots() {
                int startX = 30;
                int startY = 17;
                int slotSize = 18;

                for (int row = 0; row < 3; row++) {
                        for (int column = 0; column < 3; column++) {
                                int index = column + row * 3;
                                int x = startX + column * slotSize;
                                int y = startY + row * slotSize;
                                this.addSlot(new Slot(this.inventory, index, x, y));
                        }
                }

                this.addSlot(new GardenOvenResultSlot(this.player, this.inventory, OUTPUT_SLOT_INDEX, 124, 35));
        }

        private void addPlayerInventory(PlayerInventory playerInventory) {
                int startX = 8;
                int startY = 84;
                int slotSize = 18;

                for (int row = 0; row < 3; ++row) {
                        for (int column = 0; column < 9; ++column) {
                                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, startX + column * slotSize,
                                                startY + row * slotSize));
                        }
                }
        }

        private void addPlayerHotbar(PlayerInventory playerInventory) {
                int startX = 8;
                int startY = 142;
                int slotSize = 18;

                for (int column = 0; column < 9; ++column) {
                        this.addSlot(new Slot(playerInventory, column, startX + column * slotSize, startY));
                }
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int index) {
                ItemStack newStack = ItemStack.EMPTY;
                Slot slot = this.slots.get(index);
                if (slot != null && slot.hasStack()) {
                        ItemStack originalStack = slot.getStack();
                        newStack = originalStack.copy();

                        if (index == OUTPUT_SLOT_INDEX) {
                                if (!this.insertItem(originalStack, PLAYER_SLOT_START, HOTBAR_SLOT_END, true)) {
                                        return ItemStack.EMPTY;
                                }
                                slot.onQuickTransfer(originalStack, newStack);
                        } else if (index < INVENTORY_SLOT_COUNT) {
                                if (!this.insertItem(originalStack, PLAYER_SLOT_START, HOTBAR_SLOT_END, false)) {
                                        return ItemStack.EMPTY;
                                }
                        } else if (!this.insertItem(originalStack, 0, INPUT_SLOT_COUNT, false)) {
                                return ItemStack.EMPTY;
                        }

                        if (originalStack.isEmpty()) {
                                slot.setStack(ItemStack.EMPTY);
                        } else {
                                slot.markDirty();
                        }

                        if (originalStack.getCount() == newStack.getCount()) {
                                return ItemStack.EMPTY;
                        }

                        slot.onTakeItem(player, originalStack);
                }

                return newStack;
        }

        @Override
        public boolean canUse(PlayerEntity player) {
                return this.inventory.canPlayerUse(player);
        }

        @Override
        public void onClosed(PlayerEntity player) {
                super.onClosed(player);
                this.inventory.onClose(player);
        }

        public int getCookProgress() {
                int cookTime = this.propertyDelegate.get(0);
                int cookTimeTotal = this.propertyDelegate.get(1);
                if (cookTimeTotal <= 0 || cookTime <= 0) {
                        return 0;
                }

                return cookTime * 24 / cookTimeTotal;
        }

        public boolean isCooking() {
                return this.propertyDelegate.get(0) > 0;
        }

        private static class GardenOvenResultSlot extends Slot {
                private final PlayerEntity player;
                private final Inventory inventory;

                GardenOvenResultSlot(PlayerEntity player, Inventory inventory, int index, int x, int y) {
                        super(inventory, index, x, y);
                        this.player = player;
                        this.inventory = inventory;
                }

                @Override
                public boolean canInsert(ItemStack stack) {
                        return false;
                }

                @Override
                public void onTakeItem(PlayerEntity player, ItemStack stack) {
                        super.onTakeItem(player, stack);
                        if (!player.getWorld().isClient && player instanceof ServerPlayerEntity serverPlayer) {
                                awardSkillExperience(serverPlayer, stack);
                                if (this.inventory instanceof GardenOvenBlockEntity oven) {
                                        oven.onOutputTaken(serverPlayer);
                                }
                        }
                }
        }

        private static void awardSkillExperience(ServerPlayerEntity player, ItemStack stack) {
                if (stack.isEmpty()) {
                        return;
                }

                Optional<CropTier> tier = CropTierRegistry.get(stack.getItem());
                if (tier.isEmpty()) {
                        return;
                }

                long experiencePerItem = HarvestXpConfig.get().experienceForTierPath(tier.get().id().getPath());
                if (experiencePerItem <= 0L) {
                        return;
                }

                long totalExperience;
                try {
                        totalExperience = Math.multiplyExact(experiencePerItem, stack.getCount());
                } catch (ArithmeticException overflow) {
                        totalExperience = Long.MAX_VALUE;
                }

                if (player instanceof SkillProgressHolder holder) {
                        holder.gardenkingmod$addSkillExperience(totalExperience);
                        SkillProgressNetworking.sync(player);
                }
        }
}
