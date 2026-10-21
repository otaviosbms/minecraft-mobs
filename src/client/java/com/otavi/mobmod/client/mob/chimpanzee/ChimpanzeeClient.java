package com.otavi.mobmod.client.mob.chimpanzee;

import com.otavi.mobmod.client.core.ClientMob;
import com.otavi.mobmod.mob.chimpanzee.ChimpanzeeMob;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class ChimpanzeeClient implements ClientMob {
    public static final ModelLayerLocation ADULT_LAYER = ClientMob.layer("chimpanzee");
    public static final ModelLayerLocation BABY_LAYER = ClientMob.layer("chimpanzee_baby");

    @Override
    public void register(Registrar registrar) {
        registrar.layer(ADULT_LAYER, ChimpanzeeModel::createAdultLayer);
        registrar.layer(BABY_LAYER, ChimpanzeeModel::createBabyLayer);
        registrar.renderer(ChimpanzeeMob.TYPE, ChimpanzeeRenderer::new);
    }
}
