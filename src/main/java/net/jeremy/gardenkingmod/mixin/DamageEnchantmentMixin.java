package net.jeremy.gardenkingmod.mixin;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageEnchantment.class)
public abstract class DamageEnchantmentMixin {
    private static final Identifier SHARPNESS_ID = new Identifier("minecraft", "sharpness");
    private static final int SHARPNESS_MAX_LEVEL = 10;

    @Inject(method = "getMaxLevel", at = @At("RETURN"), cancellable = true)
    private void gardenkingmod$allowHigherCustomLevels(CallbackInfoReturnable<Integer> cir) {
        Enchantment enchantment = (Enchantment) (Object) this;
        Identifier enchantmentId = Registries.ENCHANTMENT.getId(enchantment);
        if (SHARPNESS_ID.equals(enchantmentId) && cir.getReturnValue() < SHARPNESS_MAX_LEVEL) {
            cir.setReturnValue(SHARPNESS_MAX_LEVEL);
        }
    }
}
