package com.otavi.mobmod.client.mob.camel;

import com.otavi.mobmod.client.core.HasMobTraits;
import com.otavi.mobmod.client.core.VanillaSkinSpec;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.CamelRenderState;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * What the mod adds to the vanilla camel models (adult and calf): two eyelids on the head and the sleeping pose of
 * the head. Everything else (geometry, animations, sitting down) is the game's own model.
 * <p>
 * The camel's eyes are painted on the head, two pixels on the side and, on the adult, one more round the front corner.
 * Each eyelid is a small box laid over them, a hair bigger than its pixels ({@link #EYELID_GAP}) so it covers the eye
 * without flickering; its texture is painted by {@link com.otavi.mobmod.client.core.VanillaVariantTextures}.
 */
final class CamelEyelids {
    static final String RIGHT_EYELID = "right_eyelid";
    static final String LEFT_EYELID = "left_eyelid";
    private static final CubeDeformation EYELID_GAP = new CubeDeformation(0.05F);

    /**
     * Asleep, the camel kneels (the vanilla sitting pose) and lowers its long neck forward.
     */
    private static final float SLEEP_HEAD_PITCH = 0.55F;

    /**
     * Eyelid box: 1x1x2 at the front corner of each side of the head.
     */
    static final VanillaSkinSpec.UvBox ADULT_RIGHT_UV = new VanillaSkinSpec.UvBox(0, 96, 1, 1, 2);
    static final VanillaSkinSpec.UvBox ADULT_LEFT_UV = new VanillaSkinSpec.UvBox(8, 96, 1, 1, 2);
    static final VanillaSkinSpec.UvBox BABY_RIGHT_UV = new VanillaSkinSpec.UvBox(0, 54, 1, 1, 2);
    static final VanillaSkinSpec.UvBox BABY_LEFT_UV = new VanillaSkinSpec.UvBox(8, 54, 1, 1, 2);

    private CamelEyelids() {
    }

    /**
     * Adds both eyelids to the head of a vanilla camel mesh.
     *
     * @param x    side of the head (the right eyelid starts at x, the left one ends at -x)
     * @param y    top of the eyes
     * @param z    front of the eyes
     */
    static void add(MeshDefinition mesh, float x, float y, float z, VanillaSkinSpec.UvBox right, VanillaSkinSpec.UvBox left) {
        PartDefinition head = findHead(mesh.getRoot());
        if (head == null) {
            throw new IllegalStateException("The vanilla camel model has no head part");
        }
        head.addOrReplaceChild(RIGHT_EYELID, CubeListBuilder.create()
                .texOffs(right.u(), right.v()).addBox(x, y, z, 1.0F, 1.0F, 2.0F, EYELID_GAP), PartPose.ZERO);
        head.addOrReplaceChild(LEFT_EYELID, CubeListBuilder.create()
                .texOffs(left.u(), left.v()).mirror().addBox(-x - 1.0F, y, z, 1.0F, 1.0F, 2.0F, EYELID_GAP), PartPose.ZERO);
    }

    @Nullable
    private static PartDefinition findHead(PartDefinition part) {
        for (var child : part.getChildren()) {
            if (child.getKey().equals("head")) {
                return child.getValue();
            }
            var found = findHead(child.getValue());
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * After the vanilla animations: eyes closed and the head lowered while asleep.
     */
    static void animate(ModelPart head, CamelRenderState state) {
        float sleep = state instanceof HasMobTraits traits ? traits.traits().sleepAmount : 0.0F;
        boolean closed = state instanceof HasMobTraits traits && traits.traits().eyesClosed();
        head.getChild(RIGHT_EYELID).visible = closed;
        head.getChild(LEFT_EYELID).visible = closed;
        if (sleep > 0.0F) {
            head.xRot += Mth.sin(sleep * Mth.HALF_PI) * SLEEP_HEAD_PITCH;
        }
    }
}
