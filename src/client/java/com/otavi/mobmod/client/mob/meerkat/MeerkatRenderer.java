package com.otavi.mobmod.client.mob.meerkat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.otavi.mobmod.client.core.ModMobRenderer;
import com.otavi.mobmod.mob.meerkat.Meerkat;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Textures come from {@link ModMobRenderer}: meerkat.png, meerkat_albino.png and meerkat_melanistic.png.
 * <p>
 * While it digs, the whole meerkat sinks into the ground (the block hides what is below the surface), its shadow
 * shrinks and the info bar goes away.
 */
public class MeerkatRenderer extends ModMobRenderer<Meerkat, MeerkatRenderState, MeerkatModel> {
    private static final float SHADOW_RADIUS = 0.3F;

    public MeerkatRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new MeerkatModel(context.bakeLayer(MeerkatClient.ADULT_LAYER)),
                new MeerkatModel(context.bakeLayer(MeerkatClient.BABY_LAYER)),
                SHADOW_RADIUS
        );
    }

    @Override
    public MeerkatRenderState createRenderState() {
        return new MeerkatRenderState();
    }

    @Override
    public void extractRenderState(Meerkat meerkat, MeerkatRenderState state, float partialTick) {
        super.extractRenderState(meerkat, state, partialTick);
        state.standAmount = meerkat.getStandAmount(partialTick);
        int tick = meerkat.getAttackAnimationTick();
        state.attackProgress = tick > 0 ? Math.max(0.0F, (tick - partialTick) / Meerkat.ATTACK_ANIMATION_LENGTH) : 0.0F;
        state.burrowDepth = meerkat.getBurrowDepth(partialTick);
        var burrow = meerkat.getBurrowState();
        state.digging = burrow == Meerkat.BurrowState.DIGGING || burrow == Meerkat.BurrowState.EMERGING;
        state.showInfoBar &= state.burrowDepth == 0.0F;
    }

    @Override
    protected void setupRotations(MeerkatRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        // The pose is already scaled by the entity scale here, so the depth is given in unscaled blocks
        if (state.burrowDepth > 0.0F) {
            poseStack.translate(0.0F, -state.burrowDepth * (state.boundingBoxHeight / state.scale + 0.05F), 0.0F);
        }
        super.setupRotations(state, poseStack, bodyRot, scale);
    }

    /**
     * Sleeps flat on its belly, not on its side: the model lays itself down, nothing to turn here.
     */
    @Override
    protected void applySleepPose(MeerkatRenderState state, PoseStack poseStack, float amount) {
    }

    @Override
    protected float getShadowRadius(MeerkatRenderState state) {
        return super.getShadowRadius(state) * (1.0F - state.burrowDepth);
    }
}
