package com.otavi.mobmod.core;

import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.item.feces.FecesItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Items that are not spawn eggs (spawn eggs come from each {@link MobDefinition}). Datagen generates their names and
 * flat item models from this list, so their only hand-made asset is the texture at textures/item/&lt;name&gt;.png.
 */
public final class ModItems {
    private static final List<Entry> ENTRIES = new ArrayList<>();
    public static final List<Entry> ALL = Collections.unmodifiableList(ENTRIES);

    /**
     * Thrown by chimpanzees (and players) like a snowball.
     */
    public static final Item FECES = register("feces", FecesItem::new, new Item.Properties().stacksTo(16),
            new Translation("Feces", "Fezes"), CreativeModeTabs.COMBAT, Items.SNOWBALL);

    public record Entry(Item item, Translation name, ResourceKey<CreativeModeTab> tab, Item after) {
    }

    private ModItems() {
    }

    public static void init() {
        for (var entry : ALL) {
            CreativeModeTabEvents.modifyOutputEvent(entry.tab()).register(output -> output.insertAfter(entry.after(), entry.item()));
        }
        DispenserBlock.registerProjectileBehavior(FECES);
    }

    private static <I extends Item> I register(String name, Function<Item.Properties, I> factory, Item.Properties properties,
                                               Translation translation, ResourceKey<CreativeModeTab> tab, Item after) {
        var key = ResourceKey.create(Registries.ITEM, MobMod.id(name));
        I item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
        ENTRIES.add(new Entry(item, translation, tab, after));
        return item;
    }
}
