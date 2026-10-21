package com.otavi.mobmod.entity.genetics;

/**
 * A mob with a sex and a skin variant. Renderers and the info bar only rely on this interface.
 */
public interface GeneticMob {
    Sex getSex();

    SkinVariant getSkinVariant();
}
