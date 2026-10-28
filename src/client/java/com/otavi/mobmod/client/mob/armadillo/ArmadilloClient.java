package com.otavi.mobmod.client.mob.armadillo;

import com.otavi.mobmod.client.core.ClientMob;
import com.otavi.mobmod.client.core.VanillaSkinSpec;
import com.otavi.mobmod.client.core.VanillaVariantTextures;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;

import java.util.List;

/**
 * Client side of the adapted armadillo: its renderer replaces the vanilla one, and its skin variants are made from the
 * game's armadillo textures.
 */
public final class ArmadilloClient implements ClientMob {
    public static final ModelLayerLocation ADULT_LAYER = ClientMob.layer("armadillo");
    public static final ModelLayerLocation BABY_LAYER = ClientMob.layer("armadillo_baby");

    /**
     * Adult eyes: one dark pixel on row 19 of each side of the head cube, at its front.
     */
    public static final VanillaSkinSpec ADULT_SKIN = new VanillaSkinSpec(
            Identifier.withDefaultNamespace("textures/entity/armadillo/armadillo.png"), "armadillo/armadillo",
            List.of(px(44, 19), px(48, 19)),
            43, 19,
            List.of(ArmadilloEyelids.RIGHT_UV, ArmadilloEyelids.LEFT_UV));
    /**
     * Baby eyes: one dark pixel on row 21 of each side of the head cube, one pixel behind its front.
     */
    public static final VanillaSkinSpec BABY_SKIN = new VanillaSkinSpec(
            Identifier.withDefaultNamespace("textures/entity/armadillo/armadillo_baby.png"), "armadillo/armadillo_baby",
            List.of(px(22, 21), px(27, 21)),
            21, 21,
            List.of(ArmadilloEyelids.RIGHT_UV, ArmadilloEyelids.LEFT_UV));

    @Override
    public void register(Registrar registrar) {
        registrar.layer(ADULT_LAYER, ModAdultArmadilloModel::createLayer);
        registrar.layer(BABY_LAYER, ModBabyArmadilloModel::createLayer);
        registrar.renderer(EntityTypes.ARMADILLO, ModArmadilloRenderer::new);
        VanillaVariantTextures.register(ADULT_SKIN);
        VanillaVariantTextures.register(BABY_SKIN);
    }

    private static VanillaSkinSpec.Pixel px(int x, int y) {
        return new VanillaSkinSpec.Pixel(x, y);
    }
}
