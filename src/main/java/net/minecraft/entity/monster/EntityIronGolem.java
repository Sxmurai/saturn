package net.minecraft.entity.monster;

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIDefendVillage;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookAtVillager;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class EntityIronGolem extends EntityGolem
{
    /** deincrements, and a distance-to-home check is done at 0 */
    private int homeCheckTimer;
    Village villageObj;
    private int attackTimer;
    private int holdRoseTick;

    public EntityIronGolem(World worldIn)
    {
        super(worldIn);
        setSize(1.4F, 2.9F);
        ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
        tasks.addTask(1, new EntityAIAttackOnCollide(this, 1.0D, true));
        tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9D, 32.0F));
        tasks.addTask(3, new EntityAIMoveThroughVillage(this, 0.6D, true));
        tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0D));
        tasks.addTask(5, new EntityAILookAtVillager(this));
        tasks.addTask(6, new EntityAIWander(this, 0.6D));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIDefendVillage(this));
        targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(3, new EntityIronGolem.AINearestAttackableTargetNonCreeper(this, EntityLiving.class, 10, false, true, IMob.VISIBLE_MOB_SELECTOR));
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, Byte.valueOf((byte)0));
    }

    protected void updateAITasks()
    {
        if (--homeCheckTimer <= 0)
        {
            homeCheckTimer = 70 + rand.nextInt(50);
            villageObj = worldObj.getVillageCollection().getNearestVillage(new BlockPos(this), 32);

            if (villageObj == null)
            {
                detachHome();
            }
            else
            {
                BlockPos blockpos = villageObj.getCenter();
                setHomePosAndDistance(blockpos, (int)((float) villageObj.getVillageRadius() * 0.6F));
            }
        }

        super.updateAITasks();
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(100.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    /**
     * Decrements the entity's air supply when underwater
     */
    protected int decreaseAirSupply(int p_70682_1_)
    {
        return p_70682_1_;
    }

    protected void collideWithEntity(Entity p_82167_1_)
    {
        if (p_82167_1_ instanceof IMob && !(p_82167_1_ instanceof EntityCreeper) && getRNG().nextInt(20) == 0)
        {
            setAttackTarget((EntityLivingBase)p_82167_1_);
        }

        super.collideWithEntity(p_82167_1_);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        super.onLivingUpdate();

        if (attackTimer > 0)
        {
            --attackTimer;
        }

        if (holdRoseTick > 0)
        {
            --holdRoseTick;
        }

        if (motionX * motionX + motionZ * motionZ > 2.500000277905201E-7D && rand.nextInt(5) == 0)
        {
            int i = MathHelper.floor_double(posX);
            int j = MathHelper.floor_double(posY - 0.20000000298023224D);
            int k = MathHelper.floor_double(posZ);
            IBlockState iblockstate = worldObj.getBlockState(new BlockPos(i, j, k));
            Block block = iblockstate.getBlock();

            if (block.getMaterial() != Material.air)
            {
                worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK, posX + ((double) rand.nextFloat() - 0.5D) * (double) width, getEntityBoundingBox().minY + 0.1D, posZ + ((double) rand.nextFloat() - 0.5D) * (double) width, 4.0D * ((double) rand.nextFloat() - 0.5D), 0.5D, ((double) rand.nextFloat() - 0.5D) * 4.0D, Block.getStateId(iblockstate));
            }
        }
    }

    /**
     * Returns true if this entity can attack entities of the specified class.
     */
//    public boolean canAttackClass(Class <? extends EntityLivingBase > cls)
//    {
//        return this.isPlayerCreated() && EntityPlayer.class.isAssignableFrom(cls) ? false : (cls == EntityCreeper.class ? false : super.canAttackClass(cls));
//    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("PlayerCreated", isPlayerCreated());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        setPlayerCreated(tagCompund.getBoolean("PlayerCreated"));
    }

    public boolean attackEntityAsMob(Entity entityIn)
    {
        attackTimer = 10;
        worldObj.setEntityState(this, (byte)4);
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)(7 + rand.nextInt(15)));

        if (flag)
        {
            entityIn.motionY += 0.4000000059604645D;
            applyEnchantments(this, entityIn);
        }

        playSound("mob.irongolem.throw", 1.0F, 1.0F);
        return flag;
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 4)
        {
            attackTimer = 10;
            playSound("mob.irongolem.throw", 1.0F, 1.0F);
        }
        else if (id == 11)
        {
            holdRoseTick = 400;
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    public Village getVillage()
    {
        return villageObj;
    }

    public int getAttackTimer()
    {
        return attackTimer;
    }

    public void setHoldingRose(boolean p_70851_1_)
    {
        holdRoseTick = p_70851_1_ ? 400 : 0;
        worldObj.setEntityState(this, (byte)11);
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.irongolem.hit";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.irongolem.death";
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        playSound("mob.irongolem.walk", 1.0F, 1.0F);
    }

    /**
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
        int i = rand.nextInt(3);

        for (int j = 0; j < i; ++j)
        {
            dropItemWithOffset(Item.getItemFromBlock(Blocks.red_flower), 1, (float)BlockFlower.EnumFlowerType.POPPY.getMeta());
        }

        int l = 3 + rand.nextInt(3);

        for (int k = 0; k < l; ++k)
        {
            dropItem(Items.iron_ingot, 1);
        }
    }

    public int getHoldRoseTick()
    {
        return holdRoseTick;
    }

    public boolean isPlayerCreated()
    {
        return (dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    public void setPlayerCreated(boolean p_70849_1_)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (p_70849_1_)
        {
            dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 | 1)));
        }
        else
        {
            dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & -2)));
        }
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource cause)
    {
        if (!isPlayerCreated() && attackingPlayer != null && villageObj != null)
        {
            villageObj.setReputationForPlayer(attackingPlayer.getName(), -5);
        }

        super.onDeath(cause);
    }

    static class AINearestAttackableTargetNonCreeper<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T>
    {
        public AINearestAttackableTargetNonCreeper(final EntityCreature creature, Class<T> classTarget, int chance, boolean p_i45858_4_, boolean p_i45858_5_, final Predicate <? super T > p_i45858_6_)
        {
            super(creature, classTarget, chance, p_i45858_4_, p_i45858_5_, p_i45858_6_);
            targetEntitySelector = new Predicate<T>()
            {
                public boolean apply(T p_apply_1_)
                {
                    if (p_i45858_6_ != null && !p_i45858_6_.apply(p_apply_1_))
                    {
                        return false;
                    }
                    else if (p_apply_1_ instanceof EntityCreeper)
                    {
                        return false;
                    }
                    else
                    {
                        if (p_apply_1_ instanceof EntityPlayer)
                        {
                            double d0 = getTargetDistance();

                            if (p_apply_1_.isSneaking())
                            {
                                d0 *= 0.800000011920929D;
                            }

                            if (p_apply_1_.isInvisible())
                            {
                                float f = ((EntityPlayer)p_apply_1_).getArmorVisibility();

                                if (f < 0.1F)
                                {
                                    f = 0.1F;
                                }

                                d0 *= 0.7F * f;
                            }

                            if ((double)p_apply_1_.getDistanceToEntity(creature) > d0)
                            {
                                return false;
                            }
                        }

                        return isSuitableTarget(p_apply_1_, false);
                    }
                }
            };
        }
    }
}
