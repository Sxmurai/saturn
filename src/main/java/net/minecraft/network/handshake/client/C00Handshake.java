package net.minecraft.network.handshake.client;

import java.io.IOException;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;

public class C00Handshake implements Packet<INetHandlerHandshakeServer>
{
    private int protocolVersion;
    private String ip;
    private int port;
    private EnumConnectionState requestedState;

    public C00Handshake()
    {
    }

    public C00Handshake(int version, String ip, int port, EnumConnectionState requestedState)
    {
        protocolVersion = version;
        this.ip = ip;
        this.port = port;
        this.requestedState = requestedState;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        protocolVersion = buf.readVarIntFromBuffer();
        ip = buf.readStringFromBuffer(255);
        port = buf.readUnsignedShort();
        requestedState = EnumConnectionState.getById(buf.readVarIntFromBuffer());
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(protocolVersion);
        buf.writeString(ip);
        buf.writeShort(port);
        buf.writeVarIntToBuffer(requestedState.getId());
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerHandshakeServer handler)
    {
        handler.processHandshake(this);
    }

    public EnumConnectionState getRequestedState()
    {
        return requestedState;
    }

    public int getProtocolVersion()
    {
        return protocolVersion;
    }
}
