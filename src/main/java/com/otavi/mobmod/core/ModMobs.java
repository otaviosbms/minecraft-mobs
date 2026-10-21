package com.otavi.mobmod.core;

import com.otavi.mobmod.mob.chimpanzee.ChimpanzeeMob;
import com.otavi.mobmod.mob.elephant.ElephantMob;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The list of every mob in the mod. Adding a mob = adding its definition here (and its client side to ModMobsClient).
 */
public final class ModMobs {
    public static final List<MobDefinition<?>> ALL = List.of(
            ElephantMob.DEFINITION,
            ChimpanzeeMob.DEFINITION
    );

    private ModMobs() {
    }

    public static void init() {
        ALL.forEach(MobDefinition::register);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> {
            for (var mob : ALL) {
                var egg = mob.spawnEgg();
                if (egg == null) {
                    continue;
                }
                if (mob.spawnEggAfter() != null) {
                    output.insertAfter(mob.spawnEggAfter(), egg);
                } else {
                    output.accept(new ItemStack(egg), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
        });
    }
}
