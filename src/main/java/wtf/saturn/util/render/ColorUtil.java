package wtf.saturn.util.render;

/**
 * A utility to help with coloring with renders
 *
 * @author aesthetical
 * @since 6/9/22
 */
public class ColorUtil {
    public static float[] fromRGBA(int color) {
        float alpha = (color >> 24 & 0xff) / 255f;
        float red = (color >> 16 & 0xff) / 255f;
        float green = (color >> 8 & 0xff) / 255f;
        float blue = (color & 0xff) / 255f;

        return new float[] { red, green, blue, alpha };
    }


}
