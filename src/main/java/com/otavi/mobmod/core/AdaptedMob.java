package com.otavi.mobmod.core;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;

/**
 * A vanilla animal the mod adapts to its own rules (genetics, info bar, regeneration, sleep) instead of making it from
 * scratch. Nothing is registered: the entity type, spawn egg, loot, spawns, sounds and AI are the game's own. The
 * behavior is added by mixins (see {@code docs/projeto/animais-vanilla.md}); this record only lists the animal, so
 * the tests cover it like any other mob.
 *
 * @param name       name used in test messages
 * @param type       the vanilla entity type
 * @param sampleFood one item of its vanilla food, used by the tests
 */
public record AdaptedMob<T extends Mob>(String name, EntityType<T> type, Item sampleFood) {
}
