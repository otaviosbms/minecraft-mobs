package com.otavi.mobmod.client.render.entity.model;

import com.otavi.mobmod.MobMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ModModelLayers {
    public static final ModelLayerLocation ELEPHANT = ofMain("elephant");
    public static final ModelLayerLocation ELEPHANT_BABY = ofMain("elephant_baby");

    public static void register(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registrar) {
        registrar.accept(ELEPHANT, ElephantModel::createAdultLayer);
        registrar.accept(ELEPHANT_BABY, ElephantModel::createCalfLayer);
    }

    private static ModelLayerLocation ofMain(String name) {
        return new ModelLayerLocation(MobMod.id(name), "main");
    }
}
