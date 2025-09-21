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
                translationBuilder.add("block.gardenkingmod.market_block", "Garden Market");
                translationBuilder.add("item.gardenkingmod.garden_coin", "Garden Coin");
                translationBuilder.add("container.gardenkingmod.market", "Garden Market");
                translationBuilder.add("screen.gardenkingmod.market.sell", "Sell");
                translationBuilder.add("screen.gardenkingmod.market.sale_result",
                                "You sold %1$s crops and earned %2$s garden coins.");
                translationBuilder.add("screen.gardenkingmod.market.sale_result_detailed",
                                "You sold %1$s and earned %2$s garden coins.");
                translationBuilder.add("screen.gardenkingmod.market.sale_result_item", "%1$s %2$s");
                translationBuilder.add("screen.gardenkingmod.market.lifetime",
                                "Lifetime earnings: %1$s garden coins.");
                translationBuilder.add("message.gardenkingmod.market.sold",
                                "Sold %1$s Croptopia crops for %2$s coins.");
                translationBuilder.add("message.gardenkingmod.market.sold.lifetime",
                                "Sold %1$s Croptopia crops for %2$s coins. Lifetime earnings: %3$s coins.");
                translationBuilder.add("message.gardenkingmod.market.empty", "There are no crops to sell.");
                translationBuilder.add("message.gardenkingmod.market.invalid", "Only Crops and Food can be sold here!");
                translationBuilder.add("scoreboard.gardenkingmod.garden_currency", "Garden Coins");
                translationBuilder.add("tooltip.gardenkingmod.crop_tier", "Tier: %s");
                translationBuilder.add("tooltip.gardenkingmod.crop_tier.tier_1", "Tier 1");
                translationBuilder.add("tooltip.gardenkingmod.crop_tier.tier_2", "Tier 2");
                translationBuilder.add("tooltip.gardenkingmod.crop_tier.tier_3", "Tier 3");
                translationBuilder.add("tooltip.gardenkingmod.crop_tier.tier_4", "Tier 4");
                translationBuilder.add("tooltip.gardenkingmod.crop_tier.tier_5", "Tier 5");

                definitions.forEach(definition -> translationBuilder.add(
                        definition.rottenItemId().toTranslationKey("item"),
                        "Rotten " + definition.displayName()));
    }
}
