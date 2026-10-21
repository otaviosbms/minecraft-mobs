package com.otavi.mobmod.mob.chimpanzee;

import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.core.MobDefinition;
import com.otavi.mobmod.core.SpawnRule;
import com.otavi.mobmod.core.Translation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ChimpanzeeMob {
    public static final TagKey<Item> FOOD = TagKey.create(Registries.ITEM, MobMod.id("chimpanzee_food"));

    public static final MobDefinition<Chimpanzee> DEFINITION = MobDefinition.builder("chimpanzee", Chimpanzee::new, MobCategory.CREATURE)
            // About 1.4 blocks tall walking on its knuckles; males are 15% bigger (ModAnimal)
            .type(type -> type.sized(1.0f, 1.4f).eyeHeight(1.15f).clientTrackingRange(8))
            .attributes(Chimpanzee::createChimpanzeeAttributes)
            .spawn(SpawnRule.animal(BiomeTags.IS_JUNGLE, 8, 3, 6))
            .name("Chimpanzee", "Chimpanzé")
            .food(FOOD, new Translation("Food for Chimpanzees", "Comida para Chimpanzés"),
                    Items.APPLE, Items.MELON_SLICE, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.COCOA_BEANS)
            .spawnEggAfter(Items.CHICKEN_SPAWN_EGG)
            .build();

    public static final EntityType<Chimpanzee> TYPE = DEFINITION.type();

    private ChimpanzeeMob() {
    }
}
