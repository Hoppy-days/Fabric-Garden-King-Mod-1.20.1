package net.jeremy.gardenkingmod.mixin;

import net.jeremy.gardenkingmod.currency.GardenCurrencyHolder;
import net.jeremy.gardenkingmod.skill.SkillProgressHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements GardenCurrencyHolder, SkillProgressHolder {
        @Unique
        private int gardenkingmod$lifetimeCurrency;

        @Unique
        private long gardenkingmod$bankBalance;

        @Unique
        private long gardenkingmod$skillExperience;

        @Unique
        private int gardenkingmod$skillLevel;

        @Unique
        private int gardenkingmod$unspentSkillPoints;

        @Unique
        private int gardenkingmod$chefMasteryLevel;

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

        @Override
        public long gardenkingmod$getSkillExperience() {
                return this.gardenkingmod$skillExperience;
        }

        @Override
        public void gardenkingmod$setSkillExperience(long experience) {
                this.gardenkingmod$skillExperience = Math.max(0L, experience);
        }

        @Override
        public int gardenkingmod$getSkillLevel() {
                return this.gardenkingmod$skillLevel;
        }

        @Override
        public void gardenkingmod$setSkillLevel(int level) {
                this.gardenkingmod$skillLevel = Math.max(0, level);
        }

        @Override
        public int gardenkingmod$getUnspentSkillPoints() {
                return this.gardenkingmod$unspentSkillPoints;
        }

        @Override
        public void gardenkingmod$setUnspentSkillPoints(int points) {
                this.gardenkingmod$unspentSkillPoints = Math.max(0, points);
        }

        @Override
        public int gardenkingmod$getChefMasteryLevel() {
                return this.gardenkingmod$chefMasteryLevel;
        }

        @Override
        public void gardenkingmod$setChefMasteryLevel(int level) {
                this.gardenkingmod$chefMasteryLevel = Math.max(0, level);
        }

        @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
        private void gardenkingmod$readLifetimeCurrency(NbtCompound nbt, CallbackInfo ci) {
                if (nbt.contains(LIFETIME_CURRENCY_KEY, NbtElement.NUMBER_TYPE)) {
                        gardenkingmod$setLifetimeCurrency(nbt.getInt(LIFETIME_CURRENCY_KEY));
                }

                if (nbt.contains(BANK_CURRENCY_KEY, NbtElement.NUMBER_TYPE)) {
                        gardenkingmod$setBankBalance(nbt.getLong(BANK_CURRENCY_KEY));
                }

                if (nbt.contains(SKILL_XP_KEY, NbtElement.NUMBER_TYPE)) {
                        gardenkingmod$setSkillExperience(nbt.getLong(SKILL_XP_KEY));
                }

                if (nbt.contains(SKILL_LEVEL_KEY, NbtElement.NUMBER_TYPE)) {
                        gardenkingmod$setSkillLevel(nbt.getInt(SKILL_LEVEL_KEY));
                }

                if (nbt.contains(SKILL_POINTS_KEY, NbtElement.NUMBER_TYPE)) {
                        gardenkingmod$setUnspentSkillPoints(nbt.getInt(SKILL_POINTS_KEY));
                }

                if (nbt.contains(CHEF_MASTERY_KEY, NbtElement.NUMBER_TYPE)) {
                        gardenkingmod$setChefMasteryLevel(nbt.getInt(CHEF_MASTERY_KEY));
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

                if (gardenkingmod$getSkillExperience() > 0) {
                        nbt.putLong(SKILL_XP_KEY, gardenkingmod$getSkillExperience());
                } else {
                        nbt.remove(SKILL_XP_KEY);
                }

                if (gardenkingmod$getSkillLevel() > 0) {
                        nbt.putInt(SKILL_LEVEL_KEY, gardenkingmod$getSkillLevel());
                } else {
                        nbt.remove(SKILL_LEVEL_KEY);
                }

                if (gardenkingmod$getUnspentSkillPoints() > 0) {
                        nbt.putInt(SKILL_POINTS_KEY, gardenkingmod$getUnspentSkillPoints());
                } else {
                        nbt.remove(SKILL_POINTS_KEY);
                }

                if (gardenkingmod$getChefMasteryLevel() > 0) {
                        nbt.putInt(CHEF_MASTERY_KEY, gardenkingmod$getChefMasteryLevel());
                } else {
                        nbt.remove(CHEF_MASTERY_KEY);
                }
        }
}
