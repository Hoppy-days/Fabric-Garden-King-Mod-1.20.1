package net.jeremy.gardenkingmod.util;

import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Utility methods for sanitizing data-driven JSON files that allow inline
 * documentation fields such as {@code "_comment"}.
 */
public final class JsonCommentHelper {
        private JsonCommentHelper() {
        }

        /**
         * Returns a deep copy of {@code element} with any {@code "_comment"}
         * properties stripped from every object in the tree.
         *
         * @param element The element to sanitize.
         * @return A deep copy with comment fields removed, or {@link JsonNull} if the
         *         input is {@code null}.
         */
        public static JsonElement sanitize(JsonElement element) {
                if (element == null || element.isJsonNull()) {
                        return JsonNull.INSTANCE;
                }

                JsonElement copy = element.deepCopy();
                stripComments(copy);
                return copy;
        }

        /**
         * Removes {@code "_comment"} properties from the supplied element in place.
         *
         * @param element The element to clean.
         */
        public static void stripComments(JsonElement element) {
                if (element == null || element.isJsonNull()) {
                        return;
                }

                if (element.isJsonObject()) {
                        JsonObject object = element.getAsJsonObject();
                        object.remove("_comment");
                        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                                stripComments(entry.getValue());
                        }
                } else if (element.isJsonArray()) {
                        JsonArray array = element.getAsJsonArray();
                        for (JsonElement child : array) {
                                stripComments(child);
                        }
                }
        }
}
