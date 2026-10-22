package com.otavi.mobmod.mob.meerkat;

import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.core.MobDefinition;
import com.otavi.mobmod.core.SpawnRule;
import com.otavi.mobmod.core.Translation;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class MeerkatMob {
    public static final TagKey<Item> FOOD = TagKey.create(Registries.ITEM, MobMod.id("meerkat_food"));

    public static final MobDefinition<Meerkat> DEFINITION = MobDefinition.builder("meerkat", Meerkat::new, MobCategory.CREATURE)
            // About 0.6 blocks standing up, like the reference: twice a real meerkat, so it reads well in game.
            // Males are 15% bigger (ModAnimal)
            .type(type -> type.sized(0.4f, 0.6f).eyeHeight(0.5f).clientTrackingRange(8))
            .attributes(Meerkat::createMeerkatAttributes)
            .spawn(SpawnRule.animal(ConventionalBiomeTags.IS_DESERT, 8, 3, 6, Meerkat::checkMeerkatSpawnRules))
            .name("Meerkat", "Suricato")
            .food(FOOD, new Translation("Food for Meerkats", "Comida para Suricatos"), Items.SPIDER_EYE, Items.EGG)
            .spawnEggAfter(Items.MAGMA_CUBE_SPAWN_EGG)
            .build();

    public static final EntityType<Meerkat> TYPE = DEFINITION.type();

    private MeerkatMob() {
    }
}
