package net.jeremy.gardenkingmod.block.sprinkler;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.util.Identifier;

public enum SprinklerTier {
        IRON("iron", 8, 2),
        GOLD("gold", 16, 3),
        DIAMOND("diamond", 32, 4),
        EMERALD("emerald", 64, 5);

        private final String name;
        private final int horizontalRadius;
        private final int verticalRadius;
        private final Identifier texture;

        SprinklerTier(String name, int horizontalRadius, int verticalRadius) {
                this.name = name;
                this.horizontalRadius = horizontalRadius;
                this.verticalRadius = verticalRadius;
                this.texture = new Identifier(GardenKingMod.MOD_ID,
                                "textures/entity/sprinkler/" + name + "_sprinkler.png");
        }

        public String getName() {
                return this.name;
        }

        public int getHorizontalRadius() {
                return this.horizontalRadius;
        }

        public int getVerticalRadius() {
                return this.verticalRadius;
        }

        public Identifier getTexture() {
                return this.texture;
        }
}
