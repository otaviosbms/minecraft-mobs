package com.otavi.mobmod.mixin;

import com.otavi.mobmod.entity.traits.ModCreature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * While a danger is around, the vanilla armadillo AI keeps it rolled up, peeks out now and then and unrolls once it is
 * safe. A sleeping armadillo stays in its shell instead ({@link com.otavi.mobmod.mob.armadillo.ArmadilloSleep}): the
 * behavior is skipped until it wakes up.
 */
@Mixin(targets = "net.minecraft.world.entity.animal.armadillo.ArmadilloAi$ArmadilloBallUp")
public abstract class ArmadilloBallUpMixin {
    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/armadillo/Armadillo;J)V",
            at = @At("HEAD"), cancellable = true)
    private void mobmod$notWhileAsleep(ServerLevel level, Armadillo armadillo, long time, CallbackInfo ci) {
        ModCreature creature = ModCreature.of(armadillo);
        if (creature != null && creature.isAsleep()) {
            ci.cancel();
        }
    }
}
