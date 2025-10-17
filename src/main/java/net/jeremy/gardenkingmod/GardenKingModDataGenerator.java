package net.jeremy.gardenkingmod;

import java.util.List;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.jeremy.gardenkingmod.crop.EnchantedCropDefinition;
import net.jeremy.gardenkingmod.crop.EnchantedCropDefinitions;
import net.jeremy.gardenkingmod.crop.RottenCropDefinition;
import net.jeremy.gardenkingmod.crop.RottenCropDefinitions;
import net.jeremy.gardenkingmod.datagen.EnchantedItemModelProvider;
import net.jeremy.gardenkingmod.datagen.EnchantedLanguageProvider;
import net.jeremy.gardenkingmod.datagen.RottenItemModelProvider;
import net.jeremy.gardenkingmod.datagen.RottenLanguageProvider;
import net.jeremy.gardenkingmod.ModItems;

public class GardenKingModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
                RottenCropDefinitions.reload();
                if (!ModItems.initializeRottenItems()) {
                        GardenKingMod.LOGGER.warn("No rotten crop definitions were available during data generation");
                }
                List<RottenCropDefinition> definitions = RottenCropDefinitions.all();
                EnchantedCropDefinitions.reload();
                if (!ModItems.initializeEnchantedItems()) {
                        GardenKingMod.LOGGER.warn("No enchanted crop definitions were available during data generation");
                }
                List<EnchantedCropDefinition> enchantedDefinitions = EnchantedCropDefinitions.all();

                pack.addProvider((FabricDataOutput output) -> new RottenItemModelProvider(output, definitions));
                pack.addProvider((FabricDataOutput output) -> new RottenLanguageProvider(output, definitions));
                pack.addProvider((FabricDataOutput output) -> new EnchantedItemModelProvider(output, enchantedDefinitions));
                pack.addProvider((FabricDataOutput output) -> new EnchantedLanguageProvider(output, enchantedDefinitions));
        }
}
