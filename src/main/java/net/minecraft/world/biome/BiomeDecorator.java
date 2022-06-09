package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.GeneratorBushFeature;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenClay;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenSand;
import net.minecraft.world.gen.feature.WorldGenWaterlily;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeDecorator
{
    /** The world the BiomeDecorator is currently decorating */
    protected World currentWorld;

    /** The Biome Decorator's random number generator. */
    protected Random randomGenerator;
    protected BlockPos field_180294_c;
    protected ChunkProviderSettings chunkProviderSettings;

    /** The clay generator. */
    protected WorldGenerator clayGen = new WorldGenClay(4);

    /** The sand generator. */
    protected WorldGenerator sandGen = new WorldGenSand(Blocks.sand, 7);

    /** The gravel generator. */
    protected WorldGenerator gravelAsSandGen = new WorldGenSand(Blocks.gravel, 6);

    /** The dirt generator. */
    protected WorldGenerator dirtGen;
    protected WorldGenerator gravelGen;
    protected WorldGenerator graniteGen;
    protected WorldGenerator dioriteGen;
    protected WorldGenerator andesiteGen;
    protected WorldGenerator coalGen;
    protected WorldGenerator ironGen;

    /** Field that holds gold WorldGenMinable */
    protected WorldGenerator goldGen;
    protected WorldGenerator redstoneGen;
    protected WorldGenerator diamondGen;

    /** Field that holds Lapis WorldGenMinable */
    protected WorldGenerator lapisGen;
    protected WorldGenFlowers yellowFlowerGen = new WorldGenFlowers(Blocks.yellow_flower, BlockFlower.EnumFlowerType.DANDELION);

    /** Field that holds mushroomBrown WorldGenFlowers */
    protected WorldGenerator mushroomBrownGen = new GeneratorBushFeature(Blocks.brown_mushroom);

    /** Field that holds mushroomRed WorldGenFlowers */
    protected WorldGenerator mushroomRedGen = new GeneratorBushFeature(Blocks.red_mushroom);

    /** Field that holds big mushroom generator */
    protected WorldGenerator bigMushroomGen = new WorldGenBigMushroom();

    /** Field that holds WorldGenReed */
    protected WorldGenerator reedGen = new WorldGenReed();

    /** Field that holds WorldGenCactus */
    protected WorldGenerator cactusGen = new WorldGenCactus();

    /** The water lily generation! */
    protected WorldGenerator waterlilyGen = new WorldGenWaterlily();

    /** Amount of waterlilys per chunk. */
    protected int waterlilyPerChunk;

    /**
     * The number of trees to attempt to generate per chunk. Up to 10 in forests, none in deserts.
     */
    protected int treesPerChunk;

    /**
     * The number of yellow flower patches to generate per chunk. The game generates much less than this number, since
     * it attempts to generate them at a random altitude.
     */
    protected int flowersPerChunk = 2;

    /** The amount of tall grass to generate per chunk. */
    protected int grassPerChunk = 1;

    /**
     * The number of dead bushes to generate per chunk. Used in deserts and swamps.
     */
    protected int deadBushPerChunk;

    /**
     * The number of extra mushroom patches per chunk. It generates 1/4 this number in brown mushroom patches, and 1/8
     * this number in red mushroom patches. These mushrooms go beyond the default base number of mushrooms.
     */
    protected int mushroomsPerChunk;

    /**
     * The number of reeds to generate per chunk. Reeds won't generate if the randomly selected placement is unsuitable.
     */
    protected int reedsPerChunk;

    /**
     * The number of cactus plants to generate per chunk. Cacti only work on sand.
     */
    protected int cactiPerChunk;

    /**
     * The number of sand patches to generate per chunk. Sand patches only generate when part of it is underwater.
     */
    protected int sandPerChunk = 1;

    /**
     * The number of sand patches to generate per chunk. Sand patches only generate when part of it is underwater. There
     * appear to be two separate fields for this.
     */
    protected int sandPerChunk2 = 3;

    /**
     * The number of clay patches to generate per chunk. Only generates when part of it is underwater.
     */
    protected int clayPerChunk = 1;

    /** Amount of big mushrooms per chunk */
    protected int bigMushroomsPerChunk;

    /** True if decorator should generate surface lava & water */
    public boolean generateLakes = true;

    public void decorate(World worldIn, Random random, BiomeGenBase p_180292_3_, BlockPos p_180292_4_)
    {
        if (currentWorld != null)
        {
            throw new RuntimeException("Already decorating");
        }
        else
        {
            currentWorld = worldIn;
            String s = worldIn.getWorldInfo().getGeneratorOptions();

            if (s != null)
            {
                chunkProviderSettings = ChunkProviderSettings.Factory.jsonToFactory(s).func_177864_b();
            }
            else
            {
                chunkProviderSettings = ChunkProviderSettings.Factory.jsonToFactory("").func_177864_b();
            }

            randomGenerator = random;
            field_180294_c = p_180292_4_;
            dirtGen = new WorldGenMinable(Blocks.dirt.getDefaultState(), chunkProviderSettings.dirtSize);
            gravelGen = new WorldGenMinable(Blocks.gravel.getDefaultState(), chunkProviderSettings.gravelSize);
            graniteGen = new WorldGenMinable(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), chunkProviderSettings.graniteSize);
            dioriteGen = new WorldGenMinable(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), chunkProviderSettings.dioriteSize);
            andesiteGen = new WorldGenMinable(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), chunkProviderSettings.andesiteSize);
            coalGen = new WorldGenMinable(Blocks.coal_ore.getDefaultState(), chunkProviderSettings.coalSize);
            ironGen = new WorldGenMinable(Blocks.iron_ore.getDefaultState(), chunkProviderSettings.ironSize);
            goldGen = new WorldGenMinable(Blocks.gold_ore.getDefaultState(), chunkProviderSettings.goldSize);
            redstoneGen = new WorldGenMinable(Blocks.redstone_ore.getDefaultState(), chunkProviderSettings.redstoneSize);
            diamondGen = new WorldGenMinable(Blocks.diamond_ore.getDefaultState(), chunkProviderSettings.diamondSize);
            lapisGen = new WorldGenMinable(Blocks.lapis_ore.getDefaultState(), chunkProviderSettings.lapisSize);
            genDecorations(p_180292_3_);
            currentWorld = null;
            randomGenerator = null;
        }
    }

    protected void genDecorations(BiomeGenBase biomeGenBaseIn)
    {
        generateOres();

        for (int i = 0; i < sandPerChunk2; ++i)
        {
            int j = randomGenerator.nextInt(16) + 8;
            int k = randomGenerator.nextInt(16) + 8;
            sandGen.generate(currentWorld, randomGenerator, currentWorld.getTopSolidOrLiquidBlock(field_180294_c.add(j, 0, k)));
        }

        for (int i1 = 0; i1 < clayPerChunk; ++i1)
        {
            int l1 = randomGenerator.nextInt(16) + 8;
            int i6 = randomGenerator.nextInt(16) + 8;
            clayGen.generate(currentWorld, randomGenerator, currentWorld.getTopSolidOrLiquidBlock(field_180294_c.add(l1, 0, i6)));
        }

        for (int j1 = 0; j1 < sandPerChunk; ++j1)
        {
            int i2 = randomGenerator.nextInt(16) + 8;
            int j6 = randomGenerator.nextInt(16) + 8;
            gravelAsSandGen.generate(currentWorld, randomGenerator, currentWorld.getTopSolidOrLiquidBlock(field_180294_c.add(i2, 0, j6)));
        }

        int k1 = treesPerChunk;

        if (randomGenerator.nextInt(10) == 0)
        {
            ++k1;
        }

        for (int j2 = 0; j2 < k1; ++j2)
        {
            int k6 = randomGenerator.nextInt(16) + 8;
            int l = randomGenerator.nextInt(16) + 8;
            WorldGenAbstractTree worldgenabstracttree = biomeGenBaseIn.genBigTreeChance(randomGenerator);
            worldgenabstracttree.func_175904_e();
            BlockPos blockpos = currentWorld.getHeight(field_180294_c.add(k6, 0, l));

            if (worldgenabstracttree.generate(currentWorld, randomGenerator, blockpos))
            {
                worldgenabstracttree.func_180711_a(currentWorld, randomGenerator, blockpos);
            }
        }

        for (int k2 = 0; k2 < bigMushroomsPerChunk; ++k2)
        {
            int l6 = randomGenerator.nextInt(16) + 8;
            int k10 = randomGenerator.nextInt(16) + 8;
            bigMushroomGen.generate(currentWorld, randomGenerator, currentWorld.getHeight(field_180294_c.add(l6, 0, k10)));
        }

        for (int l2 = 0; l2 < flowersPerChunk; ++l2)
        {
            int i7 = randomGenerator.nextInt(16) + 8;
            int l10 = randomGenerator.nextInt(16) + 8;
            int j14 = currentWorld.getHeight(field_180294_c.add(i7, 0, l10)).getY() + 32;

            if (j14 > 0)
            {
                int k17 = randomGenerator.nextInt(j14);
                BlockPos blockpos1 = field_180294_c.add(i7, k17, l10);
                BlockFlower.EnumFlowerType blockflower$enumflowertype = biomeGenBaseIn.pickRandomFlower(randomGenerator, blockpos1);
                BlockFlower blockflower = blockflower$enumflowertype.getBlockType().getBlock();

                if (blockflower.getMaterial() != Material.air)
                {
                    yellowFlowerGen.setGeneratedBlock(blockflower, blockflower$enumflowertype);
                    yellowFlowerGen.generate(currentWorld, randomGenerator, blockpos1);
                }
            }
        }

        for (int i3 = 0; i3 < grassPerChunk; ++i3)
        {
            int j7 = randomGenerator.nextInt(16) + 8;
            int i11 = randomGenerator.nextInt(16) + 8;
            int k14 = currentWorld.getHeight(field_180294_c.add(j7, 0, i11)).getY() * 2;

            if (k14 > 0)
            {
                int l17 = randomGenerator.nextInt(k14);
                biomeGenBaseIn.getRandomWorldGenForGrass(randomGenerator).generate(currentWorld, randomGenerator, field_180294_c.add(j7, l17, i11));
            }
        }

        for (int j3 = 0; j3 < deadBushPerChunk; ++j3)
        {
            int k7 = randomGenerator.nextInt(16) + 8;
            int j11 = randomGenerator.nextInt(16) + 8;
            int l14 = currentWorld.getHeight(field_180294_c.add(k7, 0, j11)).getY() * 2;

            if (l14 > 0)
            {
                int i18 = randomGenerator.nextInt(l14);
                (new WorldGenDeadBush()).generate(currentWorld, randomGenerator, field_180294_c.add(k7, i18, j11));
            }
        }

        for (int k3 = 0; k3 < waterlilyPerChunk; ++k3)
        {
            int l7 = randomGenerator.nextInt(16) + 8;
            int k11 = randomGenerator.nextInt(16) + 8;
            int i15 = currentWorld.getHeight(field_180294_c.add(l7, 0, k11)).getY() * 2;

            if (i15 > 0)
            {
                int j18 = randomGenerator.nextInt(i15);
                BlockPos blockpos4;
                BlockPos blockpos7;

                for (blockpos4 = field_180294_c.add(l7, j18, k11); blockpos4.getY() > 0; blockpos4 = blockpos7)
                {
                    blockpos7 = blockpos4.down();

                    if (!currentWorld.isAirBlock(blockpos7))
                    {
                        break;
                    }
                }

                waterlilyGen.generate(currentWorld, randomGenerator, blockpos4);
            }
        }

        for (int l3 = 0; l3 < mushroomsPerChunk; ++l3)
        {
            if (randomGenerator.nextInt(4) == 0)
            {
                int i8 = randomGenerator.nextInt(16) + 8;
                int l11 = randomGenerator.nextInt(16) + 8;
                BlockPos blockpos2 = currentWorld.getHeight(field_180294_c.add(i8, 0, l11));
                mushroomBrownGen.generate(currentWorld, randomGenerator, blockpos2);
            }

            if (randomGenerator.nextInt(8) == 0)
            {
                int j8 = randomGenerator.nextInt(16) + 8;
                int i12 = randomGenerator.nextInt(16) + 8;
                int j15 = currentWorld.getHeight(field_180294_c.add(j8, 0, i12)).getY() * 2;

                if (j15 > 0)
                {
                    int k18 = randomGenerator.nextInt(j15);
                    BlockPos blockpos5 = field_180294_c.add(j8, k18, i12);
                    mushroomRedGen.generate(currentWorld, randomGenerator, blockpos5);
                }
            }
        }

        if (randomGenerator.nextInt(4) == 0)
        {
            int i4 = randomGenerator.nextInt(16) + 8;
            int k8 = randomGenerator.nextInt(16) + 8;
            int j12 = currentWorld.getHeight(field_180294_c.add(i4, 0, k8)).getY() * 2;

            if (j12 > 0)
            {
                int k15 = randomGenerator.nextInt(j12);
                mushroomBrownGen.generate(currentWorld, randomGenerator, field_180294_c.add(i4, k15, k8));
            }
        }

        if (randomGenerator.nextInt(8) == 0)
        {
            int j4 = randomGenerator.nextInt(16) + 8;
            int l8 = randomGenerator.nextInt(16) + 8;
            int k12 = currentWorld.getHeight(field_180294_c.add(j4, 0, l8)).getY() * 2;

            if (k12 > 0)
            {
                int l15 = randomGenerator.nextInt(k12);
                mushroomRedGen.generate(currentWorld, randomGenerator, field_180294_c.add(j4, l15, l8));
            }
        }

        for (int k4 = 0; k4 < reedsPerChunk; ++k4)
        {
            int i9 = randomGenerator.nextInt(16) + 8;
            int l12 = randomGenerator.nextInt(16) + 8;
            int i16 = currentWorld.getHeight(field_180294_c.add(i9, 0, l12)).getY() * 2;

            if (i16 > 0)
            {
                int l18 = randomGenerator.nextInt(i16);
                reedGen.generate(currentWorld, randomGenerator, field_180294_c.add(i9, l18, l12));
            }
        }

        for (int l4 = 0; l4 < 10; ++l4)
        {
            int j9 = randomGenerator.nextInt(16) + 8;
            int i13 = randomGenerator.nextInt(16) + 8;
            int j16 = currentWorld.getHeight(field_180294_c.add(j9, 0, i13)).getY() * 2;

            if (j16 > 0)
            {
                int i19 = randomGenerator.nextInt(j16);
                reedGen.generate(currentWorld, randomGenerator, field_180294_c.add(j9, i19, i13));
            }
        }

        if (randomGenerator.nextInt(32) == 0)
        {
            int i5 = randomGenerator.nextInt(16) + 8;
            int k9 = randomGenerator.nextInt(16) + 8;
            int j13 = currentWorld.getHeight(field_180294_c.add(i5, 0, k9)).getY() * 2;

            if (j13 > 0)
            {
                int k16 = randomGenerator.nextInt(j13);
                (new WorldGenPumpkin()).generate(currentWorld, randomGenerator, field_180294_c.add(i5, k16, k9));
            }
        }

        for (int j5 = 0; j5 < cactiPerChunk; ++j5)
        {
            int l9 = randomGenerator.nextInt(16) + 8;
            int k13 = randomGenerator.nextInt(16) + 8;
            int l16 = currentWorld.getHeight(field_180294_c.add(l9, 0, k13)).getY() * 2;

            if (l16 > 0)
            {
                int j19 = randomGenerator.nextInt(l16);
                cactusGen.generate(currentWorld, randomGenerator, field_180294_c.add(l9, j19, k13));
            }
        }

        if (generateLakes)
        {
            for (int k5 = 0; k5 < 50; ++k5)
            {
                int i10 = randomGenerator.nextInt(16) + 8;
                int l13 = randomGenerator.nextInt(16) + 8;
                int i17 = randomGenerator.nextInt(248) + 8;

                if (i17 > 0)
                {
                    int k19 = randomGenerator.nextInt(i17);
                    BlockPos blockpos6 = field_180294_c.add(i10, k19, l13);
                    (new WorldGenLiquids(Blocks.flowing_water)).generate(currentWorld, randomGenerator, blockpos6);
                }
            }

            for (int l5 = 0; l5 < 20; ++l5)
            {
                int j10 = randomGenerator.nextInt(16) + 8;
                int i14 = randomGenerator.nextInt(16) + 8;
                int j17 = randomGenerator.nextInt(randomGenerator.nextInt(randomGenerator.nextInt(240) + 8) + 8);
                BlockPos blockpos3 = field_180294_c.add(j10, j17, i14);
                (new WorldGenLiquids(Blocks.flowing_lava)).generate(currentWorld, randomGenerator, blockpos3);
            }
        }
    }

    /**
     * Standard ore generation helper. Generates most ores.
     */
    protected void genStandardOre1(int blockCount, WorldGenerator generator, int minHeight, int maxHeight)
    {
        if (maxHeight < minHeight)
        {
            int i = minHeight;
            minHeight = maxHeight;
            maxHeight = i;
        }
        else if (maxHeight == minHeight)
        {
            if (minHeight < 255)
            {
                ++maxHeight;
            }
            else
            {
                --minHeight;
            }
        }

        for (int j = 0; j < blockCount; ++j)
        {
            BlockPos blockpos = field_180294_c.add(randomGenerator.nextInt(16), randomGenerator.nextInt(maxHeight - minHeight) + minHeight, randomGenerator.nextInt(16));
            generator.generate(currentWorld, randomGenerator, blockpos);
        }
    }

    /**
     * Standard ore generation helper. Generates Lapis Lazuli.
     */
    protected void genStandardOre2(int blockCount, WorldGenerator generator, int centerHeight, int spread)
    {
        for (int i = 0; i < blockCount; ++i)
        {
            BlockPos blockpos = field_180294_c.add(randomGenerator.nextInt(16), randomGenerator.nextInt(spread) + randomGenerator.nextInt(spread) + centerHeight - spread, randomGenerator.nextInt(16));
            generator.generate(currentWorld, randomGenerator, blockpos);
        }
    }

    /**
     * Generates ores in the current chunk
     */
    protected void generateOres()
    {
        genStandardOre1(chunkProviderSettings.dirtCount, dirtGen, chunkProviderSettings.dirtMinHeight, chunkProviderSettings.dirtMaxHeight);
        genStandardOre1(chunkProviderSettings.gravelCount, gravelGen, chunkProviderSettings.gravelMinHeight, chunkProviderSettings.gravelMaxHeight);
        genStandardOre1(chunkProviderSettings.dioriteCount, dioriteGen, chunkProviderSettings.dioriteMinHeight, chunkProviderSettings.dioriteMaxHeight);
        genStandardOre1(chunkProviderSettings.graniteCount, graniteGen, chunkProviderSettings.graniteMinHeight, chunkProviderSettings.graniteMaxHeight);
        genStandardOre1(chunkProviderSettings.andesiteCount, andesiteGen, chunkProviderSettings.andesiteMinHeight, chunkProviderSettings.andesiteMaxHeight);
        genStandardOre1(chunkProviderSettings.coalCount, coalGen, chunkProviderSettings.coalMinHeight, chunkProviderSettings.coalMaxHeight);
        genStandardOre1(chunkProviderSettings.ironCount, ironGen, chunkProviderSettings.ironMinHeight, chunkProviderSettings.ironMaxHeight);
        genStandardOre1(chunkProviderSettings.goldCount, goldGen, chunkProviderSettings.goldMinHeight, chunkProviderSettings.goldMaxHeight);
        genStandardOre1(chunkProviderSettings.redstoneCount, redstoneGen, chunkProviderSettings.redstoneMinHeight, chunkProviderSettings.redstoneMaxHeight);
        genStandardOre1(chunkProviderSettings.diamondCount, diamondGen, chunkProviderSettings.diamondMinHeight, chunkProviderSettings.diamondMaxHeight);
        genStandardOre2(chunkProviderSettings.lapisCount, lapisGen, chunkProviderSettings.lapisCenterHeight, chunkProviderSettings.lapisSpread);
    }
}
