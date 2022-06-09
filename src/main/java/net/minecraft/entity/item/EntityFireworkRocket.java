package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityFireworkRocket extends Entity
{
    /** The age of the firework in ticks. */
    private int fireworkAge;

    /**
     * The lifetime of the firework in ticks. When the age reaches the lifetime the firework explodes.
     */
    private int lifetime;

    public EntityFireworkRocket(World worldIn)
    {
        super(worldIn);
        setSize(0.25F, 0.25F);
    }

    protected void entityInit()
    {
        dataWatcher.addObjectByDataType(8, 5);
    }

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     */
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 4096.0D;
    }

    public EntityFireworkRocket(World worldIn, double x, double y, double z, ItemStack givenItem)
    {
        super(worldIn);
        fireworkAge = 0;
        setSize(0.25F, 0.25F);
        setPosition(x, y, z);
        int i = 1;

        if (givenItem != null && givenItem.hasTagCompound())
        {
            dataWatcher.updateObject(8, givenItem);
            NBTTagCompound nbttagcompound = givenItem.getTagCompound();
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Fireworks");

            if (nbttagcompound1 != null)
            {
                i += nbttagcompound1.getByte("Flight");
            }
        }

        motionX = rand.nextGaussian() * 0.001D;
        motionZ = rand.nextGaussian() * 0.001D;
        motionY = 0.05D;
        lifetime = 10 * i + rand.nextInt(6) + rand.nextInt(7);
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double x, double y, double z)
    {
        motionX = x;
        motionY = y;
        motionZ = z;

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(x * x + z * z);
            prevRotationYaw = rotationYaw = (float)(MathHelper.func_181159_b(x, z) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch = (float)(MathHelper.func_181159_b(y, f) * 180.0D / Math.PI);
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;
        super.onUpdate();
        motionX *= 1.15D;
        motionZ *= 1.15D;
        motionY += 0.04D;
        moveEntity(motionX, motionY, motionZ);
        float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float)(MathHelper.func_181159_b(motionX, motionZ) * 180.0D / Math.PI);

        for (rotationPitch = (float)(MathHelper.func_181159_b(motionY, f) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F)
        {
        }

        while (rotationPitch - prevRotationPitch >= 180.0F)
        {
            prevRotationPitch += 360.0F;
        }

        while (rotationYaw - prevRotationYaw < -180.0F)
        {
            prevRotationYaw -= 360.0F;
        }

        while (rotationYaw - prevRotationYaw >= 180.0F)
        {
            prevRotationYaw += 360.0F;
        }

        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;

        if (fireworkAge == 0 && !isSilent())
        {
            worldObj.playSoundAtEntity(this, "fireworks.launch", 3.0F, 1.0F);
        }

        ++fireworkAge;

        if (worldObj.isRemote && fireworkAge % 2 < 2)
        {
            worldObj.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, posX, posY - 0.3D, posZ, rand.nextGaussian() * 0.05D, -motionY * 0.5D, rand.nextGaussian() * 0.05D);
        }

        if (!worldObj.isRemote && fireworkAge > lifetime)
        {
            worldObj.setEntityState(this, (byte)17);
            setDead();
        }
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 17 && worldObj.isRemote)
        {
            ItemStack itemstack = dataWatcher.getWatchableObjectItemStack(8);
            NBTTagCompound nbttagcompound = null;

            if (itemstack != null && itemstack.hasTagCompound())
            {
                nbttagcompound = itemstack.getTagCompound().getCompoundTag("Fireworks");
            }

            worldObj.makeFireworks(posX, posY, posZ, motionX, motionY, motionZ, nbttagcompound);
        }

        super.handleStatusUpdate(id);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setInteger("Life", fireworkAge);
        tagCompound.setInteger("LifeTime", lifetime);
        ItemStack itemstack = dataWatcher.getWatchableObjectItemStack(8);

        if (itemstack != null)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            itemstack.writeToNBT(nbttagcompound);
            tagCompound.setTag("FireworksItem", nbttagcompound);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        fireworkAge = tagCompund.getInteger("Life");
        lifetime = tagCompund.getInteger("LifeTime");
        NBTTagCompound nbttagcompound = tagCompund.getCompoundTag("FireworksItem");

        if (nbttagcompound != null)
        {
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

            if (itemstack != null)
            {
                dataWatcher.updateObject(8, itemstack);
            }
        }
    }

    /**
     * Gets how bright this entity is.
     */
    public float getBrightness(float partialTicks)
    {
        return super.getBrightness(partialTicks);
    }

    public int getBrightnessForRender(float partialTicks)
    {
        return super.getBrightnessForRender(partialTicks);
    }

    /**
     * If returns false, the item will not inflict any damage against entities.
     */
    public boolean canAttackWithItem()
    {
        return false;
    }
}
