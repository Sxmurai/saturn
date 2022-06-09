package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.MathHelper;

public class S18PacketEntityTeleport implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private int posX;
    private int posY;
    private int posZ;
    private byte yaw;
    private byte pitch;
    private boolean onGround;

    public S18PacketEntityTeleport()
    {
    }

    public S18PacketEntityTeleport(Entity entityIn)
    {
        entityId = entityIn.getEntityId();
        posX = MathHelper.floor_double(entityIn.posX * 32.0D);
        posY = MathHelper.floor_double(entityIn.posY * 32.0D);
        posZ = MathHelper.floor_double(entityIn.posZ * 32.0D);
        yaw = (byte)((int)(entityIn.rotationYaw * 256.0F / 360.0F));
        pitch = (byte)((int)(entityIn.rotationPitch * 256.0F / 360.0F));
        onGround = entityIn.onGround;
    }

    public S18PacketEntityTeleport(int entityIdIn, int posXIn, int posYIn, int posZIn, byte yawIn, byte pitchIn, boolean onGroundIn)
    {
        entityId = entityIdIn;
        posX = posXIn;
        posY = posYIn;
        posZ = posZIn;
        yaw = yawIn;
        pitch = pitchIn;
        onGround = onGroundIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityId = buf.readVarIntFromBuffer();
        posX = buf.readInt();
        posY = buf.readInt();
        posZ = buf.readInt();
        yaw = buf.readByte();
        pitch = buf.readByte();
        onGround = buf.readBoolean();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityId);
        buf.writeInt(posX);
        buf.writeInt(posY);
        buf.writeInt(posZ);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeBoolean(onGround);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityTeleport(this);
    }

    public int getEntityId()
    {
        return entityId;
    }

    public int getX()
    {
        return posX;
    }

    public int getY()
    {
        return posY;
    }

    public int getZ()
    {
        return posZ;
    }

    public byte getYaw()
    {
        return yaw;
    }

    public byte getPitch()
    {
        return pitch;
    }

    public boolean getOnGround()
    {
        return onGround;
    }
}
