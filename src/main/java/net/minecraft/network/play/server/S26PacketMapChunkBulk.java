package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.chunk.Chunk;

public class S26PacketMapChunkBulk implements Packet<INetHandlerPlayClient>
{
    private int[] xPositions;
    private int[] zPositions;
    private S21PacketChunkData.Extracted[] chunksData;
    private boolean isOverworld;

    public S26PacketMapChunkBulk()
    {
    }

    public S26PacketMapChunkBulk(List<Chunk> chunks)
    {
        int i = chunks.size();
        xPositions = new int[i];
        zPositions = new int[i];
        chunksData = new S21PacketChunkData.Extracted[i];
        isOverworld = !chunks.get(0).getWorld().provider.getHasNoSky();

        for (int j = 0; j < i; ++j)
        {
            Chunk chunk = chunks.get(j);
            S21PacketChunkData.Extracted s21packetchunkdata$extracted = S21PacketChunkData.func_179756_a(chunk, true, isOverworld, 65535);
            xPositions[j] = chunk.xPosition;
            zPositions[j] = chunk.zPosition;
            chunksData[j] = s21packetchunkdata$extracted;
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        isOverworld = buf.readBoolean();
        int i = buf.readVarIntFromBuffer();
        xPositions = new int[i];
        zPositions = new int[i];
        chunksData = new S21PacketChunkData.Extracted[i];

        for (int j = 0; j < i; ++j)
        {
            xPositions[j] = buf.readInt();
            zPositions[j] = buf.readInt();
            chunksData[j] = new S21PacketChunkData.Extracted();
            chunksData[j].dataSize = buf.readShort() & 65535;
            chunksData[j].data = new byte[S21PacketChunkData.func_180737_a(Integer.bitCount(chunksData[j].dataSize), isOverworld, true)];
        }

        for (int k = 0; k < i; ++k)
        {
            buf.readBytes(chunksData[k].data);
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeBoolean(isOverworld);
        buf.writeVarIntToBuffer(chunksData.length);

        for (int i = 0; i < xPositions.length; ++i)
        {
            buf.writeInt(xPositions[i]);
            buf.writeInt(zPositions[i]);
            buf.writeShort((short)(chunksData[i].dataSize & 65535));
        }

        for (int j = 0; j < xPositions.length; ++j)
        {
            buf.writeBytes(chunksData[j].data);
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleMapChunkBulk(this);
    }

    public int getChunkX(int p_149255_1_)
    {
        return xPositions[p_149255_1_];
    }

    public int getChunkZ(int p_149253_1_)
    {
        return zPositions[p_149253_1_];
    }

    public int getChunkCount()
    {
        return xPositions.length;
    }

    public byte[] getChunkBytes(int p_149256_1_)
    {
        return chunksData[p_149256_1_].data;
    }

    public int getChunkSize(int p_179754_1_)
    {
        return chunksData[p_179754_1_].dataSize;
    }
}
