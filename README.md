# Garden King Mod

## Bonus harvest drops

Garden King now loads bonus crop drop definitions from JSON files under `data/gardenkingmod/bonus_harvest_drops/`. Each file maps a crop block or direct loot table identifier to one or more bonus entries with an `item`, `chance`, and integer `count` range. The new [`wheat_diamonds.json`](src/main/resources/data/gardenkingmod/bonus_harvest_drops/wheat_diamonds.json) file, for example, grants wheat a 5% chance to drop 1–3 diamonds when harvested.

### Creating event packs

Seasonal or event datapacks can override or replace these bonus drops without code changes:

1. Create a datapack with the `gardenkingmod` namespace.
2. Add JSON definitions under `data/gardenkingmod/bonus_harvest_drops/` mirroring the structure above.
3. Reload the server with `/reload` or restart to apply the new loot behaviour.

The repository ships an [example event configuration](src/main/resources/data/gardenkingmod_event_example/bonus_harvest_drops/wheat_diamonds.json) that boosts wheat to drop emeralds more frequently. Copy this structure into a datapack and adjust it for real events.

### Asset reminders

If an event introduces new items, remember to include their textures in `assets/gardenkingmod/textures/item/`. Place the matching `.png` files in that directory so Fabric can load them correctly.

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
