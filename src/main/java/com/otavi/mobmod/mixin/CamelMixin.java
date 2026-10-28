package com.otavi.mobmod.mixin;

import com.otavi.mobmod.entity.traits.AnimalTraits;
import com.otavi.mobmod.entity.traits.ModCreature;
import com.otavi.mobmod.mob.camel.CamelAdaptation;
import com.otavi.mobmod.mob.camel.CamelSleep;
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
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
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
 * Adapts the vanilla camel to the rules of the mod: it becomes a {@link ModCreature} and wires the shared
 * {@link AnimalTraits} into the vanilla methods, exactly like {@link com.otavi.mobmod.entity.genetics.ModAnimal} does
 * for the animals made from scratch. Sleep follows {@link CamelSleep}, since the camel's AI is a brain.
 * <p>
 * Every hook checks {@link CamelAdaptation#isAdapted}, so the camel husk (a subclass) keeps its vanilla behavior.
 * Extends {@link AbstractHorse} only to reach its protected methods; Mixin never instantiates it.
 */
@Mixin(Camel.class)
public abstract class CamelMixin extends AbstractHorse implements ModCreature {
    @Unique
    private static final EntityDataAccessor<Byte> MOBMOD$DATA_SEX = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.BYTE);
    @Unique
    private static final EntityDataAccessor<Byte> MOBMOD$DATA_SKIN_VARIANT = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.BYTE);
    @Unique
    private static final EntityDataAccessor<Boolean> MOBMOD$DATA_ASLEEP = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.BOOLEAN);

    @Unique
    @Nullable
    private AnimalTraits mobmod$traits;
    @Unique
    @Nullable
    private CamelSleep mobmod$sleep;

    protected CamelMixin(EntityType<? extends AbstractHorse> type, Level level) {
        super(type, level);
    }

    @Unique
    private Camel mobmod$self() {
        return (Camel) (Object) this;
    }

    @Unique
    private boolean mobmod$adapted() {
        return CamelAdaptation.isAdapted(this.mobmod$self());
    }

    @Unique
    private CamelSleep mobmod$sleep() {
        if (this.mobmod$sleep == null) {
            this.mobmod$sleep = new CamelSleep();
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
     * Not while dashing: a camel never falls asleep at full speed.
     */
    @Override
    public boolean canSleep() {
        return this.mobmod$traits().canSleep() && !this.mobmod$self().isDashing();
    }

    /**
     * Kneels down to sleep, the resting pose the game already has for camels.
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
        // Defined for every camel (husk included), since the ids belong to the camel class
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
        // Camels from worlds saved before the mod get a random sex here
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
        // Vanilla already stands the camel up when it gets hurt
        if (this.mobmod$adapted()) {
            this.mobmod$traits().onHurt();
        }
    }

    /**
     * No calls in the middle of the night (the camel does not override baseTick, so the sound is muted here instead).
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

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void mobmod$genes(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                              @Nullable SpawnGroupData groupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (this.mobmod$adapted()) {
            this.mobmod$traits().onFinalizeSpawn(reason);
        }
    }

    @Inject(method = "canMate", at = @At("RETURN"), cancellable = true)
    private void mobmod$onlyOppositeSex(Animal other, CallbackInfoReturnable<Boolean> cir) {
        if (this.mobmod$adapted() && cir.getReturnValueZ()) {
            cir.setReturnValue(this.mobmod$traits().isOppositeSex(other));
        }
    }

    @Inject(method = "getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/animal/camel/Camel;",
            at = @At("RETURN"))
    private void mobmod$calfGenes(ServerLevel level, AgeableMob partner, CallbackInfoReturnable<Camel> cir) {
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
