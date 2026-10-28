package com.otavi.mobmod.mixin;

import com.otavi.mobmod.entity.traits.AnimalTraits;
import com.otavi.mobmod.entity.traits.ModCreature;
import com.otavi.mobmod.mob.armadillo.ArmadilloAdaptation;
import com.otavi.mobmod.mob.armadillo.ArmadilloSleep;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adapts the vanilla armadillo to the rules of the mod, like {@link CamelMixin} does for the camel: it becomes a
 * {@link ModCreature} and wires the shared {@link AnimalTraits} into the vanilla methods. Sleep follows
 * {@link ArmadilloSleep}, since the armadillo's AI is a brain.
 * <p>
 * The armadillo does not override {@code finalizeSpawn} and {@code canMate}, so this mixin adds those two overrides
 * (calling the vanilla ones through {@code super}) instead of injecting into them. Extends {@link Animal} only to reach
 * those and its protected methods; Mixin never instantiates it.
 */
@Mixin(Armadillo.class)
public abstract class ArmadilloMixin extends Animal implements ModCreature {
    @Unique
    private static final EntityDataAccessor<Byte> MOBMOD$DATA_SEX = SynchedEntityData.defineId(Armadillo.class, EntityDataSerializers.BYTE);
    @Unique
    private static final EntityDataAccessor<Byte> MOBMOD$DATA_SKIN_VARIANT = SynchedEntityData.defineId(Armadillo.class, EntityDataSerializers.BYTE);
    @Unique
    private static final EntityDataAccessor<Boolean> MOBMOD$DATA_ASLEEP = SynchedEntityData.defineId(Armadillo.class, EntityDataSerializers.BOOLEAN);

    @Unique
    @Nullable
    private AnimalTraits mobmod$traits;
    @Unique
    @Nullable
    private ArmadilloSleep mobmod$sleep;

    protected ArmadilloMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Unique
    private Armadillo mobmod$self() {
        return (Armadillo) (Object) this;
    }

    @Unique
    private boolean mobmod$adapted() {
        return ArmadilloAdaptation.isAdapted(this.mobmod$self());
    }

    @Unique
    private ArmadilloSleep mobmod$sleep() {
        if (this.mobmod$sleep == null) {
            this.mobmod$sleep = new ArmadilloSleep();
        }
        return this.mobmod$sleep;
    }

    @Override
    public AnimalTraits mobmod$traits() {
        // Created on first use: vanilla calls defineSynchedData before any field of this class is set
        if (this.mobmod$traits == null) {
            this.mobmod$traits = new AnimalTraits(this, MOBMOD$DATA_SEX, MOBMOD$DATA_SKIN_VARIANT, MOBMOD$DATA_ASLEEP);
        }
        return this.mobmod$traits;
    }

    @Override
    public boolean isModCreature() {
        return this.mobmod$adapted();
    }

    /**
     * Only where it could stay rolled up (not panicking, in water, leashed or riding), and, asleep, only while it is
     * in its shell: anything that unrolls it wakes it up. Rolling up shrinks its hitbox, which lifts it off the ground
     * for a tick, so a sleeper in its shell does not need to touch the ground.
     */
    @Override
    public boolean canSleep() {
        var armadillo = this.mobmod$self();
        var traits = this.mobmod$traits();
        if (traits.isAsleep()) {
            return armadillo.isScared() && traits.canSleep(false) && armadillo.canStayRolledUp();
        }
        return traits.canSleep() && armadillo.canStayRolledUp();
    }

    /**
     * Rolls up into its shell to sleep, the pose the game already has for armadillos.
     */
    @Override
    public boolean fallAsleep() {
        return this.mobmod$sleep().fallAsleepNow(this.mobmod$self());
    }

    /*==========*/
    /*   DATA   */
    /*==========*/

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void mobmod$defineData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        AnimalTraits.defineData(builder, MOBMOD$DATA_SEX, MOBMOD$DATA_SKIN_VARIANT, MOBMOD$DATA_ASLEEP);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void mobmod$save(ValueOutput output, CallbackInfo ci) {
        if (this.mobmod$adapted()) {
            this.mobmod$traits().save(output);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mobmod$load(ValueInput input, CallbackInfo ci) {
        // Armadillos from worlds saved before the mod get a random sex here
        if (this.mobmod$adapted()) {
            this.mobmod$traits().load(input);
        }
    }

    /*===========*/
    /*   TICKS   */
    /*===========*/

    @Inject(method = "tick", at = @At("TAIL"))
    private void mobmod$tick(CallbackInfo ci) {
        if (this.mobmod$adapted()) {
            this.mobmod$traits().tick();
        }
    }

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void mobmod$serverAiStep(ServerLevel level, CallbackInfo ci) {
        if (this.mobmod$adapted()) {
            this.mobmod$traits().serverAiStep();
            this.mobmod$sleep().tick(this.mobmod$self());
        }
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void mobmod$hurt(ServerLevel level, DamageSource source, float damage, CallbackInfo ci) {
        // Wakes up; vanilla keeps it rolled up if the damage scared it
        if (this.mobmod$adapted()) {
            this.mobmod$traits().onHurt();
        }
    }

    /**
     * No calls in the middle of the night (the armadillo does not override baseTick, so the sound is muted here).
     */
    @Inject(method = "getAmbientSound", at = @At("RETURN"), cancellable = true)
    private void mobmod$quietWhileAsleep(CallbackInfoReturnable<SoundEvent> cir) {
        if (this.mobmod$adapted() && this.isAsleep()) {
            cir.setReturnValue(null);
        }
    }

    /*==============*/
    /*   GENETICS   */
    /*==============*/

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        if (this.mobmod$adapted()) {
            this.mobmod$traits().onFinalizeSpawn(reason);
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean canMate(Animal other) {
        return super.canMate(other) && (!this.mobmod$adapted() || this.mobmod$traits().isOppositeSex(other));
    }

    @Inject(method = "getBreedOffspring", at = @At("RETURN"))
    private void mobmod$calfGenes(ServerLevel level, AgeableMob partner, CallbackInfoReturnable<AgeableMob> cir) {
        ModCreature calf = ModCreature.of(cir.getReturnValue());
        if (this.mobmod$adapted() && calf != null) {
            this.mobmod$traits().giveGenesTo(calf, partner);
        }
    }

    /*==========*/
    /*   FOOD   */
    /*==========*/

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void mobmod$healWithFood(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (this.mobmod$adapted() && this.mobmod$traits().feedWhileInjured(player, hand)) {
            if (!this.level().isClientSide()) {
                this.playEatingSound();
            }
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
