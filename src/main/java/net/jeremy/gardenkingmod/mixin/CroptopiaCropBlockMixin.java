package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.crop.RightClickHarvestHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = { "com.epherical.croptopia.blocks.CropBlock",
                "com.epherical.croptopia.blocks.CroptopiaCropBlock",
                "com.epherical.croptopia.block.CropBlock",
                "com.epherical.croptopia.block.CroptopiaCropBlock" }, remap = false)
public abstract class CroptopiaCropBlockMixin {

        @Inject(method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
                        at = @At("HEAD"), cancellable = true, remap = false)
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
