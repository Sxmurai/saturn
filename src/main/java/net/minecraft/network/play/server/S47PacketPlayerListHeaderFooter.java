package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IChatComponent;

public class S47PacketPlayerListHeaderFooter implements Packet<INetHandlerPlayClient>
{
    private IChatComponent header;
    private IChatComponent footer;

    public S47PacketPlayerListHeaderFooter()
    {
    }

    public S47PacketPlayerListHeaderFooter(IChatComponent headerIn)
    {
        header = headerIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        header = buf.readChatComponent();
        footer = buf.readChatComponent();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeChatComponent(header);
        buf.writeChatComponent(footer);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handlePlayerListHeaderFooter(this);
    }

    public IChatComponent getHeader()
    {
        return header;
    }

    public IChatComponent getFooter()
    {
        return footer;
    }
}
