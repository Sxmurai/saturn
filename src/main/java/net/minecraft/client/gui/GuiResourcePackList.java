package net.minecraft.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.ResourcePackListEntry;
import net.minecraft.util.EnumChatFormatting;

public abstract class GuiResourcePackList extends GuiListExtended
{
    protected final Minecraft mc;
    protected final List<ResourcePackListEntry> field_148204_l;

    public GuiResourcePackList(Minecraft mcIn, int p_i45055_2_, int p_i45055_3_, List<ResourcePackListEntry> p_i45055_4_)
    {
        super(mcIn, p_i45055_2_, p_i45055_3_, 32, p_i45055_3_ - 55 + 4, 36);
        mc = mcIn;
        field_148204_l = p_i45055_4_;
        field_148163_i = false;
        setHasListHeader(true, (int)((float)mcIn.fontRendererObj.FONT_HEIGHT * 1.5F));
    }

    /**
     * Handles drawing a list's header row.
     */
    protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_)
    {
        String s = EnumChatFormatting.UNDERLINE + "" + EnumChatFormatting.BOLD + getListHeader();
        mc.fontRendererObj.drawString(s, p_148129_1_ + width / 2 - mc.fontRendererObj.getStringWidth(s) / 2, Math.min(top + 3, p_148129_2_), 16777215);
    }

    protected abstract String getListHeader();

    public List<ResourcePackListEntry> getList()
    {
        return field_148204_l;
    }

    protected int getSize()
    {
        return getList().size();
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public ResourcePackListEntry getListEntry(int index)
    {
        return getList().get(index);
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth()
    {
        return width;
    }

    protected int getScrollBarX()
    {
        return right - 6;
    }
}
