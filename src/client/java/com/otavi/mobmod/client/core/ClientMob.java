package com.otavi.mobmod.client.core;

import com.otavi.mobmod.MobMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

/**
 * The client side of one mob: its model layers and renderer.
 */
public interface ClientMob {
    void register(Registrar registrar);

    static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(MobMod.id(name), "main");
    }

    interface Registrar {
        void layer(ModelLayerLocation layer, Supplier<LayerDefinition> definition);

        <T extends Entity> void renderer(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }
}
