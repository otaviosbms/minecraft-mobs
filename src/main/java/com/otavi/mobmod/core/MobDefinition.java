package com.otavi.mobmod.core;

import com.otavi.mobmod.MobMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Everything the mod needs to know about one mob, in one place.
 * <p>
 * {@link Builder#build()} registers the entity type and its spawn egg right away (so they can be stored in
 * {@code static final} fields), while {@link #register()} hooks the rest into the game once {@link ModMobs#init()} runs.
 * Datagen reads the names, food and loot from here, so a mob never has hand-written JSON.
 */
public final class MobDefinition<T extends Mob> {
    private final String name;
    private final EntityType<T> type;
    @Nullable
    private final Item spawnEgg;
    @Nullable
    private final Item spawnEggAfter;
    private final Supplier<AttributeSupplier.Builder> attributes;
    @Nullable
    private final SpawnRule<T> spawnRule;
    private final Translation displayName;
    @Nullable
    private final Food food;
    @Nullable
    private final MobLoot loot;

    private MobDefinition(Builder<T> builder) {
        this.name = builder.name;
        this.attributes = Objects.requireNonNull(builder.attributes, () -> builder.name + " has no attributes");
        this.displayName = Objects.requireNonNull(builder.displayName, () -> builder.name + " has no display name");
        this.spawnRule = builder.spawnRule;
        this.food = builder.food;
        this.loot = builder.loot;
        this.spawnEggAfter = builder.spawnEggAfter;

        var typeKey = ResourceKey.create(Registries.ENTITY_TYPE, MobMod.id(this.name));
        this.type = Registry.register(BuiltInRegistries.ENTITY_TYPE, typeKey, builder.type.build(typeKey));

        if (builder.spawnEgg) {
            var itemKey = ResourceKey.create(Registries.ITEM, MobMod.id(this.name + "_spawn_egg"));
            this.spawnEgg = Registry.register(BuiltInRegistries.ITEM, itemKey,
                    new SpawnEggItem(new Item.Properties().setId(itemKey).spawnEgg(this.type)));
        } else {
            this.spawnEgg = null;
        }
    }

    public static <T extends Mob> Builder<T> builder(String name, EntityType.EntityFactory<T> factory, MobCategory category) {
        return new Builder<>(name, EntityType.Builder.of(factory, category));
    }

    void register() {
        FabricDefaultAttributeRegistry.register(this.type, this.attributes.get());
        if (this.spawnRule != null) {
            // Made accessible by mobmod.accesswidener
            SpawnPlacements.register(this.type, this.spawnRule.placementType(), this.spawnRule.heightmap(), this.spawnRule.predicate());
            BiomeModifications.addSpawn(this.spawnRule.biomes(), this.type.getCategory(), this.type,
                    this.spawnRule.weight(), this.spawnRule.minGroupSize(), this.spawnRule.maxGroupSize());
        }
    }

    public String name() {
        return this.name;
    }

    public EntityType<T> type() {
        return this.type;
    }

    @Nullable
    public Item spawnEgg() {
        return this.spawnEgg;
    }

    /**
     * The item the spawn egg is placed after in the creative tab, or {@code null} to append it at the end.
     */
    @Nullable
    public Item spawnEggAfter() {
        return this.spawnEggAfter;
    }

    public Translation displayName() {
        return this.displayName;
    }

    @Nullable
    public Food food() {
        return this.food;
    }

    @Nullable
    public MobLoot loot() {
        return this.loot;
    }

    /**
     * Items that tempt and breed the mob, generated as an item tag.
     */
    public record Food(TagKey<Item> tag, List<Item> items, Translation tagName) {
    }

    public static final class Builder<T extends Mob> {
        private final String name;
        private EntityType.Builder<T> type;
        private boolean spawnEgg = true;
        @Nullable
        private Item spawnEggAfter;
        @Nullable
        private Supplier<AttributeSupplier.Builder> attributes;
        @Nullable
        private SpawnRule<T> spawnRule;
        @Nullable
        private Translation displayName;
        @Nullable
        private Food food;
        @Nullable
        private MobLoot loot;

        private Builder(String name, EntityType.Builder<T> type) {
            this.name = name;
            this.type = type;
        }

        /**
         * Tweaks the vanilla entity type builder (size, eye height, tracking range...).
         */
        public Builder<T> type(UnaryOperator<EntityType.Builder<T>> configurator) {
            this.type = configurator.apply(this.type);
            return this;
        }

        public Builder<T> attributes(Supplier<AttributeSupplier.Builder> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder<T> spawn(SpawnRule<T> spawnRule) {
            this.spawnRule = spawnRule;
            return this;
        }

        public Builder<T> name(String enUs, String ptBr) {
            this.displayName = new Translation(enUs, ptBr);
            return this;
        }

        public Builder<T> food(TagKey<Item> tag, Translation tagName, Item... items) {
            this.food = new Food(tag, List.of(items), tagName);
            return this;
        }

        public Builder<T> loot(MobLoot loot) {
            this.loot = loot;
            return this;
        }

        public Builder<T> spawnEggAfter(Item item) {
            this.spawnEggAfter = item;
            return this;
        }

        public Builder<T> noSpawnEgg() {
            this.spawnEgg = false;
            return this;
        }

        public MobDefinition<T> build() {
            return new MobDefinition<>(this);
        }
    }
}
