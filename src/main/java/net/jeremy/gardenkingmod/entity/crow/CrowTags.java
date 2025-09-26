package net.jeremy.gardenkingmod.entity.crow;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

/**
 * Constants that expose the data-driven hooks used by the crow entity. Pack
 * authors can refer to these keys in biome tags and block tags to control
 * spawning and behavior without modifying Java code.
 */
public final class CrowTags {
    public static final TagKey<Biome> CROW_SPAWN_BIOMES = TagKey.of(RegistryKeys.BIOME,
            new Identifier(GardenKingMod.MOD_ID, "spawns_crows"));

    public static final TagKey<Block> CROW_TARGET_CROPS = TagKey.of(RegistryKeys.BLOCK,
            new Identifier(GardenKingMod.MOD_ID, "crow_targets"));

    public static final TagKey<Block> CROW_PERCH_BLOCKS = TagKey.of(RegistryKeys.BLOCK,
            new Identifier(GardenKingMod.MOD_ID, "crow_perches"));

    public static final TagKey<Block> CROW_WARD_BLOCKS = TagKey.of(RegistryKeys.BLOCK,
            new Identifier(GardenKingMod.MOD_ID, "crow_wards"));

    private CrowTags() {
    }
}
