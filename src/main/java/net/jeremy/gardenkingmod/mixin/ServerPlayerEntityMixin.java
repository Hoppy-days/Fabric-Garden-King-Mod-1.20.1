package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.currency.GardenCurrencyHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
        @Inject(method = "copyFrom", at = @At("TAIL"))
        private void gardenkingmod$copyLifetimeCurrency(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
                GardenCurrencyHolder newHolder = (GardenCurrencyHolder) this;
                GardenCurrencyHolder oldHolder = (GardenCurrencyHolder) oldPlayer;
                newHolder.gardenkingmod$setLifetimeCurrency(oldHolder.gardenkingmod$getLifetimeCurrency());
        }
}
