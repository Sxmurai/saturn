package net.minecraft.village;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Village
{
    private World worldObj;
    private final List<VillageDoorInfo> villageDoorInfoList = Lists.newArrayList();

    /**
     * This is the sum of all door coordinates and used to calculate the actual village center by dividing by the number
     * of doors.
     */
    private BlockPos centerHelper = BlockPos.ORIGIN;

    /** This is the actual village center. */
    private BlockPos center = BlockPos.ORIGIN;
    private int villageRadius;
    private int lastAddDoorTimestamp;
    private int tickCounter;
    private int numVillagers;

    /** Timestamp of tick count when villager last bred */
    private int noBreedTicks;
    private final TreeMap<String, Integer> playerReputation = new TreeMap();
    private final List<Village.VillageAggressor> villageAgressors = Lists.newArrayList();
    private int numIronGolems;

    public Village()
    {
    }

    public Village(World worldIn)
    {
        worldObj = worldIn;
    }

    public void setWorld(World worldIn)
    {
        worldObj = worldIn;
    }

    /**
     * Called periodically by VillageCollection
     */
    public void tick(int p_75560_1_)
    {
        tickCounter = p_75560_1_;
        removeDeadAndOutOfRangeDoors();
        removeDeadAndOldAgressors();

        if (p_75560_1_ % 20 == 0)
        {
            updateNumVillagers();
        }

        if (p_75560_1_ % 30 == 0)
        {
            updateNumIronGolems();
        }

        int i = numVillagers / 10;

        if (numIronGolems < i && villageDoorInfoList.size() > 20 && worldObj.rand.nextInt(7000) == 0)
        {
            Vec3 vec3 = func_179862_a(center, 2, 4, 2);

            if (vec3 != null)
            {
                EntityIronGolem entityirongolem = new EntityIronGolem(worldObj);
                entityirongolem.setPosition(vec3.xCoord, vec3.yCoord, vec3.zCoord);
                worldObj.spawnEntityInWorld(entityirongolem);
                ++numIronGolems;
            }
        }
    }

    private Vec3 func_179862_a(BlockPos p_179862_1_, int p_179862_2_, int p_179862_3_, int p_179862_4_)
    {
        for (int i = 0; i < 10; ++i)
        {
            BlockPos blockpos = p_179862_1_.add(worldObj.rand.nextInt(16) - 8, worldObj.rand.nextInt(6) - 3, worldObj.rand.nextInt(16) - 8);

            if (func_179866_a(blockpos) && func_179861_a(new BlockPos(p_179862_2_, p_179862_3_, p_179862_4_), blockpos))
            {
                return new Vec3(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            }
        }

        return null;
    }

    private boolean func_179861_a(BlockPos p_179861_1_, BlockPos p_179861_2_)
    {
        if (!World.doesBlockHaveSolidTopSurface(worldObj, p_179861_2_.down()))
        {
            return false;
        }
        else
        {
            int i = p_179861_2_.getX() - p_179861_1_.getX() / 2;
            int j = p_179861_2_.getZ() - p_179861_1_.getZ() / 2;

            for (int k = i; k < i + p_179861_1_.getX(); ++k)
            {
                for (int l = p_179861_2_.getY(); l < p_179861_2_.getY() + p_179861_1_.getY(); ++l)
                {
                    for (int i1 = j; i1 < j + p_179861_1_.getZ(); ++i1)
                    {
                        if (worldObj.getBlockState(new BlockPos(k, l, i1)).getBlock().isNormalCube())
                        {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    private void updateNumIronGolems()
    {
        List<EntityIronGolem> list = worldObj.getEntitiesWithinAABB(EntityIronGolem.class, new AxisAlignedBB(center.getX() - villageRadius, center.getY() - 4, center.getZ() - villageRadius, center.getX() + villageRadius, center.getY() + 4, center.getZ() + villageRadius));
        numIronGolems = list.size();
    }

    private void updateNumVillagers()
    {
        List<EntityVillager> list = worldObj.getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(center.getX() - villageRadius, center.getY() - 4, center.getZ() - villageRadius, center.getX() + villageRadius, center.getY() + 4, center.getZ() + villageRadius));
        numVillagers = list.size();

        if (numVillagers == 0)
        {
            playerReputation.clear();
        }
    }

    public BlockPos getCenter()
    {
        return center;
    }

    public int getVillageRadius()
    {
        return villageRadius;
    }

    /**
     * Actually get num village door info entries, but that boils down to number of doors. Called by
     * EntityAIVillagerMate and VillageSiege
     */
    public int getNumVillageDoors()
    {
        return villageDoorInfoList.size();
    }

    public int getTicksSinceLastDoorAdding()
    {
        return tickCounter - lastAddDoorTimestamp;
    }

    public int getNumVillagers()
    {
        return numVillagers;
    }

    public boolean func_179866_a(BlockPos pos)
    {
        return center.distanceSq(pos) < (double)(villageRadius * villageRadius);
    }

    public List<VillageDoorInfo> getVillageDoorInfoList()
    {
        return villageDoorInfoList;
    }

    public VillageDoorInfo getNearestDoor(BlockPos pos)
    {
        VillageDoorInfo villagedoorinfo = null;
        int i = Integer.MAX_VALUE;

        for (VillageDoorInfo villagedoorinfo1 : villageDoorInfoList)
        {
            int j = villagedoorinfo1.getDistanceToDoorBlockSq(pos);

            if (j < i)
            {
                villagedoorinfo = villagedoorinfo1;
                i = j;
            }
        }

        return villagedoorinfo;
    }

    /**
     * Returns {@link net.minecraft.village.VillageDoorInfo VillageDoorInfo} from given block position
     */
    public VillageDoorInfo getDoorInfo(BlockPos pos)
    {
        VillageDoorInfo villagedoorinfo = null;
        int i = Integer.MAX_VALUE;

        for (VillageDoorInfo villagedoorinfo1 : villageDoorInfoList)
        {
            int j = villagedoorinfo1.getDistanceToDoorBlockSq(pos);

            if (j > 256)
            {
                j = j * 1000;
            }
            else
            {
                j = villagedoorinfo1.getDoorOpeningRestrictionCounter();
            }

            if (j < i)
            {
                villagedoorinfo = villagedoorinfo1;
                i = j;
            }
        }

        return villagedoorinfo;
    }

    /**
     * if door not existed in this village, null will be returned
     */
    public VillageDoorInfo getExistedDoor(BlockPos doorBlock)
    {
        if (center.distanceSq(doorBlock) > (double)(villageRadius * villageRadius))
        {
            return null;
        }
        else
        {
            for (VillageDoorInfo villagedoorinfo : villageDoorInfoList)
            {
                if (villagedoorinfo.getDoorBlockPos().getX() == doorBlock.getX() && villagedoorinfo.getDoorBlockPos().getZ() == doorBlock.getZ() && Math.abs(villagedoorinfo.getDoorBlockPos().getY() - doorBlock.getY()) <= 1)
                {
                    return villagedoorinfo;
                }
            }

            return null;
        }
    }

    public void addVillageDoorInfo(VillageDoorInfo doorInfo)
    {
        villageDoorInfoList.add(doorInfo);
        centerHelper = centerHelper.add(doorInfo.getDoorBlockPos());
        updateVillageRadiusAndCenter();
        lastAddDoorTimestamp = doorInfo.getInsidePosY();
    }

    /**
     * Returns true, if there is not a single village door left. Called by VillageCollection
     */
    public boolean isAnnihilated()
    {
        return villageDoorInfoList.isEmpty();
    }

    public void addOrRenewAgressor(EntityLivingBase entitylivingbaseIn)
    {
        for (Village.VillageAggressor village$villageaggressor : villageAgressors)
        {
            if (village$villageaggressor.agressor == entitylivingbaseIn)
            {
                village$villageaggressor.agressionTime = tickCounter;
                return;
            }
        }

        villageAgressors.add(new Village.VillageAggressor(entitylivingbaseIn, tickCounter));
    }

    public EntityLivingBase findNearestVillageAggressor(EntityLivingBase entitylivingbaseIn)
    {
        double d0 = Double.MAX_VALUE;
        Village.VillageAggressor village$villageaggressor = null;

        for (int i = 0; i < villageAgressors.size(); ++i)
        {
            Village.VillageAggressor village$villageaggressor1 = villageAgressors.get(i);
            double d1 = village$villageaggressor1.agressor.getDistanceSqToEntity(entitylivingbaseIn);

            if (d1 <= d0)
            {
                village$villageaggressor = village$villageaggressor1;
                d0 = d1;
            }
        }

        return village$villageaggressor != null ? village$villageaggressor.agressor : null;
    }

    public EntityPlayer getNearestTargetPlayer(EntityLivingBase villageDefender)
    {
        double d0 = Double.MAX_VALUE;
        EntityPlayer entityplayer = null;

        for (String s : playerReputation.keySet())
        {
            if (isPlayerReputationTooLow(s))
            {
                EntityPlayer entityplayer1 = worldObj.getPlayerEntityByName(s);

                if (entityplayer1 != null)
                {
                    double d1 = entityplayer1.getDistanceSqToEntity(villageDefender);

                    if (d1 <= d0)
                    {
                        entityplayer = entityplayer1;
                        d0 = d1;
                    }
                }
            }
        }

        return entityplayer;
    }

    private void removeDeadAndOldAgressors()
    {
        Iterator<Village.VillageAggressor> iterator = villageAgressors.iterator();

        while (iterator.hasNext())
        {
            Village.VillageAggressor village$villageaggressor = iterator.next();

            if (!village$villageaggressor.agressor.isEntityAlive() || Math.abs(tickCounter - village$villageaggressor.agressionTime) > 300)
            {
                iterator.remove();
            }
        }
    }

    private void removeDeadAndOutOfRangeDoors()
    {
        boolean flag = false;
        boolean flag1 = worldObj.rand.nextInt(50) == 0;
        Iterator<VillageDoorInfo> iterator = villageDoorInfoList.iterator();

        while (iterator.hasNext())
        {
            VillageDoorInfo villagedoorinfo = iterator.next();

            if (flag1)
            {
                villagedoorinfo.resetDoorOpeningRestrictionCounter();
            }

            if (!isWoodDoor(villagedoorinfo.getDoorBlockPos()) || Math.abs(tickCounter - villagedoorinfo.getInsidePosY()) > 1200)
            {
                centerHelper = centerHelper.subtract(villagedoorinfo.getDoorBlockPos());
                flag = true;
                villagedoorinfo.setIsDetachedFromVillageFlag(true);
                iterator.remove();
            }
        }

        if (flag)
        {
            updateVillageRadiusAndCenter();
        }
    }

    private boolean isWoodDoor(BlockPos pos)
    {
        Block block = worldObj.getBlockState(pos).getBlock();
        return block instanceof BlockDoor && block.getMaterial() == Material.wood;
    }

    private void updateVillageRadiusAndCenter()
    {
        int i = villageDoorInfoList.size();

        if (i == 0)
        {
            center = new BlockPos(0, 0, 0);
            villageRadius = 0;
        }
        else
        {
            center = new BlockPos(centerHelper.getX() / i, centerHelper.getY() / i, centerHelper.getZ() / i);
            int j = 0;

            for (VillageDoorInfo villagedoorinfo : villageDoorInfoList)
            {
                j = Math.max(villagedoorinfo.getDistanceToDoorBlockSq(center), j);
            }

            villageRadius = Math.max(32, (int)Math.sqrt(j) + 1);
        }
    }

    /**
     * Return the village reputation for a player
     */
    public int getReputationForPlayer(String p_82684_1_)
    {
        Integer integer = playerReputation.get(p_82684_1_);
        return integer != null ? integer.intValue() : 0;
    }

    /**
     * Set the village reputation for a player.
     */
    public int setReputationForPlayer(String p_82688_1_, int p_82688_2_)
    {
        int i = getReputationForPlayer(p_82688_1_);
        int j = MathHelper.clamp_int(i + p_82688_2_, -30, 10);
        playerReputation.put(p_82688_1_, Integer.valueOf(j));
        return j;
    }

    /**
     * Return whether this player has a too low reputation with this village.
     */
    public boolean isPlayerReputationTooLow(String p_82687_1_)
    {
        return getReputationForPlayer(p_82687_1_) <= -15;
    }

    /**
     * Read this village's data from NBT.
     */
    public void readVillageDataFromNBT(NBTTagCompound p_82690_1_)
    {
        numVillagers = p_82690_1_.getInteger("PopSize");
        villageRadius = p_82690_1_.getInteger("Radius");
        numIronGolems = p_82690_1_.getInteger("Golems");
        lastAddDoorTimestamp = p_82690_1_.getInteger("Stable");
        tickCounter = p_82690_1_.getInteger("Tick");
        noBreedTicks = p_82690_1_.getInteger("MTick");
        center = new BlockPos(p_82690_1_.getInteger("CX"), p_82690_1_.getInteger("CY"), p_82690_1_.getInteger("CZ"));
        centerHelper = new BlockPos(p_82690_1_.getInteger("ACX"), p_82690_1_.getInteger("ACY"), p_82690_1_.getInteger("ACZ"));
        NBTTagList nbttaglist = p_82690_1_.getTagList("Doors", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            VillageDoorInfo villagedoorinfo = new VillageDoorInfo(new BlockPos(nbttagcompound.getInteger("X"), nbttagcompound.getInteger("Y"), nbttagcompound.getInteger("Z")), nbttagcompound.getInteger("IDX"), nbttagcompound.getInteger("IDZ"), nbttagcompound.getInteger("TS"));
            villageDoorInfoList.add(villagedoorinfo);
        }

        NBTTagList nbttaglist1 = p_82690_1_.getTagList("Players", 10);

        for (int j = 0; j < nbttaglist1.tagCount(); ++j)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist1.getCompoundTagAt(j);

            if (nbttagcompound1.hasKey("UUID"))
            {
                PlayerProfileCache playerprofilecache = MinecraftServer.getServer().getPlayerProfileCache();
                GameProfile gameprofile = playerprofilecache.getProfileByUUID(UUID.fromString(nbttagcompound1.getString("UUID")));

                if (gameprofile != null)
                {
                    playerReputation.put(gameprofile.getName(), Integer.valueOf(nbttagcompound1.getInteger("S")));
                }
            }
            else
            {
                playerReputation.put(nbttagcompound1.getString("Name"), Integer.valueOf(nbttagcompound1.getInteger("S")));
            }
        }
    }

    /**
     * Write this village's data to NBT.
     */
    public void writeVillageDataToNBT(NBTTagCompound p_82689_1_)
    {
        p_82689_1_.setInteger("PopSize", numVillagers);
        p_82689_1_.setInteger("Radius", villageRadius);
        p_82689_1_.setInteger("Golems", numIronGolems);
        p_82689_1_.setInteger("Stable", lastAddDoorTimestamp);
        p_82689_1_.setInteger("Tick", tickCounter);
        p_82689_1_.setInteger("MTick", noBreedTicks);
        p_82689_1_.setInteger("CX", center.getX());
        p_82689_1_.setInteger("CY", center.getY());
        p_82689_1_.setInteger("CZ", center.getZ());
        p_82689_1_.setInteger("ACX", centerHelper.getX());
        p_82689_1_.setInteger("ACY", centerHelper.getY());
        p_82689_1_.setInteger("ACZ", centerHelper.getZ());
        NBTTagList nbttaglist = new NBTTagList();

        for (VillageDoorInfo villagedoorinfo : villageDoorInfoList)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("X", villagedoorinfo.getDoorBlockPos().getX());
            nbttagcompound.setInteger("Y", villagedoorinfo.getDoorBlockPos().getY());
            nbttagcompound.setInteger("Z", villagedoorinfo.getDoorBlockPos().getZ());
            nbttagcompound.setInteger("IDX", villagedoorinfo.getInsideOffsetX());
            nbttagcompound.setInteger("IDZ", villagedoorinfo.getInsideOffsetZ());
            nbttagcompound.setInteger("TS", villagedoorinfo.getInsidePosY());
            nbttaglist.appendTag(nbttagcompound);
        }

        p_82689_1_.setTag("Doors", nbttaglist);
        NBTTagList nbttaglist1 = new NBTTagList();

        for (String s : playerReputation.keySet())
        {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            PlayerProfileCache playerprofilecache = MinecraftServer.getServer().getPlayerProfileCache();
            GameProfile gameprofile = playerprofilecache.getGameProfileForUsername(s);

            if (gameprofile != null)
            {
                nbttagcompound1.setString("UUID", gameprofile.getId().toString());
                nbttagcompound1.setInteger("S", playerReputation.get(s).intValue());
                nbttaglist1.appendTag(nbttagcompound1);
            }
        }

        p_82689_1_.setTag("Players", nbttaglist1);
    }

    /**
     * Prevent villager breeding for a fixed interval of time
     */
    public void endMatingSeason()
    {
        noBreedTicks = tickCounter;
    }

    /**
     * Return whether villagers mating refractory period has passed
     */
    public boolean isMatingSeason()
    {
        return noBreedTicks == 0 || tickCounter - noBreedTicks >= 3600;
    }

    public void setDefaultPlayerReputation(int p_82683_1_)
    {
        for (String s : playerReputation.keySet())
        {
            setReputationForPlayer(s, p_82683_1_);
        }
    }

    class VillageAggressor
    {
        public EntityLivingBase agressor;
        public int agressionTime;

        VillageAggressor(EntityLivingBase p_i1674_2_, int p_i1674_3_)
        {
            agressor = p_i1674_2_;
            agressionTime = p_i1674_3_;
        }
    }
}
