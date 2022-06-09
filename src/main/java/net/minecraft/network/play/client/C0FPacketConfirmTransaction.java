package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C0FPacketConfirmTransaction implements Packet<INetHandlerPlayServer>
{
    private int windowId;
    private short uid;
    private boolean accepted;

    public C0FPacketConfirmTransaction()
    {
    }

    public C0FPacketConfirmTransaction(int windowId, short uid, boolean accepted)
    {
        this.windowId = windowId;
        this.uid = uid;
        this.accepted = accepted;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processConfirmTransaction(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        windowId = buf.readByte();
        uid = buf.readShort();
        accepted = buf.readByte() != 0;
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeByte(windowId);
        buf.writeShort(uid);
        buf.writeByte(accepted ? 1 : 0);
    }

    public int getWindowId()
    {
        return windowId;
    }

    public short getUid()
    {
        return uid;
    }
}
