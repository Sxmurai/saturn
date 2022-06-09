package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S32PacketConfirmTransaction implements Packet<INetHandlerPlayClient>
{
    private int windowId;
    private short actionNumber;
    private boolean field_148893_c;

    public S32PacketConfirmTransaction()
    {
    }

    public S32PacketConfirmTransaction(int windowIdIn, short actionNumberIn, boolean p_i45182_3_)
    {
        windowId = windowIdIn;
        actionNumber = actionNumberIn;
        field_148893_c = p_i45182_3_;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleConfirmTransaction(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        windowId = buf.readUnsignedByte();
        actionNumber = buf.readShort();
        field_148893_c = buf.readBoolean();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeByte(windowId);
        buf.writeShort(actionNumber);
        buf.writeBoolean(field_148893_c);
    }

    public int getWindowId()
    {
        return windowId;
    }

    public short getActionNumber()
    {
        return actionNumber;
    }

    public boolean func_148888_e()
    {
        return field_148893_c;
    }
}
