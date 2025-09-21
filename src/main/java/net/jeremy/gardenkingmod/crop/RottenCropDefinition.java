package net.jeremy.gardenkingmod.crop;

import java.util.Locale;
import java.util.Objects;

import net.jeremy.gardenkingmod.GardenKingMod;

import net.minecraft.util.Identifier;

/**
 * Captures the data needed to register a rotten crop item and configure
 * harvesting behaviour adjustments.
 */
public record RottenCropDefinition(
                Identifier cropId,
                Identifier targetId,
                Identifier lootTableId,
                String rottenItemPath,
                float extraNoDropChance,
                float extraRottenChance) {
        private static final String[] NAME_SUFFIXES = new String[] { "_crop", "_plant", "_bush", "_stem", "_block",
                        "_leaves", "_vines", "_vine", "_sapling", "_buds", "_bud", "_roots" };

        public RottenCropDefinition {
                Objects.requireNonNull(cropId, "cropId");
                Objects.requireNonNull(targetId, "targetId");
                Objects.requireNonNull(lootTableId, "lootTableId");
                Objects.requireNonNull(rottenItemPath, "rottenItemPath");
        }

        public RottenCropDefinition(Identifier cropId, Identifier targetId, Identifier lootTableId) {
                this(cropId, targetId, lootTableId, defaultRottenItemPath(cropId), 0.0f, 0.0f);
        }

        public RottenCropDefinition(Identifier cropId, Identifier targetId, Identifier lootTableId,
                        float extraNoDropChance, float extraRottenChance) {
                this(cropId, targetId, lootTableId, defaultRottenItemPath(cropId), extraNoDropChance, extraRottenChance);
        }

        public Identifier rottenItemId() {
                return new Identifier(GardenKingMod.MOD_ID, rottenItemPath);
        }

        public boolean hasExtraNoDropChance() {
                return extraNoDropChance > 0.0f;
        }

        public boolean hasExtraRottenChance() {
                return extraRottenChance > 0.0f;
        }

        public boolean hasDropAdjustments() {
                return hasExtraNoDropChance() || hasExtraRottenChance();
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

        private static String defaultRottenItemPath(Identifier cropId) {
                return "rotten_" + cropId.getPath();
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
}
