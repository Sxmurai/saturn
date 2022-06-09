package net.minecraft.entity.monster;

import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityGuardian extends EntityMob
{
    private float field_175482_b;
    private float field_175484_c;
    private float field_175483_bk;
    private float field_175485_bl;
    private float field_175486_bm;
    private EntityLivingBase targetedEntity;
    private int field_175479_bo;
    private boolean field_175480_bp;
    private final EntityAIWander wander;

    public EntityGuardian(World worldIn)
    {
        super(worldIn);
        experienceValue = 10;
        setSize(0.85F, 0.85F);
        tasks.addTask(4, new EntityGuardian.AIGuardianAttack(this));
        EntityAIMoveTowardsRestriction entityaimovetowardsrestriction;
        tasks.addTask(5, entityaimovetowardsrestriction = new EntityAIMoveTowardsRestriction(this, 1.0D));
        tasks.addTask(7, wander = new EntityAIWander(this, 1.0D, 80));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityGuardian.class, 12.0F, 0.01F));
        tasks.addTask(9, new EntityAILookIdle(this));
        wander.setMutexBits(3);
        entityaimovetowardsrestriction.setMutexBits(3);
        targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 10, true, false, new EntityGuardian.GuardianTargetSelector(this)));
        moveHelper = new EntityGuardian.GuardianMoveHelper(this);
        field_175484_c = field_175482_b = rand.nextFloat();
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0D);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        setElder(tagCompund.getBoolean("Elder"));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("Elder", isElder());
    }

    /**
     * Returns new PathNavigateGround instance
     */
    protected PathNavigate getNewNavigator(World worldIn)
    {
        return new PathNavigateSwimmer(this, worldIn);
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, Integer.valueOf(0));
        dataWatcher.addObject(17, Integer.valueOf(0));
    }

    /**
     * Returns true if given flag is set
     */
    private boolean isSyncedFlagSet(int flagId)
    {
        return (dataWatcher.getWatchableObjectInt(16) & flagId) != 0;
    }

    /**
     * Sets a flag state "on/off" on both sides (client/server) by using DataWatcher
     */
    private void setSyncedFlag(int flagId, boolean state)
    {
        int i = dataWatcher.getWatchableObjectInt(16);

        if (state)
        {
            dataWatcher.updateObject(16, Integer.valueOf(i | flagId));
        }
        else
        {
            dataWatcher.updateObject(16, Integer.valueOf(i & ~flagId));
        }
    }

    public boolean func_175472_n()
    {
        return isSyncedFlagSet(2);
    }

    private void func_175476_l(boolean p_175476_1_)
    {
        setSyncedFlag(2, p_175476_1_);
    }

    public int func_175464_ck()
    {
        return isElder() ? 60 : 80;
    }

    public boolean isElder()
    {
        return isSyncedFlagSet(4);
    }

    /**
     * Sets this Guardian to be an elder or not.
     */
    public void setElder(boolean elder)
    {
        setSyncedFlag(4, elder);

        if (elder)
        {
            setSize(1.9975F, 1.9975F);
            getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
            getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(8.0D);
            getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(80.0D);
            enablePersistence();
            wander.setExecutionChance(400);
        }
    }

    public void setElder()
    {
        setElder(true);
        field_175486_bm = field_175485_bl = 1.0F;
    }

    private void setTargetedEntity(int entityId)
    {
        dataWatcher.updateObject(17, Integer.valueOf(entityId));
    }

    public boolean hasTargetedEntity()
    {
        return dataWatcher.getWatchableObjectInt(17) != 0;
    }

    public EntityLivingBase getTargetedEntity()
    {
        if (!hasTargetedEntity())
        {
            return null;
        }
        else if (worldObj.isRemote)
        {
            if (targetedEntity != null)
            {
                return targetedEntity;
            }
            else
            {
                Entity entity = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(17));

                if (entity instanceof EntityLivingBase)
                {
                    targetedEntity = (EntityLivingBase)entity;
                    return targetedEntity;
                }
                else
                {
                    return null;
                }
            }
        }
        else
        {
            return getAttackTarget();
        }
    }

    public void onDataWatcherUpdate(int dataID)
    {
        super.onDataWatcherUpdate(dataID);

        if (dataID == 16)
        {
            if (isElder() && width < 1.0F)
            {
                setSize(1.9975F, 1.9975F);
            }
        }
        else if (dataID == 17)
        {
            field_175479_bo = 0;
            targetedEntity = null;
        }
    }

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     */
    public int getTalkInterval()
    {
        return 160;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return !isInWater() ? "mob.guardian.land.idle" : (isElder() ? "mob.guardian.elder.idle" : "mob.guardian.idle");
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return !isInWater() ? "mob.guardian.land.hit" : (isElder() ? "mob.guardian.elder.hit" : "mob.guardian.hit");
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return !isInWater() ? "mob.guardian.land.death" : (isElder() ? "mob.guardian.elder.death" : "mob.guardian.death");
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    public float getEyeHeight()
    {
        return height * 0.5F;
    }

    public float getBlockPathWeight(BlockPos pos)
    {
        return worldObj.getBlockState(pos).getBlock().getMaterial() == Material.water ? 10.0F + worldObj.getLightBrightness(pos) - 0.5F : super.getBlockPathWeight(pos);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        if (worldObj.isRemote)
        {
            field_175484_c = field_175482_b;

            if (!isInWater())
            {
                field_175483_bk = 2.0F;

                if (motionY > 0.0D && field_175480_bp && !isSilent())
                {
                    worldObj.playSound(posX, posY, posZ, "mob.guardian.flop", 1.0F, 1.0F, false);
                }

                field_175480_bp = motionY < 0.0D && worldObj.isBlockNormalCube((new BlockPos(this)).down(), false);
            }
            else if (func_175472_n())
            {
                if (field_175483_bk < 0.5F)
                {
                    field_175483_bk = 4.0F;
                }
                else
                {
                    field_175483_bk += (0.5F - field_175483_bk) * 0.1F;
                }
            }
            else
            {
                field_175483_bk += (0.125F - field_175483_bk) * 0.2F;
            }

            field_175482_b += field_175483_bk;
            field_175486_bm = field_175485_bl;

            if (!isInWater())
            {
                field_175485_bl = rand.nextFloat();
            }
            else if (func_175472_n())
            {
                field_175485_bl += (0.0F - field_175485_bl) * 0.25F;
            }
            else
            {
                field_175485_bl += (1.0F - field_175485_bl) * 0.06F;
            }

            if (func_175472_n() && isInWater())
            {
                Vec3 vec3 = getLook(0.0F);

                for (int i = 0; i < 2; ++i)
                {
                    worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX + (rand.nextDouble() - 0.5D) * (double) width - vec3.xCoord * 1.5D, posY + rand.nextDouble() * (double) height - vec3.yCoord * 1.5D, posZ + (rand.nextDouble() - 0.5D) * (double) width - vec3.zCoord * 1.5D, 0.0D, 0.0D, 0.0D);
                }
            }

            if (hasTargetedEntity())
            {
                if (field_175479_bo < func_175464_ck())
                {
                    ++field_175479_bo;
                }

                EntityLivingBase entitylivingbase = getTargetedEntity();

                if (entitylivingbase != null)
                {
                    getLookHelper().setLookPositionWithEntity(entitylivingbase, 90.0F, 90.0F);
                    getLookHelper().onUpdateLook();
                    double d5 = func_175477_p(0.0F);
                    double d0 = entitylivingbase.posX - posX;
                    double d1 = entitylivingbase.posY + (double)(entitylivingbase.height * 0.5F) - (posY + (double) getEyeHeight());
                    double d2 = entitylivingbase.posZ - posZ;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    d0 = d0 / d3;
                    d1 = d1 / d3;
                    d2 = d2 / d3;
                    double d4 = rand.nextDouble();

                    while (d4 < d3)
                    {
                        d4 += 1.8D - d5 + rand.nextDouble() * (1.7D - d5);
                        worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX + d0 * d4, posY + d1 * d4 + (double) getEyeHeight(), posZ + d2 * d4, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }

        if (inWater)
        {
            setAir(300);
        }
        else if (onGround)
        {
            motionY += 0.5D;
            motionX += (rand.nextFloat() * 2.0F - 1.0F) * 0.4F;
            motionZ += (rand.nextFloat() * 2.0F - 1.0F) * 0.4F;
            rotationYaw = rand.nextFloat() * 360.0F;
            onGround = false;
            isAirBorne = true;
        }

        if (hasTargetedEntity())
        {
            rotationYaw = rotationYawHead;
        }

        super.onLivingUpdate();
    }

    public float func_175471_a(float p_175471_1_)
    {
        return field_175484_c + (field_175482_b - field_175484_c) * p_175471_1_;
    }

    public float func_175469_o(float p_175469_1_)
    {
        return field_175486_bm + (field_175485_bl - field_175486_bm) * p_175469_1_;
    }

    public float func_175477_p(float p_175477_1_)
    {
        return ((float) field_175479_bo + p_175477_1_) / (float) func_175464_ck();
    }

    protected void updateAITasks()
    {
        super.updateAITasks();

        if (isElder())
        {
            int i = 1200;
            int j = 1200;
            int k = 6000;
            int l = 2;

            if ((ticksExisted + getEntityId()) % 1200 == 0)
            {
                Potion potion = Potion.digSlowdown;

                for (EntityPlayerMP entityplayermp : worldObj.getPlayers(EntityPlayerMP.class, new Predicate<EntityPlayerMP>()
            {
                public boolean apply(EntityPlayerMP p_apply_1_)
                    {
                        return getDistanceSqToEntity(p_apply_1_) < 2500.0D && p_apply_1_.theItemInWorldManager.survivalOrAdventure();
                    }
                }))
                {
                    if (!entityplayermp.isPotionActive(potion) || entityplayermp.getActivePotionEffect(potion).getAmplifier() < 2 || entityplayermp.getActivePotionEffect(potion).getDuration() < 1200)
                    {
                        entityplayermp.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(10, 0.0F));
                        entityplayermp.addPotionEffect(new PotionEffect(potion.id, 6000, 2));
                    }
                }
            }

            if (!hasHome())
            {
                setHomePosAndDistance(new BlockPos(this), 16);
            }
        }
    }

    /**
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
        int i = rand.nextInt(3) + rand.nextInt(p_70628_2_ + 1);

        if (i > 0)
        {
            entityDropItem(new ItemStack(Items.prismarine_shard, i, 0), 1.0F);
        }

        if (rand.nextInt(3 + p_70628_2_) > 1)
        {
            entityDropItem(new ItemStack(Items.fish, 1, ItemFishFood.FishType.COD.getMetadata()), 1.0F);
        }
        else if (rand.nextInt(3 + p_70628_2_) > 1)
        {
            entityDropItem(new ItemStack(Items.prismarine_crystals, 1, 0), 1.0F);
        }

        if (p_70628_1_ && isElder())
        {
            entityDropItem(new ItemStack(Blocks.sponge, 1, 1), 1.0F);
        }
    }

    /**
     * Causes this Entity to drop a random item.
     */
    protected void addRandomDrop()
    {
        ItemStack itemstack = WeightedRandom.getRandomItem(rand, EntityFishHook.func_174855_j()).getItemStack(rand);
        entityDropItem(itemstack, 1.0F);
    }

    /**
     * Checks to make sure the light is not too bright where the mob is spawning
     */
    protected boolean isValidLightLevel()
    {
        return true;
    }

    /**
     * Checks that the entity is not colliding with any blocks / liquids
     */
    public boolean isNotColliding()
    {
        return worldObj.checkNoEntityCollision(getEntityBoundingBox(), this) && worldObj.getCollidingBoundingBoxes(this, getEntityBoundingBox()).isEmpty();
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        return (rand.nextInt(20) == 0 || !worldObj.canBlockSeeSky(new BlockPos(this))) && super.getCanSpawnHere();
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (!func_175472_n() && !source.isMagicDamage() && source.getSourceOfDamage() instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase)source.getSourceOfDamage();

            if (!source.isExplosion())
            {
                entitylivingbase.attackEntityFrom(DamageSource.causeThornsDamage(this), 2.0F);
                entitylivingbase.playSound("damage.thorns", 0.5F, 1.0F);
            }
        }

        wander.makeUpdate();
        return super.attackEntityFrom(source, amount);
    }

    /**
     * The speed it takes to move the entityliving's rotationPitch through the faceEntity method. This is only currently
     * use in wolves.
     */
    public int getVerticalFaceSpeed()
    {
        return 180;
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public void moveEntityWithHeading(float strafe, float forward)
    {
        if (isServerWorld())
        {
            if (isInWater())
            {
                moveFlying(strafe, forward, 0.1F);
                moveEntity(motionX, motionY, motionZ);
                motionX *= 0.8999999761581421D;
                motionY *= 0.8999999761581421D;
                motionZ *= 0.8999999761581421D;

                if (!func_175472_n() && getAttackTarget() == null)
                {
                    motionY -= 0.005D;
                }
            }
            else
            {
                super.moveEntityWithHeading(strafe, forward);
            }
        }
        else
        {
            super.moveEntityWithHeading(strafe, forward);
        }
    }

    static class AIGuardianAttack extends EntityAIBase
    {
        private final EntityGuardian theEntity;
        private int tickCounter;

        public AIGuardianAttack(EntityGuardian p_i45833_1_)
        {
            theEntity = p_i45833_1_;
            setMutexBits(3);
        }

        public boolean shouldExecute()
        {
            EntityLivingBase entitylivingbase = theEntity.getAttackTarget();
            return entitylivingbase != null && entitylivingbase.isEntityAlive();
        }

        public boolean continueExecuting()
        {
            return super.continueExecuting() && (theEntity.isElder() || theEntity.getDistanceSqToEntity(theEntity.getAttackTarget()) > 9.0D);
        }

        public void startExecuting()
        {
            tickCounter = -10;
            theEntity.getNavigator().clearPathEntity();
            theEntity.getLookHelper().setLookPositionWithEntity(theEntity.getAttackTarget(), 90.0F, 90.0F);
            theEntity.isAirBorne = true;
        }

        public void resetTask()
        {
            theEntity.setTargetedEntity(0);
            theEntity.setAttackTarget(null);
            theEntity.wander.makeUpdate();
        }

        public void updateTask()
        {
            EntityLivingBase entitylivingbase = theEntity.getAttackTarget();
            theEntity.getNavigator().clearPathEntity();
            theEntity.getLookHelper().setLookPositionWithEntity(entitylivingbase, 90.0F, 90.0F);

            if (!theEntity.canEntityBeSeen(entitylivingbase))
            {
                theEntity.setAttackTarget(null);
            }
            else
            {
                ++tickCounter;

                if (tickCounter == 0)
                {
                    theEntity.setTargetedEntity(theEntity.getAttackTarget().getEntityId());
                    theEntity.worldObj.setEntityState(theEntity, (byte)21);
                }
                else if (tickCounter >= theEntity.func_175464_ck())
                {
                    float f = 1.0F;

                    if (theEntity.worldObj.getDifficulty() == EnumDifficulty.HARD)
                    {
                        f += 2.0F;
                    }

                    if (theEntity.isElder())
                    {
                        f += 2.0F;
                    }

                    entitylivingbase.attackEntityFrom(DamageSource.causeIndirectMagicDamage(theEntity, theEntity), f);
                    entitylivingbase.attackEntityFrom(DamageSource.causeMobDamage(theEntity), (float) theEntity.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
                    theEntity.setAttackTarget(null);
                }
                else if (tickCounter >= 60 && tickCounter % 20 == 0)
                {
                }

                super.updateTask();
            }
        }
    }

    static class GuardianMoveHelper extends EntityMoveHelper
    {
        private final EntityGuardian entityGuardian;

        public GuardianMoveHelper(EntityGuardian p_i45831_1_)
        {
            super(p_i45831_1_);
            entityGuardian = p_i45831_1_;
        }

        public void onUpdateMoveHelper()
        {
            if (update && !entityGuardian.getNavigator().noPath())
            {
                double d0 = posX - entityGuardian.posX;
                double d1 = posY - entityGuardian.posY;
                double d2 = posZ - entityGuardian.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                d3 = MathHelper.sqrt_double(d3);
                d1 = d1 / d3;
                float f = (float)(MathHelper.func_181159_b(d2, d0) * 180.0D / Math.PI) - 90.0F;
                entityGuardian.rotationYaw = limitAngle(entityGuardian.rotationYaw, f, 30.0F);
                entityGuardian.renderYawOffset = entityGuardian.rotationYaw;
                float f1 = (float)(speed * entityGuardian.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
                entityGuardian.setAIMoveSpeed(entityGuardian.getAIMoveSpeed() + (f1 - entityGuardian.getAIMoveSpeed()) * 0.125F);
                double d4 = Math.sin((double)(entityGuardian.ticksExisted + entityGuardian.getEntityId()) * 0.5D) * 0.05D;
                double d5 = Math.cos(entityGuardian.rotationYaw * (float)Math.PI / 180.0F);
                double d6 = Math.sin(entityGuardian.rotationYaw * (float)Math.PI / 180.0F);
                entityGuardian.motionX += d4 * d5;
                entityGuardian.motionZ += d4 * d6;
                d4 = Math.sin((double)(entityGuardian.ticksExisted + entityGuardian.getEntityId()) * 0.75D) * 0.05D;
                entityGuardian.motionY += d4 * (d6 + d5) * 0.25D;
                entityGuardian.motionY += (double) entityGuardian.getAIMoveSpeed() * d1 * 0.1D;
                EntityLookHelper entitylookhelper = entityGuardian.getLookHelper();
                double d7 = entityGuardian.posX + d0 / d3 * 2.0D;
                double d8 = (double) entityGuardian.getEyeHeight() + entityGuardian.posY + d1 / d3 * 1.0D;
                double d9 = entityGuardian.posZ + d2 / d3 * 2.0D;
                double d10 = entitylookhelper.getLookPosX();
                double d11 = entitylookhelper.getLookPosY();
                double d12 = entitylookhelper.getLookPosZ();

                if (!entitylookhelper.getIsLooking())
                {
                    d10 = d7;
                    d11 = d8;
                    d12 = d9;
                }

                entityGuardian.getLookHelper().setLookPosition(d10 + (d7 - d10) * 0.125D, d11 + (d8 - d11) * 0.125D, d12 + (d9 - d12) * 0.125D, 10.0F, 40.0F);
                entityGuardian.func_175476_l(true);
            }
            else
            {
                entityGuardian.setAIMoveSpeed(0.0F);
                entityGuardian.func_175476_l(false);
            }
        }
    }

    static class GuardianTargetSelector implements Predicate<EntityLivingBase>
    {
        private final EntityGuardian parentEntity;

        public GuardianTargetSelector(EntityGuardian p_i45832_1_)
        {
            parentEntity = p_i45832_1_;
        }

        public boolean apply(EntityLivingBase p_apply_1_)
        {
            return (p_apply_1_ instanceof EntityPlayer || p_apply_1_ instanceof EntitySquid) && p_apply_1_.getDistanceSqToEntity(parentEntity) > 9.0D;
        }
    }
}
