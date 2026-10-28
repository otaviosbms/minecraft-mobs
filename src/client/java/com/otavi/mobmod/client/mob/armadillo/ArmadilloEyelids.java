package com.otavi.mobmod.client.mob.armadillo;

import com.otavi.mobmod.client.core.HasMobTraits;
import com.otavi.mobmod.client.core.VanillaSkinSpec;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ArmadilloRenderState;

/**
 * What the mod adds to the vanilla armadillo models (adult and baby): two eyelids on the head. Everything else
 * (geometry, animations, rolling up into the shell to sleep) is the game's own model.
 * <p>
 * The armadillo's eyes are one pixel on each side of its head cube, at the front. Each eyelid is a flat one-pixel
 * patch laid over it, {@link #EYELID_GAP} outside the face so it does not flicker; its texture is painted by
 * {@link com.otavi.mobmod.client.core.VanillaVariantTextures}. Rolled up, the head is inside the shell anyway: the
 * eyelids show while it rolls up and out.
 */
final class ArmadilloEyelids {
    static final String RIGHT_EYELID = "right_eyelid";
    static final String LEFT_EYELID = "left_eyelid";
    private static final String HEAD_CUBE = "head_cube";
    private static final float EYELID_GAP = 0.05F;

    /**
     * Eyelid patch: 0x1x1 (flat, facing the side).
     */
    static final VanillaSkinSpec.UvBox RIGHT_UV = new VanillaSkinSpec.UvBox(0, 60, 0, 1, 1);
    static final VanillaSkinSpec.UvBox LEFT_UV = new VanillaSkinSpec.UvBox(4, 60, 0, 1, 1);

    private ArmadilloEyelids() {
    }

    /**
     * Adds both eyelids to the head cube of a vanilla armadillo mesh.
     *
     * @param x half the width of the head cube (the eyes are on its sides)
     * @param y top of the eyes
     * @param z front of the eyes
     */
    static void add(MeshDefinition mesh, float x, float y, float z) {
        PartDefinition headCube = mesh.getRoot().getChild("body").getChild("head").getChild(HEAD_CUBE);
        headCube.addOrReplaceChild(RIGHT_EYELID, CubeListBuilder.create()
                .texOffs(RIGHT_UV.u(), RIGHT_UV.v()).addBox(-x - EYELID_GAP, y, z, 0.0F, 1.0F, 1.0F), PartPose.ZERO);
        headCube.addOrReplaceChild(LEFT_EYELID, CubeListBuilder.create()
                .texOffs(LEFT_UV.u(), LEFT_UV.v()).mirror().addBox(x + EYELID_GAP, y, z, 0.0F, 1.0F, 1.0F), PartPose.ZERO);
    }

    static ModelPart headCube(ModelPart root) {
        return root.getChild("body").getChild("head").getChild(HEAD_CUBE);
    }

    /**
     * After the vanilla animations: eyes closed while asleep.
     */
    static void animate(ModelPart headCube, ArmadilloRenderState state) {
        boolean closed = state instanceof HasMobTraits traits && traits.traits().eyesClosed();
        headCube.getChild(RIGHT_EYELID).visible = closed;
        headCube.getChild(LEFT_EYELID).visible = closed;
    }
}
