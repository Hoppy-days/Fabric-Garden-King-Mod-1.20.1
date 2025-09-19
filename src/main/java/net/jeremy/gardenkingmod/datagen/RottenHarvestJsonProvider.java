package net.jeremy.gardenkingmod.datagen;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.crop.RottenCropDefinition;

import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

/**
 * Emits the rotten harvest configuration consumed by {@link GardenKingMod} at runtime.
 */
public final class RottenHarvestJsonProvider implements DataProvider {
        private final FabricDataOutput dataOutput;
        private final List<RottenCropDefinition> definitions;

        public RottenHarvestJsonProvider(FabricDataOutput dataOutput, List<RottenCropDefinition> definitions) {
                this.dataOutput = dataOutput;
                this.definitions = definitions;
        }

        @Override
        public CompletableFuture<?> run(DataWriter writer) {
                JsonObject root = new JsonObject();

                definitions.forEach(definition -> {
                        JsonObject entry = new JsonObject();
                        entry.addProperty("rotten_item", definition.rottenItemId().toString());

                        if (definition.hasExtraNoDropChance()) {
                                entry.addProperty("extra_no_drop_chance", definition.extraNoDropChance());
                        }

                        if (definition.hasExtraRottenChance()) {
                                entry.addProperty("extra_rotten_chance", definition.extraRottenChance());
                        }

                        root.add(definition.targetId().toString(), entry);
                });

                DataOutput.PathResolver resolver = dataOutput.getResolver(DataOutput.OutputType.DATA_PACK, "rotten_harvest");
                Path path = resolver.resolveJson(new Identifier(GardenKingMod.MOD_ID, "rotten_harvest"));
                return DataProvider.writeToPath(writer, root, path);
        }

        @Override
        public String getName() {
                return "Garden King Rotten Harvest";
        }
}
