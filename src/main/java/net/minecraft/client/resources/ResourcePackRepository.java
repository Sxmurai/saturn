package net.minecraft.client.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourcePackRepository
{
    private static final Logger logger = LogManager.getLogger();
    private static final FileFilter resourcePackFilter = new FileFilter()
    {
        public boolean accept(File p_accept_1_)
        {
            boolean flag = p_accept_1_.isFile() && p_accept_1_.getName().endsWith(".zip");
            boolean flag1 = p_accept_1_.isDirectory() && (new File(p_accept_1_, "pack.mcmeta")).isFile();
            return flag || flag1;
        }
    };
    private final File dirResourcepacks;
    public final IResourcePack rprDefaultResourcePack;
    private final File dirServerResourcepacks;
    public final IMetadataSerializer rprMetadataSerializer;
    private IResourcePack resourcePackInstance;
    private final ReentrantLock lock = new ReentrantLock();
    private ListenableFuture<Object> field_177322_i;
    private List<ResourcePackRepository.Entry> repositoryEntriesAll = Lists.newArrayList();
    private final List<ResourcePackRepository.Entry> repositoryEntries = Lists.newArrayList();

    public ResourcePackRepository(File dirResourcepacksIn, File dirServerResourcepacksIn, IResourcePack rprDefaultResourcePackIn, IMetadataSerializer rprMetadataSerializerIn, GameSettings settings)
    {
        dirResourcepacks = dirResourcepacksIn;
        dirServerResourcepacks = dirServerResourcepacksIn;
        rprDefaultResourcePack = rprDefaultResourcePackIn;
        rprMetadataSerializer = rprMetadataSerializerIn;
        fixDirResourcepacks();
        updateRepositoryEntriesAll();
        Iterator<String> iterator = settings.resourcePacks.iterator();

        while (iterator.hasNext())
        {
            String s = iterator.next();

            for (ResourcePackRepository.Entry resourcepackrepository$entry : repositoryEntriesAll)
            {
                if (resourcepackrepository$entry.getResourcePackName().equals(s))
                {
                    if (resourcepackrepository$entry.func_183027_f() == 1 || settings.field_183018_l.contains(resourcepackrepository$entry.getResourcePackName()))
                    {
                        repositoryEntries.add(resourcepackrepository$entry);
                        break;
                    }

                    iterator.remove();
                    ResourcePackRepository.logger.warn("Removed selected resource pack {} because it's no longer compatible", new Object[] {resourcepackrepository$entry.getResourcePackName()});
                }
            }
        }
    }

    private void fixDirResourcepacks()
    {
        if (dirResourcepacks.exists())
        {
            if (!dirResourcepacks.isDirectory() && (!dirResourcepacks.delete() || !dirResourcepacks.mkdirs()))
            {
                ResourcePackRepository.logger.warn("Unable to recreate resourcepack folder, it exists but is not a directory: " + dirResourcepacks);
            }
        }
        else if (!dirResourcepacks.mkdirs())
        {
            ResourcePackRepository.logger.warn("Unable to create resourcepack folder: " + dirResourcepacks);
        }
    }

    private List<File> getResourcePackFiles()
    {
        return dirResourcepacks.isDirectory() ? Arrays.asList(dirResourcepacks.listFiles(ResourcePackRepository.resourcePackFilter)) : Collections.emptyList();
    }

    public void updateRepositoryEntriesAll()
    {
        List<ResourcePackRepository.Entry> list = Lists.newArrayList();

        for (File file1 : getResourcePackFiles())
        {
            ResourcePackRepository.Entry resourcepackrepository$entry = new ResourcePackRepository.Entry(file1);

            if (!repositoryEntriesAll.contains(resourcepackrepository$entry))
            {
                try
                {
                    resourcepackrepository$entry.updateResourcePack();
                    list.add(resourcepackrepository$entry);
                }
                catch (Exception var6)
                {
                    list.remove(resourcepackrepository$entry);
                }
            }
            else
            {
                int i = repositoryEntriesAll.indexOf(resourcepackrepository$entry);

                if (i > -1 && i < repositoryEntriesAll.size())
                {
                    list.add(repositoryEntriesAll.get(i));
                }
            }
        }

        repositoryEntriesAll.removeAll(list);

        for (ResourcePackRepository.Entry resourcepackrepository$entry1 : repositoryEntriesAll)
        {
            resourcepackrepository$entry1.closeResourcePack();
        }

        repositoryEntriesAll = list;
    }

    public List<ResourcePackRepository.Entry> getRepositoryEntriesAll()
    {
        return ImmutableList.copyOf(repositoryEntriesAll);
    }

    public List<ResourcePackRepository.Entry> getRepositoryEntries()
    {
        return ImmutableList.copyOf(repositoryEntries);
    }

    public void setRepositories(List<ResourcePackRepository.Entry> p_148527_1_)
    {
        repositoryEntries.clear();
        repositoryEntries.addAll(p_148527_1_);
    }

    public File getDirResourcepacks()
    {
        return dirResourcepacks;
    }

    public ListenableFuture<Object> downloadResourcePack(String url, String hash)
    {
        String s;

        if (hash.matches("^[a-f0-9]{40}$"))
        {
            s = hash;
        }
        else
        {
            s = "legacy";
        }

        final File file1 = new File(dirServerResourcepacks, s);
        lock.lock();

        try
        {
            func_148529_f();

            if (file1.exists() && hash.length() == 40)
            {
                try
                {
                    String s1 = Hashing.sha1().hashBytes(Files.toByteArray(file1)).toString();

                    if (s1.equals(hash))
                    {
                        ListenableFuture listenablefuture1 = setResourcePackInstance(file1);
                        return listenablefuture1;
                    }

                    ResourcePackRepository.logger.warn("File " + file1 + " had wrong hash (expected " + hash + ", found " + s1 + "). Deleting it.");
                    FileUtils.deleteQuietly(file1);
                }
                catch (IOException ioexception)
                {
                    ResourcePackRepository.logger.warn("File " + file1 + " couldn't be hashed. Deleting it.", ioexception);
                    FileUtils.deleteQuietly(file1);
                }
            }

            func_183028_i();
            final GuiScreenWorking guiscreenworking = new GuiScreenWorking();
            Map<String, String> map = Minecraft.getSessionInfo();
            final Minecraft minecraft = Minecraft.getMinecraft();
            Futures.getUnchecked(minecraft.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    minecraft.displayGuiScreen(guiscreenworking);
                }
            }));
            final SettableFuture<Object> settablefuture = SettableFuture.create();
            field_177322_i = HttpUtil.downloadResourcePack(file1, url, map, 52428800, guiscreenworking, minecraft.getProxy());
            Futures.addCallback(field_177322_i, new FutureCallback<Object>()
            {
                public void onSuccess(Object p_onSuccess_1_)
                {
                    setResourcePackInstance(file1);
                    settablefuture.set(null);
                }
                public void onFailure(Throwable p_onFailure_1_)
                {
                    settablefuture.setException(p_onFailure_1_);
                }
            });
            ListenableFuture listenablefuture = field_177322_i;
            return listenablefuture;
        }
        finally
        {
            lock.unlock();
        }
    }

    private void func_183028_i()
    {
        List<File> list = Lists.newArrayList(FileUtils.listFiles(dirServerResourcepacks, TrueFileFilter.TRUE, null));
        Collections.sort(list, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        int i = 0;

        for (File file1 : list)
        {
            if (i++ >= 10)
            {
                ResourcePackRepository.logger.info("Deleting old server resource pack " + file1.getName());
                FileUtils.deleteQuietly(file1);
            }
        }
    }

    public ListenableFuture<Object> setResourcePackInstance(File p_177319_1_)
    {
        resourcePackInstance = new FileResourcePack(p_177319_1_);
        return Minecraft.getMinecraft().scheduleResourcesRefresh();
    }

    /**
     * Getter for the IResourcePack instance associated with this ResourcePackRepository
     */
    public IResourcePack getResourcePackInstance()
    {
        return resourcePackInstance;
    }

    public void func_148529_f()
    {
        lock.lock();

        try
        {
            if (field_177322_i != null)
            {
                field_177322_i.cancel(true);
            }

            field_177322_i = null;

            if (resourcePackInstance != null)
            {
                resourcePackInstance = null;
                Minecraft.getMinecraft().scheduleResourcesRefresh();
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public class Entry
    {
        private final File resourcePackFile;
        private IResourcePack reResourcePack;
        private PackMetadataSection rePackMetadataSection;
        private BufferedImage texturePackIcon;
        private ResourceLocation locationTexturePackIcon;

        private Entry(File resourcePackFileIn)
        {
            resourcePackFile = resourcePackFileIn;
        }

        public void updateResourcePack() throws IOException
        {
            reResourcePack = resourcePackFile.isDirectory() ? new FolderResourcePack(resourcePackFile) : new FileResourcePack(resourcePackFile);
            rePackMetadataSection = reResourcePack.getPackMetadata(rprMetadataSerializer, "pack");

            try
            {
                texturePackIcon = reResourcePack.getPackImage();
            }
            catch (IOException var2)
            {
            }

            if (texturePackIcon == null)
            {
                texturePackIcon = rprDefaultResourcePack.getPackImage();
            }

            closeResourcePack();
        }

        public void bindTexturePackIcon(TextureManager textureManagerIn)
        {
            if (locationTexturePackIcon == null)
            {
                locationTexturePackIcon = textureManagerIn.getDynamicTextureLocation("texturepackicon", new DynamicTexture(texturePackIcon));
            }

            textureManagerIn.bindTexture(locationTexturePackIcon);
        }

        public void closeResourcePack()
        {
            if (reResourcePack instanceof Closeable)
            {
                IOUtils.closeQuietly((Closeable) reResourcePack);
            }
        }

        public IResourcePack getResourcePack()
        {
            return reResourcePack;
        }

        public String getResourcePackName()
        {
            return reResourcePack.getPackName();
        }

        public String getTexturePackDescription()
        {
            return rePackMetadataSection == null ? EnumChatFormatting.RED + "Invalid pack.mcmeta (or missing 'pack' section)" : rePackMetadataSection.getPackDescription().getFormattedText();
        }

        public int func_183027_f()
        {
            return rePackMetadataSection.getPackFormat();
        }

        public boolean equals(Object p_equals_1_)
        {
            return this == p_equals_1_ || (p_equals_1_ instanceof Entry && toString().equals(p_equals_1_.toString()));
        }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public String toString()
        {
            return String.format("%s:%s:%d", resourcePackFile.getName(), resourcePackFile.isDirectory() ? "folder" : "zip", Long.valueOf(resourcePackFile.lastModified()));
        }
    }
}
