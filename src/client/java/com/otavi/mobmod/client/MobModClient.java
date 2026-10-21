package com.otavi.mobmod.client;

import com.otavi.mobmod.client.core.ModMobsClient;
import com.otavi.mobmod.core.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class MobModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModMobsClient.init();
        // Projectiles are drawn as their item, like vanilla snowballs
        EntityRenderers.register(ModEntityTypes.FECES, ThrownItemRenderer::new);
    }
}
