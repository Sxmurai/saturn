package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S31PacketWindowProperty implements Packet<INetHandlerPlayClient>
{
    private int windowId;
    private int varIndex;
    private int varValue;

    public S31PacketWindowProperty()
    {
    }

    public S31PacketWindowProperty(int windowIdIn, int varIndexIn, int varValueIn)
    {
        windowId = windowIdIn;
        varIndex = varIndexIn;
        varValue = varValueIn;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleWindowProperty(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        windowId = buf.readUnsignedByte();
        varIndex = buf.readShort();
        varValue = buf.readShort();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeByte(windowId);
        buf.writeShort(varIndex);
        buf.writeShort(varValue);
    }

    public int getWindowId()
    {
        return windowId;
    }

    public int getVarIndex()
    {
        return varIndex;
    }

    public int getVarValue()
    {
        return varValue;
    }
}
