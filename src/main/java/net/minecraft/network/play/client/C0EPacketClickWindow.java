package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C0EPacketClickWindow implements Packet<INetHandlerPlayServer>
{
    /** The id of the window which was clicked. 0 for player inventory. */
    private int windowId;

    /** Id of the clicked slot */
    private int slotId;

    /** Button used */
    private int usedButton;

    /** A unique number for the action, used for transaction handling */
    private short actionNumber;

    /** The item stack present in the slot */
    private ItemStack clickedItem;

    /** Inventory operation mode */
    private int mode;

    public C0EPacketClickWindow()
    {
    }

    public C0EPacketClickWindow(int windowId, int slotId, int usedButton, int mode, ItemStack clickedItem, short actionNumber)
    {
        this.windowId = windowId;
        this.slotId = slotId;
        this.usedButton = usedButton;
        this.clickedItem = clickedItem != null ? clickedItem.copy() : null;
        this.actionNumber = actionNumber;
        this.mode = mode;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processClickWindow(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        windowId = buf.readByte();
        slotId = buf.readShort();
        usedButton = buf.readByte();
        actionNumber = buf.readShort();
        mode = buf.readByte();
        clickedItem = buf.readItemStackFromBuffer();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeByte(windowId);
        buf.writeShort(slotId);
        buf.writeByte(usedButton);
        buf.writeShort(actionNumber);
        buf.writeByte(mode);
        buf.writeItemStackToBuffer(clickedItem);
    }

    public int getWindowId()
    {
        return windowId;
    }

    public int getSlotId()
    {
        return slotId;
    }

    public int getUsedButton()
    {
        return usedButton;
    }

    public short getActionNumber()
    {
        return actionNumber;
    }

    public ItemStack getClickedItem()
    {
        return clickedItem;
    }

    public int getMode()
    {
        return mode;
    }
}
