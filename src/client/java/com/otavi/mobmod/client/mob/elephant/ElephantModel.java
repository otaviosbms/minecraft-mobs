package com.otavi.mobmod.client.mob.elephant;

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
 * African elephant with real-life proportions: about 3 blocks tall at the shoulder, legs close to half the height,
 * body longer than tall, a trunk that almost reaches the ground and large flat ears on the sides of the head.
 * <p>
 * Adult and calf share this model: the calf layer is the adult one scaled down by half.
 * Males are bigger through the scale attribute and show the long tusks; females show the short ones.
 * Every animation is procedural (no keyframes). UV layout (256x256) is documented in tools/textures/ElephantTexture.java.
 */
public class ElephantModel extends EntityModel<ElephantRenderState> {
    public static final String TRUNK = "trunk";
    public static final String TRUNK_MIDDLE = "trunk_middle";
    public static final String TRUNK_TIP = "trunk_tip";
    public static final String RIGHT_TUSK = "right_tusk";
    public static final String LEFT_TUSK = "left_tusk";
    public static final String RIGHT_LONG_TUSK = "right_long_tusk";
    public static final String LEFT_LONG_TUSK = "left_long_tusk";
    public static final String RIGHT_EYELID = "right_eyelid";
    public static final String LEFT_EYELID = "left_eyelid";

    public static final int TEXTURE_SIZE = 256;

    private static final MeshTransformer CALF_TRANSFORMER = MeshTransformer.scaling(0.5F);

    private static final float TRUNK_PITCH = -0.12F;
    /**
     * How far the eyelids sit outside the face, so they do not flicker against the painted eyes.
     */
    private static final float EYELID_GAP = 0.05F;

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightEyelid;
    private final ModelPart leftEyelid;
    private final ModelPart trunk;
    private final ModelPart trunkMiddle;
    private final ModelPart trunkTip;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart rightTusk;
    private final ModelPart leftTusk;
    private final ModelPart rightLongTusk;
    private final ModelPart leftLongTusk;
    private final ModelPart tail;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public ElephantModel(ModelPart root) {
        super(root);
        this.body = root.getChild(PartNames.BODY);
        this.head = root.getChild(PartNames.HEAD);
        this.rightEyelid = this.head.getChild(RIGHT_EYELID);
        this.leftEyelid = this.head.getChild(LEFT_EYELID);
        this.trunk = this.head.getChild(TRUNK);
        this.trunkMiddle = this.trunk.getChild(TRUNK_MIDDLE);
        this.trunkTip = this.trunkMiddle.getChild(TRUNK_TIP);
        this.rightEar = this.head.getChild(PartNames.RIGHT_EAR);
        this.leftEar = this.head.getChild(PartNames.LEFT_EAR);
        this.rightTusk = this.head.getChild(RIGHT_TUSK);
        this.leftTusk = this.head.getChild(LEFT_TUSK);
        this.rightLongTusk = this.head.getChild(RIGHT_LONG_TUSK);
        this.leftLongTusk = this.head.getChild(LEFT_LONG_TUSK);
        this.tail = root.getChild(PartNames.BODY).getChild(PartNames.TAIL);
        this.rightHindLeg = root.getChild(PartNames.RIGHT_HIND_LEG);
        this.leftHindLeg = root.getChild(PartNames.LEFT_HIND_LEG);
        this.rightFrontLeg = root.getChild(PartNames.RIGHT_FRONT_LEG);
        this.leftFrontLeg = root.getChild(PartNames.LEFT_FRONT_LEG);
    }

    public static LayerDefinition createAdultLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body: 26 wide, 26 tall, 40 long, from y=-22 to y=4 (the ground is y=24)
        PartDefinition body = root.addOrReplaceChild(PartNames.BODY, CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13.0F, -26.0F, -20.0F, 26.0F, 26.0F, 40.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        body.addOrReplaceChild(PartNames.TAIL, CubeListBuilder.create()
                .texOffs(30, 106).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -22.0F, 20.0F, 0.15F, 0.0F, 0.0F));

        // Legs: columns 22 tall, close to half of the shoulder height like a real elephant
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 70).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 22.0F, 10.0F);
        root.addOrReplaceChild(PartNames.RIGHT_HIND_LEG, leg, PartPose.offset(-7.5F, 2.0F, 13.0F));
        root.addOrReplaceChild(PartNames.LEFT_HIND_LEG, leg, PartPose.offset(7.5F, 2.0F, 13.0F));
        root.addOrReplaceChild(PartNames.RIGHT_FRONT_LEG, leg, PartPose.offset(-7.5F, 2.0F, -13.0F));
        root.addOrReplaceChild(PartNames.LEFT_FRONT_LEG, leg, PartPose.offset(7.5F, 2.0F, -13.0F));

        // Head: in front of the body, its top just under the shoulders
        PartDefinition head = root.addOrReplaceChild(PartNames.HEAD, CubeListBuilder.create()
                .texOffs(44, 70).addBox(-9.0F, -10.0F, -12.0F, 18.0F, 16.0F, 12.0F), PartPose.offset(0.0F, -11.0F, -19.0F));

        // Trunk: three segments hanging almost to the ground, slightly forward
        PartDefinition trunk = head.addOrReplaceChild(TRUNK, CubeListBuilder.create()
                .texOffs(104, 70).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 10.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 3.0F, -9.0F, TRUNK_PITCH, 0.0F, 0.0F));
        PartDefinition trunkMiddle = trunk.addOrReplaceChild(TRUNK_MIDDLE, CubeListBuilder.create()
                .texOffs(132, 70).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F), PartPose.offset(0.0F, 9.5F, 0.0F));
        trunkMiddle.addOrReplaceChild(TRUNK_TIP, CubeListBuilder.create()
                .texOffs(156, 70).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.offset(0.0F, 8.5F, 0.0F));

        // Tusks: forward and down from the base of the trunk. Females have short ones, males long ones
        CubeListBuilder tusk = CubeListBuilder.create().texOffs(176, 70).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F);
        head.addOrReplaceChild(RIGHT_TUSK, tusk, PartPose.offsetAndRotation(-4.5F, 4.0F, -10.0F, -0.9F, 0.0F, 0.1F));
        head.addOrReplaceChild(LEFT_TUSK, tusk, PartPose.offsetAndRotation(4.5F, 4.0F, -10.0F, -0.9F, 0.0F, -0.1F));
        CubeListBuilder longTusk = CubeListBuilder.create().texOffs(188, 70).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 13.0F, 2.0F);
        head.addOrReplaceChild(RIGHT_LONG_TUSK, longTusk, PartPose.offsetAndRotation(-4.5F, 4.0F, -10.0F, -0.9F, 0.0F, 0.1F));
        head.addOrReplaceChild(LEFT_LONG_TUSK, longTusk, PartPose.offsetAndRotation(4.5F, 4.0F, -10.0F, -0.9F, 0.0F, -0.1F));

        // Ears: large flat panels behind the eyes, flush with the sides of the body like the reference; they fan outwards
        head.addOrReplaceChild(PartNames.RIGHT_EAR, CubeListBuilder.create()
                .texOffs(0, 106).addBox(-1.0F, 0.0F, 0.0F, 1.0F, 15.0F, 13.0F), PartPose.offset(-13.0F, -9.0F, -5.0F));
        head.addOrReplaceChild(PartNames.LEFT_EAR, CubeListBuilder.create()
                .texOffs(0, 106).mirror().addBox(0.0F, 0.0F, 0.0F, 1.0F, 15.0F, 13.0F), PartPose.offset(13.0F, -9.0F, -5.0F));

        // Eyelids: flat patches of skin just outside the painted eyes (2x1 on the sides of the head), shown while asleep
        head.addOrReplaceChild(RIGHT_EYELID, CubeListBuilder.create()
                .texOffs(200, 70).addBox(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 2.0F), PartPose.offset(-9.0F - EYELID_GAP, -3.0F, -10.0F));
        head.addOrReplaceChild(LEFT_EYELID, CubeListBuilder.create()
                .texOffs(200, 70).mirror().addBox(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 2.0F), PartPose.offset(9.0F + EYELID_GAP, -3.0F, -10.0F));

        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    public static LayerDefinition createCalfLayer() {
        return createAdultLayer().apply(CALF_TRANSFORMER);
    }

    @Override
    public void setupAnim(ElephantRenderState state) {
        // Resets every part to its default pose: rotations below are relative to it
        super.setupAnim(state);

        boolean male = state.sex == Sex.MALE;
        this.rightTusk.visible = !male;
        this.leftTusk.visible = !male;
        this.rightLongTusk.visible = male;
        this.leftLongTusk.visible = male;

        float age = state.ageInTicks;

        // Head follows where the elephant looks, within what a heavy neck allows
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.5F;
        this.head.yRot = Mth.clamp(state.yRot, -40.0F, 40.0F) * Mth.DEG_TO_RAD;

        // Heavy, slow steps: diagonal legs move together, with a smaller swing than for small animals
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        float swing = Mth.cos(pos * 0.5F) * 0.7F * speed;
        this.rightHindLeg.xRot = swing;
        this.leftFrontLeg.xRot = swing;
        this.leftHindLeg.xRot = -swing;
        this.rightFrontLeg.xRot = -swing;

        // Idle: trunk sways along its whole length, ears fan, tail swishes (all of it stops while asleep)
        float sleep = state.sleepAmount;
        float awake = 1.0F - sleep;
        this.trunk.xRot += (Mth.sin(age * 0.05F) * 0.06F + swing * 0.2F) * awake;
        this.trunk.zRot = Mth.cos(age * 0.04F) * 0.05F * awake;
        this.trunkMiddle.xRot = Mth.sin(age * 0.05F + 0.8F) * 0.08F * awake;
        this.trunkTip.xRot = Mth.sin(age * 0.05F + 1.6F) * 0.12F * awake;

        // Ears only fan outwards, so they never go through the body
        float earFlap = Math.abs(Mth.sin(age * 0.08F)) * 0.3F * awake;
        this.rightEar.yRot = -earFlap;
        this.leftEar.yRot = earFlap;

        this.tail.zRot = Mth.sin(age * 0.1F) * 0.2F * awake;

        this.animateSleeping(state, sleep, age);

        // Attack: rear the head and curl the trunk up, then swing back down
        if (state.attackProgress > 0.0F) {
            float lift = Mth.sin(state.attackProgress * Mth.PI);
            this.head.xRot -= lift * 0.5F;
            this.trunk.xRot -= lift * 1.0F;
            this.trunkMiddle.xRot -= lift * 0.7F;
            this.trunkTip.xRot -= lift * 0.6F;
        }
    }

    /**
     * Elephants sleep lying on their side (the renderer rolls the model over): the legs rest a little apart,
     * front ones forward and hind ones back, the trunk curls back towards the chest along the ground, the chin drops
     * and the eyes close. The body swells slowly with deep breaths.
     */
    private void animateSleeping(ElephantRenderState state, float sleep, float age) {
        this.rightEyelid.visible = state.eyesClosed();
        this.leftEyelid.visible = state.eyesClosed();
        if (sleep <= 0.0F) {
            return;
        }
        this.rightFrontLeg.xRot = Mth.lerp(sleep, this.rightFrontLeg.xRot, -0.35F);
        this.leftFrontLeg.xRot = Mth.lerp(sleep, this.leftFrontLeg.xRot, -0.25F);
        this.rightHindLeg.xRot = Mth.lerp(sleep, this.rightHindLeg.xRot, 0.45F);
        this.leftHindLeg.xRot = Mth.lerp(sleep, this.leftHindLeg.xRot, 0.3F);

        this.head.xRot += sleep * 0.25F;
        this.trunk.xRot += sleep * 0.45F;
        this.trunkMiddle.xRot += sleep * 0.7F;
        this.trunkTip.xRot += sleep * 0.9F;
        // Ears lie flat against the head and the tail hangs limp
        this.rightEar.yRot += sleep * 0.05F;
        this.leftEar.yRot -= sleep * 0.05F;
        this.tail.xRot += sleep * 0.2F;

        // Lying on its side the width of the body points up: deep, slow breaths
        this.body.xScale = 1.0F + (Mth.sin(age * 0.05F) * 0.5F + 0.5F) * 0.03F * sleep;
    }
}
