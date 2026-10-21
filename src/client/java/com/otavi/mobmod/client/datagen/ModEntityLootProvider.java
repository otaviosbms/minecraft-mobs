package com.otavi.mobmod.client.datagen;

import com.otavi.mobmod.core.MobLoot;
import com.otavi.mobmod.core.ModMobs;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricEntityLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ModEntityLootProvider extends FabricEntityLootSubProvider {
    public ModEntityLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generate() {
        var context = new MobLoot.Context(this.items, this.enchantments);
        for (var mob : ModMobs.ALL) {
            if (mob.loot() != null) {
                var key = mob.type().getDefaultLootTable().orElseThrow();
                // Same random sequence as vanilla entity loot tables: one per table, named after it
                this.add(mob.type(), mob.loot().create(context).setRandomSequence(key.identifier()));
            }
        }
    }
}
