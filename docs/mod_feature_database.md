# Garden King Mod Feature Database

## Purpose
This document is a single source of truth for the current feature set, data-driven content, and polish backlog for the Garden King mod. It is written to help a new mod developer track what exists today and what remains to be finished before adding new features.

## Mod identity + entrypoints
- **Mod ID:** `gardenkingmod`.
- **Entrypoints:** `GardenKingMod` (server/common), `GardenKingModClient` (client), plus data generation and game tests.
- **Dependencies:** Fabric API, Minecraft 1.20.1, and Croptopia.

## Feature database

### 1) Economy + currency system
**Core concept:** a “Garden Dollar” economy with persistent balances and multiple interaction surfaces.

**Data + storage**
- Currency values are persisted on the player (NBT + scoreboard fallbacks).
- A `GardenCurrencyHolder` capability persists lifetime earnings and bank balance.

**Gameplay surfaces**
- **Wallet item** ties to the player and exposes bank balance; it can open bank screens remotely and sync balances. 
- **Bank block** provides in-world access to a bank UI.
- **Market block** (sell UI) converts crops and allowed items into dollars and XP.

**Key implementation files**
- Currency data and persistence: `GardenCurrencyHolder`, `PlayerEntityMixin`, `ModScoreboards`.
- Wallet item: `WalletItem`.
- Bank block entity/UI flow: `BankBlockEntity`, `BankScreenHandler`.
- Market block entity/UI flow: `MarketBlockEntity`, `MarketScreenHandler`.

**Data-driven pieces**
- Market buy offers: `data/gardenkingmod/garden_market_offers.json`.
- Market sell restrictions: `data/gardenkingmod/tags/items/market_unsellable.json`.

**Polish notes**
- Market sellable logic currently allows Minecraft/Croptopia items and enchanted crops; anything else is rejected.

---

### 2) Crop tiers, harvest drops, and “enchanted/rotten” conversions
**Core concept:** crops are grouped into tiers (1–5), which drive growth speed, drop multipliers, and special outcomes (no-drop, rotten, enchanted). Loot tables are dynamically patched at runtime.

**Core mechanics**
- **Tier tags:** block and item tags map crops into tiers.
- **Drop scaling:** tier-based scaling is applied to loot tables, plus optional no-drop/rotten/enchanted conversions.
- **Bonus harvest drops:** extra rolls defined in JSON for seasonal/event loot tweaks.
- **Right-click harvesting:** mature crops can be harvested without breaking the block (tools take durability).

**Key implementation files**
- Tier system: `CropTier`, `CropTierRegistry`.
- Loot patching + conversions: `CropDropModifier`.
- Bonus drop loader: `BonusHarvestDropManager`.
- Right-click harvest handling: `RightClickHarvestHandler`.

**Data-driven pieces**
- Tier tags: `data/gardenkingmod/tags/blocks/crop_tiers/tier_*.json` and `data/gardenkingmod/tags/items/crop_tiers/tier_*.json`.
- Enchanted crop definitions: `data/gardenkingmod/enchanted_crops.json`.
- Rotten crop definitions: `data/gardenkingmod/rotten_crops.json`.
- Bonus harvest drops: `data/gardenkingmod/bonus_harvest_drops/*.json`.

**Polish notes**
- Enchanted/rotten crops are data-driven, but you still need textures for each new item under `assets/gardenkingmod/textures/item/`.

---

### 3) Skill progression + Harvest XP
**Core concept:** harvesting crops grants XP that drives skill levels, with skill points spent on specializations.

**Core mechanics**
- **XP mapping by tier:** per-tier harvest XP values loaded from `config/gardenkingmod/harvest_xp.json`.
- **Skills:** Chef Mastery and Enchanter (Enchanter increases enchanted crop chances).
- **Client HUD/UI:** skill overlay + skill screen, plus networking to keep server/client state in sync.

**Key implementation files**
- Skill definitions + level thresholds: `SkillProgressManager`.
- XP awarding: `HarvestXpService`.
- Persistent skill data: `SkillProgressHolder`, `PlayerEntityMixin`.
- Client UI + HUD: `SkillScreen`, `SkillHudOverlay`, and client networking in `GardenKingModClient`.

---

### 4) Crow mob + scarecrow warding
**Core concept:** a crow mob hunts crops; scarecrows and warding mechanics deter them.

**Core mechanics**
- **Crow AI:** hunger-driven crop hunting, random flight, perching, and ward fleeing.
- **Warding:** scarecrow block emits an aura and can be upgraded by equipping items.
- **Config:** spawn rates, hunger timing, ward radius, and other balance knobs are configurable.

**Key implementation files**
- Crow entity and goals: `CrowEntity`, `CrowAiGoals`.
- Crow config: `CrowBalanceConfig`.
- Scarecrow block and aura logic: `ScarecrowBlock`, `ScarecrowBlockEntity`, `ScarecrowAuraComponent`.
- Crow tags: `CrowTags`.

**Data-driven pieces**
- Crow spawn biomes: `data/gardenkingmod/tags/worldgen/biome/spawns_crows.json`.
- Scarecrow equipment tags: `data/gardenkingmod/tags/items/scarecrow_hats.json`, `scarecrow_heads.json`, `scarecrow_shirts.json`.

**Polish notes**
- There is a tag key for `scarecrow_pitchforks` in code but no corresponding tag JSON file yet. Add `data/gardenkingmod/tags/items/scarecrow_pitchforks.json` to make pitchfork upgrades functional.
- Place scarecrow textures in `assets/gardenkingmod/textures/block/` and crow textures in `assets/gardenkingmod/textures/entity/` (do not auto-generate PNGs).

---

### 5) Sprinklers + farmland hydration
**Core concept:** multi-tier sprinkler blocks keep farmland hydrated inside a tier-defined radius.

**Core mechanics**
- **Sprinkler tiers:** iron, gold, diamond, emerald, each with increasing radii.
- **Hydration hook:** farmland moisture checks consider active sprinkler coverage.

**Key implementation files**
- Sprinkler block + entity: `SprinklerBlock`, `SprinklerBlockEntity`.
- Tier stats: `SprinklerTier`.
- Hydration registry: `SprinklerHydrationManager`.
- Farmland hook: `FarmlandBlockMixin`.

**Polish notes**
- Sprinkler entity textures should live in `assets/gardenkingmod/textures/entity/sprinkler/`.

---

### 6) Garden Oven + custom recipes
**Core concept:** a custom oven block that handles cooking recipes for Croptopia meals and Garden King content.

**Core mechanics**
- **Custom recipe type:** `gardenkingmod:garden_oven`.
- **Balance config:** default cook time is `config/gardenkingmod/garden_oven.json`.
- **Slot layout:** 9 input slots + 1 output slot.

**Key implementation files**
- Garden oven block/entity: `GardenOvenBlock`, `GardenOvenBlockEntity`.
- Recipe registration: `ModRecipes`, `GardenOvenRecipe`.
- Balance config: `GardenOvenBalanceConfig`.

**Data-driven pieces**
- Croptopia recipe overrides under `data/croptopia/recipes/`.

**Polish notes**
- Remember to put any new cooked-food textures in `assets/<namespace>/textures/item/`.

---

### 7) Gear Shop + equipment economy
**Core concept:** a four-tab gear shop UI that sells Garden King gear using data-defined offers.

**Core mechanics**
- **Offer definitions:** `gear_shop_offers.json` supports paged offers, item stacks, and multi-item prices.
- **UI pages:** capped at 4 pages (extra pages are ignored).

**Key implementation files**
- Offer loader: `GearShopOfferManager`.
- Offer model helpers: `GearShopOffer`, `GearShopStackHelper`.
- UI + block entity: `GearShopBlockEntity`, `GearShopScreenHandler`, `GearShopScreen`.

**Data-driven pieces**
- Gear shop offers: `data/gardenkingmod/gear_shop_offers.json`.

---

### 8) Items, tools, armor, blocks
**High-level item families**
- **Currency:** dollar, wallet.
- **Materials:** ruby, blue sapphire, topaz, pearl.
- **Tool sets:** ruby/blue sapphire/topaz/pearl/amethyst/obsidian/emerald (sword, pickaxe, axe, shovel, hoe).
- **Armor sets:** ruby/blue sapphire/topaz/pearl/amethyst/obsidian/emerald (helmet, chestplate, leggings, boots).
- **Special items:** special fertilizer, scarecrow cosmetic pieces.
- **Dynamic items:** enchanted crops, rotten crops (data-driven list).

**Block catalog**
- **Functional blocks:** market, gear shop, bank, scarecrow, sprinklers (iron/gold/diamond/emerald), garden oven.
- **Building block:** ruby block.

**Key implementation files**
- Items: `ModItems` and item material classes in `item/`.
- Blocks: `ModBlocks` and `block/`.
- Block entities: `ModBlockEntities` and `block/entity/`.

**Data-driven pieces**
- Recipes: `data/gardenkingmod/recipes/*.json`.
- Item models and textures: `assets/gardenkingmod/models/item/` and `assets/gardenkingmod/textures/item/`.
- Block models and textures: `assets/gardenkingmod/models/block/` and `assets/gardenkingmod/textures/block/`.

---

### 9) Sounds, particles, UI, and client rendering
**Core concept:** custom audio, particles, custom block/entity renderers, and UI screens.

**Key implementation files**
- Sound registration: `ModSoundEvents`, `assets/gardenkingmod/sounds.json` + `.ogg` files.
- Renderers: classes under `client/render` and `client/model`.
- UI screens: classes under `screen/` and `client/gui/`.
- HUD overlay: `SkillHudOverlay`.

---

## Known gaps / risk areas
1. **Missing scarecrow pitchfork tag** (code expects it, data missing). Add a tag JSON file so pitchfork upgrades are possible.
2. **Large dynamic crop lists** (rotten/enchanted) require exhaustive textures and localization strings; ensure every generated item has assets + lang entries.
3. **Data-driven balances** (crow, fertilizer, harvest XP, oven): verify defaults are aligned with gameplay goals.

---

## Actionable polish plan (before adding new features)

### Phase 1: Confirm core loops are complete (1–2 days)
1. **Economy loop sanity pass**
   - Verify market sell -> dollars -> wallet -> bank loop works in one play session.
   - Confirm lifetime currency and bank balances persist across relogs.
2. **Harvest loop sanity pass**
   - Test right-click harvesting (hoe durability, drops, XP).
   - Confirm rotten/enchanted crops appear at expected rates.
3. **Crow/scarecrow sanity pass**
   - Validate crow spawns in tagged biomes.
   - Confirm scarecrow aura works, durability ticks down, and redstone disables it.

### Phase 2: Fill data gaps + missing assets (1–3 days)
1. **Add missing scarecrow pitchfork tag file**
   - Create `data/gardenkingmod/tags/items/scarecrow_pitchforks.json` and decide which items count.
2. **Texture + lang sweep**
   - For every enchanted/rotten crop entry, verify the corresponding texture exists at:
     - `assets/gardenkingmod/textures/item/<item_id>.png`
   - Update language entries in `assets/gardenkingmod/lang/*.json` as needed.
3. **Crow assets**
   - Ensure crow texture exists at `assets/gardenkingmod/textures/entity/crow.png`.
   - Verify sound events referenced in `sounds.json` exist.

### Phase 3: Balance + content tuning (2–4 days)
1. **Tune crop tiers and loot scaling**
   - Adjust `CropTierRegistry` multipliers or tier tags for gameplay pacing.
2. **Tune skill XP curves**
   - Update `config/gardenkingmod/harvest_xp.json` and verify skill progression feels fair.
3. **Tune oven cooking times**
   - Adjust `config/gardenkingmod/garden_oven.json` and recipe cooking times for pacing.
4. **Tune crow aggression**
   - Adjust `config/gardenkingmod/crow.json` for crop damage frequency and ward radius.

### Phase 4: UX + stability cleanup (1–2 days)
1. **UI consistency pass**
   - Check Gear Shop and Market screens for layout issues and missing textures.
2. **Edge-case checks**
   - Sell empty/invalid items in Market; verify message feedback.
   - Interact with scarecrow without valid equipment; ensure UI restricts items properly.

### Phase 5: Release readiness checklist (1 day)
1. **Regression pass**
   - Build/test in a fresh world with only required mods (Fabric + Croptopia).
2. **Docs update**
   - Update README with any new configuration defaults or changes.
3. **Pack distribution**
   - Verify all assets and data files are included before packaging.

---

## Quick reference: files you will touch most while polishing
- **Data configs:** `config/gardenkingmod/crow.json`, `fertilizer.json`, `garden_oven.json`, `harvest_xp.json`.
- **Loot + crop data:** `data/gardenkingmod/bonus_harvest_drops/`, `enchanted_crops.json`, `rotten_crops.json`.
- **Shops:** `data/gardenkingmod/gear_shop_offers.json`, `garden_market_offers.json`.
- **Tags:** `data/gardenkingmod/tags/**`.
- **Assets:** `assets/gardenkingmod/textures/`, `assets/gardenkingmod/models/`, `assets/gardenkingmod/lang/`.
