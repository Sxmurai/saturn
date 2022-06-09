package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class StructureOceanMonument extends MapGenStructure
{
    private int field_175800_f;
    private int field_175801_g;
    public static final List<BiomeGenBase> field_175802_d = Arrays.asList(BiomeGenBase.ocean, BiomeGenBase.deepOcean, BiomeGenBase.river, BiomeGenBase.frozenOcean, BiomeGenBase.frozenRiver);
    private static final List<BiomeGenBase.SpawnListEntry> field_175803_h = Lists.newArrayList();

    public StructureOceanMonument()
    {
        field_175800_f = 32;
        field_175801_g = 5;
    }

    public StructureOceanMonument(Map<String, String> p_i45608_1_)
    {
        this();

        for (Map.Entry<String, String> entry : p_i45608_1_.entrySet())
        {
            if (entry.getKey().equals("spacing"))
            {
                field_175800_f = MathHelper.parseIntWithDefaultAndMax(entry.getValue(), field_175800_f, 1);
            }
            else if (entry.getKey().equals("separation"))
            {
                field_175801_g = MathHelper.parseIntWithDefaultAndMax(entry.getValue(), field_175801_g, 1);
            }
        }
    }

    public String getStructureName()
    {
        return "Monument";
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= field_175800_f - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= field_175800_f - 1;
        }

        int k = chunkX / field_175800_f;
        int l = chunkZ / field_175800_f;
        Random random = worldObj.setRandomSeed(k, l, 10387313);
        k = k * field_175800_f;
        l = l * field_175800_f;
        k = k + (random.nextInt(field_175800_f - field_175801_g) + random.nextInt(field_175800_f - field_175801_g)) / 2;
        l = l + (random.nextInt(field_175800_f - field_175801_g) + random.nextInt(field_175800_f - field_175801_g)) / 2;

        if (i == k && j == l)
        {
            if (worldObj.getWorldChunkManager().getBiomeGenerator(new BlockPos(i * 16 + 8, 64, j * 16 + 8), null) != BiomeGenBase.deepOcean)
            {
                return false;
            }

            boolean flag = worldObj.getWorldChunkManager().areBiomesViable(i * 16 + 8, j * 16 + 8, 29, StructureOceanMonument.field_175802_d);

            return flag;
        }

        return false;
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new StructureOceanMonument.StartMonument(worldObj, rand, chunkX, chunkZ);
    }

    public List<BiomeGenBase.SpawnListEntry> func_175799_b()
    {
        return StructureOceanMonument.field_175803_h;
    }

    static
    {
        StructureOceanMonument.field_175803_h.add(new BiomeGenBase.SpawnListEntry(EntityGuardian.class, 1, 2, 4));
    }

    public static class StartMonument extends StructureStart
    {
        private final Set<ChunkCoordIntPair> field_175791_c = Sets.newHashSet();
        private boolean field_175790_d;

        public StartMonument()
        {
        }

        public StartMonument(World worldIn, Random p_i45607_2_, int p_i45607_3_, int p_i45607_4_)
        {
            super(p_i45607_3_, p_i45607_4_);
            func_175789_b(worldIn, p_i45607_2_, p_i45607_3_, p_i45607_4_);
        }

        private void func_175789_b(World worldIn, Random p_175789_2_, int p_175789_3_, int p_175789_4_)
        {
            p_175789_2_.setSeed(worldIn.getSeed());
            long i = p_175789_2_.nextLong();
            long j = p_175789_2_.nextLong();
            long k = (long)p_175789_3_ * i;
            long l = (long)p_175789_4_ * j;
            p_175789_2_.setSeed(k ^ l ^ worldIn.getSeed());
            int i1 = p_175789_3_ * 16 + 8 - 29;
            int j1 = p_175789_4_ * 16 + 8 - 29;
            EnumFacing enumfacing = EnumFacing.Plane.HORIZONTAL.random(p_175789_2_);
            components.add(new StructureOceanMonumentPieces.MonumentBuilding(p_175789_2_, i1, j1, enumfacing));
            updateBoundingBox();
            field_175790_d = true;
        }

        public void generateStructure(World worldIn, Random rand, StructureBoundingBox structurebb)
        {
            if (!field_175790_d)
            {
                components.clear();
                func_175789_b(worldIn, rand, getChunkPosX(), getChunkPosZ());
            }

            super.generateStructure(worldIn, rand, structurebb);
        }

        public boolean func_175788_a(ChunkCoordIntPair pair)
        {
            return !this.field_175791_c.contains(pair) && super.func_175788_a(pair);
        }

        public void func_175787_b(ChunkCoordIntPair pair)
        {
            super.func_175787_b(pair);
            field_175791_c.add(pair);
        }

        public void writeToNBT(NBTTagCompound tagCompound)
        {
            super.writeToNBT(tagCompound);
            NBTTagList nbttaglist = new NBTTagList();

            for (ChunkCoordIntPair chunkcoordintpair : field_175791_c)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setInteger("X", chunkcoordintpair.chunkXPos);
                nbttagcompound.setInteger("Z", chunkcoordintpair.chunkZPos);
                nbttaglist.appendTag(nbttagcompound);
            }

            tagCompound.setTag("Processed", nbttaglist);
        }

        public void readFromNBT(NBTTagCompound tagCompound)
        {
            super.readFromNBT(tagCompound);

            if (tagCompound.hasKey("Processed", 9))
            {
                NBTTagList nbttaglist = tagCompound.getTagList("Processed", 10);

                for (int i = 0; i < nbttaglist.tagCount(); ++i)
                {
                    NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                    field_175791_c.add(new ChunkCoordIntPair(nbttagcompound.getInteger("X"), nbttagcompound.getInteger("Z")));
                }
            }
        }
    }
}
