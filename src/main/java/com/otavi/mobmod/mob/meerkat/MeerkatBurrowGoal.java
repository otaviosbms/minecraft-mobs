package com.otavi.mobmod.mob.meerkat;

import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Sends the meerkat underground and keeps it still until it comes back up ({@link Meerkat} runs the digging itself):
 * <ul>
 *     <li>a monster in sight or a player sprinting close by: it dives with an alarm call and the whole group follows;</li>
 *     <li>at night: meerkats sleep in their burrows until morning;</li>
 *     <li>now and then, for a short rest.</li>
 * </ul>
 * It also takes over when another meerkat's alarm (or a reload) already put this one underground.
 */
public class MeerkatBurrowGoal extends Goal {
    /**
     * Ticks underground after a threat (before checking that it is gone): 15 to 30 seconds.
     */
    static final int THREAT_TICKS_MIN = 300;
    static final int THREAT_TICKS_MAX = 600;
    /**
     * At night, each meerkat goes down at a random moment, on average 5 seconds after dark.
     */
    static final int NIGHT_CHANCE = 100;
    /**
     * A rest underground on average every 3 minutes, for 10 to 20 seconds.
     */
    static final int REST_CHANCE = 3600;
    private static final int REST_TICKS_MIN = 200;
    private static final int REST_TICKS_MAX = 400;
    /**
     * At night it checks every 10 seconds whether the sun is up.
     */
    private static final int NIGHT_TICKS = 200;

    private final Meerkat meerkat;
    @Nullable
    private Meerkat.HideReason pending;

    public MeerkatBurrowGoal(Meerkat meerkat) {
        this.meerkat = meerkat;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        this.pending = null;
        if (this.meerkat.isBurrowed()) {
            return true;
        }
        if (this.meerkat.getTarget() != null || this.meerkat.isLeashed() || this.meerkat.isInLove() || !this.meerkat.canDigHere()) {
            return false;
        }
        // Looking for threats is the expensive check: twice a second is enough
        if (this.meerkat.tickCount % 10 == 0 && this.meerkat.findThreat(Meerkat.THREAT_RANGE) != null) {
            this.pending = Meerkat.HideReason.THREAT;
        } else if (this.meerkat.level().isDarkOutside() && this.meerkat.getRandom().nextInt(NIGHT_CHANCE) == 0) {
            this.pending = Meerkat.HideReason.NIGHT;
        } else if (this.meerkat.getRandom().nextInt(REST_CHANCE) == 0) {
            this.pending = Meerkat.HideReason.REST;
        }
        return this.pending != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.meerkat.isBurrowed();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        this.meerkat.getNavigation().stop();
        if (this.pending == null) {
            return;
        }
        var random = this.meerkat.getRandom();
        switch (this.pending) {
            case THREAT -> {
                int ticks = THREAT_TICKS_MIN + random.nextInt(THREAT_TICKS_MAX - THREAT_TICKS_MIN + 1);
                if (this.meerkat.burrow(Meerkat.HideReason.THREAT, ticks)) {
                    this.meerkat.raiseAlarm(ticks);
                }
            }
            case NIGHT -> this.meerkat.burrow(Meerkat.HideReason.NIGHT, NIGHT_TICKS);
            case REST -> this.meerkat.burrow(Meerkat.HideReason.REST, REST_TICKS_MIN + random.nextInt(REST_TICKS_MAX - REST_TICKS_MIN + 1));
        }
    }

    @Override
    public void tick() {
        this.meerkat.getNavigation().stop();
    }
}
