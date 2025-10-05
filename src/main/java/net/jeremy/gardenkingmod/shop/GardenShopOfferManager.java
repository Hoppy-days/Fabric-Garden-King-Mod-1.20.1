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

    private volatile List<GardenShopOffer> offers = List.of();

    private GardenShopOfferManager() {
    }

    public static GardenShopOfferManager getInstance() {
        return INSTANCE;
    }

    public List<GardenShopOffer> getOffers() {
        return offers;
    }

    @Override
    public Identifier getFabricId() {
        return RELOAD_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        offers = loadOffers(manager);
    }

    private List<GardenShopOffer> loadOffers(ResourceManager manager) {
        Optional<Resource> resourceOptional = manager.getResource(OFFERS_FILE);
        if (resourceOptional.isEmpty()) {
            GardenKingMod.LOGGER.warn("No garden shop offer file found at {}", OFFERS_FILE);
            return List.of();
        }

        List<GardenShopOffer> loaded = new ArrayList<>();
        try (InputStream stream = resourceOptional.get().getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root.isJsonObject()) {
                JsonObject rootObject = root.getAsJsonObject();
                if (rootObject.has("offers")) {
                    parseOffersArray(rootObject.get("offers"), loaded);
                } else {
                    GardenShopOffer single = parseOfferObject(rootObject);
                    if (single != null) {
                        loaded.add(single);
                    }
                }
            } else if (root.isJsonArray()) {
                parseOffersArray(root, loaded);
            } else {
                GardenKingMod.LOGGER.warn("garden_shop_offers.json must contain an array of offers");
            }
        } catch (IOException | JsonParseException exception) {
            GardenKingMod.LOGGER.error("Failed to load garden shop offers", exception);
        }

        if (loaded.isEmpty()) {
            return List.of();
        }

        return List.copyOf(loaded);
    }

    private void parseOffersArray(JsonElement element, List<GardenShopOffer> destination) {
        if (!element.isJsonArray()) {
            GardenKingMod.LOGGER.warn("Expected an array of offers in garden_shop_offers.json");
            return;
        }

        JsonArray offersArray = element.getAsJsonArray();
        for (JsonElement offerElement : offersArray) {
            if (!offerElement.isJsonObject()) {
                GardenKingMod.LOGGER.warn("Skipping malformed garden shop offer entry: {}", offerElement);
                continue;
            }

            GardenShopOffer offer = parseOfferObject(offerElement.getAsJsonObject());
            if (offer != null) {
                destination.add(offer);
            }
        }
    }

    private GardenShopOffer parseOfferObject(JsonObject object) {
        if (!object.has("offer")) {
            GardenKingMod.LOGGER.warn("Garden shop offer entry is missing an 'offer' field: {}", object);
            return null;
        }

        ItemStack result = parseStack(object.get("offer"), "offer");
        if (result.isEmpty()) {
            return null;
        }

        if (!object.has("price")) {
            GardenKingMod.LOGGER.warn("Garden shop offer entry for {} is missing a 'price' field", describeStack(result));
            return null;
        }

        List<ItemStack> costs = parsePrice(object.get("price"));
        if (costs.isEmpty()) {
            GardenKingMod.LOGGER.warn("Garden shop offer entry for {} has no valid price entries", describeStack(result));
            return null;
        }

        return GardenShopOffer.of(result, costs);
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
