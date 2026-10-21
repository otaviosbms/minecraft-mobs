package com.otavi.mobmod.client;

import com.otavi.mobmod.client.core.ModMobsClient;
import net.fabricmc.api.ClientModInitializer;

public class MobModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModMobsClient.init();
    }
}
