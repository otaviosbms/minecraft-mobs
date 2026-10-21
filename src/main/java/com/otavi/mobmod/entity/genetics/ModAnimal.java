package com.otavi.mobmod.entity.genetics;

import com.otavi.mobmod.MobMod;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Base class of every animal in the mod: adds a sex and a skin variant.
 * <p>
 * Breeding works exactly like vanilla (food, love mode, cooldown, XP, growing calves) except that
 * only a male and a female can mate. Calves get a random sex and inherit the skin variant of a parent.
 */
public abstract class ModAnimal extends Animal implements GeneticMob {
    private static final EntityDataAccessor<Byte> DATA_SEX = SynchedEntityData.defineId(ModAnimal.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_SKIN_VARIANT = SynchedEntityData.defineId(ModAnimal.class, EntityDataSerializers.BYTE);

    private static final Identifier MALE_SCALE_ID = MobMod.id("male_scale");

    protected ModAnimal(EntityType<? extends ModAnimal> type, Level level) {
        super(type, level);
    }

    /**
     * How much bigger males are than females (0.15 = 15%). Applied through the scale attribute, so the hitbox grows too.
     */
    protected double maleScaleBonus() {
        return 0.15;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SEX, (byte) Sex.FEMALE.ordinal());
        builder.define(DATA_SKIN_VARIANT, (byte) SkinVariant.NORMAL.ordinal());
    }

    @Override
    public Sex getSex() {
        return Sex.values()[this.entityData.get(DATA_SEX)];
    }

    public void setSex(Sex sex) {
        this.entityData.set(DATA_SEX, (byte) sex.ordinal());
        var scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) {
            if (sex == Sex.MALE) {
                scale.addOrReplacePermanentModifier(new AttributeModifier(MALE_SCALE_ID, this.maleScaleBonus(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else {
                scale.removeModifier(MALE_SCALE_ID);
            }
        }
    }

    @Override
    public SkinVariant getSkinVariant() {
        return SkinVariant.values()[this.entityData.get(DATA_SKIN_VARIANT)];
    }

    public void setSkinVariant(SkinVariant variant) {
        this.entityData.set(DATA_SKIN_VARIANT, (byte) variant.ordinal());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Sex", Sex.CODEC, this.getSex());
        output.store("SkinVariant", SkinVariant.CODEC, this.getSkinVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // Animals saved before genetics existed get a random sex on first load
        this.setSex(input.read("Sex", Sex.CODEC).orElseGet(() -> Sex.random(this.random)));
        this.setSkinVariant(input.read("SkinVariant", SkinVariant.CODEC).orElse(SkinVariant.NORMAL));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        // Bred calves already got their genes in getBreedOffspring
        if (reason != EntitySpawnReason.BREEDING) {
            this.setSex(Sex.random(this.random));
            this.setSkinVariant(SkinVariant.rollNatural(this.random));
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean canMate(Animal other) {
        return super.canMate(other) && other instanceof GeneticMob mate && mate.getSex() != this.getSex();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        var calf = (ModAnimal) this.getType().create(level, EntitySpawnReason.BREEDING);
        if (calf != null) {
            calf.setSex(Sex.random(this.random));
            var partnerVariant = partner instanceof GeneticMob mate ? mate.getSkinVariant() : this.getSkinVariant();
            calf.setSkinVariant(SkinVariant.inherit(this.getSkinVariant(), partnerVariant, this.random));
        }
        return calf;
    }
}
