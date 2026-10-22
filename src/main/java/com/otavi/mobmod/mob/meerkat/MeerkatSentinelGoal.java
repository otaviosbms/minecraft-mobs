package com.otavi.mobmod.mob.meerkat;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * The meerkat stops, stands up on its hind legs and keeps watch: it stares at a nearby player or scans around.
 * Any goal with a higher priority (fleeing, fighting, food, breeding) makes it drop back on all fours.
 */
public class MeerkatSentinelGoal extends Goal {
    /**
     * Average ticks between two watches: every 15 s alone, every 3 s with a player within {@link #PLAYER_RANGE}.
     */
    static final int CHANCE = 300;
    static final int CHANCE_WITH_PLAYER = 60;
    static final double PLAYER_RANGE = 10.0;
    /**
     * A watch lasts 5 to 12 seconds.
     */
    private static final int DURATION_MIN = 100;
    private static final int DURATION_MAX = 240;

    private final Meerkat meerkat;
    private int ticksLeft;
    private int nextScan;
    @Nullable
    private Player watched;

    public MeerkatSentinelGoal(Meerkat meerkat) {
        this.meerkat = meerkat;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.canWatch()) {
            return false;
        }
        if (this.meerkat.consumeWatchRequest()) {
            return true;
        }
        Player player = this.nearestPlayer();
        return this.meerkat.getRandom().nextInt(player != null ? CHANCE_WITH_PLAYER : CHANCE) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticksLeft > 0 && this.canWatch();
    }

    private boolean canWatch() {
        return !this.meerkat.isBurrowed() && this.meerkat.onGround() && !this.meerkat.isInWater() && !this.meerkat.isInLove()
                && this.meerkat.getTarget() == null && !this.meerkat.isLeashed();
    }

    @Override
    public void start() {
        this.ticksLeft = DURATION_MIN + this.meerkat.getRandom().nextInt(DURATION_MAX - DURATION_MIN + 1);
        this.nextScan = 0;
        this.meerkat.getNavigation().stop();
        this.meerkat.setStanding(true);
    }

    @Override
    public void stop() {
        this.meerkat.setStanding(false);
        this.watched = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticksLeft--;
        if (this.meerkat.tickCount % 10 == 0) {
            this.watched = this.nearestPlayer();
        }
        if (this.watched != null) {
            this.meerkat.getLookControl().setLookAt(this.watched, 30.0F, 30.0F);
            return;
        }
        // Nobody around: turn the head to a new direction every 1 to 2.5 seconds
        if (--this.nextScan <= 0) {
            this.nextScan = 20 + this.meerkat.getRandom().nextInt(30);
            double angle = this.meerkat.getRandom().nextDouble() * Math.PI * 2.0;
            this.meerkat.getLookControl().setLookAt(this.meerkat.getX() + Math.cos(angle) * 6.0, this.meerkat.getEyeY() + 0.5,
                    this.meerkat.getZ() + Math.sin(angle) * 6.0);
        }
    }

    @Nullable
    private Player nearestPlayer() {
        Player player = this.meerkat.level().getNearestPlayer(this.meerkat, PLAYER_RANGE);
        return player != null && !player.isSpectator() ? player : null;
    }
}
