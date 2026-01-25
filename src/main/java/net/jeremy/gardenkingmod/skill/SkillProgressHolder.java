package net.jeremy.gardenkingmod.skill;

import net.minecraft.util.math.MathHelper;

/**
 * Capability-style contract injected into {@link net.minecraft.entity.player.PlayerEntity}
 * for tracking Garden King skill progress, level, and spendable skill points.
 */
public interface SkillProgressHolder {
        String SKILL_XP_KEY = "GardenKingSkillXp";
        String SKILL_LEVEL_KEY = "GardenKingSkillLevel";
        String SKILL_POINTS_KEY = "GardenKingSkillPoints";
        String CHEF_MASTERY_KEY = "GardenKingChefMastery";
        String ENCHANTER_KEY = "GardenKingEnchanter";

        long gardenkingmod$getSkillExperience();

        void gardenkingmod$setSkillExperience(long experience);

        int gardenkingmod$getSkillLevel();

        void gardenkingmod$setSkillLevel(int level);

        int gardenkingmod$getUnspentSkillPoints();

        void gardenkingmod$setUnspentSkillPoints(int points);

        int gardenkingmod$getChefMasteryLevel();

        void gardenkingmod$setChefMasteryLevel(int level);

        int gardenkingmod$getEnchanterLevel();

        void gardenkingmod$setEnchanterLevel(int level);

        default int gardenkingmod$addSkillExperience(long experience) {
                if (experience <= 0) {
                        return gardenkingmod$getSkillLevel();
                }

                long currentExperience = Math.max(0L, gardenkingmod$getSkillExperience());
                long updatedExperience;
                try {
                        updatedExperience = Math.addExact(currentExperience, experience);
                } catch (ArithmeticException overflow) {
                        updatedExperience = Long.MAX_VALUE;
                }
                gardenkingmod$setSkillExperience(updatedExperience);

                int previousLevel = Math.max(0, gardenkingmod$getSkillLevel());
                int newLevel = SkillProgressManager.getLevelForExperience(updatedExperience);
                if (newLevel > previousLevel) {
                        gardenkingmod$setSkillLevel(newLevel);
                        int awardedPoints = MathHelper.clamp(newLevel - previousLevel, 0, Integer.MAX_VALUE);
                        if (awardedPoints > 0) {
                                gardenkingmod$setUnspentSkillPoints(MathHelper.clamp(
                                                gardenkingmod$getUnspentSkillPoints() + awardedPoints, 0, Integer.MAX_VALUE));
                        }
                }

                return gardenkingmod$getSkillLevel();
        }

        default boolean gardenkingmod$spendSkillPoints(int amount) {
                if (amount <= 0) {
                        return true;
                }

                int currentPoints = gardenkingmod$getUnspentSkillPoints();
                if (currentPoints < amount) {
                        return false;
                }

                gardenkingmod$setUnspentSkillPoints(currentPoints - amount);
                return true;
        }
}
