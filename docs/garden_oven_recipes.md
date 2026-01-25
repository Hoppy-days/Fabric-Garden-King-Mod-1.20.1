# Garden Oven recipe overrides

The mod now overrides a wide range of Croptopia "meal" recipes so they must be prepared in the Garden Oven instead of a crafting grid. Each override keeps the original ingredients and outputs while switching the recipe type to `gardenkingmod:garden_oven`, which lets JEI and the oven screen show matching inputs.

## Converted Croptopia meals

The following Croptopia foods are now Garden Oven recipes:

- beef_stew
- beef_stir_fry
- beef_wellington
- blt
- burrito
- buttered_green_beans
- carnitas
- cashew_chicken
- cheese_pizza
- cheeseburger
- cheesy_asparagus
- chicken_and_dumplings
- chicken_and_noodles
- chicken_and_rice
- chili_relleno
- chimichanga
- cornish_pasty
- crema
- egg_roll
- eggplant_parmesan
- enchilada
- fajitas
- fish_and_chips
- fried_chicken
- grilled_cheese
- grilled_eggplant
- hamburger
- lemon_chicken
- nether_wart_stew
- peanut_butter_and_jam
- pineapple_pepperoni_pizza
- pizza
- potato_soup
- quesadilla
- ratatouille
- refried_beans
- roasted_asparagus
- roasted_radishes
- roasted_squash
- roasted_turnips
- shepherds_pie
- spaghetti_squash
- steamed_broccoli
- steamed_green_beans
- stir_fry
- stuffed_artichoke
- stuffed_poblanos
- supreme_pizza
- sushi
- taco
- tamales
- toast_sandwich
- tofuburger
- tostada

*(Croptopia does not ship recipes for baked_sweet_potato, baked_yam, or tofu_and_noodles, so those remain unchanged.)*

## Adding or editing Garden Oven recipes yourself

1. Place each recipe JSON inside `src/main/resources/data/<namespace>/recipes/`. To override an existing Croptopia recipe use `namespace = croptopia` and match the filename to the item id (for example, `data/croptopia/recipes/enchilada.json`).
2. Set the JSON `type` field to `"gardenkingmod:garden_oven"` so Fabric loads it with the custom serializer.
3. Provide an `ingredients` array that lists every required input. When converting shaped crafting recipes you can flatten the pattern so each occupied slot becomes one entry in the array. Use `{ "item": "namespace:item" }` or `{ "tag": "namespace:tag" }` for each ingredient, and include tools that should be consumed via their `recipe_remainder`.
4. Copy the `result` object from the source recipe so the oven produces the same output and stack size.
5. Optionally add a `group` string for JEI grouping and include an `experience` number if the oven should grant XP when the output is collected.
6. Control cook duration with the `cookingtime` field (in ticks). If you omit it the oven falls back to the configurable default from `config/gardenkingmod/garden_oven.json` (200 ticks by default). Use larger values for longer dishes.

Remember to store any new food textures at `assets/<your_namespace>/textures/item/` (for example `assets/croptopia/textures/item/your_food.png`) so Minecraft can render the output properly.
