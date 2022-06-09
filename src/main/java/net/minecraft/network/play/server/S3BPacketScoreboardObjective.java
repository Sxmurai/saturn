package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;

public class S3BPacketScoreboardObjective implements Packet<INetHandlerPlayClient>
{
    private String objectiveName;
    private String objectiveValue;
    private IScoreObjectiveCriteria.EnumRenderType type;
    private int field_149342_c;

    public S3BPacketScoreboardObjective()
    {
    }

    public S3BPacketScoreboardObjective(ScoreObjective p_i45224_1_, int p_i45224_2_)
    {
        objectiveName = p_i45224_1_.getName();
        objectiveValue = p_i45224_1_.getDisplayName();
        type = p_i45224_1_.getCriteria().getRenderType();
        field_149342_c = p_i45224_2_;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        objectiveName = buf.readStringFromBuffer(16);
        field_149342_c = buf.readByte();

        if (field_149342_c == 0 || field_149342_c == 2)
        {
            objectiveValue = buf.readStringFromBuffer(32);
            type = IScoreObjectiveCriteria.EnumRenderType.func_178795_a(buf.readStringFromBuffer(16));
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(objectiveName);
        buf.writeByte(field_149342_c);

        if (field_149342_c == 0 || field_149342_c == 2)
        {
            buf.writeString(objectiveValue);
            buf.writeString(type.func_178796_a());
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleScoreboardObjective(this);
    }

    public String func_149339_c()
    {
        return objectiveName;
    }

    public String func_149337_d()
    {
        return objectiveValue;
    }

    public int func_149338_e()
    {
        return field_149342_c;
    }

    public IScoreObjectiveCriteria.EnumRenderType func_179817_d()
    {
        return type;
    }
}
