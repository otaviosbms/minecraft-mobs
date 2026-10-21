package com.otavi.mobmod.entity.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;

/**
 * Panic that only babies feel: adults of neutral mobs fight back instead.
 */
public class BabyPanicGoal extends PanicGoal {
    public BabyPanicGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier);
    }

    @Override
    protected boolean shouldPanic() {
        return this.mob.isBaby() && super.shouldPanic();
    }
}
