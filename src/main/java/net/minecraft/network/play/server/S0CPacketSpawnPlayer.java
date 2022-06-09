package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.MathHelper;

public class S0CPacketSpawnPlayer implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private UUID playerId;
    private int x;
    private int y;
    private int z;
    private byte yaw;
    private byte pitch;
    private int currentItem;
    private DataWatcher watcher;
    private List<DataWatcher.WatchableObject> field_148958_j;

    public S0CPacketSpawnPlayer()
    {
    }

    public S0CPacketSpawnPlayer(EntityPlayer player)
    {
        entityId = player.getEntityId();
        playerId = player.getGameProfile().getId();
        x = MathHelper.floor_double(player.posX * 32.0D);
        y = MathHelper.floor_double(player.posY * 32.0D);
        z = MathHelper.floor_double(player.posZ * 32.0D);
        yaw = (byte)((int)(player.rotationYaw * 256.0F / 360.0F));
        pitch = (byte)((int)(player.rotationPitch * 256.0F / 360.0F));
        ItemStack itemstack = player.inventory.getCurrentItem();
        currentItem = itemstack == null ? 0 : Item.getIdFromItem(itemstack.getItem());
        watcher = player.getDataWatcher();
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityId = buf.readVarIntFromBuffer();
        playerId = buf.readUuid();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yaw = buf.readByte();
        pitch = buf.readByte();
        currentItem = buf.readShort();
        field_148958_j = DataWatcher.readWatchedListFromPacketBuffer(buf);
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityId);
        buf.writeUuid(playerId);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeShort(currentItem);
        watcher.writeTo(buf);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleSpawnPlayer(this);
    }

    public List<DataWatcher.WatchableObject> func_148944_c()
    {
        if (field_148958_j == null)
        {
            field_148958_j = watcher.getAllWatched();
        }

        return field_148958_j;
    }

    public int getEntityID()
    {
        return entityId;
    }

    public UUID getPlayer()
    {
        return playerId;
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

    public byte getYaw()
    {
        return yaw;
    }

    public byte getPitch()
    {
        return pitch;
    }

    public int getCurrentItemID()
    {
        return currentItem;
    }
}
