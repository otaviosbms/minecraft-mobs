package com.otavi.mobmod.item;

import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.entity.ModEntityTypes;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Function;

public class ModItems {
    public static final Item ELEPHANT_SPAWN_EGG = register("elephant_spawn_egg", s -> new SpawnEggItem(s.spawnEgg(ModEntityTypes.ELEPHANT)));

    private static <O extends Item> O register(String path, Function<Item.Properties, O> factory) {
        var key = ResourceKey.create(Registries.ITEM, MobMod.id(path));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
    }

    public static void appendItemGroups() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.insertAfter(Items.ELDER_GUARDIAN_SPAWN_EGG, ELEPHANT_SPAWN_EGG));
    }
}
