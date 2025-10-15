package net.jeremy.gardenkingmod.client.animation.sprinkler;

import net.minecraft.util.math.MathHelper;

/**
 * Blockbench export for the sprinkler timeline. The raw export lives at
 * {@code assets/gardenkingmod/animations/sprinkler/sprinkler_animation.java}.
 */
public final class sprinkler_animation {
        // The vanilla AnimationDefinition form exported by Blockbench is kept here
        // for reference:
        // AnimationDefinition.Builder.withLength(2.0F)
        //                .looping()
        //                .addAnimation("rotation",
        //                                new AnimationChannel(AnimationChannel.Targets.ROTATION,
        //                                                new Keyframe(0.0F,
        //                                                                KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
        //                                                                AnimationChannel.Interpolations.LINEAR),
        //                                                new Keyframe(0.25F,
        //                                                                KeyframeAnimations.degreeVec(0.0F, -45.0F, 0.0F),
        //                                                                AnimationChannel.Interpolations.LINEAR),
        //                                                new Keyframe(0.5F,
        //                                                                KeyframeAnimations.degreeVec(0.0F, -90.0F, 0.0F),
        //                                                                AnimationChannel.Interpolations.LINEAR),
        //                                                new Keyframe(0.75F,
        //                                                                KeyframeAnimations.degreeVec(0.0F, -135.0F, 0.0F),
        //                                                                AnimationChannel.Interpolations.LINEAR),
        //                                                new Keyframe(1.0F,
        //                                                                KeyframeAnimations.degreeVec(0.0F, -180.0F, 0.0F),
        //                                                                AnimationChannel.Interpolations.LINEAR),
        //                                                new Keyframe(1.25F,
        //                                                                KeyframeAnimations.degreeVec(0.0F, -225.0F, 0.0F),
        //                                                                AnimationChannel.Interpolations.LINEAR),
        //                                                new Keyframe(1.5F,
        //                                                                KeyframeAnimations.degreeVec(0.0F, -270.0F, 0.0F),
        //                                                                AnimationChannel.Interpolations.LINEAR),
        //                                                new Keyframe(1.75F,
        //                                                                KeyframeAnimations.degreeVec(0.0F, -315.0F, 0.0F),
        //                                                                AnimationChannel.Interpolations.LINEAR),
        //                                                new Keyframe(2.0F,
        //                                                                KeyframeAnimations.degreeVec(0.0F, -360.0F, 0.0F),
        //                                                                AnimationChannel.Interpolations.LINEAR)))
        //                .build();

        private static final float ANIMATION_LENGTH_SECONDS = 2.0F;
        private static final float[] KEYFRAME_TIMES = { 0.0F, 0.25F, 0.5F, 0.75F, 1.0F, 1.25F, 1.5F, 1.75F, 2.0F };
        private static final float[] KEYFRAME_YAWS = { 0.0F, -45.0F, -90.0F, -135.0F, -180.0F, -225.0F, -270.0F, -315.0F,
                        -360.0F };

        private sprinkler_animation() {
        }

        public static float sampleYawRadians(float animationSeconds) {
                float localTime = animationSeconds % ANIMATION_LENGTH_SECONDS;
                if (localTime < 0.0F) {
                        localTime += ANIMATION_LENGTH_SECONDS;
                }

                for (int i = 0; i < KEYFRAME_TIMES.length; i++) {
                        float startTime = KEYFRAME_TIMES[i];
                        float endTime = i == KEYFRAME_TIMES.length - 1 ? ANIMATION_LENGTH_SECONDS : KEYFRAME_TIMES[i + 1];

                        if (localTime < startTime) {
                                continue;
                        }

                        if (localTime > endTime && i != KEYFRAME_TIMES.length - 1) {
                                continue;
                        }

                        int nextIndex = (i + 1) % KEYFRAME_TIMES.length;
                        float nextTime = KEYFRAME_TIMES[nextIndex];
                        float wrappedEndTime = nextTime <= startTime ? nextTime + ANIMATION_LENGTH_SECONDS : nextTime;
                        float duration = wrappedEndTime - startTime;
                        float t;
                        if (duration <= 0.0F) {
                                t = 0.0F;
                        } else if (i == KEYFRAME_TIMES.length - 1) {
                                float wrappedLocal = localTime <= startTime ? localTime + ANIMATION_LENGTH_SECONDS : localTime;
                                t = MathHelper.clamp((wrappedLocal - startTime) / duration, 0.0F, 1.0F);
                        } else {
                                t = MathHelper.clamp((localTime - startTime) / duration, 0.0F, 1.0F);
                        }

                        float startYaw = KEYFRAME_YAWS[i];
                        float endYaw = KEYFRAME_YAWS[nextIndex];
                        if (nextIndex == 0) {
                                endYaw -= 360.0F;
                        }

                        float yawDegrees = MathHelper.lerp(t, startYaw, endYaw);
                        return yawDegrees * MathHelper.RADIANS_PER_DEGREE;
                }

                return 0.0F;
        }
}
