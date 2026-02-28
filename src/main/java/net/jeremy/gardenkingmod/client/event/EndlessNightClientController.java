package net.jeremy.gardenkingmod.client.event;

import net.jeremy.gardenkingmod.registry.ModSoundEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;

public final class EndlessNightClientController {
    private static boolean active;
    private static EndlessNightMusicInstance musicInstance;

    private EndlessNightClientController() {
    }

    public static void updateFromServer(boolean isActive) {
        active = isActive;
    }

    public static void tick(MinecraftClient client) {
        if (!active || client.player == null || client.world == null) {
            stopMusic(client);
            return;
        }

        if (musicInstance != null && client.getSoundManager().isPlaying(musicInstance)) {
            musicInstance.setAnchor(client.player.getX(), client.player.getY(), client.player.getZ());
            return;
        }

        musicInstance = new EndlessNightMusicInstance();
        musicInstance.setAnchor(client.player.getX(), client.player.getY(), client.player.getZ());
        client.getSoundManager().play(musicInstance);
    }

    public static void clear() {
        active = false;
        musicInstance = null;
    }

    private static void stopMusic(MinecraftClient client) {
        if (musicInstance != null) {
            client.getSoundManager().stop(musicInstance);
            musicInstance = null;
        }
    }

    private static final class EndlessNightMusicInstance extends MovingSoundInstance {
        private EndlessNightMusicInstance() {
            super(ModSoundEvents.ENDLESS_NIGHT_MUSIC, SoundCategory.AMBIENT, SoundInstance.createRandom());
            this.volume = 0.14F;
            this.pitch = 1.0F;
            this.repeat = true;
            this.repeatDelay = 0;
            this.relative = false;
            this.x = 0.0;
            this.y = 0.0;
            this.z = 0.0;
        }

        @Override
        public void tick() {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!active || client.player == null || client.world == null) {
                this.setDone();
                return;
            }

            this.setAnchor(client.player.getX(), client.player.getY(), client.player.getZ());
            this.volume = MathHelper.clamp(0.14F, 0.05F, 0.2F);
        }

        private void setAnchor(double anchorX, double anchorY, double anchorZ) {
            this.x = anchorX;
            this.y = anchorY;
            this.z = anchorZ;
        }
    }
}
