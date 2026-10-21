package com.otavi.mobmod;

import com.google.common.reflect.Reflection;
import com.otavi.mobmod.entity.ModEntityTypes;
import com.otavi.mobmod.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class MobMod implements ModInitializer {
    public static final String MOD_ID = "mobmod";

    @Override
    public void onInitialize() {
        // Same order as Promenade.init(): entity types first, then items that reference them
        Reflection.initialize(ModEntityTypes.class);
        Reflection.initialize(ModItems.class);

        ModEntityTypes.registerAttributes();
        ModEntityTypes.registerSpawnPlacements();
        ModEntityTypes.appendWorldGen();
        ModItems.appendItemGroups();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
