package com.otavi.mobmod.gametest;

import com.otavi.mobmod.core.ModMobs;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;

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
}
