package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.item.FertilizerBalanceConfig;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin {
        @ModifyArg(method = "emptyFullComposter", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"), index = 4)
        private static ItemStack gardenkingmod$replaceComposterOutput(ItemStack original) {
                int count = Math.max(1, FertilizerBalanceConfig.get().fertilizerOutputCount());
                return new ItemStack(ModItems.SPECIAL_FERTILIZER, count);
        }
}
