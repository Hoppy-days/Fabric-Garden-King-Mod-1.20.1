package net.jeremy.gardenkingmod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
        @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
        private void gardenkingmod$preventCropAcceleration(ItemUsageContext context,
                        CallbackInfoReturnable<ActionResult> cir) {
                BlockState state = context.getWorld().getBlockState(context.getBlockPos());
                if (state.getBlock() instanceof CropBlock || state.isIn(BlockTags.CROPS)) {
                        cir.setReturnValue(ActionResult.PASS);
                }
        }
}
