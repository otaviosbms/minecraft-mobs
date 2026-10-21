package com.otavi.mobmod.client.render.entity.model;

import com.otavi.mobmod.client.render.entity.state.ElephantRenderState;
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
 * Adult and calf share this model: the calf layer is the adult one scaled down by half.
 * Every animation is procedural (no keyframes), like Promenade's duck.
 */
public class ElephantModel extends EntityModel<ElephantRenderState> {
    public static final String TRUNK = "trunk";
    public static final String TRUNK_TIP = "trunk_tip";
    public static final String RIGHT_TUSK = "right_tusk";
    public static final String LEFT_TUSK = "left_tusk";

    private static final MeshTransformer CALF_TRANSFORMER = MeshTransformer.scaling(0.5F);

    private final ModelPart head;
    private final ModelPart trunk;
    private final ModelPart trunkTip;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart tail;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public ElephantModel(ModelPart root) {
        super(root);
        this.head = root.getChild(PartNames.HEAD);
        this.trunk = this.head.getChild(TRUNK);
        this.trunkTip = this.trunk.getChild(TRUNK_TIP);
        this.rightEar = this.head.getChild(PartNames.RIGHT_EAR);
        this.leftEar = this.head.getChild(PartNames.LEFT_EAR);
        this.tail = root.getChild(PartNames.BODY).getChild(PartNames.TAIL);
        this.rightHindLeg = root.getChild(PartNames.RIGHT_HIND_LEG);
        this.leftHindLeg = root.getChild(PartNames.LEFT_HIND_LEG);
        this.rightFrontLeg = root.getChild(PartNames.RIGHT_FRONT_LEG);
        this.leftFrontLeg = root.getChild(PartNames.LEFT_FRONT_LEG);
    }

    public static LayerDefinition createAdultLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild(PartNames.BODY, CubeListBuilder.create()
                .texOffs(0, 0).addBox(-10.0F, -10.0F, -16.0F, 20.0F, 22.0F, 32.0F), PartPose.offset(0.0F, 2.0F, 0.0F));
        body.addOrReplaceChild(PartNames.TAIL, CubeListBuilder.create()
                .texOffs(48, 80).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -6.0F, 16.0F, 0.2F, 0.0F, 0.0F));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 56).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 12.0F, 8.0F);
        root.addOrReplaceChild(PartNames.RIGHT_HIND_LEG, leg, PartPose.offset(-6.0F, 12.0F, 11.0F));
        root.addOrReplaceChild(PartNames.LEFT_HIND_LEG, leg, PartPose.offset(6.0F, 12.0F, 11.0F));
        root.addOrReplaceChild(PartNames.RIGHT_FRONT_LEG, leg, PartPose.offset(-6.0F, 12.0F, -11.0F));
        root.addOrReplaceChild(PartNames.LEFT_FRONT_LEG, leg, PartPose.offset(6.0F, 12.0F, -11.0F));

        PartDefinition head = root.addOrReplaceChild(PartNames.HEAD, CubeListBuilder.create()
                .texOffs(32, 56).addBox(-7.0F, -8.0F, -9.0F, 14.0F, 14.0F, 10.0F), PartPose.offset(0.0F, -1.0F, -16.0F));

        PartDefinition trunk = head.addOrReplaceChild(TRUNK, CubeListBuilder.create()
                .texOffs(80, 56).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 9.0F, 4.0F), PartPose.offset(0.0F, 3.0F, -9.0F));
        trunk.addOrReplaceChild(TRUNK_TIP, CubeListBuilder.create()
                .texOffs(96, 56).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F), PartPose.offset(0.0F, 9.0F, -1.0F));

        CubeListBuilder tusk = CubeListBuilder.create().texOffs(108, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F);
        head.addOrReplaceChild(RIGHT_TUSK, tusk, PartPose.offsetAndRotation(-4.0F, 4.0F, -7.0F, -0.6F, 0.0F, 0.0F));
        head.addOrReplaceChild(LEFT_TUSK, tusk, PartPose.offsetAndRotation(4.0F, 4.0F, -7.0F, -0.6F, 0.0F, 0.0F));

        // Ears sweep slightly backwards; they flap around that resting angle
        head.addOrReplaceChild(PartNames.RIGHT_EAR, CubeListBuilder.create()
                .texOffs(0, 80).addBox(-9.0F, -2.0F, 0.0F, 9.0F, 12.0F, 1.0F), PartPose.offsetAndRotation(-7.0F, -4.0F, -4.0F, 0.0F, 0.3F, 0.0F));
        head.addOrReplaceChild(PartNames.LEFT_EAR, CubeListBuilder.create()
                .texOffs(0, 80).mirror().addBox(0.0F, -2.0F, 0.0F, 9.0F, 12.0F, 1.0F), PartPose.offsetAndRotation(7.0F, -4.0F, -4.0F, 0.0F, -0.3F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    public static LayerDefinition createCalfLayer() {
        return createAdultLayer().apply(CALF_TRANSFORMER);
    }

    @Override
    public void setupAnim(ElephantRenderState state) {
        // Resets every part to its default pose: rotations below are relative to it
        super.setupAnim(state);

        float age = state.ageInTicks;

        // Head follows where the elephant looks
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;

        // Heavy, slow steps: diagonal legs move together
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        float swing = Mth.cos(pos * 0.6662F) * 1.0F * speed;
        this.rightHindLeg.xRot = swing;
        this.leftFrontLeg.xRot = swing;
        this.leftHindLeg.xRot = -swing;
        this.rightFrontLeg.xRot = -swing;

        // Idle: trunk sways, ears fan, tail swishes
        this.trunk.xRot = Mth.sin(age * 0.05F) * 0.1F + swing * 0.3F;
        this.trunk.zRot = Mth.cos(age * 0.04F) * 0.08F;
        this.trunkTip.xRot = Mth.sin(age * 0.05F + 1.0F) * 0.15F;

        float earFlap = Mth.sin(age * 0.15F) * 0.15F;
        this.rightEar.yRot += earFlap;
        this.leftEar.yRot -= earFlap;

        this.tail.zRot = Mth.sin(age * 0.1F) * 0.2F;

        // Attack: rear the head and curl the trunk up, then swing back down
        if (state.attackProgress > 0.0F) {
            float lift = Mth.sin(state.attackProgress * Mth.PI);
            this.head.xRot -= lift * 0.6F;
            this.trunk.xRot -= lift * 1.2F;
            this.trunkTip.xRot -= lift * 0.8F;
        }
    }
}
