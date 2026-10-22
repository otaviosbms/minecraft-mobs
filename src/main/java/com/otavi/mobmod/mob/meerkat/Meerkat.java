package com.otavi.mobmod.mob.meerkat;

import com.otavi.mobmod.entity.genetics.ModAnimal;
import com.otavi.mobmod.entity.goal.AdultMeleeAttackGoal;
import com.otavi.mobmod.entity.goal.BabyPanicGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * A neutral desert animal: it lives in groups (mobs), walks on all fours and now and then stands up on its hind legs
 * to keep watch ({@link MeerkatSentinelGoal}), more often with a player around. Adults hunt the bugs of the game
 * (silverfish and endermites), and the whole group mobs whoever hurts one of them.
 * <p>
 * It also digs into soft ground to hide ({@link MeerkatBurrowGoal}): from monsters and sprinting players (with an
 * alarm call that sends the whole group underground), for the night, and now and then for a short rest. Underground
 * it cannot be seen, hit or hurt, and it comes back up once it is safe.
 */
public class Meerkat extends ModAnimal {
    private static final EntityDataAccessor<Boolean> DATA_STANDING = SynchedEntityData.defineId(Meerkat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_BURROW = SynchedEntityData.defineId(Meerkat.class, EntityDataSerializers.BYTE);

    private static final byte ATTACK_EVENT = 4;
    public static final int ATTACK_ANIMATION_LENGTH = 8;

    /**
     * How fast the model goes between walking on all fours (0) and standing up (1), per tick.
     */
    private static final float STAND_SPEED = 0.2F;

    /**
     * Ticks to dig down into the ground, and to come back out.
     */
    public static final int DIG_LENGTH = 20;
    /**
     * Range in which a monster, or a player sprinting within {@link #SPRINTER_RANGE}, makes a meerkat dive underground.
     */
    static final double THREAT_RANGE = 8.0;
    static final double SPRINTER_RANGE = 5.0;
    /**
     * The alarm call sends every meerkat within this range underground.
     */
    private static final double ALARM_RANGE = 16.0;
    /**
     * Once underground, the meerkat checks again for the threat this far away before coming back up.
     */
    private static final double SAFE_RANGE = 10.0;

    private int attackAnimationTick;
    private float standAmount;
    private float standAmountO;
    private boolean forceWatch;
    /**
     * Ticks left digging down or coming back out.
     */
    private int burrowTicks;
    /**
     * Ticks left underground before checking whether it is safe to come back up.
     */
    private int hideTicks;
    private HideReason hideReason = HideReason.REST;
    private float burrowDepth;
    private float burrowDepthO;

    public Meerkat(EntityType<? extends Meerkat> type, Level level) {
        super(type, level);
    }

    /*========*/
    /*   AI   */
    /*========*/

    public static AttributeSupplier.Builder createMeerkatAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeerkatBurrowGoal(this));
        this.goalSelector.addGoal(1, new AdultMeleeAttackGoal(this, 1.4, true));
        this.goalSelector.addGoal(1, new BabyPanicGoal(this, 1.8));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.2, stack -> stack.is(MeerkatMob.FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.2));
        this.goalSelector.addGoal(5, new MeerkatSentinelGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Calves don't fight, but they still call the adults around them
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        // Meerkats eat insects: adults hunt the bugs of the game
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Silverfish.class, true, (target, level) -> !this.isBaby() && !this.isBurrowed()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Endermite.class, true, (target, level) -> !this.isBaby() && !this.isBurrowed()));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(MeerkatMob.FOOD);
    }

    /**
     * Like other animals, but on sand as well as grass: meerkats live in deserts.
     */
    public static boolean checkMeerkatSpawnRules(EntityType<Meerkat> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        BlockState ground = level.getBlockState(pos.below());
        return (ground.is(BlockTags.SAND) || ground.is(BlockTags.ANIMALS_SPAWNABLE_ON)) && isBrightEnoughToSpawn(level, pos);
    }

    /*==============*/
    /*   STANDING   */
    /*==============*/

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STANDING, false);
        builder.define(DATA_BURROW, (byte) BurrowState.SURFACE.ordinal());
    }

    /**
     * Standing up on its hind legs, keeping watch. Synced so clients can animate it.
     */
    public boolean isStanding() {
        return this.entityData.get(DATA_STANDING);
    }

    public void setStanding(boolean standing) {
        this.entityData.set(DATA_STANDING, standing);
    }

    /**
     * Makes the next {@link MeerkatSentinelGoal} check start a watch, skipping the chance roll (used by tests).
     */
    public void requestWatch() {
        this.forceWatch = true;
    }

    boolean consumeWatchRequest() {
        boolean requested = this.forceWatch;
        this.forceWatch = false;
        return requested;
    }

    @Override
    public void tick() {
        super.tick();
        this.standAmountO = this.standAmount;
        float target = this.isStanding() ? 1.0F : 0.0F;
        this.standAmount += Math.clamp(target - this.standAmount, -STAND_SPEED, STAND_SPEED);

        this.burrowDepthO = this.burrowDepth;
        BurrowState state = this.getBurrowState();
        if (state == BurrowState.HIDDEN) {
            // Already underground when it comes into view (or the world loads): no need to watch it dig
            this.burrowDepth = 1.0F;
        } else {
            float depth = state == BurrowState.DIGGING ? 1.0F : 0.0F;
            this.burrowDepth += Math.clamp(depth - this.burrowDepth, -1.0F / DIG_LENGTH, 1.0F / DIG_LENGTH);
        }
    }

    /**
     * From 0 (on all fours) to 1 (standing up), smoothed between ticks.
     */
    public float getStandAmount(float partialTick) {
        return this.standAmountO + (this.standAmount - this.standAmountO) * partialTick;
    }

    /*============*/
    /*   BURROW   */
    /*============*/

    public enum BurrowState {
        SURFACE, DIGGING, HIDDEN, EMERGING
    }

    /**
     * Why the meerkat went underground: it decides when to come back up from it.
     */
    public enum HideReason {
        /**
         * A monster or a sprinting player: comes back up once nothing is around.
         */
        THREAT,
        /**
         * Meerkats sleep in their burrows: comes back up in the morning.
         */
        NIGHT,
        /**
         * A short rest in the shade of the burrow, now and then.
         */
        REST
    }

    public BurrowState getBurrowState() {
        return BurrowState.values()[this.entityData.get(DATA_BURROW)];
    }

    private void setBurrowState(BurrowState state) {
        this.entityData.set(DATA_BURROW, (byte) state.ordinal());
    }

    /**
     * Digging down, underground or coming back out: anything but walking around on the surface.
     */
    public boolean isBurrowed() {
        return this.getBurrowState() != BurrowState.SURFACE;
    }

    /**
     * Fully underground: it cannot be seen, hit, pushed or hurt.
     */
    public boolean isHidden() {
        return this.getBurrowState() == BurrowState.HIDDEN;
    }

    public HideReason getHideReason() {
        return this.hideReason;
    }

    /**
     * From 0 (on the surface) to 1 (fully underground), smoothed between ticks.
     */
    public float getBurrowDepth(float partialTick) {
        return this.burrowDepthO + (this.burrowDepth - this.burrowDepthO) * partialTick;
    }

    /**
     * Soft ground it can dig into: sand, dirt, grass, gravel... (anything dug with a shovel).
     */
    public boolean canDigHere() {
        return this.onGround() && !this.isInWater() && this.level().getBlockState(this.getOnPos()).is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    /**
     * Starts digging down; it stays underground at least {@code ticks} ticks. Fails on ground it cannot dig.
     */
    public boolean burrow(HideReason reason, int ticks) {
        if (this.isBurrowed() || !this.canDigHere()) {
            return false;
        }
        this.hideReason = reason;
        this.hideTicks = ticks;
        this.burrowTicks = DIG_LENGTH;
        this.setStanding(false);
        this.setTarget(null);
        this.getNavigation().stop();
        this.setBurrowState(BurrowState.DIGGING);
        return true;
    }

    /**
     * Comes back up right away (used by tests and when the ground under it is gone).
     */
    public void emerge() {
        if (this.getBurrowState() == BurrowState.DIGGING || this.isHidden()) {
            this.burrowTicks = DIG_LENGTH;
            this.setBurrowState(BurrowState.EMERGING);
        }
    }

    /**
     * The nearest monster in sight (silverfish and endermites are prey, not threats) or a player sprinting close by.
     */
    @Nullable
    public LivingEntity findThreat(double range) {
        Player sprinter = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), Math.min(range, SPRINTER_RANGE),
                entity -> entity instanceof Player player && player.isSprinting() && !player.isSpectator());
        if (sprinter != null) {
            return sprinter;
        }
        List<Monster> monsters = this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(range),
                monster -> monster.isAlive() && !(monster instanceof Silverfish) && !(monster instanceof Endermite) && this.hasLineOfSight(monster));
        return monsters.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    /**
     * A sharp alarm call: every meerkat around dives underground too.
     */
    public void raiseAlarm(int hideTicks) {
        this.playSound(SoundEvents.FOX_SCREECH, 0.7F, this.getVoicePitch());
        for (Meerkat other : this.level().getEntitiesOfClass(Meerkat.class, this.getBoundingBox().inflate(ALARM_RANGE), other -> other != this)) {
            // Each one reacts a little later than the last, like a group scattering into its burrows
            other.burrow(HideReason.THREAT, hideTicks + this.random.nextInt(40));
        }
    }

    private void tickBurrow(ServerLevel level) {
        switch (this.getBurrowState()) {
            case DIGGING -> {
                this.digEffects(level, 3);
                if (--this.burrowTicks <= 0) {
                    this.setBurrowState(BurrowState.HIDDEN);
                    this.playSound(this.groundState().getSoundType().getBreakSound(), 0.6F, 1.2F);
                }
            }
            case HIDDEN -> {
                if (!this.canDigHere()) {
                    // The ground was dug away or flooded: it pops back out
                    this.emerge();
                } else if (--this.hideTicks <= 0) {
                    if (this.shouldStayHidden()) {
                        this.hideTicks = 100 + this.random.nextInt(100);
                    } else {
                        this.emerge();
                    }
                } else if (this.tickCount % 40 == 0) {
                    // A few grains now and then show where the burrow is
                    this.digEffects(level, 1);
                }
            }
            case EMERGING -> {
                this.digEffects(level, 3);
                if (--this.burrowTicks <= 0) {
                    this.setBurrowState(BurrowState.SURFACE);
                }
            }
            case SURFACE -> {
            }
        }
    }

    private boolean shouldStayHidden() {
        if (this.findThreat(SAFE_RANGE) != null) {
            return true;
        }
        return this.hideReason == HideReason.NIGHT && this.level().isDarkOutside();
    }

    private BlockState groundState() {
        return this.level().getBlockState(this.getOnPos());
    }

    /**
     * Bits of the ground thrown up around the meerkat, with the sound of the block being dug.
     */
    private void digEffects(ServerLevel level, int count) {
        BlockState ground = this.groundState();
        if (ground.isAir()) {
            return;
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.getX(), this.getY() + 0.1, this.getZ(),
                count, this.getBbWidth() * 0.4, 0.05, this.getBbWidth() * 0.4, 0.1);
        if (count > 1 && this.tickCount % 4 == 0) {
            this.playSound(ground.getSoundType().getHitSound(), 0.4F, 1.1F + this.random.nextFloat() * 0.3F);
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        // Out of reach underground, except for /kill and the void
        return super.isInvulnerableTo(level, source) || (this.isHidden() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY));
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !this.isHidden();
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !this.isBurrowed();
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return super.canBeSeenAsEnemy() && !this.isHidden();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Burrow", this.getBurrowState().name().toLowerCase(Locale.ROOT));
        output.putString("HideReason", this.hideReason.name().toLowerCase(Locale.ROOT));
        output.putInt("BurrowTicks", this.burrowTicks);
        output.putInt("HideTicks", this.hideTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setBurrowState(parse(BurrowState.class, input.getStringOr("Burrow", ""), BurrowState.SURFACE));
        this.hideReason = parse(HideReason.class, input.getStringOr("HideReason", ""), HideReason.REST);
        this.burrowTicks = input.getIntOr("BurrowTicks", 0);
        this.hideTicks = input.getIntOr("HideTicks", 0);
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String name, E fallback) {
        for (E value : type.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    /*============*/
    /*   ATTACK   */
    /*============*/

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.attackAnimationTick = ATTACK_ANIMATION_LENGTH;
        level.broadcastEntityEvent(this, ATTACK_EVENT);
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt) {
            this.playSound(SoundEvents.FOX_BITE, 0.8F, this.getVoicePitch());
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == ATTACK_EVENT) {
            this.attackAnimationTick = ATTACK_ANIMATION_LENGTH;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;
        }
        if (this.level() instanceof ServerLevel level) {
            this.tickBurrow(level);
        }
    }

    public int getAttackAnimationTick() {
        return this.attackAnimationTick;
    }

    /*============*/
    /*   SOUNDS   */
    /*============*/

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        // Quiet underground
        return this.isHidden() ? null : SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ARMADILLO_STEP, 0.15F, 1.4F);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public float getVoicePitch() {
        // Higher than the fox it borrows its voice from: short chirps
        return super.getVoicePitch() * 1.5F;
    }
}
