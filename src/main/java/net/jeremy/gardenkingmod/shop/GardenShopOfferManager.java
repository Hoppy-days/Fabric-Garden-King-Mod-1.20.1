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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

/**
 * Loads garden shop offers from {@code data/gardenkingmod/garden_shop_offers.json} so they can be
 * configured with simple {@code offer}/ {@code price} lines instead of hard-coded Java.
 */
public final class GardenShopOfferManager implements SimpleSynchronousResourceReloadListener {
    private static final Identifier RELOAD_ID = new Identifier(GardenKingMod.MOD_ID, "garden_shop_offers");
    private static final Identifier OFFERS_FILE = new Identifier(GardenKingMod.MOD_ID, "garden_shop_offers.json");
    private static final GardenShopOfferManager INSTANCE = new GardenShopOfferManager();

    private static final int MAX_SUPPORTED_PAGES = 5;

    private volatile List<List<GardenShopOffer>> pages = List.of();

    private GardenShopOfferManager() {
    }

    public static GardenShopOfferManager getInstance() {
        return INSTANCE;
    }

    public List<GardenShopOffer> getOffers() {
        if (pages.isEmpty()) {
            return List.of();
        }

        List<GardenShopOffer> flattened = new ArrayList<>();
        for (List<GardenShopOffer> page : pages) {
            flattened.addAll(page);
        }

        return List.copyOf(flattened);
    }

    public List<List<GardenShopOffer>> getOfferPages() {
        return pages;
    }

    public List<GardenShopOffer> getOffersForPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pages.size()) {
            return List.of();
        }

        return pages.get(pageIndex);
    }

    public int getPageCount() {
        return pages.size();
    }

    @Override
    public Identifier getFabricId() {
        return RELOAD_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        pages = loadOfferPages(manager);
    }

    private List<List<GardenShopOffer>> loadOfferPages(ResourceManager manager) {
        Optional<Resource> resourceOptional = manager.getResource(OFFERS_FILE);
        if (resourceOptional.isEmpty()) {
            GardenKingMod.LOGGER.warn("No garden shop offer file found at {}", OFFERS_FILE);
            return List.of();
        }

        List<List<GardenShopOffer>> loadedPages = new ArrayList<>();
        try (InputStream stream = resourceOptional.get().getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root.isJsonObject()) {
                JsonObject rootObject = root.getAsJsonObject();
                if (rootObject.has("pages")) {
                    parsePagesArray(rootObject.get("pages"), loadedPages);
                } else if (rootObject.has("offers")) {
                    List<GardenShopOffer> page = parseOffersArray(rootObject.get("offers"), "root offers");
                    loadedPages.add(List.copyOf(page));
                } else {
                    GardenShopOffer single = parseOfferObject(rootObject, "root object");
                    if (single != null) {
                        loadedPages.add(List.of(single));
                    }
                }
            } else if (root.isJsonArray()) {
                List<GardenShopOffer> page = parseOffersArray(root, "root array");
                loadedPages.add(List.copyOf(page));
            } else {
                GardenKingMod.LOGGER.warn("garden_shop_offers.json must contain an array of offers");
            }
        } catch (IOException | JsonParseException exception) {
            GardenKingMod.LOGGER.error("Failed to load garden shop offers", exception);
        }

        if (loadedPages.size() > MAX_SUPPORTED_PAGES) {
            GardenKingMod.LOGGER.warn(
                    "Garden shop defines {} pages but the UI only exposes the first {} tabs", loadedPages.size(),
                    MAX_SUPPORTED_PAGES);
            loadedPages = loadedPages.subList(0, MAX_SUPPORTED_PAGES);
        }

        if (loadedPages.isEmpty()) {
            return List.of();
        }

        List<List<GardenShopOffer>> immutablePages = new ArrayList<>(loadedPages.size());
        for (List<GardenShopOffer> page : loadedPages) {
            immutablePages.add(List.copyOf(page));
        }
        return List.copyOf(immutablePages);
    }

    private void parsePagesArray(JsonElement element, List<List<GardenShopOffer>> destination) {
        if (!element.isJsonArray()) {
            GardenKingMod.LOGGER.warn("Expected an array of pages in garden_shop_offers.json");
            return;
        }

        JsonArray pagesArray = element.getAsJsonArray();
        for (int pageIndex = 0; pageIndex < pagesArray.size(); pageIndex++) {
            JsonElement pageElement = pagesArray.get(pageIndex);
            List<GardenShopOffer> pageOffers = parsePageElement(pageElement, pageIndex);
            if (pageOffers != null) {
                destination.add(pageOffers);
            }
        }
    }

    private List<GardenShopOffer> parsePageElement(JsonElement pageElement, int pageIndex) {
        String context = "page " + (pageIndex + 1);
        if (pageElement.isJsonObject()) {
            JsonObject object = pageElement.getAsJsonObject();
            if (object.has("offers")) {
                return List.copyOf(parseOffersArray(object.get("offers"), context));
            }

            GardenShopOffer single = parseOfferObject(object, context + " object");
            if (single != null) {
                return List.of(single);
            }
            return List.of();
        }

        if (pageElement.isJsonArray()) {
            return List.copyOf(parseOffersArray(pageElement, context));
        }

        GardenKingMod.LOGGER.warn("Skipping malformed garden shop page entry at {}: {}", context, pageElement);
        return null;
    }

    private List<GardenShopOffer> parseOffersArray(JsonElement element, String context) {
        if (!element.isJsonArray()) {
            GardenKingMod.LOGGER.warn("Expected an array of offers {} in garden_shop_offers.json", context);
            return List.of();
        }

        List<GardenShopOffer> offers = new ArrayList<>();
        JsonArray offersArray = element.getAsJsonArray();
        for (int index = 0; index < offersArray.size(); index++) {
            JsonElement offerElement = offersArray.get(index);
            if (!offerElement.isJsonObject()) {
                GardenKingMod.LOGGER.warn("Skipping malformed garden shop offer entry {} #{}: {}", context, index + 1,
                        offerElement);
                continue;
            }

            GardenShopOffer offer = parseOfferObject(offerElement.getAsJsonObject(), context + " offer " + (index + 1));
            if (offer != null) {
                offers.add(offer);
            }
        }

        return offers;
    }

    private GardenShopOffer parseOfferObject(JsonObject object, String context) {
        if (!object.has("offer")) {
            GardenKingMod.LOGGER.warn("Garden shop offer {} is missing an 'offer' field: {}", context, object);
            return null;
        }

        ItemStack result = parseStack(object.get("offer"), "offer");
        if (result.isEmpty()) {
            return null;
        }

        if (!object.has("price")) {
            GardenKingMod.LOGGER.warn("Garden shop offer {} for {} is missing a 'price' field", context,
                    describeStack(result));
            return null;
        }

        List<ItemStack> costs = parsePrice(object.get("price"));
        if (costs.isEmpty()) {
            GardenKingMod.LOGGER.warn("Garden shop offer {} for {} has no valid price entries", context,
                    describeStack(result));
            return null;
        }

        return GardenShopOffer.of(result, costs);
    }

    private GardenShopOffer parseOfferObject(JsonObject object) {
        return parseOfferObject(object, "entry");
    }

    private List<ItemStack> parsePrice(JsonElement priceElement) {
        List<ItemStack> costs = new ArrayList<>();
        if (priceElement.isJsonArray()) {
            JsonArray priceArray = priceElement.getAsJsonArray();
            for (JsonElement costElement : priceArray) {
                ItemStack cost = parseStack(costElement, "price");
                if (!cost.isEmpty()) {
                    costs.add(cost);
                }
            }
        } else {
            ItemStack cost = parseStack(priceElement, "price");
            if (!cost.isEmpty()) {
                costs.add(cost);
            }
        }
        return costs;
    }

    private ItemStack parseStack(JsonElement element, String fieldName) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return parseDescriptor(element.getAsString().trim(), fieldName);
        }

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (!object.has("item")) {
                GardenKingMod.LOGGER.warn("Garden shop {} entry is missing an 'item' field: {}", fieldName, object);
                return ItemStack.EMPTY;
            }
            String itemId = JsonHelper.getString(object, "item");
            int count = JsonHelper.getInt(object, "count", 1);
            return createStack(itemId, count, fieldName);
        }

        GardenKingMod.LOGGER.warn("Garden shop {} entry must be a string or object: {}", fieldName, element);
        return ItemStack.EMPTY;
    }

    private ItemStack parseDescriptor(String descriptor, String fieldName) {
        if (descriptor.isEmpty()) {
            GardenKingMod.LOGGER.warn("Garden shop {} entry cannot be empty", fieldName);
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
                    GardenKingMod.LOGGER.warn("Invalid stack count '{}' in garden shop {} entry '{}'", countPart, fieldName, descriptor);
                    return ItemStack.EMPTY;
                }
            }
        }

        return createStack(itemPart, count, fieldName);
    }

    private ItemStack createStack(String itemId, int count, String fieldName) {
        if (count <= 0) {
            GardenKingMod.LOGGER.warn("Garden shop {} entry for '{}' must specify a positive count", fieldName, itemId);
            return ItemStack.EMPTY;
        }

        Identifier id = Identifier.tryParse(itemId);
        if (id == null) {
            GardenKingMod.LOGGER.warn("Garden shop {} entry '{}' is not a valid identifier", fieldName, itemId);
            return ItemStack.EMPTY;
        }

        Optional<Item> itemOptional = Registries.ITEM.getOrEmpty(id);
        if (itemOptional.isEmpty()) {
            GardenKingMod.LOGGER.warn("Garden shop {} entry references unknown item '{}'", fieldName, id);
            return ItemStack.EMPTY;
        }

        return new ItemStack(itemOptional.get(), count);
    }

    private static String describeStack(ItemStack stack) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return stack.getCount() + "x " + (id != null ? id.toString() : stack.getName().getString());
    }
}
