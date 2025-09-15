package net.jeremy.gardenkingmod.block;

import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.ModScoreboards;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MarketBlock extends Block {
        public MarketBlock(Settings settings) {
                super(settings);
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
                ItemStack heldStack = player.getStackInHand(hand);
                if (heldStack.isEmpty()) {
                        return ActionResult.PASS;
                }

                Identifier itemId = Registries.ITEM.getId(heldStack.getItem());
                if (itemId == null || !"croptopia".equals(itemId.getNamespace())) {
                        return ActionResult.PASS;
                }

                if (world.isClient) {
                        return ActionResult.SUCCESS;
                }

                int soldCount = heldStack.getCount();
                player.setStackInHand(hand, ItemStack.EMPTY);

                ItemStack currencyStack = new ItemStack(ModItems.GARDEN_COIN, soldCount);
                boolean fullyInserted = player.getInventory().insertStack(currencyStack);
                if (!fullyInserted) {
                        player.dropItem(currencyStack, false);
                }

                if (player instanceof ServerPlayerEntity serverPlayer) {
                        ModScoreboards.addCurrency(serverPlayer, soldCount);
                        serverPlayer.sendMessage(Text.literal("Sold " + soldCount + " Croptopia crops for " + soldCount + " coins."), true);
                }

                world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.75f, 1.0f);

                return ActionResult.SUCCESS;
        }
}
