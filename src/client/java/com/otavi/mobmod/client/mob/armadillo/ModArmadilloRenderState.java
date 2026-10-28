package com.otavi.mobmod.client.mob.armadillo;

import com.otavi.mobmod.client.core.HasMobTraits;
import com.otavi.mobmod.client.core.MobTraitsRenderData;
import net.minecraft.client.renderer.entity.state.ArmadilloRenderState;

/**
 * The vanilla armadillo render state (shell, roll and peek animations) plus the data every animal of the mod carries.
 */
public class ModArmadilloRenderState extends ArmadilloRenderState implements HasMobTraits {
    public final MobTraitsRenderData traits = new MobTraitsRenderData();

    @Override
    public MobTraitsRenderData traits() {
        return this.traits;
    }
}
