package com.otavi.mobmod.client.core;

import com.otavi.mobmod.client.mob.elephant.ElephantClient;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.function.Supplier;

/**
 * Client counterpart of {@link com.otavi.mobmod.core.ModMobs}: one entry per mob.
 */
public final class ModMobsClient {
    public static final List<ClientMob> ALL = List.of(
            new ElephantClient()
    );

    private ModMobsClient() {
    }

    public static void init() {
        var registrar = new ClientMob.Registrar() {
            @Override
            public void layer(ModelLayerLocation layer, Supplier<LayerDefinition> definition) {
                ModelLayerRegistry.registerModelLayer(layer, definition::get);
            }

            @Override
            public <T extends Entity> void renderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                EntityRenderers.register(type, provider);
            }
        };
        ALL.forEach(mob -> mob.register(registrar));
    }
}
