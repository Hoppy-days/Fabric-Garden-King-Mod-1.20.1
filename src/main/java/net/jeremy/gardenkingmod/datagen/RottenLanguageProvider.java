package net.jeremy.gardenkingmod.datagen;

import java.util.List;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import net.jeremy.gardenkingmod.crop.RottenCropDefinition;

/**
 * Writes English translations for each generated rotten crop item.
 */
public final class RottenLanguageProvider extends FabricLanguageProvider {
        private final List<RottenCropDefinition> definitions;

        public RottenLanguageProvider(FabricDataOutput dataOutput, List<RottenCropDefinition> definitions) {
                super(dataOutput);
                this.definitions = definitions;
        }

        @Override
        public void generateTranslations(TranslationBuilder translationBuilder) {
                definitions.forEach(definition -> translationBuilder.add(definition.rottenItemId(),
                                "Rotten " + definition.displayName()));
        }
}
