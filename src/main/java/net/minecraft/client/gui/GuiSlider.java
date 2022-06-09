package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class GuiSlider extends GuiButton
{
    private float sliderPosition = 1.0F;
    public boolean isMouseDown;
    private final String name;
    private final float min;
    private final float max;
    private final GuiPageButtonList.GuiResponder responder;
    private final GuiSlider.FormatHelper formatHelper;

    public GuiSlider(GuiPageButtonList.GuiResponder guiResponder, int idIn, int x, int y, String name, float min, float max, float defaultValue, GuiSlider.FormatHelper formatter)
    {
        super(idIn, x, y, 150, 20, "");
        this.name = name;
        this.min = min;
        this.max = max;
        sliderPosition = (defaultValue - min) / (max - min);
        formatHelper = formatter;
        responder = guiResponder;
        displayString = getDisplayString();
    }

    public float func_175220_c()
    {
        return min + (max - min) * sliderPosition;
    }

    public void func_175218_a(float p_175218_1_, boolean p_175218_2_)
    {
        sliderPosition = (p_175218_1_ - min) / (max - min);
        displayString = getDisplayString();

        if (p_175218_2_)
        {
            responder.onTick(id, func_175220_c());
        }
    }

    public float func_175217_d()
    {
        return sliderPosition;
    }

    private String getDisplayString()
    {
        return formatHelper == null ? I18n.format(name) + ": " + func_175220_c() : formatHelper.getText(id, I18n.format(name), func_175220_c());
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver)
    {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
        if (visible)
        {
            if (isMouseDown)
            {
                sliderPosition = (float)(mouseX - (xPosition + 4)) / (float)(width - 8);

                if (sliderPosition < 0.0F)
                {
                    sliderPosition = 0.0F;
                }

                if (sliderPosition > 1.0F)
                {
                    sliderPosition = 1.0F;
                }

                displayString = getDisplayString();
                responder.onTick(id, func_175220_c());
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexturedModalRect(xPosition + (int)(sliderPosition * (float)(width - 8)), yPosition, 0, 66, 4, 20);
            drawTexturedModalRect(xPosition + (int)(sliderPosition * (float)(width - 8)) + 4, yPosition, 196, 66, 4, 20);
        }
    }

    public void func_175219_a(float p_175219_1_)
    {
        sliderPosition = p_175219_1_;
        displayString = getDisplayString();
        responder.onTick(id, func_175220_c());
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            sliderPosition = (float)(mouseX - (xPosition + 4)) / (float)(width - 8);

            if (sliderPosition < 0.0F)
            {
                sliderPosition = 0.0F;
            }

            if (sliderPosition > 1.0F)
            {
                sliderPosition = 1.0F;
            }

            displayString = getDisplayString();
            responder.onTick(id, func_175220_c());
            isMouseDown = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY)
    {
        isMouseDown = false;
    }

    public interface FormatHelper
    {
        String getText(int id, String name, float value);
    }
}
