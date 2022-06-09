package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.MathHelper;

public class S0EPacketSpawnObject implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private int x;
    private int y;
    private int z;
    private int speedX;
    private int speedY;
    private int speedZ;
    private int pitch;
    private int yaw;
    private int type;
    private int field_149020_k;

    public S0EPacketSpawnObject()
    {
    }

    public S0EPacketSpawnObject(Entity entityIn, int typeIn)
    {
        this(entityIn, typeIn, 0);
    }

    public S0EPacketSpawnObject(Entity entityIn, int typeIn, int p_i45166_3_)
    {
        entityId = entityIn.getEntityId();
        x = MathHelper.floor_double(entityIn.posX * 32.0D);
        y = MathHelper.floor_double(entityIn.posY * 32.0D);
        z = MathHelper.floor_double(entityIn.posZ * 32.0D);
        pitch = MathHelper.floor_float(entityIn.rotationPitch * 256.0F / 360.0F);
        yaw = MathHelper.floor_float(entityIn.rotationYaw * 256.0F / 360.0F);
        type = typeIn;
        field_149020_k = p_i45166_3_;

        if (p_i45166_3_ > 0)
        {
            double d0 = entityIn.motionX;
            double d1 = entityIn.motionY;
            double d2 = entityIn.motionZ;
            double d3 = 3.9D;

            if (d0 < -d3)
            {
                d0 = -d3;
            }

            if (d1 < -d3)
            {
                d1 = -d3;
            }

            if (d2 < -d3)
            {
                d2 = -d3;
            }

            if (d0 > d3)
            {
                d0 = d3;
            }

            if (d1 > d3)
            {
                d1 = d3;
            }

            if (d2 > d3)
            {
                d2 = d3;
            }

            speedX = (int)(d0 * 8000.0D);
            speedY = (int)(d1 * 8000.0D);
            speedZ = (int)(d2 * 8000.0D);
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityId = buf.readVarIntFromBuffer();
        type = buf.readByte();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        pitch = buf.readByte();
        yaw = buf.readByte();
        field_149020_k = buf.readInt();

        if (field_149020_k > 0)
        {
            speedX = buf.readShort();
            speedY = buf.readShort();
            speedZ = buf.readShort();
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityId);
        buf.writeByte(type);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(pitch);
        buf.writeByte(yaw);
        buf.writeInt(field_149020_k);

        if (field_149020_k > 0)
        {
            buf.writeShort(speedX);
            buf.writeShort(speedY);
            buf.writeShort(speedZ);
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleSpawnObject(this);
    }

    public int getEntityID()
    {
        return entityId;
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

    public int getSpeedX()
    {
        return speedX;
    }

    public int getSpeedY()
    {
        return speedY;
    }

    public int getSpeedZ()
    {
        return speedZ;
    }

    public int getPitch()
    {
        return pitch;
    }

    public int getYaw()
    {
        return yaw;
    }

    public int getType()
    {
        return type;
    }

    public int func_149009_m()
    {
        return field_149020_k;
    }

    public void setX(int newX)
    {
        x = newX;
    }

    public void setY(int newY)
    {
        y = newY;
    }

    public void setZ(int newZ)
    {
        z = newZ;
    }

    public void setSpeedX(int newSpeedX)
    {
        speedX = newSpeedX;
    }

    public void setSpeedY(int newSpeedY)
    {
        speedY = newSpeedY;
    }

    public void setSpeedZ(int newSpeedZ)
    {
        speedZ = newSpeedZ;
    }

    public void func_149002_g(int p_149002_1_)
    {
        field_149020_k = p_149002_1_;
    }
}
