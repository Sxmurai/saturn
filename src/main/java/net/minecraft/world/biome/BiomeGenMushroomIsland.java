package net.minecraft.world.biome;

import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.init.Blocks;

public class BiomeGenMushroomIsland extends BiomeGenBase
{
    public BiomeGenMushroomIsland(int p_i1984_1_)
    {
        super(p_i1984_1_);
        theBiomeDecorator.treesPerChunk = -100;
        theBiomeDecorator.flowersPerChunk = -100;
        theBiomeDecorator.grassPerChunk = -100;
        theBiomeDecorator.mushroomsPerChunk = 1;
        theBiomeDecorator.bigMushroomsPerChunk = 1;
        topBlock = Blocks.mycelium.getDefaultState();
        spawnableMonsterList.clear();
        spawnableCreatureList.clear();
        spawnableWaterCreatureList.clear();
        spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityMooshroom.class, 8, 4, 8));
    }
}
