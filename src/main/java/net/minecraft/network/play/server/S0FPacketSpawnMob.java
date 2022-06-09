package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.MathHelper;

public class S0FPacketSpawnMob implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private int type;
    private int x;
    private int y;
    private int z;
    private int velocityX;
    private int velocityY;
    private int velocityZ;
    private byte yaw;
    private byte pitch;
    private byte headPitch;
    private DataWatcher field_149043_l;
    private List<DataWatcher.WatchableObject> watcher;

    public S0FPacketSpawnMob()
    {
    }

    public S0FPacketSpawnMob(EntityLivingBase entityIn)
    {
        entityId = entityIn.getEntityId();
        type = (byte)EntityList.getEntityID(entityIn);
        x = MathHelper.floor_double(entityIn.posX * 32.0D);
        y = MathHelper.floor_double(entityIn.posY * 32.0D);
        z = MathHelper.floor_double(entityIn.posZ * 32.0D);
        yaw = (byte)((int)(entityIn.rotationYaw * 256.0F / 360.0F));
        pitch = (byte)((int)(entityIn.rotationPitch * 256.0F / 360.0F));
        headPitch = (byte)((int)(entityIn.rotationYawHead * 256.0F / 360.0F));
        double d0 = 3.9D;
        double d1 = entityIn.motionX;
        double d2 = entityIn.motionY;
        double d3 = entityIn.motionZ;

        if (d1 < -d0)
        {
            d1 = -d0;
        }

        if (d2 < -d0)
        {
            d2 = -d0;
        }

        if (d3 < -d0)
        {
            d3 = -d0;
        }

        if (d1 > d0)
        {
            d1 = d0;
        }

        if (d2 > d0)
        {
            d2 = d0;
        }

        if (d3 > d0)
        {
            d3 = d0;
        }

        velocityX = (int)(d1 * 8000.0D);
        velocityY = (int)(d2 * 8000.0D);
        velocityZ = (int)(d3 * 8000.0D);
        field_149043_l = entityIn.getDataWatcher();
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityId = buf.readVarIntFromBuffer();
        type = buf.readByte() & 255;
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yaw = buf.readByte();
        pitch = buf.readByte();
        headPitch = buf.readByte();
        velocityX = buf.readShort();
        velocityY = buf.readShort();
        velocityZ = buf.readShort();
        watcher = DataWatcher.readWatchedListFromPacketBuffer(buf);
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityId);
        buf.writeByte(type & 255);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeByte(headPitch);
        buf.writeShort(velocityX);
        buf.writeShort(velocityY);
        buf.writeShort(velocityZ);
        field_149043_l.writeTo(buf);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleSpawnMob(this);
    }

    public List<DataWatcher.WatchableObject> func_149027_c()
    {
        if (watcher == null)
        {
            watcher = field_149043_l.getAllWatched();
        }

        return watcher;
    }

    public int getEntityID()
    {
        return entityId;
    }

    public int getEntityType()
    {
        return type;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    public int getVelocityX()
    {
        return velocityX;
    }

    public int getVelocityY()
    {
        return velocityY;
    }

    public int getVelocityZ()
    {
        return velocityZ;
    }

    public byte getYaw()
    {
        return yaw;
    }

    public byte getPitch()
    {
        return pitch;
    }

    public byte getHeadPitch()
    {
        return headPitch;
    }
}
