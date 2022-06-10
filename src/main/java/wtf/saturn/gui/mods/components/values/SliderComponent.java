package wtf.saturn.gui.mods.components.values;

import org.lwjgl.input.Mouse;
import wtf.saturn.feature.impl.setting.Setting;
import wtf.saturn.util.render.RenderUtil;
import wtf.saturn.util.render.components.Component;

public class SliderComponent extends Component {
    private final Setting<Number> setting;
    private final float difference;
    private boolean dragging = false;

    private int textWidth;

    public SliderComponent(Setting<Number> setting) {
        this.setting = setting;
        difference = setting.getMax().floatValue() - setting.getMin().floatValue();
    }

    @Override
    public void drawComponent(int mouseX, int mouseY, float partialTicks) {
        textWidth = mc.fontRendererObj.getStringWidth(setting.getName());

        double barWidth = setting.getValue().floatValue() <= setting.getMin().floatValue() ? 0.0 : (getWidth() / 2.0) * partialMultiplier();

        double textY = getY() + (getHeight() / 2.0) - (mc.fontRendererObj.FONT_HEIGHT / 2.0);
        mc.fontRendererObj.drawString("" + setting.getName(), (int) (getX() + 2.3), (int) textY, -1);

        RenderUtil.drawLine(getX() + 2.3 + textWidth + 4.0, textY, barWidth, 2.0, 0x8979A3E8);
        RenderUtil.drawCircle((getX() + 2.3 + textWidth + 4.0 + barWidth) - 1.0, textY, 2.0, 0x8979A3E8);

        if (dragging) {
            if (!isInBounds(mouseX, mouseY, getX() + 2.3 + textWidth + 4.0, getY(), getWidth(), getHeight())) {
                dragging = false;
                return;
            }

            float percent = (float) ((mouseX - getX()) / (getWidth() / 2.0));

            if (setting.getValue() instanceof Float) {
                float result = setting.getMin().floatValue() + difference * percent;
                setting.setValue(Math.round(10.0f * result) / 10.0f);
            } else if (setting.getValue() instanceof Double) {
                double result = setting.getMin().doubleValue() + difference * percent;
                setting.setValue(Math.round(10.0 * result) / 10.0);
            } else {
                setting.setValue(Math.round(setting.getMin().intValue() + difference * percent));
            }
        }
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        if (isInBounds(mouseX, mouseY, getX() + 2.3 + textWidth + 4.0, getY(), getWidth(), getHeight()) && button == 0) {
            dragging = true;
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int state) {
        if (dragging && state == 0) {
            dragging = false;
        }
    }

    @Override
    public void mouseScroll(int mouseX, int mouseY, int scroll) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    private float part() {
        return setting.getValue().floatValue() - setting.getMin().floatValue();
    }

    private float partialMultiplier() {
        return part() / difference;
    }
}
