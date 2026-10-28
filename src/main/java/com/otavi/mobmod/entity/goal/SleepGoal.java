package com.otavi.mobmod.entity.goal;

import com.otavi.mobmod.entity.genetics.ModAnimal;
import com.otavi.mobmod.entity.traits.SleepSchedule;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Every {@link ModAnimal} gets this goal on its own: at night the animal lies down where it stands and sleeps until
 * morning, on the {@link SleepSchedule} shared with the adapted vanilla animals.
 * <p>
 * Being hurt, getting a target (a herd mate called for help), water, a leash or love mode wake it up right away; any
 * goal with a higher priority (fighting, panicking) also does.
 */
public class SleepGoal extends Goal {
    private final ModAnimal animal;
    private final SleepSchedule schedule = new SleepSchedule();

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
        return this.animal.isAsleep() || SleepSchedule.isTimeToSleep(this.animal);
    }

    @Override
    public boolean canContinueToUse() {
        return this.animal.isAsleep() && this.animal.canSleep();
    }

    @Override
    public void start() {
        this.schedule.start();
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
        if (this.schedule.isTimeToWakeUp(this.animal)) {
            this.animal.wakeUp();
        }
    }
}
