package com.otavi.mobmod.client.mob.camel;

import com.otavi.mobmod.client.core.ClientMob;
import com.otavi.mobmod.client.core.VanillaSkinSpec;
import com.otavi.mobmod.client.core.VanillaVariantTextures;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;

import java.util.List;

/**
 * Client side of the adapted camel: its renderer replaces the vanilla one, and its skin variants are made from the
 * game's camel textures.
 */
public final class CamelClient implements ClientMob {
    public static final ModelLayerLocation ADULT_LAYER = ClientMob.layer("camel");
    public static final ModelLayerLocation BABY_LAYER = ClientMob.layer("camel_baby");

    /**
     * Adult eyes: a dark line one pixel tall on row 8, two pixels on each side face and one round the front corner.
     */
    public static final VanillaSkinSpec ADULT_SKIN = new VanillaSkinSpec(
            Identifier.withDefaultNamespace("textures/entity/camel/camel.png"), "camel/camel",
            List.of(px(26, 8), px(27, 8), px(28, 8), px(34, 8), px(35, 8), px(36, 8)),
            24, 8,
            List.of(CamelEyelids.ADULT_RIGHT_UV, CamelEyelids.ADULT_LEFT_UV));
    /**
     * Calf eyes: two pixels on row 6 of each side of the head.
     */
    public static final VanillaSkinSpec BABY_SKIN = new VanillaSkinSpec(
            Identifier.withDefaultNamespace("textures/entity/camel/camel_baby.png"), "camel/camel_baby",
            List.of(px(3, 6), px(4, 6), px(10, 6), px(11, 6)),
            1, 6,
            List.of(CamelEyelids.BABY_RIGHT_UV, CamelEyelids.BABY_LEFT_UV));

    @Override
    public void register(Registrar registrar) {
        registrar.layer(ADULT_LAYER, ModAdultCamelModel::createLayer);
        registrar.layer(BABY_LAYER, ModBabyCamelModel::createLayer);
        registrar.renderer(EntityTypes.CAMEL, ModCamelRenderer::new);
        VanillaVariantTextures.register(ADULT_SKIN);
        VanillaVariantTextures.register(BABY_SKIN);
    }

    private static VanillaSkinSpec.Pixel px(int x, int y) {
        return new VanillaSkinSpec.Pixel(x, y);
    }
}
