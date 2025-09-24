package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.item.FortuneProvidingItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
        @Inject(method = "getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I",
                        at = @At("RETURN"), cancellable = true)
        private static void gardenkingmod$provideHoeFortune(Enchantment enchantment, ItemStack stack,
                        CallbackInfoReturnable<Integer> cir) {
                if (enchantment == Enchantments.FORTUNE && !stack.isEmpty()) {
                        Item item = stack.getItem();
                        if (item instanceof FortuneProvidingItem fortuneItem) {
                                int builtInLevel = fortuneItem.gardenkingmod$getFortuneLevel(stack);
                                if (builtInLevel > cir.getReturnValue()) {
                                        cir.setReturnValue(builtInLevel);
                                }
                        }
                }
        }
}
