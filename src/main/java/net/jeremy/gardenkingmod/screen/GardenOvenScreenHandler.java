package net.jeremy.gardenkingmod.screen;

import java.util.Optional;

import net.jeremy.gardenkingmod.ModScreenHandlers;
import net.jeremy.gardenkingmod.crop.CropTier;
import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.network.SkillProgressNetworking;
import net.jeremy.gardenkingmod.recipe.ModRecipes;
import net.jeremy.gardenkingmod.skill.HarvestXpConfig;
import net.jeremy.gardenkingmod.skill.SkillProgressHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class GardenOvenScreenHandler extends AbstractFurnaceScreenHandler {
        private static final int RESULT_SLOT_INDEX = 2;

        public GardenOvenScreenHandler(int syncId, PlayerInventory playerInventory) {
                this(syncId, playerInventory, new SimpleInventory(3), new ArrayPropertyDelegate(4));
        }

        public GardenOvenScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory,
                        PropertyDelegate propertyDelegate) {
                super(ModScreenHandlers.GARDEN_OVEN_SCREEN_HANDLER, ModRecipes.GARDEN_OVEN_RECIPE_TYPE,
                                RecipeBookCategory.FURNACE, syncId, playerInventory, inventory, propertyDelegate);
                replaceResultSlot(playerInventory.player, inventory);
        }

        private void replaceResultSlot(PlayerEntity player, Inventory inventory) {
                Slot original = this.slots.get(RESULT_SLOT_INDEX);
                GardenOvenResultSlot replacement = new GardenOvenResultSlot(player, inventory, original.getIndex(),
                                original.x, original.y);
                replacement.id = original.id;
                this.slots.set(RESULT_SLOT_INDEX, replacement);
                this.setPreviousTrackedSlot(RESULT_SLOT_INDEX, replacement.getStack());
        }

        private static class GardenOvenResultSlot extends FurnaceOutputSlot {
                GardenOvenResultSlot(PlayerEntity player, Inventory inventory, int index, int x, int y) {
                        super(player, inventory, index, x, y);
                }

                @Override
                public void onTakeItem(PlayerEntity player, ItemStack stack) {
                        super.onTakeItem(player, stack);
                        if (!player.getWorld().isClient && player instanceof ServerPlayerEntity serverPlayer) {
                                awardSkillExperience(serverPlayer, stack);
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
