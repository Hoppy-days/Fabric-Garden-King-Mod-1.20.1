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
 * Loads gear shop offers from {@code data/gardenkingmod/gear_shop_offers.json} so they can be
 * configured with simple {@code offer}/ {@code price} lines instead of hard-coded Java.
 */
public final class GearShopOfferManager implements SimpleSynchronousResourceReloadListener {
    private static final Identifier RELOAD_ID = new Identifier(GardenKingMod.MOD_ID, "gear_shop_offers");
    private static final Identifier OFFERS_FILE = new Identifier(GardenKingMod.MOD_ID, "gear_shop_offers.json");
    private static final GearShopOfferManager INSTANCE = new GearShopOfferManager();

    private static final int MAX_SUPPORTED_PAGES = 4;

    private volatile List<List<GearShopOffer>> pages = List.of();

    private GearShopOfferManager() {
    }

    public static GearShopOfferManager getInstance() {
        return INSTANCE;
    }

    public List<GearShopOffer> getOffers() {
        if (pages.isEmpty()) {
            return List.of();
        }

        List<GearShopOffer> flattened = new ArrayList<>();
        for (List<GearShopOffer> page : pages) {
            flattened.addAll(page);
        }

        return List.copyOf(flattened);
    }

    public List<List<GearShopOffer>> getOfferPages() {
        return pages;
    }

    public List<GearShopOffer> getOffersForPage(int pageIndex) {
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

    private List<List<GearShopOffer>> loadOfferPages(ResourceManager manager) {
        Optional<Resource> resourceOptional = manager.getResource(OFFERS_FILE);
        if (resourceOptional.isEmpty()) {
            GardenKingMod.LOGGER.warn("No gear shop offer file found at {}", OFFERS_FILE);
            return List.of();
        }

        List<List<GearShopOffer>> loadedPages = new ArrayList<>();
        try (InputStream stream = resourceOptional.get().getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonElement sanitized = JsonCommentHelper.sanitize(root);
            if (sanitized.isJsonObject()) {
                JsonObject rootObject = sanitized.getAsJsonObject();
                if (rootObject.has("pages")) {
                    parsePagesArray(rootObject.get("pages"), loadedPages);
                } else if (rootObject.has("offers")) {
                    List<GearShopOffer> page = parseOffersArray(rootObject.get("offers"), "root offers");
                    loadedPages.add(List.copyOf(page));
                } else {
                    GearShopOffer single = parseOfferObject(rootObject, "root object");
                    if (single != null) {
                        loadedPages.add(List.of(single));
                    }
                }
            } else if (sanitized.isJsonArray()) {
                List<GearShopOffer> page = parseOffersArray(sanitized, "root array");
                loadedPages.add(List.copyOf(page));
            } else {
                GardenKingMod.LOGGER.warn("gear_shop_offers.json must contain an array of offers");
            }
        } catch (IOException | JsonParseException exception) {
            GardenKingMod.LOGGER.error("Failed to load gear shop offers", exception);
        }

        if (loadedPages.size() > MAX_SUPPORTED_PAGES) {
            GardenKingMod.LOGGER.warn(
                    "Gear shop defines {} pages but the UI only exposes the first {} tabs", loadedPages.size(),
                    MAX_SUPPORTED_PAGES);
            loadedPages = loadedPages.subList(0, MAX_SUPPORTED_PAGES);
        }

        if (loadedPages.isEmpty()) {
            return List.of();
        }

        List<List<GearShopOffer>> immutablePages = new ArrayList<>(loadedPages.size());
        for (List<GearShopOffer> page : loadedPages) {
            immutablePages.add(List.copyOf(page));
        }
        return List.copyOf(immutablePages);
    }

    private void parsePagesArray(JsonElement element, List<List<GearShopOffer>> destination) {
        if (!element.isJsonArray()) {
            GardenKingMod.LOGGER.warn("Expected an array of pages in gear_shop_offers.json");
            return;
        }

        JsonArray pagesArray = element.getAsJsonArray();
        for (int pageIndex = 0; pageIndex < pagesArray.size(); pageIndex++) {
            JsonElement pageElement = pagesArray.get(pageIndex);
            List<GearShopOffer> pageOffers = parsePageElement(pageElement, pageIndex);
            if (pageOffers != null) {
                destination.add(pageOffers);
            }
        }
    }

    private List<GearShopOffer> parsePageElement(JsonElement pageElement, int pageIndex) {
        String context = "page " + (pageIndex + 1);
        if (pageElement.isJsonObject()) {
            JsonObject object = pageElement.getAsJsonObject();
            if (object.has("offers")) {
                return List.copyOf(parseOffersArray(object.get("offers"), context));
            }

            GearShopOffer single = parseOfferObject(object, context + " object");
            if (single != null) {
                return List.of(single);
            }
            return List.of();
        }

        if (pageElement.isJsonArray()) {
            return List.copyOf(parseOffersArray(pageElement, context));
        }

        GardenKingMod.LOGGER.warn("Skipping malformed gear shop page entry at {}: {}", context, pageElement);
        return null;
    }

    private List<GearShopOffer> parseOffersArray(JsonElement element, String context) {
        if (!element.isJsonArray()) {
            GardenKingMod.LOGGER.warn("Expected an array of offers {} in gear_shop_offers.json", context);
            return List.of();
        }

        List<GearShopOffer> offers = new ArrayList<>();
        JsonArray offersArray = element.getAsJsonArray();
        for (int index = 0; index < offersArray.size(); index++) {
            JsonElement offerElement = offersArray.get(index);
            if (!offerElement.isJsonObject()) {
                GardenKingMod.LOGGER.warn("Skipping malformed gear shop offer entry {} #{}: {}", context, index + 1,
                        offerElement);
                continue;
            }

            GearShopOffer offer = parseOfferObject(offerElement.getAsJsonObject(), context + " offer " + (index + 1));
            if (offer != null) {
                offers.add(offer);
            }
        }

        return offers;
    }

    private GearShopOffer parseOfferObject(JsonObject object, String context) {
        if (!object.has("offer")) {
            GardenKingMod.LOGGER.warn("Gear shop offer {} is missing an 'offer' field: {}", context, object);
            return null;
        }

        ItemStack result = parseStack(object.get("offer"), "offer");
        if (result.isEmpty()) {
            return null;
        }

        if (!object.has("price")) {
            GardenKingMod.LOGGER.warn("Gear shop offer {} for {} is missing a 'price' field", context,
                    describeStack(result));
            return null;
        }

        List<ItemStack> costs = parsePrice(object.get("price"));
        if (costs.isEmpty()) {
            GardenKingMod.LOGGER.warn("Gear shop offer {} for {} has no valid price entries", context,
                    describeStack(result));
            return null;
        }

        return GearShopOffer.of(result, costs);
    }

    private GearShopOffer parseOfferObject(JsonObject object) {
        return parseOfferObject(object, "entry");
    }

    private List<ItemStack> parsePrice(JsonElement priceElement) {
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
        return costs;
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
                GardenKingMod.LOGGER.warn("Gear shop {} entry is missing an 'item' field: {}", fieldName, object);
                return ItemStack.EMPTY;
            }
            String itemId = JsonHelper.getString(object, "item");
            int count = JsonHelper.getInt(object, "count", 1);
            return createStack(itemId, count, fieldName, preserveFullCount);
        }

        GardenKingMod.LOGGER.warn("Gear shop {} entry must be a string or object: {}", fieldName, element);
        return ItemStack.EMPTY;
    }

    private ItemStack parseDescriptor(String descriptor, String fieldName) {
        return parseDescriptor(descriptor, fieldName, false);
    }

    private ItemStack parseDescriptor(String descriptor, String fieldName, boolean preserveFullCount) {
        if (descriptor.isEmpty()) {
            GardenKingMod.LOGGER.warn("Gear shop {} entry cannot be empty", fieldName);
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
                    GardenKingMod.LOGGER.warn("Invalid stack count '{}' in gear shop {} entry '{}'", countPart, fieldName, descriptor);
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
            GardenKingMod.LOGGER.warn("Gear shop {} entry for '{}' must specify a positive count", fieldName, itemId);
            return ItemStack.EMPTY;
        }

        Identifier id = Identifier.tryParse(itemId);
        if (id == null) {
            GardenKingMod.LOGGER.warn("Gear shop {} entry '{}' is not a valid identifier", fieldName, itemId);
            return ItemStack.EMPTY;
        }

        Optional<Item> itemOptional = Registries.ITEM.getOrEmpty(id);
        if (itemOptional.isEmpty()) {
            GardenKingMod.LOGGER.warn("Gear shop {} entry references unknown item '{}'", fieldName, id);
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
}
