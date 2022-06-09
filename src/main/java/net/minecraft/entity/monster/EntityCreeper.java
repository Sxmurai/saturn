package net.minecraft.entity.monster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityCreeper extends EntityMob
{
    /**
     * Time when this creeper was last in an active state (Messed up code here, probably causes creeper animation to go
     * weird)
     */
    private int lastActiveTime;

    /**
     * The amount of time since the creeper was close enough to the player to ignite
     */
    private int timeSinceIgnited;
    private int fuseTime = 30;

    /** Explosion radius for this creeper. */
    private int explosionRadius = 3;
    private int field_175494_bm = 0;

    public EntityCreeper(World worldIn)
    {
        super(worldIn);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAICreeperSwell(this));
        tasks.addTask(3, new EntityAIAvoidEntity(this, EntityOcelot.class, 6.0F, 1.0D, 1.2D));
        tasks.addTask(4, new EntityAIAttackOnCollide(this, 1.0D, false));
        tasks.addTask(5, new EntityAIWander(this, 0.8D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(6, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
        targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    /**
     * The maximum height from where the entity is alowed to jump (used in pathfinder)
     */
    public int getMaxFallHeight()
    {
        return getAttackTarget() == null ? 3 : 3 + (int)(getHealth() - 1.0F);
    }

    public void fall(float distance, float damageMultiplier)
    {
        super.fall(distance, damageMultiplier);
        timeSinceIgnited = (int)((float) timeSinceIgnited + distance * 1.5F);

        if (timeSinceIgnited > fuseTime - 5)
        {
            timeSinceIgnited = fuseTime - 5;
        }
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, Byte.valueOf((byte) - 1));
        dataWatcher.addObject(17, Byte.valueOf((byte)0));
        dataWatcher.addObject(18, Byte.valueOf((byte)0));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);

        if (dataWatcher.getWatchableObjectByte(17) == 1)
        {
            tagCompound.setBoolean("powered", true);
        }

        tagCompound.setShort("Fuse", (short) fuseTime);
        tagCompound.setByte("ExplosionRadius", (byte) explosionRadius);
        tagCompound.setBoolean("ignited", hasIgnited());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        dataWatcher.updateObject(17, Byte.valueOf((byte)(tagCompund.getBoolean("powered") ? 1 : 0)));

        if (tagCompund.hasKey("Fuse", 99))
        {
            fuseTime = tagCompund.getShort("Fuse");
        }

        if (tagCompund.hasKey("ExplosionRadius", 99))
        {
            explosionRadius = tagCompund.getByte("ExplosionRadius");
        }

        if (tagCompund.getBoolean("ignited"))
        {
            ignite();
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        if (isEntityAlive())
        {
            lastActiveTime = timeSinceIgnited;

            if (hasIgnited())
            {
                setCreeperState(1);
            }

            int i = getCreeperState();

            if (i > 0 && timeSinceIgnited == 0)
            {
                playSound("creeper.primed", 1.0F, 0.5F);
            }

            timeSinceIgnited += i;

            if (timeSinceIgnited < 0)
            {
                timeSinceIgnited = 0;
            }

            if (timeSinceIgnited >= fuseTime)
            {
                timeSinceIgnited = fuseTime;
                explode();
            }
        }

        super.onUpdate();
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.creeper.say";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.creeper.death";
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);

        if (cause.getEntity() instanceof EntitySkeleton)
        {
            int i = Item.getIdFromItem(Items.record_13);
            int j = Item.getIdFromItem(Items.record_wait);
            int k = i + rand.nextInt(j - i + 1);
            dropItem(Item.getItemById(k), 1);
        }
        else if (cause.getEntity() instanceof EntityCreeper && cause.getEntity() != this && ((EntityCreeper)cause.getEntity()).getPowered() && ((EntityCreeper)cause.getEntity()).isAIEnabled())
        {
            ((EntityCreeper)cause.getEntity()).func_175493_co();
            entityDropItem(new ItemStack(Items.skull, 1, 4), 0.0F);
        }
    }

    public boolean attackEntityAsMob(Entity entityIn)
    {
        return true;
    }

    /**
     * Returns true if the creeper is powered by a lightning bolt.
     */
    public boolean getPowered()
    {
        return dataWatcher.getWatchableObjectByte(17) == 1;
    }

    /**
     * Params: (Float)Render tick. Returns the intensity of the creeper's flash when it is ignited.
     */
    public float getCreeperFlashIntensity(float p_70831_1_)
    {
        return ((float) lastActiveTime + (float)(timeSinceIgnited - lastActiveTime) * p_70831_1_) / (float)(fuseTime - 2);
    }

    protected Item getDropItem()
    {
        return Items.gunpowder;
    }

    /**
     * Returns the current state of creeper, -1 is idle, 1 is 'in fuse'
     */
    public int getCreeperState()
    {
        return dataWatcher.getWatchableObjectByte(16);
    }

    /**
     * Sets the state of creeper, -1 to idle and 1 to be 'in fuse'
     */
    public void setCreeperState(int state)
    {
        dataWatcher.updateObject(16, Byte.valueOf((byte)state));
    }

    /**
     * Called when a lightning bolt hits the entity.
     */
    public void onStruckByLightning(EntityLightningBolt lightningBolt)
    {
        super.onStruckByLightning(lightningBolt);
        dataWatcher.updateObject(17, Byte.valueOf((byte)1));
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    protected boolean interact(EntityPlayer player)
    {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() == Items.flint_and_steel)
        {
            worldObj.playSoundEffect(posX + 0.5D, posY + 0.5D, posZ + 0.5D, "fire.ignite", 1.0F, rand.nextFloat() * 0.4F + 0.8F);
            player.swingItem();

            if (!worldObj.isRemote)
            {
                ignite();
                itemstack.damageItem(1, player);
                return true;
            }
        }

        return super.interact(player);
    }

    /**
     * Creates an explosion as determined by this creeper's power and explosion radius.
     */
    private void explode()
    {
        if (!worldObj.isRemote)
        {
            boolean flag = worldObj.getGameRules().getBoolean("mobGriefing");
            float f = getPowered() ? 2.0F : 1.0F;
            worldObj.createExplosion(this, posX, posY, posZ, (float) explosionRadius * f, flag);
            setDead();
        }
    }

    public boolean hasIgnited()
    {
        return dataWatcher.getWatchableObjectByte(18) != 0;
    }

    public void ignite()
    {
        dataWatcher.updateObject(18, Byte.valueOf((byte)1));
    }

    /**
     * Returns true if the newer Entity AI code should be run
     */
    public boolean isAIEnabled()
    {
        return field_175494_bm < 1 && worldObj.getGameRules().getBoolean("doMobLoot");
    }

    public void func_175493_co()
    {
        ++field_175494_bm;
    }
}
