package com.otavi.mobmod.client.core;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Render state shared by every mob the mod makes from scratch: the vanilla data plus the {@link MobTraitsRenderData}.
 */
public class ModMobRenderState extends LivingEntityRenderState implements HasMobTraits {
    public final MobTraitsRenderData traits = new MobTraitsRenderData();

    @Override
    public MobTraitsRenderData traits() {
        return this.traits;
    }
}
