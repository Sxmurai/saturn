package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import optifine.Config;
import optifine.RandomMobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadersmod.client.ShadersTex;

public class TextureManager implements ITickable, IResourceManagerReloadListener
{
    private static final Logger logger = LogManager.getLogger();
    private final Map mapTextureObjects = Maps.newHashMap();
    private final List listTickables = Lists.newArrayList();
    private final Map mapTextureCounters = Maps.newHashMap();
    private final IResourceManager theResourceManager;
    private static final String __OBFID = "CL_00001064";

    public TextureManager(IResourceManager resourceManager)
    {
        theResourceManager = resourceManager;
    }

    public void bindTexture(ResourceLocation resource)
    {
        if (Config.isRandomMobs())
        {
            resource = RandomMobs.getTextureLocation(resource);
        }

        Object object = mapTextureObjects.get(resource);

        if (object == null)
        {
            object = new SimpleTexture(resource);
            loadTexture(resource, (ITextureObject)object);
        }

        if (Config.isShaders())
        {
            ShadersTex.bindTexture((ITextureObject)object);
        }
        else
        {
            TextureUtil.bindTexture(((ITextureObject)object).getGlTextureId());
        }
    }

    public boolean loadTickableTexture(ResourceLocation textureLocation, ITickableTextureObject textureObj)
    {
        if (loadTexture(textureLocation, textureObj))
        {
            listTickables.add(textureObj);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean loadTexture(ResourceLocation textureLocation, ITextureObject textureObj)
    {
        boolean flag = true;
        ITextureObject itextureobject = textureObj;

        try
        {
            textureObj.loadTexture(theResourceManager);
        }
        catch (IOException ioexception)
        {
            TextureManager.logger.warn("Failed to load texture: " + textureLocation, ioexception);
            itextureobject = TextureUtil.missingTexture;
            mapTextureObjects.put(textureLocation, itextureobject);
            flag = false;
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Registering texture");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Resource location being registered");
            crashreportcategory.addCrashSection("Resource location", textureLocation);
            crashreportcategory.addCrashSectionCallable("Texture object class", new Callable()
            {
                private static final String __OBFID = "CL_00001065";
                public String call() throws Exception
                {
                    return textureObj.getClass().getName();
                }
            });
            throw new ReportedException(crashreport);
        }

        mapTextureObjects.put(textureLocation, itextureobject);
        return flag;
    }

    public ITextureObject getTexture(ResourceLocation textureLocation)
    {
        return (ITextureObject) mapTextureObjects.get(textureLocation);
    }

    public ResourceLocation getDynamicTextureLocation(String name, DynamicTexture texture)
    {
        if (name.equals("logo"))
        {
            texture = Config.getMojangLogoTexture(texture);
        }

        Integer integer = (Integer) mapTextureCounters.get(name);

        if (integer == null)
        {
            integer = Integer.valueOf(1);
        }
        else
        {
            integer = Integer.valueOf(integer.intValue() + 1);
        }

        mapTextureCounters.put(name, integer);
        ResourceLocation resourcelocation = new ResourceLocation(String.format("dynamic/%s_%d", name, integer));
        loadTexture(resourcelocation, texture);
        return resourcelocation;
    }

    public void tick()
    {
        for (Object itickable : listTickables)
        {
            ((ITickable) itickable).tick();
        }
    }

    public void deleteTexture(ResourceLocation textureLocation)
    {
        ITextureObject itextureobject = getTexture(textureLocation);

        if (itextureobject != null)
        {
            mapTextureObjects.remove(textureLocation);
            TextureUtil.deleteTexture(itextureobject.getGlTextureId());
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        Config.dbg("*** Reloading textures ***");
        Config.log("Resource packs: " + Config.getResourcePackNames());
        Iterator iterator = mapTextureObjects.keySet().iterator();

        while (iterator.hasNext())
        {
            ResourceLocation resourcelocation = (ResourceLocation)iterator.next();
            String s = resourcelocation.getResourcePath();

            if (s.startsWith("mcpatcher/") || s.startsWith("optifine/"))
            {
                ITextureObject itextureobject = (ITextureObject) mapTextureObjects.get(resourcelocation);

                if (itextureobject instanceof AbstractTexture)
                {
                    AbstractTexture abstracttexture = (AbstractTexture)itextureobject;
                    abstracttexture.deleteGlTexture();
                }

                iterator.remove();
            }
        }

        for (Object entry : mapTextureObjects.entrySet())
        {
            loadTexture((ResourceLocation)((Map.Entry) entry).getKey(), (ITextureObject)((Map.Entry) entry).getValue());
        }
    }

    public void reloadBannerTextures()
    {
        for (Object entry : this.mapTextureObjects.entrySet())
        {
            ResourceLocation resourcelocation = (ResourceLocation)((Map.Entry) entry).getKey();
            ITextureObject itextureobject = (ITextureObject)((Map.Entry) entry).getValue();

            if (itextureobject instanceof LayeredColorMaskTexture)
            {
                loadTexture(resourcelocation, itextureobject);
            }
        }
    }
}
