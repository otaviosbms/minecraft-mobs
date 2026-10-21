package com.otavi.mobmod.client.datagen;

import com.otavi.mobmod.core.ModEntityTypes;
import com.otavi.mobmod.core.ModItems;
import com.otavi.mobmod.core.ModMobs;
import com.otavi.mobmod.core.Translation;
import com.otavi.mobmod.entity.genetics.Sex;
import com.otavi.mobmod.entity.genetics.SkinVariant;
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
        builder.add(Sex.MALE.translationKey(), new Translation("Male", "Macho").get(this.locale));
        builder.add(Sex.FEMALE.translationKey(), new Translation("Female", "Fêmea").get(this.locale));
        builder.add(SkinVariant.NORMAL.translationKey(), new Translation("Normal", "Normal").get(this.locale));
        builder.add(SkinVariant.ALBINO.translationKey(), new Translation("Albino", "Albino").get(this.locale));
        builder.add(SkinVariant.MELANISTIC.translationKey(), new Translation("Melanistic", "Melânico").get(this.locale));

        for (var item : ModItems.ALL) {
            builder.add(item.item(), item.name().get(this.locale));
        }
        // The thrown projectile shows up in death messages
        builder.add(ModEntityTypes.COCONUT, new Translation("Coconut", "Coco").get(this.locale));

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
