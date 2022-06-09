package net.minecraft.world.gen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.biome.BiomeGenBase;

public class ChunkProviderSettings
{
    public final float coordinateScale;
    public final float heightScale;
    public final float upperLimitScale;
    public final float lowerLimitScale;
    public final float depthNoiseScaleX;
    public final float depthNoiseScaleZ;
    public final float depthNoiseScaleExponent;
    public final float mainNoiseScaleX;
    public final float mainNoiseScaleY;
    public final float mainNoiseScaleZ;
    public final float baseSize;
    public final float stretchY;
    public final float biomeDepthWeight;
    public final float biomeDepthOffSet;
    public final float biomeScaleWeight;
    public final float biomeScaleOffset;
    public final int seaLevel;
    public final boolean useCaves;
    public final boolean useDungeons;
    public final int dungeonChance;
    public final boolean useStrongholds;
    public final boolean useVillages;
    public final boolean useMineShafts;
    public final boolean useTemples;
    public final boolean useMonuments;
    public final boolean useRavines;
    public final boolean useWaterLakes;
    public final int waterLakeChance;
    public final boolean useLavaLakes;
    public final int lavaLakeChance;
    public final boolean useLavaOceans;
    public final int fixedBiome;
    public final int biomeSize;
    public final int riverSize;
    public final int dirtSize;
    public final int dirtCount;
    public final int dirtMinHeight;
    public final int dirtMaxHeight;
    public final int gravelSize;
    public final int gravelCount;
    public final int gravelMinHeight;
    public final int gravelMaxHeight;
    public final int graniteSize;
    public final int graniteCount;
    public final int graniteMinHeight;
    public final int graniteMaxHeight;
    public final int dioriteSize;
    public final int dioriteCount;
    public final int dioriteMinHeight;
    public final int dioriteMaxHeight;
    public final int andesiteSize;
    public final int andesiteCount;
    public final int andesiteMinHeight;
    public final int andesiteMaxHeight;
    public final int coalSize;
    public final int coalCount;
    public final int coalMinHeight;
    public final int coalMaxHeight;
    public final int ironSize;
    public final int ironCount;
    public final int ironMinHeight;
    public final int ironMaxHeight;
    public final int goldSize;
    public final int goldCount;
    public final int goldMinHeight;
    public final int goldMaxHeight;
    public final int redstoneSize;
    public final int redstoneCount;
    public final int redstoneMinHeight;
    public final int redstoneMaxHeight;
    public final int diamondSize;
    public final int diamondCount;
    public final int diamondMinHeight;
    public final int diamondMaxHeight;
    public final int lapisSize;
    public final int lapisCount;
    public final int lapisCenterHeight;
    public final int lapisSpread;

    private ChunkProviderSettings(ChunkProviderSettings.Factory settingsFactory)
    {
        coordinateScale = settingsFactory.coordinateScale;
        heightScale = settingsFactory.heightScale;
        upperLimitScale = settingsFactory.upperLimitScale;
        lowerLimitScale = settingsFactory.lowerLimitScale;
        depthNoiseScaleX = settingsFactory.depthNoiseScaleX;
        depthNoiseScaleZ = settingsFactory.depthNoiseScaleZ;
        depthNoiseScaleExponent = settingsFactory.depthNoiseScaleExponent;
        mainNoiseScaleX = settingsFactory.mainNoiseScaleX;
        mainNoiseScaleY = settingsFactory.mainNoiseScaleY;
        mainNoiseScaleZ = settingsFactory.mainNoiseScaleZ;
        baseSize = settingsFactory.baseSize;
        stretchY = settingsFactory.stretchY;
        biomeDepthWeight = settingsFactory.biomeDepthWeight;
        biomeDepthOffSet = settingsFactory.biomeDepthOffset;
        biomeScaleWeight = settingsFactory.biomeScaleWeight;
        biomeScaleOffset = settingsFactory.biomeScaleOffset;
        seaLevel = settingsFactory.seaLevel;
        useCaves = settingsFactory.useCaves;
        useDungeons = settingsFactory.useDungeons;
        dungeonChance = settingsFactory.dungeonChance;
        useStrongholds = settingsFactory.useStrongholds;
        useVillages = settingsFactory.useVillages;
        useMineShafts = settingsFactory.useMineShafts;
        useTemples = settingsFactory.useTemples;
        useMonuments = settingsFactory.useMonuments;
        useRavines = settingsFactory.useRavines;
        useWaterLakes = settingsFactory.useWaterLakes;
        waterLakeChance = settingsFactory.waterLakeChance;
        useLavaLakes = settingsFactory.useLavaLakes;
        lavaLakeChance = settingsFactory.lavaLakeChance;
        useLavaOceans = settingsFactory.useLavaOceans;
        fixedBiome = settingsFactory.fixedBiome;
        biomeSize = settingsFactory.biomeSize;
        riverSize = settingsFactory.riverSize;
        dirtSize = settingsFactory.dirtSize;
        dirtCount = settingsFactory.dirtCount;
        dirtMinHeight = settingsFactory.dirtMinHeight;
        dirtMaxHeight = settingsFactory.dirtMaxHeight;
        gravelSize = settingsFactory.gravelSize;
        gravelCount = settingsFactory.gravelCount;
        gravelMinHeight = settingsFactory.gravelMinHeight;
        gravelMaxHeight = settingsFactory.gravelMaxHeight;
        graniteSize = settingsFactory.graniteSize;
        graniteCount = settingsFactory.graniteCount;
        graniteMinHeight = settingsFactory.graniteMinHeight;
        graniteMaxHeight = settingsFactory.graniteMaxHeight;
        dioriteSize = settingsFactory.dioriteSize;
        dioriteCount = settingsFactory.dioriteCount;
        dioriteMinHeight = settingsFactory.dioriteMinHeight;
        dioriteMaxHeight = settingsFactory.dioriteMaxHeight;
        andesiteSize = settingsFactory.andesiteSize;
        andesiteCount = settingsFactory.andesiteCount;
        andesiteMinHeight = settingsFactory.andesiteMinHeight;
        andesiteMaxHeight = settingsFactory.andesiteMaxHeight;
        coalSize = settingsFactory.coalSize;
        coalCount = settingsFactory.coalCount;
        coalMinHeight = settingsFactory.coalMinHeight;
        coalMaxHeight = settingsFactory.coalMaxHeight;
        ironSize = settingsFactory.ironSize;
        ironCount = settingsFactory.ironCount;
        ironMinHeight = settingsFactory.ironMinHeight;
        ironMaxHeight = settingsFactory.ironMaxHeight;
        goldSize = settingsFactory.goldSize;
        goldCount = settingsFactory.goldCount;
        goldMinHeight = settingsFactory.goldMinHeight;
        goldMaxHeight = settingsFactory.goldMaxHeight;
        redstoneSize = settingsFactory.redstoneSize;
        redstoneCount = settingsFactory.redstoneCount;
        redstoneMinHeight = settingsFactory.redstoneMinHeight;
        redstoneMaxHeight = settingsFactory.redstoneMaxHeight;
        diamondSize = settingsFactory.diamondSize;
        diamondCount = settingsFactory.diamondCount;
        diamondMinHeight = settingsFactory.diamondMinHeight;
        diamondMaxHeight = settingsFactory.diamondMaxHeight;
        lapisSize = settingsFactory.lapisSize;
        lapisCount = settingsFactory.lapisCount;
        lapisCenterHeight = settingsFactory.lapisCenterHeight;
        lapisSpread = settingsFactory.lapisSpread;
    }

    public static class Factory
    {
        static final Gson JSON_ADAPTER = (new GsonBuilder()).registerTypeAdapter(ChunkProviderSettings.Factory.class, new ChunkProviderSettings.Serializer()).create();
        public float coordinateScale = 684.412F;
        public float heightScale = 684.412F;
        public float upperLimitScale = 512.0F;
        public float lowerLimitScale = 512.0F;
        public float depthNoiseScaleX = 200.0F;
        public float depthNoiseScaleZ = 200.0F;
        public float depthNoiseScaleExponent = 0.5F;
        public float mainNoiseScaleX = 80.0F;
        public float mainNoiseScaleY = 160.0F;
        public float mainNoiseScaleZ = 80.0F;
        public float baseSize = 8.5F;
        public float stretchY = 12.0F;
        public float biomeDepthWeight = 1.0F;
        public float biomeDepthOffset = 0.0F;
        public float biomeScaleWeight = 1.0F;
        public float biomeScaleOffset = 0.0F;
        public int seaLevel = 63;
        public boolean useCaves = true;
        public boolean useDungeons = true;
        public int dungeonChance = 8;
        public boolean useStrongholds = true;
        public boolean useVillages = true;
        public boolean useMineShafts = true;
        public boolean useTemples = true;
        public boolean useMonuments = true;
        public boolean useRavines = true;
        public boolean useWaterLakes = true;
        public int waterLakeChance = 4;
        public boolean useLavaLakes = true;
        public int lavaLakeChance = 80;
        public boolean useLavaOceans = false;
        public int fixedBiome = -1;
        public int biomeSize = 4;
        public int riverSize = 4;
        public int dirtSize = 33;
        public int dirtCount = 10;
        public int dirtMinHeight = 0;
        public int dirtMaxHeight = 256;
        public int gravelSize = 33;
        public int gravelCount = 8;
        public int gravelMinHeight = 0;
        public int gravelMaxHeight = 256;
        public int graniteSize = 33;
        public int graniteCount = 10;
        public int graniteMinHeight = 0;
        public int graniteMaxHeight = 80;
        public int dioriteSize = 33;
        public int dioriteCount = 10;
        public int dioriteMinHeight = 0;
        public int dioriteMaxHeight = 80;
        public int andesiteSize = 33;
        public int andesiteCount = 10;
        public int andesiteMinHeight = 0;
        public int andesiteMaxHeight = 80;
        public int coalSize = 17;
        public int coalCount = 20;
        public int coalMinHeight = 0;
        public int coalMaxHeight = 128;
        public int ironSize = 9;
        public int ironCount = 20;
        public int ironMinHeight = 0;
        public int ironMaxHeight = 64;
        public int goldSize = 9;
        public int goldCount = 2;
        public int goldMinHeight = 0;
        public int goldMaxHeight = 32;
        public int redstoneSize = 8;
        public int redstoneCount = 8;
        public int redstoneMinHeight = 0;
        public int redstoneMaxHeight = 16;
        public int diamondSize = 8;
        public int diamondCount = 1;
        public int diamondMinHeight = 0;
        public int diamondMaxHeight = 16;
        public int lapisSize = 7;
        public int lapisCount = 1;
        public int lapisCenterHeight = 16;
        public int lapisSpread = 16;

        public static ChunkProviderSettings.Factory jsonToFactory(String p_177865_0_)
        {
            if (p_177865_0_.length() == 0)
            {
                return new ChunkProviderSettings.Factory();
            }
            else
            {
                try
                {
                    return Factory.JSON_ADAPTER.fromJson(p_177865_0_, Factory.class);
                }
                catch (Exception var2)
                {
                    return new ChunkProviderSettings.Factory();
                }
            }
        }

        public String toString()
        {
            return Factory.JSON_ADAPTER.toJson(this);
        }

        public Factory()
        {
            func_177863_a();
        }

        public void func_177863_a()
        {
            coordinateScale = 684.412F;
            heightScale = 684.412F;
            upperLimitScale = 512.0F;
            lowerLimitScale = 512.0F;
            depthNoiseScaleX = 200.0F;
            depthNoiseScaleZ = 200.0F;
            depthNoiseScaleExponent = 0.5F;
            mainNoiseScaleX = 80.0F;
            mainNoiseScaleY = 160.0F;
            mainNoiseScaleZ = 80.0F;
            baseSize = 8.5F;
            stretchY = 12.0F;
            biomeDepthWeight = 1.0F;
            biomeDepthOffset = 0.0F;
            biomeScaleWeight = 1.0F;
            biomeScaleOffset = 0.0F;
            seaLevel = 63;
            useCaves = true;
            useDungeons = true;
            dungeonChance = 8;
            useStrongholds = true;
            useVillages = true;
            useMineShafts = true;
            useTemples = true;
            useMonuments = true;
            useRavines = true;
            useWaterLakes = true;
            waterLakeChance = 4;
            useLavaLakes = true;
            lavaLakeChance = 80;
            useLavaOceans = false;
            fixedBiome = -1;
            biomeSize = 4;
            riverSize = 4;
            dirtSize = 33;
            dirtCount = 10;
            dirtMinHeight = 0;
            dirtMaxHeight = 256;
            gravelSize = 33;
            gravelCount = 8;
            gravelMinHeight = 0;
            gravelMaxHeight = 256;
            graniteSize = 33;
            graniteCount = 10;
            graniteMinHeight = 0;
            graniteMaxHeight = 80;
            dioriteSize = 33;
            dioriteCount = 10;
            dioriteMinHeight = 0;
            dioriteMaxHeight = 80;
            andesiteSize = 33;
            andesiteCount = 10;
            andesiteMinHeight = 0;
            andesiteMaxHeight = 80;
            coalSize = 17;
            coalCount = 20;
            coalMinHeight = 0;
            coalMaxHeight = 128;
            ironSize = 9;
            ironCount = 20;
            ironMinHeight = 0;
            ironMaxHeight = 64;
            goldSize = 9;
            goldCount = 2;
            goldMinHeight = 0;
            goldMaxHeight = 32;
            redstoneSize = 8;
            redstoneCount = 8;
            redstoneMinHeight = 0;
            redstoneMaxHeight = 16;
            diamondSize = 8;
            diamondCount = 1;
            diamondMinHeight = 0;
            diamondMaxHeight = 16;
            lapisSize = 7;
            lapisCount = 1;
            lapisCenterHeight = 16;
            lapisSpread = 16;
        }

        public boolean equals(Object p_equals_1_)
        {
            if (this == p_equals_1_)
            {
                return true;
            }
            else if (p_equals_1_ != null && getClass() == p_equals_1_.getClass())
            {
                ChunkProviderSettings.Factory chunkprovidersettings$factory = (ChunkProviderSettings.Factory)p_equals_1_;
                return andesiteCount == chunkprovidersettings$factory.andesiteCount && (andesiteMaxHeight == chunkprovidersettings$factory.andesiteMaxHeight && (andesiteMinHeight == chunkprovidersettings$factory.andesiteMinHeight && (andesiteSize == chunkprovidersettings$factory.andesiteSize && (Float.compare(chunkprovidersettings$factory.baseSize, baseSize) == 0 && (Float.compare(chunkprovidersettings$factory.biomeDepthOffset, biomeDepthOffset) == 0 && (Float.compare(chunkprovidersettings$factory.biomeDepthWeight, biomeDepthWeight) == 0 && (Float.compare(chunkprovidersettings$factory.biomeScaleOffset, biomeScaleOffset) == 0 && (Float.compare(chunkprovidersettings$factory.biomeScaleWeight, biomeScaleWeight) == 0 && (biomeSize == chunkprovidersettings$factory.biomeSize && (coalCount == chunkprovidersettings$factory.coalCount && (coalMaxHeight == chunkprovidersettings$factory.coalMaxHeight && (coalMinHeight == chunkprovidersettings$factory.coalMinHeight && (coalSize == chunkprovidersettings$factory.coalSize && (Float.compare(chunkprovidersettings$factory.coordinateScale, coordinateScale) == 0 && (Float.compare(chunkprovidersettings$factory.depthNoiseScaleExponent, depthNoiseScaleExponent) == 0 && (Float.compare(chunkprovidersettings$factory.depthNoiseScaleX, depthNoiseScaleX) == 0 && (Float.compare(chunkprovidersettings$factory.depthNoiseScaleZ, depthNoiseScaleZ) == 0 && (diamondCount == chunkprovidersettings$factory.diamondCount && (diamondMaxHeight == chunkprovidersettings$factory.diamondMaxHeight && (diamondMinHeight == chunkprovidersettings$factory.diamondMinHeight && (diamondSize == chunkprovidersettings$factory.diamondSize && (dioriteCount == chunkprovidersettings$factory.dioriteCount && (dioriteMaxHeight == chunkprovidersettings$factory.dioriteMaxHeight && (dioriteMinHeight == chunkprovidersettings$factory.dioriteMinHeight && (dioriteSize == chunkprovidersettings$factory.dioriteSize && (dirtCount == chunkprovidersettings$factory.dirtCount && (dirtMaxHeight == chunkprovidersettings$factory.dirtMaxHeight && (dirtMinHeight == chunkprovidersettings$factory.dirtMinHeight && (dirtSize == chunkprovidersettings$factory.dirtSize && (dungeonChance == chunkprovidersettings$factory.dungeonChance && (fixedBiome == chunkprovidersettings$factory.fixedBiome && (goldCount == chunkprovidersettings$factory.goldCount && (goldMaxHeight == chunkprovidersettings$factory.goldMaxHeight && (goldMinHeight == chunkprovidersettings$factory.goldMinHeight && (goldSize == chunkprovidersettings$factory.goldSize && (graniteCount == chunkprovidersettings$factory.graniteCount && (graniteMaxHeight == chunkprovidersettings$factory.graniteMaxHeight && (graniteMinHeight == chunkprovidersettings$factory.graniteMinHeight && (graniteSize == chunkprovidersettings$factory.graniteSize && (gravelCount == chunkprovidersettings$factory.gravelCount && (gravelMaxHeight == chunkprovidersettings$factory.gravelMaxHeight && (gravelMinHeight == chunkprovidersettings$factory.gravelMinHeight && (gravelSize == chunkprovidersettings$factory.gravelSize && (Float.compare(chunkprovidersettings$factory.heightScale, heightScale) == 0 && (ironCount == chunkprovidersettings$factory.ironCount && (ironMaxHeight == chunkprovidersettings$factory.ironMaxHeight && (ironMinHeight == chunkprovidersettings$factory.ironMinHeight && (ironSize == chunkprovidersettings$factory.ironSize && (lapisCenterHeight == chunkprovidersettings$factory.lapisCenterHeight && (lapisCount == chunkprovidersettings$factory.lapisCount && (lapisSize == chunkprovidersettings$factory.lapisSize && (lapisSpread == chunkprovidersettings$factory.lapisSpread && (lavaLakeChance == chunkprovidersettings$factory.lavaLakeChance && (Float.compare(chunkprovidersettings$factory.lowerLimitScale, lowerLimitScale) == 0 && (Float.compare(chunkprovidersettings$factory.mainNoiseScaleX, mainNoiseScaleX) == 0 && (Float.compare(chunkprovidersettings$factory.mainNoiseScaleY, mainNoiseScaleY) == 0 && (Float.compare(chunkprovidersettings$factory.mainNoiseScaleZ, mainNoiseScaleZ) == 0 && (redstoneCount == chunkprovidersettings$factory.redstoneCount && (redstoneMaxHeight == chunkprovidersettings$factory.redstoneMaxHeight && (redstoneMinHeight == chunkprovidersettings$factory.redstoneMinHeight && (redstoneSize == chunkprovidersettings$factory.redstoneSize && (riverSize == chunkprovidersettings$factory.riverSize && (seaLevel == chunkprovidersettings$factory.seaLevel && (Float.compare(chunkprovidersettings$factory.stretchY, stretchY) == 0 && (Float.compare(chunkprovidersettings$factory.upperLimitScale, upperLimitScale) == 0 && (useCaves == chunkprovidersettings$factory.useCaves && (useDungeons == chunkprovidersettings$factory.useDungeons && (useLavaLakes == chunkprovidersettings$factory.useLavaLakes && (useLavaOceans == chunkprovidersettings$factory.useLavaOceans && (useMineShafts == chunkprovidersettings$factory.useMineShafts && (useRavines == chunkprovidersettings$factory.useRavines && (useStrongholds == chunkprovidersettings$factory.useStrongholds && (useTemples == chunkprovidersettings$factory.useTemples && (useMonuments == chunkprovidersettings$factory.useMonuments && (useVillages == chunkprovidersettings$factory.useVillages && (useWaterLakes == chunkprovidersettings$factory.useWaterLakes && waterLakeChance == chunkprovidersettings$factory.waterLakeChance))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))));
            }
            else
            {
                return false;
            }
        }

        public int hashCode()
        {
            int i = coordinateScale != 0.0F ? Float.floatToIntBits(coordinateScale) : 0;
            i = 31 * i + (heightScale != 0.0F ? Float.floatToIntBits(heightScale) : 0);
            i = 31 * i + (upperLimitScale != 0.0F ? Float.floatToIntBits(upperLimitScale) : 0);
            i = 31 * i + (lowerLimitScale != 0.0F ? Float.floatToIntBits(lowerLimitScale) : 0);
            i = 31 * i + (depthNoiseScaleX != 0.0F ? Float.floatToIntBits(depthNoiseScaleX) : 0);
            i = 31 * i + (depthNoiseScaleZ != 0.0F ? Float.floatToIntBits(depthNoiseScaleZ) : 0);
            i = 31 * i + (depthNoiseScaleExponent != 0.0F ? Float.floatToIntBits(depthNoiseScaleExponent) : 0);
            i = 31 * i + (mainNoiseScaleX != 0.0F ? Float.floatToIntBits(mainNoiseScaleX) : 0);
            i = 31 * i + (mainNoiseScaleY != 0.0F ? Float.floatToIntBits(mainNoiseScaleY) : 0);
            i = 31 * i + (mainNoiseScaleZ != 0.0F ? Float.floatToIntBits(mainNoiseScaleZ) : 0);
            i = 31 * i + (baseSize != 0.0F ? Float.floatToIntBits(baseSize) : 0);
            i = 31 * i + (stretchY != 0.0F ? Float.floatToIntBits(stretchY) : 0);
            i = 31 * i + (biomeDepthWeight != 0.0F ? Float.floatToIntBits(biomeDepthWeight) : 0);
            i = 31 * i + (biomeDepthOffset != 0.0F ? Float.floatToIntBits(biomeDepthOffset) : 0);
            i = 31 * i + (biomeScaleWeight != 0.0F ? Float.floatToIntBits(biomeScaleWeight) : 0);
            i = 31 * i + (biomeScaleOffset != 0.0F ? Float.floatToIntBits(biomeScaleOffset) : 0);
            i = 31 * i + seaLevel;
            i = 31 * i + (useCaves ? 1 : 0);
            i = 31 * i + (useDungeons ? 1 : 0);
            i = 31 * i + dungeonChance;
            i = 31 * i + (useStrongholds ? 1 : 0);
            i = 31 * i + (useVillages ? 1 : 0);
            i = 31 * i + (useMineShafts ? 1 : 0);
            i = 31 * i + (useTemples ? 1 : 0);
            i = 31 * i + (useMonuments ? 1 : 0);
            i = 31 * i + (useRavines ? 1 : 0);
            i = 31 * i + (useWaterLakes ? 1 : 0);
            i = 31 * i + waterLakeChance;
            i = 31 * i + (useLavaLakes ? 1 : 0);
            i = 31 * i + lavaLakeChance;
            i = 31 * i + (useLavaOceans ? 1 : 0);
            i = 31 * i + fixedBiome;
            i = 31 * i + biomeSize;
            i = 31 * i + riverSize;
            i = 31 * i + dirtSize;
            i = 31 * i + dirtCount;
            i = 31 * i + dirtMinHeight;
            i = 31 * i + dirtMaxHeight;
            i = 31 * i + gravelSize;
            i = 31 * i + gravelCount;
            i = 31 * i + gravelMinHeight;
            i = 31 * i + gravelMaxHeight;
            i = 31 * i + graniteSize;
            i = 31 * i + graniteCount;
            i = 31 * i + graniteMinHeight;
            i = 31 * i + graniteMaxHeight;
            i = 31 * i + dioriteSize;
            i = 31 * i + dioriteCount;
            i = 31 * i + dioriteMinHeight;
            i = 31 * i + dioriteMaxHeight;
            i = 31 * i + andesiteSize;
            i = 31 * i + andesiteCount;
            i = 31 * i + andesiteMinHeight;
            i = 31 * i + andesiteMaxHeight;
            i = 31 * i + coalSize;
            i = 31 * i + coalCount;
            i = 31 * i + coalMinHeight;
            i = 31 * i + coalMaxHeight;
            i = 31 * i + ironSize;
            i = 31 * i + ironCount;
            i = 31 * i + ironMinHeight;
            i = 31 * i + ironMaxHeight;
            i = 31 * i + goldSize;
            i = 31 * i + goldCount;
            i = 31 * i + goldMinHeight;
            i = 31 * i + goldMaxHeight;
            i = 31 * i + redstoneSize;
            i = 31 * i + redstoneCount;
            i = 31 * i + redstoneMinHeight;
            i = 31 * i + redstoneMaxHeight;
            i = 31 * i + diamondSize;
            i = 31 * i + diamondCount;
            i = 31 * i + diamondMinHeight;
            i = 31 * i + diamondMaxHeight;
            i = 31 * i + lapisSize;
            i = 31 * i + lapisCount;
            i = 31 * i + lapisCenterHeight;
            i = 31 * i + lapisSpread;
            return i;
        }

        public ChunkProviderSettings func_177864_b()
        {
            return new ChunkProviderSettings(this);
        }
    }

    public static class Serializer implements JsonDeserializer<ChunkProviderSettings.Factory>, JsonSerializer<ChunkProviderSettings.Factory>
    {
        public ChunkProviderSettings.Factory deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException
        {
            JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
            ChunkProviderSettings.Factory chunkprovidersettings$factory = new ChunkProviderSettings.Factory();

            try
            {
                chunkprovidersettings$factory.coordinateScale = JsonUtils.getFloat(jsonobject, "coordinateScale", chunkprovidersettings$factory.coordinateScale);
                chunkprovidersettings$factory.heightScale = JsonUtils.getFloat(jsonobject, "heightScale", chunkprovidersettings$factory.heightScale);
                chunkprovidersettings$factory.lowerLimitScale = JsonUtils.getFloat(jsonobject, "lowerLimitScale", chunkprovidersettings$factory.lowerLimitScale);
                chunkprovidersettings$factory.upperLimitScale = JsonUtils.getFloat(jsonobject, "upperLimitScale", chunkprovidersettings$factory.upperLimitScale);
                chunkprovidersettings$factory.depthNoiseScaleX = JsonUtils.getFloat(jsonobject, "depthNoiseScaleX", chunkprovidersettings$factory.depthNoiseScaleX);
                chunkprovidersettings$factory.depthNoiseScaleZ = JsonUtils.getFloat(jsonobject, "depthNoiseScaleZ", chunkprovidersettings$factory.depthNoiseScaleZ);
                chunkprovidersettings$factory.depthNoiseScaleExponent = JsonUtils.getFloat(jsonobject, "depthNoiseScaleExponent", chunkprovidersettings$factory.depthNoiseScaleExponent);
                chunkprovidersettings$factory.mainNoiseScaleX = JsonUtils.getFloat(jsonobject, "mainNoiseScaleX", chunkprovidersettings$factory.mainNoiseScaleX);
                chunkprovidersettings$factory.mainNoiseScaleY = JsonUtils.getFloat(jsonobject, "mainNoiseScaleY", chunkprovidersettings$factory.mainNoiseScaleY);
                chunkprovidersettings$factory.mainNoiseScaleZ = JsonUtils.getFloat(jsonobject, "mainNoiseScaleZ", chunkprovidersettings$factory.mainNoiseScaleZ);
                chunkprovidersettings$factory.baseSize = JsonUtils.getFloat(jsonobject, "baseSize", chunkprovidersettings$factory.baseSize);
                chunkprovidersettings$factory.stretchY = JsonUtils.getFloat(jsonobject, "stretchY", chunkprovidersettings$factory.stretchY);
                chunkprovidersettings$factory.biomeDepthWeight = JsonUtils.getFloat(jsonobject, "biomeDepthWeight", chunkprovidersettings$factory.biomeDepthWeight);
                chunkprovidersettings$factory.biomeDepthOffset = JsonUtils.getFloat(jsonobject, "biomeDepthOffset", chunkprovidersettings$factory.biomeDepthOffset);
                chunkprovidersettings$factory.biomeScaleWeight = JsonUtils.getFloat(jsonobject, "biomeScaleWeight", chunkprovidersettings$factory.biomeScaleWeight);
                chunkprovidersettings$factory.biomeScaleOffset = JsonUtils.getFloat(jsonobject, "biomeScaleOffset", chunkprovidersettings$factory.biomeScaleOffset);
                chunkprovidersettings$factory.seaLevel = JsonUtils.getInt(jsonobject, "seaLevel", chunkprovidersettings$factory.seaLevel);
                chunkprovidersettings$factory.useCaves = JsonUtils.getBoolean(jsonobject, "useCaves", chunkprovidersettings$factory.useCaves);
                chunkprovidersettings$factory.useDungeons = JsonUtils.getBoolean(jsonobject, "useDungeons", chunkprovidersettings$factory.useDungeons);
                chunkprovidersettings$factory.dungeonChance = JsonUtils.getInt(jsonobject, "dungeonChance", chunkprovidersettings$factory.dungeonChance);
                chunkprovidersettings$factory.useStrongholds = JsonUtils.getBoolean(jsonobject, "useStrongholds", chunkprovidersettings$factory.useStrongholds);
                chunkprovidersettings$factory.useVillages = JsonUtils.getBoolean(jsonobject, "useVillages", chunkprovidersettings$factory.useVillages);
                chunkprovidersettings$factory.useMineShafts = JsonUtils.getBoolean(jsonobject, "useMineShafts", chunkprovidersettings$factory.useMineShafts);
                chunkprovidersettings$factory.useTemples = JsonUtils.getBoolean(jsonobject, "useTemples", chunkprovidersettings$factory.useTemples);
                chunkprovidersettings$factory.useMonuments = JsonUtils.getBoolean(jsonobject, "useMonuments", chunkprovidersettings$factory.useMonuments);
                chunkprovidersettings$factory.useRavines = JsonUtils.getBoolean(jsonobject, "useRavines", chunkprovidersettings$factory.useRavines);
                chunkprovidersettings$factory.useWaterLakes = JsonUtils.getBoolean(jsonobject, "useWaterLakes", chunkprovidersettings$factory.useWaterLakes);
                chunkprovidersettings$factory.waterLakeChance = JsonUtils.getInt(jsonobject, "waterLakeChance", chunkprovidersettings$factory.waterLakeChance);
                chunkprovidersettings$factory.useLavaLakes = JsonUtils.getBoolean(jsonobject, "useLavaLakes", chunkprovidersettings$factory.useLavaLakes);
                chunkprovidersettings$factory.lavaLakeChance = JsonUtils.getInt(jsonobject, "lavaLakeChance", chunkprovidersettings$factory.lavaLakeChance);
                chunkprovidersettings$factory.useLavaOceans = JsonUtils.getBoolean(jsonobject, "useLavaOceans", chunkprovidersettings$factory.useLavaOceans);
                chunkprovidersettings$factory.fixedBiome = JsonUtils.getInt(jsonobject, "fixedBiome", chunkprovidersettings$factory.fixedBiome);

                if (chunkprovidersettings$factory.fixedBiome < 38 && chunkprovidersettings$factory.fixedBiome >= -1)
                {
                    if (chunkprovidersettings$factory.fixedBiome >= BiomeGenBase.hell.biomeID)
                    {
                        chunkprovidersettings$factory.fixedBiome += 2;
                    }
                }
                else
                {
                    chunkprovidersettings$factory.fixedBiome = -1;
                }

                chunkprovidersettings$factory.biomeSize = JsonUtils.getInt(jsonobject, "biomeSize", chunkprovidersettings$factory.biomeSize);
                chunkprovidersettings$factory.riverSize = JsonUtils.getInt(jsonobject, "riverSize", chunkprovidersettings$factory.riverSize);
                chunkprovidersettings$factory.dirtSize = JsonUtils.getInt(jsonobject, "dirtSize", chunkprovidersettings$factory.dirtSize);
                chunkprovidersettings$factory.dirtCount = JsonUtils.getInt(jsonobject, "dirtCount", chunkprovidersettings$factory.dirtCount);
                chunkprovidersettings$factory.dirtMinHeight = JsonUtils.getInt(jsonobject, "dirtMinHeight", chunkprovidersettings$factory.dirtMinHeight);
                chunkprovidersettings$factory.dirtMaxHeight = JsonUtils.getInt(jsonobject, "dirtMaxHeight", chunkprovidersettings$factory.dirtMaxHeight);
                chunkprovidersettings$factory.gravelSize = JsonUtils.getInt(jsonobject, "gravelSize", chunkprovidersettings$factory.gravelSize);
                chunkprovidersettings$factory.gravelCount = JsonUtils.getInt(jsonobject, "gravelCount", chunkprovidersettings$factory.gravelCount);
                chunkprovidersettings$factory.gravelMinHeight = JsonUtils.getInt(jsonobject, "gravelMinHeight", chunkprovidersettings$factory.gravelMinHeight);
                chunkprovidersettings$factory.gravelMaxHeight = JsonUtils.getInt(jsonobject, "gravelMaxHeight", chunkprovidersettings$factory.gravelMaxHeight);
                chunkprovidersettings$factory.graniteSize = JsonUtils.getInt(jsonobject, "graniteSize", chunkprovidersettings$factory.graniteSize);
                chunkprovidersettings$factory.graniteCount = JsonUtils.getInt(jsonobject, "graniteCount", chunkprovidersettings$factory.graniteCount);
                chunkprovidersettings$factory.graniteMinHeight = JsonUtils.getInt(jsonobject, "graniteMinHeight", chunkprovidersettings$factory.graniteMinHeight);
                chunkprovidersettings$factory.graniteMaxHeight = JsonUtils.getInt(jsonobject, "graniteMaxHeight", chunkprovidersettings$factory.graniteMaxHeight);
                chunkprovidersettings$factory.dioriteSize = JsonUtils.getInt(jsonobject, "dioriteSize", chunkprovidersettings$factory.dioriteSize);
                chunkprovidersettings$factory.dioriteCount = JsonUtils.getInt(jsonobject, "dioriteCount", chunkprovidersettings$factory.dioriteCount);
                chunkprovidersettings$factory.dioriteMinHeight = JsonUtils.getInt(jsonobject, "dioriteMinHeight", chunkprovidersettings$factory.dioriteMinHeight);
                chunkprovidersettings$factory.dioriteMaxHeight = JsonUtils.getInt(jsonobject, "dioriteMaxHeight", chunkprovidersettings$factory.dioriteMaxHeight);
                chunkprovidersettings$factory.andesiteSize = JsonUtils.getInt(jsonobject, "andesiteSize", chunkprovidersettings$factory.andesiteSize);
                chunkprovidersettings$factory.andesiteCount = JsonUtils.getInt(jsonobject, "andesiteCount", chunkprovidersettings$factory.andesiteCount);
                chunkprovidersettings$factory.andesiteMinHeight = JsonUtils.getInt(jsonobject, "andesiteMinHeight", chunkprovidersettings$factory.andesiteMinHeight);
                chunkprovidersettings$factory.andesiteMaxHeight = JsonUtils.getInt(jsonobject, "andesiteMaxHeight", chunkprovidersettings$factory.andesiteMaxHeight);
                chunkprovidersettings$factory.coalSize = JsonUtils.getInt(jsonobject, "coalSize", chunkprovidersettings$factory.coalSize);
                chunkprovidersettings$factory.coalCount = JsonUtils.getInt(jsonobject, "coalCount", chunkprovidersettings$factory.coalCount);
                chunkprovidersettings$factory.coalMinHeight = JsonUtils.getInt(jsonobject, "coalMinHeight", chunkprovidersettings$factory.coalMinHeight);
                chunkprovidersettings$factory.coalMaxHeight = JsonUtils.getInt(jsonobject, "coalMaxHeight", chunkprovidersettings$factory.coalMaxHeight);
                chunkprovidersettings$factory.ironSize = JsonUtils.getInt(jsonobject, "ironSize", chunkprovidersettings$factory.ironSize);
                chunkprovidersettings$factory.ironCount = JsonUtils.getInt(jsonobject, "ironCount", chunkprovidersettings$factory.ironCount);
                chunkprovidersettings$factory.ironMinHeight = JsonUtils.getInt(jsonobject, "ironMinHeight", chunkprovidersettings$factory.ironMinHeight);
                chunkprovidersettings$factory.ironMaxHeight = JsonUtils.getInt(jsonobject, "ironMaxHeight", chunkprovidersettings$factory.ironMaxHeight);
                chunkprovidersettings$factory.goldSize = JsonUtils.getInt(jsonobject, "goldSize", chunkprovidersettings$factory.goldSize);
                chunkprovidersettings$factory.goldCount = JsonUtils.getInt(jsonobject, "goldCount", chunkprovidersettings$factory.goldCount);
                chunkprovidersettings$factory.goldMinHeight = JsonUtils.getInt(jsonobject, "goldMinHeight", chunkprovidersettings$factory.goldMinHeight);
                chunkprovidersettings$factory.goldMaxHeight = JsonUtils.getInt(jsonobject, "goldMaxHeight", chunkprovidersettings$factory.goldMaxHeight);
                chunkprovidersettings$factory.redstoneSize = JsonUtils.getInt(jsonobject, "redstoneSize", chunkprovidersettings$factory.redstoneSize);
                chunkprovidersettings$factory.redstoneCount = JsonUtils.getInt(jsonobject, "redstoneCount", chunkprovidersettings$factory.redstoneCount);
                chunkprovidersettings$factory.redstoneMinHeight = JsonUtils.getInt(jsonobject, "redstoneMinHeight", chunkprovidersettings$factory.redstoneMinHeight);
                chunkprovidersettings$factory.redstoneMaxHeight = JsonUtils.getInt(jsonobject, "redstoneMaxHeight", chunkprovidersettings$factory.redstoneMaxHeight);
                chunkprovidersettings$factory.diamondSize = JsonUtils.getInt(jsonobject, "diamondSize", chunkprovidersettings$factory.diamondSize);
                chunkprovidersettings$factory.diamondCount = JsonUtils.getInt(jsonobject, "diamondCount", chunkprovidersettings$factory.diamondCount);
                chunkprovidersettings$factory.diamondMinHeight = JsonUtils.getInt(jsonobject, "diamondMinHeight", chunkprovidersettings$factory.diamondMinHeight);
                chunkprovidersettings$factory.diamondMaxHeight = JsonUtils.getInt(jsonobject, "diamondMaxHeight", chunkprovidersettings$factory.diamondMaxHeight);
                chunkprovidersettings$factory.lapisSize = JsonUtils.getInt(jsonobject, "lapisSize", chunkprovidersettings$factory.lapisSize);
                chunkprovidersettings$factory.lapisCount = JsonUtils.getInt(jsonobject, "lapisCount", chunkprovidersettings$factory.lapisCount);
                chunkprovidersettings$factory.lapisCenterHeight = JsonUtils.getInt(jsonobject, "lapisCenterHeight", chunkprovidersettings$factory.lapisCenterHeight);
                chunkprovidersettings$factory.lapisSpread = JsonUtils.getInt(jsonobject, "lapisSpread", chunkprovidersettings$factory.lapisSpread);
            }
            catch (Exception var7)
            {
            }

            return chunkprovidersettings$factory;
        }

        public JsonElement serialize(ChunkProviderSettings.Factory p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_)
        {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("coordinateScale", Float.valueOf(p_serialize_1_.coordinateScale));
            jsonobject.addProperty("heightScale", Float.valueOf(p_serialize_1_.heightScale));
            jsonobject.addProperty("lowerLimitScale", Float.valueOf(p_serialize_1_.lowerLimitScale));
            jsonobject.addProperty("upperLimitScale", Float.valueOf(p_serialize_1_.upperLimitScale));
            jsonobject.addProperty("depthNoiseScaleX", Float.valueOf(p_serialize_1_.depthNoiseScaleX));
            jsonobject.addProperty("depthNoiseScaleZ", Float.valueOf(p_serialize_1_.depthNoiseScaleZ));
            jsonobject.addProperty("depthNoiseScaleExponent", Float.valueOf(p_serialize_1_.depthNoiseScaleExponent));
            jsonobject.addProperty("mainNoiseScaleX", Float.valueOf(p_serialize_1_.mainNoiseScaleX));
            jsonobject.addProperty("mainNoiseScaleY", Float.valueOf(p_serialize_1_.mainNoiseScaleY));
            jsonobject.addProperty("mainNoiseScaleZ", Float.valueOf(p_serialize_1_.mainNoiseScaleZ));
            jsonobject.addProperty("baseSize", Float.valueOf(p_serialize_1_.baseSize));
            jsonobject.addProperty("stretchY", Float.valueOf(p_serialize_1_.stretchY));
            jsonobject.addProperty("biomeDepthWeight", Float.valueOf(p_serialize_1_.biomeDepthWeight));
            jsonobject.addProperty("biomeDepthOffset", Float.valueOf(p_serialize_1_.biomeDepthOffset));
            jsonobject.addProperty("biomeScaleWeight", Float.valueOf(p_serialize_1_.biomeScaleWeight));
            jsonobject.addProperty("biomeScaleOffset", Float.valueOf(p_serialize_1_.biomeScaleOffset));
            jsonobject.addProperty("seaLevel", Integer.valueOf(p_serialize_1_.seaLevel));
            jsonobject.addProperty("useCaves", Boolean.valueOf(p_serialize_1_.useCaves));
            jsonobject.addProperty("useDungeons", Boolean.valueOf(p_serialize_1_.useDungeons));
            jsonobject.addProperty("dungeonChance", Integer.valueOf(p_serialize_1_.dungeonChance));
            jsonobject.addProperty("useStrongholds", Boolean.valueOf(p_serialize_1_.useStrongholds));
            jsonobject.addProperty("useVillages", Boolean.valueOf(p_serialize_1_.useVillages));
            jsonobject.addProperty("useMineShafts", Boolean.valueOf(p_serialize_1_.useMineShafts));
            jsonobject.addProperty("useTemples", Boolean.valueOf(p_serialize_1_.useTemples));
            jsonobject.addProperty("useMonuments", Boolean.valueOf(p_serialize_1_.useMonuments));
            jsonobject.addProperty("useRavines", Boolean.valueOf(p_serialize_1_.useRavines));
            jsonobject.addProperty("useWaterLakes", Boolean.valueOf(p_serialize_1_.useWaterLakes));
            jsonobject.addProperty("waterLakeChance", Integer.valueOf(p_serialize_1_.waterLakeChance));
            jsonobject.addProperty("useLavaLakes", Boolean.valueOf(p_serialize_1_.useLavaLakes));
            jsonobject.addProperty("lavaLakeChance", Integer.valueOf(p_serialize_1_.lavaLakeChance));
            jsonobject.addProperty("useLavaOceans", Boolean.valueOf(p_serialize_1_.useLavaOceans));
            jsonobject.addProperty("fixedBiome", Integer.valueOf(p_serialize_1_.fixedBiome));
            jsonobject.addProperty("biomeSize", Integer.valueOf(p_serialize_1_.biomeSize));
            jsonobject.addProperty("riverSize", Integer.valueOf(p_serialize_1_.riverSize));
            jsonobject.addProperty("dirtSize", Integer.valueOf(p_serialize_1_.dirtSize));
            jsonobject.addProperty("dirtCount", Integer.valueOf(p_serialize_1_.dirtCount));
            jsonobject.addProperty("dirtMinHeight", Integer.valueOf(p_serialize_1_.dirtMinHeight));
            jsonobject.addProperty("dirtMaxHeight", Integer.valueOf(p_serialize_1_.dirtMaxHeight));
            jsonobject.addProperty("gravelSize", Integer.valueOf(p_serialize_1_.gravelSize));
            jsonobject.addProperty("gravelCount", Integer.valueOf(p_serialize_1_.gravelCount));
            jsonobject.addProperty("gravelMinHeight", Integer.valueOf(p_serialize_1_.gravelMinHeight));
            jsonobject.addProperty("gravelMaxHeight", Integer.valueOf(p_serialize_1_.gravelMaxHeight));
            jsonobject.addProperty("graniteSize", Integer.valueOf(p_serialize_1_.graniteSize));
            jsonobject.addProperty("graniteCount", Integer.valueOf(p_serialize_1_.graniteCount));
            jsonobject.addProperty("graniteMinHeight", Integer.valueOf(p_serialize_1_.graniteMinHeight));
            jsonobject.addProperty("graniteMaxHeight", Integer.valueOf(p_serialize_1_.graniteMaxHeight));
            jsonobject.addProperty("dioriteSize", Integer.valueOf(p_serialize_1_.dioriteSize));
            jsonobject.addProperty("dioriteCount", Integer.valueOf(p_serialize_1_.dioriteCount));
            jsonobject.addProperty("dioriteMinHeight", Integer.valueOf(p_serialize_1_.dioriteMinHeight));
            jsonobject.addProperty("dioriteMaxHeight", Integer.valueOf(p_serialize_1_.dioriteMaxHeight));
            jsonobject.addProperty("andesiteSize", Integer.valueOf(p_serialize_1_.andesiteSize));
            jsonobject.addProperty("andesiteCount", Integer.valueOf(p_serialize_1_.andesiteCount));
            jsonobject.addProperty("andesiteMinHeight", Integer.valueOf(p_serialize_1_.andesiteMinHeight));
            jsonobject.addProperty("andesiteMaxHeight", Integer.valueOf(p_serialize_1_.andesiteMaxHeight));
            jsonobject.addProperty("coalSize", Integer.valueOf(p_serialize_1_.coalSize));
            jsonobject.addProperty("coalCount", Integer.valueOf(p_serialize_1_.coalCount));
            jsonobject.addProperty("coalMinHeight", Integer.valueOf(p_serialize_1_.coalMinHeight));
            jsonobject.addProperty("coalMaxHeight", Integer.valueOf(p_serialize_1_.coalMaxHeight));
            jsonobject.addProperty("ironSize", Integer.valueOf(p_serialize_1_.ironSize));
            jsonobject.addProperty("ironCount", Integer.valueOf(p_serialize_1_.ironCount));
            jsonobject.addProperty("ironMinHeight", Integer.valueOf(p_serialize_1_.ironMinHeight));
            jsonobject.addProperty("ironMaxHeight", Integer.valueOf(p_serialize_1_.ironMaxHeight));
            jsonobject.addProperty("goldSize", Integer.valueOf(p_serialize_1_.goldSize));
            jsonobject.addProperty("goldCount", Integer.valueOf(p_serialize_1_.goldCount));
            jsonobject.addProperty("goldMinHeight", Integer.valueOf(p_serialize_1_.goldMinHeight));
            jsonobject.addProperty("goldMaxHeight", Integer.valueOf(p_serialize_1_.goldMaxHeight));
            jsonobject.addProperty("redstoneSize", Integer.valueOf(p_serialize_1_.redstoneSize));
            jsonobject.addProperty("redstoneCount", Integer.valueOf(p_serialize_1_.redstoneCount));
            jsonobject.addProperty("redstoneMinHeight", Integer.valueOf(p_serialize_1_.redstoneMinHeight));
            jsonobject.addProperty("redstoneMaxHeight", Integer.valueOf(p_serialize_1_.redstoneMaxHeight));
            jsonobject.addProperty("diamondSize", Integer.valueOf(p_serialize_1_.diamondSize));
            jsonobject.addProperty("diamondCount", Integer.valueOf(p_serialize_1_.diamondCount));
            jsonobject.addProperty("diamondMinHeight", Integer.valueOf(p_serialize_1_.diamondMinHeight));
            jsonobject.addProperty("diamondMaxHeight", Integer.valueOf(p_serialize_1_.diamondMaxHeight));
            jsonobject.addProperty("lapisSize", Integer.valueOf(p_serialize_1_.lapisSize));
            jsonobject.addProperty("lapisCount", Integer.valueOf(p_serialize_1_.lapisCount));
            jsonobject.addProperty("lapisCenterHeight", Integer.valueOf(p_serialize_1_.lapisCenterHeight));
            jsonobject.addProperty("lapisSpread", Integer.valueOf(p_serialize_1_.lapisSpread));
            return jsonobject;
        }
    }
}
