package com.otavi.mobmod.client.mob.camel;

import com.otavi.mobmod.client.core.HasMobTraits;
import com.otavi.mobmod.client.core.MobTraitsRenderData;
import net.minecraft.client.renderer.entity.state.CamelRenderState;

/**
 * The vanilla camel render state (animations, saddle, riding) plus the data every animal of the mod carries.
 */
public class ModCamelRenderState extends CamelRenderState implements HasMobTraits {
    public final MobTraitsRenderData traits = new MobTraitsRenderData();

    @Override
    public MobTraitsRenderData traits() {
        return this.traits;
    }
}
