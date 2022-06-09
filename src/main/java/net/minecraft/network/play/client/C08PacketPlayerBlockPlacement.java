package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;

public class C08PacketPlayerBlockPlacement implements Packet<INetHandlerPlayServer>
{
    private static final BlockPos field_179726_a = new BlockPos(-1, -1, -1);
    private BlockPos position;
    private int placedBlockDirection;
    private ItemStack stack;
    private float facingX;
    private float facingY;
    private float facingZ;

    public C08PacketPlayerBlockPlacement()
    {
    }

    public C08PacketPlayerBlockPlacement(ItemStack stackIn)
    {
        this(C08PacketPlayerBlockPlacement.field_179726_a, 255, stackIn, 0.0F, 0.0F, 0.0F);
    }

    public C08PacketPlayerBlockPlacement(BlockPos positionIn, int placedBlockDirectionIn, ItemStack stackIn, float facingXIn, float facingYIn, float facingZIn)
    {
        position = positionIn;
        placedBlockDirection = placedBlockDirectionIn;
        stack = stackIn != null ? stackIn.copy() : null;
        facingX = facingXIn;
        facingY = facingYIn;
        facingZ = facingZIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        position = buf.readBlockPos();
        placedBlockDirection = buf.readUnsignedByte();
        stack = buf.readItemStackFromBuffer();
        facingX = (float)buf.readUnsignedByte() / 16.0F;
        facingY = (float)buf.readUnsignedByte() / 16.0F;
        facingZ = (float)buf.readUnsignedByte() / 16.0F;
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeBlockPos(position);
        buf.writeByte(placedBlockDirection);
        buf.writeItemStackToBuffer(stack);
        buf.writeByte((int)(facingX * 16.0F));
        buf.writeByte((int)(facingY * 16.0F));
        buf.writeByte((int)(facingZ * 16.0F));
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processPlayerBlockPlacement(this);
    }

    public BlockPos getPosition()
    {
        return position;
    }

    public int getPlacedBlockDirection()
    {
        return placedBlockDirection;
    }

    public ItemStack getStack()
    {
        return stack;
    }

    /**
     * Returns the offset from xPosition where the actual click took place.
     */
    public float getPlacedBlockOffsetX()
    {
        return facingX;
    }

    /**
     * Returns the offset from yPosition where the actual click took place.
     */
    public float getPlacedBlockOffsetY()
    {
        return facingY;
    }

    /**
     * Returns the offset from zPosition where the actual click took place.
     */
    public float getPlacedBlockOffsetZ()
    {
        return facingZ;
    }
}
