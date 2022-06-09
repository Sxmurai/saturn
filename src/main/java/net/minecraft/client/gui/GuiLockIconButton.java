package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiLockIconButton extends GuiButton
{
    private boolean field_175231_o = false;

    public GuiLockIconButton(int p_i45538_1_, int p_i45538_2_, int p_i45538_3_)
    {
        super(p_i45538_1_, p_i45538_2_, p_i45538_3_, 20, 20, "");
    }

    public boolean func_175230_c()
    {
        return field_175231_o;
    }

    public void func_175229_b(boolean p_175229_1_)
    {
        field_175231_o = p_175229_1_;
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (visible)
        {
            mc.getTextureManager().bindTexture(GuiButton.buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            boolean flag = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            GuiLockIconButton.Icon guilockiconbutton$icon;

            if (field_175231_o)
            {
                if (!enabled)
                {
                    guilockiconbutton$icon = GuiLockIconButton.Icon.LOCKED_DISABLED;
                }
                else if (flag)
                {
                    guilockiconbutton$icon = GuiLockIconButton.Icon.LOCKED_HOVER;
                }
                else
                {
                    guilockiconbutton$icon = GuiLockIconButton.Icon.LOCKED;
                }
            }
            else if (!enabled)
            {
                guilockiconbutton$icon = GuiLockIconButton.Icon.UNLOCKED_DISABLED;
            }
            else if (flag)
            {
                guilockiconbutton$icon = GuiLockIconButton.Icon.UNLOCKED_HOVER;
            }
            else
            {
                guilockiconbutton$icon = GuiLockIconButton.Icon.UNLOCKED;
            }

            drawTexturedModalRect(xPosition, yPosition, guilockiconbutton$icon.func_178910_a(), guilockiconbutton$icon.func_178912_b(), width, height);
        }
    }

    static enum Icon
    {
        LOCKED(0, 146),
        LOCKED_HOVER(0, 166),
        LOCKED_DISABLED(0, 186),
        UNLOCKED(20, 146),
        UNLOCKED_HOVER(20, 166),
        UNLOCKED_DISABLED(20, 186);

        private final int field_178914_g;
        private final int field_178920_h;

        private Icon(int p_i45537_3_, int p_i45537_4_)
        {
            field_178914_g = p_i45537_3_;
            field_178920_h = p_i45537_4_;
        }

        public int func_178910_a()
        {
            return field_178914_g;
        }

        public int func_178912_b()
        {
            return field_178920_h;
        }
    }
}
