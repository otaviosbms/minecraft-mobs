package com.otavi.mobmod.client.render.entity;

import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.client.render.entity.model.ElephantModel;
import com.otavi.mobmod.client.render.entity.model.ModModelLayers;
import com.otavi.mobmod.client.render.entity.state.ElephantRenderState;
import com.otavi.mobmod.entity.Elephant;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class ElephantRenderer extends AgeableMobRenderer<Elephant, ElephantRenderState, ElephantModel> {
    // The calf model is a scaled-down adult, so both share the same UV layout and texture
    private static final Identifier TEXTURE = MobMod.id("textures/entity/elephant/elephant.png");

    public ElephantRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ElephantModel(context.bakeLayer(ModModelLayers.ELEPHANT)),
                new ElephantModel(context.bakeLayer(ModModelLayers.ELEPHANT_BABY)),
                1.2F
        );
    }

    @Override
    public ElephantRenderState createRenderState() {
        return new ElephantRenderState();
    }

    @Override
    public Identifier getTextureLocation(ElephantRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(Elephant elephant, ElephantRenderState state, float partialTick) {
        super.extractRenderState(elephant, state, partialTick);
        int tick = elephant.getAttackAnimationTick();
        state.attackProgress = tick > 0 ? Math.max(0.0F, (tick - partialTick) / Elephant.ATTACK_ANIMATION_LENGTH) : 0.0F;
    }
}
