package com.otavi.mobmod.client.mob.meerkat;

import com.otavi.mobmod.client.core.ClientMob;
import com.otavi.mobmod.mob.meerkat.MeerkatMob;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class MeerkatClient implements ClientMob {
    public static final ModelLayerLocation ADULT_LAYER = ClientMob.layer("meerkat");
    public static final ModelLayerLocation BABY_LAYER = ClientMob.layer("meerkat_baby");

    @Override
    public void register(Registrar registrar) {
        registrar.layer(ADULT_LAYER, MeerkatModel::createAdultLayer);
        registrar.layer(BABY_LAYER, MeerkatModel::createBabyLayer);
        registrar.renderer(MeerkatMob.TYPE, MeerkatRenderer::new);
    }
}
