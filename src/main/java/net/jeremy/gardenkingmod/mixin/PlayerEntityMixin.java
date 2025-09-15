package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.currency.GardenCurrencyHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements GardenCurrencyHolder {
        @Unique
        private int gardenkingmod$lifetimeCurrency;

        @Override
        public int gardenkingmod$getLifetimeCurrency() {
                return this.gardenkingmod$lifetimeCurrency;
        }

        @Override
        public void gardenkingmod$setLifetimeCurrency(int amount) {
                this.gardenkingmod$lifetimeCurrency = Math.max(0, amount);
        }

        @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
        private void gardenkingmod$readLifetimeCurrency(NbtCompound nbt, CallbackInfo ci) {
                if (nbt.contains(LIFETIME_CURRENCY_KEY, NbtElement.NUMBER_TYPE)) {
                        gardenkingmod$setLifetimeCurrency(nbt.getInt(LIFETIME_CURRENCY_KEY));
                }
        }

        @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
        private void gardenkingmod$writeLifetimeCurrency(NbtCompound nbt, CallbackInfo ci) {
                if (gardenkingmod$getLifetimeCurrency() > 0) {
                        nbt.putInt(LIFETIME_CURRENCY_KEY, gardenkingmod$getLifetimeCurrency());
                } else {
                        nbt.remove(LIFETIME_CURRENCY_KEY);
                }
        }
}
