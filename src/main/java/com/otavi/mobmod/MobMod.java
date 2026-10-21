package com.otavi.mobmod;

import com.otavi.mobmod.core.ModMobs;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class MobMod implements ModInitializer {
    public static final String MOD_ID = "mobmod";

    @Override
    public void onInitialize() {
        ModMobs.init();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
