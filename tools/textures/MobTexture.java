import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Shared base of the texture generators: every mob texture is painted once as "material + shade" per pixel,
 * then rendered with one palette per skin variant (normal, albino, melanistic).
 * <p>
 * Art style shared by every mob of the mod: two octaves of value noise (soft blotches plus fine grain),
 * light streaks, wrinkles drawn as darker lines or spirals, top faces a bit lighter and bottom faces darker.
 * <p>
 * Generators are single-file programs that use this class; run them from the project root, e.g.:
 * <pre>java tools/textures/ElephantTexture.java src/client/resources/assets/mobmod/textures</pre>
 */
public abstract class MobTexture {
    public static final int EMPTY = 0;

    protected final int size;
    protected final int[][] material;
    /**
     * -1 (darkest) to 1 (lightest).
     */
    protected final double[][] shade;

    /**
     * Colors of one material: shade -1 gives {@code dark}, 0 gives {@code base}, 1 gives {@code light}.
     */
    public record Ramp(int dark, int base, int light) {
        public static Ramp flat(int color) {
            return new Ramp(color, color, color);
        }

        /**
         * Ramp from the color itself towards black and white by {@code amount}.
         */
        public static Ramp around(int color, double amount) {
            return new Ramp(lerp(color, 0x000000, amount), color, lerp(color, 0xFFFFFF, amount));
        }

        public Ramp mix(int color, double amount) {
            return new Ramp(lerp(this.dark, color, amount), lerp(this.base, color, amount), lerp(this.light, color, amount));
        }

        int at(double shade) {
            return shade < 0 ? lerp(this.base, this.dark, -shade) : lerp(this.base, this.light, shade);
        }
    }

    public record Palette(String suffix, Map<Integer, Ramp> ramps) {
    }

    protected MobTexture(int size) {
        this.size = size;
        this.material = new int[size][size];
        this.shade = new double[size][size];
    }

    /**
     * Name of the mob: files are written as &lt;dir&gt;/entity/&lt;name&gt;/&lt;name&gt;&lt;suffix&gt;.png.
     */
    protected abstract String name();

    protected abstract void paint();

    protected abstract List<Palette> palettes();

    /**
     * Writes every variant of the entity texture under {@code texturesDir}/entity/&lt;name&gt;/.
     */
    public void writeAll(File texturesDir) throws IOException {
        this.paint();
        File dir = new File(texturesDir, "entity/" + this.name());
        dir.mkdirs();
        for (Palette palette : this.palettes()) {
            File file = new File(dir, this.name() + palette.suffix() + ".png");
            ImageIO.write(this.render(palette), "png", file);
            System.out.println("Wrote " + file);
        }
    }

    /*===========*/
    /*   BOXES   */
    /*===========*/

    protected interface PixelAction {
        void apply(int x, int y);
    }

    protected interface FaceAction {
        void apply(int x, int y, int width, int height);
    }

    /**
     * Fills a box UV area with a material and the shared noise, with lighter top and darker bottom faces.
     */
    protected void shadedBox(int u, int v, int w, int h, int d, int mat, int seed) {
        this.forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            this.material[x][y] = mat;
            // Two octaves of value noise: blotches plus fine grain
            this.shade[x][y] = (noise(x / 3, y / 3, seed) - 0.5) * 0.45 + (noise(x, y, seed + 1) - 0.5) * 0.22;
        });
        this.fillShade(u + d, v, w, d, 0.12);
        this.fillShade(u + d + w, v, w, d, -0.3);
    }

    /**
     * Box UVs follow vanilla: a w x h x d box at (u, v) uses a (2d + 2w) x (d + h) area.
     * Top row: top face then bottom face. Bottom row: right side, front, left side, back.
     */
    protected void forEachBoxPixel(int u, int v, int w, int h, int d, PixelAction action) {
        for (int x = u + d; x < u + d + 2 * w; x++) {
            for (int y = v; y < v + d; y++) {
                action.apply(x, y);
            }
        }
        for (int x = u; x < u + 2 * d + 2 * w; x++) {
            for (int y = v + d; y < v + d + h; y++) {
                action.apply(x, y);
            }
        }
    }

    /**
     * The four side faces of a box, in UV order: right side, front, left side, back.
     */
    protected void sideFaces(int u, int v, int w, int h, int d, FaceAction action) {
        int y = v + d;
        action.apply(u, y, d, h);
        action.apply(u + d, y, w, h);
        action.apply(u + d + w, y, d, h);
        action.apply(u + 2 * d + w, y, w, h);
    }

    /*=============*/
    /*   DETAILS   */
    /*=============*/

    /**
     * Light streaks (and a few darker ones) following the rows of an area, like the hide of the elephant.
     */
    protected void streaks(int u, int v, int w, int h, int d, double frequency, double light, double dark, int seed) {
        this.forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            double streak = Math.sin(y * frequency + noise(x / 7, y, seed) * 2.5);
            if (streak > 0.82) {
                this.shade[x][y] += light;
            } else if (streak < -0.9) {
                this.shade[x][y] -= dark;
            }
        });
    }

    /**
     * Concentric rings darkening an area, like spiral wrinkles.
     */
    protected void spiral(int x0, int y0, int width, int height, double strength) {
        double cx = x0 + width / 2.0 - 0.5;
        double cy = y0 + height / 2.0 - 0.5;
        for (int x = x0; x < x0 + width; x++) {
            for (int y = y0; y < y0 + Math.max(height, 0); y++) {
                double dx = x - cx;
                double dy = (y - cy) * 0.8;
                double angle = Math.atan2(dy, dx);
                double radius = Math.sqrt(dx * dx + dy * dy) + angle * 0.45;
                if (radius > 0.8 && ((int) Math.floor(radius)) % 2 == 0) {
                    this.shade[x][y] -= strength;
                }
            }
        }
    }

    protected void fill(int x0, int y0, int width, int height, int mat) {
        for (int x = x0; x < x0 + width; x++) {
            for (int y = y0; y < y0 + height; y++) {
                this.material[x][y] = mat;
                this.shade[x][y] = 0;
            }
        }
    }

    /**
     * Changes the material of an area but keeps its noise, so the new material has the same texture.
     */
    protected void repaint(int x0, int y0, int width, int height, int mat) {
        for (int x = x0; x < x0 + width; x++) {
            for (int y = y0; y < y0 + height; y++) {
                this.material[x][y] = mat;
            }
        }
    }

    protected void fillShade(int x0, int y0, int width, int height, double amount) {
        for (int x = x0; x < x0 + width; x++) {
            for (int y = y0; y < y0 + height; y++) {
                this.shade[x][y] += amount;
            }
        }
    }

    /**
     * Deterministic hash noise in [0, 1): the same layout on every run.
     */
    protected static double noise(int x, int y, int seed) {
        long h = x * 374761393L + y * 668265263L + seed * 2147483647L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        h ^= h >>> 16;
        return (h & 0xFFFF) / 65536.0;
    }

    /*============*/
    /*   OUTPUT   */
    /*============*/

    protected BufferedImage render(Palette palette) {
        var image = new BufferedImage(this.size, this.size, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < this.size; x++) {
            for (int y = 0; y < this.size; y++) {
                int mat = this.material[x][y];
                if (mat == EMPTY) {
                    continue;
                }
                Ramp ramp = palette.ramps().get(mat);
                if (ramp == null) {
                    throw new IllegalStateException("Palette '" + palette.suffix() + "' has no color for material " + mat);
                }
                double s = Math.max(-1.0, Math.min(1.0, this.shade[x][y]));
                image.setRGB(x, y, 0xFF000000 | ramp.at(s));
            }
        }
        return image;
    }

    /**
     * A 16x16 spawn egg in the vanilla shape: dark outline, base color, spots and a highlight.
     */
    protected static void writeSpawnEgg(File texturesDir, String name, int outline, int base, int spots, int highlight, int seed) throws IOException {
        var image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        boolean[][] inside = new boolean[16][16];
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                // Egg: an ellipse narrower at the top
                double dy = (y + 0.5 - 8.8) / 7.0;
                double rx = dy < 0 ? 5.6 - 1.6 * dy * dy : 5.6;
                double dx = (x + 0.5 - 8.0) / rx;
                inside[x][y] = dx * dx + dy * dy <= 1.0;
            }
        }
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (!inside[x][y]) {
                    continue;
                }
                boolean edge = x == 0 || y == 0 || x == 15 || y == 15 || !inside[x - 1][y] || !inside[x + 1][y] || !inside[x][y - 1] || !inside[x][y + 1];
                int color;
                if (edge) {
                    color = outline;
                } else if (noise(x / 2, y / 2, seed) > 0.72) {
                    color = spots;
                } else {
                    color = lerp(base, 0x000000, noise(x, y, seed + 1) * 0.12);
                }
                image.setRGB(x, y, 0xFF000000 | color);
            }
        }
        // Highlight on the upper left
        image.setRGB(5, 3, 0xFF000000 | highlight);
        image.setRGB(4, 4, 0xFF000000 | highlight);
        File file = new File(texturesDir, "item/" + name + "_spawn_egg.png");
        file.getParentFile().mkdirs();
        ImageIO.write(image, "png", file);
        System.out.println("Wrote " + file);
    }

    protected static int lerp(int a, int b, double t) {
        int r = (int) Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = (int) Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = (int) Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
