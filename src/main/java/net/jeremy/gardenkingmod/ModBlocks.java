package net.jeremy.gardenkingmod;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.jeremy.gardenkingmod.block.BankBlock;
import net.jeremy.gardenkingmod.block.GardenOvenBlock;
import net.jeremy.gardenkingmod.block.GearShopBlock;
import net.jeremy.gardenkingmod.block.GearShopBlockPart;
import net.jeremy.gardenkingmod.block.MarketBlock;
import net.jeremy.gardenkingmod.block.MarketBlockPart;
import net.jeremy.gardenkingmod.block.sprinkler.SprinklerBlock;
import net.jeremy.gardenkingmod.block.sprinkler.SprinklerTier;
import net.jeremy.gardenkingmod.item.SprinklerBlockItem;
import net.jeremy.gardenkingmod.block.ward.ScarecrowBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlocks {
        public static final Block MARKET_BLOCK = registerBlock("market_block",
                        new MarketBlock(
                                        FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).strength(2.5f).nonOpaque()));

        public static final Block GEAR_SHOP_BLOCK = registerBlock("gear_shop_block",
                        new GearShopBlock(
                                        FabricBlockSettings.copyOf(Blocks.SPRUCE_PLANKS).strength(2.5f).nonOpaque()));

        public static final Block SCARECROW_BLOCK = registerBlock("scarecrow",
                        new ScarecrowBlock(FabricBlockSettings.copyOf(Blocks.HAY_BLOCK).strength(1.5f).nonOpaque()));

        public static final Block IRON_SPRINKLER_BLOCK = registerBlock("iron_sprinkler",
                        new SprinklerBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(1.5f).nonOpaque(),
                                        SprinklerTier.IRON));

        public static final Block GOLD_SPRINKLER_BLOCK = registerBlock("gold_sprinkler",
                        new SprinklerBlock(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).strength(1.5f).nonOpaque(),
                                        SprinklerTier.GOLD));

        public static final Block DIAMOND_SPRINKLER_BLOCK = registerBlock("diamond_sprinkler",
                        new SprinklerBlock(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).strength(2.0f).nonOpaque(),
                                        SprinklerTier.DIAMOND));

        public static final Block EMERALD_SPRINKLER_BLOCK = registerBlock("emerald_sprinkler",
                        new SprinklerBlock(FabricBlockSettings.copyOf(Blocks.EMERALD_BLOCK).strength(2.0f).nonOpaque(),
                                        SprinklerTier.EMERALD));

        public static final Block BANK_BLOCK = registerBlock("bank_block",
                        new BankBlock(FabricBlockSettings.copyOf(Blocks.SPRUCE_PLANKS).strength(2.5f)));

        public static final Block RUBY_BLOCK = registerBlock("ruby_block",
                        new Block(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK)));

        public static final Block GARDEN_OVEN_BLOCK = registerBlock("garden_oven",
                        new GardenOvenBlock(FabricBlockSettings.copyOf(Blocks.BRICKS).strength(3.5f)
                                        .luminance(state -> state.get(GardenOvenBlock.LIT) ? 13 : 0)));

        public static final Block MARKET_BLOCK_PART = registerBlockWithoutItem("market_block_part",
                        new MarketBlockPart(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).dropsNothing().nonOpaque()));

        public static final Block GEAR_SHOP_BLOCK_PART = registerBlockWithoutItem("gear_shop_block_part",
                        new GearShopBlockPart(
                                        FabricBlockSettings.copyOf(Blocks.SPRUCE_PLANKS).dropsNothing().nonOpaque()));

        private ModBlocks() {
        }

        private static Block registerBlock(String name, Block block) {
                registerBlockItem(name, block);
                return Registry.register(Registries.BLOCK, new Identifier(GardenKingMod.MOD_ID, name), block);
        }

        private static Item registerBlockItem(String name, Block block) {
                BlockItem blockItem = block instanceof SprinklerBlock sprinklerBlock
                                ? new SprinklerBlockItem(sprinklerBlock, new FabricItemSettings())
                                : new BlockItem(block, new FabricItemSettings());
                return Registry.register(Registries.ITEM, new Identifier(GardenKingMod.MOD_ID, name), blockItem);
        }

        private static Block registerBlockWithoutItem(String name, Block block) {
                return Registry.register(Registries.BLOCK, new Identifier(GardenKingMod.MOD_ID, name), block);
        }

        public static void registerModBlocks() {
                GardenKingMod.LOGGER.info("Registering mod blocks for {}", GardenKingMod.MOD_ID);
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
                        entries.add(MARKET_BLOCK);
                        entries.add(GEAR_SHOP_BLOCK);
                        entries.add(BANK_BLOCK);
                        entries.add(SCARECROW_BLOCK);
                        entries.add(IRON_SPRINKLER_BLOCK);
                        entries.add(GOLD_SPRINKLER_BLOCK);
                        entries.add(DIAMOND_SPRINKLER_BLOCK);
                        entries.add(EMERALD_SPRINKLER_BLOCK);
                        entries.add(GARDEN_OVEN_BLOCK);
                });
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> entries.add(RUBY_BLOCK));
        }
}
