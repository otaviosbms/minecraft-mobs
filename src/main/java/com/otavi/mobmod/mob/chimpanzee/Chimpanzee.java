package com.otavi.mobmod.mob.chimpanzee;

import com.otavi.mobmod.entity.genetics.ModAnimal;
import com.otavi.mobmod.entity.goal.AdultMeleeAttackGoal;
import com.otavi.mobmod.entity.goal.BabyPanicGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A neutral jungle ape: it lives in troops, climbs walls and trees like a spider, and the whole troop
 * fights back when one of them gets hurt.
 */
public class Chimpanzee extends ModAnimal {
    private static final EntityDataAccessor<Boolean> DATA_CLIMBING = SynchedEntityData.defineId(Chimpanzee.class, EntityDataSerializers.BOOLEAN);

    private static final byte ATTACK_EVENT = 4;
    public static final int ATTACK_ANIMATION_LENGTH = 10;

    private int attackAnimationTick;

    public Chimpanzee(EntityType<? extends Chimpanzee> type, Level level) {
        super(type, level);
    }

    /*========*/
    /*   AI   */
    /*========*/

    public static AttributeSupplier.Builder createChimpanzeeAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AdultMeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(1, new BabyPanicGoal(this, 1.8));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.2, stack -> stack.is(ChimpanzeeMob.FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.2));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Babies don't fight, but they still call the troop
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ChimpanzeeMob.FOOD);
    }

    /*==============*/
    /*   CLIMBING   */
    /*==============*/

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLIMBING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        // Paths may go up walls and tree trunks, like a spider
        return new WallClimberNavigation(this, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_CLIMBING, this.horizontalCollision);
        }
    }

    @Override
    public boolean onClimbable() {
        return this.entityData.get(DATA_CLIMBING);
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
            this.playSound(SoundEvents.PANDA_BITE, 1.0F, this.getVoicePitch());
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
    }

    public int getAttackAnimationTick() {
        return this.attackAnimationTick;
    }

    /*============*/
    /*   SOUNDS   */
    /*============*/

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PANDA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PANDA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PANDA_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.PANDA_STEP, 0.15F, 1.3F);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    public float getVoicePitch() {
        // Higher than the panda it borrows its voice from, closer to a chimpanzee hoot
        return super.getVoicePitch() * 1.35F;
    }
}
