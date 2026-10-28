package com.otavi.mobmod.client.mob.camel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.otavi.mobmod.client.core.ModMobRenderer;
import net.minecraft.client.model.animal.camel.CamelModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.CamelRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.camel.Camel;

/**
 * Replaces the vanilla camel renderer, reusing all of it: the game's models (with eyelids added), animations, saddle
 * layer and shadow. From {@link ModMobRenderer} it gets the info bar and the sleep data; the skin variant textures
 * are made from the game's camel textures by {@link com.otavi.mobmod.client.core.VanillaVariantTextures}.
 */
public class ModCamelRenderer extends ModMobRenderer<Camel, ModCamelRenderState, CamelModel> {
    /**
     * Same shadow as the vanilla camel.
     */
    private static final float SHADOW_RADIUS = 0.7F;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ModCamelRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ModAdultCamelModel(context.bakeLayer(CamelClient.ADULT_LAYER)),
                new ModBabyCamelModel(context.bakeLayer(CamelClient.BABY_LAYER)),
                SHADOW_RADIUS
        );
        // The vanilla saddle layer is typed for the vanilla render state, which this one extends
        RenderLayer saddle = CamelRenderer.createCamelSaddleLayer(context, (MobRenderer) this,
                EquipmentClientInfo.LayerType.CAMEL_SADDLE, ModelLayers.CAMEL_SADDLE);
        this.addLayer(saddle);
    }

    @Override
    public ModCamelRenderState createRenderState() {
        return new ModCamelRenderState();
    }

    @Override
    public void extractRenderState(Camel camel, ModCamelRenderState state, float partialTick) {
        super.extractRenderState(camel, state, partialTick);
        // Animation states, saddle, riding and dash cooldown, as the vanilla renderer does
        CamelRenderer.extractAdditionalState(camel, state, partialTick);
    }

    @Override
    public Identifier getTextureLocation(ModCamelRenderState state) {
        var spec = state.isBaby ? CamelClient.BABY_SKIN : CamelClient.ADULT_SKIN;
        return spec.texture(state.traits.skinVariant);
    }

    /**
     * The camel sleeps kneeling, the vanilla sitting pose (started by the entity), with its head lowered by the
     * model: nothing to turn here.
     */
    @Override
    protected void applySleepPose(ModCamelRenderState state, PoseStack poseStack, float amount) {
    }
}
