package com.otavi.mobmod.client.mob.armadillo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.otavi.mobmod.client.core.ModMobRenderer;
import net.minecraft.client.model.animal.armadillo.ArmadilloModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

/**
 * Replaces the vanilla armadillo renderer, reusing all of it: the game's models (with eyelids added), animations and
 * shadow. From {@link ModMobRenderer} it gets the info bar and the sleep data; the skin variant textures are made from
 * the game's armadillo textures by {@link com.otavi.mobmod.client.core.VanillaVariantTextures}.
 */
public class ModArmadilloRenderer extends ModMobRenderer<Armadillo, ModArmadilloRenderState, ArmadilloModel> {
    /**
     * Same shadow as the vanilla armadillo.
     */
    private static final float SHADOW_RADIUS = 0.4F;

    public ModArmadilloRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ModAdultArmadilloModel(context.bakeLayer(ArmadilloClient.ADULT_LAYER)),
                new ModBabyArmadilloModel(context.bakeLayer(ArmadilloClient.BABY_LAYER)),
                SHADOW_RADIUS
        );
    }

    @Override
    public ModArmadilloRenderState createRenderState() {
        return new ModArmadilloRenderState();
    }

    @Override
    public void extractRenderState(Armadillo armadillo, ModArmadilloRenderState state, float partialTick) {
        super.extractRenderState(armadillo, state, partialTick);
        // Shell and animation states, as the vanilla renderer does
        state.isHidingInShell = armadillo.shouldHideInShell();
        state.peekAnimationState.copyFrom(armadillo.peekAnimationState);
        state.rollOutAnimationState.copyFrom(armadillo.rollOutAnimationState);
        state.rollUpAnimationState.copyFrom(armadillo.rollUpAnimationState);
    }

    @Override
    public Identifier getTextureLocation(ModArmadilloRenderState state) {
        var spec = state.isBaby ? ArmadilloClient.BABY_SKIN : ArmadilloClient.ADULT_SKIN;
        return spec.texture(state.traits.skinVariant);
    }

    /**
     * The armadillo sleeps rolled up in its shell, the vanilla pose (started by the entity): nothing to turn here.
     */
    @Override
    protected void applySleepPose(ModArmadilloRenderState state, PoseStack poseStack, float amount) {
    }
}
