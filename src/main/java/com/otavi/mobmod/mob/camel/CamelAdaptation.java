package com.otavi.mobmod.mob.camel;

import com.otavi.mobmod.core.AdaptedMob;
import com.otavi.mobmod.entity.traits.ModCreature;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.item.Items;

/**
 * The vanilla camel, adapted to the rules of the mod by {@link com.otavi.mobmod.mixin.CamelMixin}: a sex and a skin
 * variant, the info bar, regeneration and sleep. Everything else is the game's own: model and animations, sounds,
 * riding and dash, its brain AI (wandering, panicking, sitting down now and then), loot, spawns and spawn egg.
 * <p>
 * Only {@code minecraft:camel} is adapted: the camel husk extends the camel class but stays as it is.
 */
public final class CamelAdaptation {
    public static final AdaptedMob<Camel> MOB = new AdaptedMob<>("camel", EntityTypes.CAMEL, Items.CACTUS);

    private CamelAdaptation() {
    }

    /**
     * True for the camel itself, false for the camel husk (and any other subclass).
     */
    public static boolean isAdapted(Camel camel) {
        return camel.getType() == EntityTypes.CAMEL;
    }

    /**
     * The interface the mixin adds to every camel (only meaningful when {@link #isAdapted}).
     */
    public static ModCreature creature(Camel camel) {
        return (ModCreature) camel;
    }
}
