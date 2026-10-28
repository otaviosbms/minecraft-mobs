package com.otavi.mobmod.client.core;

import com.otavi.mobmod.entity.genetics.Sex;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;

/**
 * Render state shared by every mob of the mod: genetics plus what the info bar shows.
 */
public class ModMobRenderState extends LivingEntityRenderState {
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
