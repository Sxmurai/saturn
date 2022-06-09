package net.minecraft.world.gen;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;

public class ChunkProviderGenerate implements IChunkProvider
{
    /** RNG. */
    private final Random rand;
    private final NoiseGeneratorOctaves field_147431_j;
    private final NoiseGeneratorOctaves field_147432_k;
    private final NoiseGeneratorOctaves field_147429_l;
    private final NoiseGeneratorPerlin field_147430_m;

    /** A NoiseGeneratorOctaves used in generating terrain */
    public NoiseGeneratorOctaves noiseGen5;

    /** A NoiseGeneratorOctaves used in generating terrain */
    public NoiseGeneratorOctaves noiseGen6;
    public NoiseGeneratorOctaves mobSpawnerNoise;

    /** Reference to the World object. */
    private final World worldObj;

    /** are map structures going to be generated (e.g. strongholds) */
    private final boolean mapFeaturesEnabled;
    private final WorldType field_177475_o;
    private final double[] field_147434_q;
    private final float[] parabolicField;
    private ChunkProviderSettings settings;
    private Block field_177476_s = Blocks.water;
    private double[] stoneNoise = new double[256];
    private final MapGenBase caveGenerator = new MapGenCaves();

    /** Holds Stronghold Generator */
    private final MapGenStronghold strongholdGenerator = new MapGenStronghold();

    /** Holds Village Generator */
    private final MapGenVillage villageGenerator = new MapGenVillage();

    /** Holds Mineshaft Generator */
    private final MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    private final MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();

    /** Holds ravine generator */
    private final MapGenBase ravineGenerator = new MapGenRavine();
    private final StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();

    /** The biomes that are used to generate the chunk */
    private BiomeGenBase[] biomesForGeneration;
    double[] field_147427_d;
    double[] field_147428_e;
    double[] field_147425_f;
    double[] field_147426_g;

    public ChunkProviderGenerate(World worldIn, long p_i45636_2_, boolean p_i45636_4_, String p_i45636_5_)
    {
        worldObj = worldIn;
        mapFeaturesEnabled = p_i45636_4_;
        field_177475_o = worldIn.getWorldInfo().getTerrainType();
        rand = new Random(p_i45636_2_);
        field_147431_j = new NoiseGeneratorOctaves(rand, 16);
        field_147432_k = new NoiseGeneratorOctaves(rand, 16);
        field_147429_l = new NoiseGeneratorOctaves(rand, 8);
        field_147430_m = new NoiseGeneratorPerlin(rand, 4);
        noiseGen5 = new NoiseGeneratorOctaves(rand, 10);
        noiseGen6 = new NoiseGeneratorOctaves(rand, 16);
        mobSpawnerNoise = new NoiseGeneratorOctaves(rand, 8);
        field_147434_q = new double[825];
        parabolicField = new float[25];

        for (int i = -2; i <= 2; ++i)
        {
            for (int j = -2; j <= 2; ++j)
            {
                float f = 10.0F / MathHelper.sqrt_float((float)(i * i + j * j) + 0.2F);
                parabolicField[i + 2 + (j + 2) * 5] = f;
            }
        }

        if (p_i45636_5_ != null)
        {
            settings = ChunkProviderSettings.Factory.jsonToFactory(p_i45636_5_).func_177864_b();
            field_177476_s = settings.useLavaOceans ? Blocks.lava : Blocks.water;
            worldIn.func_181544_b(settings.seaLevel);
        }
    }

    public void setBlocksInChunk(int p_180518_1_, int p_180518_2_, ChunkPrimer p_180518_3_)
    {
        biomesForGeneration = worldObj.getWorldChunkManager().getBiomesForGeneration(biomesForGeneration, p_180518_1_ * 4 - 2, p_180518_2_ * 4 - 2, 10, 10);
        func_147423_a(p_180518_1_ * 4, 0, p_180518_2_ * 4);

        for (int i = 0; i < 4; ++i)
        {
            int j = i * 5;
            int k = (i + 1) * 5;

            for (int l = 0; l < 4; ++l)
            {
                int i1 = (j + l) * 33;
                int j1 = (j + l + 1) * 33;
                int k1 = (k + l) * 33;
                int l1 = (k + l + 1) * 33;

                for (int i2 = 0; i2 < 32; ++i2)
                {
                    double d0 = 0.125D;
                    double d1 = field_147434_q[i1 + i2];
                    double d2 = field_147434_q[j1 + i2];
                    double d3 = field_147434_q[k1 + i2];
                    double d4 = field_147434_q[l1 + i2];
                    double d5 = (field_147434_q[i1 + i2 + 1] - d1) * d0;
                    double d6 = (field_147434_q[j1 + i2 + 1] - d2) * d0;
                    double d7 = (field_147434_q[k1 + i2 + 1] - d3) * d0;
                    double d8 = (field_147434_q[l1 + i2 + 1] - d4) * d0;

                    for (int j2 = 0; j2 < 8; ++j2)
                    {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int k2 = 0; k2 < 4; ++k2)
                        {
                            double d14 = 0.25D;
                            double d16 = (d11 - d10) * d14;
                            double lvt_45_1_ = d10 - d16;

                            for (int l2 = 0; l2 < 4; ++l2)
                            {
                                if ((lvt_45_1_ += d16) > 0.0D)
                                {
                                    p_180518_3_.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, Blocks.stone.getDefaultState());
                                }
                                else if (i2 * 8 + j2 < settings.seaLevel)
                                {
                                    p_180518_3_.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, field_177476_s.getDefaultState());
                                }
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    public void replaceBlocksForBiome(int p_180517_1_, int p_180517_2_, ChunkPrimer p_180517_3_, BiomeGenBase[] p_180517_4_)
    {
        double d0 = 0.03125D;
        stoneNoise = field_147430_m.func_151599_a(stoneNoise, p_180517_1_ * 16, p_180517_2_ * 16, 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);

        for (int i = 0; i < 16; ++i)
        {
            for (int j = 0; j < 16; ++j)
            {
                BiomeGenBase biomegenbase = p_180517_4_[j + i * 16];
                biomegenbase.genTerrainBlocks(worldObj, rand, p_180517_3_, p_180517_1_ * 16 + i, p_180517_2_ * 16 + j, stoneNoise[j + i * 16]);
            }
        }
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int x, int z)
    {
        rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        setBlocksInChunk(x, z, chunkprimer);
        biomesForGeneration = worldObj.getWorldChunkManager().loadBlockGeneratorData(biomesForGeneration, x * 16, z * 16, 16, 16);
        replaceBlocksForBiome(x, z, chunkprimer, biomesForGeneration);

        if (settings.useCaves)
        {
            caveGenerator.generate(this, worldObj, x, z, chunkprimer);
        }

        if (settings.useRavines)
        {
            ravineGenerator.generate(this, worldObj, x, z, chunkprimer);
        }

        if (settings.useMineShafts && mapFeaturesEnabled)
        {
            mineshaftGenerator.generate(this, worldObj, x, z, chunkprimer);
        }

        if (settings.useVillages && mapFeaturesEnabled)
        {
            villageGenerator.generate(this, worldObj, x, z, chunkprimer);
        }

        if (settings.useStrongholds && mapFeaturesEnabled)
        {
            strongholdGenerator.generate(this, worldObj, x, z, chunkprimer);
        }

        if (settings.useTemples && mapFeaturesEnabled)
        {
            scatteredFeatureGenerator.generate(this, worldObj, x, z, chunkprimer);
        }

        if (settings.useMonuments && mapFeaturesEnabled)
        {
            oceanMonumentGenerator.generate(this, worldObj, x, z, chunkprimer);
        }

        Chunk chunk = new Chunk(worldObj, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i)
        {
            abyte[i] = (byte) biomesForGeneration[i].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    private void func_147423_a(int p_147423_1_, int p_147423_2_, int p_147423_3_)
    {
        field_147426_g = noiseGen6.generateNoiseOctaves(field_147426_g, p_147423_1_, p_147423_3_, 5, 5, settings.depthNoiseScaleX, settings.depthNoiseScaleZ, settings.depthNoiseScaleExponent);
        float f = settings.coordinateScale;
        float f1 = settings.heightScale;
        field_147427_d = field_147429_l.generateNoiseOctaves(field_147427_d, p_147423_1_, p_147423_2_, p_147423_3_, 5, 33, 5, f / settings.mainNoiseScaleX, f1 / settings.mainNoiseScaleY, f / settings.mainNoiseScaleZ);
        field_147428_e = field_147431_j.generateNoiseOctaves(field_147428_e, p_147423_1_, p_147423_2_, p_147423_3_, 5, 33, 5, f, f1, f);
        field_147425_f = field_147432_k.generateNoiseOctaves(field_147425_f, p_147423_1_, p_147423_2_, p_147423_3_, 5, 33, 5, f, f1, f);
        p_147423_3_ = 0;
        p_147423_1_ = 0;
        int i = 0;
        int j = 0;

        for (int k = 0; k < 5; ++k)
        {
            for (int l = 0; l < 5; ++l)
            {
                float f2 = 0.0F;
                float f3 = 0.0F;
                float f4 = 0.0F;
                int i1 = 2;
                BiomeGenBase biomegenbase = biomesForGeneration[k + 2 + (l + 2) * 10];

                for (int j1 = -i1; j1 <= i1; ++j1)
                {
                    for (int k1 = -i1; k1 <= i1; ++k1)
                    {
                        BiomeGenBase biomegenbase1 = biomesForGeneration[k + j1 + 2 + (l + k1 + 2) * 10];
                        float f5 = settings.biomeDepthOffSet + biomegenbase1.minHeight * settings.biomeDepthWeight;
                        float f6 = settings.biomeScaleOffset + biomegenbase1.maxHeight * settings.biomeScaleWeight;

                        if (field_177475_o == WorldType.AMPLIFIED && f5 > 0.0F)
                        {
                            f5 = 1.0F + f5 * 2.0F;
                            f6 = 1.0F + f6 * 4.0F;
                        }

                        float f7 = parabolicField[j1 + 2 + (k1 + 2) * 5] / (f5 + 2.0F);

                        if (biomegenbase1.minHeight > biomegenbase.minHeight)
                        {
                            f7 /= 2.0F;
                        }

                        f2 += f6 * f7;
                        f3 += f5 * f7;
                        f4 += f7;
                    }
                }

                f2 = f2 / f4;
                f3 = f3 / f4;
                f2 = f2 * 0.9F + 0.1F;
                f3 = (f3 * 4.0F - 1.0F) / 8.0F;
                double d7 = field_147426_g[j] / 8000.0D;

                if (d7 < 0.0D)
                {
                    d7 = -d7 * 0.3D;
                }

                d7 = d7 * 3.0D - 2.0D;

                if (d7 < 0.0D)
                {
                    d7 = d7 / 2.0D;

                    if (d7 < -1.0D)
                    {
                        d7 = -1.0D;
                    }

                    d7 = d7 / 1.4D;
                    d7 = d7 / 2.0D;
                }
                else
                {
                    if (d7 > 1.0D)
                    {
                        d7 = 1.0D;
                    }

                    d7 = d7 / 8.0D;
                }

                ++j;
                double d8 = f3;
                double d9 = f2;
                d8 = d8 + d7 * 0.2D;
                d8 = d8 * (double) settings.baseSize / 8.0D;
                double d0 = (double) settings.baseSize + d8 * 4.0D;

                for (int l1 = 0; l1 < 33; ++l1)
                {
                    double d1 = ((double)l1 - d0) * (double) settings.stretchY * 128.0D / 256.0D / d9;

                    if (d1 < 0.0D)
                    {
                        d1 *= 4.0D;
                    }

                    double d2 = field_147428_e[i] / (double) settings.lowerLimitScale;
                    double d3 = field_147425_f[i] / (double) settings.upperLimitScale;
                    double d4 = (field_147427_d[i] / 10.0D + 1.0D) / 2.0D;
                    double d5 = MathHelper.denormalizeClamp(d2, d3, d4) - d1;

                    if (l1 > 29)
                    {
                        double d6 = (float)(l1 - 29) / 3.0F;
                        d5 = d5 * (1.0D - d6) + -10.0D * d6;
                    }

                    field_147434_q[i] = d5;
                    ++i;
                }
            }
        }
    }

    /**
     * Checks to see if a chunk exists at x, z
     */
    public boolean chunkExists(int x, int z)
    {
        return true;
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_)
    {
        BlockFalling.fallInstantly = true;
        int i = p_73153_2_ * 16;
        int j = p_73153_3_ * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        BiomeGenBase biomegenbase = worldObj.getBiomeGenForCoords(blockpos.add(16, 0, 16));
        rand.setSeed(worldObj.getSeed());
        long k = rand.nextLong() / 2L * 2L + 1L;
        long l = rand.nextLong() / 2L * 2L + 1L;
        rand.setSeed((long)p_73153_2_ * k + (long)p_73153_3_ * l ^ worldObj.getSeed());
        boolean flag = false;
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(p_73153_2_, p_73153_3_);

        if (settings.useMineShafts && mapFeaturesEnabled)
        {
            mineshaftGenerator.generateStructure(worldObj, rand, chunkcoordintpair);
        }

        if (settings.useVillages && mapFeaturesEnabled)
        {
            flag = villageGenerator.generateStructure(worldObj, rand, chunkcoordintpair);
        }

        if (settings.useStrongholds && mapFeaturesEnabled)
        {
            strongholdGenerator.generateStructure(worldObj, rand, chunkcoordintpair);
        }

        if (settings.useTemples && mapFeaturesEnabled)
        {
            scatteredFeatureGenerator.generateStructure(worldObj, rand, chunkcoordintpair);
        }

        if (settings.useMonuments && mapFeaturesEnabled)
        {
            oceanMonumentGenerator.generateStructure(worldObj, rand, chunkcoordintpair);
        }

        if (biomegenbase != BiomeGenBase.desert && biomegenbase != BiomeGenBase.desertHills && settings.useWaterLakes && !flag && rand.nextInt(settings.waterLakeChance) == 0)
        {
            int i1 = rand.nextInt(16) + 8;
            int j1 = rand.nextInt(256);
            int k1 = rand.nextInt(16) + 8;
            (new WorldGenLakes(Blocks.water)).generate(worldObj, rand, blockpos.add(i1, j1, k1));
        }

        if (!flag && rand.nextInt(settings.lavaLakeChance / 10) == 0 && settings.useLavaLakes)
        {
            int i2 = rand.nextInt(16) + 8;
            int l2 = rand.nextInt(rand.nextInt(248) + 8);
            int k3 = rand.nextInt(16) + 8;

            if (l2 < worldObj.func_181545_F() || rand.nextInt(settings.lavaLakeChance / 8) == 0)
            {
                (new WorldGenLakes(Blocks.lava)).generate(worldObj, rand, blockpos.add(i2, l2, k3));
            }
        }

        if (settings.useDungeons)
        {
            for (int j2 = 0; j2 < settings.dungeonChance; ++j2)
            {
                int i3 = rand.nextInt(16) + 8;
                int l3 = rand.nextInt(256);
                int l1 = rand.nextInt(16) + 8;
                (new WorldGenDungeons()).generate(worldObj, rand, blockpos.add(i3, l3, l1));
            }
        }

        biomegenbase.decorate(worldObj, rand, new BlockPos(i, 0, j));
        SpawnerAnimals.performWorldGenSpawning(worldObj, biomegenbase, i + 8, j + 8, 16, 16, rand);
        blockpos = blockpos.add(8, 0, 8);

        for (int k2 = 0; k2 < 16; ++k2)
        {
            for (int j3 = 0; j3 < 16; ++j3)
            {
                BlockPos blockpos1 = worldObj.getPrecipitationHeight(blockpos.add(k2, 0, j3));
                BlockPos blockpos2 = blockpos1.down();

                if (worldObj.canBlockFreezeWater(blockpos2))
                {
                    worldObj.setBlockState(blockpos2, Blocks.ice.getDefaultState(), 2);
                }

                if (worldObj.canSnowAt(blockpos1, true))
                {
                    worldObj.setBlockState(blockpos1, Blocks.snow_layer.getDefaultState(), 2);
                }
            }
        }

        BlockFalling.fallInstantly = false;
    }

    public boolean func_177460_a(IChunkProvider p_177460_1_, Chunk p_177460_2_, int p_177460_3_, int p_177460_4_)
    {
        boolean flag = false;

        if (settings.useMonuments && mapFeaturesEnabled && p_177460_2_.getInhabitedTime() < 3600L)
        {
            flag |= oceanMonumentGenerator.generateStructure(worldObj, rand, new ChunkCoordIntPair(p_177460_3_, p_177460_4_));
        }

        return flag;
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate progressCallback)
    {
        return true;
    }

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
    public void saveExtraData()
    {
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    public boolean unloadQueuedChunks()
    {
        return false;
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave()
    {
        return true;
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString()
    {
        return "RandomLevelSource";
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        BiomeGenBase biomegenbase = worldObj.getBiomeGenForCoords(pos);

        if (mapFeaturesEnabled)
        {
            if (creatureType == EnumCreatureType.MONSTER && scatteredFeatureGenerator.func_175798_a(pos))
            {
                return scatteredFeatureGenerator.getScatteredFeatureSpawnList();
            }

            if (creatureType == EnumCreatureType.MONSTER && settings.useMonuments && oceanMonumentGenerator.func_175796_a(worldObj, pos))
            {
                return oceanMonumentGenerator.func_175799_b();
            }
        }

        return biomegenbase.getSpawnableList(creatureType);
    }

    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position)
    {
        return "Stronghold".equals(structureName) && strongholdGenerator != null ? strongholdGenerator.getClosestStrongholdPos(worldIn, position) : null;
    }

    public int getLoadedChunkCount()
    {
        return 0;
    }

    public void recreateStructures(Chunk p_180514_1_, int p_180514_2_, int p_180514_3_)
    {
        if (settings.useMineShafts && mapFeaturesEnabled)
        {
            mineshaftGenerator.generate(this, worldObj, p_180514_2_, p_180514_3_, null);
        }

        if (settings.useVillages && mapFeaturesEnabled)
        {
            villageGenerator.generate(this, worldObj, p_180514_2_, p_180514_3_, null);
        }

        if (settings.useStrongholds && mapFeaturesEnabled)
        {
            strongholdGenerator.generate(this, worldObj, p_180514_2_, p_180514_3_, null);
        }

        if (settings.useTemples && mapFeaturesEnabled)
        {
            scatteredFeatureGenerator.generate(this, worldObj, p_180514_2_, p_180514_3_, null);
        }

        if (settings.useMonuments && mapFeaturesEnabled)
        {
            oceanMonumentGenerator.generate(this, worldObj, p_180514_2_, p_180514_3_, null);
        }
    }

    public Chunk provideChunk(BlockPos blockPosIn)
    {
        return provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}
