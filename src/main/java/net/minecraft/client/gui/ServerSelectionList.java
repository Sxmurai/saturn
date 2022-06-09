package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;

public class ServerSelectionList extends GuiListExtended
{
    private final GuiMultiplayer owner;
    private final List<ServerListEntryNormal> field_148198_l = Lists.newArrayList();
    private final List<ServerListEntryLanDetected> field_148199_m = Lists.newArrayList();
    private final GuiListExtended.IGuiListEntry lanScanEntry = new ServerListEntryLanScan();
    private int selectedSlotIndex = -1;

    public ServerSelectionList(GuiMultiplayer ownerIn, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        owner = ownerIn;
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public GuiListExtended.IGuiListEntry getListEntry(int index)
    {
        if (index < field_148198_l.size())
        {
            return field_148198_l.get(index);
        }
        else
        {
            index = index - field_148198_l.size();

            if (index == 0)
            {
                return lanScanEntry;
            }
            else
            {
                --index;
                return field_148199_m.get(index);
            }
        }
    }

    protected int getSize()
    {
        return field_148198_l.size() + 1 + field_148199_m.size();
    }

    public void setSelectedSlotIndex(int selectedSlotIndexIn)
    {
        selectedSlotIndex = selectedSlotIndexIn;
    }

    /**
     * Returns true if the element passed in is currently selected
     */
    protected boolean isSelected(int slotIndex)
    {
        return slotIndex == selectedSlotIndex;
    }

    public int func_148193_k()
    {
        return selectedSlotIndex;
    }

    public void func_148195_a(ServerList p_148195_1_)
    {
        field_148198_l.clear();

        for (int i = 0; i < p_148195_1_.countServers(); ++i)
        {
            field_148198_l.add(new ServerListEntryNormal(owner, p_148195_1_.getServerData(i)));
        }
    }

    public void func_148194_a(List<LanServerDetector.LanServer> p_148194_1_)
    {
        field_148199_m.clear();

        for (LanServerDetector.LanServer lanserverdetector$lanserver : p_148194_1_)
        {
            field_148199_m.add(new ServerListEntryLanDetected(owner, lanserverdetector$lanserver));
        }
    }

    protected int getScrollBarX()
    {
        return super.getScrollBarX() + 30;
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth()
    {
        return super.getListWidth() + 85;
    }
}
