package com.otavi.mobmod.mob.chimpanzee;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Chimpanzees go up trees often: walk to the side of a trunk, climb it through the foliage and come out on top of the
 * canopy, where they stay for a while (see {@link ClimbDownTreeGoal}). The edge guard keeps them from falling off.
 */
class ClimbTreeGoal extends Goal {
    /**
     * Average ticks between attempts while a tree is in range (10 s).
     */
    static final int CHANCE = 200;
    private static final int SEARCH_RADIUS = 10;
    /**
     * Only trees whose canopy is at least this many blocks above the ground are worth climbing.
     */
    private static final int MIN_TREE_HEIGHT = 4;
    private static final int APPROACH_TIMEOUT = 300;
    private static final double CLIMB_SPEED = 0.12;

    private final Chimpanzee chimpanzee;
    @Nullable
    private TreeSpot spot;
    private boolean climbing;
    private boolean done;
    private int timeout;

    /**
     * A trunk and the free ground column next to it, under the canopy, that leads up to {@code topY}.
     */
    record TreeSpot(BlockPos trunk, BlockPos side, int topY) {
    }

    ClimbTreeGoal(Chimpanzee chimpanzee) {
        this.chimpanzee = chimpanzee;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.chimpanzee.onGround() || this.chimpanzee.isInTree() || this.chimpanzee.getTarget() != null || this.chimpanzee.isInLove()) {
            return false;
        }
        if (!this.chimpanzee.isForcedToClimb() && this.chimpanzee.getRandom().nextInt(reducedTickDelay(CHANCE)) != 0) {
            return false;
        }
        this.spot = findTree(this.chimpanzee.level(), this.chimpanzee.blockPosition());
        return this.spot != null;
    }

    @Override
    public void start() {
        this.climbing = false;
        this.done = false;
        this.timeout = APPROACH_TIMEOUT;
        this.chimpanzee.clearForcedClimb();
        this.moveToSide();
    }

    @Override
    public boolean canContinueToUse() {
        return this.spot != null && !this.done && (this.climbing || this.timeout > 0);
    }

    /**
     * Once it holds the trunk it finishes the climb, even if something else wants its attention.
     */
    @Override
    public boolean isInterruptable() {
        return !this.climbing;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.spot == null) {
            return;
        }
        Vec3 side = Vec3.atBottomCenterOf(this.spot.side());
        if (this.climbing) {
            this.chimpanzee.faceBlock(this.spot.trunk());
            if (this.chimpanzee.tickTrunkSlide()) {
                this.done = true;
                this.chimpanzee.startTreeStay();
            }
            return;
        }
        this.timeout--;
        double dx = side.x - this.chimpanzee.getX();
        double dz = side.z - this.chimpanzee.getZ();
        if (dx * dx + dz * dz < 0.5 * 0.5) {
            // At the foot of the trunk: grab it and climb through the foliage up to the top of the canopy
            this.climbing = true;
            this.chimpanzee.getNavigation().stop();
            this.chimpanzee.startTrunkSlide(new Vec3(side.x, this.spot.topY(), side.z), CLIMB_SPEED);
        } else if (this.chimpanzee.getNavigation().isDone()) {
            this.moveToSide();
        }
    }

    @Override
    public void stop() {
        if (this.chimpanzee.isSlidingOnTrunk()) {
            this.chimpanzee.stopTrunkSlide();
        }
        this.spot = null;
    }

    private void moveToSide() {
        if (this.spot != null) {
            Vec3 side = Vec3.atBottomCenterOf(this.spot.side());
            this.chimpanzee.getNavigation().moveTo(side.x, side.y, side.z, 1.1);
        }
    }

    /**
     * Nearest trunk base around {@code origin} with a free, standable column next to it that goes up to a canopy.
     */
    @Nullable
    static TreeSpot findTree(Level level, BlockPos origin) {
        TreeSpot best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-SEARCH_RADIUS, -1, -SEARCH_RADIUS), origin.offset(SEARCH_RADIUS, 2, SEARCH_RADIUS))) {
            // Only the lowest log of a trunk
            if (!level.getBlockState(pos).is(BlockTags.LOGS) || level.getBlockState(pos.below()).is(BlockTags.LOGS)) {
                continue;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos side = pos.relative(direction);
                if (!isStandable(level, side)) {
                    continue;
                }
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, side.getX(), side.getZ());
                if (top - side.getY() < MIN_TREE_HEIGHT) {
                    continue;
                }
                double distance = side.distSqr(origin);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new TreeSpot(pos.immutable(), side.immutable(), top);
                }
            }
        }
        return best;
    }

    private static boolean isStandable(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty();
    }
}
