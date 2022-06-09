package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StringUtils;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;

public abstract class MobSpawnerBaseLogic
{
    /** The delay to spawn. */
    private int spawnDelay = 20;
    private String mobID = "Pig";
    private final List<MobSpawnerBaseLogic.WeightedRandomMinecart> minecartToSpawn = Lists.newArrayList();
    private MobSpawnerBaseLogic.WeightedRandomMinecart randomEntity;

    /** The rotation of the mob inside the mob spawner */
    private double mobRotation;

    /** the previous rotation of the mob inside the mob spawner */
    private double prevMobRotation;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;

    /** Cached instance of the entity to render inside the spawner. */
    private Entity cachedEntity;
    private int maxNearbyEntities = 6;

    /** The distance from which a player activates the spawner. */
    private int activatingRangeFromPlayer = 16;

    /** The range coefficient for spawning entities around. */
    private int spawnRange = 4;

    /**
     * Gets the entity name that should be spawned.
     */
    private String getEntityNameToSpawn()
    {
        if (getRandomEntity() == null)
        {
            if (mobID != null && mobID.equals("Minecart"))
            {
                mobID = "MinecartRideable";
            }

            return mobID;
        }
        else
        {
            return getRandomEntity().entityType;
        }
    }

    public void setEntityName(String name)
    {
        mobID = name;
    }

    /**
     * Returns true if there's a player close enough to this mob spawner to activate it.
     */
    private boolean isActivated()
    {
        BlockPos blockpos = getSpawnerPosition();
        return getSpawnerWorld().isAnyPlayerWithinRangeAt((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, activatingRangeFromPlayer);
    }

    public void updateSpawner()
    {
        if (isActivated())
        {
            BlockPos blockpos = getSpawnerPosition();

            if (getSpawnerWorld().isRemote)
            {
                double d3 = (float)blockpos.getX() + getSpawnerWorld().rand.nextFloat();
                double d4 = (float)blockpos.getY() + getSpawnerWorld().rand.nextFloat();
                double d5 = (float)blockpos.getZ() + getSpawnerWorld().rand.nextFloat();
                getSpawnerWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d3, d4, d5, 0.0D, 0.0D, 0.0D);
                getSpawnerWorld().spawnParticle(EnumParticleTypes.FLAME, d3, d4, d5, 0.0D, 0.0D, 0.0D);

                if (spawnDelay > 0)
                {
                    --spawnDelay;
                }

                prevMobRotation = mobRotation;
                mobRotation = (mobRotation + (double)(1000.0F / ((float) spawnDelay + 200.0F))) % 360.0D;
            }
            else
            {
                if (spawnDelay == -1)
                {
                    resetTimer();
                }

                if (spawnDelay > 0)
                {
                    --spawnDelay;
                    return;
                }

                boolean flag = false;

                for (int i = 0; i < spawnCount; ++i)
                {
                    Entity entity = EntityList.createEntityByName(getEntityNameToSpawn(), getSpawnerWorld());

                    if (entity == null)
                    {
                        return;
                    }

                    int j = getSpawnerWorld().getEntitiesWithinAABB(entity.getClass(), (new AxisAlignedBB(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() + 1, blockpos.getY() + 1, blockpos.getZ() + 1)).expand(spawnRange, spawnRange, spawnRange)).size();

                    if (j >= maxNearbyEntities)
                    {
                        resetTimer();
                        return;
                    }

                    double d0 = (double)blockpos.getX() + (getSpawnerWorld().rand.nextDouble() - getSpawnerWorld().rand.nextDouble()) * (double) spawnRange + 0.5D;
                    double d1 = blockpos.getY() + getSpawnerWorld().rand.nextInt(3) - 1;
                    double d2 = (double)blockpos.getZ() + (getSpawnerWorld().rand.nextDouble() - getSpawnerWorld().rand.nextDouble()) * (double) spawnRange + 0.5D;
                    EntityLiving entityliving = entity instanceof EntityLiving ? (EntityLiving)entity : null;
                    entity.setLocationAndAngles(d0, d1, d2, getSpawnerWorld().rand.nextFloat() * 360.0F, 0.0F);

                    if (entityliving == null || entityliving.getCanSpawnHere() && entityliving.isNotColliding())
                    {
                        spawnNewEntity(entity, true);
                        getSpawnerWorld().playAuxSFX(2004, blockpos, 0);

                        if (entityliving != null)
                        {
                            entityliving.spawnExplosionParticle();
                        }

                        flag = true;
                    }
                }

                if (flag)
                {
                    resetTimer();
                }
            }
        }
    }

    private Entity spawnNewEntity(Entity entityIn, boolean spawn)
    {
        if (getRandomEntity() != null)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            entityIn.writeToNBTOptional(nbttagcompound);

            for (String s : getRandomEntity().nbtData.getKeySet())
            {
                NBTBase nbtbase = getRandomEntity().nbtData.getTag(s);
                nbttagcompound.setTag(s, nbtbase.copy());
            }

            entityIn.readFromNBT(nbttagcompound);

            if (entityIn.worldObj != null && spawn)
            {
                entityIn.worldObj.spawnEntityInWorld(entityIn);
            }

            NBTTagCompound nbttagcompound2;

            for (Entity entity = entityIn; nbttagcompound.hasKey("Riding", 10); nbttagcompound = nbttagcompound2)
            {
                nbttagcompound2 = nbttagcompound.getCompoundTag("Riding");
                Entity entity1 = EntityList.createEntityByName(nbttagcompound2.getString("id"), entityIn.worldObj);

                if (entity1 != null)
                {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    entity1.writeToNBTOptional(nbttagcompound1);

                    for (String s1 : nbttagcompound2.getKeySet())
                    {
                        NBTBase nbtbase1 = nbttagcompound2.getTag(s1);
                        nbttagcompound1.setTag(s1, nbtbase1.copy());
                    }

                    entity1.readFromNBT(nbttagcompound1);
                    entity1.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

                    if (entityIn.worldObj != null && spawn)
                    {
                        entityIn.worldObj.spawnEntityInWorld(entity1);
                    }

                    entity.mountEntity(entity1);
                }

                entity = entity1;
            }
        }
        else if (entityIn instanceof EntityLivingBase && entityIn.worldObj != null && spawn)
        {
            if (entityIn instanceof EntityLiving)
            {
                ((EntityLiving)entityIn).onInitialSpawn(entityIn.worldObj.getDifficultyForLocation(new BlockPos(entityIn)), null);
            }

            entityIn.worldObj.spawnEntityInWorld(entityIn);
        }

        return entityIn;
    }

    private void resetTimer()
    {
        if (maxSpawnDelay <= minSpawnDelay)
        {
            spawnDelay = minSpawnDelay;
        }
        else
        {
            int i = maxSpawnDelay - minSpawnDelay;
            spawnDelay = minSpawnDelay + getSpawnerWorld().rand.nextInt(i);
        }

        if (minecartToSpawn.size() > 0)
        {
            setRandomEntity(WeightedRandom.getRandomItem(getSpawnerWorld().rand, minecartToSpawn));
        }

        func_98267_a(1);
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        mobID = nbt.getString("EntityId");
        spawnDelay = nbt.getShort("Delay");
        minecartToSpawn.clear();

        if (nbt.hasKey("SpawnPotentials", 9))
        {
            NBTTagList nbttaglist = nbt.getTagList("SpawnPotentials", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                minecartToSpawn.add(new MobSpawnerBaseLogic.WeightedRandomMinecart(nbttaglist.getCompoundTagAt(i)));
            }
        }

        if (nbt.hasKey("SpawnData", 10))
        {
            setRandomEntity(new MobSpawnerBaseLogic.WeightedRandomMinecart(nbt.getCompoundTag("SpawnData"), mobID));
        }
        else
        {
            setRandomEntity(null);
        }

        if (nbt.hasKey("MinSpawnDelay", 99))
        {
            minSpawnDelay = nbt.getShort("MinSpawnDelay");
            maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
            spawnCount = nbt.getShort("SpawnCount");
        }

        if (nbt.hasKey("MaxNearbyEntities", 99))
        {
            maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
            activatingRangeFromPlayer = nbt.getShort("RequiredPlayerRange");
        }

        if (nbt.hasKey("SpawnRange", 99))
        {
            spawnRange = nbt.getShort("SpawnRange");
        }

        if (getSpawnerWorld() != null)
        {
            cachedEntity = null;
        }
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        String s = getEntityNameToSpawn();

        if (!StringUtils.isNullOrEmpty(s))
        {
            nbt.setString("EntityId", s);
            nbt.setShort("Delay", (short) spawnDelay);
            nbt.setShort("MinSpawnDelay", (short) minSpawnDelay);
            nbt.setShort("MaxSpawnDelay", (short) maxSpawnDelay);
            nbt.setShort("SpawnCount", (short) spawnCount);
            nbt.setShort("MaxNearbyEntities", (short) maxNearbyEntities);
            nbt.setShort("RequiredPlayerRange", (short) activatingRangeFromPlayer);
            nbt.setShort("SpawnRange", (short) spawnRange);

            if (getRandomEntity() != null)
            {
                nbt.setTag("SpawnData", getRandomEntity().nbtData.copy());
            }

            if (getRandomEntity() != null || minecartToSpawn.size() > 0)
            {
                NBTTagList nbttaglist = new NBTTagList();

                if (minecartToSpawn.size() > 0)
                {
                    for (MobSpawnerBaseLogic.WeightedRandomMinecart mobspawnerbaselogic$weightedrandomminecart : minecartToSpawn)
                    {
                        nbttaglist.appendTag(mobspawnerbaselogic$weightedrandomminecart.toNBT());
                    }
                }
                else
                {
                    nbttaglist.appendTag(getRandomEntity().toNBT());
                }

                nbt.setTag("SpawnPotentials", nbttaglist);
            }
        }
    }

    public Entity func_180612_a(World worldIn)
    {
        if (cachedEntity == null)
        {
            Entity entity = EntityList.createEntityByName(getEntityNameToSpawn(), worldIn);

            if (entity != null)
            {
                entity = spawnNewEntity(entity, false);
                cachedEntity = entity;
            }
        }

        return cachedEntity;
    }

    /**
     * Sets the delay to minDelay if parameter given is 1, else return false.
     */
    public boolean setDelayToMin(int delay)
    {
        if (delay == 1 && getSpawnerWorld().isRemote)
        {
            spawnDelay = minSpawnDelay;
            return true;
        }
        else
        {
            return false;
        }
    }

    private MobSpawnerBaseLogic.WeightedRandomMinecart getRandomEntity()
    {
        return randomEntity;
    }

    public void setRandomEntity(MobSpawnerBaseLogic.WeightedRandomMinecart p_98277_1_)
    {
        randomEntity = p_98277_1_;
    }

    public abstract void func_98267_a(int id);

    public abstract World getSpawnerWorld();

    public abstract BlockPos getSpawnerPosition();

    public double getMobRotation()
    {
        return mobRotation;
    }

    public double getPrevMobRotation()
    {
        return prevMobRotation;
    }

    public class WeightedRandomMinecart extends WeightedRandom.Item
    {
        private final NBTTagCompound nbtData;
        private final String entityType;

        public WeightedRandomMinecart(NBTTagCompound tagCompound)
        {
            this(tagCompound.getCompoundTag("Properties"), tagCompound.getString("Type"), tagCompound.getInteger("Weight"));
        }

        public WeightedRandomMinecart(NBTTagCompound tagCompound, String type)
        {
            this(tagCompound, type, 1);
        }

        private WeightedRandomMinecart(NBTTagCompound tagCompound, String type, int weight)
        {
            super(weight);

            if (type.equals("Minecart"))
            {
                if (tagCompound != null)
                {
                    type = EntityMinecart.EnumMinecartType.byNetworkID(tagCompound.getInteger("Type")).getName();
                }
                else
                {
                    type = "MinecartRideable";
                }
            }

            nbtData = tagCompound;
            entityType = type;
        }

        public NBTTagCompound toNBT()
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setTag("Properties", nbtData);
            nbttagcompound.setString("Type", entityType);
            nbttagcompound.setInteger("Weight", itemWeight);
            return nbttagcompound;
        }
    }
}
