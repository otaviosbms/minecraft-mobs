package com.otavi.mobmod.client.datagen;

import com.otavi.mobmod.core.ModItems;
import com.otavi.mobmod.core.ModMobs;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
    }

    @Override
    public void generateItemModels(ItemModelGenerators generators) {
        // Spawn eggs use a flat texture at textures/item/<mob>_spawn_egg.png
        for (var item : ModItems.ALL) {
            generators.generateFlatItem(item.item(), ModelTemplates.FLAT_ITEM);
        }
        for (var mob : ModMobs.ALL) {
            if (mob.spawnEgg() != null) {
                generators.generateFlatItem(mob.spawnEgg(), ModelTemplates.FLAT_ITEM);
            }
        }
    }
}
