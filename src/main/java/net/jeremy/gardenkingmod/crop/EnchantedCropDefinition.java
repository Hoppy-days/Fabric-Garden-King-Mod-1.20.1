package net.jeremy.gardenkingmod.crop;

import java.util.Locale;
import java.util.Objects;

import net.jeremy.gardenkingmod.GardenKingMod;

import net.minecraft.util.Identifier;

/**
 * Describes an enchanted crop variant, including its drop chance, item id and
 * payout multiplier.
 */
public record EnchantedCropDefinition(
                Identifier cropId,
                Identifier targetId,
                Identifier lootTableId,
                String enchantedItemPath,
                float dropChance,
                float valueMultiplier) {
        public static final float DEFAULT_DROP_CHANCE = 0.02f;
        public static final float DEFAULT_VALUE_MULTIPLIER = 2.0f;

        private static final String[] NAME_SUFFIXES = new String[] { "_crop", "_plant", "_bush", "_stem", "_block",
                        "_leaves", "_vines", "_vine", "_sapling", "_buds", "_bud", "_roots" };

        public EnchantedCropDefinition {
                Objects.requireNonNull(cropId, "cropId");
                Objects.requireNonNull(targetId, "targetId");
                Objects.requireNonNull(lootTableId, "lootTableId");
                Objects.requireNonNull(enchantedItemPath, "enchantedItemPath");
        }

        public EnchantedCropDefinition(Identifier cropId, Identifier targetId, Identifier lootTableId) {
                this(cropId, targetId, lootTableId, defaultEnchantedItemPath(cropId), DEFAULT_DROP_CHANCE,
                                DEFAULT_VALUE_MULTIPLIER);
        }

        public EnchantedCropDefinition(Identifier cropId, Identifier targetId, Identifier lootTableId,
                        float dropChance, float valueMultiplier) {
                this(cropId, targetId, lootTableId, defaultEnchantedItemPath(cropId), dropChance, valueMultiplier);
        }

        public Identifier enchantedItemId() {
                return new Identifier(GardenKingMod.MOD_ID, enchantedItemPath);
        }

        public float effectiveDropChance() {
                return Math.max(0.0f, Math.min(1.0f, dropChance));
        }

        public float effectiveValueMultiplier() {
                return Math.max(1.0f, valueMultiplier);
        }

        public String displayName() {
                return toDisplayName(cropId.getPath());
        }

        public static String toDisplayName(String path) {
                String sanitized = sanitize(path);
                String[] parts = sanitized.split("_");
                StringBuilder builder = new StringBuilder();

                for (String part : parts) {
                        if (part.isEmpty()) {
                                continue;
                        }

                        if (builder.length() > 0) {
                                builder.append(' ');
                        }

                        String lower = part.toLowerCase(Locale.ROOT);
                        builder.append(Character.toUpperCase(lower.charAt(0)));
                        if (lower.length() > 1) {
                                builder.append(lower.substring(1));
                        }
                }

                if (builder.length() == 0) {
                        return sanitize(path).replace('_', ' ');
                }

                return builder.toString();
        }

        public static String sanitizedPath(String input) {
                return sanitize(input);
        }

        private static String sanitize(String input) {
                String sanitized = input;

                for (String suffix : NAME_SUFFIXES) {
                        if (sanitized.endsWith(suffix)) {
                                sanitized = sanitized.substring(0, sanitized.length() - suffix.length());
                                break;
                        }
                }

                sanitized = sanitized.replaceAll("__+", "_");

                if (sanitized.endsWith("ies")) {
                        sanitized = sanitized.substring(0, sanitized.length() - 3) + "y";
                } else if (sanitized.endsWith("oes")) {
                        sanitized = sanitized.substring(0, sanitized.length() - 2);
                } else if (sanitized.endsWith("ses")) {
                        sanitized = sanitized.substring(0, sanitized.length() - 2);
                } else if (sanitized.endsWith("s") && !sanitized.endsWith("ss") && !sanitized.endsWith("us")
                                && !sanitized.endsWith("is")) {
                        sanitized = sanitized.substring(0, sanitized.length() - 1);
                }

                sanitized = sanitized.replaceAll("^_+", "");
                sanitized = sanitized.replaceAll("_+$", "");

                if (sanitized.isEmpty()) {
                        return input;
                }

                return sanitized;
        }

        private static String defaultEnchantedItemPath(Identifier cropId) {
                return "enchanted_" + sanitize(cropId.getPath());
        }
}
