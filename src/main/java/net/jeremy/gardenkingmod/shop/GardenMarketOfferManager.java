package net.jeremy.gardenkingmod.shop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.util.JsonCommentHelper;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

/**
 * Loads the Garden Market's buy offers from {@code data/gardenkingmod/garden_market_offers.json}.
 */
public final class GardenMarketOfferManager implements SimpleSynchronousResourceReloadListener {
    private static final Identifier RELOAD_ID = new Identifier(GardenKingMod.MOD_ID, "garden_market_offers");
    private static final Identifier OFFERS_FILE = new Identifier(GardenKingMod.MOD_ID, "garden_market_offers.json");
    private static final GardenMarketOfferManager INSTANCE = new GardenMarketOfferManager();
    private static final int DEFAULT_MIN_OFFERS = 5;
    private static final int DEFAULT_MAX_OFFERS = 8;
    private static final int DEFAULT_REFRESH_MINUTES = 30;
    private static final boolean DEFAULT_SHOW_ALL_OFFERS = false;

    private volatile List<GearShopOffer> offers = List.of();
    private volatile int minOffers = DEFAULT_MIN_OFFERS;
    private volatile int maxOffers = DEFAULT_MAX_OFFERS;
    private volatile int refreshMinutes = DEFAULT_REFRESH_MINUTES;
    private volatile boolean showAllOffers = DEFAULT_SHOW_ALL_OFFERS;

    private GardenMarketOfferManager() {
    }

    public static GardenMarketOfferManager getInstance() {
        return INSTANCE;
    }

    public List<GearShopOffer> getMasterOffers() {
        return offers;
    }

    public List<GearShopOffer> getOffersByIndices(List<Integer> indices) {
        if (indices == null || indices.isEmpty()) {
            return List.of();
        }

        List<GearShopOffer> master = offers;
        if (master.isEmpty()) {
            return List.of();
        }

        List<GearShopOffer> resolved = new ArrayList<>(indices.size());
        for (Integer index : indices) {
            if (index == null) {
                continue;
            }
            int offerIndex = index;
            if (offerIndex >= 0 && offerIndex < master.size()) {
                resolved.add(master.get(offerIndex));
            }
        }
        return List.copyOf(resolved);
    }

    public int getMinOffers() {
        return minOffers;
    }

    public int getMaxOffers() {
        return maxOffers;
    }

    public long getRefreshIntervalTicks() {
        int minutes = Math.max(1, refreshMinutes);
        return minutes * 60L * 20L;
    }

    public boolean shouldShowAllOffers() {
        return showAllOffers;
    }

    @Override
    public Identifier getFabricId() {
        return RELOAD_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        offers = loadOffers(manager);
    }

    private List<GearShopOffer> loadOffers(ResourceManager manager) {
        minOffers = DEFAULT_MIN_OFFERS;
        maxOffers = DEFAULT_MAX_OFFERS;
        refreshMinutes = DEFAULT_REFRESH_MINUTES;
        showAllOffers = DEFAULT_SHOW_ALL_OFFERS;

        Optional<Resource> resourceOptional = manager.getResource(OFFERS_FILE);
        if (resourceOptional.isEmpty()) {
            GardenKingMod.LOGGER.warn("No garden market offer file found at {}", OFFERS_FILE);
            return List.of();
        }

        List<GearShopOffer> loadedOffers = new ArrayList<>();
        try (InputStream stream = resourceOptional.get().getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonElement sanitized = JsonCommentHelper.sanitize(root);
            if (sanitized.isJsonObject()) {
                JsonObject rootObject = sanitized.getAsJsonObject();
                parseSettings(rootObject);
                if (rootObject.has("offers")) {
                    loadedOffers = parseOffersArray(rootObject.get("offers"), "root offers");
                } else {
                    GearShopOffer single = parseOfferObject(rootObject, "root object");
                    if (single != null) {
                        loadedOffers = List.of(single);
                    }
                }
            } else if (sanitized.isJsonArray()) {
                loadedOffers = parseOffersArray(sanitized.getAsJsonArray(), "root array");
            } else {
                GardenKingMod.LOGGER.warn("garden_market_offers.json must define an array or object of offers");
            }
        } catch (IOException | JsonParseException exception) {
            GardenKingMod.LOGGER.error("Failed to load garden market offers", exception);
        }

        if (loadedOffers.isEmpty()) {
            return List.of();
        }

        return List.copyOf(loadedOffers);
    }

    private List<GearShopOffer> parseOffersArray(JsonElement element, String context) {
        if (!element.isJsonArray()) {
            GardenKingMod.LOGGER.warn("Expected an array of offers {} in garden_market_offers.json", context);
            return List.of();
        }

        List<GearShopOffer> parsedOffers = new ArrayList<>();
        JsonArray offersArray = element.getAsJsonArray();
        for (int index = 0; index < offersArray.size(); index++) {
            JsonElement offerElement = offersArray.get(index);
            if (!offerElement.isJsonObject()) {
                GardenKingMod.LOGGER.warn("Skipping malformed garden market offer {} #{}: {}", context, index + 1,
                        offerElement);
                continue;
            }

            GearShopOffer offer = parseOfferObject(offerElement.getAsJsonObject(), context + " offer " + (index + 1));
            if (offer != null) {
                parsedOffers.add(offer);
            }
        }

        return parsedOffers;
    }

    private void parseSettings(JsonObject rootObject) {
        if (rootObject.has("settings") && rootObject.get("settings").isJsonObject()) {
            applySettings(rootObject.getAsJsonObject("settings"));
        }
        if (rootObject.has("min_offers") || rootObject.has("max_offers") || rootObject.has("refresh_minutes")
                || rootObject.has("show_all_offers")) {
            applySettings(rootObject);
        }
    }

    private void applySettings(JsonObject object) {
        minOffers = JsonHelper.getInt(object, "min_offers", minOffers);
        maxOffers = JsonHelper.getInt(object, "max_offers", maxOffers);
        refreshMinutes = JsonHelper.getInt(object, "refresh_minutes", refreshMinutes);
        showAllOffers = JsonHelper.getBoolean(object, "show_all_offers", showAllOffers);
        if (maxOffers < minOffers) {
            maxOffers = minOffers;
        }
    }

    private GearShopOffer parseOfferObject(JsonObject object, String context) {
        if (!object.has("offer")) {
            GardenKingMod.LOGGER.warn("Garden market offer {} is missing an 'offer' field: {}", context, object);
            return null;
        }

        ItemStack result = parseStack(object.get("offer"), "offer");
        if (result.isEmpty()) {
            return null;
        }

        if (!object.has("price")) {
            GardenKingMod.LOGGER.warn("Garden market offer {} for {} is missing a 'price' field", context,
                    describeStack(result));
            return null;
        }

        JsonElement priceElement = object.get("price");
        List<ItemStack> costs = new ArrayList<>();
        if (priceElement.isJsonArray()) {
            JsonArray priceArray = priceElement.getAsJsonArray();
            for (JsonElement costElement : priceArray) {
                ItemStack cost = parseStack(costElement, "price", true);
                if (!cost.isEmpty()) {
                    costs.add(cost);
                }
            }
        } else {
            ItemStack cost = parseStack(priceElement, "price", true);
            if (!cost.isEmpty()) {
                costs.add(cost);
            }
        }

        if (costs.isEmpty()) {
            GardenKingMod.LOGGER.warn("Garden market offer {} for {} has no valid price entries", context,
                    describeStack(result));
            return null;
        }

        return GearShopOffer.of(result, costs);
    }

    private ItemStack parseStack(JsonElement element, String fieldName) {
        return parseStack(element, fieldName, false);
    }

    private ItemStack parseStack(JsonElement element, String fieldName, boolean preserveFullCount) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return parseDescriptor(element.getAsString().trim(), fieldName, preserveFullCount);
        }

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (!object.has("item")) {
                GardenKingMod.LOGGER.warn("Garden market {} entry is missing an 'item' field: {}", fieldName, object);
                return ItemStack.EMPTY;
            }
            String itemId = JsonHelper.getString(object, "item");
            int count = JsonHelper.getInt(object, "count", 1);
            return createStack(itemId, count, fieldName, preserveFullCount);
        }

        GardenKingMod.LOGGER.warn("Garden market {} entry must be a string or object: {}", fieldName, element);
        return ItemStack.EMPTY;
    }

    private ItemStack parseDescriptor(String descriptor, String fieldName) {
        return parseDescriptor(descriptor, fieldName, false);
    }

    private ItemStack parseDescriptor(String descriptor, String fieldName, boolean preserveFullCount) {
        if (descriptor.isEmpty()) {
            GardenKingMod.LOGGER.warn("Garden market {} entry cannot be empty", fieldName);
            return ItemStack.EMPTY;
        }

        String itemPart = descriptor;
        int count = 1;
        int multiplierIndex = descriptor.indexOf('*');
        if (multiplierIndex >= 0) {
            itemPart = descriptor.substring(0, multiplierIndex).trim();
            String countPart = descriptor.substring(multiplierIndex + 1).trim();
            if (!countPart.isEmpty()) {
                try {
                    count = Integer.parseInt(countPart);
                } catch (NumberFormatException exception) {
                    GardenKingMod.LOGGER.warn("Invalid stack count '{}' in garden market {} entry '{}'", countPart,
                            fieldName, descriptor);
                    return ItemStack.EMPTY;
                }
            }
        }

        return createStack(itemPart, count, fieldName, preserveFullCount);
    }

    private ItemStack createStack(String itemId, int count, String fieldName) {
        return createStack(itemId, count, fieldName, false);
    }

    private ItemStack createStack(String itemId, int count, String fieldName, boolean preserveFullCount) {
        if (count <= 0) {
            GardenKingMod.LOGGER.warn("Garden market {} entry for '{}' must specify a positive count", fieldName, itemId);
            return ItemStack.EMPTY;
        }

        Identifier id = Identifier.tryParse(itemId);
        if (id == null) {
            GardenKingMod.LOGGER.warn("Garden market {} entry '{}' is not a valid identifier", fieldName, itemId);
            return ItemStack.EMPTY;
        }

        Optional<Item> itemOptional = resolveItem(id);
        if (itemOptional.isEmpty()) {
            GardenKingMod.LOGGER.warn("Garden market {} entry references unknown item '{}'", fieldName, id);
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(itemOptional.get());
        if (preserveFullCount) {
            GearShopStackHelper.applyRequestedCount(stack, count);
        } else {
            stack.setCount(Math.min(count, stack.getMaxCount()));
        }
        return stack;
    }

    private static String describeStack(ItemStack stack) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return stack.getCount() + "x " + (id != null ? id.toString() : stack.getName().getString());
    }

    private Optional<Item> resolveItem(Identifier id) {
        Optional<Item> itemOptional = Registries.ITEM.getOrEmpty(id);
        if (itemOptional.isPresent()) {
            return itemOptional;
        }

        String path = id.getPath();
        if (path.endsWith("_crop")) {
            Identifier fallbackId = new Identifier(id.getNamespace(), path.substring(0, path.length() - "_crop".length()));
            itemOptional = Registries.ITEM.getOrEmpty(fallbackId);
            if (itemOptional.isPresent()) {
                return itemOptional;
            }
        }

        return Optional.empty();
    }
}
