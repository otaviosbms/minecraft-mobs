package com.otavi.mobmod.mob.elephant;

import com.otavi.mobmod.entity.goal.AdultMeleeAttackGoal;
import com.otavi.mobmod.entity.goal.BabyPanicGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A neutral savanna animal: it wanders in herds and only fights back when it, or a calf of its herd, gets hurt.
 */
public class Elephant extends Animal {
    private static final byte ATTACK_EVENT = 4;
    public static final int ATTACK_ANIMATION_LENGTH = 10;

    private int attackAnimationTick;

    public Elephant(EntityType<? extends Elephant> type, Level level) {
        super(type, level);
    }

    /*========*/
    /*   AI   */
    /*========*/

    public static AttributeSupplier.Builder createElephantAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AdultMeleeAttackGoal(this, 1.25, true));
        this.goalSelector.addGoal(1, new BabyPanicGoal(this, 1.6));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, stack -> stack.is(ElephantMob.FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Calves don't fight, but they still call the adults around them
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ElephantMob.FOOD);
    }

    @Nullable
    @Override
    public Elephant getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ElephantMob.TYPE.create(level, EntitySpawnReason.BREEDING);
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
            this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, this.getVoicePitch());
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
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.3F, 0.8F);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    public float getVoicePitch() {
        // Deeper than the ravager it borrows its voice from
        return super.getVoicePitch() * 0.8F;
    }
}
