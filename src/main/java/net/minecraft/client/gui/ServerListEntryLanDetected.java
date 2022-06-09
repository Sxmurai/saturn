package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.resources.I18n;

public class ServerListEntryLanDetected implements GuiListExtended.IGuiListEntry
{
    private final GuiMultiplayer field_148292_c;
    protected final Minecraft mc;
    protected final LanServerDetector.LanServer field_148291_b;
    private long field_148290_d = 0L;

    protected ServerListEntryLanDetected(GuiMultiplayer p_i45046_1_, LanServerDetector.LanServer p_i45046_2_)
    {
        field_148292_c = p_i45046_1_;
        field_148291_b = p_i45046_2_;
        mc = Minecraft.getMinecraft();
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        mc.fontRendererObj.drawString(I18n.format("lanServer.title"), x + 32 + 3, y + 1, 16777215);
        mc.fontRendererObj.drawString(field_148291_b.getServerMotd(), x + 32 + 3, y + 12, 8421504);

        if (mc.gameSettings.hideServerAddress)
        {
            mc.fontRendererObj.drawString(I18n.format("selectServer.hiddenAddress"), x + 32 + 3, y + 12 + 11, 3158064);
        }
        else
        {
            mc.fontRendererObj.drawString(field_148291_b.getServerIpPort(), x + 32 + 3, y + 12 + 11, 3158064);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control.
     */
    public boolean mousePressed(int slotIndex, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
    {
        field_148292_c.selectServer(slotIndex);

        if (Minecraft.getSystemTime() - field_148290_d < 250L)
        {
            field_148292_c.connectToSelected();
        }

        field_148290_d = Minecraft.getSystemTime();
        return false;
    }

    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }

    public LanServerDetector.LanServer getLanServer()
    {
        return field_148291_b;
    }
}
