package com.otavi.mobmod.mob.armadillo;

import com.otavi.mobmod.core.AdaptedMob;
import com.otavi.mobmod.entity.traits.ModCreature;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.item.Items;

/**
 * The vanilla armadillo, adapted to the rules of the mod by {@link com.otavi.mobmod.mixin.ArmadilloMixin}: a sex and a
 * skin variant, the info bar, regeneration and sleep. Everything else is the game's own: model and animations,
 * sounds, rolling up when scared, scutes and brushing, its brain AI, loot, spawns and spawn egg.
 */
public final class ArmadilloAdaptation {
    public static final AdaptedMob<Armadillo> MOB = new AdaptedMob<>("armadillo", EntityTypes.ARMADILLO, Items.SPIDER_EYE);

    private ArmadilloAdaptation() {
    }

    /**
     * True for the armadillo itself, false for any subclass another mod might add.
     */
    public static boolean isAdapted(Armadillo armadillo) {
        return armadillo.getType() == EntityTypes.ARMADILLO;
    }

    /**
     * The interface the mixin adds to every armadillo (only meaningful when {@link #isAdapted}).
     */
    public static ModCreature creature(Armadillo armadillo) {
        return (ModCreature) armadillo;
    }
}
