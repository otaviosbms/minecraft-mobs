package com.otavi.mobmod.client.datagen;

import com.otavi.mobmod.core.ModMobs;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ModLanguageProvider extends FabricLanguageProvider {
    private final String locale;

    public ModLanguageProvider(FabricPackOutput output, String locale, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, locale, registries);
        this.locale = locale;
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
        for (var mob : ModMobs.ALL) {
            var name = mob.displayName().get(this.locale);
            builder.add(mob.type(), name);
            if (mob.spawnEgg() != null) {
                builder.add(mob.spawnEgg(), spawnEggName(name));
            }
            if (mob.food() != null) {
                builder.add(mob.food().tag(), mob.food().tagName().get(this.locale));
            }
        }
    }

    private String spawnEggName(String mobName) {
        return switch (this.locale) {
            case "pt_br" -> "Ovo Gerador de " + mobName;
            default -> mobName + " Spawn Egg";
        };
    }
}
