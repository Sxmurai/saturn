package net.minecraft.client.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

public class SimpleResource implements IResource
{
    private final Map<String, IMetadataSection> mapMetadataSections = Maps.newHashMap();
    private final String resourcePackName;
    private final ResourceLocation srResourceLocation;
    private final InputStream resourceInputStream;
    private final InputStream mcmetaInputStream;
    private final IMetadataSerializer srMetadataSerializer;
    private boolean mcmetaJsonChecked;
    private JsonObject mcmetaJson;

    public SimpleResource(String resourcePackNameIn, ResourceLocation srResourceLocationIn, InputStream resourceInputStreamIn, InputStream mcmetaInputStreamIn, IMetadataSerializer srMetadataSerializerIn)
    {
        resourcePackName = resourcePackNameIn;
        srResourceLocation = srResourceLocationIn;
        resourceInputStream = resourceInputStreamIn;
        mcmetaInputStream = mcmetaInputStreamIn;
        srMetadataSerializer = srMetadataSerializerIn;
    }

    public ResourceLocation getResourceLocation()
    {
        return srResourceLocation;
    }

    public InputStream getInputStream()
    {
        return resourceInputStream;
    }

    public boolean hasMetadata()
    {
        return mcmetaInputStream != null;
    }

    public <T extends IMetadataSection> T getMetadata(String p_110526_1_)
    {
        if (!hasMetadata())
        {
            return null;
        }
        else
        {
            if (mcmetaJson == null && !mcmetaJsonChecked)
            {
                mcmetaJsonChecked = true;
                BufferedReader bufferedreader = null;

                try
                {
                    bufferedreader = new BufferedReader(new InputStreamReader(mcmetaInputStream));
                    mcmetaJson = (new JsonParser()).parse(bufferedreader).getAsJsonObject();
                }
                finally
                {
                    IOUtils.closeQuietly(bufferedreader);
                }
            }

            T t = (T) mapMetadataSections.get(p_110526_1_);

            if (t == null)
            {
                t = srMetadataSerializer.parseMetadataSection(p_110526_1_, mcmetaJson);
            }

            return t;
        }
    }

    public String getResourcePackName()
    {
        return resourcePackName;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof SimpleResource))
        {
            return false;
        }
        else
        {
            SimpleResource simpleresource = (SimpleResource)p_equals_1_;

            if (srResourceLocation != null)
            {
                if (!srResourceLocation.equals(simpleresource.srResourceLocation))
                {
                    return false;
                }
            }
            else if (simpleresource.srResourceLocation != null)
            {
                return false;
            }

            if (resourcePackName != null)
            {
                return resourcePackName.equals(simpleresource.resourcePackName);
            }
            else return simpleresource.resourcePackName == null;
        }
    }

    public int hashCode()
    {
        int i = resourcePackName != null ? resourcePackName.hashCode() : 0;
        i = 31 * i + (srResourceLocation != null ? srResourceLocation.hashCode() : 0);
        return i;
    }
}
