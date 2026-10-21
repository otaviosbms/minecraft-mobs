package com.otavi.mobmod.mob.chimpanzee;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * When its time in the tree is over, the chimpanzee climbs back down along the nearest trunk (or straight down through
 * the foliage if there is none close by) to the ground, without falling.
 */
class ClimbDownTreeGoal extends Goal {
    private static final int TRUNK_SEARCH_RADIUS = 3;
    private static final double DESCEND_SPEED = 0.15;

    private final Chimpanzee chimpanzee;
    @Nullable
    private Vec3 target;
    @Nullable
    private BlockPos trunk;
    private boolean done;

    ClimbDownTreeGoal(Chimpanzee chimpanzee) {
        this.chimpanzee = chimpanzee;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.chimpanzee.isInTree() || this.chimpanzee.isStayingInTree() || this.chimpanzee.isInLove()) {
            return false;
        }
        this.findWayDown();
        return this.target != null;
    }

    @Override
    public void start() {
        this.done = false;
        if (this.target != null) {
            this.chimpanzee.getNavigation().stop();
            this.chimpanzee.startTrunkSlide(this.target, DESCEND_SPEED);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && !this.done;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.trunk != null) {
            this.chimpanzee.faceBlock(this.trunk);
        }
        if (this.chimpanzee.tickTrunkSlide()) {
            this.done = true;
        }
    }

    @Override
    public void stop() {
        if (this.chimpanzee.isSlidingOnTrunk()) {
            this.chimpanzee.stopTrunkSlide();
        }
        this.target = null;
        this.trunk = null;
    }

    /**
     * Picks the free column next to the nearest log below the chimpanzee, or its own column, and the ground under it.
     */
    private void findWayDown() {
        var level = this.chimpanzee.level();
        BlockPos feet = this.chimpanzee.blockPosition();
        BlockPos bestColumn = feet;
        BlockPos bestTrunk = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-TRUNK_SEARCH_RADIUS, -6, -TRUNK_SEARCH_RADIUS), feet.offset(TRUNK_SEARCH_RADIUS, 0, TRUNK_SEARCH_RADIUS))) {
            if (!level.getBlockState(pos).is(BlockTags.LOGS)) {
                continue;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos side = pos.relative(direction);
                if (level.getBlockState(side).is(BlockTags.LOGS)) {
                    continue;
                }
                double distance = side.distSqr(feet);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestColumn = side.immutable();
                    bestTrunk = pos.immutable();
                }
            }
        }
        int groundY = this.chimpanzee.groundYBelow(bestColumn.atY(feet.getY()));
        if (groundY >= this.chimpanzee.getY() - 1) {
            this.target = null;
            return;
        }
        this.trunk = bestTrunk;
        this.target = new Vec3(bestColumn.getX() + 0.5, groundY, bestColumn.getZ() + 0.5);
    }
}
