package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.ScoreObjective;

public class S3DPacketDisplayScoreboard implements Packet<INetHandlerPlayClient>
{
    private int position;
    private String scoreName;

    public S3DPacketDisplayScoreboard()
    {
    }

    public S3DPacketDisplayScoreboard(int positionIn, ScoreObjective scoreIn)
    {
        position = positionIn;

        if (scoreIn == null)
        {
            scoreName = "";
        }
        else
        {
            scoreName = scoreIn.getName();
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        position = buf.readByte();
        scoreName = buf.readStringFromBuffer(16);
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeByte(position);
        buf.writeString(scoreName);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleDisplayScoreboard(this);
    }

    public int func_149371_c()
    {
        return position;
    }

    public String func_149370_d()
    {
        return scoreName;
    }
}
