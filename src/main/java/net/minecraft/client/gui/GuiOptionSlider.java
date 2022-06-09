package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;

public class GuiOptionSlider extends GuiButton
{
    private float sliderValue;
    public boolean dragging;
    private final GameSettings.Options options;
    private final float field_146132_r;
    private final float field_146131_s;

    public GuiOptionSlider(int p_i45016_1_, int p_i45016_2_, int p_i45016_3_, GameSettings.Options p_i45016_4_)
    {
        this(p_i45016_1_, p_i45016_2_, p_i45016_3_, p_i45016_4_, 0.0F, 1.0F);
    }

    public GuiOptionSlider(int p_i45017_1_, int p_i45017_2_, int p_i45017_3_, GameSettings.Options p_i45017_4_, float p_i45017_5_, float p_i45017_6_)
    {
        super(p_i45017_1_, p_i45017_2_, p_i45017_3_, 150, 20, "");
        sliderValue = 1.0F;
        options = p_i45017_4_;
        field_146132_r = p_i45017_5_;
        field_146131_s = p_i45017_6_;
        Minecraft minecraft = Minecraft.getMinecraft();
        sliderValue = p_i45017_4_.normalizeValue(minecraft.gameSettings.getOptionFloatValue(p_i45017_4_));
        displayString = minecraft.gameSettings.getKeyBinding(p_i45017_4_);
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
            if (dragging)
            {
                sliderValue = (float)(mouseX - (xPosition + 4)) / (float)(width - 8);
                sliderValue = MathHelper.clamp_float(sliderValue, 0.0F, 1.0F);
                float f = options.denormalizeValue(sliderValue);
                mc.gameSettings.setOptionFloatValue(options, f);
                sliderValue = options.normalizeValue(f);
                displayString = mc.gameSettings.getKeyBinding(options);
            }

            mc.getTextureManager().bindTexture(GuiButton.buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexturedModalRect(xPosition + (int)(sliderValue * (float)(width - 8)), yPosition, 0, 66, 4, 20);
            drawTexturedModalRect(xPosition + (int)(sliderValue * (float)(width - 8)) + 4, yPosition, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            sliderValue = (float)(mouseX - (xPosition + 4)) / (float)(width - 8);
            sliderValue = MathHelper.clamp_float(sliderValue, 0.0F, 1.0F);
            mc.gameSettings.setOptionFloatValue(options, options.denormalizeValue(sliderValue));
            displayString = mc.gameSettings.getKeyBinding(options);
            dragging = true;
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
        dragging = false;
    }
}
