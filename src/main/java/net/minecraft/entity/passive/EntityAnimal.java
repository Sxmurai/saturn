package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityAnimal extends EntityAgeable implements IAnimals
{
    protected Block spawnableBlock = Blocks.grass;
    private int inLove;
    private EntityPlayer playerInLove;

    public EntityAnimal(World worldIn)
    {
        super(worldIn);
    }

    protected void updateAITasks()
    {
        if (getGrowingAge() != 0)
        {
            inLove = 0;
        }

        super.updateAITasks();
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        super.onLivingUpdate();

        if (getGrowingAge() != 0)
        {
            inLove = 0;
        }

        if (inLove > 0)
        {
            --inLove;

            if (inLove % 10 == 0)
            {
                double d0 = rand.nextGaussian() * 0.02D;
                double d1 = rand.nextGaussian() * 0.02D;
                double d2 = rand.nextGaussian() * 0.02D;
                worldObj.spawnParticle(EnumParticleTypes.HEART, posX + (double)(rand.nextFloat() * width * 2.0F) - (double) width, posY + 0.5D + (double)(rand.nextFloat() * height), posZ + (double)(rand.nextFloat() * width * 2.0F) - (double) width, d0, d1, d2);
            }
        }
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
            inLove = 0;
            return super.attackEntityFrom(source, amount);
        }
    }

    public float getBlockPathWeight(BlockPos pos)
    {
        return worldObj.getBlockState(pos.down()).getBlock() == Blocks.grass ? 10.0F : worldObj.getLightBrightness(pos) - 0.5F;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("InLove", inLove);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        inLove = tagCompund.getInteger("InLove");
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(getEntityBoundingBox().minY);
        int k = MathHelper.floor_double(posZ);
        BlockPos blockpos = new BlockPos(i, j, k);
        return worldObj.getBlockState(blockpos.down()).getBlock() == spawnableBlock && worldObj.getLight(blockpos) > 8 && super.getCanSpawnHere();
    }

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     */
    public int getTalkInterval()
    {
        return 120;
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    protected boolean canDespawn()
    {
        return false;
    }

    /**
     * Get the experience points the entity currently has.
     */
    protected int getExperiencePoints(EntityPlayer player)
    {
        return 1 + worldObj.rand.nextInt(3);
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isBreedingItem(ItemStack stack)
    {
        return stack != null && stack.getItem() == Items.wheat;
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean interact(EntityPlayer player)
    {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (itemstack != null)
        {
            if (isBreedingItem(itemstack) && getGrowingAge() == 0 && inLove <= 0)
            {
                consumeItemFromStack(player, itemstack);
                setInLove(player);
                return true;
            }

            if (isChild() && isBreedingItem(itemstack))
            {
                consumeItemFromStack(player, itemstack);
                func_175501_a((int)((float)(-getGrowingAge() / 20) * 0.1F), true);
                return true;
            }
        }

        return super.interact(player);
    }

    /**
     * Decreases ItemStack size by one
     */
    protected void consumeItemFromStack(EntityPlayer player, ItemStack stack)
    {
        if (!player.capabilities.isCreativeMode)
        {
            --stack.stackSize;

            if (stack.stackSize <= 0)
            {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
        }
    }

    public void setInLove(EntityPlayer player)
    {
        inLove = 600;
        playerInLove = player;
        worldObj.setEntityState(this, (byte)18);
    }

    public EntityPlayer getPlayerInLove()
    {
        return playerInLove;
    }

    /**
     * Returns if the entity is currently in 'love mode'.
     */
    public boolean isInLove()
    {
        return inLove > 0;
    }

    public void resetInLove()
    {
        inLove = 0;
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    public boolean canMateWith(EntityAnimal otherAnimal)
    {
        return otherAnimal != this && (otherAnimal.getClass() == getClass() && isInLove() && otherAnimal.isInLove());
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 18)
        {
            for (int i = 0; i < 7; ++i)
            {
                double d0 = rand.nextGaussian() * 0.02D;
                double d1 = rand.nextGaussian() * 0.02D;
                double d2 = rand.nextGaussian() * 0.02D;
                worldObj.spawnParticle(EnumParticleTypes.HEART, posX + (double)(rand.nextFloat() * width * 2.0F) - (double) width, posY + 0.5D + (double)(rand.nextFloat() * height), posZ + (double)(rand.nextFloat() * width * 2.0F) - (double) width, d0, d1, d2);
            }
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }
}
