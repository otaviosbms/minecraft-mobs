package com.otavi.mobmod.entity.genetics;

import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.entity.goal.SleepGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Base class of every animal in the mod: adds a sex, a skin variant, health regeneration and sleep.
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
public abstract class ModAnimal extends Animal implements GeneticMob {
    private static final EntityDataAccessor<Byte> DATA_SEX = SynchedEntityData.defineId(ModAnimal.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_SKIN_VARIANT = SynchedEntityData.defineId(ModAnimal.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_ASLEEP = SynchedEntityData.defineId(ModAnimal.class, EntityDataSerializers.BOOLEAN);

    private static final Identifier MALE_SCALE_ID = MobMod.id("male_scale");

    /**
     * Health comes back in steps once per second, so the info bar visibly fills up.
     */
    private static final int REGEN_INTERVAL = 20;

    /**
     * Above the goals that feed, follow or wander, below fighting and panicking (priority 1), which wake it up.
     */
    private static final int SLEEP_GOAL_PRIORITY = 2;
    /**
     * Ticks to lie down, and to get back up.
     */
    public static final int SLEEP_TRANSITION_LENGTH = 20;
    /**
     * It does not fall asleep within 5 seconds of being hurt.
     */
    private static final int SLEEP_AFTER_DAMAGE_TICKS = 100;
    /**
     * A soft snore on average every 6 seconds.
     */
    private static final int SNORE_CHANCE = 120;

    private int ticksSinceDamage = Integer.MAX_VALUE / 2;
    private float sleepAmount;
    private float sleepAmountO;

    protected ModAnimal(EntityType<? extends ModAnimal> type, Level level) {
        super(type, level);
        // Added here rather than in registerGoals, so no animal can forget it
        if (!level.isClientSide()) {
            Goal sleep = this.createSleepGoal();
            if (sleep != null) {
                this.goalSelector.addGoal(SLEEP_GOAL_PRIORITY, sleep);
            }
        }
    }

    /**
     * How much bigger males are than females (0.15 = 15%). Applied through the scale attribute, so the hitbox grows too.
     */
    protected double maleScaleBonus() {
        return 0.15;
    }

    /**
     * Seconds to go from 0 to full health by regeneration alone: every mob heals at a pace proportional to its health.
     */
    protected int regenFullHealthSeconds() {
        return 120;
    }

    /**
     * Seconds without taking damage before regeneration starts.
     */
    protected int regenDelaySeconds() {
        return 10;
    }

    /**
     * Part of the max health healed by eating one of its food items while injured.
     */
    protected float foodHealFraction() {
        return 0.1F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SEX, (byte) Sex.FEMALE.ordinal());
        builder.define(DATA_SKIN_VARIANT, (byte) SkinVariant.NORMAL.ordinal());
        builder.define(DATA_ASLEEP, false);
    }

    @Override
    public Sex getSex() {
        return Sex.values()[this.entityData.get(DATA_SEX)];
    }

    public void setSex(Sex sex) {
        this.entityData.set(DATA_SEX, (byte) sex.ordinal());
        var scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) {
            if (sex == Sex.MALE) {
                scale.addOrReplacePermanentModifier(new AttributeModifier(MALE_SCALE_ID, this.maleScaleBonus(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else {
                scale.removeModifier(MALE_SCALE_ID);
            }
        }
    }

    @Override
    public SkinVariant getSkinVariant() {
        return SkinVariant.values()[this.entityData.get(DATA_SKIN_VARIANT)];
    }

    public void setSkinVariant(SkinVariant variant) {
        this.entityData.set(DATA_SKIN_VARIANT, (byte) variant.ordinal());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Sex", Sex.CODEC, this.getSex());
        output.store("SkinVariant", SkinVariant.CODEC, this.getSkinVariant());
        output.putInt("TicksSinceDamage", this.ticksSinceDamage);
        output.putBoolean("Asleep", this.entityData.get(DATA_ASLEEP));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // Animals saved before genetics existed get a random sex on first load
        this.setSex(input.read("Sex", Sex.CODEC).orElseGet(() -> Sex.random(this.random)));
        this.setSkinVariant(input.read("SkinVariant", SkinVariant.CODEC).orElse(SkinVariant.NORMAL));
        this.ticksSinceDamage = input.getIntOr("TicksSinceDamage", this.ticksSinceDamage);
        this.entityData.set(DATA_ASLEEP, input.getBooleanOr("Asleep", false));
    }

    /*==================*/
    /*   REGENERATION   */
    /*==================*/

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float damage) {
        super.actuallyHurt(level, source, damage);
        this.ticksSinceDamage = 0;
        this.wakeUp();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        if (this.ticksSinceDamage < Integer.MAX_VALUE / 2) {
            this.ticksSinceDamage++;
        }
        if (this.tickCount % REGEN_INTERVAL == 0 && this.canRegenerate()) {
            this.heal(this.getMaxHealth() / this.regenFullHealthSeconds() * (REGEN_INTERVAL / 20.0F));
        }
        if (this.entityData.get(DATA_ASLEEP)) {
            if (!this.canSleep()) {
                // Pushed into water, leashed... also when no goal is watching (right after a world reload)
                this.wakeUp();
            } else if (this.random.nextInt(SNORE_CHANCE) == 0) {
                this.playSound(this.getSleepSound(), 0.3F, this.getVoicePitch() * 0.9F);
            }
        }
    }

    /**
     * Alive, injured, out of combat (no target) and not hurt for {@link #regenDelaySeconds()} seconds.
     */
    public boolean canRegenerate() {
        return this.isAlive() && this.getHealth() < this.getMaxHealth() && this.getTarget() == null
                && this.ticksSinceDamage >= this.regenDelaySeconds() * 20;
    }

    /**
     * Pretends the last damage was taken {@code ticks} ago (used by tests, to skip the regeneration delay).
     */
    public void setTicksSinceDamage(int ticks) {
        this.ticksSinceDamage = ticks;
    }

    /*===========*/
    /*   SLEEP   */
    /*===========*/

    /**
     * The goal that puts the animal to sleep at night, or null for an animal that handles it on its own.
     */
    @Nullable
    protected Goal createSleepGoal() {
        return new SleepGoal(this);
    }

    /**
     * Somewhere and in a state it can sleep in (or keep sleeping in): alive, on the ground, out of water, with no
     * target, not leashed, in love or riding, and not hurt in the last few seconds. Asleep, losing any of this wakes it up.
     */
    public boolean canSleep() {
        return this.isAlive() && this.onGround() && !this.isInWater() && this.getTarget() == null && !this.isLeashed()
                && !this.isInLove() && !this.isPassenger() && !this.isVehicle() && this.ticksSinceDamage >= SLEEP_AFTER_DAMAGE_TICKS;
    }

    /**
     * Sleeping: lying down and quiet. Synced so clients can play the pose.
     */
    public boolean isAsleep() {
        return this.entityData.get(DATA_ASLEEP);
    }

    /**
     * Lies down right where it stands. Returns false when it cannot.
     */
    public boolean fallAsleep() {
        if (this.level().isClientSide() || !this.canSleep()) {
            return false;
        }
        this.getNavigation().stop();
        this.entityData.set(DATA_ASLEEP, true);
        return true;
    }

    public void wakeUp() {
        this.entityData.set(DATA_ASLEEP, false);
    }

    /**
     * Played softly now and then while it sleeps, at the animal's voice pitch.
     */
    protected SoundEvent getSleepSound() {
        return SoundEvents.FOX_SLEEP;
    }

    @Override
    public void baseTick() {
        // Asleep, the ambient sound timer never runs out: no calls in the middle of the night
        if (this.isAsleep()) {
            this.ambientSoundTime = -this.getAmbientSoundInterval();
        }
        super.baseTick();
    }

    @Override
    public void tick() {
        super.tick();
        this.sleepAmountO = this.sleepAmount;
        float target = this.entityData.get(DATA_ASLEEP) ? 1.0F : 0.0F;
        this.sleepAmount += Math.clamp(target - this.sleepAmount, -1.0F / SLEEP_TRANSITION_LENGTH, 1.0F / SLEEP_TRANSITION_LENGTH);
    }

    /**
     * From 0 (awake) to 1 (lying down asleep), smoothed between ticks.
     */
    public float getSleepAmount(float partialTick) {
        return this.sleepAmountO + (this.sleepAmount - this.sleepAmountO) * partialTick;
    }

    /**
     * Its food heals an injured animal (one item, {@link #foodHealFraction()} of its max health) instead of making it
     * fall in love; at full health the food works as in vanilla (breeding, growing up).
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isFood(stack) || this.getHealth() >= this.getMaxHealth()) {
            return super.mobInteract(player, hand);
        }
        if (this.level() instanceof ServerLevel level) {
            this.heal(this.getMaxHealth() * this.foodHealFraction());
            stack.consume(1, player);
            this.playEatingSound();
            level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        // Bred calves already got their genes in getBreedOffspring
        if (reason != EntitySpawnReason.BREEDING) {
            this.setSex(Sex.random(this.random));
            this.setSkinVariant(SkinVariant.rollNatural(this.random));
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean canMate(Animal other) {
        return super.canMate(other) && other instanceof GeneticMob mate && mate.getSex() != this.getSex();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        var calf = (ModAnimal) this.getType().create(level, EntitySpawnReason.BREEDING);
        if (calf != null) {
            calf.setSex(Sex.random(this.random));
            var partnerVariant = partner instanceof GeneticMob mate ? mate.getSkinVariant() : this.getSkinVariant();
            calf.setSkinVariant(SkinVariant.inherit(this.getSkinVariant(), partnerVariant, this.random));
        }
        return calf;
    }
}
