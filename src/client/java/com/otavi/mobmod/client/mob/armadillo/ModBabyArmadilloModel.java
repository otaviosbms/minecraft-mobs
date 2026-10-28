package com.otavi.mobmod.client.mob.armadillo;

import net.minecraft.client.model.animal.armadillo.BabyArmadilloModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.ArmadilloRenderState;

/**
 * The vanilla baby armadillo model and animations, plus the eyelids of {@link ArmadilloEyelids}.
 */
public class ModBabyArmadilloModel extends BabyArmadilloModel {
    private final ModelPart headCube;

    public ModBabyArmadilloModel(ModelPart root) {
        super(root);
        this.headCube = ArmadilloEyelids.headCube(root);
    }

    /**
     * The vanilla mesh (64x64 texture; opened by the access widener) with the eyelids over the eyes: the head cube is
     * 2 wide, the eyes 1 px at y=-2, one pixel behind its front (z=-3).
     */
    public static LayerDefinition createLayer() {
        var mesh = BabyArmadilloModel.createBodyLayer().mesh;
        ArmadilloEyelids.add(mesh, 1.0F, -2.0F, -3.0F);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(ArmadilloRenderState state) {
        super.setupAnim(state);
        ArmadilloEyelids.animate(this.headCube, state);
    }
}
