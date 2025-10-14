package net.jeremy.gardenkingmod.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.WorldEvents;

/**
 * Custom fertilizer produced from rotten crops. Works similarly to bone meal
 * but uses the configurable growth chance defined in
 * {@link FertilizerBalanceConfig}.
 */
public class CompostFertilizerItem extends Item {
        public CompostFertilizerItem(Settings settings) {
                super(settings);
        }

        @Override
        public ActionResult useOnBlock(ItemUsageContext context) {
                World world = context.getWorld();
                BlockPos pos = context.getBlockPos();
                BlockState state = world.getBlockState(pos);
                if (!(state.getBlock() instanceof Fertilizable fertilizable)) {
                        return ActionResult.PASS;
                }

                if (world.isClient) {
                        return ActionResult.SUCCESS;
                }

                return applyFertilizer(context, (ServerWorld) world, fertilizable, state);
        }

        private ActionResult applyFertilizer(ItemUsageContext context, ServerWorld world, Fertilizable fertilizable,
                        BlockState state) {
                BlockPos pos = context.getBlockPos();
                if (!fertilizable.isFertilizable(world, pos, state, false)) {
                        return ActionResult.PASS;
                }

                ItemStack stack = context.getStack();
                double chance = MathHelper.clamp(FertilizerBalanceConfig.get().fertilizerGrowthChance(), 0.0, 1.0);
                boolean grew = false;

                var random = world.getRandom();
                if (fertilizable.canGrow(world, random, pos, state)) {
                        if (chance > 0.0 && random.nextDouble() < chance) {
                                fertilizable.grow(world, random, pos, state);
                                grew = true;
                        }
                }

                if (context.getPlayer() == null || !context.getPlayer().getAbilities().creativeMode) {
                        stack.decrement(1);
                }

                world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 0);
                if (grew) {
                        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos,
                                        GameEvent.Emitter.of(context.getPlayer(), state));
                        return ActionResult.SUCCESS;
                }

                return ActionResult.CONSUME;
        }
}
