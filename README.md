# Garden King Mod

## Bonus harvest drops

Garden King now loads bonus crop drop definitions from JSON files under `data/gardenkingmod/bonus_harvest_drops/`. Each file maps a crop block or direct loot table identifier to one or more bonus entries with an `item`, `chance`, and integer `count` range. All of the project's data-driven loaders strip any field named `_comment`, so you can document bonus drops, garden shop offers, or rotten crop definitions with entries such as `"_comment": "This is a general comment about the file."` anywhere in the JSON tree. The new [`wheat_diamonds.json`](src/main/resources/data/gardenkingmod/bonus_harvest_drops/wheat_diamonds.json) file, for example, grants wheat a 5% chance to drop 1–3 diamonds when harvested.

### Creating event packs

Seasonal or event datapacks can override or replace these bonus drops without code changes:

1. Create a datapack with the `gardenkingmod` namespace.
2. Add JSON definitions under `data/gardenkingmod/bonus_harvest_drops/` mirroring the structure above.
3. Reload the server with `/reload` or restart to apply the new loot behaviour.

The repository ships an [example event configuration](src/main/resources/data/gardenkingmod_event_example/bonus_harvest_drops/wheat_diamonds.json) that boosts wheat to drop emeralds more frequently. Copy this structure into a datapack and adjust it for real events.

### Asset reminders

If an event introduces new items, remember to include their textures in `assets/gardenkingmod/textures/item/`. Place the matching `.png` files in that directory so Fabric can load them correctly.

## Crow spawning

Crow spawning is configured with two JSON files so that designers can keep biome targeting separate from spawn behaviour:

* [`data/gardenkingmod/tags/worldgen/biome/spawns_crows.json`](src/main/resources/data/gardenkingmod/tags/worldgen/biome/spawns_crows.json) lists the biome IDs that should spawn crows.
* [`config/gardenkingmod/crow.json`](config/gardenkingmod/crow.json) stores the spawn weight and minimum/maximum group sizes used by Fabric's mob spawning rules.

### Adjusting biomes

1. Open `data/gardenkingmod/tags/worldgen/biome/spawns_crows.json`.
2. Add or remove biome IDs under the `values` array to control which biomes spawn crows. Use fully qualified identifiers such as `minecraft:plains`.
3. Save the file and reload the game or server with `/reload`, or restart the instance, to apply the new biome list.

### Tuning spawn rates

1. Open `config/gardenkingmod/crow.json`.
2. Change `spawnWeight` to increase or decrease how frequently crows spawn relative to other mobs in the same biome.
3. Adjust `minSpawnGroupSize` and `maxSpawnGroupSize` to define how many crows appear together. Ensure the minimum does not exceed the maximum.
4. Save the file and use `/reload` or restart the game/server to refresh the spawn parameters.

### Crow assets

Crow models, textures, and other assets follow the same conventions as the rest of the project. Keep any `.png` textures for the crow entity under `assets/gardenkingmod/textures/entity/` so that they are picked up automatically at runtime.

## Garden shop trades

The garden shop's inventory is now data-driven. All default trades live in
[`data/gardenkingmod/garden_shop_offers.json`](src/main/resources/data/gardenkingmod/garden_shop_offers.json), which groups
offers by UI tab through a `pages` array. Use `_comment` fields at the root, page, or offer level to label long files without affecting parsing:

```json
{
  "_comment": "Starter inventory for the travelling gardener.",
  "pages": [
    {
      "_comment": "Page 1: equipment",
      "offers": [
        {
          "_comment": "Keep the elytra trade near the top for visibility.",
          "offer": "minecraft:elytra",
          "price": "gardenkingmod:ruby*32"
        }
      ]
    }
  ]
}
```

* Each object inside `pages` represents one tab from top to bottom. The example file defines five pages to match the five tab
  buttons in the UI, but you can leave an `offers` array empty if you want a blank tab.
* `offer` is the item the shop will sell. You can provide it as a plain string (`"namespace:item"`) or as an object with
  explicit fields such as `{ "item": "croptopia:cheese", "count": 2 }`.
* `price` accepts either a single string/object or an array if you want multiple inputs. To specify stack sizes inline, append
  `*<count>` to the identifier (for example, `"gardenkingmod:ruby*32"`).

The stock configuration distributes the ruby-for-elytra upgrades across all five tabs while keeping the cheese trade on the
first tab.

### Adding more trades

1. Open [`garden_shop_offers.json`](src/main/resources/data/gardenkingmod/garden_shop_offers.json).
2. Decide which tab should display the trade and add a new JSON object to that page's `offers` array using the format above.
   Add a brand-new page object under `pages` if you want to populate another tab.
3. Save the file and reload your data packs (or restart the game/server) so Fabric's resource loader picks up the change.
4. If the trade produces a brand-new item, place its `.png` texture inside `assets/gardenkingmod/textures/item/` so Fabric can
   find it at runtime.

## Fortune levels and loot drops

The Fortune effect rolls for extra items whenever a block or crop is flagged as "fortune affected" in its loot table. For ore-style drops (diamonds, coal, emeralds, etc.) the final stack size equals `1 + random(0, level)`, so higher levels guarantee more bonus items on average. The table below shows how this plays out in practice:

| Fortune level | Possible drops from diamond ore | Average yield |
| ------------- | -------------------------------- | ------------- |
| 0             | Always 1 item                    | 1.0           |
| 1             | 1–2 items                        | 1.5           |
| 2             | 1–3 items                        | 2.0           |
| 3             | 1–4 items                        | 2.5           |
| 5             | 1–6 items                        | 3.5           |
| 10            | 1–11 items                       | 5.5           |

The `1 + random(0, level)` formula above keeps scaling with whatever Fortune level a tool reports—there is no hard upper ceiling baked into vanilla's ore-style loot formula. A hoe (or any other tool) that provides Fortune 10 will therefore roll between one and eleven items each time. The only practical limit is whichever level you assign in code, though individual loot tables can choose to ignore Fortune or apply their own caps.

Many crops and modded harvestables also respect Fortune. Carrots, potatoes, and glow berries all perform extra rolls that increase their stack size in the same way, while seeds from wheat gain additional chances to drop. Because the Ruby Hoe is hard-wired to provide Fortune level 5, harvesting fortune-aware plants with it can produce up to six items per block, making it ideal for squeezing every last crop or ore out of a field.
