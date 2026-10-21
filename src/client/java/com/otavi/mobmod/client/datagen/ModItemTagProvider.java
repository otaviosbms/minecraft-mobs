package com.otavi.mobmod.client.datagen;

import com.otavi.mobmod.core.ModMobs;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        for (var mob : ModMobs.ALL) {
            var food = mob.food();
            if (food != null) {
                var tag = this.builder(food.tag());
                food.items().forEach(item -> tag.add(BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow()));
            }
        }
    }
}
