package net.minecraft.world.biome;

import net.minecraft.init.Blocks;

public class BiomeGenBeach extends BiomeGenBase
{
    public BiomeGenBeach(int p_i1969_1_)
    {
        super(p_i1969_1_);
        spawnableCreatureList.clear();
        topBlock = Blocks.sand.getDefaultState();
        fillerBlock = Blocks.sand.getDefaultState();
        theBiomeDecorator.treesPerChunk = -999;
        theBiomeDecorator.deadBushPerChunk = 0;
        theBiomeDecorator.reedsPerChunk = 0;
        theBiomeDecorator.cactiPerChunk = 0;
    }
}
