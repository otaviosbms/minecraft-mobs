package com.otavi.mobmod.client.core;

import com.otavi.mobmod.entity.genetics.Sex;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import net.minecraft.network.chat.Component;

/**
 * What every animal of the mod hands to its renderer, on top of the vanilla render state: genetics, what the info bar
 * shows and the sleep pose. Kept in its own object so a render state of the mod ({@link ModMobRenderState}) and a
 * vanilla one extended for an adapted animal (the camel's) carry the same data ({@link HasMobTraits}).
 */
public class MobTraitsRenderData {
    public Sex sex = Sex.FEMALE;
    public SkinVariant skinVariant = SkinVariant.NORMAL;
    public float health;
    public float maxHealth;
    public Component displayName = Component.empty();
    public boolean showInfoBar;
    /**
     * From 0 (awake) to 1 (lying down asleep).
     */
    public float sleepAmount;

    /**
     * Eyes shut halfway through lying down: models show their eyelids over the painted eyes.
     */
    public boolean eyesClosed() {
        return this.sleepAmount > 0.5F;
    }
}
