import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Paints the meerkat textures (normal, albino, melanistic) on the UV layout of MeerkatModel,
 * in the same art style as the elephant (see {@link MobTexture}), with the colors of the reference:
 * sandy orange fur, dark bands across the back, a dark eye mask, a pale face and belly and a dark tail tip.
 * <p>
 * Also writes the spawn egg: a 16x16 front view of the face, hand-drawn below as a pixel map
 * in the style of the other eggs, with a transparent background.
 * <p>
 * Run from the project root with the project JDK:
 * <pre>java tools/textures/MeerkatTexture.java src/client/resources/assets/mobmod/textures</pre>
 *
 * <h2>UV layout (128x128)</h2>
 * <pre>
 * body    12x15x9   at (0, 0)
 * head    14x9x12   at (0, 24)
 * snout    6x5x5    at (54, 24)
 * ear      2x3x3    at (78, 24)   mirrored for the left ear
 * arm      3x8x3    at (0, 46)    shared by both arms
 * haunch   5x6x6    at (14, 46)   mirrored for the left leg
 * foot     5x3x6    at (38, 46)   mirrored for the left leg
 * tail     2x2x14   at (0, 60)
 * eyelid   0x2x2    at (90, 24)   mirrored for the left eye, shown while asleep
 * </pre>
 */
public class MeerkatTexture extends MobTexture {
    private static final int FUR = 1;
    private static final int BELLY = 2;
    private static final int STRIPE = 3;
    private static final int MASK = 4;
    private static final int EYE = 5;
    private static final int EYE_SHINE = 6;
    private static final int NOSE = 7;
    private static final int EAR = 8;
    private static final int CLAW = 9;
    private static final int TAIL_TIP = 10;

    /**
     * The spawn egg, a front view of the face: O outline, E ear, F fur, L light fur, D dark fur, M eye mask,
     * W eye shine, C cream face, N nose, . transparent.
     */
    private static final String[] EGG = {
            "................",
            "...OOOOOOOOOO...",
            "..OLLLLFFLLLLO..",
            "..OFLFFDDFFLFO..",
            ".OOFFFFFFFFFFOO.",
            "OEOMMMFLLFMMMOEO",
            "OEMMWMMCCMMWMMEO",
            "OEOMMMCCCCMMMOEO",
            ".OOFCCCCCCCCFOO.",
            "..OFCCCLLCCCFO..",
            "..OFCCNNNNCCFO..",
            "..ODCCNNNNCCDO..",
            "...OFCCCCCCFO...",
            "....ODCCCCDO....",
            ".....OOOOOO.....",
            "................"
    };
    private static final Map<Character, Integer> EGG_COLORS = Map.of(
            'O', 0x3a1e12,
            'E', 0x6a341f,
            'F', 0xdc9558,
            'L', 0xefb87e,
            'D', 0xb8743f,
            'M', 0x5a2a18,
            'W', 0xf4efe6,
            'C', 0xf3d2a0,
            'N', 0x2a140c
    );

    public MeerkatTexture() {
        super(128);
    }

    public static void main(String[] args) throws IOException {
        File texturesDir = new File(args.length > 0 ? args[0] : ".");
        new MeerkatTexture().writeAll(texturesDir);
        writeEgg(texturesDir);
    }

    @Override
    protected String name() {
        return "meerkat";
    }

    @Override
    protected List<Palette> palettes() {
        return List.of(
                // Sandy orange fur, cream face and belly, dark brown bands and mask, white eye shine
                palette("", new Ramp(0xb06d3c, 0xd9935a, 0xf1bb80), new Ramp(0xd9ab76, 0xf0cf9c, 0xfbe6c4),
                        new Ramp(0x3e1d10, 0x5c2c1a, 0x7c4126), new Ramp(0x3a1a0e, 0x552716, 0x6e3820),
                        0x1e0d07, 0xf2eee6, 0x24110a, new Ramp(0x40200f, 0x613019, 0x7e4427), 0x2a160c, 0x2c150b),
                // White fur, faint beige bands, pink skin and red eyes
                palette("_albino", new Ramp(0xd8cdbd, 0xeee6da, 0xfcf9f3), new Ramp(0xe8ddcc, 0xf7f0e4, 0xfffcf6),
                        new Ramp(0xcdb9a2, 0xdccbb6, 0xeadcca), new Ramp(0xe0bfb6, 0xecd0c8, 0xf6e1dc),
                        0xc03a46, 0xffe8ea, 0xd98e8e, new Ramp(0xe4b4ac, 0xf0c8c0, 0xf8dcd6), 0xe6d3c6, 0xd8c6b2),
                // Near-black brown fur, dark bands and mask that barely stand out
                palette("_melanistic", new Ramp(0x1c130f, 0x30221b, 0x4a3529), new Ramp(0x3a2a20, 0x55402f, 0x705744),
                        new Ramp(0x070504, 0x110b09, 0x1c1410), new Ramp(0x060403, 0x0e0907, 0x18110d),
                        0x050303, 0xa8a29a, 0x060404, new Ramp(0x0d0907, 0x1a120e, 0x271c16), 0x0c0807, 0x080605)
        );
    }

    private static Palette palette(String suffix, Ramp fur, Ramp belly, Ramp stripe, Ramp mask, int eye, int eyeShine,
                                   int nose, Ramp ear, int claw, int tailTip) {
        return new Palette(suffix, Map.ofEntries(
                Map.entry(FUR, fur),
                Map.entry(BELLY, belly),
                Map.entry(STRIPE, stripe),
                Map.entry(MASK, mask),
                Map.entry(EYE, Ramp.flat(eye)),
                Map.entry(EYE_SHINE, Ramp.flat(eyeShine)),
                Map.entry(NOSE, Ramp.around(nose, 0.25)),
                Map.entry(EAR, ear),
                Map.entry(CLAW, Ramp.flat(claw)),
                Map.entry(TAIL_TIP, Ramp.around(tailTip, 0.3))
        ));
    }

    @Override
    protected void paint() {
        this.body(0, 0, 12, 15, 9);
        this.head(0, 24, 14, 9, 12);
        this.snout(54, 24, 6, 5, 5);
        this.ear(78, 24, 2, 3, 3);
        this.arm(0, 46, 3, 8, 3);
        this.haunch(14, 46, 5, 6, 6);
        this.foot(38, 46, 5, 3, 6);
        this.tail(0, 60, 2, 2, 14);
        this.eyelid(90, 24, 0, 2, 2);
    }

    /*==========*/
    /*   BODY   */
    /*==========*/

    private void body(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, FUR, 11);
        int y = v + d;
        // Cream belly and chest on the front, the sides getting lighter towards it
        this.repaint(u + d + 1, y, w - 2, h, BELLY);
        this.fillShade(u + d - 2, y, 2, h, 0.2);
        this.fillShade(u + d + w, y, 2, h, 0.2);
        // Dark bands across the back that fade out down the flanks, like the reference
        int backX = u + 2 * d + w;
        for (int band = 0; band < (h - 2) / 4 + 1; band++) {
            int row = y + 2 + band * 4;
            for (int x = 0; x < w; x++) {
                // Each band wobbles up and down a little
                int wobble = noise(x / 2, band, 17) > 0.6 ? 1 : 0;
                this.repaint(backX + x, row + wobble, 1, 2, STRIPE);
            }
            // On the sides the bands get shorter towards the belly (the back edge is col 0 on the right side)
            int reach = 3 + (int) (noise(band, 1, 19) * 3);
            for (int x = 0; x < reach; x++) {
                int thick = x < reach - 1 ? 2 : 1;
                this.repaint(u + x, row + (x > 1 ? 1 : 0), 1, thick, STRIPE);
                this.repaint(u + d + w + d - 1 - x, row + (x > 1 ? 1 : 0), 1, thick, STRIPE);
            }
        }
        // Darker line along the spine, lighter top of the shoulders
        this.fillShade(backX + w / 2 - 1, y, 2, h, -0.15);
        this.fillShade(u + d, v, w, d, 0.1);
    }

    /*==========*/
    /*   HEAD   */
    /*==========*/

    private void head(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, FUR, 31);
        int y = v + d;
        int fx = u + d;
        // Front: fur forehead, the eye mask across both corners, a pale bridge down to the snout and pale cheeks
        int maskWidth = w / 2 - 1;
        this.repaint(fx, y + 2, maskWidth, 3, MASK);
        this.repaint(fx + w - maskWidth, y + 2, maskWidth, 3, MASK);
        this.repaint(fx + maskWidth, y + 3, w - 2 * maskWidth, 2, BELLY);
        this.repaint(fx, y + 5, w, h - 5, BELLY);
        this.fillShade(fx, y + 5, w, 1, -0.15);
        // Top: lighter crown with a couple of darker blotches, like the reference
        this.fillShade(fx, v, w, d, 0.1);
        this.fillShade(fx + 2, v + 3, 3, 2, -0.3);
        this.fillShade(fx + w - 5, v + 5, 3, 2, -0.3);
        // Under the chin: cream throat
        this.repaint(fx + w, v, w, d, BELLY);
        // Sides: the mask wraps around from the front to the eyes, cream cheeks below, ears sit behind
        this.sideMask(u, y, d, h, false);
        this.sideMask(u + d + w, y, d, h, true);
    }

    /**
     * One side of the head: front edge at the right end of the right side, at the left end of the left side.
     */
    private void sideMask(int x0, int y, int d, int h, boolean frontAtLeft) {
        for (int i = 0; i < 7; i++) {
            int x = frontAtLeft ? x0 + i : x0 + d - 1 - i;
            // The band narrows towards the back of the head
            int rows = i < 5 ? 3 : 2;
            this.repaint(x, y + 2, 1, rows, MASK);
        }
        for (int i = 0; i < 6; i++) {
            int x = frontAtLeft ? x0 + i : x0 + d - 1 - i;
            this.repaint(x, y + 5, 1, h - 5, BELLY);
        }
        // Eye: dark, with a white shine towards the front like the reference
        int eyeX = frontAtLeft ? x0 + 2 : x0 + d - 4;
        this.fill(eyeX, y + 2, 2, 2, EYE);
        this.material[frontAtLeft ? eyeX : eyeX + 1][y + 2] = EYE_SHINE;
    }

    /**
     * Closed eye, laid over the painted one while the meerkat sleeps: the dark mask with a black line of lashes.
     */
    private void eyelid(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, MASK, 71);
        this.fillShade(u, v + d, 2 * d + 2 * w, 1, 0.2);
        this.fillShade(u, v + d + h - 1, 2 * d + 2 * w, 1, -0.6);
    }

    private void snout(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, BELLY, 41);
        int fx = u + d;
        int fy = v + d;
        // Dark nose on the tip: the top of the front face and the front rows of the top face
        this.fill(fx + 1, fy, w - 2, 2, NOSE);
        this.fill(fx + 1, v + d - 1, w - 2, 1, NOSE);
        this.fill(fx + 2, v + d - 2, w - 4, 1, NOSE);
        this.fillShade(fx + 2, fy, 1, 1, 0.5);
        // Mouth: a dark line in the middle of the front and along the bottom of the sides
        this.fillShade(fx + w / 2 - 1, fy + 2, 2, 1, -0.35);
        this.fillShade(fx + 1, fy + 3, w - 2, 1, -0.25);
        this.fillShade(u, fy + h - 1, d, 1, -0.3);
        this.fillShade(u + d + w, fy + h - 1, d, 1, -0.3);
        // The bridge darkens a little towards the nose
        this.fillShade(fx + 1, v, w - 2, d - 2, -0.1);
    }

    private void ear(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, EAR, 61);
        // Darker inside on the faces against the head, a lighter rim on top
        this.fillShade(u + d + w, v + d, d, h, -0.3);
        this.fillShade(u, v + d, d, 1, 0.2);
    }

    /*===========*/
    /*   LIMBS   */
    /*===========*/

    private void arm(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, FUR, 101);
        // Lighter inside of the arm, paws at the bottom with dark claws on the front
        this.repaint(u + d + w, v + d, d, h, BELLY);
        this.sideFaces(u, v, w, h, d, (fx, fy, fw, fh) -> this.fillShade(fx, fy + fh - 3, fw, 3, 0.15));
        int frontX = u + d;
        for (int x = 0; x < w; x += 2) {
            this.repaint(frontX + x, v + d + h - 1, 1, 1, CLAW);
        }
        this.fillShade(u + d + w, v, w, d, -0.3);
    }

    private void haunch(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, FUR, 113);
        // A dark band across the outer side of the haunch, like the reference
        for (int i = 0; i < d; i++) {
            this.repaint(u + i, v + d + 1 + i / 2, 1, 2, STRIPE);
        }
        // Inner side lighter
        this.fillShade(u + d + w, v + d, d, h, 0.2);
    }

    private void foot(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, FUR, 127);
        this.fillShade(u, v + d, 2 * d + 2 * w, h, 0.12);
        // Three long dark claws on the front face, a darker sole
        int frontX = u + d;
        for (int x = 0; x < w; x += 2) {
            this.repaint(frontX + x, v + d + h - 1, 1, 1, CLAW);
        }
        this.fillShade(u + d + w, v, w, d, -0.3);
    }

    private void tail(int u, int v, int w, int h, int d) {
        this.shadedBox(u, v, w, h, d, FUR, 137);
        int tip = 4;
        // The tail ends towards +z: rows near v on the top and bottom faces, the left end of the right side,
        // the right end of the left side, and the whole back face
        this.repaint(u + d, v, 2 * w, tip, TAIL_TIP);
        this.repaint(u, v + d, tip, h, TAIL_TIP);
        this.repaint(u + d + w + d - tip, v + d, tip, h, TAIL_TIP);
        this.repaint(u + 2 * d + w, v + d, w, h, TAIL_TIP);
        // Fading into the tip
        this.fillShade(u + d, v + tip, 2 * w, 2, -0.2);
        this.fillShade(u + tip, v + d, 2, h, -0.2);
        this.fillShade(u + d + w + d - tip - 2, v + d, 2, h, -0.2);
    }

    /*=========*/
    /*   EGG   */
    /*=========*/

    private static void writeEgg(File texturesDir) throws IOException {
        var image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                Integer color = EGG_COLORS.get(EGG[y].charAt(x));
                if (color != null) {
                    image.setRGB(x, y, 0xFF000000 | color);
                }
            }
        }
        File file = new File(texturesDir, "item/meerkat_spawn_egg.png");
        file.getParentFile().mkdirs();
        ImageIO.write(image, "png", file);
        System.out.println("Wrote " + file);
    }
}
