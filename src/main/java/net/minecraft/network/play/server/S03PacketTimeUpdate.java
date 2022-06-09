package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S03PacketTimeUpdate implements Packet<INetHandlerPlayClient>
{
    private long totalWorldTime;
    private long worldTime;

    public S03PacketTimeUpdate()
    {
    }

    public S03PacketTimeUpdate(long totalWorldTimeIn, long totalTimeIn, boolean doDayLightCycle)
    {
        totalWorldTime = totalWorldTimeIn;
        worldTime = totalTimeIn;

        if (!doDayLightCycle)
        {
            worldTime = -worldTime;

            if (worldTime == 0L)
            {
                worldTime = -1L;
            }
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        totalWorldTime = buf.readLong();
        worldTime = buf.readLong();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeLong(totalWorldTime);
        buf.writeLong(worldTime);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleTimeUpdate(this);
    }

    public long getTotalWorldTime()
    {
        return totalWorldTime;
    }

    public long getWorldTime()
    {
        return worldTime;
    }
}
