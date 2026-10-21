import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Paints the elephant textures (normal, albino, melanistic) on the UV layout of ElephantModel.
 * <p>
 * Run from the project root with the project JDK:
 * <pre>java tools/textures/ElephantTexture.java src/client/resources/assets/mobmod/textures/entity/elephant</pre>
 * Every variant uses the same pixel layout ("material + shade") with a different palette, so a hand-made
 * texture can replace any of the PNGs as long as it keeps this layout.
 *
 * <h2>UV layout (256x256)</h2>
 * Box UVs follow vanilla: a box of size w x h x d at (u, v) uses a (2d + 2w) x (d + h) area.
 * <pre>
 * body       26x26x40  at (0, 0)
 * leg        10x22x10  at (0, 70)    shared by the four legs
 * head       18x16x12  at (44, 70)
 * trunk       6x10x6   at (104, 70)
 * trunk mid   5x9x5    at (132, 70)
 * trunk tip   4x8x4    at (156, 70)
 * tusk        2x9x2    at (176, 70)  female
 * long tusk   2x13x2   at (188, 70)  male
 * ear         1x15x13  at (0, 106)   mirrored for the left ear
 * tail        2x14x2   at (30, 106)
 * </pre>
 */
public class ElephantTexture {
    private static final int SIZE = 256;

    // Materials
    private static final int EMPTY = 0;
    private static final int SKIN = 1;
    private static final int NAIL = 2;
    private static final int TUSK = 3;
    private static final int EYE = 4;
    private static final int EYE_SHINE = 5;
    private static final int EAR_INNER = 6;
    private static final int TAIL_TUFT = 7;

    private final int[][] material = new int[SIZE][SIZE];
    /**
     * -1 (darkest) to 1 (lightest) for skin-like materials.
     */
    private final double[][] shade = new double[SIZE][SIZE];

    record Palette(String suffix, int dark, int base, int light, int nail, int tusk, int eye, int eyeShine, int earInner, int tuft) {
    }

    private static final Palette[] PALETTES = {
            new Palette("", 0x5f5856, 0x7d7572, 0xa59c97, 0xe4dccb, 0xf0e9d8, 0x141010, 0xe8e8e8, 0x8a7774, 0x3a3432),
            new Palette("_albino", 0xc6a99f, 0xdcc5bb, 0xf0e2da, 0xf2ebe0, 0xf5f0e4, 0xb8323e, 0xf6c9cf, 0xe8b9b4, 0xe9d8cf),
            new Palette("_melanistic", 0x1f1b1a, 0x332e2c, 0x4a4341, 0xb9b0a1, 0xe6dfcf, 0x080606, 0x9a9a9a, 0x3b3130, 0x121010),
    };

    public static void main(String[] args) throws IOException {
        File outputDir = new File(args.length > 0 ? args[0] : ".");
        var texture = new ElephantTexture();
        texture.paint();
        for (Palette palette : PALETTES) {
            File file = new File(outputDir, "elephant" + palette.suffix() + ".png");
            ImageIO.write(texture.render(palette), "png", file);
            System.out.println("Wrote " + file);
        }
    }

    private void paint() {
        body(0, 0, 26, 26, 40);
        leg(0, 70, 10, 22, 10);
        head(44, 70, 18, 16, 12);
        trunk(104, 70, 6, 10, 6, 0);
        trunk(132, 70, 5, 9, 5, 1);
        trunk(156, 70, 4, 8, 4, 2);
        tusk(176, 70, 2, 9, 2);
        tusk(188, 70, 2, 13, 2);
        ear(0, 106, 1, 15, 13);
        tail(30, 106, 2, 14, 2);
    }

    /*==========*/
    /*   PARTS  */
    /*==========*/

    private void body(int u, int v, int w, int h, int d) {
        skinBox(u, v, w, h, d, 11);
        // Long light streaks along the back and the flanks, like the reference
        forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            double streak = Math.sin(y * 0.9 + noise(x / 7, y, 3) * 2.5);
            if (streak > 0.82) {
                this.shade[x][y] += 0.45;
            } else if (streak < -0.9) {
                this.shade[x][y] -= 0.25;
            }
        });
    }

    private void leg(int u, int v, int w, int h, int d) {
        skinBox(u, v, w, h, d, 23);
        // Spiral wrinkles on every side face, toenails at the bottom of the front face
        sideFaces(u, v, w, h, d, (fx, fy, fw, fh) -> spiral(fx, fy, fw, fh - 4, 0.5));
        int frontX = u + d;
        int frontY = v + d;
        for (int nail = 0; nail < 3; nail++) {
            int nx = frontX + 1 + nail * 3;
            fill(nx, frontY + h - 2, 2, 2, NAIL);
        }
        // Darker, rougher sole
        fillShade(u + d + w, v, w, d, -0.5);
    }

    private void head(int u, int v, int w, int h, int d) {
        skinBox(u, v, w, h, d, 37);
        int frontX = u + d;
        int frontY = v + d;
        // Forehead wrinkles on the front face
        for (int row = 2; row < h - 4; row += 3) {
            for (int x = 1; x < w - 1; x++) {
                this.shade[frontX + x][frontY + row] -= 0.35;
            }
        }
        spiral(frontX, frontY, w, h, 0.2);
        // Eyes near the front edge of both side faces
        int eyeY = v + d + 7;
        paintEye(u + d - 4, eyeY, false);
        paintEye(u + d + w + 2, eyeY, true);
    }

    private void paintEye(int x, int y, boolean mirrored) {
        fill(x, y, 2, 1, EYE);
        this.material[mirrored ? x : x + 1][y] = EYE_SHINE;
        // Wrinkle above the eye
        for (int i = -1; i < 3; i++) {
            this.shade[x + i][y - 1] -= 0.4;
        }
    }

    private void trunk(int u, int v, int w, int h, int d, int segment) {
        skinBox(u, v, w, h, d, 51 + segment);
        // Rings of wrinkles all around the trunk
        forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            if (y >= v + d && (y - v - d + segment) % 2 == 0) {
                this.shade[x][y] -= 0.3;
            }
        });
    }

    private void tusk(int u, int v, int w, int h, int d) {
        forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            this.material[x][y] = TUSK;
            // Darker at the root, brighter toward the tip
            this.shade[x][y] = y >= v + d ? 0.4 * (y - v - d) / (double) h - 0.2 : 0.2;
        });
    }

    private void ear(int u, int v, int w, int h, int d) {
        skinBox(u, v, w, h, d, 67);
        // The two big faces of the ear are the side faces (d x h); give the inner side a pinker tone and a fold
        int outerX = u;
        int innerX = u + d + w;
        int faceY = v + d;
        for (int x = 0; x < d; x++) {
            for (int y = 0; y < h; y++) {
                boolean border = x == 0 || y == 0 || x == d - 1 || y == h - 1;
                if (!border) {
                    this.material[innerX + x][faceY + y] = EAR_INNER;
                }
            }
        }
        // Dark rim and a fold inside, so the ear stands out against the body like in the reference
        for (int x = 0; x < d; x++) {
            for (int y = 0; y < h; y++) {
                boolean border = x == 0 || y == 0 || x == d - 1 || y == h - 1;
                boolean fold = !border && (x == 2 || y == 2 || x == d - 3 || y == h - 3) && x >= 2 && y >= 2 && x <= d - 3 && y <= h - 3;
                if (border) {
                    this.shade[outerX + x][faceY + y] -= 0.45;
                } else if (fold) {
                    this.shade[outerX + x][faceY + y] -= 0.3;
                } else {
                    this.shade[outerX + x][faceY + y] += 0.12;
                }
            }
        }
        spiral(innerX, faceY, d, h, 0.2);
    }

    private void tail(int u, int v, int w, int h, int d) {
        skinBox(u, v, w, h, d, 79);
        // Tuft of dark hair at the end
        forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            if (y >= v + d + h - 3) {
                this.material[x][y] = TAIL_TUFT;
            }
        });
    }

    /*=============*/
    /*   HELPERS   */
    /*=============*/

    private interface PixelAction {
        void apply(int x, int y);
    }

    private interface FaceAction {
        void apply(int x, int y, int width, int height);
    }

    private void skinBox(int u, int v, int w, int h, int d, int seed) {
        forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            this.material[x][y] = SKIN;
            // Two octaves of value noise: blotches plus fine grain
            this.shade[x][y] = (noise(x / 3, y / 3, seed) - 0.5) * 0.45 + (noise(x, y, seed + 1) - 0.5) * 0.22;
        });
        // Top face a bit lighter, bottom face darker (fake lighting baked like vanilla textures)
        fillShade(u + d, v, w, d, 0.12);
        fillShade(u + d + w, v, w, d, -0.3);
    }

    private void forEachBoxPixel(int u, int v, int w, int h, int d, PixelAction action) {
        // Top row: top and bottom faces
        for (int x = u + d; x < u + d + 2 * w; x++) {
            for (int y = v; y < v + d; y++) {
                action.apply(x, y);
            }
        }
        // Bottom row: the four side faces
        for (int x = u; x < u + 2 * d + 2 * w; x++) {
            for (int y = v + d; y < v + d + h; y++) {
                action.apply(x, y);
            }
        }
    }

    private void sideFaces(int u, int v, int w, int h, int d, FaceAction action) {
        int y = v + d;
        action.apply(u, y, d, h);
        action.apply(u + d, y, w, h);
        action.apply(u + d + w, y, d, h);
        action.apply(u + 2 * d + w, y, w, h);
    }

    /**
     * Concentric rings darkening the skin, like the spiral wrinkles on the legs of the reference.
     */
    private void spiral(int x0, int y0, int width, int height, double strength) {
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

    private void fill(int x0, int y0, int width, int height, int mat) {
        for (int x = x0; x < x0 + width; x++) {
            for (int y = y0; y < y0 + height; y++) {
                this.material[x][y] = mat;
                this.shade[x][y] = 0;
            }
        }
    }

    private void fillShade(int x0, int y0, int width, int height, double amount) {
        for (int x = x0; x < x0 + width; x++) {
            for (int y = y0; y < y0 + height; y++) {
                this.shade[x][y] += amount;
            }
        }
    }

    /**
     * Deterministic hash noise in [0, 1): the same layout on every run.
     */
    private static double noise(int x, int y, int seed) {
        long h = x * 374761393L + y * 668265263L + seed * 2147483647L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        h ^= h >>> 16;
        return (h & 0xFFFF) / 65536.0;
    }

    /*============*/
    /*   OUTPUT   */
    /*============*/

    private BufferedImage render(Palette palette) {
        var image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                double s = Math.max(-1.0, Math.min(1.0, this.shade[x][y]));
                int rgb = switch (this.material[x][y]) {
                    case SKIN -> skin(palette, s);
                    case NAIL -> palette.nail();
                    case TUSK -> lighten(palette.tusk(), s);
                    case EYE -> palette.eye();
                    case EYE_SHINE -> palette.eyeShine();
                    case EAR_INNER -> lerp(skin(palette, s), palette.earInner(), 0.55);
                    case TAIL_TUFT -> palette.tuft();
                    default -> -1;
                };
                image.setRGB(x, y, rgb == -1 ? 0 : 0xFF000000 | rgb);
            }
        }
        return image;
    }

    private static int skin(Palette palette, double shade) {
        return shade < 0 ? lerp(palette.base(), palette.dark(), -shade) : lerp(palette.base(), palette.light(), shade);
    }

    private static int lighten(int color, double amount) {
        return amount < 0 ? lerp(color, 0x000000, -amount * 0.3) : lerp(color, 0xFFFFFF, amount * 0.3);
    }

    private static int lerp(int a, int b, double t) {
        int r = (int) Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = (int) Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = (int) Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
