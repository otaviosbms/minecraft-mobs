import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Paints the 16x16 feces item texture: a swirled pile in three tiers with a tip, dark outline, a lighter ridge on
 * top of each tier and a highlight, in the same style as the spawn eggs of {@link MobTexture}.
 * <p>
 * Run from the project root with the project JDK:
 * <pre>java tools/textures/FecesTexture.java src/client/resources/assets/mobmod/textures</pre>
 */
public class FecesTexture {
    private static final int OUTLINE = 0x24160c;
    private static final int DARK = 0x3d2814;
    private static final int BASE = 0x5c3d1f;
    private static final int LIGHT = 0x7d5a32;
    private static final int SHINE = 0xa07a4a;
    private static final int SEED = 23;

    /**
     * Each tier as {top row, bottom row, left column, right column}, from the tip down to the base.
     */
    private static final int[][] TIERS = {
            {2, 4, 6, 9},
            {5, 7, 4, 11},
            {8, 10, 3, 12},
            {11, 13, 1, 14},
    };

    public static void main(String[] args) throws IOException {
        File texturesDir = new File(args.length > 0 ? args[0] : ".");
        var image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        boolean[][] inside = new boolean[16][16];
        int[][] tierOf = new int[16][16];
        for (int t = 0; t < TIERS.length; t++) {
            int[] tier = TIERS[t];
            for (int y = tier[0]; y <= tier[1]; y++) {
                for (int x = tier[2]; x <= tier[3]; x++) {
                    // Round the corners of each tier
                    boolean corner = (y == tier[0] || y == tier[1]) && (x == tier[2] || x == tier[3]);
                    if (!corner) {
                        inside[x][y] = true;
                        tierOf[x][y] = t;
                    }
                }
            }
        }
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (!inside[x][y]) {
                    continue;
                }
                boolean edge = x == 0 || y == 0 || x == 15 || y == 15 || !inside[x - 1][y] || !inside[x + 1][y] || !inside[x][y - 1] || !inside[x][y + 1];
                int color;
                if (edge && (y == 15 || !inside[x][y + 1] || !inside[x - 1][y] || !inside[x + 1][y])) {
                    color = OUTLINE;
                } else {
                    int[] tier = TIERS[tierOf[x][y]];
                    // Lighter ridge on top of each tier, darker crease at its bottom: the swirl
                    double shade = (MobTexture.noise(x, y, SEED) - 0.5) * 0.35;
                    if (y == tier[0]) {
                        shade += 0.45;
                    } else if (y == tier[1]) {
                        shade -= 0.4;
                    }
                    // Light comes from the upper left
                    shade += (8.0 - x) * 0.04;
                    shade = Math.max(-1.0, Math.min(1.0, shade));
                    color = shade < 0 ? MobTexture.lerp(BASE, DARK, -shade) : MobTexture.lerp(BASE, LIGHT, shade);
                }
                image.setRGB(x, y, 0xFF000000 | color);
            }
        }
        // Glossy highlights on the left of the upper tiers
        image.setRGB(7, 3, 0xFF000000 | SHINE);
        image.setRGB(6, 5, 0xFF000000 | SHINE);
        image.setRGB(4, 8, 0xFF000000 | SHINE);

        File file = new File(texturesDir, "item/feces.png");
        file.getParentFile().mkdirs();
        ImageIO.write(image, "png", file);
        System.out.println("Wrote " + file);
    }
}
