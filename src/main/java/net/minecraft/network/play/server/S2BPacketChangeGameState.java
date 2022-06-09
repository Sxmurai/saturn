package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S2BPacketChangeGameState implements Packet<INetHandlerPlayClient>
{
    public static final String[] MESSAGE_NAMES = new String[] {"tile.bed.notValid"};
    private int state;
    private float field_149141_c;

    public S2BPacketChangeGameState()
    {
    }

    public S2BPacketChangeGameState(int stateIn, float p_i45194_2_)
    {
        state = stateIn;
        field_149141_c = p_i45194_2_;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        state = buf.readUnsignedByte();
        field_149141_c = buf.readFloat();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeByte(state);
        buf.writeFloat(field_149141_c);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleChangeGameState(this);
    }

    public int getGameState()
    {
        return state;
    }

    public float func_149137_d()
    {
        return field_149141_c;
    }
}
