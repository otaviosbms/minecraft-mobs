package com.otavi.mobmod.entity.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * Melee attack that babies never use: they rely on {@link BabyPanicGoal} instead.
 */
public class AdultMeleeAttackGoal extends MeleeAttackGoal {
    public AdultMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    public boolean canUse() {
        return !this.mob.isBaby() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.isBaby() && super.canContinueToUse();
    }
}
