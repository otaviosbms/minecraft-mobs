package com.otavi.mobmod.client.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.otavi.mobmod.entity.genetics.Sex;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Floating panel above a mob, like Mo'Creatures: name, sex symbol and a health bar, plus a "z Z z" while it sleeps.
 * Drawn with the vanilla name tag pipeline, two lines stacked on top of each other.
 */
public final class MobInfoBar {
    private static final double MAX_DISTANCE = 16.0;
    private static final int HEALTH_SEGMENTS = 20;
    /**
     * Same line spacing as vanilla between the score and the name tag.
     */
    private static final float LINE_HEIGHT = 9.0F * 1.15F * 0.025F;

    private static final int MALE_COLOR = 0x55AAFF;
    private static final int FEMALE_COLOR = 0xFF77CC;
    private static final int EMPTY_COLOR = 0x404040;
    private static final int SLEEP_COLOR = 0xAACCFF;
    /**
     * Part of the hitbox height the panel comes down by while the animal lies asleep.
     */
    private static final float SLEEP_DROP = 0.4F;

    private MobInfoBar() {
    }

    static boolean shouldShow(ModMobRenderState state) {
        return !state.isInvisible && state.distanceToCameraSq < MAX_DISTANCE * MAX_DISTANCE;
    }

    static void submit(ModMobRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // Comes down with an animal lying asleep, so it does not float high above it
        float height = state.boundingBoxHeight * (1.0F - state.sleepAmount * SLEEP_DROP);
        var attachment = new Vec3(0.0, height + 0.5, 0.0);
        boolean seeThrough = !state.isDiscrete;

        poseStack.pushPose();
        collector.submitNameTag(poseStack, attachment, 0, healthBar(state.health, state.maxHealth), seeThrough, state.lightCoords, camera);
        poseStack.translate(0.0F, LINE_HEIGHT, 0.0F);
        collector.submitNameTag(poseStack, attachment, 0, title(state), seeThrough, state.lightCoords, camera);
        if (state.sleepAmount >= 1.0F) {
            poseStack.translate(0.0F, LINE_HEIGHT, 0.0F);
            collector.submitNameTag(poseStack, attachment, 0, snore(state.ageInTicks), seeThrough, state.lightCoords, camera);
        }
        poseStack.popPose();
    }

    /**
     * "z", "z Z", "z Z z", over and over: one more letter every half second.
     */
    private static Component snore(float ageInTicks) {
        int letters = 1 + (int) (ageInTicks / 10.0F) % 3;
        return Component.literal("z Z z".substring(0, letters * 2 - 1)).withColor(SLEEP_COLOR);
    }

    private static Component title(ModMobRenderState state) {
        MutableComponent title = state.displayName.copy();
        if (state.skinVariant != SkinVariant.NORMAL) {
            title.append(" (").append(Component.translatable(state.skinVariant.translationKey())).append(")");
        }
        boolean male = state.sex == Sex.MALE;
        return title.append(" ").append(Component.literal(male ? "♂" : "♀").withColor(male ? MALE_COLOR : FEMALE_COLOR));
    }

    private static Component healthBar(float health, float maxHealth) {
        float ratio = maxHealth > 0.0F ? Mth.clamp(health / maxHealth, 0.0F, 1.0F) : 0.0F;
        int filled = Mth.ceil(ratio * HEALTH_SEGMENTS);
        int color = ratio > 0.5F ? 0x55FF55 : ratio > 0.25F ? 0xFFFF55 : 0xFF5555;
        return Component.literal("|".repeat(filled)).withColor(color)
                .append(Component.literal("|".repeat(HEALTH_SEGMENTS - filled)).withColor(EMPTY_COLOR));
    }
}
