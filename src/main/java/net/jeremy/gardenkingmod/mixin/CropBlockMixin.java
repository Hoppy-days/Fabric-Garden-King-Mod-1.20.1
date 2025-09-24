package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin {
        @ModifyVariable(method = "randomTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V",
                        at = @At(value = "STORE"), ordinal = 0)
        private float gardenkingmod$scaleGrowthChance(float moisture, BlockState state, ServerWorld world, BlockPos pos,
                        Random random) {
                return CropTierRegistry.scaleGrowthChance(state, moisture);
        }
}
