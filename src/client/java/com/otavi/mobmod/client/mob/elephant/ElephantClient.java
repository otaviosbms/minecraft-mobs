package com.otavi.mobmod.client.mob.elephant;

import com.otavi.mobmod.client.core.ClientMob;
import com.otavi.mobmod.mob.elephant.ElephantMob;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class ElephantClient implements ClientMob {
    public static final ModelLayerLocation ADULT_LAYER = ClientMob.layer("elephant");
    public static final ModelLayerLocation BABY_LAYER = ClientMob.layer("elephant_baby");

    @Override
    public void register(Registrar registrar) {
        registrar.layer(ADULT_LAYER, ElephantModel::createAdultLayer);
        registrar.layer(BABY_LAYER, ElephantModel::createCalfLayer);
        registrar.renderer(ElephantMob.TYPE, ElephantRenderer::new);
    }
}
