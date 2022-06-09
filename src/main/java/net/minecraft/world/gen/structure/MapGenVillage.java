package net.minecraft.world.gen.structure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class MapGenVillage extends MapGenStructure
{
    public static final List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(BiomeGenBase.plains, BiomeGenBase.desert, BiomeGenBase.savanna);

    /** World terrain type, 0 for normal, 1 for flat map */
    private int terrainType;
    private int field_82665_g;
    private final int field_82666_h;

    public MapGenVillage()
    {
        field_82665_g = 32;
        field_82666_h = 8;
    }

    public MapGenVillage(Map<String, String> p_i2093_1_)
    {
        this();

        for (Map.Entry<String, String> entry : p_i2093_1_.entrySet())
        {
            if (entry.getKey().equals("size"))
            {
                terrainType = MathHelper.parseIntWithDefaultAndMax(entry.getValue(), terrainType, 0);
            }
            else if (entry.getKey().equals("distance"))
            {
                field_82665_g = MathHelper.parseIntWithDefaultAndMax(entry.getValue(), field_82665_g, field_82666_h + 1);
            }
        }
    }

    public String getStructureName()
    {
        return "Village";
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= field_82665_g - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= field_82665_g - 1;
        }

        int k = chunkX / field_82665_g;
        int l = chunkZ / field_82665_g;
        Random random = worldObj.setRandomSeed(k, l, 10387312);
        k = k * field_82665_g;
        l = l * field_82665_g;
        k = k + random.nextInt(field_82665_g - field_82666_h);
        l = l + random.nextInt(field_82665_g - field_82666_h);

        if (i == k && j == l)
        {
            boolean flag = worldObj.getWorldChunkManager().areBiomesViable(i * 16 + 8, j * 16 + 8, 0, MapGenVillage.villageSpawnBiomes);

            return flag;
        }

        return false;
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new MapGenVillage.Start(worldObj, rand, chunkX, chunkZ, terrainType);
    }

    public static class Start extends StructureStart
    {
        private boolean hasMoreThanTwoComponents;

        public Start()
        {
        }

        public Start(World worldIn, Random rand, int x, int z, int p_i2092_5_)
        {
            super(x, z);
            List<StructureVillagePieces.PieceWeight> list = StructureVillagePieces.getStructureVillageWeightedPieceList(rand, p_i2092_5_);
            StructureVillagePieces.Start structurevillagepieces$start = new StructureVillagePieces.Start(worldIn.getWorldChunkManager(), 0, rand, (x << 4) + 2, (z << 4) + 2, list, p_i2092_5_);
            components.add(structurevillagepieces$start);
            structurevillagepieces$start.buildComponent(structurevillagepieces$start, components, rand);
            List<StructureComponent> list1 = structurevillagepieces$start.field_74930_j;
            List<StructureComponent> list2 = structurevillagepieces$start.field_74932_i;

            while (!list1.isEmpty() || !list2.isEmpty())
            {
                if (list1.isEmpty())
                {
                    int i = rand.nextInt(list2.size());
                    StructureComponent structurecomponent = list2.remove(i);
                    structurecomponent.buildComponent(structurevillagepieces$start, components, rand);
                }
                else
                {
                    int j = rand.nextInt(list1.size());
                    StructureComponent structurecomponent2 = list1.remove(j);
                    structurecomponent2.buildComponent(structurevillagepieces$start, components, rand);
                }
            }

            updateBoundingBox();
            int k = 0;

            for (StructureComponent structurecomponent1 : components)
            {
                if (!(structurecomponent1 instanceof StructureVillagePieces.Road))
                {
                    ++k;
                }
            }

            hasMoreThanTwoComponents = k > 2;
        }

        public boolean isSizeableStructure()
        {
            return hasMoreThanTwoComponents;
        }

        public void writeToNBT(NBTTagCompound tagCompound)
        {
            super.writeToNBT(tagCompound);
            tagCompound.setBoolean("Valid", hasMoreThanTwoComponents);
        }

        public void readFromNBT(NBTTagCompound tagCompound)
        {
            super.readFromNBT(tagCompound);
            hasMoreThanTwoComponents = tagCompound.getBoolean("Valid");
        }
    }
}
