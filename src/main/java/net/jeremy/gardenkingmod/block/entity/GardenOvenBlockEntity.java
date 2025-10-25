package net.jeremy.gardenkingmod.block.entity;

import net.jeremy.gardenkingmod.ModBlockEntities;
import net.jeremy.gardenkingmod.recipe.ModRecipes;
import net.jeremy.gardenkingmod.screen.GardenOvenScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class GardenOvenBlockEntity extends AbstractFurnaceBlockEntity {
        public static final int INVENTORY_SIZE = 3;

        public GardenOvenBlockEntity(BlockPos pos, BlockState state) {
                super(ModBlockEntities.GARDEN_OVEN_BLOCK_ENTITY, pos, state, ModRecipes.GARDEN_OVEN_RECIPE_TYPE);
        }

        protected Text getContainerName() {
                return Text.translatable("container.gardenkingmod.garden_oven");
        }

        protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
                return new GardenOvenScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
        }

        protected RecipeBookCategory getRecipeBookCategory() {
                return RecipeBookCategory.FURNACE;
        }

}
