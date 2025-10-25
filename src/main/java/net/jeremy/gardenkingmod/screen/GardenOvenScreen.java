package net.jeremy.gardenkingmod.screen;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.recipebook.FurnaceRecipeBookScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GardenOvenScreen extends AbstractFurnaceScreen<GardenOvenScreenHandler> {
        private static final Identifier TEXTURE = new Identifier(GardenKingMod.MOD_ID,
                        "textures/gui/container/oven_gui.png");

        public GardenOvenScreen(GardenOvenScreenHandler handler, PlayerInventory inventory, Text title) {
                super(handler, new FurnaceRecipeBookScreen(), inventory, title, TEXTURE);
        }
}
