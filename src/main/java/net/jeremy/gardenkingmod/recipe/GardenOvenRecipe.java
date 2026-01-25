package net.jeremy.gardenkingmod.recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.jeremy.gardenkingmod.util.GardenOvenBalanceConfig;
import net.jeremy.gardenkingmod.block.entity.GardenOvenBlockEntity;
import net.jeremy.gardenkingmod.recipe.ModRecipes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraft.util.JsonHelper;

public class GardenOvenRecipe implements Recipe<Inventory> {
        private final Identifier id;
        private final String group;
        private final CookingRecipeCategory category;
        private final DefaultedList<Ingredient> ingredients;
        private final ItemStack result;
        private final float experience;
        private final int cookingTime;

        public GardenOvenRecipe(Identifier id, String group, CookingRecipeCategory category,
                        DefaultedList<Ingredient> ingredients, ItemStack result, float experience, int cookingTime) {
                this.id = id;
                this.group = group;
                this.category = category;
                this.ingredients = ingredients;
                this.result = result;
                this.experience = experience;
                this.cookingTime = cookingTime;
        }

        @Override
        public boolean matches(Inventory inventory, World world) {
                if (inventory == null) {
                        return false;
                }

                List<Ingredient> remaining = new ArrayList<>(this.ingredients);
                int ingredientCount = 0;

                for (int slot = 0; slot < inventory.size(); slot++) {
                        ItemStack stack = inventory.getStack(slot);
                        if (stack.isEmpty()) {
                                continue;
                        }

                        ingredientCount++;
                        boolean matched = false;
                        Iterator<Ingredient> iterator = remaining.iterator();
                        while (iterator.hasNext()) {
                                Ingredient ingredient = iterator.next();
                                if (ingredient.test(stack)) {
                                        matched = true;
                                        iterator.remove();
                                        break;
                                }
                        }

                        if (!matched) {
                                return false;
                        }
                }

                return remaining.isEmpty() && ingredientCount == this.ingredients.size();
        }

        @Override
        public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
                return this.result.copy();
        }

        @Override
        public boolean fits(int width, int height) {
                return width * height >= this.ingredients.size();
        }

        @Override
        public ItemStack getOutput(DynamicRegistryManager registryManager) {
                return this.result.copy();
        }

        @Override
        public DefaultedList<Ingredient> getIngredients() {
                return this.ingredients;
        }

        public Identifier getId() {
                return this.id;
        }

        public String getGroup() {
                return this.group;
        }

        public CookingRecipeCategory getCategory() {
                return this.category;
        }

        public float getExperience() {
                return this.experience;
        }

        public int getCookingTime() {
                return this.cookingTime;
        }

        @Override
        public DefaultedList<ItemStack> getRemainder(Inventory inventory) {
                DefaultedList<ItemStack> remainders = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
                for (int slot = 0; slot < inventory.size(); slot++) {
                        ItemStack stack = inventory.getStack(slot);
                        if (!stack.isEmpty()) {
                                remainders.set(slot, stack.getRecipeRemainder());
                        }
                }
                return remainders;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
                return ModRecipes.GARDEN_OVEN_RECIPE_SERIALIZER;
        }

        @Override
        public RecipeType<?> getType() {
                return ModRecipes.GARDEN_OVEN_RECIPE_TYPE;
        }

        public static class Serializer implements RecipeSerializer<GardenOvenRecipe> {
                @Override
                public GardenOvenRecipe read(Identifier id, JsonObject json) {
                        String group = json.has("group") ? json.get("group").getAsString() : "";
                        CookingRecipeCategory category = readCategory(json);
                        DefaultedList<Ingredient> ingredients = readIngredients(JsonHelper.getArray(json, "ingredients"));
                        ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
                        float experience = JsonHelper.getFloat(json, "experience", 0.0F);
                        int cookingTime = JsonHelper.getInt(json, "cookingtime", GardenOvenBalanceConfig.get().cookTime());
                        return new GardenOvenRecipe(id, group, category, ingredients, result, experience, cookingTime);
                }

                @Override
                public GardenOvenRecipe read(Identifier id, PacketByteBuf buf) {
                        String group = buf.readString();
                        CookingRecipeCategory category = buf.readEnumConstant(CookingRecipeCategory.class);
                        int ingredientCount = buf.readVarInt();
                        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(ingredientCount, Ingredient.EMPTY);
                        for (int i = 0; i < ingredientCount; i++) {
                                ingredients.set(i, Ingredient.fromPacket(buf));
                        }
                        ItemStack result = buf.readItemStack();
                        float experience = buf.readFloat();
                        int cookingTime = buf.readVarInt();
                        return new GardenOvenRecipe(id, group, category, ingredients, result, experience, cookingTime);
                }

                @Override
                public void write(PacketByteBuf buf, GardenOvenRecipe recipe) {
                        buf.writeString(recipe.group);
                        buf.writeEnumConstant(recipe.category);
                        buf.writeVarInt(recipe.ingredients.size());
                        for (Ingredient ingredient : recipe.ingredients) {
                                ingredient.write(buf);
                        }
                        buf.writeItemStack(recipe.result);
                        buf.writeFloat(recipe.experience);
                        buf.writeVarInt(recipe.cookingTime);
                }

                private static CookingRecipeCategory readCategory(JsonObject json) {
                        if (json.has("category")) {
                                String name = json.get("category").getAsString();
                                try {
                                        return CookingRecipeCategory.valueOf(name.toUpperCase(Locale.ROOT));
                                } catch (IllegalArgumentException ignored) {
                                }
                        }
                        return CookingRecipeCategory.MISC;
                }

                private static DefaultedList<Ingredient> readIngredients(JsonArray array) {
                        DefaultedList<Ingredient> ingredients = DefaultedList.of();
                        for (JsonElement element : array) {
                                Ingredient ingredient = Ingredient.fromJson(element);
                                if (!ingredient.isEmpty()) {
                                        ingredients.add(ingredient);
                                }
                        }
                        if (ingredients.isEmpty()) {
                                throw new IllegalArgumentException("Garden oven recipe must have at least one ingredient");
                        }
                        if (ingredients.size() > GardenOvenBlockEntity.INPUT_SLOT_COUNT) {
                                throw new IllegalArgumentException("Garden oven recipe cannot have more than "
                                                + GardenOvenBlockEntity.INPUT_SLOT_COUNT + " ingredients");
                        }
                        return ingredients;
                }
        }
}
