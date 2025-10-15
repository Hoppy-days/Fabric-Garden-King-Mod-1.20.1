package net.jeremy.gardenkingmod.client.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Evaluates the Blockbench-authored sprinkler animation at runtime so we can
 * reuse the JSON timeline without manually recreating the keyframes in code.
 */
final class SprinklerRotationAnimation {
        private static final Identifier ANIMATION_ID = new Identifier(GardenKingMod.MOD_ID,
                        "animations/sprinkler.animation.json");

        private final float animationLengthSeconds;
        private final NavigableMap<Float, Float> yawKeyframes;

        private SprinklerRotationAnimation(float animationLengthSeconds, NavigableMap<Float, Float> yawKeyframes) {
                this.animationLengthSeconds = animationLengthSeconds;
                this.yawKeyframes = yawKeyframes;
        }

        static SprinklerRotationAnimation load() {
                NavigableMap<Float, Float> keyframes = new TreeMap<>();
                float animationLengthSeconds = 1.0F;

                ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
                Optional<Resource> resourceOptional = manager.getResource(ANIMATION_ID);
                if (resourceOptional.isPresent()) {
                        Resource resource = resourceOptional.get();
                        try (InputStream stream = resource.getInputStream();
                                        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                                JsonObject animations = root.getAsJsonObject("animations");
                                if (animations != null) {
                                        JsonObject animation = animations.getAsJsonObject("sprinkler_animation");
                                        if (animation != null) {
                                                if (animation.has("animation_length")) {
                                                        animationLengthSeconds = animation.get("animation_length").getAsFloat();
                                                }
                                                JsonObject bones = animation.getAsJsonObject("bones");
                                                if (bones != null) {
                                                        JsonObject rotationBone = bones.getAsJsonObject("rotation");
                                                        if (rotationBone != null) {
                                                                JsonObject rotation = rotationBone.getAsJsonObject("rotation");
                                                                if (rotation != null) {
                                                                        for (Map.Entry<String, JsonElement> entry : rotation.entrySet()) {
                                                                                float keyTime = parseTime(entry.getKey());
                                                                                float yaw = parseYaw(entry.getValue());
                                                                                keyframes.put(Float.valueOf(keyTime), Float.valueOf(yaw));
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        } catch (IOException ignored) {
                                // Fall through to default animation behaviour.
                        }
                }

                if (keyframes.isEmpty()) {
                        keyframes.put(Float.valueOf(0.0F), Float.valueOf(0.0F));
                        keyframes.put(Float.valueOf(animationLengthSeconds), Float.valueOf(-360.0F));
                }

                return new SprinklerRotationAnimation(animationLengthSeconds, keyframes);
        }

        float sampleYaw(float animationSeconds) {
                float duration = this.animationLengthSeconds;
                if (duration <= 0.0F || this.yawKeyframes.isEmpty()) {
                        return animationSeconds * ((float) Math.PI * 2.0F);
                }

                float localTime = animationSeconds % duration;
                if (localTime < 0.0F) {
                        localTime += duration;
                }

                Map.Entry<Float, Float> lower = this.yawKeyframes.floorEntry(Float.valueOf(localTime));
                Map.Entry<Float, Float> higher = this.yawKeyframes.higherEntry(Float.valueOf(localTime));

                if (lower == null) {
                        lower = this.yawKeyframes.lastEntry();
                }
                if (higher == null) {
                        higher = this.yawKeyframes.firstEntry();
                        if (higher == null) {
                                return 0.0F;
                        }
                        float wrappedUpperTime = higher.getKey().floatValue() + duration;
                        float lowerTime = lower.getKey().floatValue();
                        float t = MathHelper.clamp((localTime - lowerTime) / (wrappedUpperTime - lowerTime), 0.0F, 1.0F);
                        float lowerYaw = lower.getValue().floatValue();
                        float upperYaw = higher.getValue().floatValue() - 360.0F;
                        return MathHelper.lerp(t, lowerYaw, upperYaw) * MathHelper.RADIANS_PER_DEGREE;
                }

                float lowerTime = lower.getKey().floatValue();
                float upperTime = higher.getKey().floatValue();
                float t = upperTime == lowerTime ? 0.0F
                                : MathHelper.clamp((localTime - lowerTime) / (upperTime - lowerTime), 0.0F, 1.0F);
                float lowerYaw = lower.getValue().floatValue();
                float upperYaw = higher.getValue().floatValue();
                return MathHelper.lerp(t, lowerYaw, upperYaw) * MathHelper.RADIANS_PER_DEGREE;
        }

        private static float parseTime(String raw) {
                try {
                        return Float.parseFloat(raw);
                } catch (NumberFormatException exception) {
                        return 0.0F;
                }
        }

        private static float parseYaw(JsonElement element) {
                JsonArray values = null;
                if (element == null || element.isJsonNull()) {
                        return 0.0F;
                }
                if (element.isJsonArray()) {
                        values = element.getAsJsonArray();
                } else if (element.isJsonObject()) {
                        JsonObject object = element.getAsJsonObject();
                        if (object.has("post")) {
                                values = object.getAsJsonArray("post");
                        } else if (object.has("pre")) {
                                values = object.getAsJsonArray("pre");
                        }
                }

                if (values != null && values.size() >= 2) {
                        return values.get(1).getAsFloat();
                }
                return 0.0F;
        }
}
