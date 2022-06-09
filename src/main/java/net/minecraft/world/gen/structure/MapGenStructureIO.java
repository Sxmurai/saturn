package net.minecraft.world.gen.structure;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapGenStructureIO
{
    private static final Logger logger = LogManager.getLogger();
    private static final Map < String, Class <? extends StructureStart >> startNameToClassMap = Maps.newHashMap();
    private static final Map < Class <? extends StructureStart > , String > startClassToNameMap = Maps.newHashMap();
    private static final Map < String, Class <? extends StructureComponent >> componentNameToClassMap = Maps.newHashMap();
    private static final Map < Class <? extends StructureComponent > , String > componentClassToNameMap = Maps.newHashMap();

    private static void registerStructure(Class <? extends StructureStart > startClass, String structureName)
    {
        MapGenStructureIO.startNameToClassMap.put(structureName, startClass);
        MapGenStructureIO.startClassToNameMap.put(startClass, structureName);
    }

    static void registerStructureComponent(Class <? extends StructureComponent > componentClass, String componentName)
    {
        MapGenStructureIO.componentNameToClassMap.put(componentName, componentClass);
        MapGenStructureIO.componentClassToNameMap.put(componentClass, componentName);
    }

    public static String getStructureStartName(StructureStart start)
    {
        return MapGenStructureIO.startClassToNameMap.get(start.getClass());
    }

    public static String getStructureComponentName(StructureComponent component)
    {
        return MapGenStructureIO.componentClassToNameMap.get(component.getClass());
    }

    public static StructureStart getStructureStart(NBTTagCompound tagCompound, World worldIn)
    {
        StructureStart structurestart = null;

        try
        {
            Class <? extends StructureStart > oclass = MapGenStructureIO.startNameToClassMap.get(tagCompound.getString("id"));

            if (oclass != null)
            {
                structurestart = oclass.newInstance();
            }
        }
        catch (Exception exception)
        {
            MapGenStructureIO.logger.warn("Failed Start with id " + tagCompound.getString("id"));
            exception.printStackTrace();
        }

        if (structurestart != null)
        {
            structurestart.readStructureComponentsFromNBT(worldIn, tagCompound);
        }
        else
        {
            MapGenStructureIO.logger.warn("Skipping Structure with id " + tagCompound.getString("id"));
        }

        return structurestart;
    }

    public static StructureComponent getStructureComponent(NBTTagCompound tagCompound, World worldIn)
    {
        StructureComponent structurecomponent = null;

        try
        {
            Class <? extends StructureComponent > oclass = MapGenStructureIO.componentNameToClassMap.get(tagCompound.getString("id"));

            if (oclass != null)
            {
                structurecomponent = oclass.newInstance();
            }
        }
        catch (Exception exception)
        {
            MapGenStructureIO.logger.warn("Failed Piece with id " + tagCompound.getString("id"));
            exception.printStackTrace();
        }

        if (structurecomponent != null)
        {
            structurecomponent.readStructureBaseNBT(worldIn, tagCompound);
        }
        else
        {
            MapGenStructureIO.logger.warn("Skipping Piece with id " + tagCompound.getString("id"));
        }

        return structurecomponent;
    }

    static
    {
        MapGenStructureIO.registerStructure(StructureMineshaftStart.class, "Mineshaft");
        MapGenStructureIO.registerStructure(MapGenVillage.Start.class, "Village");
        MapGenStructureIO.registerStructure(MapGenNetherBridge.Start.class, "Fortress");
        MapGenStructureIO.registerStructure(MapGenStronghold.Start.class, "Stronghold");
        MapGenStructureIO.registerStructure(MapGenScatteredFeature.Start.class, "Temple");
        MapGenStructureIO.registerStructure(StructureOceanMonument.StartMonument.class, "Monument");
        StructureMineshaftPieces.registerStructurePieces();
        StructureVillagePieces.registerVillagePieces();
        StructureNetherBridgePieces.registerNetherFortressPieces();
        StructureStrongholdPieces.registerStrongholdPieces();
        ComponentScatteredFeaturePieces.registerScatteredFeaturePieces();
        StructureOceanMonumentPieces.registerOceanMonumentPieces();
    }
}
