package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityChicken extends EntityAnimal
{
    public float wingRotation;
    public float destPos;
    public float field_70884_g;
    public float field_70888_h;
    public float wingRotDelta = 1.0F;

    /** The time until the next egg is spawned. */
    public int timeUntilNextEgg;
    public boolean chickenJockey;

    public EntityChicken(World worldIn)
    {
        super(worldIn);
        setSize(0.4F, 0.7F);
        timeUntilNextEgg = rand.nextInt(6000) + 6000;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.4D));
        tasks.addTask(2, new EntityAIMate(this, 1.0D));
        tasks.addTask(3, new EntityAITempt(this, 1.0D, Items.wheat_seeds, false));
        tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        tasks.addTask(5, new EntityAIWander(this, 1.0D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
    }

    public float getEyeHeight()
    {
        return height;
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(4.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        field_70888_h = wingRotation;
        field_70884_g = destPos;
        destPos = (float)((double) destPos + (double)(onGround ? -1 : 4) * 0.3D);
        destPos = MathHelper.clamp_float(destPos, 0.0F, 1.0F);

        if (!onGround && wingRotDelta < 1.0F)
        {
            wingRotDelta = 1.0F;
        }

        wingRotDelta = (float)((double) wingRotDelta * 0.9D);

        if (!onGround && motionY < 0.0D)
        {
            motionY *= 0.6D;
        }

        wingRotation += wingRotDelta * 2.0F;

        if (!worldObj.isRemote && !isChild() && !isChickenJockey() && --timeUntilNextEgg <= 0)
        {
            playSound("mob.chicken.plop", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            dropItem(Items.egg, 1);
            timeUntilNextEgg = rand.nextInt(6000) + 6000;
        }
    }

    public void fall(float distance, float damageMultiplier)
    {
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return "mob.chicken.say";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.chicken.hurt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.chicken.hurt";
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        playSound("mob.chicken.step", 0.15F, 1.0F);
    }

    protected Item getDropItem()
    {
        return Items.feather;
    }

    /**
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
        int i = rand.nextInt(3) + rand.nextInt(1 + p_70628_2_);

        for (int j = 0; j < i; ++j)
        {
            dropItem(Items.feather, 1);
        }

        if (isBurning())
        {
            dropItem(Items.cooked_chicken, 1);
        }
        else
        {
            dropItem(Items.chicken, 1);
        }
    }

    public EntityChicken createChild(EntityAgeable ageable)
    {
        return new EntityChicken(worldObj);
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isBreedingItem(ItemStack stack)
    {
        return stack != null && stack.getItem() == Items.wheat_seeds;
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        chickenJockey = tagCompund.getBoolean("IsChickenJockey");

        if (tagCompund.hasKey("EggLayTime"))
        {
            timeUntilNextEgg = tagCompund.getInteger("EggLayTime");
        }
    }

    /**
     * Get the experience points the entity currently has.
     */
    protected int getExperiencePoints(EntityPlayer player)
    {
        return isChickenJockey() ? 10 : super.getExperiencePoints(player);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("IsChickenJockey", chickenJockey);
        tagCompound.setInteger("EggLayTime", timeUntilNextEgg);
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    protected boolean canDespawn()
    {
        return isChickenJockey() && riddenByEntity == null;
    }

    public void updateRiderPosition()
    {
        super.updateRiderPosition();
        float f = MathHelper.sin(renderYawOffset * (float)Math.PI / 180.0F);
        float f1 = MathHelper.cos(renderYawOffset * (float)Math.PI / 180.0F);
        float f2 = 0.1F;
        float f3 = 0.0F;
        riddenByEntity.setPosition(posX + (double)(f2 * f), posY + (double)(height * 0.5F) + riddenByEntity.getYOffset() + (double)f3, posZ - (double)(f2 * f1));

        if (riddenByEntity instanceof EntityLivingBase)
        {
            ((EntityLivingBase) riddenByEntity).renderYawOffset = renderYawOffset;
        }
    }

    /**
     * Determines if this chicken is a jokey with a zombie riding it.
     */
    public boolean isChickenJockey()
    {
        return chickenJockey;
    }

    /**
     * Sets whether this chicken is a jockey or not.
     */
    public void setChickenJockey(boolean jockey)
    {
        chickenJockey = jockey;
    }
}
