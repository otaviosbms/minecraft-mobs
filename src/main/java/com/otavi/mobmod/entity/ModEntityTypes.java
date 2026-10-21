package com.otavi.mobmod.entity;

import com.otavi.mobmod.MobMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;

public class ModEntityTypes {
    public static final EntityType<Elephant> ELEPHANT = register("elephant", EntityType.Builder.of(Elephant::new, MobCategory.CREATURE)
            .sized(1.8f, 2.2f)
            .eyeHeight(1.9f)
            .clientTrackingRange(10));

    private static <T extends Entity> EntityType<T> register(String path, EntityType.Builder<T> type) {
        var key = ResourceKey.create(Registries.ENTITY_TYPE, MobMod.id(path));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type.build(key));
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(ELEPHANT, Elephant.createElephantAttributes());
    }

    public static void registerSpawnPlacements() {
        // Made accessible by mobmod.accesswidener
        SpawnPlacements.register(ELEPHANT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
    }

    public static void appendWorldGen() {
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_SAVANNA), MobCategory.CREATURE, ELEPHANT, 5, 2, 4);
    }
}
