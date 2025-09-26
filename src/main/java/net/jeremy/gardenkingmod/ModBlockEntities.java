package net.jeremy.gardenkingmod;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.jeremy.gardenkingmod.block.entity.MarketBlockEntity;
import net.jeremy.gardenkingmod.block.ward.ScarecrowBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
        public static final BlockEntityType<MarketBlockEntity> MARKET_BLOCK_ENTITY = Registry.register(
                        Registries.BLOCK_ENTITY_TYPE, new Identifier(GardenKingMod.MOD_ID, "market_block"),
                        FabricBlockEntityTypeBuilder.create(MarketBlockEntity::new, ModBlocks.MARKET_BLOCK).build());

        public static final BlockEntityType<ScarecrowBlockEntity> SCARECROW_BLOCK_ENTITY = Registry.register(
                        Registries.BLOCK_ENTITY_TYPE, new Identifier(GardenKingMod.MOD_ID, "scarecrow"),
                        FabricBlockEntityTypeBuilder.create(ScarecrowBlockEntity::new, ModBlocks.SCARECROW_BLOCK).build());

        private ModBlockEntities() {
        }

        public static void registerBlockEntities() {
                GardenKingMod.LOGGER.info("Registering block entities for {}", GardenKingMod.MOD_ID);
        }
}
