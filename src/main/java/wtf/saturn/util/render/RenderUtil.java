package wtf.saturn.util.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import static org.lwjgl.opengl.GL11.*;

/**
 * A utility for rendering things onto the screen
 *
 * @author aesthetical
 * @since 6/9/22
 */
public class RenderUtil {

    /**
     * Draws a rectangle
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param color the color of the rectangle
     */
    public static void drawRect(double x, double y, double width, double height, int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(771, 770, 0, 1);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getWorldRenderer();

        float[] colorArr = ColorUtil.fromRGBA(color);

        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, 0.0).color(colorArr[0], colorArr[1], colorArr[2], colorArr[3]).endVertex();
        buffer.pos(x, y + height, 0.0).color(colorArr[0], colorArr[1], colorArr[2], colorArr[3]).endVertex();
        buffer.pos(x + width, y + height, 0.0).color(colorArr[0], colorArr[1], colorArr[2], colorArr[3]).endVertex();
        buffer.pos(x + width, y, 0.0).color(colorArr[0], colorArr[1], colorArr[2], colorArr[3]).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    /**
     *
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param radius
     * @param color
     */
    public static void drawRoundedRect(double x, double y, double width, double height, double radius, int color) {

    }

    public static void drawCircle(double x, double y, double radius, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getWorldRenderer();

        float[] colorArr = ColorUtil.fromRGBA(color);

        GlStateManager.disableTexture2D();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(771, 770, 0, 1);

        glEnable(GL_LINE_SMOOTH);

        buffer.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

        for (double angle = 0.0; angle < 2.0 * Math.PI; angle += 0.1) {
            buffer
                    .pos(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0.0)
                    .color(colorArr[0], colorArr[1], colorArr[2], colorArr[3])
                    .endVertex();
        }

        tessellator.draw();

        glDisable(GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    public static void drawLine(double x, double y, double width, double height, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getWorldRenderer();

        float[] colorArr = ColorUtil.fromRGBA(color);

        GlStateManager.disableTexture2D();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(771, 770, 0, 1);
        glLineWidth((float) height);

        buffer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, 0.0).color(colorArr[0], colorArr[1], colorArr[2], colorArr[3]).endVertex();
        buffer.pos(x + width, y, 0.0).color(colorArr[0], colorArr[1], colorArr[2], colorArr[3]).endVertex();
        tessellator.draw();

        glLineWidth(1.0f);

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }
}
