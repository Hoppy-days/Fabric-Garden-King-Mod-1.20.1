package net.jeremy.gardenkingmod;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.jeremy.gardenkingmod.entity.crow.CrowEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
        public static final EntityType<CrowEntity> CROW = Registry.register(Registries.ENTITY_TYPE,
                        new Identifier(GardenKingMod.MOD_ID, "crow"),
                        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, CrowEntity::new)
                                        .dimensions(EntityDimensions.changing(0.6f, 0.9f))
                                        .trackRangeBlocks(80)
                                        .trackedUpdateRate(3)
                                        .build());

        private ModEntities() {
        }

        public static void registerModEntities() {
                GardenKingMod.LOGGER.info("Registering mod entities for {}", GardenKingMod.MOD_ID);
                FabricDefaultAttributeRegistry.register(CROW, CrowEntity.createCrowAttributes());
        }
}
