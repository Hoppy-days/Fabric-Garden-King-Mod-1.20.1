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
        public void sellingCroptopiaTomatoAwardsCoins(GameTestHelper helper) {
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

                        blockEntity.setStack(MarketBlockEntity.INPUT_SLOT, new ItemStack(tomatoOptional.get(), 64));

                        ServerPlayerEntity player = helper.spawnPlayer(BlockPos.ORIGIN.up());
                        boolean sold = blockEntity.sell(player);
                        if (!sold) {
                                helper.fail("Market failed to sell a stack of tomatoes");
                                return;
                        }

                        int coinCount = 0;
                        for (ItemStack inventoryStack : player.getInventory().main) {
                                if (inventoryStack.isOf(ModItems.GARDEN_COIN)) {
                                        coinCount += inventoryStack.getCount();
                                }
                        }

                        if (coinCount <= 0) {
                                helper.fail("Player did not receive any garden coins after selling tomatoes");
                                return;
                        }

                        if (!blockEntity.getStack(MarketBlockEntity.INPUT_SLOT).isEmpty()) {
                                helper.fail("Market input slot should be cleared after selling tomatoes");
                                return;
                        }

                        helper.succeed();
                });
        }
}
