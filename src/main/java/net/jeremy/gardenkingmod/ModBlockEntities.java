package net.jeremy.gardenkingmod;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.jeremy.gardenkingmod.block.entity.BankBlockEntity;
import net.jeremy.gardenkingmod.block.entity.GardenOvenBlockEntity;
import net.jeremy.gardenkingmod.block.entity.GearShopBlockEntity;
import net.jeremy.gardenkingmod.block.entity.MarketBlockEntity;
import net.jeremy.gardenkingmod.block.sprinkler.SprinklerBlockEntity;
import net.jeremy.gardenkingmod.block.ward.ScarecrowBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
        public static final BlockEntityType<MarketBlockEntity> MARKET_BLOCK_ENTITY = Registry.register(
                        Registries.BLOCK_ENTITY_TYPE, new Identifier(GardenKingMod.MOD_ID, "market_block"),
                        FabricBlockEntityTypeBuilder.create(MarketBlockEntity::new, ModBlocks.MARKET_BLOCK).build());

        public static final BlockEntityType<GearShopBlockEntity> GEAR_SHOP_BLOCK_ENTITY = Registry.register(
                        Registries.BLOCK_ENTITY_TYPE, new Identifier(GardenKingMod.MOD_ID, "gear_shop_block"),
                        FabricBlockEntityTypeBuilder.create(GearShopBlockEntity::new, ModBlocks.GEAR_SHOP_BLOCK).build());

        public static final BlockEntityType<ScarecrowBlockEntity> SCARECROW_BLOCK_ENTITY = Registry.register(
                        Registries.BLOCK_ENTITY_TYPE, new Identifier(GardenKingMod.MOD_ID, "scarecrow"),
                        FabricBlockEntityTypeBuilder.create(ScarecrowBlockEntity::new, ModBlocks.SCARECROW_BLOCK).build());

        public static final BlockEntityType<SprinklerBlockEntity> SPRINKLER_BLOCK_ENTITY = Registry.register(
                        Registries.BLOCK_ENTITY_TYPE, new Identifier(GardenKingMod.MOD_ID, "sprinkler"),
                        FabricBlockEntityTypeBuilder.create(SprinklerBlockEntity::new, ModBlocks.IRON_SPRINKLER_BLOCK,
                                        ModBlocks.GOLD_SPRINKLER_BLOCK, ModBlocks.DIAMOND_SPRINKLER_BLOCK,
                                        ModBlocks.EMERALD_SPRINKLER_BLOCK).build());

        public static final BlockEntityType<BankBlockEntity> BANK_BLOCK_ENTITY = Registry.register(
                        Registries.BLOCK_ENTITY_TYPE, new Identifier(GardenKingMod.MOD_ID, "bank_block"),
                        FabricBlockEntityTypeBuilder.create(BankBlockEntity::new, ModBlocks.BANK_BLOCK).build());

        public static final BlockEntityType<GardenOvenBlockEntity> GARDEN_OVEN_BLOCK_ENTITY = Registry.register(
                        Registries.BLOCK_ENTITY_TYPE, new Identifier(GardenKingMod.MOD_ID, "garden_oven"),
                        FabricBlockEntityTypeBuilder.create(GardenOvenBlockEntity::new, ModBlocks.GARDEN_OVEN_BLOCK).build());

        private ModBlockEntities() {
        }

        public static void registerBlockEntities() {
                GardenKingMod.LOGGER.info("Registering block entities for {}", GardenKingMod.MOD_ID);
        }
}
