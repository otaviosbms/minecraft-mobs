package com.otavi.mobmod.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Generates every JSON derived from {@link com.otavi.mobmod.core.ModMobs}. Run with {@code ./gradlew runDatagen}.
 */
public class MobModDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var pack = generator.createPack();
        pack.addProvider((output, registries) -> new ModLanguageProvider(output, "en_us", registries));
        pack.addProvider((output, registries) -> new ModLanguageProvider(output, "pt_br", registries));
        pack.addProvider(ModEntityLootProvider::new);
        pack.addProvider(ModItemTagProvider::new);
        pack.addProvider(ModModelProvider::new);
    }
}
