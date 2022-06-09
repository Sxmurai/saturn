package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class GuiScreenOptionsSounds extends GuiScreen
{
    private final GuiScreen field_146505_f;

    /** Reference to the GameSettings object. */
    private final GameSettings game_settings_4;
    protected String field_146507_a = "Options";
    private String field_146508_h;

    public GuiScreenOptionsSounds(GuiScreen p_i45025_1_, GameSettings p_i45025_2_)
    {
        field_146505_f = p_i45025_1_;
        game_settings_4 = p_i45025_2_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        int i = 0;
        field_146507_a = I18n.format("options.sounds.title");
        field_146508_h = I18n.format("options.off");
        buttonList.add(new GuiScreenOptionsSounds.Button(SoundCategory.MASTER.getCategoryId(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), SoundCategory.MASTER, true));
        i = i + 2;

        for (SoundCategory soundcategory : SoundCategory.values())
        {
            if (soundcategory != SoundCategory.MASTER)
            {
                buttonList.add(new GuiScreenOptionsSounds.Button(soundcategory.getCategoryId(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), soundcategory, false));
                ++i;
            }
        }

        buttonList.add(new GuiButton(200, width / 2 - 100, height / 6 + 168, I18n.format("gui.done")));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 200)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(field_146505_f);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, field_146507_a, width / 2, 15, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected String getSoundVolume(SoundCategory p_146504_1_)
    {
        float f = game_settings_4.getSoundLevel(p_146504_1_);
        return f == 0.0F ? field_146508_h : (int)(f * 100.0F) + "%";
    }

    class Button extends GuiButton
    {
        private final SoundCategory field_146153_r;
        private final String field_146152_s;
        public float field_146156_o = 1.0F;
        public boolean field_146155_p;

        public Button(int p_i45024_2_, int p_i45024_3_, int p_i45024_4_, SoundCategory p_i45024_5_, boolean p_i45024_6_)
        {
            super(p_i45024_2_, p_i45024_3_, p_i45024_4_, p_i45024_6_ ? 310 : 150, 20, "");
            field_146153_r = p_i45024_5_;
            field_146152_s = I18n.format("soundCategory." + p_i45024_5_.getCategoryName());
            displayString = field_146152_s + ": " + getSoundVolume(p_i45024_5_);
            field_146156_o = game_settings_4.getSoundLevel(p_i45024_5_);
        }

        protected int getHoverState(boolean mouseOver)
        {
            return 0;
        }

        protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
        {
            if (visible)
            {
                if (field_146155_p)
                {
                    field_146156_o = (float)(mouseX - (xPosition + 4)) / (float)(width - 8);
                    field_146156_o = MathHelper.clamp_float(field_146156_o, 0.0F, 1.0F);
                    mc.gameSettings.setSoundLevel(field_146153_r, field_146156_o);
                    mc.gameSettings.saveOptions();
                    displayString = field_146152_s + ": " + getSoundVolume(field_146153_r);
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                drawTexturedModalRect(xPosition + (int)(field_146156_o * (float)(width - 8)), yPosition, 0, 66, 4, 20);
                drawTexturedModalRect(xPosition + (int)(field_146156_o * (float)(width - 8)) + 4, yPosition, 196, 66, 4, 20);
            }
        }

        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
        {
            if (super.mousePressed(mc, mouseX, mouseY))
            {
                field_146156_o = (float)(mouseX - (xPosition + 4)) / (float)(width - 8);
                field_146156_o = MathHelper.clamp_float(field_146156_o, 0.0F, 1.0F);
                mc.gameSettings.setSoundLevel(field_146153_r, field_146156_o);
                mc.gameSettings.saveOptions();
                displayString = field_146152_s + ": " + getSoundVolume(field_146153_r);
                field_146155_p = true;
                return true;
            }
            else
            {
                return false;
            }
        }

        public void playPressSound(SoundHandler soundHandlerIn)
        {
        }

        public void mouseReleased(int mouseX, int mouseY)
        {
            if (field_146155_p)
            {
                if (field_146153_r == SoundCategory.MASTER)
                {
                    float f = 1.0F;
                }
                else
                {
                    game_settings_4.getSoundLevel(field_146153_r);
                }

                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            }

            field_146155_p = false;
        }
    }
}
