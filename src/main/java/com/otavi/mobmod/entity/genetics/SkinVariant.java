package com.otavi.mobmod.entity.genetics;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

/**
 * Skin color variant shared by every mob. Each variant has its own texture: textures/entity/&lt;mob&gt;/&lt;mob&gt;&lt;suffix&gt;.png
 */
public enum SkinVariant implements StringRepresentable {
    NORMAL("normal", ""),
    ALBINO("albino", "_albino"),
    MELANISTIC("melanistic", "_melanistic");

    public static final Codec<SkinVariant> CODEC = StringRepresentable.fromEnum(SkinVariant::values);

    /**
     * Chance of each rare variant on a natural spawn.
     */
    public static final float NATURAL_CHANCE = 0.01F;
    /**
     * Chance of each rare variant appearing in a calf regardless of its parents.
     */
    public static final float MUTATION_CHANCE = 0.01F;

    private final String name;
    private final String textureSuffix;

    SkinVariant(String name, String textureSuffix) {
        this.name = name;
        this.textureSuffix = textureSuffix;
    }

    public static SkinVariant rollNatural(RandomSource random) {
        return rollRare(random, NATURAL_CHANCE, NORMAL);
    }

    /**
     * The calf takes the variant of one of its parents, unless it mutates.
     */
    public static SkinVariant inherit(SkinVariant first, SkinVariant second, RandomSource random) {
        return rollRare(random, MUTATION_CHANCE, random.nextBoolean() ? first : second);
    }

    private static SkinVariant rollRare(RandomSource random, float chance, SkinVariant fallback) {
        float roll = random.nextFloat();
        if (roll < chance) {
            return ALBINO;
        }
        if (roll < chance * 2) {
            return MELANISTIC;
        }
        return fallback;
    }

    public String textureSuffix() {
        return this.textureSuffix;
    }

    public String translationKey() {
        return "mobmod.skin_variant." + this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
