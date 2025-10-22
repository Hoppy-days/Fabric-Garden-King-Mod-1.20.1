package net.jeremy.gardenkingmod.skill;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.Identifier;

/**
 * Central registry for Garden King skills, their display information, and the
 * XP requirements for each level. Server logic and client UI should query this
 * class to keep skill progression consistent.
 */
public final class SkillProgressManager {
        public static final Identifier CHEF_SKILL = new Identifier("gardenkingmod", "chef");
        public static final Identifier ENCHANTER_SKILL = new Identifier("gardenkingmod", "enchanter");

        private static final Map<Identifier, SkillDefinition> SKILL_DEFINITIONS = new LinkedHashMap<>();
        private static final List<Long> LEVEL_THRESHOLDS = List.of(
                        0L,   // Level 0
                        100L, // Level 1
                        250L, // Level 2
                        500L, // Level 3
                        900L, // Level 4
                        1400L, // Level 5
                        2000L, // Level 6
                        2700L, // Level 7
                        3500L, // Level 8
                        4400L, // Level 9
                        5400L  // Level 10
        );

        static {
                registerSkill(new SkillDefinition(CHEF_SKILL, "Chef Mastery", "Improve your cooking prowess."));
                registerSkill(new SkillDefinition(ENCHANTER_SKILL, "Enchanter",
                                "Each level increases enchanted crop chances by 1%."));
        }

        private SkillProgressManager() {
        }

        public static void registerSkill(SkillDefinition definition) {
                SKILL_DEFINITIONS.put(definition.id(), definition);
        }

        public static Map<Identifier, SkillDefinition> getSkillDefinitions() {
                return Collections.unmodifiableMap(SKILL_DEFINITIONS);
        }

        public static int getLevelForExperience(long experience) {
                int level = 0;
                for (int i = 0; i < LEVEL_THRESHOLDS.size(); i++) {
                        long threshold = LEVEL_THRESHOLDS.get(i);
                        if (experience < threshold) {
                                break;
                        }
                        level = i;
                }
                return level;
        }

        public static long getExperienceForLevel(int level) {
                if (level < 0) {
                        return 0L;
                }
                if (level >= LEVEL_THRESHOLDS.size()) {
                        return LEVEL_THRESHOLDS.get(LEVEL_THRESHOLDS.size() - 1);
                }
                return LEVEL_THRESHOLDS.get(level);
        }

        public static int getMaxDefinedLevel() {
                return LEVEL_THRESHOLDS.size() - 1;
        }

        public record SkillDefinition(Identifier id, String displayName, String description) {
        }
}
