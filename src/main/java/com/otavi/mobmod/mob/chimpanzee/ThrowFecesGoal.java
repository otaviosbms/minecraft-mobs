package com.otavi.mobmod.mob.chimpanzee;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Now and then, a chimpanzee near a player stops, stares at them for a moment and throws feces.
 * Low chance on purpose: about once every {@link #CHANCE} ticks while a player is in range, then a long cooldown.
 */
class ThrowFecesGoal extends Goal {
    /**
     * Average ticks between throws while a player is in range (60 s).
     */
    static final int CHANCE = 1200;
    static final double RANGE = 12.0;
    private static final int WIND_UP = 15;

    private final Chimpanzee chimpanzee;
    @Nullable
    private Player player;
    private int windUp;

    ThrowFecesGoal(Chimpanzee chimpanzee) {
        this.chimpanzee = chimpanzee;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.chimpanzee.isBaby() || !this.chimpanzee.canThrowFeces() || this.chimpanzee.getTarget() != null
                || this.chimpanzee.isInLove() || this.chimpanzee.isSlidingOnTrunk()) {
            return false;
        }
        if (!this.chimpanzee.isForcedToThrow() && this.chimpanzee.getRandom().nextInt(reducedTickDelay(CHANCE)) != 0) {
            return false;
        }
        Player nearest = this.chimpanzee.level().getNearestPlayer(this.chimpanzee, RANGE);
        if (nearest == null || nearest.isSpectator() || !this.chimpanzee.hasLineOfSight(nearest)) {
            return false;
        }
        this.player = nearest;
        return true;
    }

    @Override
    public void start() {
        this.windUp = WIND_UP;
        this.chimpanzee.getNavigation().stop();
    }

    @Override
    public boolean canContinueToUse() {
        return this.player != null && this.windUp > 0 && this.player.isAlive() && this.chimpanzee.distanceTo(this.player) < RANGE + 4.0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.player == null) {
            return;
        }
        // Stare at the target while winding up, then throw
        this.chimpanzee.getLookControl().setLookAt(this.player, 30.0F, 30.0F);
        if (--this.windUp <= 0) {
            this.chimpanzee.throwFecesAt(this.player);
        }
    }

    @Override
    public void stop() {
        this.player = null;
    }
}
