package net.jeremy.gardenkingmod.datagen;

import java.util.List;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;

import net.jeremy.gardenkingmod.crop.EnchantedCropDefinition;

import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureMap;
import net.minecraft.util.Identifier;

/**
 * Generates simple item models for each enchanted crop that point at the base
 * crop texture.
 */
public final class EnchantedItemModelProvider extends FabricModelProvider {
        private final List<EnchantedCropDefinition> definitions;

        public EnchantedItemModelProvider(FabricDataOutput dataOutput, List<EnchantedCropDefinition> definitions) {
                super(dataOutput);
                this.definitions = definitions;
        }

        @Override
        public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
                // No block states are required for enchanted items.
        }

        @Override
        public void generateItemModels(ItemModelGenerator itemModelGenerator) {
                definitions.forEach(definition -> {
                        Identifier enchantedItemId = definition.enchantedItemId();
                        Identifier textureId = new Identifier(definition.cropId().getNamespace(),
                                        "item/" + definition.cropId().getPath());
                        Models.GENERATED.upload(enchantedItemId, TextureMap.layer0(textureId),
                                        itemModelGenerator.writer);
                });
        }
}
