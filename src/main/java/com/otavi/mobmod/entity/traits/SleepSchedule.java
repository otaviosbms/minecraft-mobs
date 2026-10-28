package com.otavi.mobmod.entity.traits;

import net.minecraft.world.entity.Mob;

/**
 * When an animal goes to sleep and gets up, shared by every kind of AI: the {@link com.otavi.mobmod.entity.goal.SleepGoal}
 * of the animals made from scratch, and the brain-driven vanilla animals the mod adapts (the camel).
 * <p>
 * Each animal goes down at a random moment after dark and, asleep, checks now and then whether the sun is up, so a
 * herd does not go down or get up all at once.
 */
public final class SleepSchedule {
    /**
     * At night, each animal goes down at a random moment, on average 5 seconds after dark.
     */
    public static final int NIGHT_CHANCE = 100;
    /**
     * Asleep, it checks every 10 seconds whether the sun is up.
     */
    public static final int CHECK_TICKS = 200;

    private int checkTicks;

    /**
     * Rolled every tick while awake: true when it is night and this is the moment to lie down.
     */
    public static boolean isTimeToSleep(Mob mob) {
        return mob.level().isDarkOutside() && mob.getRandom().nextInt(NIGHT_CHANCE) == 0;
    }

    /**
     * Restarts the morning check, right after falling asleep.
     */
    public void start() {
        this.checkTicks = CHECK_TICKS;
    }

    /**
     * Called every tick while asleep: true when the check comes round and the sun is up.
     */
    public boolean isTimeToWakeUp(Mob mob) {
        if (--this.checkTicks > 0) {
            return false;
        }
        this.checkTicks = CHECK_TICKS;
        return !mob.level().isDarkOutside();
    }
}
