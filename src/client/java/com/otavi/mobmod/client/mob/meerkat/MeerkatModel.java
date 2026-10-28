package com.otavi.mobmod.client.mob.meerkat;

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
 * Meerkat like the reference: a big flat-topped head with a dark eye mask and a pale snout, small dark ears,
 * a stocky body with dark bands across the back, short arms, thick haunches with long feet and a thin tail.
 * <p>
 * The mesh is built standing up on the hind legs (the sentinel pose). On all fours the body pitches forward around
 * the hips; head and arms are not children of the body but are placed at the neck and shoulders every frame, so the
 * head stays level and turns around the vertical axis, and the arms reach the ground as front legs.
 * <p>
 * The geometry is built at about four times the usual pixel size and scaled down, like the chimpanzee, so the face
 * can be painted in detail. Males are bigger through the scale attribute.
 * UV layout (128x128) is documented in tools/textures/MeerkatTexture.java.
 */
public class MeerkatModel extends EntityModel<MeerkatRenderState> {
    public static final String RIGHT_EYELID = "right_eyelid";
    public static final String LEFT_EYELID = "left_eyelid";

    public static final int TEXTURE_SIZE = 128;

    /**
     * Brings the 32 px tall standing geometry down to about 0.6 blocks.
     */
    private static final float ADULT_SCALE = 0.3F;

    /**
     * Body pitch on all fours: the chest stays a bit higher than the hips, so the arms reach the ground.
     */
    private static final float WALK_PITCH = 1.18F;
    /**
     * On all fours the body reaches far forward: the whole model moves back to stay centered on the hitbox.
     */
    private static final float WALK_SHIFT = 10.0F;
    private static final float ARM_STAND = -0.35F;
    private static final float TAIL_STAND = -0.55F;
    private static final float TAIL_WALK = 0.35F;
    /**
     * Asleep it lies flat on its belly: the body level with the ground and lowered until the belly touches it.
     */
    private static final float SLEEP_PITCH = Mth.HALF_PI;
    private static final float SLEEP_DROP = 3.5F;
    /**
     * How far the eyelids sit outside the face, so they do not flicker against the painted eyes.
     */
    private static final float EYELID_GAP = 0.05F;

    // Where the head and the arms join the body, from the hips in body space
    private static final float NECK_Y = -15.0F;
    private static final float NECK_Z = -0.5F;
    private static final float SHOULDER_Y = -11.0F;
    private static final float SHOULDER_Z = -4.5F;

    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart rightEyelid;
    private final ModelPart leftEyelid;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public MeerkatModel(ModelPart root) {
        super(root);
        this.body = root.getChild(PartNames.BODY);
        this.tail = this.body.getChild(PartNames.TAIL);
        this.head = root.getChild(PartNames.HEAD);
        this.rightEar = this.head.getChild(PartNames.RIGHT_EAR);
        this.leftEar = this.head.getChild(PartNames.LEFT_EAR);
        this.rightEyelid = this.head.getChild(RIGHT_EYELID);
        this.leftEyelid = this.head.getChild(LEFT_EYELID);
        this.rightArm = root.getChild(PartNames.RIGHT_ARM);
        this.leftArm = root.getChild(PartNames.LEFT_ARM);
        this.rightLeg = root.getChild(PartNames.RIGHT_LEG);
        this.leftLeg = root.getChild(PartNames.LEFT_LEG);
    }

    /**
     * Detailed geometry, standing up: the ground is at y=24 and the hips at y=16.
     */
    private static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body: a stocky column above the hips, the belly facing forward
        PartDefinition body = root.addOrReplaceChild(PartNames.BODY, CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, -15.0F, -4.5F, 12.0F, 15.0F, 9.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
        // Thin tail from the base of the back, lying on the ground behind
        body.addOrReplaceChild(PartNames.TAIL, CubeListBuilder.create()
                .texOffs(0, 60).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F), PartPose.offsetAndRotation(0.0F, -2.0F, 4.0F, TAIL_STAND, 0.0F, 0.0F));

        // Head: big, wide and flat on top, with the snout sticking out of the lower half of the face
        PartDefinition head = root.addOrReplaceChild(PartNames.HEAD, CubeListBuilder.create()
                .texOffs(0, 24).addBox(-7.0F, -9.0F, -8.0F, 14.0F, 9.0F, 12.0F)
                .texOffs(54, 24).addBox(-3.0F, -5.0F, -13.0F, 6.0F, 5.0F, 5.0F), PartPose.offset(0.0F, 16.0F + NECK_Y, NECK_Z));
        // Small round ears on the sides, just behind the eye mask
        head.addOrReplaceChild(PartNames.RIGHT_EAR, CubeListBuilder.create()
                .texOffs(78, 24).addBox(-2.0F, -1.5F, -1.5F, 2.0F, 3.0F, 3.0F), PartPose.offset(-7.0F, -5.5F, 1.0F));
        head.addOrReplaceChild(PartNames.LEFT_EAR, CubeListBuilder.create()
                .texOffs(78, 24).mirror().addBox(0.0F, -1.5F, -1.5F, 2.0F, 3.0F, 3.0F), PartPose.offset(7.0F, -5.5F, 1.0F));
        // Eyelids: flat patches of the mask just outside the painted eyes (2x2 on the sides of the head), shown while asleep
        head.addOrReplaceChild(RIGHT_EYELID, CubeListBuilder.create()
                .texOffs(90, 24).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 2.0F), PartPose.offset(-7.0F - EYELID_GAP, -7.0F, -6.0F));
        head.addOrReplaceChild(LEFT_EYELID, CubeListBuilder.create()
                .texOffs(90, 24).mirror().addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 2.0F), PartPose.offset(7.0F + EYELID_GAP, -7.0F, -6.0F));

        // Short arms in front of the chest: paws held forward when standing, front legs on all fours
        CubeListBuilder arm = CubeListBuilder.create().texOffs(0, 46).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F);
        root.addOrReplaceChild(PartNames.RIGHT_ARM, arm, PartPose.offset(-4.0F, 16.0F + SHOULDER_Y, SHOULDER_Z));
        root.addOrReplaceChild(PartNames.LEFT_ARM, arm, PartPose.offset(4.0F, 16.0F + SHOULDER_Y, SHOULDER_Z));

        // Hind legs: a thick haunch sticking out of the sides of the hips and a long foot pointing forward
        root.addOrReplaceChild(PartNames.RIGHT_LEG, CubeListBuilder.create()
                .texOffs(14, 46).addBox(-2.5F, -1.0F, -3.0F, 5.0F, 6.0F, 6.0F)
                .texOffs(38, 46).addBox(-2.5F, 5.0F, -5.0F, 5.0F, 3.0F, 6.0F), PartPose.offset(-4.0F, 16.0F, 0.0F));
        root.addOrReplaceChild(PartNames.LEFT_LEG, CubeListBuilder.create().mirror()
                .texOffs(14, 46).addBox(-2.5F, -1.0F, -3.0F, 5.0F, 6.0F, 6.0F)
                .texOffs(38, 46).addBox(-2.5F, 5.0F, -5.0F, 5.0F, 3.0F, 6.0F), PartPose.offset(4.0F, 16.0F, 0.0F));

        return mesh;
    }

    public static LayerDefinition createAdultLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_SIZE, TEXTURE_SIZE).apply(MeshTransformer.scaling(ADULT_SCALE));
    }

    public static LayerDefinition createBabyLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_SIZE, TEXTURE_SIZE).apply(MeshTransformer.scaling(ADULT_SCALE * 0.5F));
    }

    @Override
    public void setupAnim(MeerkatRenderState state) {
        // Resets every part to its default (standing) pose: rotations and offsets below are relative to it
        super.setupAnim(state);
        // Fully underground: nothing to draw (from a cave below it would show through the ground)
        this.root().visible = state.burrowDepth < 1.0F;

        float age = state.ageInTicks;
        float stand = state.standAmount;
        float fours = 1.0F - stand;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float still = 1.0F - speed;
        // Offsets are in detailed pixels: the part scale brings them to the size of this layer (adult or baby)
        float px = this.body.yScale;

        // Quick little steps: diagonal limbs move together and the body bobs twice per stride
        float swing = Mth.cos(pos * 1.2F) * speed;
        float bob = Math.abs(Mth.sin(pos * 1.2F)) * speed * fours;

        // Asleep it lies flat on its belly, so the body pitches all the way down and sinks onto the ground
        float sleep = state.sleepAmount;
        float awake = 1.0F - sleep;
        float drop = SLEEP_DROP * sleep * px;
        this.body.xRot = Mth.lerp(sleep, WALK_PITCH * fours, SLEEP_PITCH);
        this.body.y -= bob * 1.5F * px;
        this.body.y += drop;
        this.rightLeg.y += drop;
        this.leftLeg.y += drop;
        // Breathing, faster than a big animal
        this.body.y += Mth.sin(age * 0.15F) * 0.4F * px * still;
        float shift = WALK_SHIFT * fours * px;
        this.body.z += shift;
        this.rightLeg.z += shift;
        this.leftLeg.z += shift;

        // Hind legs stretched out behind when asleep
        this.rightLeg.xRot = Mth.lerp(sleep, -swing * 0.8F * fours, 1.3F);
        this.leftLeg.xRot = Mth.lerp(sleep, swing * 0.8F * fours, 1.3F);

        // Head and arms follow the neck and the shoulders wherever the body pitch puts them
        this.attachToBody(this.head, NECK_Y, NECK_Z, px);
        this.attachToBody(this.rightArm, SHOULDER_Y, SHOULDER_Z, px);
        this.attachToBody(this.leftArm, SHOULDER_Y, SHOULDER_Z, px);

        // Paws held in front of the chest when standing, with a small fidget; legs swinging on all fours
        float paws = ARM_STAND + Mth.sin(age * 0.09F) * 0.05F;
        this.rightArm.xRot = Mth.lerp(stand, swing * 0.9F, paws);
        this.leftArm.xRot = Mth.lerp(stand, -swing * 0.9F, paws);

        // Head follows where the meerkat looks (the sentinel scans around with it)
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = Mth.clamp(state.yRot, -70.0F, 70.0F) * Mth.DEG_TO_RAD;
        this.head.xRot += Mth.sin(pos * 2.4F) * 0.05F * speed * fours;

        // Tail: on the ground behind when standing, held up a bit on all fours, always swaying a little
        float tailAngle = Mth.lerp(stand, TAIL_WALK, TAIL_STAND);
        this.tail.xRot = tailAngle - this.body.xRot;
        this.tail.yRot = Mth.sin(age * 0.12F) * 0.15F + Mth.sin(pos * 1.2F) * 0.2F * speed;

        this.rightEar.zRot = -twitch(age * 0.19F) * 0.4F * awake;
        this.leftEar.zRot = twitch(age * 0.15F + 2.0F) * 0.4F * awake;

        this.animateSleeping(state, sleep, px);

        // Digging: nose down, the front paws scratching fast at the ground in turn
        if (state.digging) {
            float scratch = Mth.sin(age * 1.6F);
            this.rightArm.xRot = -0.6F + scratch * 0.7F;
            this.leftArm.xRot = -0.6F - scratch * 0.7F;
            this.head.xRot = 0.5F;
            this.tail.yRot = Mth.sin(age * 0.8F) * 0.35F;
        }

        // Attack: a quick lunge, snapping the head forward and the paws down onto the target
        if (state.attackProgress > 0.0F) {
            float lunge = Mth.sin(state.attackProgress * Mth.PI);
            this.head.xRot += lunge * 0.4F;
            this.head.z -= lunge * 2.0F * px;
            this.rightArm.xRot -= lunge * 1.0F;
            this.leftArm.xRot -= lunge * 1.0F;
        }
    }

    /**
     * Meerkats sleep sprawled flat on their belly (the body is already lying down, see {@link #setupAnim}): front
     * paws stretched forward along the ground, the chin resting on them, the tail curled round to one side and the
     * eyes closed. In the wild they sleep in their burrows; this pose is for ground too hard to dig.
     */
    private void animateSleeping(MeerkatRenderState state, float sleep, float px) {
        this.rightEyelid.visible = state.eyesClosed();
        this.leftEyelid.visible = state.eyesClosed();
        if (sleep <= 0.0F) {
            return;
        }
        this.rightArm.xRot = Mth.lerp(sleep, this.rightArm.xRot, -1.45F);
        this.leftArm.xRot = Mth.lerp(sleep, this.leftArm.xRot, -1.45F);
        this.rightArm.yRot = sleep * 0.15F;
        this.leftArm.yRot = -sleep * 0.15F;
        this.head.y += sleep * 1.5F * px;
        this.head.xRot = Mth.lerp(sleep, this.head.xRot, 0.25F);
        this.head.zRot = sleep * 0.15F;
        // Draped over the rump down to the ground and curled round to the side
        this.tail.xRot = Mth.lerp(sleep, this.tail.xRot, -0.55F - this.body.xRot);
        this.tail.yRot = Mth.lerp(sleep, this.tail.yRot, 0.9F);
    }

    /**
     * Puts a part at a point of the body (given in body space, from the hips), following the body pitch.
     */
    private void attachToBody(ModelPart part, float y, float z, float px) {
        float cos = Mth.cos(this.body.xRot);
        float sin = Mth.sin(this.body.xRot);
        part.y = this.body.y + (y * cos - z * sin) * px;
        part.z = this.body.z + (y * sin + z * cos) * px;
    }

    /**
     * Short spikes now and then: 0 most of the time, briefly up to 1.
     */
    private static float twitch(float phase) {
        return (float) Math.pow(Math.max(0.0F, Mth.sin(phase)), 12.0);
    }
}
