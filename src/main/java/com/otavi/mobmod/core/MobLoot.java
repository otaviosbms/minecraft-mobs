package com.otavi.mobmod.core;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Builds a mob's loot table. Only called by datagen, which writes the result to src/main/generated.
 */
@FunctionalInterface
public interface MobLoot {
    LootTable.Builder create(Context context);

    record Context(HolderGetter<Item> items, HolderGetter<Enchantment> enchantments) {
    }
}
