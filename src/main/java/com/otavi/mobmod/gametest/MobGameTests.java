package com.otavi.mobmod.gametest;

import com.otavi.mobmod.core.ModMobs;
import com.otavi.mobmod.entity.genetics.ModAnimal;
import com.otavi.mobmod.entity.genetics.Sex;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Smoke tests that cover every mob in {@link ModMobs#ALL}, so a new mob is tested without writing any test code.
 * Run with {@code ./gradlew runGameTestServer}; only loaded when Fabric's game test system is enabled.
 */
public class MobGameTests {
    @GameTest(maxTicks = 100)
    public void everyMobSpawnsAndSurvives(GameTestHelper helper) {
        var spawned = new ArrayList<Mob>();
        for (var mob : ModMobs.ALL) {
            var entity = helper.spawn(mob.type(), new BlockPos(1, 2, 1));
            helper.assertTrue(entity.getHealth() > 0, mob.name() + " spawned without health");
            spawned.add(entity);
        }
        helper.runAfterDelay(40, () -> {
            for (var entity : spawned) {
                helper.assertTrue(entity.isAlive(), entity.getType().toShortString() + " died right after spawning");
            }
            helper.succeed();
        });
    }

    @GameTest
    public void everyMobWithLootHasALootTable(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        for (var mob : ModMobs.ALL) {
            if (mob.loot() == null) {
                continue;
            }
            var key = mob.type().getDefaultLootTable().orElseThrow();
            var table = server.reloadableRegistries().getLootTable(key);
            helper.assertTrue(table != LootTable.EMPTY, mob.name() + " has no generated loot table, run ./gradlew runDatagen");
        }
        helper.succeed();
    }

    @GameTest
    public void malesAreBiggerThanFemales(GameTestHelper helper) {
        for (var animal : spawnAnimals(helper)) {
            var male = animal[0];
            var female = animal[1];
            male.setSex(Sex.MALE);
            female.setSex(Sex.FEMALE);
            helper.assertTrue(male.getScale() > female.getScale(), male.getType().toShortString() + " males should be bigger than females");
        }
        helper.succeed();
    }

    @GameTest
    public void onlyMaleAndFemaleCanMate(GameTestHelper helper) {
        for (var animal : spawnAnimals(helper)) {
            var first = animal[0];
            var second = animal[1];
            first.setInLove(null);
            second.setInLove(null);

            first.setSex(Sex.MALE);
            second.setSex(Sex.MALE);
            helper.assertTrue(!first.canMate(second), "two " + first.getType().toShortString() + " males should not mate");

            second.setSex(Sex.FEMALE);
            helper.assertTrue(first.canMate(second) && second.canMate(first), first.getType().toShortString() + " male and female should mate");
        }
        helper.succeed();
    }

    @GameTest
    public void calvesInheritRareSkinVariants(GameTestHelper helper) {
        for (var animal : spawnAnimals(helper)) {
            var father = animal[0];
            var mother = animal[1];
            father.setSkinVariant(SkinVariant.ALBINO);
            mother.setSkinVariant(SkinVariant.ALBINO);
            // Two albino parents never have a normal calf: it is albino, or a mutation (albino/melanistic)
            for (int i = 0; i < 20; i++) {
                var calf = (ModAnimal) father.getBreedOffspring(helper.getLevel(), mother);
                helper.assertTrue(calf != null && calf.getSkinVariant() != SkinVariant.NORMAL, father.getType().toShortString() + " calf lost its parents' variant");
            }
        }
        helper.succeed();
    }

    /**
     * Two adults of every animal in {@link ModMobs#ALL}.
     */
    private static List<ModAnimal[]> spawnAnimals(GameTestHelper helper) {
        var pairs = new ArrayList<ModAnimal[]>();
        for (var mob : ModMobs.ALL) {
            var first = helper.spawn(mob.type(), new BlockPos(1, 2, 1));
            var second = helper.spawn(mob.type(), new BlockPos(1, 2, 1));
            if (first instanceof ModAnimal a && second instanceof ModAnimal b) {
                pairs.add(new ModAnimal[]{a, b});
            }
        }
        return pairs;
    }
}
