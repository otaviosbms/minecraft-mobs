package com.otavi.mobmod.entity.genetics;

import com.otavi.mobmod.entity.goal.SleepGoal;
import com.otavi.mobmod.entity.traits.AnimalTraits;
import com.otavi.mobmod.entity.traits.ModCreature;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Base class of every animal the mod makes from scratch: adds a sex, a skin variant, health regeneration and sleep.
 * The rules themselves live in {@link AnimalTraits}, shared with the vanilla animals the mod adapts (see
 * {@link ModCreature}); this class only wires them into the vanilla methods.
 * <p>
 * Breeding works exactly like vanilla (food, love mode, cooldown, XP, growing calves) except that
 * only a male and a female can mate. Calves get a random sex and inherit the skin variant of a parent.
 * <p>
 * Out of combat, health comes back over time: full health in {@link #regenFullHealthSeconds()} seconds, starting
 * {@link #regenDelaySeconds()} seconds after the last damage. Feeding an injured animal its food also heals it.
 * <p>
 * At night every animal sleeps ({@link SleepGoal}, added on its own): it lies down, goes quiet and wakes up in the
 * morning, or right away when something disturbs it. An animal with its own way of sleeping (the meerkat in its
 * burrow) overrides {@link #createSleepGoal()}, {@link #canSleep()} and {@link #fallAsleep()}.
 */
public abstract class ModAnimal extends Animal implements ModCreature {
    private static final EntityDataAccessor<Byte> DATA_SEX = SynchedEntityData.defineId(ModAnimal.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_SKIN_VARIANT = SynchedEntityData.defineId(ModAnimal.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_ASLEEP = SynchedEntityData.defineId(ModAnimal.class, EntityDataSerializers.BOOLEAN);

    /**
     * Above the goals that feed, follow or wander, below fighting and panicking (priority 1), which wake it up.
     */
    private static final int SLEEP_GOAL_PRIORITY = 2;

    private final AnimalTraits traits;

    protected ModAnimal(EntityType<? extends ModAnimal> type, Level level) {
        super(type, level);
        this.traits = new AnimalTraits(this, DATA_SEX, DATA_SKIN_VARIANT, DATA_ASLEEP);
        // Added here rather than in registerGoals, so no animal can forget it
        if (!level.isClientSide()) {
            Goal sleep = this.createSleepGoal();
            if (sleep != null) {
                this.goalSelector.addGoal(SLEEP_GOAL_PRIORITY, sleep);
            }
        }
    }

    @Override
    public AnimalTraits mobmod$traits() {
        return this.traits;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        AnimalTraits.defineData(builder, DATA_SEX, DATA_SKIN_VARIANT, DATA_ASLEEP);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.traits.save(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.traits.load(input);
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float damage) {
        super.actuallyHurt(level, source, damage);
        this.traits.onHurt();
    }

    @Override
    public void baseTick() {
        this.traits.beforeBaseTick();
        super.baseTick();
    }

    @Override
    public void tick() {
        super.tick();
        this.traits.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel) {
            this.traits.serverAiStep();
        }
    }

    /**
     * The goal that puts the animal to sleep at night, or null for an animal that handles it on its own.
     */
    @Nullable
    protected Goal createSleepGoal() {
        return new SleepGoal(this);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.traits.feedWhileInjured(player, hand)) {
            if (!this.level().isClientSide()) {
                this.playEatingSound();
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        this.traits.onFinalizeSpawn(reason);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean canMate(Animal other) {
        return super.canMate(other) && this.traits.isOppositeSex(other);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        var calf = (ModAnimal) this.getType().create(level, EntitySpawnReason.BREEDING);
        if (calf != null) {
            this.traits.giveGenesTo(calf, partner);
        }
        return calf;
    }
}
