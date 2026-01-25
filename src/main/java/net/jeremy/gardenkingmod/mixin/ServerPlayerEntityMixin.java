package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.currency.GardenCurrencyHolder;
import net.jeremy.gardenkingmod.skill.SkillProgressHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
        @Inject(method = "copyFrom", at = @At("TAIL"))
        private void gardenkingmod$copyLifetimeCurrency(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
                GardenCurrencyHolder newCurrencyHolder = (GardenCurrencyHolder) this;
                GardenCurrencyHolder oldCurrencyHolder = (GardenCurrencyHolder) oldPlayer;
                newCurrencyHolder.gardenkingmod$setLifetimeCurrency(oldCurrencyHolder.gardenkingmod$getLifetimeCurrency());
                newCurrencyHolder.gardenkingmod$setBankBalance(oldCurrencyHolder.gardenkingmod$getBankBalance());

                SkillProgressHolder newSkillHolder = (SkillProgressHolder) this;
                SkillProgressHolder oldSkillHolder = (SkillProgressHolder) oldPlayer;
                newSkillHolder.gardenkingmod$setSkillExperience(oldSkillHolder.gardenkingmod$getSkillExperience());
                newSkillHolder.gardenkingmod$setSkillLevel(oldSkillHolder.gardenkingmod$getSkillLevel());
                newSkillHolder.gardenkingmod$setUnspentSkillPoints(oldSkillHolder.gardenkingmod$getUnspentSkillPoints());
                newSkillHolder.gardenkingmod$setChefMasteryLevel(oldSkillHolder.gardenkingmod$getChefMasteryLevel());
                newSkillHolder.gardenkingmod$setEnchanterLevel(oldSkillHolder.gardenkingmod$getEnchanterLevel());
        }
}
