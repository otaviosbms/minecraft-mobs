package com.otavi.mobmod.client.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import com.otavi.mobmod.entity.traits.ModCreature;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.Map;

/**
 * Base renderer of every animal of the mod, made from scratch or adapted from vanilla (any {@link ModCreature}).
 * <ul>
 *     <li>Picks the texture of the skin variant by convention: textures/entity/&lt;mob&gt;/&lt;mob&gt;&lt;suffix&gt;.png</li>
 *     <li>Replaces the vanilla name tag with the {@link MobInfoBar} (name, sex and health)</li>
 *     <li>Lays an animal that sleeps down on its side, with its head still, so any model gets a sleeping pose
 *     (renderers can pick another one in {@link #applySleepPose})</li>
 * </ul>
 * The render state can be a vanilla one (the camel's) as long as it carries the {@link MobTraitsRenderData}.
 */
public abstract class ModMobRenderer<T extends Mob, S extends LivingEntityRenderState & HasMobTraits, M extends EntityModel<? super S>> extends AgeableMobRenderer<T, S, M> {
    private static final Map<EntityType<?>, Map<SkinVariant, Identifier>> TEXTURES = new HashMap<>();

    /**
     * Asleep, the animal rolls onto its side by this many degrees (not quite 90, so it does not look dead).
     */
    private static final float SLEEP_ROLL = 85.0F;

    protected ModMobRenderer(EntityRendererProvider.Context context, M adultModel, M babyModel, float shadowRadius) {
        super(context, adultModel, babyModel, shadowRadius);
    }

    @Override
    public Identifier getTextureLocation(S state) {
        return TEXTURES.computeIfAbsent(state.entityType, type -> new HashMap<>()).computeIfAbsent(state.traits().skinVariant, variant -> {
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(state.entityType);
            return id.withPath(path -> "textures/entity/" + path + "/" + path + variant.textureSuffix() + ".png");
        });
    }

    @Override
    public void extractRenderState(T mob, S state, float partialTick) {
        super.extractRenderState(mob, state, partialTick);
        var traits = state.traits();
        // Every entity drawn here is a mod creature (vanilla subclasses left as they are, like the camel husk, have
        // their own vanilla renderer)
        var creature = (ModCreature) mob;
        traits.sex = creature.getSex();
        traits.skinVariant = creature.getSkinVariant();
        traits.health = mob.getHealth();
        traits.maxHealth = mob.getMaxHealth();
        traits.displayName = mob.getDisplayName();
        traits.showInfoBar = MobInfoBar.shouldShow(state);

        traits.sleepAmount = creature.getSleepAmount(partialTick);
        if (traits.sleepAmount > 0.0F) {
            // Asleep, the head rests in line with the body and the legs stay still
            float awake = 1.0F - traits.sleepAmount;
            state.yRot *= awake;
            state.xRot *= awake;
            state.walkAnimationSpeed *= awake;
        }
    }

    @Override
    protected void setupRotations(S state, PoseStack poseStack, float bodyRot, float scale) {
        super.setupRotations(state, poseStack, bodyRot, scale);
        float sleep = state.traits().sleepAmount;
        if (sleep > 0.0F) {
            // Eased, so lying down starts and ends softly
            float amount = sleep * sleep * (3.0F - 2.0F * sleep);
            this.applySleepPose(state, poseStack, amount);
        }
    }

    /**
     * Moves the whole model into its sleeping pose; the model itself bends the limbs (from {@code traits.sleepAmount}).
     * By default the animal lies on its side. An animal that sleeps another way (the meerkat on its belly, the camel
     * kneeling) overrides it.
     *
     * @param amount from 0 (awake) to 1 (asleep), eased
     */
    protected void applySleepPose(S state, PoseStack poseStack, float amount) {
        // Rolls around the middle of the body, then sets the side of the body down on the ground.
        // The pose is already scaled by the entity scale here, so sizes are given in unscaled blocks
        float middle = state.boundingBoxHeight * 0.5F / state.scale;
        float lift = this.getSleepingHalfWidth(state) / state.scale;
        poseStack.translate(0.0F, middle + (lift - middle) * amount, 0.0F);
        poseStack.rotateDegrees(Axis.ZP, SLEEP_ROLL * amount);
        poseStack.translate(0.0F, -middle, 0.0F);
    }

    /**
     * Half the width of the body (in blocks): how high the middle of the body sits once the animal lies on its side.
     * By default 30% of the hitbox width, which fits bodies narrower than their hitbox (legs, ears and tusks stick out).
     */
    protected float getSleepingHalfWidth(S state) {
        return state.boundingBoxWidth * 0.3F;
    }

    @Override
    protected void submitNameDisplay(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.traits().showInfoBar) {
            MobInfoBar.submit(state, state.traits(), poseStack, collector, camera);
        }
    }
}
