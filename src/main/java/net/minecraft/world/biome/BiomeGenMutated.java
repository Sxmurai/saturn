package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import java.util.Random;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeGenMutated extends BiomeGenBase
{
    protected BiomeGenBase baseBiome;

    public BiomeGenMutated(int id, BiomeGenBase biome)
    {
        super(id);
        baseBiome = biome;
        func_150557_a(biome.color, true);
        biomeName = biome.biomeName + " M";
        topBlock = biome.topBlock;
        fillerBlock = biome.fillerBlock;
        fillerBlockMetadata = biome.fillerBlockMetadata;
        minHeight = biome.minHeight;
        maxHeight = biome.maxHeight;
        temperature = biome.temperature;
        rainfall = biome.rainfall;
        waterColorMultiplier = biome.waterColorMultiplier;
        enableSnow = biome.enableSnow;
        enableRain = biome.enableRain;
        spawnableCreatureList = Lists.newArrayList(biome.spawnableCreatureList);
        spawnableMonsterList = Lists.newArrayList(biome.spawnableMonsterList);
        spawnableCaveCreatureList = Lists.newArrayList(biome.spawnableCaveCreatureList);
        spawnableWaterCreatureList = Lists.newArrayList(biome.spawnableWaterCreatureList);
        temperature = biome.temperature;
        rainfall = biome.rainfall;
        minHeight = biome.minHeight + 0.1F;
        maxHeight = biome.maxHeight + 0.2F;
    }

    public void decorate(World worldIn, Random rand, BlockPos pos)
    {
        baseBiome.theBiomeDecorator.decorate(worldIn, rand, this, pos);
    }

    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int p_180622_4_, int p_180622_5_, double p_180622_6_)
    {
        baseBiome.genTerrainBlocks(worldIn, rand, chunkPrimerIn, p_180622_4_, p_180622_5_, p_180622_6_);
    }

    /**
     * returns the chance a creature has to spawn.
     */
    public float getSpawningChance()
    {
        return baseBiome.getSpawningChance();
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand)
    {
        return baseBiome.genBigTreeChance(rand);
    }

    public int getFoliageColorAtPos(BlockPos pos)
    {
        return baseBiome.getFoliageColorAtPos(pos);
    }

    public int getGrassColorAtPos(BlockPos pos)
    {
        return baseBiome.getGrassColorAtPos(pos);
    }

    public Class <? extends BiomeGenBase > getBiomeClass()
    {
        return baseBiome.getBiomeClass();
    }

    /**
     * returns true if the biome specified is equal to this biome
     */
    public boolean isEqualTo(BiomeGenBase biome)
    {
        return baseBiome.isEqualTo(biome);
    }

    public BiomeGenBase.TempCategory getTempCategory()
    {
        return baseBiome.getTempCategory();
    }
}
