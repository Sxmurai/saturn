package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IChatComponent;

public class S2DPacketOpenWindow implements Packet<INetHandlerPlayClient>
{
    private int windowId;
    private String inventoryType;
    private IChatComponent windowTitle;
    private int slotCount;
    private int entityId;

    public S2DPacketOpenWindow()
    {
    }

    public S2DPacketOpenWindow(int incomingWindowId, String incomingWindowTitle, IChatComponent windowTitleIn)
    {
        this(incomingWindowId, incomingWindowTitle, windowTitleIn, 0);
    }

    public S2DPacketOpenWindow(int windowIdIn, String guiId, IChatComponent windowTitleIn, int slotCountIn)
    {
        windowId = windowIdIn;
        inventoryType = guiId;
        windowTitle = windowTitleIn;
        slotCount = slotCountIn;
    }

    public S2DPacketOpenWindow(int windowIdIn, String guiId, IChatComponent windowTitleIn, int slotCountIn, int incomingEntityId)
    {
        this(windowIdIn, guiId, windowTitleIn, slotCountIn);
        entityId = incomingEntityId;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleOpenWindow(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        windowId = buf.readUnsignedByte();
        inventoryType = buf.readStringFromBuffer(32);
        windowTitle = buf.readChatComponent();
        slotCount = buf.readUnsignedByte();

        if (inventoryType.equals("EntityHorse"))
        {
            entityId = buf.readInt();
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeByte(windowId);
        buf.writeString(inventoryType);
        buf.writeChatComponent(windowTitle);
        buf.writeByte(slotCount);

        if (inventoryType.equals("EntityHorse"))
        {
            buf.writeInt(entityId);
        }
    }

    public int getWindowId()
    {
        return windowId;
    }

    public String getGuiId()
    {
        return inventoryType;
    }

    public IChatComponent getWindowTitle()
    {
        return windowTitle;
    }

    public int getSlotCount()
    {
        return slotCount;
    }

    public int getEntityId()
    {
        return entityId;
    }

    public boolean hasSlots()
    {
        return slotCount > 0;
    }
}
