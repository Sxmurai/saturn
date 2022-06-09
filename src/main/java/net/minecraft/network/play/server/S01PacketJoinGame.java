package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class S01PacketJoinGame implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private boolean hardcoreMode;
    private WorldSettings.GameType gameType;
    private int dimension;
    private EnumDifficulty difficulty;
    private int maxPlayers;
    private WorldType worldType;
    private boolean reducedDebugInfo;

    public S01PacketJoinGame()
    {
    }

    public S01PacketJoinGame(int entityIdIn, WorldSettings.GameType gameTypeIn, boolean hardcoreModeIn, int dimensionIn, EnumDifficulty difficultyIn, int maxPlayersIn, WorldType worldTypeIn, boolean reducedDebugInfoIn)
    {
        entityId = entityIdIn;
        dimension = dimensionIn;
        difficulty = difficultyIn;
        gameType = gameTypeIn;
        maxPlayers = maxPlayersIn;
        hardcoreMode = hardcoreModeIn;
        worldType = worldTypeIn;
        reducedDebugInfo = reducedDebugInfoIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityId = buf.readInt();
        int i = buf.readUnsignedByte();
        hardcoreMode = (i & 8) == 8;
        i = i & -9;
        gameType = WorldSettings.GameType.getByID(i);
        dimension = buf.readByte();
        difficulty = EnumDifficulty.getDifficultyEnum(buf.readUnsignedByte());
        maxPlayers = buf.readUnsignedByte();
        worldType = WorldType.parseWorldType(buf.readStringFromBuffer(16));

        if (worldType == null)
        {
            worldType = WorldType.DEFAULT;
        }

        reducedDebugInfo = buf.readBoolean();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeInt(entityId);
        int i = gameType.getID();

        if (hardcoreMode)
        {
            i |= 8;
        }

        buf.writeByte(i);
        buf.writeByte(dimension);
        buf.writeByte(difficulty.getDifficultyId());
        buf.writeByte(maxPlayers);
        buf.writeString(worldType.getWorldTypeName());
        buf.writeBoolean(reducedDebugInfo);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleJoinGame(this);
    }

    public int getEntityId()
    {
        return entityId;
    }

    public boolean isHardcoreMode()
    {
        return hardcoreMode;
    }

    public WorldSettings.GameType getGameType()
    {
        return gameType;
    }

    public int getDimension()
    {
        return dimension;
    }

    public EnumDifficulty getDifficulty()
    {
        return difficulty;
    }

    public int getMaxPlayers()
    {
        return maxPlayers;
    }

    public WorldType getWorldType()
    {
        return worldType;
    }

    public boolean isReducedDebugInfo()
    {
        return reducedDebugInfo;
    }
}
