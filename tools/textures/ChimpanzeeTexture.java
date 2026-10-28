import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Paints the chimpanzee textures (normal, albino, melanistic) on the UV layout of ChimpanzeeModel,
 * in the same art style as the elephant (see {@link MobTexture}).
 * <p>
 * The model is built at double detail (then scaled down), so every face has twice the pixels of a normal model:
 * fur strands, eyes, lips, teeth, fingers and toes are painted at that resolution.
 * <p>
 * Run from the project root with the project JDK:
 * <pre>java tools/textures/ChimpanzeeTexture.java src/client/resources/assets/mobmod/textures</pre>
 *
 * <h2>UV layout (256x256)</h2>
 * <pre>
 * body       24x20x28  at (0, 0)
 * head       18x16x14  at (0, 52)
 * brow ridge 16x4x2    at (68, 52)
 * snout      12x5x6    at (68, 60)
 * jaw        12x3x6    at (68, 74)
 * ear         2x8x8    at (108, 52)  mirrored for the left ear
 * arm        10x30x10  at (0, 86)    shared by both arms
 * leg        10x18x10  at (44, 86)   shared by both legs
 * swelling   20x12x6   at (0, 140)   adult females only
 * eyelid      4x2x0    at (132, 52)  mirrored for the left eye, shown while asleep
 * </pre>
 */
public class ChimpanzeeTexture extends MobTexture {
    private static final int FUR = 1;
    private static final int SKIN = 2;
    private static final int EYE = 3;
    private static final int PUPIL = 4;
    private static final int EYE_SHINE = 5;
    private static final int LIP = 6;
    private static final int MOUTH = 7;
    private static final int TEETH = 8;
    private static final int EAR_INNER = 9;
    private static final int NAIL = 10;
    private static final int SWELLING = 11;

    public ChimpanzeeTexture() {
        super(256);
    }

    public static void main(String[] args) throws IOException {
        File texturesDir = new File(args.length > 0 ? args[0] : ".");
        new ChimpanzeeTexture().writeAll(texturesDir);
        // The spawn egg (textures/item/chimpanzee_spawn_egg.png) is hand-drawn, not generated here
    }

    @Override
    protected String name() {
        return "chimpanzee";
    }

    @Override
    protected List<Palette> palettes() {
        return List.of(
                // Near-black brown fur with a grey sheen, pale pinkish face, hands and feet, brown eyes
                palette("", new Ramp(0x100d0c, 0x2b2421, 0x5a4c45), new Ramp(0x9c8073, 0xd2b6a8, 0xecdbd0),
                        0x7a3f1c, 0x140a05, 0xd8cfc8, new Ramp(0x6e3f3a, 0x9a5f58, 0xb67a72), 0x3a1714, 0xe8e0cc,
                        new Ramp(0x4e3933, 0x7d625a, 0x9a7f76), 0x8c7a70, new Ramp(0x8a2f25, 0xb84a3c, 0xdb6d5f)),
                // White fur, pink skin and red eyes
                palette("_albino", new Ramp(0xc9bfb0, 0xe6dfd3, 0xfbf8f2), new Ramp(0xd29d94, 0xefc6bd, 0xfae2dc),
                        0xc03a46, 0x6e1019, 0xfff2f2, new Ramp(0xc06a68, 0xdc8a86, 0xeca6a0), 0x8a3a40, 0xf4efe2,
                        new Ramp(0xc08a82, 0xdba59c, 0xecbdb4), 0xe8d6cc, new Ramp(0xbd554d, 0xde7a70, 0xf29d93)),
                // Jet-black fur and dark skin
                palette("_melanistic", new Ramp(0x030202, 0x0f0c0b, 0x2c2421), new Ramp(0x2c211e, 0x54423d, 0x765f58),
                        0x3d200f, 0x020101, 0x9a9087, new Ramp(0x1e1210, 0x2f1d1a, 0x452c27), 0x120807, 0xcfc6b2,
                        new Ramp(0x1a1210, 0x2b201d, 0x3f302b), 0x4a3d37, new Ramp(0x4e1914, 0x7a2c25, 0x9a453b))
        );
    }

    private static Palette palette(String suffix, Ramp fur, Ramp skin, int eye, int pupil, int eyeShine, Ramp lip, int mouth,
                                   int teeth, Ramp earInner, int nail, Ramp swelling) {
        return new Palette(suffix, Map.ofEntries(
                Map.entry(FUR, fur),
                Map.entry(SKIN, skin),
                Map.entry(EYE, Ramp.flat(eye)),
                Map.entry(PUPIL, Ramp.flat(pupil)),
                Map.entry(EYE_SHINE, Ramp.flat(eyeShine)),
                Map.entry(LIP, lip),
                Map.entry(MOUTH, Ramp.around(mouth, 0.2)),
                Map.entry(TEETH, Ramp.around(teeth, 0.15)),
                Map.entry(EAR_INNER, earInner),
                Map.entry(NAIL, Ramp.flat(nail)),
                Map.entry(SWELLING, swelling)
        ));
    }

    @Override
    protected void paint() {
        this.body(0, 0, 24, 20, 28);
        this.head(0, 52, 18, 16, 14);
        this.brow(68, 52, 16, 4, 2);
        this.snout(68, 60, 12, 5, 6);
        this.jaw(68, 74, 12, 3, 6);
        this.ear(108, 52, 2, 8, 8);
        this.limb(0, 86, 10, 30, 10, 8, 101);
        this.limb(44, 86, 10, 18, 10, 5, 113);
        this.swelling(0, 140, 20, 12, 6);
        this.eyelid(132, 52, 4, 2, 0);
    }

    /*=========*/
    /*   FUR   */
    /*=========*/

    /**
     * Fur: the shared hide noise plus short hair strands falling down the sides and lying along the back.
     */
    private void furBox(int u, int v, int w, int h, int d, int seed) {
        this.shadedBox(u, v, w, h, d, FUR, seed);
        this.forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            boolean topRow = y < v + d;
            // Strands are 3 px long: vertical on the side faces, horizontal on the top and bottom faces
            double strand = topRow ? noise(x / 3, y, seed + 5) : noise(x, y / 3, seed + 5);
            if (strand > 0.86) {
                this.shade[x][y] += 0.35;
            } else if (strand < 0.1) {
                this.shade[x][y] -= 0.3;
            }
        });
    }

    private void body(int u, int v, int w, int h, int d) {
        this.furBox(u, v, w, h, d, 11);
        // Grey sheen on the shoulders and back, like the reference
        this.fillShade(u + d, v, w, d / 2, 0.18);
        // Thinner, greyer fur on the belly and chest
        this.fillShade(u + d + w, v, w, d, 0.3);
        // Darker band along the spine
        this.fillShade(u + d + w / 2 - 1, v, 2, d, -0.2);
    }

    /*==========*/
    /*   HEAD   */
    /*==========*/

    private void head(int u, int v, int w, int h, int d) {
        this.furBox(u, v, w, h, d, 31);
        int fx = u + d;
        int fy = v + d;
        // Pale face from the eyes down, framed by fur on the sides and a widow's peak on top
        this.repaint(fx + 2, fy + 4, w - 4, h - 4, SKIN);
        this.repaint(fx + 4, fy + 2, w - 8, 2, SKIN);
        this.repaint(fx + w / 2 - 1, fy + 2, 2, 1, FUR);
        // Sunken eye sockets under the brow ridge
        this.fillShade(fx + 2, fy + 5, 6, 3, -0.3);
        this.fillShade(fx + w - 8, fy + 5, 6, 3, -0.3);
        this.paintEye(fx + 3, fy + 6, false);
        this.paintEye(fx + w - 7, fy + 6, true);
        // Cheek wrinkles beside the snout
        this.fillShade(fx + 2, fy + 10, 1, 4, -0.3);
        this.fillShade(fx + w - 3, fy + 10, 1, 4, -0.3);
        // Ears sit on fur: a lighter patch where the skin shows through on the sides
        this.fillShade(u + 4, fy + 4, 6, 6, 0.1);
        this.fillShade(u + d + w + 4, fy + 4, 6, 6, 0.1);
    }

    /**
     * Brown iris, dark pupil looking forward and a small shine, with a heavy upper eyelid.
     */
    private void paintEye(int x, int y, boolean mirrored) {
        this.fill(x, y, 4, 2, EYE);
        this.fill(mirrored ? x + 1 : x + 2, y, 1, 2, PUPIL);
        this.material[mirrored ? x + 2 : x + 1][y] = EYE_SHINE;
        // Upper eyelid and the skin fold under the eye
        this.fillShade(x - 1, y - 1, 6, 1, -0.45);
        this.fillShade(x, y + 2, 4, 1, -0.2);
    }

    /**
     * Closed eye, laid over the painted one while the chimpanzee sleeps: the heavy upper lid down,
     * with the dark line of the lashes at the bottom.
     */
    private void eyelid(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 57);
        this.fillShade(u, v + d, 2 * d + 2 * w, 1, -0.25);
        this.fillShade(u, v + d + h - 1, 2 * d + 2 * w, 1, -0.7);
    }

    private void brow(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 53);
        int fx = u + d;
        int fy = v + d;
        // Heavy ridge: wrinkled skin, light on top, dark shadow underneath
        this.fillShade(fx, fy, w, 1, 0.15);
        this.fillShade(fx + 1, fy + 1, w - 2, 1, -0.2);
        this.fillShade(fx, fy + h - 1, w, 1, -0.4);
        // Slight dip in the middle of the ridge
        this.fillShade(fx + w / 2 - 1, fy, 2, h, -0.15);
    }

    private void snout(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 41);
        int fx = u + d;
        int fy = v + d;
        // Wide nostrils, a groove down to the upper lip and wrinkles across the snout
        this.fill(fx + 3, fy + 1, 2, 1, PUPIL);
        this.fill(fx + w - 5, fy + 1, 2, 1, PUPIL);
        this.fillShade(fx + w / 2 - 1, fy + 2, 2, 2, -0.25);
        this.repaint(fx, fy + h - 1, w, 1, LIP);
        for (int row = v + 1; row < v + d; row += 2) {
            this.fillShade(u + d + 1, row, w - 2, 1, -0.25);
        }
        // Roof of the mouth with the upper teeth along the front edge (the last row of the face), seen when the jaw opens
        int bx = u + d + w;
        int front = v + d - 1;
        this.repaint(bx, v, w, d, MOUTH);
        this.repaint(bx + 1, front, w - 2, 1, TEETH);
        this.fillShade(bx + 1, front, 1, 1, -0.4);
        this.fillShade(bx + w - 2, front, 1, 1, -0.4);
    }

    private void jaw(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 47);
        int fx = u + d;
        int fy = v + d;
        // Lower lip, pale chin underneath
        this.repaint(fx, fy, w, 1, LIP);
        this.fillShade(fx + 2, fy + h - 1, w - 4, 1, 0.2);
        // Inside of the mouth with the lower teeth along the front edge (the last row of the face) and the tongue behind
        int tx = u + d;
        this.repaint(tx, v, w, d, MOUTH);
        this.repaint(tx + 1, v + d - 1, w - 2, 1, TEETH);
        this.repaint(tx + 3, v + 1, w - 6, d - 3, LIP);
    }

    private void ear(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SKIN, 61);
        // Both big faces (d x h): skin rim with a dark "C" fold inside and a lighter center, like the reference
        for (int faceX : new int[]{u, u + d + w}) {
            int faceY = v + d;
            this.repaint(faceX + 1, faceY + 1, d - 2, h - 2, EAR_INNER);
            this.repaint(faceX + 3, faceY + 3, d - 5, h - 5, SKIN);
            this.fillShade(faceX + 3, faceY + 3, d - 5, h - 5, -0.1);
            this.fillShade(faceX + 1, faceY + 1, d - 2, 1, -0.25);
        }
    }

    /*===========*/
    /*   LIMBS   */
    /*===========*/

    /**
     * Arm or leg: fur that gets greyer towards the bottom, then pale hands or feet with fingers or toes.
     */
    private void limb(int u, int v, int w, int h, int d, int skinRows, int seed) {
        this.furBox(u, v, w, h, d, seed);
        this.sideFaces(u, v, w, h, d, (fx, fy, fw, fh) -> {
            int skinTop = fy + fh - skinRows;
            // Greyer, shaggier fur on the forearm/shin, ending in a ragged line over the skin
            this.fillShade(fx, skinTop - 5, fw, 5, 0.25);
            this.repaint(fx, skinTop, fw, skinRows, SKIN);
            for (int x = fx; x < fx + fw; x += 2) {
                this.repaint(x, skinTop, 1, 1, FUR);
            }
            // Knuckles (a wrinkled band) and fingers/toes separated by dark lines, nails at the tips
            this.fillShade(fx, skinTop + 2, fw, 1, -0.3);
            for (int x = fx + 2; x < fx + fw - 1; x += 3) {
                this.fillShade(x, fy + fh - 3, 1, 3, -0.4);
            }
            for (int x = fx; x < fx + fw; x += 3) {
                this.repaint(x + 1, fy + fh - 1, 1, 1, NAIL);
            }
        });
        // Palm or sole: skin with creases
        int sx = u + d + w;
        this.repaint(sx, v, w, d, SKIN);
        this.fillShade(sx, v, w, d, -0.15);
        this.fillShade(sx + 1, v + d / 2, w - 2, 1, -0.3);
        this.fillShade(sx + w / 2, v + 1, 1, d - 2, -0.2);
    }

    private void swelling(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, SWELLING, 71);
        // Swollen and glossy: folds across, a lighter highlight on the back face
        this.forEachBoxPixel(u, v, w, h, d, (x, y) -> {
            if (y >= v + d && (y - v - d) % 4 == 3) {
                this.shade[x][y] -= 0.3;
            }
        });
        int backX = u + 2 * d + w;
        this.fillShade(backX + 3, v + d + 2, w - 6, 3, 0.3);
        this.spiral(backX, v + d, w, h, 0.12);
    }
}
