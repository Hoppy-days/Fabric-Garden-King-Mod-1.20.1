package net.jeremy.gardenkingmod.enchantment;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEnchantments {
        public static final Enchantment LUMBER_JACK = register("enchantment_lumberjack", new LumberJackEnchantment());

        private ModEnchantments() {
        }

        public static void register() {
                LumberJackTreeChopHandler.register();
        }

        private static Enchantment register(String name, Enchantment enchantment) {
                return Registry.register(Registries.ENCHANTMENT, new Identifier(GardenKingMod.MOD_ID, name), enchantment);
        }
}
