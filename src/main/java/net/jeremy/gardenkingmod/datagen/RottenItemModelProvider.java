package net.jeremy.gardenkingmod.datagen;

import java.util.List;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;

import net.jeremy.gardenkingmod.ModItems;
import net.jeremy.gardenkingmod.crop.RottenCropDefinition;

import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.item.Item;

/**
 * Generates simple generated item models for every rotten crop item.
 */
public final class RottenItemModelProvider extends FabricModelProvider {
        private final List<RottenCropDefinition> definitions;

        public RottenItemModelProvider(FabricDataOutput dataOutput, List<RottenCropDefinition> definitions) {
                super(dataOutput);
                this.definitions = definitions;
        }

        @Override
        public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
                // No block states are required for rotten items.
        }

        @Override
        public void generateItemModels(ItemModelGenerator itemModelGenerator) {
                definitions.forEach(definition -> {
                        Item rottenItem = ModItems.getRottenItemForTarget(definition.targetId());
                        if (rottenItem != null) {
                                itemModelGenerator.register(rottenItem, Models.GENERATED);
                        }
                });
        }
}
