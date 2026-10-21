package com.otavi.mobmod.entity.genetics;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public enum Sex implements StringRepresentable {
    MALE("male"),
    FEMALE("female");

    public static final Codec<Sex> CODEC = StringRepresentable.fromEnum(Sex::values);

    private final String name;

    Sex(String name) {
        this.name = name;
    }

    public static Sex random(RandomSource random) {
        return random.nextBoolean() ? MALE : FEMALE;
    }

    public String translationKey() {
        return "mobmod.sex." + this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
