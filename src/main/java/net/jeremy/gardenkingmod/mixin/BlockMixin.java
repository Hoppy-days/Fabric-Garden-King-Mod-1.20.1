package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.crop.RightClickHarvestHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class BlockMixin {
        @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
        private void gardenkingmod$harvestCrops(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                        BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
                if (!(state.getBlock() instanceof CropBlock crop)) {
                        return;
                }

                ActionResult result = RightClickHarvestHandler.tryHarvest(crop, state, world, pos, player, hand);
                if (result != ActionResult.PASS) {
                        cir.setReturnValue(result);
                        cir.cancel();
                }
        }
}
