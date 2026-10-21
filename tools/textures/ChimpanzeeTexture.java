import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Paints the chimpanzee textures (normal, albino, melanistic) and its spawn egg on the UV layout of ChimpanzeeModel,
 * in the same art style as the elephant (see {@link MobTexture}).
 * <p>
 * Run from the project root with the project JDK:
 * <pre>java tools/textures/ChimpanzeeTexture.java src/client/resources/assets/mobmod/textures</pre>
 *
 * <h2>UV layout (128x128)</h2>
 * <pre>
 * body       12x10x14  at (0, 0)
 * head        9x8x7    at (0, 26)
 * muzzle      6x4x3    at (34, 26)
 * brow ridge  8x2x1    at (54, 26)
 * ear         1x4x4    at (74, 26)   mirrored for the left ear
 * arm         5x15x5   at (0, 44)    shared by both arms
 * leg         5x9x5    at (22, 44)   shared by both legs
 * swelling   10x6x3    at (44, 44)   adult females only
 * </pre>
 */
public class ChimpanzeeTexture extends MobTexture {
    private static final int FUR = 1;
    private static final int SKIN = 2;
    private static final int EYE = 3;
    private static final int PUPIL = 4;
    private static final int MOUTH = 5;
    private static final int EAR_INNER = 6;
    private static final int SWELLING = 7;

    public ChimpanzeeTexture() {
        super(128);
    }

    public static void main(String[] args) throws IOException {
        File texturesDir = new File(args.length > 0 ? args[0] : ".");
        new ChimpanzeeTexture().writeAll(texturesDir);
        writeSpawnEgg(texturesDir, "chimpanzee", 0x141110, 0x2b2421, 0xd2b6a8, 0x5a4c45, 91);
    }

    @Override
    protected String name() {
        return "chimpanzee";
    }

    @Override
    protected List<Palette> palettes() {
        return List.of(
                // Near-black brown fur, pale pinkish face, hands and feet, brown eyes
                palette("", new Ramp(0x141110, 0x2b2421, 0x4b403a), new Ramp(0xa88c80, 0xd2b6a8, 0xe9d6cb),
                        0x7a3f1c, 0x1a0e08, 0x7a4a44, new Ramp(0x5e4640, 0x7d625a, 0x977b72), new Ramp(0x8f3328, 0xb84a3c, 0xd4675a)),
                // White fur, pink skin and red eyes
                palette("_albino", new Ramp(0xcfc6b8, 0xe6dfd3, 0xf7f3ec), new Ramp(0xd9a9a0, 0xefc6bd, 0xf8dcd6),
                        0xc03a46, 0x7a1620, 0xb06a66, new Ramp(0xc8958c, 0xdba59c, 0xe8b9b0), new Ramp(0xc25a52, 0xde7a70, 0xef9a90)),
                // Jet-black fur and dark skin
                palette("_melanistic", new Ramp(0x060505, 0x0f0c0b, 0x231d1b), new Ramp(0x3a2d29, 0x54423d, 0x6d5852),
                        0x3d200f, 0x050302, 0x2a1a17, new Ramp(0x241b18, 0x332723, 0x44352f), new Ramp(0x5a1f1a, 0x7a2c25, 0x95433a))
        );
    }

    private static Palette palette(String suffix, Ramp fur, Ramp skin, int eye, int pupil, int mouth, Ramp earInner, Ramp swelling) {
        return new Palette(suffix, Map.of(
                FUR, fur,
                SKIN, skin,
                EYE, Ramp.flat(eye),
                PUPIL, Ramp.flat(pupil),
                MOUTH, Ramp.flat(mouth),
                EAR_INNER, earInner,
                SWELLING, swelling
        ));
    }

    @Override
    protected void paint() {
        this.body(0, 0, 12, 10, 14);
        this.head(0, 26, 9, 8, 7);
        this.muzzle(34, 26, 6, 4, 3);
        this.brow(54, 26, 8, 2, 1);
        this.ear(74, 26, 1, 4, 4);
        this.limb(0, 44, 5, 15, 5, 3, 101);
        this.limb(22, 44, 5, 9, 5, 2, 113);
        this.swelling(44, 44, 10, 6, 3);
    }

    /**
     * Fur: same noise as the elephant hide, with more grain and light streaks for the hair.
     */
    private void furBox(int u, int v, int w, int h, int d, int seed) {
        this.shadedBox(u, v, w, h, d, FUR, seed);
        this.forEachBoxPixel(u, v, w, h, d, (x, y) -> this.shade[x][y] += (noise(x, y, seed + 7) - 0.5) * 0.35);
        this.streaks(u, v, w, h, d, 1.3, 0.4, 0.2, seed + 3);
    }

    private void body(int u, int v, int w, int h, int d) {
        this.furBox(u, v, w, h, d, 11);
        // Greyer, thinner fur on the belly
        this.fillShade(u + d + w, v, w, d, 0.2);
    }

    private void head(int u, int v, int w, int h, int d) {
        this.furBox(u, v, w, h, d, 31);
        int frontX = u + d;
        int frontY = v + d;
        // Pale face under the brow ridge, framed by fur on the sides and top
        this.repaint(frontX + 1, frontY + 2, w - 2, h - 2, SKIN);
        this.repaint(frontX + 2, frontY + 1, w - 4, 1, SKIN);
        // Wrinkles under the eyes
        this.fillShade(frontX + 1, frontY + 4, w - 2, 1, -0.3);
        // Eyes: brown iris with a dark pupil, deep under the brow
        this.paintEye(frontX + 1, frontY + 3);
        this.paintEye(frontX + w - 3, frontY + 3);
    }

    private void paintEye(int x, int y) {
        this.fill(x, y, 2, 1, EYE);
        this.material[x + 1][y] = PUPIL;
        // Shadow of the brow ridge right above the eye
        this.fillShade(x - 1, y - 1, 4, 1, -0.45);
    }

    private void muzzle(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 41);
        int frontX = u + d;
        int frontY = v + d;
        // Nostrils, a wide mouth line and a lighter lower lip
        this.fill(frontX + 2, frontY, 1, 1, MOUTH);
        this.fill(frontX + w - 3, frontY, 1, 1, MOUTH);
        this.fill(frontX, frontY + 2, w, 1, MOUTH);
        this.fillShade(frontX, frontY + 3, w, 1, 0.25);
        // Wrinkles along the snout
        this.spiral(frontX, frontY, w, h, 0.12);
    }

    private void brow(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 53);
        // Heavy ridge: light on top, dark underneath
        this.forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            if (y == v + d + h - 1) {
                this.shade[x][y] -= 0.35;
            }
        });
    }

    private void ear(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 61);
        // Both big faces: skin rim with a dark "C" fold inside, like the reference
        for (int faceX : new int[]{u, u + d + w}) {
            int faceY = v + d;
            this.repaint(faceX + 1, faceY + 1, d - 2, h - 2, EAR_INNER);
            this.shade[faceX + 2][faceY + 2] += 0.4;
            this.shade[faceX + 1][faceY + 1] -= 0.2;
        }
    }

    /**
     * Arm or leg: fur that gets lighter towards the bottom, then pale hands or feet.
     */
    private void limb(int u, int v, int w, int h, int d, int skinRows, int seed) {
        this.furBox(u, v, w, h, d, seed);
        this.sideFaces(u, v, w, h, d, (fx, fy, fw, fh) -> {
            // Greyer fur on the forearm/shin
            this.fillShade(fx, fy + fh - skinRows - 3, fw, 3, 0.25);
            this.repaint(fx, fy + fh - skinRows, fw, skinRows, SKIN);
            // Knuckles and toes
            this.fillShade(fx, fy + fh - skinRows, fw, 1, -0.25);
            for (int x = fx + 1; x < fx + fw; x += 2) {
                this.shade[x][fy + fh - 1] -= 0.3;
            }
        });
        // Palm or sole
        this.repaint(u + d + w, v, w, d, SKIN);
        this.fillShade(u + d + w, v, w, d, -0.2);
    }

    private void swelling(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SWELLING, 71);
        // Smooth and swollen: lighter in the middle, darker folds
        this.sideFaces(u, v, w, h, d, (fx, fy, fw, fh) -> this.spiral(fx, fy, fw, fh, 0.15));
        this.fillShade(u + d + w + d + 1, v + d + 1, w - 2, h - 2, 0.2);
    }
}
