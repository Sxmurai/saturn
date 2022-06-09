package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.EnumParticleTypes;

public class S2APacketParticles implements Packet<INetHandlerPlayClient>
{
    private EnumParticleTypes particleType;
    private float xCoord;
    private float yCoord;
    private float zCoord;
    private float xOffset;
    private float yOffset;
    private float zOffset;
    private float particleSpeed;
    private int particleCount;
    private boolean longDistance;

    /**
     * These are the block/item ids and possibly metaData ids that are used to color or texture the particle.
     */
    private int[] particleArguments;

    public S2APacketParticles()
    {
    }

    public S2APacketParticles(EnumParticleTypes particleTypeIn, boolean longDistanceIn, float x, float y, float z, float xOffsetIn, float yOffset, float zOffset, float particleSpeedIn, int particleCountIn, int... particleArgumentsIn)
    {
        particleType = particleTypeIn;
        longDistance = longDistanceIn;
        xCoord = x;
        yCoord = y;
        zCoord = z;
        xOffset = xOffsetIn;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        particleSpeed = particleSpeedIn;
        particleCount = particleCountIn;
        particleArguments = particleArgumentsIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        particleType = EnumParticleTypes.getParticleFromId(buf.readInt());

        if (particleType == null)
        {
            particleType = EnumParticleTypes.BARRIER;
        }

        longDistance = buf.readBoolean();
        xCoord = buf.readFloat();
        yCoord = buf.readFloat();
        zCoord = buf.readFloat();
        xOffset = buf.readFloat();
        yOffset = buf.readFloat();
        zOffset = buf.readFloat();
        particleSpeed = buf.readFloat();
        particleCount = buf.readInt();
        int i = particleType.getArgumentCount();
        particleArguments = new int[i];

        for (int j = 0; j < i; ++j)
        {
            particleArguments[j] = buf.readVarIntFromBuffer();
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeInt(particleType.getParticleID());
        buf.writeBoolean(longDistance);
        buf.writeFloat(xCoord);
        buf.writeFloat(yCoord);
        buf.writeFloat(zCoord);
        buf.writeFloat(xOffset);
        buf.writeFloat(yOffset);
        buf.writeFloat(zOffset);
        buf.writeFloat(particleSpeed);
        buf.writeInt(particleCount);
        int i = particleType.getArgumentCount();

        for (int j = 0; j < i; ++j)
        {
            buf.writeVarIntToBuffer(particleArguments[j]);
        }
    }

    public EnumParticleTypes getParticleType()
    {
        return particleType;
    }

    public boolean isLongDistance()
    {
        return longDistance;
    }

    /**
     * Gets the x coordinate to spawn the particle.
     */
    public double getXCoordinate()
    {
        return xCoord;
    }

    /**
     * Gets the y coordinate to spawn the particle.
     */
    public double getYCoordinate()
    {
        return yCoord;
    }

    /**
     * Gets the z coordinate to spawn the particle.
     */
    public double getZCoordinate()
    {
        return zCoord;
    }

    /**
     * Gets the x coordinate offset for the particle. The particle may use the offset for particle spread.
     */
    public float getXOffset()
    {
        return xOffset;
    }

    /**
     * Gets the y coordinate offset for the particle. The particle may use the offset for particle spread.
     */
    public float getYOffset()
    {
        return yOffset;
    }

    /**
     * Gets the z coordinate offset for the particle. The particle may use the offset for particle spread.
     */
    public float getZOffset()
    {
        return zOffset;
    }

    /**
     * Gets the speed of the particle animation (used in client side rendering).
     */
    public float getParticleSpeed()
    {
        return particleSpeed;
    }

    /**
     * Gets the amount of particles to spawn
     */
    public int getParticleCount()
    {
        return particleCount;
    }

    /**
     * Gets the particle arguments. Some particles rely on block and/or item ids and sometimes metadata ids to color or
     * texture the particle.
     */
    public int[] getParticleArgs()
    {
        return particleArguments;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleParticles(this);
    }
}
