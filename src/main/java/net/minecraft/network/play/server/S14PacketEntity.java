package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;

public class S14PacketEntity implements Packet<INetHandlerPlayClient>
{
    protected int entityId;
    protected byte posX;
    protected byte posY;
    protected byte posZ;
    protected byte yaw;
    protected byte pitch;
    protected boolean onGround;
    protected boolean field_149069_g;

    public S14PacketEntity()
    {
    }

    public S14PacketEntity(int entityIdIn)
    {
        entityId = entityIdIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityId = buf.readVarIntFromBuffer();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityId);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityMovement(this);
    }

    public String toString()
    {
        return "Entity_" + super.toString();
    }

    public Entity getEntity(World worldIn)
    {
        return worldIn.getEntityByID(entityId);
    }

    public byte func_149062_c()
    {
        return posX;
    }

    public byte func_149061_d()
    {
        return posY;
    }

    public byte func_149064_e()
    {
        return posZ;
    }

    public byte func_149066_f()
    {
        return yaw;
    }

    public byte func_149063_g()
    {
        return pitch;
    }

    public boolean func_149060_h()
    {
        return field_149069_g;
    }

    public boolean getOnGround()
    {
        return onGround;
    }

    public static class S15PacketEntityRelMove extends S14PacketEntity
    {
        public S15PacketEntityRelMove()
        {
        }

        public S15PacketEntityRelMove(int entityIdIn, byte x, byte y, byte z, boolean onGroundIn)
        {
            super(entityIdIn);
            posX = x;
            posY = y;
            posZ = z;
            onGround = onGroundIn;
        }

        public void readPacketData(PacketBuffer buf) throws IOException
        {
            super.readPacketData(buf);
            posX = buf.readByte();
            posY = buf.readByte();
            posZ = buf.readByte();
            onGround = buf.readBoolean();
        }

        public void writePacketData(PacketBuffer buf) throws IOException
        {
            super.writePacketData(buf);
            buf.writeByte(posX);
            buf.writeByte(posY);
            buf.writeByte(posZ);
            buf.writeBoolean(onGround);
        }
    }

    public static class S16PacketEntityLook extends S14PacketEntity
    {
        public S16PacketEntityLook()
        {
            field_149069_g = true;
        }

        public S16PacketEntityLook(int entityIdIn, byte yawIn, byte pitchIn, boolean onGroundIn)
        {
            super(entityIdIn);
            yaw = yawIn;
            pitch = pitchIn;
            field_149069_g = true;
            onGround = onGroundIn;
        }

        public void readPacketData(PacketBuffer buf) throws IOException
        {
            super.readPacketData(buf);
            yaw = buf.readByte();
            pitch = buf.readByte();
            onGround = buf.readBoolean();
        }

        public void writePacketData(PacketBuffer buf) throws IOException
        {
            super.writePacketData(buf);
            buf.writeByte(yaw);
            buf.writeByte(pitch);
            buf.writeBoolean(onGround);
        }
    }

    public static class S17PacketEntityLookMove extends S14PacketEntity
    {
        public S17PacketEntityLookMove()
        {
            field_149069_g = true;
        }

        public S17PacketEntityLookMove(int p_i45973_1_, byte p_i45973_2_, byte p_i45973_3_, byte p_i45973_4_, byte p_i45973_5_, byte p_i45973_6_, boolean p_i45973_7_)
        {
            super(p_i45973_1_);
            posX = p_i45973_2_;
            posY = p_i45973_3_;
            posZ = p_i45973_4_;
            yaw = p_i45973_5_;
            pitch = p_i45973_6_;
            onGround = p_i45973_7_;
            field_149069_g = true;
        }

        public void readPacketData(PacketBuffer buf) throws IOException
        {
            super.readPacketData(buf);
            posX = buf.readByte();
            posY = buf.readByte();
            posZ = buf.readByte();
            yaw = buf.readByte();
            pitch = buf.readByte();
            onGround = buf.readBoolean();
        }

        public void writePacketData(PacketBuffer buf) throws IOException
        {
            super.writePacketData(buf);
            buf.writeByte(posX);
            buf.writeByte(posY);
            buf.writeByte(posZ);
            buf.writeByte(yaw);
            buf.writeByte(pitch);
            buf.writeBoolean(onGround);
        }
    }
}
