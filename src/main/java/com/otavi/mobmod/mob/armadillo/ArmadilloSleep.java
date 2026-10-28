package com.otavi.mobmod.mob.armadillo;

import com.otavi.mobmod.entity.traits.ModCreature;
import com.otavi.mobmod.entity.traits.SleepSchedule;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

/**
 * Sleep for the armadillo, whose AI is a vanilla brain. Same {@link SleepSchedule} as every other animal; the pose is
 * the one the game already has: it rolls up into a ball inside its shell ({@link Armadillo#rollUp()}), like the meerkat
 * goes down its burrow, and rolls back out in the morning ({@link Armadillo#rollOut()}).
 * <p>
 * Vanilla only finishes rolling up (rolling to scared) while it is hiding from a danger, so this does it for a sleeper;
 * {@link com.otavi.mobmod.mixin.ArmadilloBallUpMixin} stops the vanilla AI from peeking and unrolling it in its sleep.
 */
public final class ArmadilloSleep {
    private final SleepSchedule schedule = new SleepSchedule();
    private boolean wasAsleep;

    /**
     * Every server tick, after the vanilla AI.
     */
    public void tick(Armadillo armadillo) {
        ModCreature creature = ArmadilloAdaptation.creature(armadillo);
        if (creature.isAsleep()) {
            armadillo.getNavigation().stop();
            if (armadillo.shouldSwitchToScaredState()) {
                armadillo.switchToState(Armadillo.ArmadilloState.SCARED);
            }
            if (this.schedule.isTimeToWakeUp(armadillo)) {
                creature.wakeUp();
            }
        } else if (armadillo.getState() == Armadillo.ArmadilloState.IDLE && creature.canSleep()
                && SleepSchedule.isTimeToSleep(armadillo)) {
            this.fallAsleepNow(armadillo);
        }

        // Woken up for any reason: it rolls back out, unless a danger keeps it in the shell (then vanilla takes over)
        boolean asleep = creature.isAsleep();
        if (this.wasAsleep && !asleep && armadillo.isScared()
                && !armadillo.getBrain().hasMemoryValue(MemoryModuleType.DANGER_DETECTED_RECENTLY)) {
            armadillo.rollOut();
        }
        this.wasAsleep = asleep;
    }

    /**
     * Falls asleep right now (also used by tests): rolls up if it is not in its shell yet.
     */
    public boolean fallAsleepNow(Armadillo armadillo) {
        if (!ArmadilloAdaptation.creature(armadillo).mobmod$traits().fallAsleep()) {
            return false;
        }
        this.schedule.start();
        if (!armadillo.isScared()) {
            armadillo.rollUp();
        }
        return true;
    }
}
