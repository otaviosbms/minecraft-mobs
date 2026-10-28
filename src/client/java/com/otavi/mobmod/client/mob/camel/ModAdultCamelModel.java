package com.otavi.mobmod.client.mob.camel;

import net.minecraft.client.model.animal.camel.AdultCamelModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.CamelRenderState;

/**
 * The vanilla adult camel model and animations, plus the eyelids and the sleeping head of {@link CamelEyelids}.
 */
public class ModAdultCamelModel extends AdultCamelModel {
    public ModAdultCamelModel(ModelPart root) {
        super(root);
    }

    /**
     * The vanilla mesh (128x128 texture) with the eyelids over the eyes: 1 px tall at y=-20, from the front of the
     * head (z=-15) 2 px back, round the corner at x=-3.5 and x=3.5.
     */
    public static LayerDefinition createLayer() {
        var mesh = createBodyMesh();
        CamelEyelids.add(mesh, -3.5F, -20.0F, -15.0F, CamelEyelids.ADULT_RIGHT_UV, CamelEyelids.ADULT_LEFT_UV);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(CamelRenderState state) {
        super.setupAnim(state);
        CamelEyelids.animate(this.head, state);
    }
}
