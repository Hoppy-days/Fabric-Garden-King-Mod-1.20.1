package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.jeremy.gardenkingmod.crop.RightClickHarvestHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = { "com.epherical.croptopia.blocks.CropBlock",
                "com.epherical.croptopia.blocks.CroptopiaCropBlock" }, remap = false)
public abstract class CroptopiaCropBlockMixin {

        @ModifyVariable(method = "randomTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;"
                        + "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V",
                        at = @At(value = "STORE"), ordinal = 0, remap = false)
        private float gardenkingmod$scaleGrowthChance(float moisture, BlockState state, ServerWorld world, BlockPos pos,
                        Random random) {
                return CropTierRegistry.scaleGrowthChance(state, moisture);
        }

        @Inject(method = "method_9534", at = @At("HEAD"), cancellable = true, remap = false)
        private void gardenkingmod$harvestCrops(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                        BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
                CropBlock crop = (CropBlock) (Object) this;
                ActionResult result = RightClickHarvestHandler.tryHarvest(crop, state, world, pos, player, hand);
                if (result != ActionResult.PASS) {
                        cir.setReturnValue(result);
                        cir.cancel();
                }
        }
}
