package com.otavi.mobmod.entity.traits;

import com.otavi.mobmod.entity.genetics.GeneticMob;
import com.otavi.mobmod.entity.genetics.Sex;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Every animal of the mod, made from scratch ({@link com.otavi.mobmod.entity.genetics.ModAnimal}) or adapted from a
 * vanilla one (the camel, through a mixin): a sex, a skin variant, health regeneration and sleep, all run by its
 * {@link AnimalTraits}. Renderers, the info bar, goals and tests only rely on this interface.
 * <p>
 * Defaults hold the shared rules; an animal overrides them to tune its own values.
 */
public interface ModCreature extends GeneticMob {
    /**
     * The state and logic of the shared systems. Prefixed because mixins add this method to vanilla classes.
     */
    AnimalTraits mobmod$traits();

    /**
     * The animal as a mod creature, or null when it is not one: not an animal of the mod, or a vanilla subclass
     * that is left as it is (the camel husk extends the camel).
     */
    @Nullable
    static ModCreature of(@Nullable Entity entity) {
        return entity instanceof ModCreature creature && creature.isModCreature() ? creature : null;
    }

    /**
     * False for subclasses of an adapted vanilla animal that keep their vanilla behavior.
     */
    default boolean isModCreature() {
        return true;
    }

    /*==============*/
    /*   GENETICS   */
    /*==============*/

    @Override
    default Sex getSex() {
        return this.mobmod$traits().getSex();
    }

    default void setSex(Sex sex) {
        this.mobmod$traits().setSex(sex);
    }

    @Override
    default SkinVariant getSkinVariant() {
        return this.mobmod$traits().getSkinVariant();
    }

    default void setSkinVariant(SkinVariant variant) {
        this.mobmod$traits().setSkinVariant(variant);
    }

    /**
     * How much bigger males are than females (0.15 = 15%). Applied through the scale attribute, so the hitbox grows too.
     */
    default double maleScaleBonus() {
        return 0.15;
    }

    /*==================*/
    /*   REGENERATION   */
    /*==================*/

    /**
     * Seconds to go from 0 to full health by regeneration alone: every mob heals at a pace proportional to its health.
     */
    default int regenFullHealthSeconds() {
        return 120;
    }

    /**
     * Seconds without taking damage before regeneration starts.
     */
    default int regenDelaySeconds() {
        return 10;
    }

    /**
     * Part of the max health healed by eating one of its food items while injured.
     */
    default float foodHealFraction() {
        return 0.1F;
    }

    /**
     * Alive, injured, out of combat (no target) and not hurt for {@link #regenDelaySeconds()} seconds.
     */
    default boolean canRegenerate() {
        return this.mobmod$traits().canRegenerate();
    }

    /**
     * Pretends the last damage was taken {@code ticks} ago (used by tests, to skip the regeneration delay).
     */
    default void setTicksSinceDamage(int ticks) {
        this.mobmod$traits().setTicksSinceDamage(ticks);
    }

    /*===========*/
    /*   SLEEP   */
    /*===========*/

    /**
     * Somewhere and in a state it can sleep in (or keep sleeping in): alive, on the ground, out of water, with no
     * target, not leashed, in love or riding, and not hurt in the last few seconds. Asleep, losing any of this wakes it up.
     */
    default boolean canSleep() {
        return this.mobmod$traits().canSleep();
    }

    /**
     * Sleeping: lying down and quiet. Synced so clients can play the pose.
     */
    default boolean isAsleep() {
        return this.mobmod$traits().isAsleep();
    }

    /**
     * Lies down right where it stands. Returns false when it cannot.
     */
    default boolean fallAsleep() {
        return this.mobmod$traits().fallAsleep();
    }

    default void wakeUp() {
        this.mobmod$traits().wakeUp();
    }

    /**
     * From 0 (awake) to 1 (lying down asleep), smoothed between ticks.
     */
    default float getSleepAmount(float partialTick) {
        return this.mobmod$traits().getSleepAmount(partialTick);
    }

    /**
     * Played softly now and then while it sleeps, at the animal's voice pitch.
     */
    default SoundEvent getSleepSound() {
        return SoundEvents.FOX_SLEEP;
    }
}
