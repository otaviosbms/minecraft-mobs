import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Paints the elephant textures (normal, albino, melanistic) on the UV layout of ElephantModel.
 * <p>
 * Run from the project root with the project JDK:
 * <pre>java tools/textures/ElephantTexture.java src/client/resources/assets/mobmod/textures</pre>
 *
 * <h2>UV layout (256x256)</h2>
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
public class ElephantTexture extends MobTexture {
    private static final int SKIN = 1;
    private static final int NAIL = 2;
    private static final int TUSK = 3;
    private static final int EYE = 4;
    private static final int EYE_SHINE = 5;
    private static final int EAR_INNER = 6;
    private static final int TAIL_TUFT = 7;

    public ElephantTexture() {
        super(256);
    }

    public static void main(String[] args) throws IOException {
        new ElephantTexture().writeAll(new File(args.length > 0 ? args[0] : "."));
    }

    @Override
    protected String name() {
        return "elephant";
    }

    @Override
    protected List<Palette> palettes() {
        return List.of(
                palette("", new Ramp(0x5f5856, 0x7d7572, 0xa59c97), 0xe4dccb, 0xf0e9d8, 0x141010, 0xe8e8e8, 0x8a7774, 0x3a3432),
                palette("_albino", new Ramp(0xc6a99f, 0xdcc5bb, 0xf0e2da), 0xf2ebe0, 0xf5f0e4, 0xb8323e, 0xf6c9cf, 0xe8b9b4, 0xe9d8cf),
                palette("_melanistic", new Ramp(0x1f1b1a, 0x332e2c, 0x4a4341), 0xb9b0a1, 0xe6dfcf, 0x080606, 0x9a9a9a, 0x3b3130, 0x121010)
        );
    }

    private static Palette palette(String suffix, Ramp skin, int nail, int tusk, int eye, int eyeShine, int earInner, int tuft) {
        return new Palette(suffix, Map.of(
                SKIN, skin,
                NAIL, Ramp.flat(nail),
                TUSK, Ramp.around(tusk, 0.3),
                EYE, Ramp.flat(eye),
                EYE_SHINE, Ramp.flat(eyeShine),
                EAR_INNER, skin.mix(earInner, 0.55),
                TAIL_TUFT, Ramp.flat(tuft)
        ));
    }

    @Override
    protected void paint() {
        this.body(0, 0, 26, 26, 40);
        this.leg(0, 70, 10, 22, 10);
        this.head(44, 70, 18, 16, 12);
        this.trunk(104, 70, 6, 10, 6, 0);
        this.trunk(132, 70, 5, 9, 5, 1);
        this.trunk(156, 70, 4, 8, 4, 2);
        this.tusk(176, 70, 2, 9, 2);
        this.tusk(188, 70, 2, 13, 2);
        this.ear(0, 106, 1, 15, 13);
        this.tail(30, 106, 2, 14, 2);
    }

    private void body(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 11);
        // Long light streaks along the back and the flanks, like the reference
        this.streaks(u, v, w, h, d, 0.9, 0.45, 0.25, 3);
    }

    private void leg(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 23);
        // Spiral wrinkles on every side face, toenails at the bottom of the front face
        this.sideFaces(u, v, w, h, d, (fx, fy, fw, fh) -> this.spiral(fx, fy, fw, fh - 4, 0.5));
        int frontX = u + d;
        int frontY = v + d;
        for (int nail = 0; nail < 3; nail++) {
            this.fill(frontX + 1 + nail * 3, frontY + h - 2, 2, 2, NAIL);
        }
        // Darker, rougher sole
        this.fillShade(u + d + w, v, w, d, -0.5);
    }

    private void head(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 37);
        int frontX = u + d;
        int frontY = v + d;
        // Forehead wrinkles on the front face
        for (int row = 2; row < h - 4; row += 3) {
            this.fillShade(frontX + 1, frontY + row, w - 2, 1, -0.35);
        }
        this.spiral(frontX, frontY, w, h, 0.2);
        // Eyes near the front edge of both side faces
        int eyeY = v + d + 7;
        this.paintEye(u + d - 4, eyeY, false);
        this.paintEye(u + d + w + 2, eyeY, true);
    }

    private void paintEye(int x, int y, boolean mirrored) {
        this.fill(x, y, 2, 1, EYE);
        this.material[mirrored ? x : x + 1][y] = EYE_SHINE;
        // Wrinkle above the eye
        this.fillShade(x - 1, y - 1, 4, 1, -0.4);
    }

    private void trunk(int u, int v, int w, int h, int d, int segment) {
        this.shadedBox(u, v, w, h, d, SKIN, 51 + segment);
        // Rings of wrinkles all around the trunk
        this.forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            if (y >= v + d && (y - v - d + segment) % 2 == 0) {
                this.shade[x][y] -= 0.3;
            }
        });
    }

    private void tusk(int u, int v, int w, int h, int d) {
        this.forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            this.material[x][y] = TUSK;
            // Darker at the root, brighter toward the tip
            this.shade[x][y] = y >= v + d ? 0.4 * (y - v - d) / (double) h - 0.2 : 0.2;
        });
    }

    private void ear(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 67);
        // The two big faces of the ear are the side faces (d x h)
        int outerX = u;
        int innerX = u + d + w;
        int faceY = v + d;
        // Inner side: pinker tone inside a skin-colored rim
        this.repaint(innerX + 1, faceY + 1, d - 2, h - 2, EAR_INNER);
        // Outer side: dark rim and a fold inside, so the ear stands out against the body like in the reference
        for (int x = 0; x < d; x++) {
            for (int y = 0; y < h; y++) {
                boolean border = x == 0 || y == 0 || x == d - 1 || y == h - 1;
                boolean fold = !border && (x == 2 || y == 2 || x == d - 3 || y == h - 3) && x >= 2 && y >= 2 && x <= d - 3 && y <= h - 3;
                this.shade[outerX + x][faceY + y] += border ? -0.45 : fold ? -0.3 : 0.12;
            }
        }
        this.spiral(innerX, faceY, d, h, 0.2);
    }

    private void tail(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 79);
        // Tuft of dark hair at the end
        this.forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            if (y >= v + d + h - 3) {
                this.material[x][y] = TAIL_TUFT;
            }
        });
    }
}
