package wtf.saturn.util.render.components;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lwjgl.input.Mouse;
import wtf.saturn.util.Globals;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public abstract class Component implements Globals {
    private double x, y;
    private double width, height;

    protected final List<Component> components = new ArrayList<>();

    public Component(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public Component(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void drawComponent(int mouseX, int mouseY, float partialTicks) {
        int scroll = Mouse.getDWheel();
        if (scroll != 0) {
            mouseScroll(mouseX, mouseY, scroll);
        }
    }

    public abstract void mouseClick(int mouseX, int mouseY, int button);
    public abstract void mouseRelease(int mouseX, int mouseY, int state);
    public abstract void mouseScroll(int mouseX, int mouseY, int scroll);
    public abstract void keyTyped(char typedChar, int keyCode);

    public boolean isInBounds(int mouseX, int mouseY) {
        return isInBounds(mouseX, mouseY, x, y, width, height);
    }

    public static boolean isInBounds(int mouseX, int mouseY, double x, double y, double w, double h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public boolean isVisible() {
        return true;
    }
}
