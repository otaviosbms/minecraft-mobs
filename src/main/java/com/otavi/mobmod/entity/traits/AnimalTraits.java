package com.otavi.mobmod.entity.traits;

import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.entity.genetics.GeneticMob;
import com.otavi.mobmod.entity.genetics.Sex;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The shared systems of every {@link ModCreature}, written once so an animal made from scratch and an adapted
 * vanilla animal follow exactly the same rules:
 * <ul>
 *     <li>genetics: a sex (males bigger through the scale attribute), a skin variant, breeding only between a male
 *     and a female, calves with a random sex that inherit the variant of a parent;</li>
 *     <li>regeneration: full health in {@link ModCreature#regenFullHealthSeconds()} seconds, starting
 *     {@link ModCreature#regenDelaySeconds()} seconds after the last damage, out of combat; its food heals it while
 *     injured;</li>
 *     <li>sleep: the synced asleep flag, the smoothed pose amount, waking up when disturbed, snoring and no ambient
 *     calls. When to fall asleep is up to the animal's AI ({@link SleepSchedule}).</li>
 * </ul>
 * The owner calls these hooks from its own methods (a class of the mod, or a mixin on a vanilla class) and defines the
 * three synced values with its own class, as vanilla requires.
 */
public final class AnimalTraits {
    private static final Identifier MALE_SCALE_ID = MobMod.id("male_scale");

    /**
     * Health comes back in steps once per second, so the info bar visibly fills up.
     */
    private static final int REGEN_INTERVAL = 20;
    /**
     * Ticks to lie down, and to get back up.
     */
    public static final int SLEEP_TRANSITION_LENGTH = 20;
    /**
     * It does not fall asleep within 5 seconds of being hurt.
     */
    private static final int SLEEP_AFTER_DAMAGE_TICKS = 100;
    /**
     * A soft snore on average every 6 seconds.
     */
    private static final int SNORE_CHANCE = 120;

    private final Animal owner;
    private final ModCreature creature;
    private final EntityDataAccessor<Byte> sexData;
    private final EntityDataAccessor<Byte> skinVariantData;
    private final EntityDataAccessor<Boolean> asleepData;

    private int ticksSinceDamage = Integer.MAX_VALUE / 2;
    private float sleepAmount;
    private float sleepAmountO;

    /**
     * @param owner the animal itself, which must also implement {@link ModCreature}
     */
    public AnimalTraits(Animal owner, EntityDataAccessor<Byte> sexData, EntityDataAccessor<Byte> skinVariantData, EntityDataAccessor<Boolean> asleepData) {
        this.owner = owner;
        this.creature = (ModCreature) owner;
        this.sexData = sexData;
        this.skinVariantData = skinVariantData;
        this.asleepData = asleepData;
    }

    /**
     * From the owner's {@code defineSynchedData}. Static because vanilla defines synced data before the constructor
     * body (and so before this object) runs.
     */
    public static void defineData(SynchedEntityData.Builder builder, EntityDataAccessor<Byte> sexData,
                                  EntityDataAccessor<Byte> skinVariantData, EntityDataAccessor<Boolean> asleepData) {
        builder.define(sexData, (byte) Sex.FEMALE.ordinal());
        builder.define(skinVariantData, (byte) SkinVariant.NORMAL.ordinal());
        builder.define(asleepData, false);
    }

    /*==============*/
    /*   GENETICS   */
    /*==============*/

    public Sex getSex() {
        return Sex.values()[this.owner.getEntityData().get(this.sexData)];
    }

    public void setSex(Sex sex) {
        this.owner.getEntityData().set(this.sexData, (byte) sex.ordinal());
        var scale = this.owner.getAttribute(Attributes.SCALE);
        if (scale != null) {
            if (sex == Sex.MALE) {
                scale.addOrReplacePermanentModifier(new AttributeModifier(MALE_SCALE_ID, this.creature.maleScaleBonus(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else {
                scale.removeModifier(MALE_SCALE_ID);
            }
        }
    }

    public SkinVariant getSkinVariant() {
        return SkinVariant.values()[this.owner.getEntityData().get(this.skinVariantData)];
    }

    public void setSkinVariant(SkinVariant variant) {
        this.owner.getEntityData().set(this.skinVariantData, (byte) variant.ordinal());
    }

    /**
     * From {@code finalizeSpawn}: a random sex and a natural roll of the variant. Bred calves already got their genes.
     */
    public void onFinalizeSpawn(EntitySpawnReason reason) {
        if (reason != EntitySpawnReason.BREEDING) {
            this.setSex(Sex.random(this.owner.getRandom()));
            this.setSkinVariant(SkinVariant.rollNatural(this.owner.getRandom()));
        }
    }

    /**
     * Added to the owner's {@code canMate}: only a male and a female mate.
     */
    public boolean isOppositeSex(Animal other) {
        return other instanceof GeneticMob mate && mate.getSex() != this.getSex();
    }

    /**
     * Genes of a calf from {@code getBreedOffspring}: a random sex and the variant inherited from the parents.
     */
    public void giveGenesTo(ModCreature calf, Object partner) {
        calf.setSex(Sex.random(this.owner.getRandom()));
        var partnerVariant = partner instanceof GeneticMob mate ? mate.getSkinVariant() : this.getSkinVariant();
        calf.setSkinVariant(SkinVariant.inherit(this.getSkinVariant(), partnerVariant, this.owner.getRandom()));
    }

    /*==========*/
    /*   SAVE   */
    /*==========*/

    public void save(ValueOutput output) {
        output.store("Sex", Sex.CODEC, this.getSex());
        output.store("SkinVariant", SkinVariant.CODEC, this.getSkinVariant());
        output.putInt("TicksSinceDamage", this.ticksSinceDamage);
        output.putBoolean("Asleep", this.owner.getEntityData().get(this.asleepData));
    }

    public void load(ValueInput input) {
        // Animals saved before genetics existed (or before the mod adapted them) get a random sex on first load
        this.setSex(input.read("Sex", Sex.CODEC).orElseGet(() -> Sex.random(this.owner.getRandom())));
        this.setSkinVariant(input.read("SkinVariant", SkinVariant.CODEC).orElse(SkinVariant.NORMAL));
        this.ticksSinceDamage = input.getIntOr("TicksSinceDamage", this.ticksSinceDamage);
        this.owner.getEntityData().set(this.asleepData, input.getBooleanOr("Asleep", false));
    }

    /*==================*/
    /*   REGENERATION   */
    /*==================*/

    /**
     * From {@code actuallyHurt}: restarts the regeneration delay and wakes the animal up.
     */
    public void onHurt() {
        this.ticksSinceDamage = 0;
        this.creature.wakeUp();
    }

    public boolean canRegenerate() {
        return this.owner.isAlive() && this.owner.getHealth() < this.owner.getMaxHealth() && this.owner.getTarget() == null
                && this.ticksSinceDamage >= this.creature.regenDelaySeconds() * 20;
    }

    public void setTicksSinceDamage(int ticks) {
        this.ticksSinceDamage = ticks;
    }

    /**
     * From {@code mobInteract}, before anything else: its food heals an injured animal (one item,
     * {@link ModCreature#foodHealFraction()} of its max health) instead of making it fall in love. Returns true when
     * the food was eaten; the owner then plays its eating sound and returns SUCCESS. At full health it returns false
     * and the food works as in vanilla (breeding, growing up).
     */
    public boolean feedWhileInjured(Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!this.owner.isFood(stack) || this.owner.getHealth() >= this.owner.getMaxHealth()) {
            return false;
        }
        if (this.owner.level() instanceof ServerLevel level) {
            this.owner.heal(this.owner.getMaxHealth() * this.creature.foodHealFraction());
            stack.consume(1, player);
            level.sendParticles(ParticleTypes.HEART, this.owner.getX(), this.owner.getY() + this.owner.getBbHeight(), this.owner.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        }
        return true;
    }

    /*===========*/
    /*   SLEEP   */
    /*===========*/

    public boolean canSleep() {
        return this.canSleep(true);
    }

    /**
     * @param needsGround false for an animal whose sleeping pose changes its hitbox and so lifts it off the ground for
     *                    a tick (the armadillo rolling up)
     */
    public boolean canSleep(boolean needsGround) {
        var owner = this.owner;
        return owner.isAlive() && (owner.onGround() || !needsGround) && !owner.isInWater() && owner.getTarget() == null && !owner.isLeashed()
                && !owner.isInLove() && !owner.isPassenger() && !owner.isVehicle() && this.ticksSinceDamage >= SLEEP_AFTER_DAMAGE_TICKS;
    }

    /**
     * The synced flag only: an animal can count other states as sleep too ({@link ModCreature#isAsleep()}).
     */
    public boolean isAsleep() {
        return this.owner.getEntityData().get(this.asleepData);
    }

    public boolean fallAsleep() {
        if (this.owner.level().isClientSide() || !this.creature.canSleep()) {
            return false;
        }
        this.owner.getNavigation().stop();
        this.owner.getEntityData().set(this.asleepData, true);
        return true;
    }

    public void wakeUp() {
        this.owner.getEntityData().set(this.asleepData, false);
    }

    public float getSleepAmount(float partialTick) {
        return this.sleepAmountO + (this.sleepAmount - this.sleepAmountO) * partialTick;
    }

    /*===========*/
    /*   TICKS   */
    /*===========*/

    /**
     * From {@code baseTick}, before vanilla runs it: asleep, the ambient sound timer never runs out, so no calls in
     * the middle of the night.
     */
    public void beforeBaseTick() {
        if (this.creature.isAsleep()) {
            this.owner.ambientSoundTime = -this.owner.getAmbientSoundInterval();
        }
    }

    /**
     * From {@code tick}, on both sides: smooths the pose between awake and asleep.
     */
    public void tick() {
        this.sleepAmountO = this.sleepAmount;
        float target = this.isAsleep() ? 1.0F : 0.0F;
        this.sleepAmount += Math.clamp(target - this.sleepAmount, -1.0F / SLEEP_TRANSITION_LENGTH, 1.0F / SLEEP_TRANSITION_LENGTH);
    }

    /**
     * From {@code aiStep}, after vanilla, on the server: regeneration, waking up a disturbed sleeper and snoring.
     */
    public void serverAiStep() {
        if (this.ticksSinceDamage < Integer.MAX_VALUE / 2) {
            this.ticksSinceDamage++;
        }
        if (this.owner.tickCount % REGEN_INTERVAL == 0 && this.creature.canRegenerate()) {
            this.owner.heal(this.owner.getMaxHealth() / this.creature.regenFullHealthSeconds() * (REGEN_INTERVAL / 20.0F));
        }
        if (this.isAsleep()) {
            if (!this.creature.canSleep()) {
                // Pushed into water, leashed... also when no AI is watching (right after a world reload)
                this.creature.wakeUp();
            } else if (this.owner.getRandom().nextInt(SNORE_CHANCE) == 0) {
                this.owner.playSound(this.creature.getSleepSound(), 0.3F, this.owner.getVoicePitch() * 0.9F);
            }
        }
    }
}
