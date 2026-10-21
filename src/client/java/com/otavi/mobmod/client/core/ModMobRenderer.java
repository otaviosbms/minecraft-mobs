package com.otavi.mobmod.client.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.otavi.mobmod.entity.genetics.GeneticMob;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.Map;

/**
 * Base renderer of every mob of the mod.
 * <ul>
 *     <li>Picks the texture of the skin variant by convention: textures/entity/&lt;mob&gt;/&lt;mob&gt;&lt;suffix&gt;.png</li>
 *     <li>Replaces the vanilla name tag with the {@link MobInfoBar} (name, sex and health)</li>
 * </ul>
 */
public abstract class ModMobRenderer<T extends Mob & GeneticMob, S extends ModMobRenderState, M extends EntityModel<? super S>> extends AgeableMobRenderer<T, S, M> {
    private static final Map<EntityType<?>, Map<SkinVariant, Identifier>> TEXTURES = new HashMap<>();

    protected ModMobRenderer(EntityRendererProvider.Context context, M adultModel, M babyModel, float shadowRadius) {
        super(context, adultModel, babyModel, shadowRadius);
    }

    @Override
    public Identifier getTextureLocation(S state) {
        return TEXTURES.computeIfAbsent(state.entityType, type -> new HashMap<>()).computeIfAbsent(state.skinVariant, variant -> {
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(state.entityType);
            return id.withPath(path -> "textures/entity/" + path + "/" + path + variant.textureSuffix() + ".png");
        });
    }

    @Override
    public void extractRenderState(T mob, S state, float partialTick) {
        super.extractRenderState(mob, state, partialTick);
        state.sex = mob.getSex();
        state.skinVariant = mob.getSkinVariant();
        state.health = mob.getHealth();
        state.maxHealth = mob.getMaxHealth();
        state.displayName = mob.getDisplayName();
        state.showInfoBar = MobInfoBar.shouldShow(state);
    }

    @Override
    protected void submitNameDisplay(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.showInfoBar) {
            MobInfoBar.submit(state, poseStack, collector, camera);
        }
    }
}
