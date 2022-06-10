package wtf.saturn.util.render.scissor;

import net.minecraft.client.gui.ScaledResolution;
import wtf.saturn.util.Globals;

import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author GiantNuker
 * <p>
 * NOT MY CODE
 */
public class ScissorStack implements Globals {
    private final LinkedList<Vec4d> stack = new LinkedList<>();

    public void scissor(double x, double y, double width, double height) {
        scissor((int) x, (int) y, (int) width, (int) height);
    }

    public void scissor(int x, int y, int width, int height) {
        ScaledResolution res = new ScaledResolution(mc);
        int scale = res.getScaleFactor();

        int sx = x * scale;
        int sy = ((res.getScaledHeight() - height) * scale);
        int sWidth = ((width - x) * scale);
        int sHeight = ((height - y) * scale);

        Vec4d s = new Vec4d(sx, sy, sWidth, sHeight);

        if (!stack.isEmpty()) {
            glPopAttrib();
            glDisable(GL_SCISSOR_TEST);

            Vec4d last = stack.getLast();

            int nx = Math.max(sx, (int) last.getX());
            int ny = Math.max(sy, (int) last.getY());

            int hDiff = sx - nx;
            int nWidth = (int) Math.min(Math.min(last.getX2() + (last.getX() - sx), last.getX2()), sWidth + hDiff);

            int diff = sy - ny;
            int nHeight = (int) Math.min(Math.min(last.getY2() + (last.getY() - sy), last.getY2()), hDiff + diff);

            s = new Vec4d(nx, ny, nWidth, nHeight);
        }

        glEnable(GL_SCISSOR_TEST);

        if (stack.isEmpty()) {
            glPushAttrib(GL_SCISSOR_BIT);
        }

        if (s.getY2() > 0.0 && s.getY2() > 0.0) {
            glScissor((int) s.getX(), (int) s.getY(), (int) s.getX2(), (int) s.getY2());
        } else {
            glScissor(0, 0, 0, 0);
        }

        stack.add(s);
    }

    public void pop() {
        if (!stack.isEmpty()) {
            glPopAttrib();
            glDisable(GL_SCISSOR_TEST);

            stack.removeLast();

            if (!stack.isEmpty()) {
                Vec4d s = stack.getLast();

                glEnable(GL_SCISSOR_TEST);
                glPushAttrib(GL_SCISSOR_BIT);
                glScissor((int) s.getX(), (int) s.getY(), (int) s.getX2(), (int) s.getY2());
            }
        }
    }
}
