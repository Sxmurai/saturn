package net.minecraft.tileentity;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

public class TileEntityMobSpawner extends TileEntity implements ITickable
{
    private final MobSpawnerBaseLogic spawnerLogic = new MobSpawnerBaseLogic()
    {
        public void func_98267_a(int id)
        {
            worldObj.addBlockEvent(pos, Blocks.mob_spawner, id, 0);
        }
        public World getSpawnerWorld()
        {
            return worldObj;
        }
        public BlockPos getSpawnerPosition()
        {
            return pos;
        }
        public void setRandomEntity(MobSpawnerBaseLogic.WeightedRandomMinecart p_98277_1_)
        {
            super.setRandomEntity(p_98277_1_);

            if (getSpawnerWorld() != null)
            {
                getSpawnerWorld().markBlockForUpdate(pos);
            }
        }
    };

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        spawnerLogic.readFromNBT(compound);
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        spawnerLogic.writeToNBT(compound);
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        spawnerLogic.updateSpawner();
    }

    /**
     * Allows for a specialized description packet to be created. This is often used to sync tile entity data from the
     * server to the client easily. For example this is used by signs to synchronise the text to be displayed.
     */
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        writeToNBT(nbttagcompound);
        nbttagcompound.removeTag("SpawnPotentials");
        return new S35PacketUpdateTileEntity(pos, 1, nbttagcompound);
    }

    public boolean receiveClientEvent(int id, int type)
    {
        return spawnerLogic.setDelayToMin(id) || super.receiveClientEvent(id, type);
    }

    public boolean func_183000_F()
    {
        return true;
    }

    public MobSpawnerBaseLogic getSpawnerBaseLogic()
    {
        return spawnerLogic;
    }
}
