package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class MapGenScatteredFeature extends MapGenStructure
{
    private static final List<BiomeGenBase> biomelist = Arrays.asList(BiomeGenBase.desert, BiomeGenBase.desertHills, BiomeGenBase.jungle, BiomeGenBase.jungleHills, BiomeGenBase.swampland);
    private final List<BiomeGenBase.SpawnListEntry> scatteredFeatureSpawnList;

    /** the maximum distance between scattered features */
    private int maxDistanceBetweenScatteredFeatures;

    /** the minimum distance between scattered features */
    private final int minDistanceBetweenScatteredFeatures;

    public MapGenScatteredFeature()
    {
        scatteredFeatureSpawnList = Lists.newArrayList();
        maxDistanceBetweenScatteredFeatures = 32;
        minDistanceBetweenScatteredFeatures = 8;
        scatteredFeatureSpawnList.add(new BiomeGenBase.SpawnListEntry(EntityWitch.class, 1, 1, 1));
    }

    public MapGenScatteredFeature(Map<String, String> p_i2061_1_)
    {
        this();

        for (Map.Entry<String, String> entry : p_i2061_1_.entrySet())
        {
            if (entry.getKey().equals("distance"))
            {
                maxDistanceBetweenScatteredFeatures = MathHelper.parseIntWithDefaultAndMax(entry.getValue(), maxDistanceBetweenScatteredFeatures, minDistanceBetweenScatteredFeatures + 1);
            }
        }
    }

    public String getStructureName()
    {
        return "Temple";
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= maxDistanceBetweenScatteredFeatures - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= maxDistanceBetweenScatteredFeatures - 1;
        }

        int k = chunkX / maxDistanceBetweenScatteredFeatures;
        int l = chunkZ / maxDistanceBetweenScatteredFeatures;
        Random random = worldObj.setRandomSeed(k, l, 14357617);
        k = k * maxDistanceBetweenScatteredFeatures;
        l = l * maxDistanceBetweenScatteredFeatures;
        k = k + random.nextInt(maxDistanceBetweenScatteredFeatures - minDistanceBetweenScatteredFeatures);
        l = l + random.nextInt(maxDistanceBetweenScatteredFeatures - minDistanceBetweenScatteredFeatures);

        if (i == k && j == l)
        {
            BiomeGenBase biomegenbase = worldObj.getWorldChunkManager().getBiomeGenerator(new BlockPos(i * 16 + 8, 0, j * 16 + 8));

            if (biomegenbase == null)
            {
                return false;
            }

            for (BiomeGenBase biomegenbase1 : MapGenScatteredFeature.biomelist)
            {
                if (biomegenbase == biomegenbase1)
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new MapGenScatteredFeature.Start(worldObj, rand, chunkX, chunkZ);
    }

    public boolean func_175798_a(BlockPos p_175798_1_)
    {
        StructureStart structurestart = func_175797_c(p_175798_1_);

        if (structurestart != null && structurestart instanceof MapGenScatteredFeature.Start && !structurestart.components.isEmpty())
        {
            StructureComponent structurecomponent = structurestart.components.getFirst();
            return structurecomponent instanceof ComponentScatteredFeaturePieces.SwampHut;
        }
        else
        {
            return false;
        }
    }

    public List<BiomeGenBase.SpawnListEntry> getScatteredFeatureSpawnList()
    {
        return scatteredFeatureSpawnList;
    }

    public static class Start extends StructureStart
    {
        public Start()
        {
        }

        public Start(World worldIn, Random p_i2060_2_, int p_i2060_3_, int p_i2060_4_)
        {
            super(p_i2060_3_, p_i2060_4_);
            BiomeGenBase biomegenbase = worldIn.getBiomeGenForCoords(new BlockPos(p_i2060_3_ * 16 + 8, 0, p_i2060_4_ * 16 + 8));

            if (biomegenbase != BiomeGenBase.jungle && biomegenbase != BiomeGenBase.jungleHills)
            {
                if (biomegenbase == BiomeGenBase.swampland)
                {
                    ComponentScatteredFeaturePieces.SwampHut componentscatteredfeaturepieces$swamphut = new ComponentScatteredFeaturePieces.SwampHut(p_i2060_2_, p_i2060_3_ * 16, p_i2060_4_ * 16);
                    components.add(componentscatteredfeaturepieces$swamphut);
                }
                else if (biomegenbase == BiomeGenBase.desert || biomegenbase == BiomeGenBase.desertHills)
                {
                    ComponentScatteredFeaturePieces.DesertPyramid componentscatteredfeaturepieces$desertpyramid = new ComponentScatteredFeaturePieces.DesertPyramid(p_i2060_2_, p_i2060_3_ * 16, p_i2060_4_ * 16);
                    components.add(componentscatteredfeaturepieces$desertpyramid);
                }
            }
            else
            {
                ComponentScatteredFeaturePieces.JunglePyramid componentscatteredfeaturepieces$junglepyramid = new ComponentScatteredFeaturePieces.JunglePyramid(p_i2060_2_, p_i2060_3_ * 16, p_i2060_4_ * 16);
                components.add(componentscatteredfeaturepieces$junglepyramid);
            }

            updateBoundingBox();
        }
    }
}
