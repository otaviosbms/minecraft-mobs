package com.otavi.mobmod.client.core;

import com.mojang.blaze3d.platform.NativeImage;
import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Skin variants of the adapted vanilla animals, made at runtime from the game's own textures: nothing of the game is
 * copied into the mod, and a resource pack that changes the vanilla texture changes the variants too.
 * <p>
 * On every resource reload, each {@link VanillaSkinSpec} reads its vanilla texture and, for each {@link SkinVariant}:
 * <ul>
 *     <li>recolors it: the normal variant keeps the game's colors; the albino fades to a pale cream with red eyes;
 *     the melanistic one turns dark brown. The shading of the original is kept, so every detail stays;</li>
 *     <li>paints the eyelids of the model in free space of the texture, with the fur color next to the eyes and a
 *     darker crease where the eye was;</li>
 *     <li>registers the result as a dynamic texture, named by {@link VanillaSkinSpec#texture}.</li>
 * </ul>
 */
public final class VanillaVariantTextures {
    private static final Logger LOGGER = LoggerFactory.getLogger("mobmod");
    private static final List<VanillaSkinSpec> SPECS = new ArrayList<>();

    private static final int ALBINO_DARK = 0xD3B7AA;
    private static final int ALBINO_LIGHT = 0xFFF6EC;
    private static final int ALBINO_EYE_DARK = 0x7A1A22;
    private static final int ALBINO_EYE_LIGHT = 0xC8424E;
    private static final int MELANISTIC_DARK = 0x16110D;
    private static final int MELANISTIC_LIGHT = 0x5C4636;
    private static final int MELANISTIC_EYE = 0x0A0706;
    /**
     * The crease of a closed eye is the eyelid color darkened by this much: light, since vanilla eyes are thin dark
     * lines and the closed eye has to read as fur.
     */
    private static final float CREASE = 0.15F;

    private VanillaVariantTextures() {
    }

    public static void register(VanillaSkinSpec spec) {
        SPECS.add(spec);
    }

    /**
     * Regenerates every variant after each resource reload, once the vanilla textures are loaded.
     */
    public static void init() {
        var id = MobMod.id("vanilla_variant_textures");
        var loader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
        loader.registerReloadListener(id, (ResourceManagerReloadListener) VanillaVariantTextures::reload);
        loader.addListenerOrdering(ResourceReloaderKeys.Client.TEXTURES, id);
    }

    private static void reload(ResourceManager manager) {
        var textures = Minecraft.getInstance().getTextureManager();
        for (var spec : SPECS) {
            var resource = manager.getResource(spec.source());
            if (resource.isEmpty()) {
                LOGGER.warn("Vanilla texture {} not found, its variants will be missing", spec.source());
                continue;
            }
            try (var stream = resource.get().open(); var original = NativeImage.read(stream)) {
                for (var variant : SkinVariant.values()) {
                    var image = new NativeImage(original.getWidth(), original.getHeight(), true);
                    image.copyFrom(original);
                    recolor(image, spec, variant);
                    paintEyelids(image, spec);
                    var name = spec.texture(variant);
                    textures.register(name, new DynamicTexture(name::toString, image));
                }
            } catch (IOException e) {
                LOGGER.error("Could not read vanilla texture {}", spec.source(), e);
            }
        }
    }

    /*=============*/
    /*   RECOLOR   */
    /*=============*/

    private static void recolor(NativeImage image, VanillaSkinSpec spec, SkinVariant variant) {
        if (variant == SkinVariant.NORMAL) {
            return;
        }
        var eyes = new HashSet<>(spec.eyes());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int argb = image.getPixel(x, y);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                float light = lightness(argb);
                boolean eye = eyes.contains(new VanillaSkinSpec.Pixel(x, y));
                int rgb = switch (variant) {
                    case ALBINO -> eye ? lerp(ALBINO_EYE_DARK, ALBINO_EYE_LIGHT, light) : lerp(ALBINO_DARK, ALBINO_LIGHT, light);
                    case MELANISTIC -> eye ? MELANISTIC_EYE : lerp(MELANISTIC_DARK, MELANISTIC_LIGHT, light);
                    case NORMAL -> argb;
                };
                image.setPixel(x, y, (argb & 0xFF000000) | (rgb & 0xFFFFFF));
            }
        }
    }

    /**
     * Perceived lightness from 0 to 1, stretched over the range a fur texture uses, so the variants keep its contrast.
     */
    private static float lightness(int argb) {
        float l = (0.299F * (argb >> 16 & 0xFF) + 0.587F * (argb >> 8 & 0xFF) + 0.114F * (argb & 0xFF)) / 255.0F;
        return Mth.clamp((l - 0.2F) / 0.65F, 0.0F, 1.0F);
    }

    private static int lerp(int from, int to, float t) {
        int r = (int) Mth.lerp(t, from >> 16 & 0xFF, to >> 16 & 0xFF);
        int g = (int) Mth.lerp(t, from >> 8 & 0xFF, to >> 8 & 0xFF);
        int b = (int) Mth.lerp(t, from & 0xFF, to & 0xFF);
        return r << 16 | g << 8 | b;
    }

    /*=============*/
    /*   EYELIDS   */
    /*=============*/

    /**
     * The whole box takes the fur color next to the eyes (already recolored for the variant); its side faces, the ones
     * laid over the eye, get the darker crease of a closed eye.
     */
    private static void paintEyelids(NativeImage image, VanillaSkinSpec spec) {
        int fur = image.getPixel(spec.furX(), spec.furY());
        int crease = darken(fur, CREASE);
        for (var box : spec.eyelids()) {
            int width = 2 * box.d() + 2 * box.w();
            for (int x = box.u(); x < box.u() + width; x++) {
                for (int y = box.v(); y < box.v() + box.d() + box.h(); y++) {
                    boolean side = y >= box.v() + box.d();
                    image.setPixel(x, y, side ? crease : fur);
                }
            }
        }
    }

    private static int darken(int argb, float amount) {
        int rgb = lerp(argb & 0xFFFFFF, 0x000000, amount);
        return (argb & 0xFF000000) | rgb;
    }
}
