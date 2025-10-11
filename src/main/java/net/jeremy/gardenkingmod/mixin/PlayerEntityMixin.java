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

        @Unique
        private long gardenkingmod$bankBalance;

        @Override
        public int gardenkingmod$getLifetimeCurrency() {
                return this.gardenkingmod$lifetimeCurrency;
        }

        @Override
        public void gardenkingmod$setLifetimeCurrency(int amount) {
                this.gardenkingmod$lifetimeCurrency = Math.max(0, amount);
        }

        @Override
        public long gardenkingmod$getBankBalance() {
                return this.gardenkingmod$bankBalance;
        }

        @Override
        public void gardenkingmod$setBankBalance(long amount) {
                this.gardenkingmod$bankBalance = Math.max(0L, amount);
        }

        @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
        private void gardenkingmod$readLifetimeCurrency(NbtCompound nbt, CallbackInfo ci) {
                if (nbt.contains(LIFETIME_CURRENCY_KEY, NbtElement.NUMBER_TYPE)) {
                        gardenkingmod$setLifetimeCurrency(nbt.getInt(LIFETIME_CURRENCY_KEY));
                }

                if (nbt.contains(BANK_CURRENCY_KEY, NbtElement.NUMBER_TYPE)) {
                        gardenkingmod$setBankBalance(nbt.getLong(BANK_CURRENCY_KEY));
                }
        }

        @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
        private void gardenkingmod$writeLifetimeCurrency(NbtCompound nbt, CallbackInfo ci) {
                if (gardenkingmod$getLifetimeCurrency() > 0) {
                        nbt.putInt(LIFETIME_CURRENCY_KEY, gardenkingmod$getLifetimeCurrency());
                } else {
                        nbt.remove(LIFETIME_CURRENCY_KEY);
                }

                if (gardenkingmod$getBankBalance() > 0) {
                        nbt.putLong(BANK_CURRENCY_KEY, gardenkingmod$getBankBalance());
                } else {
                        nbt.remove(BANK_CURRENCY_KEY);
                }
        }
}
