package net.jeremy.gardenkingmod;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModItemTags {
        public static final TagKey<Item> SCARECROW_HATS = of("scarecrow_hats");
        public static final TagKey<Item> SCARECROW_HEADS = of("scarecrow_heads");
        public static final TagKey<Item> SCARECROW_SHIRTS = of("scarecrow_shirts");
        public static final TagKey<Item> SCARECROW_PITCHFORKS = of("scarecrow_pitchforks");

        private ModItemTags() {
        }

        private static TagKey<Item> of(String path) {
                return TagKey.of(RegistryKeys.ITEM, new Identifier(GardenKingMod.MOD_ID, path));
        }
}
