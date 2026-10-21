package com.otavi.mobmod.gametest;

import com.otavi.mobmod.core.ModEntityTypes;
import com.otavi.mobmod.core.ModMobs;
import com.otavi.mobmod.entity.genetics.ModAnimal;
import com.otavi.mobmod.entity.genetics.Sex;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import com.otavi.mobmod.mob.chimpanzee.ChimpanzeeMob;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

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

    @GameTest(maxTicks = 100)
    public void injuredMobsRegenerateOutOfCombat(GameTestHelper helper) {
        var injured = new ArrayList<ModAnimal>();
        for (var animal : spawnAnimals(helper)) {
            var mob = animal[0];
            animal[1].discard();
            mob.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), mob.getMaxHealth() / 2);
            // Skip the delay after the damage
            mob.setTicksSinceDamage(Integer.MAX_VALUE / 2);
            injured.add(mob);
        }
        float[] before = new float[injured.size()];
        for (int i = 0; i < injured.size(); i++) {
            before[i] = injured.get(i).getHealth();
        }
        // Three regeneration steps (one per second)
        helper.runAfterDelay(65, () -> {
            for (int i = 0; i < injured.size(); i++) {
                var mob = injured.get(i);
                float expected = mob.getMaxHealth() / 120.0F * 3.0F;
                helper.assertTrue(mob.getHealth() - before[i] >= expected * 0.99F,
                        mob.getType().toShortString() + " regenerated " + (mob.getHealth() - before[i]) + " instead of " + expected);
            }
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 100)
    public void mobsDoNotRegenerateRightAfterDamage(GameTestHelper helper) {
        var injured = new ArrayList<ModAnimal>();
        for (var animal : spawnAnimals(helper)) {
            animal[1].discard();
            animal[0].hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), animal[0].getMaxHealth() / 2);
            injured.add(animal[0]);
        }
        float[] before = new float[injured.size()];
        for (int i = 0; i < injured.size(); i++) {
            before[i] = injured.get(i).getHealth();
        }
        // 3 s later: still inside the 10 s delay
        helper.runAfterDelay(60, () -> {
            for (int i = 0; i < injured.size(); i++) {
                helper.assertTrue(injured.get(i).getHealth() == before[i], injured.get(i).getType().toShortString() + " regenerated right after being hurt");
            }
            helper.succeed();
        });
    }

    @GameTest
    public void foodHealsInjuredMobs(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (var definition : ModMobs.ALL) {
            if (definition.food() == null || definition.food().items().isEmpty()) {
                continue;
            }
            var mob = helper.spawn(definition.type(), new BlockPos(1, 0, 1));
            if (!(mob instanceof ModAnimal animal)) {
                continue;
            }
            animal.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), animal.getMaxHealth() / 2);
            float before = animal.getHealth();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(definition.food().items().getFirst(), 2));
            animal.mobInteract(player, InteractionHand.MAIN_HAND);
            float expected = animal.getMaxHealth() * 0.1F;
            helper.assertTrue(Math.abs(animal.getHealth() - before - expected) < 0.01F,
                    definition.name() + " healed " + (animal.getHealth() - before) + " instead of " + expected);
            helper.assertTrue(!animal.isInLove(), definition.name() + " fell in love instead of just eating");
            helper.assertTrue(player.getMainHandItem().getCount() == 1, definition.name() + " did not eat exactly one item");
        }
        helper.succeed();
    }

    @GameTest(maxTicks = 100)
    public void chimpanzeeDoesNotWalkOffHighLedges(GameTestHelper helper) {
        // A single block 5 high: walking off it would be a fall far higher than Chimpanzee.SAFE_DROP
        var platform = new BlockPos(3, 5, 3);
        helper.setBlock(platform, Blocks.STONE);
        var chimpanzee = helper.spawn(ChimpanzeeMob.TYPE, platform.above());
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(chimpanzee.onGround(), "chimpanzee should be standing on the platform");
            double y = chimpanzee.getY();
            // Try to walk two blocks off the edge in one go
            chimpanzee.move(MoverType.SELF, new Vec3(2.0, 0.0, 0.0));
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(Math.abs(chimpanzee.getY() - y) < 0.01, "chimpanzee walked off the ledge and fell");
                helper.succeed();
            });
        });
    }

    @GameTest(maxTicks = 600)
    public void chimpanzeeClimbsUpAndDownTrees(GameTestHelper helper) {
        // A small tree: a trunk 5 logs high with two layers of leaves around its top
        for (int y = 0; y < 5; y++) {
            helper.setBlock(new BlockPos(5, y, 5), Blocks.JUNGLE_LOG);
        }
        for (int x = 4; x <= 6; x++) {
            for (int z = 4; z <= 6; z++) {
                for (int y = 4; y <= 5; y++) {
                    if (x != 5 || z != 5 || y == 5) {
                        helper.setBlock(new BlockPos(x, y, z), Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
                    }
                }
            }
        }
        var chimpanzee = helper.spawn(ChimpanzeeMob.TYPE, new BlockPos(1, 0, 5));
        double groundY = chimpanzee.getY();
        chimpanzee.requestTreeClimb();
        // The client plays the climbing animation when the chimpanzee is climbable and off the ground
        boolean[] animated = {false};
        helper.succeedWhen(() -> {
            if (chimpanzee.isSlidingOnTrunk() && chimpanzee.onClimbable() && !chimpanzee.onGround()) {
                animated[0] = true;
            }
            if (chimpanzee.getY() < groundY + 5.5) {
                // Still on its way up: keep waiting
                helper.assertTrue(false, "chimpanzee did not reach the canopy");
            }
            helper.assertTrue(chimpanzee.isInTree(), "chimpanzee is up but not standing on the canopy");
            helper.assertTrue(animated[0], "chimpanzee climbed the trunk without the climbing animation state");
        });
    }

    /**
     * A chimpanzee that climbs a trunk like a spider (walking into it) used to get stuck under the first layer of
     * leaves, right where the trunk meets the canopy.
     */
    @GameTest(maxTicks = 400)
    public void chimpanzeeDoesNotGetStuckUnderTheCanopy(GameTestHelper helper) {
        buildTree(helper);
        // Right next to the trunk, under the leaves, pushing towards the top of the trunk
        var chimpanzee = helper.spawn(ChimpanzeeMob.TYPE, new BlockPos(5, 0, 4));
        var trunkTop = helper.absoluteVec(new Vec3(5.5, 6.0, 5.5));
        helper.succeedWhen(() -> {
            if (!chimpanzee.isInTree()) {
                chimpanzee.getNavigation().moveTo(trunkTop.x, trunkTop.y, trunkTop.z, 1.0);
            }
            helper.assertTrue(chimpanzee.isInTree(), "chimpanzee is stuck at " + chimpanzee.position().subtract(helper.absoluteVec(Vec3.ZERO)));
        });
    }

    /**
     * The trunk slide is not saved: a chimpanzee reloaded in the middle of the foliage must still get out on top.
     */
    @GameTest(maxTicks = 200)
    public void chimpanzeeInsideTheFoliageClimbsOut(GameTestHelper helper) {
        buildTree(helper);
        var chimpanzee = helper.spawn(ChimpanzeeMob.TYPE, new Vec3(4.5, 4.2, 4.5));
        helper.succeedWhen(() -> helper.assertTrue(chimpanzee.isInTree(), "chimpanzee is stuck at " + chimpanzee.position().subtract(helper.absoluteVec(Vec3.ZERO))));
    }

    /**
     * Jungle-like tree: a trunk 5 logs high, with two layers of leaves around its top that overhang the trunk sides.
     */
    private static void buildTree(GameTestHelper helper) {
        for (int y = 0; y < 5; y++) {
            helper.setBlock(new BlockPos(5, y, 5), Blocks.JUNGLE_LOG);
        }
        for (int x = 4; x <= 6; x++) {
            for (int z = 4; z <= 6; z++) {
                for (int y = 4; y <= 5; y++) {
                    if (x != 5 || z != 5 || y == 5) {
                        helper.setBlock(new BlockPos(x, y, z), Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
                    }
                }
            }
        }
    }

    @GameTest(maxTicks = 400)
    public void chimpanzeeClimbsDownWhenItsStayEnds(GameTestHelper helper) {
        for (int y = 0; y < 5; y++) {
            helper.setBlock(new BlockPos(5, y, 5), Blocks.JUNGLE_LOG);
        }
        for (int x = 4; x <= 6; x++) {
            for (int z = 4; z <= 6; z++) {
                helper.setBlock(new BlockPos(x, 5, z), Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
            }
        }
        // On top of the canopy, with its stay already over
        var chimpanzee = helper.spawn(ChimpanzeeMob.TYPE, new BlockPos(4, 6, 5));
        chimpanzee.endTreeStay();
        helper.succeedWhen(() -> {
            helper.assertTrue(chimpanzee.onGround() && chimpanzee.getY() < helper.absoluteVec(Vec3.ZERO).y + 1.0, "chimpanzee is still up the tree");
            helper.assertTrue(chimpanzee.getHealth() == chimpanzee.getMaxHealth(), "chimpanzee got hurt climbing down");
        });
    }

    @GameTest
    public void chimpanzeeThrowsFecesAtPlayers(GameTestHelper helper) {
        var chimpanzee = helper.spawn(ChimpanzeeMob.TYPE, new BlockPos(1, 0, 1));
        var target = helper.spawn(EntityTypes.PIG, new BlockPos(6, 0, 6));
        chimpanzee.throwFecesAt(target);
        helper.assertEntityPresent(ModEntityTypes.FECES);
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
