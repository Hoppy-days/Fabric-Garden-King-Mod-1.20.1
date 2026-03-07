package net.jeremy.gardenkingmod;

import java.util.Optional;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.jeremy.gardenkingmod.block.entity.MarketBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class GardenKingModGameTest implements FabricGameTest {
        @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
        public void sellingCroptopiaTomatoAwardsDollars(GameTestHelper helper) {
                helper.setBlock(BlockPos.ORIGIN, ModBlocks.MARKET_BLOCK.getDefaultState());

                helper.runAtTickTime(1, () -> {
                        MarketBlockEntity blockEntity = helper.getBlockEntity(BlockPos.ORIGIN);
                        if (blockEntity == null) {
                                helper.fail("Market block entity was not created");
                                return;
                        }

                        Optional<Item> tomatoOptional = Registries.ITEM.getOrEmpty(new Identifier("croptopia", "tomato"));
                        if (tomatoOptional.isEmpty()) {
                                helper.fail("Croptopia tomato item is missing from the registry");
                                return;
                        }

                        blockEntity.setStack(0, new ItemStack(tomatoOptional.get(), 64));
                        blockEntity.setStack(1, new ItemStack(tomatoOptional.get(), 32));

                        ServerPlayerEntity player = helper.spawnPlayer(BlockPos.ORIGIN.up());
                        boolean sold = blockEntity.sell(player);
                        if (!sold) {
                                helper.fail("Market failed to sell a stack of tomatoes");
                                return;
                        }

                        int dollarCount = 0;
                        int tomatoCount = 0;
                        for (ItemStack inventoryStack : player.getInventory().main) {
                                if (inventoryStack.isOf(ModItems.DOLLAR)) {
                                        dollarCount += inventoryStack.getCount();
                                } else if (inventoryStack.isOf(tomatoOptional.get())) {
                                        tomatoCount += inventoryStack.getCount();
                                }
                        }

                        if (dollarCount <= 0) {
                                helper.fail("Player did not receive any dollars after selling tomatoes");
                                return;
                        }

                        if (!blockEntity.getStack(0).isEmpty()) {
                                helper.fail("Market input slot should be cleared after selling tomatoes");
                                return;
                        }

                        if (!blockEntity.getStack(1).isEmpty()) {
                                helper.fail("Market should clear all processed slots after selling tomatoes");
                                return;
                        }

                        if (tomatoCount != 32) {
                                helper.fail("Player should receive the partial stack of tomatoes back after selling");
                                return;
                        }

                        helper.succeed();
                });
        }


        @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
        public void sellingSeedIsRejected(GameTestHelper helper) {
                helper.setBlock(BlockPos.ORIGIN, ModBlocks.MARKET_BLOCK.getDefaultState());

                helper.runAtTickTime(1, () -> {
                        MarketBlockEntity blockEntity = helper.getBlockEntity(BlockPos.ORIGIN);
                        if (blockEntity == null) {
                                helper.fail("Market block entity was not created");
                                return;
                        }

                        Optional<Item> seedOptional = Registries.ITEM.getOrEmpty(new Identifier("croptopia", "tomato_seed"));
                        if (seedOptional.isEmpty()) {
                                helper.fail("Croptopia tomato seed item is missing from the registry");
                                return;
                        }

                        Item seedItem = seedOptional.get();
                        blockEntity.setStack(0, new ItemStack(seedItem, 16));

                        ServerPlayerEntity player = helper.spawnPlayer(BlockPos.ORIGIN.up());
                        boolean sold = blockEntity.sell(player);
                        if (sold) {
                                helper.fail("Market should not sell seed items");
                                return;
                        }

                        if (!blockEntity.getStack(0).isOf(seedItem) || blockEntity.getStack(0).getCount() != 16) {
                                helper.fail("Seed stack should remain in the market input when sale is rejected");
                                return;
                        }

                        int dollarCount = 0;
                        for (ItemStack inventoryStack : player.getInventory().main) {
                                if (inventoryStack.isOf(ModItems.DOLLAR)) {
                                        dollarCount += inventoryStack.getCount();
                                }
                        }

                        if (dollarCount > 0) {
                                helper.fail("Player should not receive dollars when trying to sell seeds");
                                return;
                        }

                        helper.succeed();
                });
        }

}
