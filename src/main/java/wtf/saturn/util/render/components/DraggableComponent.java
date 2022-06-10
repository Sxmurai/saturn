package wtf.saturn.util.render.components;

import lombok.Getter;

@Getter
public abstract class DraggableComponent extends Component {
    private double dragX, dragY;
    private boolean dragState = false;

    @Override
    public void drawComponent(int mouseX, int mouseY, float partialTicks) {
        super.drawComponent(mouseX, mouseY, partialTicks);

        if (dragState) {
            setX(mouseX + dragX);
            setY(mouseY + dragY);
        }
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        if (button == 0) {
            dragState = true;

            dragX = getX() - mouseX;
            dragY = getY() - mouseY;
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int state) {
        if (dragState && state == 0) {
            dragState = false;
        }
    }
}
