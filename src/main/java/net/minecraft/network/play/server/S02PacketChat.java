package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IChatComponent;

public class S02PacketChat implements Packet<INetHandlerPlayClient>
{
    private IChatComponent chatComponent;
    private byte type;

    public S02PacketChat()
    {
    }

    public S02PacketChat(IChatComponent component)
    {
        this(component, (byte)1);
    }

    public S02PacketChat(IChatComponent message, byte typeIn)
    {
        chatComponent = message;
        type = typeIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        chatComponent = buf.readChatComponent();
        type = buf.readByte();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeChatComponent(chatComponent);
        buf.writeByte(type);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleChat(this);
    }

    public IChatComponent getChatComponent()
    {
        return chatComponent;
    }

    public boolean isChat()
    {
        return type == 1 || type == 2;
    }

    /**
     * Returns the id of the area to display the text, 2 for above the action bar, anything else currently for the chat
     * window
     */
    public byte getType()
    {
        return type;
    }
}
