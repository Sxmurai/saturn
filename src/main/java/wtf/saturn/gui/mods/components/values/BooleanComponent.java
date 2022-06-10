package wtf.saturn.gui.mods.components.values;

import wtf.saturn.feature.impl.setting.Setting;
import wtf.saturn.util.render.RenderUtil;
import wtf.saturn.util.render.components.Component;

public class BooleanComponent extends Component {
    private final Setting<Boolean> setting;

    public BooleanComponent(Setting<Boolean> setting) {
        this.setting = setting;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY, float partialTicks) {
        super.drawComponent(mouseX, mouseY, partialTicks);

        RenderUtil.drawRect(getX() + 2.3, getY(), 10.0, 10.0, setting.getValue() ? 0x8979A3E8 : 0x56000000);

        double textY = getY() + (10.0 / 2.0) - (mc.fontRendererObj.FONT_HEIGHT / 2.0);
        mc.fontRendererObj.drawString(setting.getName(), (int) (getX() + 20.3), (int) textY, -1);
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        if (isInBounds(mouseX, mouseY, getX() + 2.3, getY(), 10.0, 10.0) && button == 0) {
            setting.setValue(!setting.getValue());
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int state) {

    }

    @Override
    public void mouseScroll(int mouseX, int mouseY, int scroll) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public boolean isVisible() {
        return setting.getVisibility();
    }
}
