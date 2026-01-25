# Crow Mob and Warding Mechanic Design

## Goals
- Add a flying crow mob that periodically seeks out and damages crop blocks.
- Introduce a warding mechanic (scarecrow block or protective charm entity) that deters crows within a configurable radius.
- Keep implementation modular to allow reuse of AI goals, data assets, and warding behavior for future hostile wildlife.

## High-Level Architecture

### Packages / Namespaces
- `net.jeremy.gardenkingmod.entity.crow`
  - `CrowEntity`: custom `PathAwareEntity` (or `MobEntity`) subclass with flight controls.
  - `CrowEntityModel` / `CrowEntityRenderer`: client-side rendering layer.
  - `CrowAiGoals`: helper class containing reusable `Goal` implementations (e.g., `CircleCropGoal`, `RandomPerchGoal`).
- `net.jeremy.gardenkingmod.block.ward`
  - `ScarecrowBlock`: ward block providing deterrence aura.
  - `ScarecrowBlockEntity`: tracks aura state, cooldowns, and optionally durability.
  - `ScarecrowAuraComponent`: server-side utility for ward radius calculations.
- `net.jeremy.gardenkingmod.registry`
  - Follow existing patterns such as `ModBlocks`, `ModItems`, and the planned `ModEntities` for registering crow and warding assets (sound events, loot tables, etc.) so new code slots cleanly into the current architecture.

### Resources
- Models: `src/main/resources/assets/gardenkingmod/models/entity/crow.json` (GeckoLib or vanilla format).
- Textures: `src/main/resources/assets/gardenkingmod/textures/entity/crow.png` and ward block textures under `textures/block/`.
- Animations (if GeckoLib is used): `assets/gardenkingmod/animations/crow.animation.json`.
- Sounds: `assets/gardenkingmod/sounds/crow_call.ogg`, `crow_flap.ogg`, etc.; referenced in `sounds.json`.

> **Texture Reminder:** add the crow texture PNG under `src/main/resources/assets/gardenkingmod/textures/entity/crow.png` and ward block textures under `textures/block/`. (Per user instruction, do not auto-generate these files.)

## Crow Entity Specification

### Core Attributes
- **Health:** 10 HP (configurable via JSON or gamerule).
- **Armor:** 0 by default; allow modifications through data-driven attributes.
- **Movement:** Custom flight movement control with strafing and hovering (similar to parrots or ghasts but faster).
- **Spawn Weight:** Controlled by biome tags (e.g., `gardenkingmod:spawns_crows`).

### AI Goal Stack (ordered by priority)
1. **FleeWardingGoal:** High priority; checks for nearby active ward auras and navigates away.
2. **BreakCropGoal:** Seek farmland crops when hunger timer elapses; break block, eat item (remove or drop seeds).
3. **RandomFlightGoal:** Wander within sky-level Y-range; avoid ground obstacles.
4. **PerchAndObserveGoal:** Land on fences, trees, or scarecrows when idle (optional flavor).
5. **LookAroundGoal / LookAtEntityGoal:** Standard ambiance behavior.
6. **MeleeAttackGoal (optional):** Quick peck against players or golems if aggravated.

### Crop Targeting Workflow
- Maintain a server-side hunger timer (e.g., 600–1200 ticks). When zero, switch to `BreakCropGoal` state.
- `BreakCropGoal` steps:
  1. Search within configurable radius for mature crop blocks (tag `minecraft:crop` or custom tag `gardenkingmod:crow_targets`).
  2. Use a `BlockPos` predicate to ensure mature state (e.g., `CropBlock.isMature`).
  3. Pathfind to block (3D path, use `BirdNavigation`).
  4. Upon reaching, trigger animation/sound, call `world.breakBlock(pos, dropLoot)`.
  5. Reset hunger timer; optionally store `lastCropBreakTime` to throttle frequency.

### Interactions and Drops
- Loot Table: seeds, feathers, rare trinkets (configurable).
- Sounds: idle caws, wing flaps, crop destruction (custom sound events).
- Breeding/Taming: optionally disable; if enabled, use shiny objects or seeds.

### Data & Config
- Add data-driven settings in `config/gardenkingmod/crow.json` with fields: spawn weight, hunger timer range, ward fear radius multiplier, etc.
- Optional gamerule: `gardenkingmod:crowGriefing` toggles crop breaking.

## Warding Mechanic Specification

### Option A: Scarecrow Block
- **Registration:** Custom block with block entity to store aura state and inventory (for upgrades, e.g., charm items).
- **Placement Rules:** Must be placed on farmland/grass; stands two blocks tall (use multi-part block or model).
- **Aura Logic:**
  - Base radius: 12 blocks horizontally, 8 blocks vertically; scaled by upgrades.
  - Each tick, the block entity checks for `CrowEntity` instances within radius.
  - Applies `StatusEffects.SLOWNESS` or custom `Warded` effect to crows; triggers their `FleeWardingGoal`.
  - Aura pulses every 40 ticks to limit performance impact.
- **Durability:** Optional—decrease durability when crow attempts to attack; break when zero.
- **Upgrades:** Accepts talismans or food offerings in internal inventory to increase aura duration or radius.

#### Implementation knobs
- `ScarecrowAuraComponent` owns the core ward parameters, including `BASE_HORIZONTAL_RADIUS`, `BASE_VERTICAL_RADIUS`, `PULSE_INTERVAL_TICKS`, and `PULSE_DURATION_TICKS`, which define the base aura shape and pulse cadence.
- `ScarecrowBlockEntity` wires the aura into gameplay: `tick` advances pulses, `MAX_DURABILITY` and `onCrowRepelled` handle durability drain, `getUpgradeRadiusBonus` / `getUpgradeVerticalBonus` / `getUpgradeLevel` apply inventory-based upgrades, and `isValidUpgrade` plus `getMaxCountPerStack` enforce allowed items and stack limits.
- `CrowFleeWardingGoal` queries `ScarecrowAuraComponent.findNearestActiveAura` to decide when crows should flee.
- `ScarecrowBlock` exposes the `POWERED` redstone property; `ScarecrowBlockEntity.isAuraActive` checks it so redstone power disables the aura.

To adjust gameplay tuning:
- **Base radii:** update `BASE_HORIZONTAL_RADIUS` / `BASE_VERTICAL_RADIUS` in `ScarecrowAuraComponent`.
- **Pulse interval/duration:** change `PULSE_INTERVAL_TICKS` and `PULSE_DURATION_TICKS` in the same component, and ensure any cooldown math in `ScarecrowAuraComponent.tick` still behaves as expected.
- **Durability drain & cap:** modify `MAX_DURABILITY` or the decrement logic in `ScarecrowBlockEntity.onCrowRepelled`.
- **Upgrade items & limits:** tweak `ScarecrowBlockEntity.isValidUpgrade`, `getUpgradeRadiusBonus`, `getUpgradeVerticalBonus`, `getUpgradeLevel`, and `getMaxCountPerStack` to match the desired item tags, scaling, and stack caps.
- **Redstone disabling:** the aura respects the `POWERED` flag set in `ScarecrowBlock`; adjust `ScarecrowBlockEntity.isAuraActive` or block state updates if you want different redstone behavior.

> **Texture placement:** keep scarecrow block textures under `src/main/resources/assets/gardenkingmod/textures/block/`. No PNGs are auto-generated, so add the artwork manually.

### Option B: Protective Charm Entity
- **Entity Type:** Stationary, invisible entity spawned by block placement or thrown item.
- **Behavior:** Emits aura, can move slowly or hover; uses `LivingEntity` or `AreaEffectCloud` subclass for built-in radius handling.
- **Advantages:** Simplifies aura updates, but requires lifetime management.

### Client Presentation
- Custom model and texture for scarecrow block (`models/block/scarecrow.json`, `textures/block/scarecrow.png`).
- Particle effects (e.g., small light wisps) when aura repels crows.
- Optional UI overlay when interacting (opens upgrade inventory).

## Systems Integration Workflow

1. **Data Preparation**
   - Define biome tags (`data/gardenkingmod/tags/worldgen/biome/spawns_crows.json`).
   - Add loot tables for crow and scarecrow block.
   - Update language files with new strings.

2. **Entity Registration**
   - Register `EntityType<CrowEntity>` with Fabric API (Server + Client initializer hooks).
   - Set default attributes using `FabricDefaultAttributeRegistry`.
   - Register renderer via `EntityRendererRegistry.register` in `ClientModInitializer`.

3. **AI and Behavior Implementation**
   - Implement `CrowEntity` using `BirdNavigation` and custom flight control.
   - Add `Goal` classes: `CrowBreakCropGoal`, `CrowFleeWardingGoal`, `CrowRandomFlyGoal`, `CrowPerchGoal`.
   - Hook into Fabric events for crop break logging (optional).

4. **Crop Interaction Logic**
   - Create block tag for crow targets.
   - Ensure `CrowBreakCropGoal` verifies block state maturity and handles loot dropping.
   - Optionally, integrate with Farmer villagers or golems (e.g., set them to target crows).

5. **Warding Block/Entity Implementation**
   - Register block, item, block entity type.
   - Implement aura tick method: iterate through world’s `CrowEntity` list using `world.iterateEntities` or `ServerWorld.getEntitiesByClass` with bounding box.
   - Provide data tracker or component for aura intensity; crows read this to adjust flee distance.
   - Add upgrades/inventory using `BlockEntity` + `ScreenHandler` if needed.

6. **Client Rendering**
   - Create block model states for scarecrow, item model for block item, and entity renderer for crow (flapping wings animation).
   - Provide `AnimationController` definitions if using GeckoLib.

7. **Sound & Particle Effects**
   - Register custom sound events and hook into crow actions (idle, flap, crop break, warded shriek).
   - Add particle emission on ward aura pulses (use `world.addParticle`).

8. **Testing & Balancing**
   - Unit tests for goal activation (if using Fabric’s test harness) or manual integration testing.
   - Verify spawn rates in different biomes.
   - Check that ward aura reduces crow crop destruction frequency to acceptable levels.

## Future Extensions
- Seasonal behavior (higher activity during harvest season events).
- Crow flock AI using `FlockLeaderGoal`/`FlockMemberGoal` style patterns.
- Player-crafted bait to lure crows away from fields.
- Integration with advancement system for deterring crows.

## Development Workflow Checklist
- [ ] Define data tags and config files.
- [ ] Implement crow entity class and register.
- [ ] Write AI goals and integrate with navigation.
- [ ] Add renderer/model/texture placeholders.
- [ ] Implement scarecrow block/entity with aura logic.
- [ ] Hook up sound events, particles, loot tables.
- [ ] Create advancements and recipes.
- [ ] Perform gameplay testing and iterate on balance.

