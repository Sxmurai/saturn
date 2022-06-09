package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S1FPacketSetExperience implements Packet<INetHandlerPlayClient>
{
    private float field_149401_a;
    private int totalExperience;
    private int level;

    public S1FPacketSetExperience()
    {
    }

    public S1FPacketSetExperience(float p_i45222_1_, int totalExperienceIn, int levelIn)
    {
        field_149401_a = p_i45222_1_;
        totalExperience = totalExperienceIn;
        level = levelIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        field_149401_a = buf.readFloat();
        level = buf.readVarIntFromBuffer();
        totalExperience = buf.readVarIntFromBuffer();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeFloat(field_149401_a);
        buf.writeVarIntToBuffer(level);
        buf.writeVarIntToBuffer(totalExperience);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleSetExperience(this);
    }

    public float func_149397_c()
    {
        return field_149401_a;
    }

    public int getTotalExperience()
    {
        return totalExperience;
    }

    public int getLevel()
    {
        return level;
    }
}
