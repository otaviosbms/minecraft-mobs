package com.otavi.mobmod.client.mob.camel;

import net.minecraft.client.model.animal.camel.BabyCamelModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.CamelRenderState;

/**
 * The vanilla calf model and animations, plus the eyelids and the sleeping head of {@link CamelEyelids}.
 */
public class ModBabyCamelModel extends BabyCamelModel {
    public ModBabyCamelModel(ModelPart root) {
        super(root);
    }

    /**
     * The vanilla mesh (64x64 texture; its mesh is opened by the access widener) with the eyelids over the eyes:
     * 1 px tall at y=-11 on the sides of the head (x=-2.5 and x=2.5), from z=-7.5 2 px back.
     */
    public static LayerDefinition createLayer() {
        var mesh = BabyCamelModel.createBodyLayer().mesh;
        CamelEyelids.add(mesh, -2.5F, -11.0F, -7.5F, CamelEyelids.BABY_RIGHT_UV, CamelEyelids.BABY_LEFT_UV);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(CamelRenderState state) {
        super.setupAnim(state);
        CamelEyelids.animate(this.head, state);
    }
}
