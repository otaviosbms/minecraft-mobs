package com.otavi.mobmod.mob.chimpanzee;

import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.core.MobDefinition;
import com.otavi.mobmod.core.ModItems;
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

public final class ChimpanzeeMob {
    public static final TagKey<Item> FOOD = TagKey.create(Registries.ITEM, MobMod.id("chimpanzee_food"));

    public static final MobDefinition<Chimpanzee> DEFINITION = MobDefinition.builder("chimpanzee", Chimpanzee::new, MobCategory.CREATURE)
            // Real-life size: about 1 block tall walking on its knuckles; males are 15% bigger (ModAnimal)
            .type(type -> type.sized(0.8f, 1.05f).eyeHeight(0.9f).clientTrackingRange(8))
            .attributes(Chimpanzee::createChimpanzeeAttributes)
            .spawn(SpawnRule.animal(BiomeTags.IS_JUNGLE, 8, 3, 6))
            .name("Chimpanzee", "Chimpanzé")
            .food(FOOD, new Translation("Food for Chimpanzees", "Comida para Chimpanzés"),
                    Items.APPLE, Items.MELON_SLICE, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.COCOA_BEANS)
            // Sometimes drops the coconut it was carrying
            .loot(context -> LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(ContextIntProviders.exactly(1))
                            .add(LootItem.lootTableItem(ModItems.COCONUT)
                                    .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 1)))
                                    .apply(EnchantedCountIncreaseFunction.lootingMultiplier(context.enchantments(), ContextFloatProviders.between(0.0F, 1.0F))))))
            .spawnEggAfter(Items.CHICKEN_SPAWN_EGG)
            .build();

    public static final EntityType<Chimpanzee> TYPE = DEFINITION.type();

    private ChimpanzeeMob() {
    }
}
