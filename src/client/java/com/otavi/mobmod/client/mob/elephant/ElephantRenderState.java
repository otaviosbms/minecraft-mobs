package com.otavi.mobmod.client.mob.elephant;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class ElephantRenderState extends LivingEntityRenderState {
    /**
     * Goes from 1 (attack just started) to 0 (attack animation finished).
     */
    public float attackProgress;
}
