package net.minecraft.entity.monster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearest;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class EntitySlime extends EntityLiving implements IMob
{
    public float squishAmount;
    public float squishFactor;
    public float prevSquishFactor;
    private boolean wasOnGround;

    public EntitySlime(World worldIn)
    {
        super(worldIn);
        moveHelper = new EntitySlime.SlimeMoveHelper(this);
        tasks.addTask(1, new EntitySlime.AISlimeFloat(this));
        tasks.addTask(2, new EntitySlime.AISlimeAttack(this));
        tasks.addTask(3, new EntitySlime.AISlimeFaceRandom(this));
        tasks.addTask(5, new EntitySlime.AISlimeHop(this));
        targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
        targetTasks.addTask(3, new EntityAIFindEntityNearest(this, EntityIronGolem.class));
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, Byte.valueOf((byte)1));
    }

    protected void setSlimeSize(int size)
    {
        dataWatcher.updateObject(16, Byte.valueOf((byte)size));
        setSize(0.51000005F * (float)size, 0.51000005F * (float)size);
        setPosition(posX, posY, posZ);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(size * size);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.2F + 0.1F * (float)size);
        setHealth(getMaxHealth());
        experienceValue = size;
    }

    /**
     * Returns the size of the slime.
     */
    public int getSlimeSize()
    {
        return dataWatcher.getWatchableObjectByte(16);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("Size", getSlimeSize() - 1);
        tagCompound.setBoolean("wasOnGround", wasOnGround);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        int i = tagCompund.getInteger("Size");

        if (i < 0)
        {
            i = 0;
        }

        setSlimeSize(i + 1);
        wasOnGround = tagCompund.getBoolean("wasOnGround");
    }

    protected EnumParticleTypes getParticleType()
    {
        return EnumParticleTypes.SLIME;
    }

    /**
     * Returns the name of the sound played when the slime jumps.
     */
    protected String getJumpSound()
    {
        return "mob.slime." + (getSlimeSize() > 1 ? "big" : "small");
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        if (!worldObj.isRemote && worldObj.getDifficulty() == EnumDifficulty.PEACEFUL && getSlimeSize() > 0)
        {
            isDead = true;
        }

        squishFactor += (squishAmount - squishFactor) * 0.5F;
        prevSquishFactor = squishFactor;
        super.onUpdate();

        if (onGround && !wasOnGround)
        {
            int i = getSlimeSize();

            for (int j = 0; j < i * 8; ++j)
            {
                float f = rand.nextFloat() * (float)Math.PI * 2.0F;
                float f1 = rand.nextFloat() * 0.5F + 0.5F;
                float f2 = MathHelper.sin(f) * (float)i * 0.5F * f1;
                float f3 = MathHelper.cos(f) * (float)i * 0.5F * f1;
                World world = worldObj;
                EnumParticleTypes enumparticletypes = getParticleType();
                double d0 = posX + (double)f2;
                double d1 = posZ + (double)f3;
                world.spawnParticle(enumparticletypes, d0, getEntityBoundingBox().minY, d1, 0.0D, 0.0D, 0.0D);
            }

            if (makesSoundOnLand())
            {
                playSound(getJumpSound(), getSoundVolume(), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
            }

            squishAmount = -0.5F;
        }
        else if (!onGround && wasOnGround)
        {
            squishAmount = 1.0F;
        }

        wasOnGround = onGround;
        alterSquishAmount();
    }

    protected void alterSquishAmount()
    {
        squishAmount *= 0.6F;
    }

    /**
     * Gets the amount of time the slime needs to wait between jumps.
     */
    protected int getJumpDelay()
    {
        return rand.nextInt(20) + 10;
    }

    protected EntitySlime createInstance()
    {
        return new EntitySlime(worldObj);
    }

    public void onDataWatcherUpdate(int dataID)
    {
        if (dataID == 16)
        {
            int i = getSlimeSize();
            setSize(0.51000005F * (float)i, 0.51000005F * (float)i);
            rotationYaw = rotationYawHead;
            renderYawOffset = rotationYawHead;

            if (isInWater() && rand.nextInt(20) == 0)
            {
                resetHeight();
            }
        }

        super.onDataWatcherUpdate(dataID);
    }

    /**
     * Will get destroyed next tick.
     */
    public void setDead()
    {
        int i = getSlimeSize();

        if (!worldObj.isRemote && i > 1 && getHealth() <= 0.0F)
        {
            int j = 2 + rand.nextInt(3);

            for (int k = 0; k < j; ++k)
            {
                float f = ((float)(k % 2) - 0.5F) * (float)i / 4.0F;
                float f1 = ((float)(k / 2) - 0.5F) * (float)i / 4.0F;
                EntitySlime entityslime = createInstance();

                if (hasCustomName())
                {
                    entityslime.setCustomNameTag(getCustomNameTag());
                }

                if (isNoDespawnRequired())
                {
                    entityslime.enablePersistence();
                }

                entityslime.setSlimeSize(i / 2);
                entityslime.setLocationAndAngles(posX + (double)f, posY + 0.5D, posZ + (double)f1, rand.nextFloat() * 360.0F, 0.0F);
                worldObj.spawnEntityInWorld(entityslime);
            }
        }

        super.setDead();
    }

    /**
     * Applies a velocity to each of the entities pushing them away from each other. Args: entity
     */
    public void applyEntityCollision(Entity entityIn)
    {
        super.applyEntityCollision(entityIn);

        if (entityIn instanceof EntityIronGolem && canDamagePlayer())
        {
            func_175451_e((EntityLivingBase)entityIn);
        }
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    public void onCollideWithPlayer(EntityPlayer entityIn)
    {
        if (canDamagePlayer())
        {
            func_175451_e(entityIn);
        }
    }

    protected void func_175451_e(EntityLivingBase p_175451_1_)
    {
        int i = getSlimeSize();

        if (canEntityBeSeen(p_175451_1_) && getDistanceSqToEntity(p_175451_1_) < 0.6D * (double)i * 0.6D * (double)i && p_175451_1_.attackEntityFrom(DamageSource.causeMobDamage(this), (float) getAttackStrength()))
        {
            playSound("mob.attack", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            applyEnchantments(this, p_175451_1_);
        }
    }

    public float getEyeHeight()
    {
        return 0.625F * height;
    }

    /**
     * Indicates weather the slime is able to damage the player (based upon the slime's size)
     */
    protected boolean canDamagePlayer()
    {
        return getSlimeSize() > 1;
    }

    /**
     * Gets the amount of damage dealt to the player when "attacked" by the slime.
     */
    protected int getAttackStrength()
    {
        return getSlimeSize();
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.slime." + (getSlimeSize() > 1 ? "big" : "small");
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.slime." + (getSlimeSize() > 1 ? "big" : "small");
    }

    protected Item getDropItem()
    {
        return getSlimeSize() == 1 ? Items.slime_ball : null;
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        BlockPos blockpos = new BlockPos(MathHelper.floor_double(posX), 0, MathHelper.floor_double(posZ));
        Chunk chunk = worldObj.getChunkFromBlockCoords(blockpos);

        if (worldObj.getWorldInfo().getTerrainType() == WorldType.FLAT && rand.nextInt(4) != 1)
        {
            return false;
        }
        else
        {
            if (worldObj.getDifficulty() != EnumDifficulty.PEACEFUL)
            {
                BiomeGenBase biomegenbase = worldObj.getBiomeGenForCoords(blockpos);

                if (biomegenbase == BiomeGenBase.swampland && posY > 50.0D && posY < 70.0D && rand.nextFloat() < 0.5F && rand.nextFloat() < worldObj.getCurrentMoonPhaseFactor() && worldObj.getLightFromNeighbors(new BlockPos(this)) <= rand.nextInt(8))
                {
                    return super.getCanSpawnHere();
                }

                if (rand.nextInt(10) == 0 && chunk.getRandomWithSeed(987234911L).nextInt(10) == 0 && posY < 40.0D)
                {
                    return super.getCanSpawnHere();
                }
            }

            return false;
        }
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.4F * (float) getSlimeSize();
    }

    /**
     * The speed it takes to move the entityliving's rotationPitch through the faceEntity method. This is only currently
     * use in wolves.
     */
    public int getVerticalFaceSpeed()
    {
        return 0;
    }

    /**
     * Returns true if the slime makes a sound when it jumps (based upon the slime's size)
     */
    protected boolean makesSoundOnJump()
    {
        return getSlimeSize() > 0;
    }

    /**
     * Returns true if the slime makes a sound when it lands after a jump (based upon the slime's size)
     */
    protected boolean makesSoundOnLand()
    {
        return getSlimeSize() > 2;
    }

    /**
     * Causes this entity to do an upwards motion (jumping).
     */
    protected void jump()
    {
        motionY = 0.41999998688697815D;
        isAirBorne = true;
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata)
    {
        int i = rand.nextInt(3);

        if (i < 2 && rand.nextFloat() < 0.5F * difficulty.getClampedAdditionalDifficulty())
        {
            ++i;
        }

        int j = 1 << i;
        setSlimeSize(j);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    static class AISlimeAttack extends EntityAIBase
    {
        private final EntitySlime slime;
        private int field_179465_b;

        public AISlimeAttack(EntitySlime p_i45824_1_)
        {
            slime = p_i45824_1_;
            setMutexBits(2);
        }

        public boolean shouldExecute()
        {
            EntityLivingBase entitylivingbase = slime.getAttackTarget();
            return entitylivingbase != null && (entitylivingbase.isEntityAlive() && (!(entitylivingbase instanceof EntityPlayer) || !((EntityPlayer) entitylivingbase).capabilities.disableDamage));
        }

        public void startExecuting()
        {
            field_179465_b = 300;
            super.startExecuting();
        }

        public boolean continueExecuting()
        {
            EntityLivingBase entitylivingbase = slime.getAttackTarget();
            return entitylivingbase != null && (entitylivingbase.isEntityAlive() && ((!(entitylivingbase instanceof EntityPlayer) || !((EntityPlayer) entitylivingbase).capabilities.disableDamage) && --field_179465_b > 0));
        }

        public void updateTask()
        {
            slime.faceEntity(slime.getAttackTarget(), 10.0F, 10.0F);
            ((EntitySlime.SlimeMoveHelper) slime.getMoveHelper()).func_179920_a(slime.rotationYaw, slime.canDamagePlayer());
        }
    }

    static class AISlimeFaceRandom extends EntityAIBase
    {
        private final EntitySlime slime;
        private float field_179459_b;
        private int field_179460_c;

        public AISlimeFaceRandom(EntitySlime p_i45820_1_)
        {
            slime = p_i45820_1_;
            setMutexBits(2);
        }

        public boolean shouldExecute()
        {
            return slime.getAttackTarget() == null && (slime.onGround || slime.isInWater() || slime.isInLava());
        }

        public void updateTask()
        {
            if (--field_179460_c <= 0)
            {
                field_179460_c = 40 + slime.getRNG().nextInt(60);
                field_179459_b = (float) slime.getRNG().nextInt(360);
            }

            ((EntitySlime.SlimeMoveHelper) slime.getMoveHelper()).func_179920_a(field_179459_b, false);
        }
    }

    static class AISlimeFloat extends EntityAIBase
    {
        private final EntitySlime slime;

        public AISlimeFloat(EntitySlime p_i45823_1_)
        {
            slime = p_i45823_1_;
            setMutexBits(5);
            ((PathNavigateGround)p_i45823_1_.getNavigator()).setCanSwim(true);
        }

        public boolean shouldExecute()
        {
            return slime.isInWater() || slime.isInLava();
        }

        public void updateTask()
        {
            if (slime.getRNG().nextFloat() < 0.8F)
            {
                slime.getJumpHelper().setJumping();
            }

            ((EntitySlime.SlimeMoveHelper) slime.getMoveHelper()).setSpeed(1.2D);
        }
    }

    static class AISlimeHop extends EntityAIBase
    {
        private final EntitySlime slime;

        public AISlimeHop(EntitySlime p_i45822_1_)
        {
            slime = p_i45822_1_;
            setMutexBits(5);
        }

        public boolean shouldExecute()
        {
            return true;
        }

        public void updateTask()
        {
            ((EntitySlime.SlimeMoveHelper) slime.getMoveHelper()).setSpeed(1.0D);
        }
    }

    static class SlimeMoveHelper extends EntityMoveHelper
    {
        private float field_179922_g;
        private int field_179924_h;
        private final EntitySlime slime;
        private boolean field_179923_j;

        public SlimeMoveHelper(EntitySlime p_i45821_1_)
        {
            super(p_i45821_1_);
            slime = p_i45821_1_;
        }

        public void func_179920_a(float p_179920_1_, boolean p_179920_2_)
        {
            field_179922_g = p_179920_1_;
            field_179923_j = p_179920_2_;
        }

        public void setSpeed(double speedIn)
        {
            speed = speedIn;
            update = true;
        }

        public void onUpdateMoveHelper()
        {
            entity.rotationYaw = limitAngle(entity.rotationYaw, field_179922_g, 30.0F);
            entity.rotationYawHead = entity.rotationYaw;
            entity.renderYawOffset = entity.rotationYaw;

            if (!update)
            {
                entity.setMoveForward(0.0F);
            }
            else
            {
                update = false;

                if (entity.onGround)
                {
                    entity.setAIMoveSpeed((float)(speed * entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue()));

                    if (field_179924_h-- <= 0)
                    {
                        field_179924_h = slime.getJumpDelay();

                        if (field_179923_j)
                        {
                            field_179924_h /= 3;
                        }

                        slime.getJumpHelper().setJumping();

                        if (slime.makesSoundOnJump())
                        {
                            slime.playSound(slime.getJumpSound(), slime.getSoundVolume(), ((slime.getRNG().nextFloat() - slime.getRNG().nextFloat()) * 0.2F + 1.0F) * 0.8F);
                        }
                    }
                    else
                    {
                        slime.moveStrafing = slime.moveForward = 0.0F;
                        entity.setAIMoveSpeed(0.0F);
                    }
                }
                else
                {
                    entity.setAIMoveSpeed((float)(speed * entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue()));
                }
            }
        }
    }
}
