package net.minecraft.world.biome;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeEndDecorator extends BiomeDecorator
{
    protected WorldGenerator spikeGen = new WorldGenSpikes(Blocks.end_stone);

    protected void genDecorations(BiomeGenBase biomeGenBaseIn)
    {
        generateOres();

        if (randomGenerator.nextInt(5) == 0)
        {
            int i = randomGenerator.nextInt(16) + 8;
            int j = randomGenerator.nextInt(16) + 8;
            spikeGen.generate(currentWorld, randomGenerator, currentWorld.getTopSolidOrLiquidBlock(field_180294_c.add(i, 0, j)));
        }

        if (field_180294_c.getX() == 0 && field_180294_c.getZ() == 0)
        {
            EntityDragon entitydragon = new EntityDragon(currentWorld);
            entitydragon.setLocationAndAngles(0.0D, 128.0D, 0.0D, randomGenerator.nextFloat() * 360.0F, 0.0F);
            currentWorld.spawnEntityInWorld(entitydragon);
        }
    }
}
