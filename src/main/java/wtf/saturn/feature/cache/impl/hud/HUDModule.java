package wtf.saturn.feature.cache.impl.hud;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;
import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;

@Getter @Setter
public abstract class HUDModule extends Module {
    private double x, y;
    private double width, height;

    private boolean dragging = false;
    private double dragX, dragY;

    public HUDModule(String name, String description) {
        super(name, ModuleCategory.HUD, description);
    }

    public abstract void render(ScaledResolution res);

    public void setDraggingPos(int mouseX, int mouseY) {
        if (dragging) {
            setX(mouseX + dragX);
            setY(mouseY + dragY);
        }
    }

    public void mouseClick(int mouseX, int mouseY, int button) {
        if (button == 0) {
            dragging = true;

            dragX = getX() - mouseX;
            dragY = getY() - mouseY;
        }
    }

    public void mouseRelease(int mouseX, int mouseY, int state) {
        if (dragging && state == 0) {
            dragging = false;
        }
    }
}
