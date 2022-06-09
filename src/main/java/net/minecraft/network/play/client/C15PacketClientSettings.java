package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C15PacketClientSettings implements Packet<INetHandlerPlayServer>
{
    private String lang;
    private int view;
    private EntityPlayer.EnumChatVisibility chatVisibility;
    private boolean enableColors;
    private int modelPartFlags;

    public C15PacketClientSettings()
    {
    }

    public C15PacketClientSettings(String langIn, int viewIn, EntityPlayer.EnumChatVisibility chatVisibilityIn, boolean enableColorsIn, int modelPartFlagsIn)
    {
        lang = langIn;
        view = viewIn;
        chatVisibility = chatVisibilityIn;
        enableColors = enableColorsIn;
        modelPartFlags = modelPartFlagsIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        lang = buf.readStringFromBuffer(7);
        view = buf.readByte();
        chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(buf.readByte());
        enableColors = buf.readBoolean();
        modelPartFlags = buf.readUnsignedByte();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(lang);
        buf.writeByte(view);
        buf.writeByte(chatVisibility.getChatVisibility());
        buf.writeBoolean(enableColors);
        buf.writeByte(modelPartFlags);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processClientSettings(this);
    }

    public String getLang()
    {
        return lang;
    }

    public EntityPlayer.EnumChatVisibility getChatVisibility()
    {
        return chatVisibility;
    }

    public boolean isColorsEnabled()
    {
        return enableColors;
    }

    public int getModelPartFlags()
    {
        return modelPartFlags;
    }
}
