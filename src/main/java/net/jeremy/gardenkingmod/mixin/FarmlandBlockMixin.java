package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.block.sprinkler.SprinklerHydrationManager;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {
        @Inject(method = "isWaterNearby", at = @At("RETURN"), cancellable = true)
        private static void gardenkingmod$extendMoisture(WorldView world, BlockPos pos,
                        CallbackInfoReturnable<Boolean> cir) {
                if (!cir.getReturnValue() && world instanceof ServerWorld serverWorld) {
                        if (SprinklerHydrationManager.keepsFarmlandWet(serverWorld, pos)) {
                                cir.setReturnValue(true);
                        }
                }
        }
}
