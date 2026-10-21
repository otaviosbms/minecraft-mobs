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
 * The head has a heavy brow ridge, a pale protruding muzzle and round ears, like the reference.
 * <p>
 * Adult females show a red swelling on the rump. Males are bigger through the scale attribute.
 * Adult and baby share this model: the baby layer is the adult one scaled down by half.
 * UV layout (128x128) is documented in tools/textures/ChimpanzeeTexture.java.
 */
public class ChimpanzeeModel extends EntityModel<ChimpanzeeRenderState> {
    public static final String MUZZLE = "muzzle";
    public static final String SWELLING = "swelling";

    public static final int TEXTURE_SIZE = 128;

    private static final float BODY_PITCH = -0.35F;
    private static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5F);

    private final ModelPart head;
    private final ModelPart swelling;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ChimpanzeeModel(ModelPart root) {
        super(root);
        this.head = root.getChild(PartNames.HEAD);
        this.swelling = root.getChild(PartNames.BODY).getChild(SWELLING);
        this.rightArm = root.getChild(PartNames.RIGHT_ARM);
        this.leftArm = root.getChild(PartNames.LEFT_ARM);
        this.rightLeg = root.getChild(PartNames.RIGHT_LEG);
        this.leftLeg = root.getChild(PartNames.LEFT_LEG);
    }

    public static LayerDefinition createAdultLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body slopes down from the shoulders (front) to the hips (back), like a knuckle-walking ape
        PartDefinition body = root.addOrReplaceChild(PartNames.BODY, CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, -5.0F, -7.0F, 12.0F, 10.0F, 14.0F), PartPose.offsetAndRotation(0.0F, 12.0F, 1.0F, BODY_PITCH, 0.0F, 0.0F));
        body.addOrReplaceChild(SWELLING, CubeListBuilder.create()
                .texOffs(44, 44).addBox(-5.0F, -3.0F, 0.0F, 10.0F, 6.0F, 3.0F), PartPose.offset(0.0F, -1.0F, 7.0F));

        // Long arms: the chimpanzee leans on its knuckles
        CubeListBuilder arm = CubeListBuilder.create().texOffs(0, 44).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 15.0F, 5.0F);
        root.addOrReplaceChild(PartNames.RIGHT_ARM, arm, PartPose.offset(-6.5F, 9.0F, -5.0F));
        root.addOrReplaceChild(PartNames.LEFT_ARM, arm, PartPose.offset(6.5F, 9.0F, -5.0F));

        // Short, bent legs under the hips
        CubeListBuilder leg = CubeListBuilder.create().texOffs(22, 44).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F);
        root.addOrReplaceChild(PartNames.RIGHT_LEG, leg, PartPose.offset(-4.5F, 15.0F, 7.0F));
        root.addOrReplaceChild(PartNames.LEFT_LEG, leg, PartPose.offset(4.5F, 15.0F, 7.0F));

        // Head on top of the shoulders, with the brow ridge in the same part so it moves with the head
        PartDefinition head = root.addOrReplaceChild(PartNames.HEAD, CubeListBuilder.create()
                .texOffs(0, 26).addBox(-4.5F, -6.0F, -7.0F, 9.0F, 8.0F, 7.0F)
                .texOffs(54, 26).addBox(-4.0F, -5.0F, -8.0F, 8.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 7.0F, -5.0F));
        head.addOrReplaceChild(MUZZLE, CubeListBuilder.create()
                .texOffs(34, 26).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 4.0F, 3.0F), PartPose.offset(0.0F, -2.0F, -7.0F));
        head.addOrReplaceChild(PartNames.RIGHT_EAR, CubeListBuilder.create()
                .texOffs(74, 26).addBox(-1.0F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F), PartPose.offset(-4.5F, -3.0F, -3.0F));
        head.addOrReplaceChild(PartNames.LEFT_EAR, CubeListBuilder.create()
                .texOffs(74, 26).mirror().addBox(0.0F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F), PartPose.offset(4.5F, -3.0F, -3.0F));

        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    public static LayerDefinition createBabyLayer() {
        return createAdultLayer().apply(BABY_TRANSFORMER);
    }

    @Override
    public void setupAnim(ChimpanzeeRenderState state) {
        // Resets every part to its default pose: rotations below are relative to it
        super.setupAnim(state);

        // Only adult females show the swelling
        this.swelling.visible = state.sex == Sex.FEMALE && !state.isBaby;

        float age = state.ageInTicks;

        // Head follows where the chimpanzee looks, with a small idle bob
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD + Mth.sin(age * 0.1F) * 0.03F;
        this.head.yRot = Mth.clamp(state.yRot, -60.0F, 60.0F) * Mth.DEG_TO_RAD;

        // Knuckle-walking: diagonal limbs move together, arms swing wider than the short legs
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        float swing = Mth.cos(pos * 0.8F) * speed;
        this.rightArm.xRot = swing * 0.9F;
        this.leftLeg.xRot = swing * 0.7F;
        this.leftArm.xRot = -swing * 0.9F;
        this.rightLeg.xRot = -swing * 0.7F;

        // Climbing: both arms reach up the wall
        if (state.climbing) {
            float reach = Mth.sin(age * 0.4F) * 0.3F;
            this.rightArm.xRot = -2.2F + reach;
            this.leftArm.xRot = -2.2F - reach;
        }

        // Attack: rear up and swing both arms down onto the target
        if (state.attackProgress > 0.0F) {
            float lift = Mth.sin(state.attackProgress * Mth.PI);
            this.rightArm.xRot -= lift * 2.0F;
            this.leftArm.xRot -= lift * 2.0F;
            this.head.xRot -= lift * 0.4F;
        }
    }
}
