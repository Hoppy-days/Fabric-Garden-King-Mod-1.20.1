package net.jeremy.gardenkingmod.recipe;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModRecipes {
        public static final RecipeType<GardenOvenRecipe> GARDEN_OVEN_RECIPE_TYPE = Registry.register(
                        Registries.RECIPE_TYPE, new Identifier(GardenKingMod.MOD_ID, "garden_oven"),
                        new RecipeType<GardenOvenRecipe>() {
                                @Override
                                public String toString() {
                                        return new Identifier(GardenKingMod.MOD_ID, "garden_oven").toString();
                                }
                        });

        public static final RecipeSerializer<GardenOvenRecipe> GARDEN_OVEN_RECIPE_SERIALIZER = Registry.register(
                        Registries.RECIPE_SERIALIZER, new Identifier(GardenKingMod.MOD_ID, "garden_oven"),
                        new GardenOvenRecipe.Serializer());

        private ModRecipes() {
        }

        public static void register() {
                GardenKingMod.LOGGER.info("Registering Garden King recipes");
        }
}
