package net.jeremy.gardenkingmod;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.jeremy.gardenkingmod.block.MarketBlock;
import net.jeremy.gardenkingmod.block.MarketBlockPart;
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

        public static final Block RUBY_BLOCK = registerBlock("ruby_block",
                        new Block(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK)));

        public static final Block MARKET_BLOCK_PART = registerBlockWithoutItem("market_block_part",
                        new MarketBlockPart(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).dropsNothing().nonOpaque()));

        private ModBlocks() {
        }

        private static Block registerBlock(String name, Block block) {
                registerBlockItem(name, block);
                return Registry.register(Registries.BLOCK, new Identifier(GardenKingMod.MOD_ID, name), block);
        }

        private static Item registerBlockItem(String name, Block block) {
                return Registry.register(Registries.ITEM, new Identifier(GardenKingMod.MOD_ID, name), new BlockItem(block, new FabricItemSettings()));
        }

        private static Block registerBlockWithoutItem(String name, Block block) {
                return Registry.register(Registries.BLOCK, new Identifier(GardenKingMod.MOD_ID, name), block);
        }

        public static void registerModBlocks() {
                GardenKingMod.LOGGER.info("Registering mod blocks for {}", GardenKingMod.MOD_ID);
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(MARKET_BLOCK));
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> entries.add(RUBY_BLOCK));
        }
}
