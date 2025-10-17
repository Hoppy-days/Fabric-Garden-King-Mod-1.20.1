package net.jeremy.gardenkingmod.datagen;

import java.util.List;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import net.jeremy.gardenkingmod.crop.EnchantedCropDefinition;

/**
 * Writes English translations for each enchanted crop item.
 */
public final class EnchantedLanguageProvider extends FabricLanguageProvider {
        private final List<EnchantedCropDefinition> definitions;

        public EnchantedLanguageProvider(FabricDataOutput dataOutput, List<EnchantedCropDefinition> definitions) {
                super(dataOutput);
                this.definitions = definitions;
        }

        @Override
        public void generateTranslations(TranslationBuilder translationBuilder) {
                definitions.forEach(definition -> translationBuilder.add(
                                definition.enchantedItemId().toTranslationKey("item"),
                                "Enchanted " + definition.displayName()));
        }
}
