package wtf.saturn.gui.mods.components;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.EnumChatFormatting;
import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.impl.setting.Bind;
import wtf.saturn.gui.mods.components.values.BooleanComponent;
import wtf.saturn.gui.mods.components.values.SliderComponent;
import wtf.saturn.util.render.RenderUtil;
import wtf.saturn.util.render.components.Component;

import java.awt.*;

@Getter @Setter
public class ModComponent extends Component {
    private boolean selected;
    private final Module module;

    public ModComponent(Module module) {
        this.module = module;

        module.getSettings().forEach((setting) -> {
            if (setting instanceof Bind) {

            } else {
                if (setting.getValue() instanceof Boolean) {
                    getComponents().add(new BooleanComponent(setting));
                } else if (setting.getValue() instanceof Number) {
                    getComponents().add(new SliderComponent(setting));
                }
            }
        });
    }

    @Override
    public void drawComponent(int mouseX, int mouseY, float partialTicks) {
        super.drawComponent(mouseX, mouseY, partialTicks);


        if (!selected) {
            RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x56000000);

            int width = mc.fontRendererObj.getStringWidth(module.getName());
            mc.fontRendererObj.drawString(module.getName(), (int) (getX() + (getWidth() / 2.0) - width / 2.0), (int) (getY() + getHeight() - 12), module.isToggled() ? Color.green.getRGB() : -1);
        } else {
            mc.fontRendererObj.drawString(module.getName(), (int) (getX() + 2.3), (int) (getY() + 2.0), module.isToggled() ? Color.green.getRGB() : -1);
            mc.fontRendererObj.drawString(EnumChatFormatting.GRAY + module.getDescription(), (int) (getX() + 2.3), (int) (getY() + mc.fontRendererObj.FONT_HEIGHT + 3.0), module.isToggled() ? Color.green.getRGB() : -1);

            double y = getY() + (mc.fontRendererObj.FONT_HEIGHT * 2.0) + 10.0;
            for (Component component : getComponents()) {
                component.setX(getX() + 2.3);
                component.setY(y);
                component.setWidth(getWidth());
                component.setHeight(10.0);

                component.drawComponent(mouseX, mouseY, partialTicks);

                y += 16.0;
            }
        }
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        if (selected) {
            getComponents().forEach((c) -> c.mouseClick(mouseX, mouseY, button));
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int state) {
        if (selected) {
            getComponents().forEach((c) -> c.mouseRelease(mouseX, mouseY, state));
        }
    }

    @Override
    public void mouseScroll(int mouseX, int mouseY, int scroll) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (selected) {
            getComponents().forEach((c) -> c.keyTyped(typedChar, keyCode));
        }
    }
}
