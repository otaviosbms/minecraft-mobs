package com.otavi.mobmod.client.mob.chimpanzee;

import com.otavi.mobmod.entity.genetics.Sex;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Chimpanzee walking on its knuckles: long arms, short legs and a body sloping down from the shoulders to the hips.
 * The head has a heavy brow ridge, a pale protruding snout with a jaw that opens, and round ears, like the reference.
 * <p>
 * The geometry is built at twice the usual pixel size and then scaled down to real-life size (about 1 block tall),
 * so the texture has twice the detail per block, close to the detail level of the elephant.
 * <p>
 * Adult females show a red swelling on the rump. Males are bigger through the scale attribute.
 * UV layout (256x256) is documented in tools/textures/ChimpanzeeTexture.java.
 */
public class ChimpanzeeModel extends EntityModel<ChimpanzeeRenderState> {
    public static final String SNOUT = "snout";
    public static final String JAW = "jaw";
    public static final String SWELLING = "swelling";
    public static final String RIGHT_EYELID = "right_eyelid";
    public static final String LEFT_EYELID = "left_eyelid";

    public static final int TEXTURE_SIZE = 256;

    /**
     * Brings the double-detail geometry (46 px tall) down to about 1.05 blocks, the height of a knuckle-walking chimpanzee.
     */
    private static final float ADULT_SCALE = 0.37F;
    private static final float BODY_PITCH = -0.35F;
    private static final float CLIMBING_BODY_PITCH = -1.0F;
    /**
     * Asleep, curled up: the back rounds forward a little instead of sloping up to the shoulders.
     */
    private static final float SLEEPING_BODY_PITCH = 0.2F;
    /**
     * How far the eyelids sit in front of the face, so they do not flicker against the painted eyes.
     */
    private static final float EYELID_GAP = 0.05F;

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightEyelid;
    private final ModelPart leftEyelid;
    private final ModelPart jaw;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart swelling;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ChimpanzeeModel(ModelPart root) {
        super(root);
        this.body = root.getChild(PartNames.BODY);
        this.head = root.getChild(PartNames.HEAD);
        this.jaw = this.head.getChild(JAW);
        this.rightEyelid = this.head.getChild(RIGHT_EYELID);
        this.leftEyelid = this.head.getChild(LEFT_EYELID);
        this.rightEar = this.head.getChild(PartNames.RIGHT_EAR);
        this.leftEar = this.head.getChild(PartNames.LEFT_EAR);
        this.swelling = this.body.getChild(SWELLING);
        this.rightArm = root.getChild(PartNames.RIGHT_ARM);
        this.leftArm = root.getChild(PartNames.LEFT_ARM);
        this.rightLeg = root.getChild(PartNames.RIGHT_LEG);
        this.leftLeg = root.getChild(PartNames.LEFT_LEG);
    }

    /**
     * Double-detail geometry: the ground is at y=24 and every size is twice what it would be at normal detail.
     */
    private static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body slopes down from the shoulders (front) to the hips (back), like a knuckle-walking ape
        PartDefinition body = root.addOrReplaceChild(PartNames.BODY, CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12.0F, -10.0F, -14.0F, 24.0F, 20.0F, 28.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, BODY_PITCH, 0.0F, 0.0F));
        body.addOrReplaceChild(SWELLING, CubeListBuilder.create()
                .texOffs(0, 140).addBox(-10.0F, -6.0F, 0.0F, 20.0F, 12.0F, 6.0F), PartPose.offset(0.0F, -2.0F, 14.0F));

        // Long arms: the chimpanzee leans on its knuckles
        CubeListBuilder arm = CubeListBuilder.create().texOffs(0, 86).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 30.0F, 10.0F);
        root.addOrReplaceChild(PartNames.RIGHT_ARM, arm, PartPose.offset(-13.0F, -6.0F, -10.0F));
        root.addOrReplaceChild(PartNames.LEFT_ARM, arm, PartPose.offset(13.0F, -6.0F, -10.0F));

        // Short, bent legs under the hips
        CubeListBuilder leg = CubeListBuilder.create().texOffs(44, 86).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 18.0F, 10.0F);
        root.addOrReplaceChild(PartNames.RIGHT_LEG, leg, PartPose.offset(-9.0F, 6.0F, 14.0F));
        root.addOrReplaceChild(PartNames.LEFT_LEG, leg, PartPose.offset(9.0F, 6.0F, 14.0F));

        // Head on top of the shoulders, with the brow ridge in the same part so it moves with the head
        PartDefinition head = root.addOrReplaceChild(PartNames.HEAD, CubeListBuilder.create()
                .texOffs(0, 52).addBox(-9.0F, -12.0F, -14.0F, 18.0F, 16.0F, 14.0F)
                .texOffs(68, 52).addBox(-8.0F, -10.0F, -16.0F, 16.0F, 4.0F, 2.0F), PartPose.offset(0.0F, -10.0F, -10.0F));
        // Snout and a jaw hinged at its back, so the mouth can open
        head.addOrReplaceChild(SNOUT, CubeListBuilder.create()
                .texOffs(68, 60).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 5.0F, 6.0F), PartPose.offset(0.0F, -4.0F, -14.0F));
        head.addOrReplaceChild(JAW, CubeListBuilder.create()
                .texOffs(68, 74).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 3.0F, 6.0F), PartPose.offset(0.0F, 1.0F, -14.0F));
        head.addOrReplaceChild(PartNames.RIGHT_EAR, CubeListBuilder.create()
                .texOffs(108, 52).addBox(-2.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F), PartPose.offset(-9.0F, -6.0F, -6.0F));
        head.addOrReplaceChild(PartNames.LEFT_EAR, CubeListBuilder.create()
                .texOffs(108, 52).mirror().addBox(0.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F), PartPose.offset(9.0F, -6.0F, -6.0F));
        // Eyelids: flat patches of skin just in front of the painted eyes (4x2 under the brow ridge), shown while asleep
        head.addOrReplaceChild(RIGHT_EYELID, CubeListBuilder.create()
                .texOffs(132, 52).addBox(0.0F, 0.0F, 0.0F, 4.0F, 2.0F, 0.0F), PartPose.offset(-6.0F, -6.0F, -14.0F - EYELID_GAP));
        head.addOrReplaceChild(LEFT_EYELID, CubeListBuilder.create()
                .texOffs(132, 52).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 2.0F, 0.0F), PartPose.offset(2.0F, -6.0F, -14.0F - EYELID_GAP));

        return mesh;
    }

    public static LayerDefinition createAdultLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_SIZE, TEXTURE_SIZE).apply(MeshTransformer.scaling(ADULT_SCALE));
    }

    public static LayerDefinition createBabyLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_SIZE, TEXTURE_SIZE).apply(MeshTransformer.scaling(ADULT_SCALE * 0.5F));
    }

    @Override
    public void setupAnim(ChimpanzeeRenderState state) {
        // Resets every part to its default pose: rotations and offsets below are relative to it
        super.setupAnim(state);

        // Only adult females show the swelling
        this.swelling.visible = state.sex == Sex.FEMALE && !state.isBaby;

        float age = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float still = 1.0F - speed;
        // Offsets are in double-detail pixels: the part scale brings them to the size of this layer (adult or baby)
        float px = this.body.yScale;

        // Head follows where the chimpanzee looks
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = Mth.clamp(state.yRot, -60.0F, 60.0F) * Mth.DEG_TO_RAD;

        if (state.climbing) {
            this.animateClimbing(age);
        } else {
            this.animateWalking(pos, speed, px);
        }

        // Idle: breathing, a curious head tilt and ears twitching now and then
        float breath = Mth.sin(age * 0.08F) * 0.6F * px * still;
        this.body.y += breath;
        this.head.y += breath * 0.5F;
        this.head.zRot = Mth.sin(age * 0.03F) * 0.1F * still;
        this.rightEar.zRot = -twitch(age * 0.17F) * 0.35F;
        this.leftEar.zRot = twitch(age * 0.13F + 2.0F) * 0.35F;

        this.animateSleeping(state, px);

        // Hoot: raise the head and open the mouth while calling
        if (state.hootProgress > 0.0F) {
            float call = Mth.sin(state.hootProgress * Mth.PI);
            this.jaw.xRot = call * 0.7F;
            this.head.xRot -= call * 0.5F;
        }

        // Throw: the right arm goes back over the shoulder and whips forward, mouth open with excitement
        if (state.throwProgress > 0.0F) {
            float t = 1.0F - state.throwProgress;
            this.rightArm.xRot = t < 0.4F ? -3.0F * (t / 0.4F) : -3.0F + 3.4F * ((t - 0.4F) / 0.6F);
            this.rightArm.zRot = 0.2F;
            this.jaw.xRot = Math.max(this.jaw.xRot, Mth.sin(state.throwProgress * Mth.PI) * 0.4F);
        }

        // Attack: rear up and swing both arms down onto the target, mouth open
        if (state.attackProgress > 0.0F) {
            float lift = Mth.sin(state.attackProgress * Mth.PI);
            this.rightArm.xRot -= lift * 2.0F;
            this.leftArm.xRot -= lift * 2.0F;
            this.head.xRot -= lift * 0.4F;
            this.jaw.xRot = Math.max(this.jaw.xRot, lift * 0.5F);
        }
    }

    /**
     * Knuckle-walking: diagonal limbs move together, the body bobs and rolls with each step and the head counters it.
     */
    private void animateWalking(float pos, float speed, float px) {
        float swing = Mth.cos(pos * 0.8F) * speed;
        this.rightArm.xRot = swing * 0.9F;
        this.leftLeg.xRot = swing * 0.7F;
        this.leftArm.xRot = -swing * 0.9F;
        this.rightLeg.xRot = -swing * 0.7F;

        // Two bobs per stride: the body rises when a pair of limbs passes under it
        float bob = Math.abs(Mth.sin(pos * 0.8F)) * speed * px;
        this.body.y -= bob * 2.0F;
        this.head.y -= bob * 1.4F;
        this.body.zRot = Mth.sin(pos * 0.8F) * 0.06F * speed;
        this.head.xRot += Mth.sin(pos * 1.6F) * 0.05F * speed;
    }

    /**
     * Climbing: the body turns upright against the wall, arms reach up in turn while the opposite leg pushes.
     */
    private void animateClimbing(float age) {
        float cycle = Mth.sin(age * 0.4F);
        this.body.xRot = CLIMBING_BODY_PITCH;
        this.rightArm.xRot = -2.4F + cycle * 0.35F;
        this.leftArm.xRot = -2.4F - cycle * 0.35F;
        this.rightLeg.xRot = -0.9F - cycle * 0.5F;
        this.leftLeg.xRot = -0.9F + cycle * 0.5F;
        this.rightArm.zRot = 0.15F;
        this.leftArm.zRot = -0.15F;
        // Looks up the wall
        this.head.xRot -= 0.6F;
    }

    /**
     * Chimpanzees sleep curled up on their side, like in their nests (the renderer rolls the model over): knees pulled
     * up to the chest, arms hugged in front of them, head tucked down and eyes closed. The breathing slows down.
     */
    private void animateSleeping(ChimpanzeeRenderState state, float px) {
        this.rightEyelid.visible = state.eyesClosed();
        this.leftEyelid.visible = state.eyesClosed();
        float sleep = state.sleepAmount;
        if (sleep <= 0.0F) {
            return;
        }
        float awake = 1.0F - sleep;
        this.body.xRot = Mth.lerp(sleep, this.body.xRot, SLEEPING_BODY_PITCH);
        this.rightLeg.xRot = Mth.lerp(sleep, this.rightLeg.xRot, -1.35F);
        this.leftLeg.xRot = Mth.lerp(sleep, this.leftLeg.xRot, -1.2F);
        this.rightArm.xRot = Mth.lerp(sleep, this.rightArm.xRot, -0.95F);
        this.leftArm.xRot = Mth.lerp(sleep, this.leftArm.xRot, -0.8F);
        // Turned inwards, so the hands meet in front of the chest (outwards, the upper arm would point at the sky)
        this.rightArm.yRot = -sleep * 0.6F;
        this.leftArm.yRot = sleep * 0.55F;
        // The curled back brings the shoulders, and the head with them, down towards the knees
        this.head.y += sleep * 3.0F * px;
        this.head.xRot += sleep * 0.6F;
        this.head.zRot *= awake;
        this.jaw.xRot *= awake;
        this.rightEar.zRot *= awake;
        this.leftEar.zRot *= awake;
    }

    /**
     * Short spikes now and then: 0 most of the time, briefly up to 1.
     */
    private static float twitch(float phase) {
        return (float) Math.pow(Math.max(0.0F, Mth.sin(phase)), 12.0);
    }
}
