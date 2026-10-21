package com.otavi.mobmod.mob.elephant;

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
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public final class ElephantMob {
    public static final TagKey<Item> FOOD = TagKey.create(Registries.ITEM, MobMod.id("elephant_food"));

    public static final MobDefinition<Elephant> DEFINITION = MobDefinition.builder("elephant", Elephant::new, MobCategory.CREATURE)
            // About 3 blocks tall at the shoulder like a real female African elephant; males are 15% bigger (ModAnimal)
            .type(type -> type.sized(2.6f, 3.0f).eyeHeight(2.6f).clientTrackingRange(10))
            .attributes(Elephant::createElephantAttributes)
            .spawn(SpawnRule.animal(BiomeTags.IS_SAVANNA, 5, 2, 4))
            .name("Elephant", "Elefante")
            .food(FOOD, new Translation("Food for Elephants", "Comida para Elefantes"), Items.WHEAT, Items.APPLE, Items.HAY_BLOCK)
            .loot(context -> LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(ContextIntProviders.exactly(1))
                            .add(LootItem.lootTableItem(Items.LEATHER)
                                    .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))
                                    .apply(EnchantedCountIncreaseFunction.lootingMultiplier(context.enchantments(), ContextFloatProviders.between(0.0F, 1.0F))))))
            .spawnEggAfter(Items.ELDER_GUARDIAN_SPAWN_EGG)
            .build();

    public static final EntityType<Elephant> TYPE = DEFINITION.type();

    private ElephantMob() {
    }
}
