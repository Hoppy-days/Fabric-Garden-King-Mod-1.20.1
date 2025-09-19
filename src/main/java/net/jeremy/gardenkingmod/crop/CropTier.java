package net.jeremy.gardenkingmod.crop;

import net.minecraft.util.Identifier;

/**
 * Describes a crop tier including the tier identifier along with modifiers that
 * influence growth speed, item drops, the probability of producing rotten
 * harvests, and the probability of producing no harvest at all.
 */
public record CropTier(Identifier id, float growthMultiplier, float dropMultiplier, float rottenChance,
	float noDropChance) {
}
