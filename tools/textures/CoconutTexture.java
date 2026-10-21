import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Paints the 16x16 coconut item texture: a round brown husk with fibers, the three "eyes" on top, a dark outline and
 * a highlight, in the same style as the spawn eggs of {@link MobTexture}.
 * <p>
 * Run from the project root with the project JDK:
 * <pre>java tools/textures/CoconutTexture.java src/client/resources/assets/mobmod/textures</pre>
 */
public class CoconutTexture {
    private static final int OUTLINE = 0x2a1b0f;
    private static final int DARK = 0x4a3220;
    private static final int BASE = 0x6e4c2e;
    private static final int LIGHT = 0x93704a;
    private static final int EYE = 0x24160b;
    private static final int SEED = 17;

    public static void main(String[] args) throws IOException {
        File texturesDir = new File(args.length > 0 ? args[0] : ".");
        var image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        boolean[][] inside = new boolean[16][16];
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                // Slightly oval, wider than tall
                double dx = (x + 0.5 - 8.0) / 6.2;
                double dy = (y + 0.5 - 8.5) / 5.8;
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
                    color = OUTLINE;
                } else {
                    // Husk fibers: short diagonal strands, lighter on the upper left (light from above, like vanilla items)
                    double fiber = MobTexture.noise((x + y) / 2, x - y, SEED);
                    double light = (8.5 - y) * 0.05 + (8.0 - x) * 0.03;
                    double shade = (MobTexture.noise(x, y, SEED + 1) - 0.5) * 0.4 + light + (fiber > 0.8 ? 0.35 : fiber < 0.15 ? -0.35 : 0.0);
                    shade = Math.max(-1.0, Math.min(1.0, shade));
                    color = shade < 0 ? MobTexture.lerp(BASE, DARK, -shade) : MobTexture.lerp(BASE, LIGHT, shade);
                }
                image.setRGB(x, y, 0xFF000000 | color);
            }
        }
        // The three eyes of the coconut, on the upper part
        image.setRGB(6, 5, 0xFF000000 | EYE);
        image.setRGB(9, 5, 0xFF000000 | EYE);
        image.setRGB(8, 7, 0xFF000000 | EYE);
        // Highlight
        image.setRGB(4, 6, 0xFF000000 | LIGHT);
        image.setRGB(5, 4, 0xFF000000 | 0xb08a5e);

        File file = new File(texturesDir, "item/coconut.png");
        file.getParentFile().mkdirs();
        ImageIO.write(image, "png", file);
        System.out.println("Wrote " + file);
    }
}
