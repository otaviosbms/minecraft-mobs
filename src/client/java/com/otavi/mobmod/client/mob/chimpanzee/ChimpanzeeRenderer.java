package com.otavi.mobmod.client.mob.chimpanzee;

import com.otavi.mobmod.client.core.ModMobRenderer;
import com.otavi.mobmod.mob.chimpanzee.Chimpanzee;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Textures come from {@link ModMobRenderer}: chimpanzee.png, chimpanzee_albino.png and chimpanzee_melanistic.png.
 */
public class ChimpanzeeRenderer extends ModMobRenderer<Chimpanzee, ChimpanzeeRenderState, ChimpanzeeModel> {
    public ChimpanzeeRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ChimpanzeeModel(context.bakeLayer(ChimpanzeeClient.ADULT_LAYER)),
                new ChimpanzeeModel(context.bakeLayer(ChimpanzeeClient.BABY_LAYER)),
                0.6F
        );
    }

    @Override
    public ChimpanzeeRenderState createRenderState() {
        return new ChimpanzeeRenderState();
    }

    @Override
    public void extractRenderState(Chimpanzee chimpanzee, ChimpanzeeRenderState state, float partialTick) {
        super.extractRenderState(chimpanzee, state, partialTick);
        int tick = chimpanzee.getAttackAnimationTick();
        state.attackProgress = tick > 0 ? Math.max(0.0F, (tick - partialTick) / Chimpanzee.ATTACK_ANIMATION_LENGTH) : 0.0F;
        state.climbing = chimpanzee.onClimbable() && !chimpanzee.onGround();
    }
}
