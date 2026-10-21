package com.otavi.mobmod.client.mob.elephant;

import com.otavi.mobmod.client.core.ModMobRenderState;

public class ElephantRenderState extends ModMobRenderState {
    /**
     * Goes from 1 (attack just started) to 0 (attack animation finished).
     */
    public float attackProgress;
}
