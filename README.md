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
