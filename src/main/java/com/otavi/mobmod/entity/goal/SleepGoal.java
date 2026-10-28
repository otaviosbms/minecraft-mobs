package com.otavi.mobmod.entity.goal;

import com.otavi.mobmod.entity.genetics.ModAnimal;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Every {@link ModAnimal} gets this goal on its own: at night the animal lies down where it stands and sleeps until
 * morning. Each one falls asleep at a random moment after dark and checks now and then whether the sun is up, so a
 * herd does not go down or get up all at once.
 * <p>
 * Being hurt, getting a target (a herd mate called for help), water, a leash or love mode wake it up right away; any
 * goal with a higher priority (fighting, panicking) also does.
 */
public class SleepGoal extends Goal {
    /**
     * At night, each animal goes down at a random moment, on average 5 seconds after dark.
     */
    public static final int NIGHT_CHANCE = 100;
    /**
     * Asleep, it checks every 10 seconds whether the sun is up.
     */
    public static final int CHECK_TICKS = 200;

    private final ModAnimal animal;
    private int checkTicks;

    public SleepGoal(ModAnimal animal) {
        this.animal = animal;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.animal.canSleep()) {
            return false;
        }
        // Already asleep: put to sleep by a test, or the world was saved while it slept
        return this.animal.isAsleep() || this.animal.level().isDarkOutside() && this.animal.getRandom().nextInt(NIGHT_CHANCE) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.animal.isAsleep() && this.animal.canSleep();
    }

    @Override
    public void start() {
        this.checkTicks = CHECK_TICKS;
        this.animal.fallAsleep();
    }

    @Override
    public void stop() {
        this.animal.wakeUp();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.animal.getNavigation().stop();
        if (--this.checkTicks <= 0) {
            this.checkTicks = CHECK_TICKS;
            if (!this.animal.level().isDarkOutside()) {
                this.animal.wakeUp();
            }
        }
    }
}
