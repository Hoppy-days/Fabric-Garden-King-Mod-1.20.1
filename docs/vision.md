# Garden King – Vision & Design Document

*Version: SMP Launch (v0.9)*

---

## 1. Player Overview

### What Is Garden King?

Garden King is a **farming-first progression mod**.

Instead of mining being the only way to “go endgame”, Garden King turns
farming, cooking, and selling crops into a full progression path:

> Plant crops → manage growth & threats → harvest → sell → buy powerful gear.

You’ll:
- Grow and harvest **tiered crops** with different risks and rewards.
- Deal with **crows** that try to destroy your fields.
- Use **sprinklers, scarecrows, and fertilizer** to automate and protect.
- Turn crops into **cooked meals** in a Garden Oven for more profit.
- Earn **Garden Dollars**, store them in a **wallet** or **bank**, and
  spend them at a **gear shop** on unique tools and armor.
- Gain **harvest XP**, level up, and specialize in skills like Chef or Enchanter.

### Core Fantasy

You are not just a farmer.

You are the **Garden King** – a specialist producer who can rival
miners and adventurers purely through the power of your fields.

---

## 2. Core Features (Player-Facing)

### 2.1 Currency & Economy

- **Garden Dollars** – the main currency of the mod.
- **Wallet** – bound to a specific player; shows your current balance and
  opens your bank remotely.
- **Bank Block** – in-world interface to deposit and withdraw dollars.
- **Market Block** – sell crops and other allowed items for money.
- **Gear Shop Block** – spend money on powerful tools, weapons, and armor.
- Your earnings are tracked as:
  - **Bank Balance** – how much you currently have.
  - **Lifetime Earnings** – how much you’ve earned in total.

### 2.2 Crops, Tiers, & Quality

All crops are assigned to a **tier** (1–5). Tiers affect:

- Growth speed
- Drop multipliers (how many items you get)
- Chance of a **no-drop** (crop fails)
- Chance of a **rotten crop**
- Chance of an **enchanted crop**

On harvest you might get:

- Normal crop (most of the time)
- **Rotten crop** – a bad outcome, often used for compost/fertilizer.
- **Enchanted crop** – rare, high-value version of the crop.

You can also **right-click harvest** mature crops to collect drops and replant
automatically. Using a hoe respects enchantments like Fortune.

### 2.3 Threat: Crows & Scarecrows

- **Crows**:
  - Spawn in suitable biomes.
  - Periodically get hungry and actively hunt mature crops.
  - Fly in, break the crop (optionally dropping the items), and reset hunger.
- **Scarecrows**:
  - Provide a **warding aura** that crows avoid.
  - Place them strategically to cover your fields and create safe zones.

Servers can configure this with a **`crowGriefing` gamerule** and
spawn/balance settings.

### 2.4 Infrastructure: Sprinklers & Fertilizer

- **Sprinklers** (Iron, Gold, Diamond, Emerald tiers):
  - Hydrate farmland in a radius, replacing the need for nearby water.
  - Higher tiers cover larger areas.
- **Compost / Special Fertilizer**:
  - Acts like a custom bonemeal with its own success chance.
  - Lets you convert bad outcomes (like rotten crops) into growth acceleration.

### 2.5 Cooking: Garden Oven

- **Garden Oven**:
  - A dedicated cooking block for complex meals (especially Croptopia recipes).
  - Accepts up to 9 ingredients in patterns similar to crafting recipes.
  - Uses time-based cooking instead of instant crafting.
  - Some recipes award experience when finished.

High-tier meals sell better at the market and can provide strong
food effects.

### 2.6 Skills & Progression

- Harvesting crops grants **Harvest XP**, scaled by crop tier.
- As you gain XP, you **level up** and earn **skill points**.
- Skill points will be spendable in specializations such as:
  - **Chef Mastery** – focused on cooking and food value.
  - **Enchanter** – focused on enchanted crop outcomes.

> **SMP launch note:** The basic XP and leveling system is active.
> Deeper Chef/Enchanter effects are being expanded over time.

### 2.7 Gear & Armor Sets

Garden King adds multiple new material sets, each with its own identity:

- **Amethyst**
- **Blue Sapphire**
- **Emerald**
- **Obsidian**
- **Pearl**
- **Ruby**
- **Topaz**

Each material can have:

- Full toolset (sword, pickaxe, axe, shovel, hoe).
- Full armor set (helmet, chestplate, leggings, boots).
- **Built-in enchantments and attribute bonuses** defined by data.
- **Full-set armor bonuses** (for example):
  - Movement, mining, or combat buffs.
  - Trade-, luck-, or defensive-focused bonuses.
  - Some sets add tradeoffs like Slowness in exchange for being extra tanky.

These items are primarily acquired via the **Gear Shop** using Garden Dollars.

---

## 3. Progression Overview (Player-Facing)

1. Start with basic crops and vanilla tools.
2. Sell harvests at the **Market** to earn your first Garden Dollars.
3. Use the **Wallet** and **Bank** to manage your money.
4. Begin investing in:
   - **Sprinklers** to automate hydration.
   - **Scarecrows** to defend your fields.
   - **Fertilizer** to speed up growth.
5. Unlock the **Garden Oven** to turn crops into profitable meals.
6. Grind towards your first **upgraded tools and armor** from the Gear Shop.
7. Level up your **farming skills**, eventually specializing into
   Chef, Enchanter, or both.
8. Expand your farm, manage crows, and iterate on builds to become
   a true Garden King.

---

## 4. Server Owner Quick Start (SMP)

Recommended settings for an SMP:

- Ensure the **client and server modpacks** match
  (Fabric loader, Garden King, Croptopia, libraries).
- Review and tweak:
  - `garden_market_offers.json` – sell prices and eligible items.
  - `gear_shop_offers.json` – available gear and prices.
  - `harvest_xp.json` – XP per crop tier.
  - Sprinkler & scarecrow radius configs.
  - Crow spawn and behavior configs.

Optional gamerules / balancing:

- `gamerule crowGriefing true` – full challenge (crows break crops).
- `gamerule crowGriefing false` – crows become ambient and harmless.

Plan on doing a short **test season** with a small group to see how
the economy feels, then adjust sell prices and gear costs as needed.

---

## 5. Dev-Facing Design Notes

### 5.1 Design Pillars

1. **Farming as a primary progression path**
   - A fully-supported alternative to mining/raiding as “endgame”.

2. **Systems over content bloat**
   - Fewer, deeper systems: tiers, quality, skills, threats, and economy.

3. **Data-driven where possible**
   - Loot, offers, crop tiers, tool buffs, and oven recipes are defined in JSON.

4. **Multiplayer friendly**
   - Wallets + bank and scoreboard integration.
   - Crows and scarecrows create emergent farm defense gameplay.

5. **Extensibility**
   - New crops or foods (from other mods) can join the system via tags/JSON.
   - New gear paths can be added via gear shop offers and tool buff definitions.

### 5.2 Implementation Sketch (High-Level)

> This section is for contributors. Class names and paths refer to
> the Java source in `src/main/java/net/jeremy/gardenkingmod`.

- **Initialization & Registries**
  - Main mod init: block/item registration, data loader hookup.
  - Data managers for:
    - Crop tiers
    - Rotten crops
    - Enchanted crops
    - Bonus harvest drops
    - Tool buffs
    - Market offers
    - Gear shop offers

- **Economy**
  - Currency tracking via player-attached data + scoreboard.
  - Wallet item with owner UUID & bank access.
  - Bank and Market blocks with their own containers and screens.
  - Gear Shop block with tabbed UI and data-driven offers.

- **Crops & Loot**
  - Loot modifier or injection for adjusting crop drops by tier.
  - Helpers for applying rotten/enchanted drops.
  - Right-click harvest handler wiring into loot and XP logic.

- **Threats & Wards**
  - Crow entity & AI goals for:
    - Wandering / perching
    - Crop search & break behavior
    - Fleeing from scarecrows
  - Crow-related configs for hunger, search radius, and spawn rates.
  - Scarecrow block entity with radius and tag-based upgrades.
  - Custom gamerule for `crowGriefing`.

- **Infrastructure**
  - Sprinkler blocks & block entities.
  - Hydration manager and farmland hooks.
  - Fertilizer item logic with config-defined growth chance.

- **Cooking**
  - Garden Oven block + block entity + screen & container.
  - Custom recipe type + serializer.
  - JSON overrides for Croptopia recipes.

- **Skills**
  - Player capability / interface for XP, level, and skill points.
  - XP service that awards XP by crop tier and recognizes rotten/enchanted.
  - Client sync via networking.
  - Skill HUD & potential skill screen for spending points.

- **Gear**
  - Tool & armor materials for each new set.
  - Tool buff system that reads JSON and applies enchantments & attributes.
  - Armor set effect logic that checks full sets and applies potion effects.

### 5.3 Near-Term Roadmap (Post SMP Launch)

**Short-term (1.0 polish):**

- Implement Enchanter skill impact on enchanted crop chance.
- Implement Chef Mastery impact on Garden Oven (cook speed, bonus yield, etc.).
- Finalize scarecrow upgrades (hats/head/shirt/pitchfork) and document their effects.
- Clean up gem usage (worldgen vs shop-only vs repair-only).
- Confirm all new items/blocks have textures and localization keys.

**Medium-term ideas:**

- Add more bonus harvest drops and seasonal events.
- Add a few “signature” Garden King oven recipes for very high profit.
- Consider a late-game “Farm Estate” concept (structures, titles, or extra perks).

---

## 6. Contributor Expectations

When contributing:

- Favor **data-driven** approaches.
- Keep **multiplayer balance** in mind (no infinite money loops).
- Respect the **core fantasy**:
  - If a feature doesn’t touch farming, economy, cooking, or farm defense,
    it probably doesn’t belong in this mod.
