package net.jeremy.gardenkingmod.registry;

import net.jeremy.gardenkingmod.GardenKingMod;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Registers custom sound events used throughout the mod.
 */
public final class ModSoundEvents {
    public static final SoundEvent CROW_CAW = register("entity.crow.caw");
    public static final SoundEvent CROW_CROP_BREAK = register("entity.crow.crop_break");
    public static final SoundEvent CROW_FLAP = register("entity.crow.flap");
    public static final SoundEvent CROW_WARDED = register("entity.crow.warded");

    private ModSoundEvents() {
    }

    private static SoundEvent register(String path) {
        Identifier id = new Identifier(GardenKingMod.MOD_ID, path);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void register() {
        GardenKingMod.LOGGER.debug("Registered sound events for Garden King Mod");
    }
}
