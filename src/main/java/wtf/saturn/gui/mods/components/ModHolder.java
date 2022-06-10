package wtf.saturn.gui.mods.components;

import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import wtf.saturn.feature.cache.impl.module.ModuleCache;
import wtf.saturn.util.render.RenderUtil;
import wtf.saturn.util.render.components.Component;
import wtf.saturn.util.render.scissor.ScissorStack;

public class ModHolder extends Component {
    private double scrollOffset = 0;

    private final ScissorStack scissorStack = new ScissorStack();
    private ModComponent selected = null;

    public ModHolder(double width, double height) {
        super(width, height);
        ModuleCache.get().getModules()
                .forEach((module) -> getComponents().add(new ModComponent(module)));
    }

    @Override
    public void drawComponent(int mouseX, int mouseY, float partialTicks) {
        super.drawComponent(mouseX, mouseY, partialTicks);

        ScaledResolution res = new ScaledResolution(mc);

        setX(res.getScaledWidth() / 2.0 - (getWidth() / 2.0));
        setY(res.getScaledHeight() / 2.0 - (getHeight() / 2.0));

        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x56000000);

        scissorStack.scissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());

        double widthPerMod = (getWidth() / 3.0) - 6.0;

        double x = getX() + 4.0;
        double y = getY() + 2.0 + scrollOffset;

        if (selected == null) {
            for (Component component : getComponents()) {
                if (x + widthPerMod + 4.0 > getX() + getWidth()) {
                    x = getX() + 4.0;
                    y += 69.0;
                }

                component.setX(x);
                component.setY(y);
                component.setWidth(widthPerMod);
                component.setHeight(65.0);

                component.drawComponent(mouseX, mouseY, partialTicks);

                x += widthPerMod + 4.0;
            }
        } else {
            selected.setX(x);
            selected.setY(y);
            selected.setWidth(getWidth());
            selected.setHeight(getHeight());

            selected.drawComponent(mouseX, mouseY, partialTicks);
        }

        scissorStack.pop();
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        if (selected != null) {
            selected.mouseClick(mouseX, mouseY, button);
            return;
        }

        if (isInBounds(mouseX, mouseY)) {
            for (Component component : getComponents()) {
                if (component.isInBounds(mouseX, mouseY)) {

                    ModComponent comp = (ModComponent) component;

                    if (button == 0) {
                        comp.getModule().setState(!comp.getModule().isToggled());
                    } else {
                        select(comp);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int state) {
        if (selected != null) {
            selected.mouseRelease(mouseX, mouseY, state);
        }
    }

    @Override
    public void mouseScroll(int mouseX, int mouseY, int scroll) {
        if (scroll > 0) {
            scrollOffset += 10.0;
        } else if (scroll < 0) {
            scrollOffset -= 10.0;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_BACK && selected != null) {
            unselect();
        }
    }

    public void select(ModComponent component) {
        if (selected != null) {
            selected.setSelected(false);
        }

        selected = component;
        selected.setSelected(true);
    }

    public void unselect() {
        if (selected != null) {
            selected.setSelected(false);
            selected = null;
        }
    }
}
