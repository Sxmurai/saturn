package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;

public class EntityEnderCrystal extends Entity
{
    /** Used to create the rotation animation when rendering the crystal. */
    public int innerRotation;
    public int health;

    public EntityEnderCrystal(World worldIn)
    {
        super(worldIn);
        preventEntitySpawning = true;
        setSize(2.0F, 2.0F);
        health = 5;
        innerRotation = rand.nextInt(100000);
    }

    public EntityEnderCrystal(World worldIn, double p_i1699_2_, double p_i1699_4_, double p_i1699_6_)
    {
        this(worldIn);
        setPosition(p_i1699_2_, p_i1699_4_, p_i1699_6_);
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    protected void entityInit()
    {
        dataWatcher.addObject(8, Integer.valueOf(health));
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        ++innerRotation;
        dataWatcher.updateObject(8, Integer.valueOf(health));
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(posY);
        int k = MathHelper.floor_double(posZ);

        if (worldObj.provider instanceof WorldProviderEnd && worldObj.getBlockState(new BlockPos(i, j, k)).getBlock() != Blocks.fire)
        {
            worldObj.setBlockState(new BlockPos(i, j, k), Blocks.fire.getDefaultState());
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound tagCompound)
    {
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound tagCompund)
    {
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return true;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            if (!isDead && !worldObj.isRemote)
            {
                health = 0;

                if (health <= 0)
                {
                    setDead();

                    if (!worldObj.isRemote)
                    {
                        worldObj.createExplosion(null, posX, posY, posZ, 6.0F, true);
                    }
                }
            }

            return true;
        }
    }
}
