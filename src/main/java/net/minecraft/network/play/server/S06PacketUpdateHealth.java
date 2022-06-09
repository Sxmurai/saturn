package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S06PacketUpdateHealth implements Packet<INetHandlerPlayClient>
{
    private float health;
    private int foodLevel;
    private float saturationLevel;

    public S06PacketUpdateHealth()
    {
    }

    public S06PacketUpdateHealth(float healthIn, int foodLevelIn, float saturationIn)
    {
        health = healthIn;
        foodLevel = foodLevelIn;
        saturationLevel = saturationIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        health = buf.readFloat();
        foodLevel = buf.readVarIntFromBuffer();
        saturationLevel = buf.readFloat();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeFloat(health);
        buf.writeVarIntToBuffer(foodLevel);
        buf.writeFloat(saturationLevel);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleUpdateHealth(this);
    }

    public float getHealth()
    {
        return health;
    }

    public int getFoodLevel()
    {
        return foodLevel;
    }

    public float getSaturationLevel()
    {
        return saturationLevel;
    }
}
