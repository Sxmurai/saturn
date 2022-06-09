package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FallbackResourceManager implements IResourceManager
{
    private static final Logger logger = LogManager.getLogger();
    protected final List<IResourcePack> resourcePacks = Lists.newArrayList();
    private final IMetadataSerializer frmMetadataSerializer;

    public FallbackResourceManager(IMetadataSerializer frmMetadataSerializerIn)
    {
        frmMetadataSerializer = frmMetadataSerializerIn;
    }

    public void addResourcePack(IResourcePack resourcePack)
    {
        resourcePacks.add(resourcePack);
    }

    public Set<String> getResourceDomains()
    {
        return null;
    }

    public IResource getResource(ResourceLocation location) throws IOException
    {
        IResourcePack iresourcepack = null;
        ResourceLocation resourcelocation = FallbackResourceManager.getLocationMcmeta(location);

        for (int i = resourcePacks.size() - 1; i >= 0; --i)
        {
            IResourcePack iresourcepack1 = resourcePacks.get(i);

            if (iresourcepack == null && iresourcepack1.resourceExists(resourcelocation))
            {
                iresourcepack = iresourcepack1;
            }

            if (iresourcepack1.resourceExists(location))
            {
                InputStream inputstream = null;

                if (iresourcepack != null)
                {
                    inputstream = getInputStream(resourcelocation, iresourcepack);
                }

                return new SimpleResource(iresourcepack1.getPackName(), location, getInputStream(location, iresourcepack1), inputstream, frmMetadataSerializer);
            }
        }

        throw new FileNotFoundException(location.toString());
    }

    protected InputStream getInputStream(ResourceLocation location, IResourcePack resourcePack) throws IOException
    {
        InputStream inputstream = resourcePack.getInputStream(location);
        return FallbackResourceManager.logger.isDebugEnabled() ? new InputStreamLeakedResourceLogger(inputstream, location, resourcePack.getPackName()) : inputstream;
    }

    public List<IResource> getAllResources(ResourceLocation location) throws IOException
    {
        List<IResource> list = Lists.newArrayList();
        ResourceLocation resourcelocation = FallbackResourceManager.getLocationMcmeta(location);

        for (IResourcePack iresourcepack : resourcePacks)
        {
            if (iresourcepack.resourceExists(location))
            {
                InputStream inputstream = iresourcepack.resourceExists(resourcelocation) ? getInputStream(resourcelocation, iresourcepack) : null;
                list.add(new SimpleResource(iresourcepack.getPackName(), location, getInputStream(location, iresourcepack), inputstream, frmMetadataSerializer));
            }
        }

        if (list.isEmpty())
        {
            throw new FileNotFoundException(location.toString());
        }
        else
        {
            return list;
        }
    }

    static ResourceLocation getLocationMcmeta(ResourceLocation location)
    {
        return new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".mcmeta");
    }

    static class InputStreamLeakedResourceLogger extends InputStream
    {
        private final InputStream field_177330_a;
        private final String field_177328_b;
        private boolean field_177329_c = false;

        public InputStreamLeakedResourceLogger(InputStream p_i46093_1_, ResourceLocation location, String p_i46093_3_)
        {
            field_177330_a = p_i46093_1_;
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            (new Exception()).printStackTrace(new PrintStream(bytearrayoutputstream));
            field_177328_b = "Leaked resource: '" + location + "' loaded from pack: '" + p_i46093_3_ + "'\n" + bytearrayoutputstream;
        }

        public void close() throws IOException
        {
            field_177330_a.close();
            field_177329_c = true;
        }

        protected void finalize() throws Throwable
        {
            if (!field_177329_c)
            {
                FallbackResourceManager.logger.warn(field_177328_b);
            }

            super.finalize();
        }

        public int read() throws IOException
        {
            return field_177330_a.read();
        }
    }
}
