package com.otavi.mobmod.mob.camel;

import com.otavi.mobmod.entity.traits.ModCreature;
import com.otavi.mobmod.entity.traits.SleepSchedule;
import net.minecraft.world.entity.animal.camel.Camel;

/**
 * Sleep for the camel, whose AI is a vanilla brain instead of goals, so the {@link com.otavi.mobmod.entity.goal.SleepGoal}
 * cannot run. Same {@link SleepSchedule} as every other animal; the pose is the one the game already has for camels
 * resting: it kneels down ({@link Camel#sitDown()}) and gets back up ({@link Camel#standUp()}).
 * <p>
 * While it sleeps, {@link com.otavi.mobmod.mixin.CamelRandomSittingMixin} stops the vanilla AI from standing it up.
 */
public final class CamelSleep {
    private final SleepSchedule schedule = new SleepSchedule();
    private boolean wasAsleep;

    /**
     * Every server tick, after the vanilla AI.
     */
    public void tick(Camel camel) {
        ModCreature creature = CamelAdaptation.creature(camel);
        if (creature.isAsleep()) {
            camel.getNavigation().stop();
            if (this.schedule.isTimeToWakeUp(camel)) {
                creature.wakeUp();
            }
        } else if (creature.canSleep() && !camel.isInPoseTransition() && SleepSchedule.isTimeToSleep(camel)
                && (camel.isCamelSitting() || camel.canCamelChangePose()) && creature.fallAsleep()) {
            this.schedule.start();
            if (!camel.isCamelSitting()) {
                camel.sitDown();
            }
        }

        // Woken up for any reason (morning, hurt, leashed, ridden...): it gets back on its feet
        boolean asleep = creature.isAsleep();
        if (this.wasAsleep && !asleep && camel.isCamelSitting() && camel.canCamelChangePose()) {
            camel.standUp();
        }
        this.wasAsleep = asleep;
    }

    /**
     * Falls asleep right now (tests): kneels down if it is standing.
     */
    public boolean fallAsleepNow(Camel camel) {
        if (!CamelAdaptation.creature(camel).mobmod$traits().fallAsleep()) {
            return false;
        }
        this.schedule.start();
        if (!camel.isCamelSitting()) {
            camel.sitDown();
        }
        return true;
    }
}
