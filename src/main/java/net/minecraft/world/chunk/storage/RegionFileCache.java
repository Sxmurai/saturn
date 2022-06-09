package net.minecraft.world.chunk.storage;

import com.google.common.collect.Maps;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RegionFileCache
{
    private static final Map<File, RegionFile> regionsByFilename = Maps.newHashMap();

    public static synchronized RegionFile createOrLoadRegionFile(File worldDir, int chunkX, int chunkZ)
    {
        File file1 = new File(worldDir, "region");
        File file2 = new File(file1, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca");
        RegionFile regionfile = RegionFileCache.regionsByFilename.get(file2);

        if (regionfile != null)
        {
            return regionfile;
        }
        else
        {
            if (!file1.exists())
            {
                file1.mkdirs();
            }

            if (RegionFileCache.regionsByFilename.size() >= 256)
            {
                RegionFileCache.clearRegionFileReferences();
            }

            RegionFile regionfile1 = new RegionFile(file2);
            RegionFileCache.regionsByFilename.put(file2, regionfile1);
            return regionfile1;
        }
    }

    /**
     * clears region file references
     */

    public static synchronized void clearRegionFileReferences()
    {
        for (RegionFile regionfile : RegionFileCache.regionsByFilename.values())
        {
            try
            {
                if (regionfile != null)
                {
                    regionfile.close();
                }
            }
            catch (IOException ioexception)
            {
                ioexception.printStackTrace();
            }
        }

        RegionFileCache.regionsByFilename.clear();
    }

    /**
     * Returns an input stream for the specified chunk. Args: worldDir, chunkX, chunkZ
     */
    public static DataInputStream getChunkInputStream(File worldDir, int chunkX, int chunkZ)
    {
        RegionFile regionfile = RegionFileCache.createOrLoadRegionFile(worldDir, chunkX, chunkZ);
        return regionfile.getChunkDataInputStream(chunkX & 31, chunkZ & 31);
    }

    /**
     * Returns an output stream for the specified chunk. Args: worldDir, chunkX, chunkZ
     */
    public static DataOutputStream getChunkOutputStream(File worldDir, int chunkX, int chunkZ)
    {
        RegionFile regionfile = RegionFileCache.createOrLoadRegionFile(worldDir, chunkX, chunkZ);
        return regionfile.getChunkDataOutputStream(chunkX & 31, chunkZ & 31);
    }
}
