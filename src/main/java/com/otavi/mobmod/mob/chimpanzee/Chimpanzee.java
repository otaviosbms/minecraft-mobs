package com.otavi.mobmod.mob.chimpanzee;

import com.otavi.mobmod.core.ModItems;
import com.otavi.mobmod.entity.genetics.ModAnimal;
import com.otavi.mobmod.entity.goal.AdultMeleeAttackGoal;
import com.otavi.mobmod.entity.goal.BabyPanicGoal;
import com.otavi.mobmod.item.coconut.CoconutItem;
import com.otavi.mobmod.item.coconut.ThrownCoconut;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A neutral jungle ape: it lives in troops, climbs walls like a spider, goes up trees often to spend time on the
 * canopy ({@link ClimbTreeGoal}, {@link ClimbDownTreeGoal}), now and then throws a coconut at a nearby player
 * ({@link ThrowCoconutGoal}), and the whole troop fights back when one of them gets hurt.
 * <p>
 * It never walks off a ledge higher than {@link #SAFE_DROP} blocks by accident (wandering, following, fleeing),
 * so it does not fall from the treetops it climbs. Only while chasing a target it may jump down.
 */
public class Chimpanzee extends ModAnimal {
    private static final EntityDataAccessor<Boolean> DATA_CLIMBING = SynchedEntityData.defineId(Chimpanzee.class, EntityDataSerializers.BOOLEAN);

    private static final byte ATTACK_EVENT = 4;
    private static final byte HOOT_EVENT = 5;
    private static final byte THROW_EVENT = 6;
    public static final int ATTACK_ANIMATION_LENGTH = 10;
    public static final int HOOT_LENGTH = 20;
    public static final int THROW_LENGTH = 10;

    /**
     * Time spent on the canopy after climbing a tree: 30 to 90 seconds.
     */
    private static final int TREE_STAY_MIN = 600;
    private static final int TREE_STAY_MAX = 1800;
    /**
     * Height above the ground (in blocks, under the leaves) from which the chimpanzee counts as being up a tree.
     */
    private static final int IN_TREE_HEIGHT = 3;
    /**
     * Time between two coconuts thrown by the same chimpanzee: 60 to 120 seconds.
     */
    private static final int THROW_COOLDOWN_MIN = 1200;
    private static final int THROW_COOLDOWN_MAX = 2400;

    /**
     * Highest drop (in blocks) the chimpanzee walks down on its own.
     */
    public static final int SAFE_DROP = 2;
    private static final double EDGE_STEP = 0.05;
    /**
     * How far inside its hitbox the chimpanzee must still have ground under it: a sliver of support at the very edge
     * of the block would not hold it.
     */
    private static final double EDGE_MARGIN = 0.15;

    private int attackAnimationTick;
    private int hootTick;
    private int throwTick;
    private int throwCooldown;
    private int treeStayTicks;
    @Nullable
    private Vec3 slideTarget;
    private double slideSpeed;
    private boolean forceClimb;
    private boolean forceThrow;

    public Chimpanzee(EntityType<? extends Chimpanzee> type, Level level) {
        super(type, level);
    }

    /*========*/
    /*   AI   */
    /*========*/

    public static AttributeSupplier.Builder createChimpanzeeAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AdultMeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(1, new BabyPanicGoal(this, 1.8));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.2, stack -> stack.is(ChimpanzeeMob.FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.2));
        this.goalSelector.addGoal(4, new ClimbDownTreeGoal(this));
        this.goalSelector.addGoal(4, new ThrowCoconutGoal(this));
        this.goalSelector.addGoal(4, new ClimbTreeGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Babies don't fight, but they still call the troop
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ChimpanzeeMob.FOOD);
    }

    /*==============*/
    /*   CLIMBING   */
    /*==============*/

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLIMBING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        // Paths may go up walls and tree trunks, like a spider
        return new WallClimberNavigation(this, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_CLIMBING, this.horizontalCollision || this.isSlidingOnTrunk());
        }
    }

    @Override
    public boolean onClimbable() {
        return this.entityData.get(DATA_CLIMBING);
    }

    /*===========*/
    /*   TREES   */
    /*===========*/

    /**
     * Standing on leaves or logs, with the real ground (under the foliage) at least {@link #IN_TREE_HEIGHT} blocks below.
     */
    public boolean isInTree() {
        if (!this.onGround()) {
            return false;
        }
        var below = this.level().getBlockState(this.blockPosition().below());
        if (!below.is(BlockTags.LEAVES) && !below.is(BlockTags.LOGS)) {
            return false;
        }
        return this.getY() - this.groundYBelow(this.blockPosition()) >= IN_TREE_HEIGHT;
    }

    /**
     * Top of the first solid block under {@code pos} that is not part of a tree (leaves and logs are skipped).
     */
    public int groundYBelow(BlockPos pos) {
        var level = this.level();
        var cursor = pos.mutable();
        while (cursor.getY() > level.getMinY()) {
            cursor.move(0, -1, 0);
            var state = level.getBlockState(cursor);
            if (!state.getCollisionShape(level, cursor).isEmpty() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)) {
                return cursor.getY() + 1;
            }
        }
        return level.getMinY();
    }

    void startTreeStay() {
        this.treeStayTicks = TREE_STAY_MIN + this.random.nextInt(TREE_STAY_MAX - TREE_STAY_MIN);
    }

    boolean isStayingInTree() {
        return this.treeStayTicks > 0;
    }

    /**
     * Climbs straight along a trunk, through the foliage, towards {@code target}: no collision and no gravity
     * while it holds the trunk, so leaves do not block it and it never falls.
     */
    void startTrunkSlide(Vec3 target, double speed) {
        this.slideTarget = target;
        this.slideSpeed = speed;
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /**
     * Moves one tick along the trunk. Returns true once the target is reached and normal physics are back.
     */
    boolean tickTrunkSlide() {
        if (this.slideTarget == null) {
            return true;
        }
        Vec3 position = this.position();
        double dy = this.slideTarget.y - position.y;
        double dx = Mth.clamp(this.slideTarget.x - position.x, -0.1, 0.1);
        double dz = Mth.clamp(this.slideTarget.z - position.z, -0.1, 0.1);
        this.setDeltaMovement(Vec3.ZERO);
        if (Math.abs(dy) <= this.slideSpeed && Math.abs(dx) < 0.1 && Math.abs(dz) < 0.1) {
            this.setPos(this.slideTarget);
            this.stopTrunkSlide();
            return true;
        }
        this.setPos(position.x + dx, position.y + Mth.clamp(dy, -this.slideSpeed, this.slideSpeed), position.z + dz);
        return false;
    }

    void stopTrunkSlide() {
        this.slideTarget = null;
        this.noPhysics = false;
        this.setNoGravity(false);
        this.resetFallDistance();
    }

    public boolean isSlidingOnTrunk() {
        return this.slideTarget != null;
    }

    void faceBlock(BlockPos pos) {
        float yaw = (float) (Mth.atan2(pos.getZ() + 0.5 - this.getZ(), pos.getX() + 0.5 - this.getX()) * Mth.RAD_TO_DEG) - 90.0F;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    /**
     * Makes the next {@link ClimbTreeGoal} check succeed instead of rolling its chance (used by tests).
     */
    public void requestTreeClimb() {
        this.forceClimb = true;
    }

    boolean isForcedToClimb() {
        return this.forceClimb;
    }

    void clearForcedClimb() {
        this.forceClimb = false;
    }

    /**
     * Ends the stay on the canopy, so {@link ClimbDownTreeGoal} starts (used by tests).
     */
    public void endTreeStay() {
        this.treeStayTicks = 0;
    }

    /*==============*/
    /*   COCONUTS   */
    /*==============*/

    boolean canThrowCoconut() {
        return this.throwCooldown <= 0;
    }

    boolean isForcedToThrow() {
        return this.forceThrow;
    }

    /**
     * Makes the next {@link ThrowCoconutGoal} check succeed instead of rolling its chance (used by tests).
     */
    public void requestCoconutThrow() {
        this.forceThrow = true;
        this.throwCooldown = 0;
    }

    /**
     * Throws a coconut at the target with a small arc, like a snow golem throws snowballs.
     */
    public void throwCoconutAt(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        var stack = new ItemStack(ModItems.COCONUT);
        var coconut = new ThrownCoconut(level, this, stack);
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - 1.1 - coconut.getY();
        double dz = target.getZ() - this.getZ();
        double arc = Math.sqrt(dx * dx + dz * dz) * 0.2;
        Projectile.spawnProjectileUsingShoot(coconut, level, stack, dx, dy + arc, dz, CoconutItem.PROJECTILE_SHOOT_POWER, 8.0F);
        this.playSound(SoundEvents.SNOWBALL_THROW, 0.6F, 0.4F / (this.random.nextFloat() * 0.4F + 0.8F));
        level.broadcastEntityEvent(this, THROW_EVENT);
        this.throwTick = THROW_LENGTH;
        this.throwCooldown = THROW_COOLDOWN_MIN + this.random.nextInt(THROW_COOLDOWN_MAX - THROW_COOLDOWN_MIN);
        this.forceThrow = false;
    }

    public int getThrowTick() {
        return this.throwTick;
    }

    /*================*/
    /*   FALL SAFETY  */
    /*================*/

    @Override
    public int getMaxFallDistance() {
        // Paths never plan a drop higher than the edge guard allows, so the chimpanzee never gets stuck following one
        return this.isChasing() ? super.getMaxFallDistance() : SAFE_DROP;
    }

    /**
     * Same idea as a sneaking player: movement that would take the chimpanzee over a ledge higher than
     * {@link #SAFE_DROP} is cut back, one small step at a time on each axis, until it stays on solid ground.
     */
    @Override
    protected Vec3 maybeBackOffFromEdge(Vec3 movement, MoverType type) {
        if (type != MoverType.SELF || movement.y > 0.0 || !this.onGround() || this.onClimbable() || this.isChasing()) {
            return movement;
        }
        // Already hanging over the edge (pushed, knocked back...): never lock it in place, let it move back or fall
        if (this.isAboveDrop(0.0, 0.0)) {
            return movement;
        }
        double dx = movement.x;
        double dz = movement.z;
        while (dx != 0.0 && this.isAboveDrop(dx, 0.0)) {
            dx = stepBack(dx);
        }
        while (dz != 0.0 && this.isAboveDrop(0.0, dz)) {
            dz = stepBack(dz);
        }
        while (dx != 0.0 && dz != 0.0 && this.isAboveDrop(dx, dz)) {
            dx = stepBack(dx);
            dz = stepBack(dz);
        }
        return new Vec3(dx, movement.y, dz);
    }

    private boolean isChasing() {
        return this.getTarget() != null;
    }

    /**
     * True when nothing solid is under the inner part of the moved hitbox within {@link #SAFE_DROP} blocks.
     */
    private boolean isAboveDrop(double dx, double dz) {
        AABB box = this.getBoundingBox();
        double margin = Math.min(EDGE_MARGIN, box.getXsize() / 2.0 - 0.01);
        AABB below = new AABB(box.minX + dx + margin, box.minY - SAFE_DROP - 1.0E-5, box.minZ + dz + margin,
                box.maxX + dx - margin, box.minY, box.maxZ + dz - margin);
        return this.level().noCollision(this, below);
    }

    private static double stepBack(double value) {
        return Math.abs(value) <= EDGE_STEP ? 0.0 : value - EDGE_STEP * Math.signum(value);
    }

    /*============*/
    /*   ATTACK   */
    /*============*/

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.attackAnimationTick = ATTACK_ANIMATION_LENGTH;
        level.broadcastEntityEvent(this, ATTACK_EVENT);
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt) {
            this.playSound(SoundEvents.PANDA_BITE, 1.0F, this.getVoicePitch());
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == ATTACK_EVENT) {
            this.attackAnimationTick = ATTACK_ANIMATION_LENGTH;
        } else if (id == HOOT_EVENT) {
            this.hootTick = HOOT_LENGTH;
        } else if (id == THROW_EVENT) {
            this.throwTick = THROW_LENGTH;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;
        }
        if (this.hootTick > 0) {
            this.hootTick--;
        }
        if (this.throwTick > 0) {
            this.throwTick--;
        }
        if (this.throwCooldown > 0) {
            this.throwCooldown--;
        }
        if (this.treeStayTicks > 0) {
            this.treeStayTicks--;
        }
    }

    public int getAttackAnimationTick() {
        return this.attackAnimationTick;
    }

    public int getHootTick() {
        return this.hootTick;
    }

    /*============*/
    /*   SOUNDS   */
    /*============*/

    @Override
    public void playAmbientSound() {
        super.playAmbientSound();
        // Clients open the mouth and raise the head while the call plays
        if (this.level() instanceof ServerLevel level) {
            level.broadcastEntityEvent(this, HOOT_EVENT);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PANDA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PANDA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PANDA_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.PANDA_STEP, 0.15F, 1.3F);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    public float getVoicePitch() {
        // Higher than the panda it borrows its voice from, closer to a chimpanzee hoot
        return super.getVoicePitch() * 1.35F;
    }
}
