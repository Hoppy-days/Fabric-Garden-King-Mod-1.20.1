package net.jeremy.gardenkingmod.client.skill;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.jeremy.gardenkingmod.skill.SkillProgressManager;
import net.minecraft.util.Identifier;

/**
 * Lightweight client-side cache of the player's Garden King skill data. The
 * HUD and other client UI components should query this singleton instead of
 * reaching directly into the player instance to avoid threading issues with
 * networking callbacks.
 */
public final class SkillState {
    private static final SkillState INSTANCE = new SkillState();

    private long totalExperience;
    private int level;
    private long experienceTowardsNextLevel;
    private long experienceRequiredForNextLevel;
    private int unspentSkillPoints;
    private final Map<Identifier, Integer> allocations = new LinkedHashMap<>();

    private SkillState() {
    }

    public static SkillState getInstance() {
        return INSTANCE;
    }

    public synchronized void update(long totalExperience, int level, long experienceTowardsNextLevel,
            long experienceRequiredForNextLevel, int unspentSkillPoints, Map<Identifier, Integer> allocations) {
        this.totalExperience = Math.max(0L, totalExperience);
        this.level = Math.max(0, level);
        this.experienceTowardsNextLevel = Math.max(0L, experienceTowardsNextLevel);
        this.experienceRequiredForNextLevel = Math.max(0L, experienceRequiredForNextLevel);
        this.unspentSkillPoints = Math.max(0, unspentSkillPoints);

        this.allocations.clear();
        if (allocations != null && !allocations.isEmpty()) {
            allocations.forEach((id, value) -> {
                if (id != null) {
                    this.allocations.put(id, Math.max(0, value));
                }
            });
        }
    }

    public synchronized void reset() {
        totalExperience = 0L;
        level = 0;
        experienceTowardsNextLevel = 0L;
        experienceRequiredForNextLevel = 0L;
        unspentSkillPoints = 0;
        allocations.clear();
    }

    public synchronized long getTotalExperience() {
        return totalExperience;
    }

    public synchronized int getLevel() {
        return level;
    }

    public synchronized long getExperienceTowardsNextLevel() {
        return experienceTowardsNextLevel;
    }

    public synchronized long getExperienceRequiredForNextLevel() {
        return experienceRequiredForNextLevel;
    }

    public synchronized float getProgressPercentage() {
        if (experienceRequiredForNextLevel <= 0L) {
            return 1.0f;
        }
        return Math.min(1.0f, (float) experienceTowardsNextLevel / (float) experienceRequiredForNextLevel);
    }

    public synchronized int getUnspentSkillPoints() {
        return unspentSkillPoints;
    }

    public synchronized Map<Identifier, Integer> getAllocations() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(allocations));
    }

    public synchronized int getAllocation(Identifier skillId) {
        return allocations.getOrDefault(skillId, 0);
    }

    public synchronized int getChefMasteryLevel() {
        return getAllocation(SkillProgressManager.CHEF_SKILL);
    }

    public synchronized int getEnchanterLevel() {
        return getAllocation(SkillProgressManager.ENCHANTER_SKILL);
    }
}
