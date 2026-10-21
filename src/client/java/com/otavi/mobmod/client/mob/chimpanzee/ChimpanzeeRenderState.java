package com.otavi.mobmod.client.mob.chimpanzee;

import com.otavi.mobmod.client.core.ModMobRenderState;

public class ChimpanzeeRenderState extends ModMobRenderState {
    /**
     * Goes from 1 (attack just started) to 0 (attack animation finished).
     */
    public float attackProgress;
    /**
     * Goes from 1 (call just started) to 0 (call finished).
     */
    public float hootProgress;
    public boolean climbing;
}
