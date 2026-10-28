package com.otavi.mobmod.core;

import com.otavi.mobmod.mob.armadillo.ArmadilloAdaptation;
import com.otavi.mobmod.mob.camel.CamelAdaptation;
import com.otavi.mobmod.mob.chimpanzee.ChimpanzeeMob;
import com.otavi.mobmod.mob.elephant.ElephantMob;
import com.otavi.mobmod.mob.meerkat.MeerkatMob;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of every mob in the mod. Adding a mob = adding its definition here (and its client side to ModMobsClient).
 */
public final class ModMobs {
    public static final List<MobDefinition<?>> ALL = List.of(
            ElephantMob.DEFINITION,
            ChimpanzeeMob.DEFINITION,
            MeerkatMob.DEFINITION
    );

    /**
     * Vanilla animals adapted to the rules of the mod (by mixins). Adapting one = adding it here, plus its mixin and
     * its client side (see docs/projeto/animais-vanilla.md).
     */
    public static final List<AdaptedMob<?>> ADAPTED = List.of(
            CamelAdaptation.MOB,
            ArmadilloAdaptation.MOB
    );

    private ModMobs() {
    }

    /**
     * Entity type of every animal that follows the rules of the mod, made from scratch or adapted, for the tests.
     */
    public static List<EntityType<?>> allAnimalTypes() {
        var types = new ArrayList<EntityType<?>>();
        ALL.forEach(mob -> types.add(mob.type()));
        ADAPTED.forEach(mob -> types.add(mob.type()));
        return types;
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
