package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;

public class S3CPacketUpdateScore implements Packet<INetHandlerPlayClient>
{
    private String name = "";
    private String objective = "";
    private int value;
    private S3CPacketUpdateScore.Action action;

    public S3CPacketUpdateScore()
    {
    }

    public S3CPacketUpdateScore(Score scoreIn)
    {
        name = scoreIn.getPlayerName();
        objective = scoreIn.getObjective().getName();
        value = scoreIn.getScorePoints();
        action = S3CPacketUpdateScore.Action.CHANGE;
    }

    public S3CPacketUpdateScore(String nameIn)
    {
        name = nameIn;
        objective = "";
        value = 0;
        action = S3CPacketUpdateScore.Action.REMOVE;
    }

    public S3CPacketUpdateScore(String nameIn, ScoreObjective objectiveIn)
    {
        name = nameIn;
        objective = objectiveIn.getName();
        value = 0;
        action = S3CPacketUpdateScore.Action.REMOVE;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        name = buf.readStringFromBuffer(40);
        action = buf.readEnumValue(Action.class);
        objective = buf.readStringFromBuffer(16);

        if (action != S3CPacketUpdateScore.Action.REMOVE)
        {
            value = buf.readVarIntFromBuffer();
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(name);
        buf.writeEnumValue(action);
        buf.writeString(objective);

        if (action != S3CPacketUpdateScore.Action.REMOVE)
        {
            buf.writeVarIntToBuffer(value);
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleUpdateScore(this);
    }

    public String getPlayerName()
    {
        return name;
    }

    public String getObjectiveName()
    {
        return objective;
    }

    public int getScoreValue()
    {
        return value;
    }

    public S3CPacketUpdateScore.Action getScoreAction()
    {
        return action;
    }

    public static enum Action
    {
        CHANGE,
        REMOVE
    }
}
