package net.minecraft.tileentity;

import io.netty.buffer.ByteBuf;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityCommandBlock extends TileEntity
{
    private final CommandBlockLogic commandBlockLogic = new CommandBlockLogic()
    {
        public BlockPos getPosition()
        {
            return pos;
        }
        public Vec3 getPositionVector()
        {
            return new Vec3((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
        }
        public World getEntityWorld()
        {
            return getWorld();
        }
        public void setCommand(String command)
        {
            super.setCommand(command);
            markDirty();
        }
        public void updateCommand()
        {
            getWorld().markBlockForUpdate(pos);
        }
        public int func_145751_f()
        {
            return 0;
        }
        public void func_145757_a(ByteBuf p_145757_1_)
        {
            p_145757_1_.writeInt(pos.getX());
            p_145757_1_.writeInt(pos.getY());
            p_145757_1_.writeInt(pos.getZ());
        }
        public Entity getCommandSenderEntity()
        {
            return null;
        }
    };

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        commandBlockLogic.writeDataToNBT(compound);
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        commandBlockLogic.readDataFromNBT(compound);
    }

    /**
     * Allows for a specialized description packet to be created. This is often used to sync tile entity data from the
     * server to the client easily. For example this is used by signs to synchronise the text to be displayed.
     */
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(pos, 2, nbttagcompound);
    }

    public boolean func_183000_F()
    {
        return true;
    }

    public CommandBlockLogic getCommandBlockLogic()
    {
        return commandBlockLogic;
    }

    public CommandResultStats getCommandResultStats()
    {
        return commandBlockLogic.getCommandResultStats();
    }
}
