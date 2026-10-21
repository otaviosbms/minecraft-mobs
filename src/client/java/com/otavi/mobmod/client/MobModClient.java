package com.otavi.mobmod.client;

import com.otavi.mobmod.client.render.entity.ElephantRenderer;
import com.otavi.mobmod.client.render.entity.model.ModModelLayers;
import com.otavi.mobmod.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class MobModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModModelLayers.register((layer, definition) -> ModelLayerRegistry.registerModelLayer(layer, definition::get));
        EntityRenderers.register(ModEntityTypes.ELEPHANT, ElephantRenderer::new);
    }
}
