package net.jeremy.gardenkingmod.registry;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.entity.crow.CrowBalanceConfig;
import net.jeremy.gardenkingmod.entity.crow.CrowEntity;
import net.jeremy.gardenkingmod.entity.crow.CrowTags;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;

/**
 * Registers all custom entity types and related game rules.
 */
public final class ModEntities {
    public static final GameRules.Key<GameRules.BooleanRule> CROW_GRIEFING_RULE = GameRuleRegistry.register(
            "crowGriefing", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));

    public static final EntityType<CrowEntity> CROW = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(GardenKingMod.MOD_ID, "crow"),
            FabricEntityTypeBuilder.<CrowEntity>create(SpawnGroup.CREATURE, CrowEntity::new)
                    .dimensions(EntityDimensions.changing(0.6f, 0.8f)).trackRangeBlocks(80).build());

    private ModEntities() {
    }

    public static void register() {
        CrowBalanceConfig.reload();
        CrowBalanceConfig config = CrowBalanceConfig.get();
        FabricDefaultAttributeRegistry.register(CROW, CrowEntity.createCrowAttributes());
        SpawnRestriction.register(CROW, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                CrowEntity::canSpawn);
        // Datapacks can edit data/gardenkingmod/tags/worldgen/biome/spawns_crows.json to change where crows spawn.
        BiomeModifications.addSpawn(BiomeSelectors.tag(CrowTags.CROW_SPAWN_BIOMES), SpawnGroup.CREATURE, CROW,
                config.spawnWeight(), config.minSpawnGroupSize(), config.maxSpawnGroupSize());
        GardenKingMod.LOGGER.info("Registered crow entity with config {}", config);
    }
}
