package net.jeremy.gardenkingmod.crop;

import net.minecraft.util.Identifier;

/**
 * Describes a crop tier including the tier identifier along with modifiers that
 * influence growth speed and item drops.
 */
public record CropTier(Identifier id, float growthMultiplier, float dropMultiplier) {
}
