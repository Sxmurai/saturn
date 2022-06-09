package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class GuiLabel extends Gui
{
    protected int field_146167_a = 200;
    protected int field_146161_f = 20;
    public int field_146162_g;
    public int field_146174_h;
    private final List<String> field_146173_k;
    public int field_175204_i;
    private boolean centered;
    public boolean visible = true;
    private final boolean labelBgEnabled;
    private final int field_146168_n;
    private final int field_146169_o;
    private final int field_146166_p;
    private final int field_146165_q;
    private final FontRenderer fontRenderer;
    private final int field_146163_s;

    public GuiLabel(FontRenderer fontRendererObj, int p_i45540_2_, int p_i45540_3_, int p_i45540_4_, int p_i45540_5_, int p_i45540_6_, int p_i45540_7_)
    {
        fontRenderer = fontRendererObj;
        field_175204_i = p_i45540_2_;
        field_146162_g = p_i45540_3_;
        field_146174_h = p_i45540_4_;
        field_146167_a = p_i45540_5_;
        field_146161_f = p_i45540_6_;
        field_146173_k = Lists.newArrayList();
        centered = false;
        labelBgEnabled = false;
        field_146168_n = p_i45540_7_;
        field_146169_o = -1;
        field_146166_p = -1;
        field_146165_q = -1;
        field_146163_s = 0;
    }

    public void func_175202_a(String p_175202_1_)
    {
        field_146173_k.add(I18n.format(p_175202_1_));
    }

    /**
     * Sets the Label to be centered
     */
    public GuiLabel setCentered()
    {
        centered = true;
        return this;
    }

    public void drawLabel(Minecraft mc, int mouseX, int mouseY)
    {
        if (visible)
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            drawLabelBackground(mc, mouseX, mouseY);
            int i = field_146174_h + field_146161_f / 2 + field_146163_s / 2;
            int j = i - field_146173_k.size() * 10 / 2;

            for (int k = 0; k < field_146173_k.size(); ++k)
            {
                if (centered)
                {
                    drawCenteredString(fontRenderer, field_146173_k.get(k), field_146162_g + field_146167_a / 2, j + k * 10, field_146168_n);
                }
                else
                {
                    drawString(fontRenderer, field_146173_k.get(k), field_146162_g, j + k * 10, field_146168_n);
                }
            }
        }
    }

    protected void drawLabelBackground(Minecraft mcIn, int p_146160_2_, int p_146160_3_)
    {
        if (labelBgEnabled)
        {
            int i = field_146167_a + field_146163_s * 2;
            int j = field_146161_f + field_146163_s * 2;
            int k = field_146162_g - field_146163_s;
            int l = field_146174_h - field_146163_s;
            Gui.drawRect(k, l, k + i, l + j, field_146169_o);
            drawHorizontalLine(k, k + i, l, field_146166_p);
            drawHorizontalLine(k, k + i, l + j, field_146165_q);
            drawVerticalLine(k, l, l + j, field_146166_p);
            drawVerticalLine(k + i, l, l + j, field_146165_q);
        }
    }
}
