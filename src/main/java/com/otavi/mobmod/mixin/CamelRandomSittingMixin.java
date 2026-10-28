package com.otavi.mobmod.mixin;

import com.otavi.mobmod.entity.traits.ModCreature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.camel.CamelAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The vanilla camel AI sits down and stands up at random. A sleeping camel stays down until it wakes up
 * ({@link com.otavi.mobmod.mob.camel.CamelSleep}).
 */
@Mixin(CamelAi.RandomSitting.class)
public abstract class CamelRandomSittingMixin {
    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/camel/Camel;)Z",
            at = @At("HEAD"), cancellable = true)
    private void mobmod$notWhileAsleep(ServerLevel level, Camel camel, CallbackInfoReturnable<Boolean> cir) {
        ModCreature creature = ModCreature.of(camel);
        if (creature != null && creature.isAsleep()) {
            cir.setReturnValue(false);
        }
    }
}
