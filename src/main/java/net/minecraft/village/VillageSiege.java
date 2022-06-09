package net.minecraft.village;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;

public class VillageSiege
{
    private final World worldObj;
    private boolean field_75535_b;
    private int field_75536_c = -1;
    private int field_75533_d;
    private int field_75534_e;

    /** Instance of Village. */
    private Village theVillage;
    private int field_75532_g;
    private int field_75538_h;
    private int field_75539_i;

    public VillageSiege(World worldIn)
    {
        worldObj = worldIn;
    }

    /**
     * Runs a single tick for the village siege
     */
    public void tick()
    {
        if (worldObj.isDaytime())
        {
            field_75536_c = 0;
        }
        else if (field_75536_c != 2)
        {
            if (field_75536_c == 0)
            {
                float f = worldObj.getCelestialAngle(0.0F);

                if ((double)f < 0.5D || (double)f > 0.501D)
                {
                    return;
                }

                field_75536_c = worldObj.rand.nextInt(10) == 0 ? 1 : 2;
                field_75535_b = false;

                if (field_75536_c == 2)
                {
                    return;
                }
            }

            if (field_75536_c != -1)
            {
                if (!field_75535_b)
                {
                    if (!func_75529_b())
                    {
                        return;
                    }

                    field_75535_b = true;
                }

                if (field_75534_e > 0)
                {
                    --field_75534_e;
                }
                else
                {
                    field_75534_e = 2;

                    if (field_75533_d > 0)
                    {
                        spawnZombie();
                        --field_75533_d;
                    }
                    else
                    {
                        field_75536_c = 2;
                    }
                }
            }
        }
    }

    private boolean func_75529_b()
    {
        List<EntityPlayer> list = worldObj.playerEntities;
        Iterator iterator = list.iterator();

        while (true)
        {
            if (!iterator.hasNext())
            {
                return false;
            }

            EntityPlayer entityplayer = (EntityPlayer)iterator.next();

            if (!entityplayer.isSpectator())
            {
                theVillage = worldObj.getVillageCollection().getNearestVillage(new BlockPos(entityplayer), 1);

                if (theVillage != null && theVillage.getNumVillageDoors() >= 10 && theVillage.getTicksSinceLastDoorAdding() >= 20 && theVillage.getNumVillagers() >= 20)
                {
                    BlockPos blockpos = theVillage.getCenter();
                    float f = (float) theVillage.getVillageRadius();
                    boolean flag = false;

                    for (int i = 0; i < 10; ++i)
                    {
                        float f1 = worldObj.rand.nextFloat() * (float)Math.PI * 2.0F;
                        field_75532_g = blockpos.getX() + (int)((double)(MathHelper.cos(f1) * f) * 0.9D);
                        field_75538_h = blockpos.getY();
                        field_75539_i = blockpos.getZ() + (int)((double)(MathHelper.sin(f1) * f) * 0.9D);
                        flag = false;

                        for (Village village : worldObj.getVillageCollection().getVillageList())
                        {
                            if (village != theVillage && village.func_179866_a(new BlockPos(field_75532_g, field_75538_h, field_75539_i)))
                            {
                                flag = true;
                                break;
                            }
                        }

                        if (!flag)
                        {
                            break;
                        }
                    }

                    if (flag)
                    {
                        return false;
                    }

                    Vec3 vec3 = func_179867_a(new BlockPos(field_75532_g, field_75538_h, field_75539_i));

                    if (vec3 != null)
                    {
                        break;
                    }
                }
            }
        }

        field_75534_e = 0;
        field_75533_d = 20;
        return true;
    }

    private boolean spawnZombie()
    {
        Vec3 vec3 = func_179867_a(new BlockPos(field_75532_g, field_75538_h, field_75539_i));

        if (vec3 == null)
        {
            return false;
        }
        else
        {
            EntityZombie entityzombie;

            try
            {
                entityzombie = new EntityZombie(worldObj);
                entityzombie.onInitialSpawn(worldObj.getDifficultyForLocation(new BlockPos(entityzombie)), null);
                entityzombie.setVillager(false);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
                return false;
            }

            entityzombie.setLocationAndAngles(vec3.xCoord, vec3.yCoord, vec3.zCoord, worldObj.rand.nextFloat() * 360.0F, 0.0F);
            worldObj.spawnEntityInWorld(entityzombie);
            BlockPos blockpos = theVillage.getCenter();
            entityzombie.setHomePosAndDistance(blockpos, theVillage.getVillageRadius());
            return true;
        }
    }

    private Vec3 func_179867_a(BlockPos p_179867_1_)
    {
        for (int i = 0; i < 10; ++i)
        {
            BlockPos blockpos = p_179867_1_.add(worldObj.rand.nextInt(16) - 8, worldObj.rand.nextInt(6) - 3, worldObj.rand.nextInt(16) - 8);

            if (theVillage.func_179866_a(blockpos) && SpawnerAnimals.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, worldObj, blockpos))
            {
                return new Vec3(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            }
        }

        return null;
    }
}
