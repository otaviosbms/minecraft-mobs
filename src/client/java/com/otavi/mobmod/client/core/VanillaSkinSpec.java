package com.otavi.mobmod.client.core;

import com.otavi.mobmod.MobMod;
import com.otavi.mobmod.entity.genetics.SkinVariant;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * How {@link VanillaVariantTextures} turns one texture of the game into the skin variants of an adapted animal.
 *
 * @param source  the vanilla texture, read from the resource packs (so a pack that changes it changes the variants too)
 * @param name    base name of the generated textures: mobmod:textures/entity/&lt;name&gt;&lt;suffix&gt;.png
 * @param eyes    pixels of the painted eyes, recolored on their own (red for albinos)
 * @param furX    a fur pixel next to the eyes, whose color the eyelids take
 * @param furY    see {@code furX}
 * @param eyelids UV boxes of the eyelid parts added to the model, painted into free space of the texture
 */
public record VanillaSkinSpec(Identifier source, String name, List<Pixel> eyes, int furX, int furY, List<UvBox> eyelids) {
    public record Pixel(int x, int y) {
    }

    /**
     * A box UV area, laid out like vanilla: a w x h x d box at (u, v) uses a (2d + 2w) x (d + h) area.
     */
    public record UvBox(int u, int v, int w, int h, int d) {
    }

    public Identifier texture(SkinVariant variant) {
        return MobMod.id("textures/entity/" + this.name + variant.textureSuffix() + ".png");
    }
}
