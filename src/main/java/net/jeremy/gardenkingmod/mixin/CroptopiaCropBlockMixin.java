package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.crop.CropTierRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "com.epherical.croptopia.blocks.CropBlock", remap = false)
public abstract class CroptopiaCropBlockMixin {
        @ModifyVariable(method = "randomTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V",
                        at = @At(value = "STORE"), ordinal = 0, remap = false)
        private float gardenkingmod$scaleGrowthChance(float moisture, BlockState state, ServerWorld world, BlockPos pos,
                        Random random) {
                return CropTierRegistry.scaleGrowthChance(state, moisture);
        }
}
