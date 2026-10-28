package com.otavi.mobmod.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.otavi.mobmod.entity.traits.ModCreature;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla horses (and so camels) heal 1 point at random, about every 45 seconds. Adapted animals regenerate by the
 * rules of the mod only ({@link com.otavi.mobmod.entity.traits.AnimalTraits}), so that random heal is skipped for them.
 */
@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {
    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;heal(F)V"))
    private boolean mobmod$onlyModRegeneration(AbstractHorse horse, float amount) {
        return ModCreature.of(horse) == null;
    }
}
