package net.jeremy.gardenkingmod;

import java.util.List;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.jeremy.gardenkingmod.crop.RottenCropDefinition;
import net.jeremy.gardenkingmod.crop.RottenCropDefinitions;
import net.jeremy.gardenkingmod.datagen.RottenHarvestJsonProvider;
import net.jeremy.gardenkingmod.datagen.RottenItemModelProvider;
import net.jeremy.gardenkingmod.datagen.RottenLanguageProvider;
import net.jeremy.gardenkingmod.ModItems;

public class GardenKingModDataGenerator implements DataGeneratorEntrypoint {
        @Override
        public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
                FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
                ModItems.getRottenItems();
                List<RottenCropDefinition> definitions = RottenCropDefinitions.all();

                pack.addProvider((FabricDataOutput output) -> new RottenItemModelProvider(output, definitions));
                pack.addProvider((FabricDataOutput output) -> new RottenLanguageProvider(output, definitions));
                pack.addProvider((FabricDataOutput output) -> new RottenHarvestJsonProvider(output, definitions));
        }
}
