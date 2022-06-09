package net.minecraft.world;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class ChunkCache implements IBlockAccess
{
    protected int chunkX;
    protected int chunkZ;
    protected Chunk[][] chunkArray;

    /** set by !chunk.getAreLevelsEmpty */
    protected boolean hasExtendedLevels;

    /** Reference to the World object. */
    protected World worldObj;

    public ChunkCache(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn)
    {
        worldObj = worldIn;
        chunkX = posFromIn.getX() - subIn >> 4;
        chunkZ = posFromIn.getZ() - subIn >> 4;
        int i = posToIn.getX() + subIn >> 4;
        int j = posToIn.getZ() + subIn >> 4;
        chunkArray = new Chunk[i - chunkX + 1][j - chunkZ + 1];
        hasExtendedLevels = true;

        for (int k = chunkX; k <= i; ++k)
        {
            for (int l = chunkZ; l <= j; ++l)
            {
                chunkArray[k - chunkX][l - chunkZ] = worldIn.getChunkFromChunkCoords(k, l);
            }
        }

        for (int i1 = posFromIn.getX() >> 4; i1 <= posToIn.getX() >> 4; ++i1)
        {
            for (int j1 = posFromIn.getZ() >> 4; j1 <= posToIn.getZ() >> 4; ++j1)
            {
                Chunk chunk = chunkArray[i1 - chunkX][j1 - chunkZ];

                if (chunk != null && !chunk.getAreLevelsEmpty(posFromIn.getY(), posToIn.getY()))
                {
                    hasExtendedLevels = false;
                }
            }
        }
    }

    /**
     * set by !chunk.getAreLevelsEmpty
     */
    public boolean extendedLevelsInChunkCache()
    {
        return hasExtendedLevels;
    }

    public TileEntity getTileEntity(BlockPos pos)
    {
        int i = (pos.getX() >> 4) - chunkX;
        int j = (pos.getZ() >> 4) - chunkZ;
        return chunkArray[i][j].getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
    }

    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        int i = getLightForExt(EnumSkyBlock.SKY, pos);
        int j = getLightForExt(EnumSkyBlock.BLOCK, pos);

        if (j < lightValue)
        {
            j = lightValue;
        }

        return i << 20 | j << 4;
    }

    public IBlockState getBlockState(BlockPos pos)
    {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int i = (pos.getX() >> 4) - chunkX;
            int j = (pos.getZ() >> 4) - chunkZ;

            if (i >= 0 && i < chunkArray.length && j >= 0 && j < chunkArray[i].length)
            {
                Chunk chunk = chunkArray[i][j];

                if (chunk != null)
                {
                    return chunk.getBlockState(pos);
                }
            }
        }

        return Blocks.air.getDefaultState();
    }

    public BiomeGenBase getBiomeGenForCoords(BlockPos pos)
    {
        return worldObj.getBiomeGenForCoords(pos);
    }

    private int getLightForExt(EnumSkyBlock p_175629_1_, BlockPos pos)
    {
        if (p_175629_1_ == EnumSkyBlock.SKY && worldObj.provider.getHasNoSky())
        {
            return 0;
        }
        else if (pos.getY() >= 0 && pos.getY() < 256)
        {
            if (getBlockState(pos).getBlock().getUseNeighborBrightness())
            {
                int l = 0;

                for (EnumFacing enumfacing : EnumFacing.values())
                {
                    int k = getLightFor(p_175629_1_, pos.offset(enumfacing));

                    if (k > l)
                    {
                        l = k;
                    }

                    if (l >= 15)
                    {
                        return l;
                    }
                }

                return l;
            }
            else
            {
                int i = (pos.getX() >> 4) - chunkX;
                int j = (pos.getZ() >> 4) - chunkZ;
                return chunkArray[i][j].getLightFor(p_175629_1_, pos);
            }
        }
        else
        {
            return p_175629_1_.defaultLightValue;
        }
    }

    /**
     * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
     * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
     */
    public boolean isAirBlock(BlockPos pos)
    {
        return getBlockState(pos).getBlock().getMaterial() == Material.air;
    }

    public int getLightFor(EnumSkyBlock p_175628_1_, BlockPos pos)
    {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int i = (pos.getX() >> 4) - chunkX;
            int j = (pos.getZ() >> 4) - chunkZ;
            return chunkArray[i][j].getLightFor(p_175628_1_, pos);
        }
        else
        {
            return p_175628_1_.defaultLightValue;
        }
    }

    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        IBlockState iblockstate = getBlockState(pos);
        return iblockstate.getBlock().getStrongPower(this, pos, iblockstate, direction);
    }

    public WorldType getWorldType()
    {
        return worldObj.getWorldType();
    }
}
