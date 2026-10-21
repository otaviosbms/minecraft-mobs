package com.otavi.mobmod.core;

import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.item.feces.ThrownFeces;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Entities that are not mobs (projectiles...). Mobs are declared with a {@link MobDefinition} in {@link ModMobs}.
 */
public final class ModEntityTypes {
    public static final EntityType<ThrownFeces> FECES = register("feces", EntityType.Builder.<ThrownFeces>of(ThrownFeces::new, MobCategory.MISC)
            // Same settings as the vanilla snowball
            .noLootTable()
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10));

    private ModEntityTypes() {
    }

    /**
     * Loads the class, which registers every entity type above.
     */
    public static void init() {
    }

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        var key = ResourceKey.create(Registries.ENTITY_TYPE, MobMod.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }
}
