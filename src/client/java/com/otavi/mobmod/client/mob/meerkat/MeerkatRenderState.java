package com.otavi.mobmod.client.mob.meerkat;

import com.otavi.mobmod.client.core.ModMobRenderState;

public class MeerkatRenderState extends ModMobRenderState {
    /**
     * From 0 (on all fours) to 1 (standing up on the hind legs).
     */
    public float standAmount;
    /**
     * Goes from 1 (attack just started) to 0 (attack animation finished).
     */
    public float attackProgress;
    /**
     * From 0 (on the surface) to 1 (fully underground).
     */
    public float burrowDepth;
    /**
     * Digging down or climbing back out: the front paws scratch at the ground.
     */
    public boolean digging;
}
