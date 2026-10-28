package com.otavi.mobmod.client.mob.armadillo;

import net.minecraft.client.model.animal.armadillo.AdultArmadilloModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.ArmadilloRenderState;

/**
 * The vanilla adult armadillo model and animations, plus the eyelids of {@link ArmadilloEyelids}.
 */
public class ModAdultArmadilloModel extends AdultArmadilloModel {
    private final ModelPart headCube;

    public ModAdultArmadilloModel(ModelPart root) {
        super(root);
        this.headCube = ArmadilloEyelids.headCube(root);
    }

    /**
     * The vanilla mesh (64x64 texture; opened by the access widener) with the eyelids over the eyes: the head cube is
     * 3 wide, the eyes 1 px at y=1, at its front (z=-1).
     */
    public static LayerDefinition createLayer() {
        var mesh = AdultArmadilloModel.createBodyLayer().mesh;
        ArmadilloEyelids.add(mesh, 1.5F, 1.0F, -1.0F);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(ArmadilloRenderState state) {
        super.setupAnim(state);
        ArmadilloEyelids.animate(this.headCube, state);
    }
}
