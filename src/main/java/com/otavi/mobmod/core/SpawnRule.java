package com.otavi.mobmod.core;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Predicate;

/**
 * Where and how a mob spawns naturally: the biome spawn entry plus its spawn placement.
 */
public record SpawnRule<T extends Mob>(
        Predicate<BiomeSelectionContext> biomes,
        int weight,
        int minGroupSize,
        int maxGroupSize,
        SpawnPlacementType placementType,
        Heightmap.Types heightmap,
        SpawnPlacements.SpawnPredicate<T> predicate
) {
    /**
     * Land animal on grass, like cows or sheep.
     */
    public static <T extends Animal> SpawnRule<T> animal(TagKey<Biome> biomes, int weight, int minGroupSize, int maxGroupSize) {
        return new SpawnRule<>(BiomeSelectors.tag(biomes), weight, minGroupSize, maxGroupSize,
                SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
    }
}
