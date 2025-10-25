package net.jeremy.gardenkingmod.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.util.Identifier;

public class GardenOvenRecipe extends AbstractCookingRecipe {
        public GardenOvenRecipe(Identifier id, String group, CookingRecipeCategory category, Ingredient input,
                        ItemStack result, float experience, int cookingTime) {
                super(ModRecipes.GARDEN_OVEN_RECIPE_TYPE, id, group, category, input, result, experience, cookingTime);
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
                return ModRecipes.GARDEN_OVEN_RECIPE_SERIALIZER;
        }

        @Override
        public RecipeType<?> getType() {
                return ModRecipes.GARDEN_OVEN_RECIPE_TYPE;
        }
}
