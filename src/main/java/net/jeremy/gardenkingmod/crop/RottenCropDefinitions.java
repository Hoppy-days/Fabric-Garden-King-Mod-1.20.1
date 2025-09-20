package net.jeremy.gardenkingmod.crop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import net.jeremy.gardenkingmod.GardenKingMod;

import net.minecraft.block.Block;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.GourdBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.PitcherCropBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

/**
 * Builds the list of {@link RottenCropDefinition definitions} that power both
 * runtime registration and data generation.
 */
public final class RottenCropDefinitions {
	private static List<RottenCropDefinition> allDefinitions = List.of();
	private static Map<Identifier, RottenCropDefinition> definitionsByTarget = Map.of();
	private static Map<Identifier, RottenCropDefinition> definitionsByCrop = Map.of();
	private static Map<Identifier, RottenCropDefinition> definitionsByLootTable = Map.of();
	private static Map<Identifier, RottenCropDefinition> definitionsByRottenItem = Map.of();
	private static boolean initialized;

	private RottenCropDefinitions() {
	}

	public static List<RottenCropDefinition> all() {
		ensureLoaded();
		return allDefinitions;
	}

	public static boolean hasDefinitions() {
		return initialized && !allDefinitions.isEmpty();
	}

	public static Optional<RottenCropDefinition> findByTargetId(Identifier identifier) {
		ensureLoaded();
		return Optional.ofNullable(definitionsByTarget.get(identifier));
	}

	public static Optional<RottenCropDefinition> findByCropId(Identifier identifier) {
		ensureLoaded();
		return Optional.ofNullable(definitionsByCrop.get(identifier));
	}

	public static Optional<RottenCropDefinition> findByLootTableId(Identifier identifier) {
		ensureLoaded();
		return Optional.ofNullable(definitionsByLootTable.get(identifier));
	}

	public static Optional<RottenCropDefinition> findByRottenItemId(Identifier identifier) {
		ensureLoaded();
		return Optional.ofNullable(definitionsByRottenItem.get(identifier));
	}

	public static synchronized void reload() {
		List<RottenCropDefinition> definitions = createDefinitions();
		allDefinitions = definitions;
		definitionsByTarget = indexBy(definitions, RottenCropDefinition::targetId);
		definitionsByCrop = indexBy(definitions, RottenCropDefinition::cropId);
		definitionsByLootTable = indexBy(definitions, RottenCropDefinition::lootTableId);
		definitionsByRottenItem = indexBy(definitions, RottenCropDefinition::rottenItemId);
		initialized = true;
	}

	private static void ensureLoaded() {
		if (!initialized) {
			reload();
		}
	}

	private static Map<Identifier, RottenCropDefinition> indexBy(List<RottenCropDefinition> definitions,
			Function<RottenCropDefinition, Identifier> keyExtractor) {
		Map<Identifier, RottenCropDefinition> map = new LinkedHashMap<>();
		for (RottenCropDefinition definition : definitions) {
			map.put(keyExtractor.apply(definition), definition);
		}

		return Collections.unmodifiableMap(map);
	}

	private static List<RottenCropDefinition> createDefinitions() {
                Map<Identifier, RottenCropDefinition> definitionsByTarget = new LinkedHashMap<>();
                Set<String> usedRottenPaths = new HashSet<>();

                for (RottenCropDefinition definition : manualDefinitions()) {
                        definitionsByTarget.put(definition.targetId(), definition);
                        usedRottenPaths.add(definition.rottenItemId().getPath());
                }

                Map<Identifier, RottenCropOverride> overrides = createOverrides();

                for (Identifier blockId : Registries.BLOCK.getIds()) {
                        if (GardenKingMod.MOD_ID.equals(blockId.getNamespace())) {
                                continue;
                        }

                        if (definitionsByTarget.containsKey(blockId)) {
                                continue;
                        }

                        Block block = Registries.BLOCK.get(blockId);
                        if (!isEligibleBlock(block, blockId)) {
                                continue;
                        }

                        Identifier lootTableId = block.getLootTableId();
                        if (LootTables.EMPTY.equals(lootTableId)) {
                                continue;
                        }

                        RottenCropOverride override = overrides.get(blockId);
                        Identifier targetId = override != null && override.targetId() != null ? override.targetId() : blockId;
                        Identifier cropId = override != null && override.cropId() != null ? override.cropId()
                                        : createCropIdentifier(blockId);
                        Identifier resolvedLootTable = override != null && override.lootTableId() != null ? override.lootTableId()
                                        : lootTableId;
                        float extraNoDropChance = override != null ? override.extraNoDropChance() : 0.0f;
                        float extraRottenChance = override != null ? override.extraRottenChance() : 0.0f;

                        String rottenItemPath = uniqueRottenPath(cropId.getPath(), blockId, usedRottenPaths);
                        usedRottenPaths.add(rottenItemPath);

                        RottenCropDefinition definition = new RottenCropDefinition(cropId, targetId, resolvedLootTable,
                                        rottenItemPath, extraNoDropChance, extraRottenChance);
                        definitionsByTarget.put(targetId, definition);
                }

                List<RottenCropDefinition> definitions = new ArrayList<>(definitionsByTarget.values());
                definitions.sort(Comparator.comparing(definition -> definition.rottenItemId().toString()));
                return List.copyOf(definitions);
        }

        private static Identifier createCropIdentifier(Identifier blockId) {
                String sanitizedPath = RottenCropDefinition.sanitizedPath(blockId.getPath());
                if (sanitizedPath.isEmpty()) {
                        sanitizedPath = blockId.getPath();
                }

                return new Identifier(blockId.getNamespace(), sanitizedPath);
        }

        private static String uniqueRottenPath(String basePath, Identifier blockId, Set<String> usedPaths) {
                String candidate = "rotten_" + basePath;

                if (usedPaths.contains(candidate)) {
                        if (!"minecraft".equals(blockId.getNamespace())) {
                                String namespaced = "rotten_" + blockId.getNamespace() + "_" + basePath;
                                if (!usedPaths.contains(namespaced)) {
                                        return namespaced;
                                }
                        }

                        int attempt = 2;
                        while (true) {
                                String numbered = "rotten_" + basePath + "_" + attempt;
                                if (!usedPaths.contains(numbered)) {
                                        return numbered;
                                }

                                attempt++;
                        }
                }

                return candidate;
        }

        private static boolean isEligibleBlock(Block block, Identifier blockId) {
                if (block == null) {
                        return false;
                }

                if (block.getDefaultState().isIn(BlockTags.CROPS)) {
                        return true;
                }

                if (block instanceof CropBlock || block instanceof GourdBlock || block instanceof NetherWartBlock
                                || block instanceof CocoaBlock || block instanceof SweetBerryBushBlock
                                || block instanceof PitcherCropBlock) {
                        return true;
                }

                String className = block.getClass().getName();
                if (className.startsWith("com.epherical.croptopia.blocks.")
                                && (className.endsWith("CroptopiaCropBlock") || className.endsWith("TallCropBlock")
                                                || className.endsWith("LeafCropBlock"))) {
                        return true;
                }

                return false;
        }

        private static List<RottenCropDefinition> manualDefinitions() {
                return List.of();
        }

        private static Map<Identifier, RottenCropOverride> createOverrides() {
                return Collections.emptyMap();
        }

        private record RottenCropOverride(Identifier cropId, Identifier targetId, Identifier lootTableId,
                        float extraNoDropChance, float extraRottenChance) {
        }
}
