package com.otavi.mobmod.client.mob.elephant;

import com.otavi.mobmod.client.core.ModMobRenderer;
import com.otavi.mobmod.mob.elephant.Elephant;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Textures come from {@link ModMobRenderer}: elephant.png, elephant_albino.png and elephant_melanistic.png.
 * The calf model is a scaled-down adult, so both share the same UV layout.
 */
public class ElephantRenderer extends ModMobRenderer<Elephant, ElephantRenderState, ElephantModel> {
    public ElephantRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ElephantModel(context.bakeLayer(ElephantClient.ADULT_LAYER)),
                new ElephantModel(context.bakeLayer(ElephantClient.BABY_LAYER)),
                1.8F
        );
    }

    @Override
    public ElephantRenderState createRenderState() {
        return new ElephantRenderState();
    }

    @Override
    public void extractRenderState(Elephant elephant, ElephantRenderState state, float partialTick) {
        super.extractRenderState(elephant, state, partialTick);
        int tick = elephant.getAttackAnimationTick();
        state.attackProgress = tick > 0 ? Math.max(0.0F, (tick - partialTick) / Elephant.ATTACK_ANIMATION_LENGTH) : 0.0F;
    }

    /**
     * Lies on its side like the default pose: the body (26 px wide) rests on the ground, the flat ears just above it.
     */
    @Override
    protected float getSleepingHalfWidth(ElephantRenderState state) {
        return state.boundingBoxWidth * 0.32F;
    }
}
