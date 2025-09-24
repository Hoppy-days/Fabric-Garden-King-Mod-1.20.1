package net.jeremy.gardenkingmod.crop;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public final class RightClickHarvestHandler {
        private static final Map<ToolMaterial, Integer> HOE_FORTUNE_LEVELS = Map.of(
                        ToolMaterials.WOOD, 0,
                        ToolMaterials.STONE, 1,
                        ToolMaterials.IRON, 2,
                        ToolMaterials.GOLD, 2,
                        ToolMaterials.DIAMOND, 3,
                        ToolMaterials.NETHERITE, 4);

        private RightClickHarvestHandler() {
        }

        public static ActionResult tryHarvest(CropBlock crop, BlockState state, World world, BlockPos pos, PlayerEntity player,
                        Hand hand) {
                if (!crop.isMature(state)) {
                        return ActionResult.PASS;
                }

                if (!player.canModifyBlocks()) {
                        return ActionResult.PASS;
                }

                ItemStack heldStack = player.getStackInHand(hand);
                ItemStack toolForDrops = ItemStack.EMPTY;

                if (heldStack.getItem() instanceof HoeItem hoeItem) {
                        toolForDrops = heldStack.copy();
                        int defaultFortune = getDefaultFortuneLevel(hoeItem.getMaterial());
                        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(toolForDrops);
                        int fortuneLevel = Math.max(defaultFortune,
                                        enchantments.getOrDefault(Enchantments.FORTUNE, 0));
                        if (fortuneLevel > 0) {
                                enchantments.put(Enchantments.FORTUNE, fortuneLevel);
                        } else {
                                enchantments.remove(Enchantments.FORTUNE);
                        }
                        EnchantmentHelper.set(enchantments, toolForDrops);
                }

                if (world.isClient) {
                        return ActionResult.SUCCESS;
                }

                ServerWorld serverWorld = (ServerWorld) world;
                BlockEntity blockEntity = world.getBlockEntity(pos);
                Block.dropStacks(state, serverWorld, pos, blockEntity, player, toolForDrops);

                BlockState resetState = crop.withAge(0);
                world.setBlockState(pos, resetState, Block.NOTIFY_ALL);
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                world.playSound(null, pos, SoundEvents.BLOCK_CROP_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);

                if (heldStack.getItem() instanceof HoeItem && !player.isCreative()) {
                        heldStack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                }

                return ActionResult.SUCCESS;
        }

        private static int getDefaultFortuneLevel(ToolMaterial material) {
                return HOE_FORTUNE_LEVELS.getOrDefault(material, Math.max(0, material.getMiningLevel() - 1));
        }
}
