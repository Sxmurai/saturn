package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoundHandler implements IResourceManagerReloadListener, ITickable
{
    private static final Logger logger = LogManager.getLogger();
    private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(SoundList.class, new SoundListSerializer()).create();
    private static final ParameterizedType TYPE = new ParameterizedType()
    {
        public Type[] getActualTypeArguments()
        {
            return new Type[] {String.class, SoundList.class};
        }
        public Type getRawType()
        {
            return Map.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };
    public static final SoundPoolEntry missing_sound = new SoundPoolEntry(new ResourceLocation("meta:missing_sound"), 0.0D, 0.0D, false);
    private final SoundRegistry sndRegistry = new SoundRegistry();
    private final SoundManager sndManager;
    private final IResourceManager mcResourceManager;

    public SoundHandler(IResourceManager manager, GameSettings gameSettingsIn)
    {
        mcResourceManager = manager;
        sndManager = new SoundManager(this, gameSettingsIn);
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        sndManager.reloadSoundSystem();
        sndRegistry.clearMap();

        for (String s : resourceManager.getResourceDomains())
        {
            try
            {
                for (IResource iresource : resourceManager.getAllResources(new ResourceLocation(s, "sounds.json")))
                {
                    try
                    {
                        Map<String, SoundList> map = getSoundMap(iresource.getInputStream());

                        for (Map.Entry<String, SoundList> entry : map.entrySet())
                        {
                            loadSoundResource(new ResourceLocation(s, entry.getKey()), entry.getValue());
                        }
                    }
                    catch (RuntimeException runtimeexception)
                    {
                        SoundHandler.logger.warn("Invalid sounds.json", runtimeexception);
                    }
                }
            }
            catch (IOException var11)
            {
            }
        }
    }

    protected Map<String, SoundList> getSoundMap(InputStream stream)
    {
        Map map;

        try
        {
            map = SoundHandler.GSON.fromJson(new InputStreamReader(stream), SoundHandler.TYPE);
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }

        return map;
    }

    private void loadSoundResource(ResourceLocation location, SoundList sounds)
    {
        boolean flag = !sndRegistry.containsKey(location);
        SoundEventAccessorComposite soundeventaccessorcomposite;

        if (!flag && !sounds.canReplaceExisting())
        {
            soundeventaccessorcomposite = sndRegistry.getObject(location);
        }
        else
        {
            if (!flag)
            {
                SoundHandler.logger.debug("Replaced sound event location {}", new Object[] {location});
            }

            soundeventaccessorcomposite = new SoundEventAccessorComposite(location, 1.0D, 1.0D, sounds.getSoundCategory());
            sndRegistry.registerSound(soundeventaccessorcomposite);
        }

        for (final SoundList.SoundEntry soundlist$soundentry : sounds.getSoundList())
        {
            String s = soundlist$soundentry.getSoundEntryName();
            ResourceLocation resourcelocation = new ResourceLocation(s);
            final String s1 = s.contains(":") ? resourcelocation.getResourceDomain() : location.getResourceDomain();
            Object lvt_10_1_;

            switch (soundlist$soundentry.getSoundEntryType())
            {
                case FILE:
                    ResourceLocation resourcelocation1 = new ResourceLocation(s1, "sounds/" + resourcelocation.getResourcePath() + ".ogg");
                    InputStream inputstream = null;

                    try
                    {
                        inputstream = mcResourceManager.getResource(resourcelocation1).getInputStream();
                    }
                    catch (FileNotFoundException var18)
                    {
                        SoundHandler.logger.warn("File {} does not exist, cannot add it to event {}", new Object[] {resourcelocation1, location});
                        continue;
                    }
                    catch (IOException ioexception)
                    {
                        SoundHandler.logger.warn("Could not load sound file " + resourcelocation1 + ", cannot add it to event " + location, ioexception);
                        continue;
                    }
                    finally
                    {
                        IOUtils.closeQuietly(inputstream);
                    }

                    lvt_10_1_ = new SoundEventAccessor(new SoundPoolEntry(resourcelocation1, soundlist$soundentry.getSoundEntryPitch(), soundlist$soundentry.getSoundEntryVolume(), soundlist$soundentry.isStreaming()), soundlist$soundentry.getSoundEntryWeight());
                    break;

                case SOUND_EVENT:
                    lvt_10_1_ = new ISoundEventAccessor<SoundPoolEntry>()
                    {
                        final ResourceLocation field_148726_a = new ResourceLocation(s1, soundlist$soundentry.getSoundEntryName());
                        public int getWeight()
                        {
                            SoundEventAccessorComposite soundeventaccessorcomposite1 = sndRegistry.getObject(field_148726_a);
                            return soundeventaccessorcomposite1 == null ? 0 : soundeventaccessorcomposite1.getWeight();
                        }
                        public SoundPoolEntry cloneEntry()
                        {
                            SoundEventAccessorComposite soundeventaccessorcomposite1 = sndRegistry.getObject(field_148726_a);
                            return soundeventaccessorcomposite1 == null ? SoundHandler.missing_sound : soundeventaccessorcomposite1.cloneEntry();
                        }
                    };

                    break;
                default:
                    throw new IllegalStateException("IN YOU FACE");
            }

            soundeventaccessorcomposite.addSoundToEventPool((ISoundEventAccessor<SoundPoolEntry>)lvt_10_1_);
        }
    }

    public SoundEventAccessorComposite getSound(ResourceLocation location)
    {
        return sndRegistry.getObject(location);
    }

    /**
     * Play a sound
     */
    public void playSound(ISound sound)
    {
        sndManager.playSound(sound);
    }

    /**
     * Plays the sound in n ticks
     */
    public void playDelayedSound(ISound sound, int delay)
    {
        sndManager.playDelayedSound(sound, delay);
    }

    public void setListener(EntityPlayer player, float p_147691_2_)
    {
        sndManager.setListener(player, p_147691_2_);
    }

    public void pauseSounds()
    {
        sndManager.pauseAllSounds();
    }

    public void stopSounds()
    {
        sndManager.stopAllSounds();
    }

    public void unloadSounds()
    {
        sndManager.unloadSoundSystem();
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        sndManager.updateAllSounds();
    }

    public void resumeSounds()
    {
        sndManager.resumeAllSounds();
    }

    public void setSoundLevel(SoundCategory category, float volume)
    {
        if (category == SoundCategory.MASTER && volume <= 0.0F)
        {
            stopSounds();
        }

        sndManager.setSoundCategoryVolume(category, volume);
    }

    public void stopSound(ISound p_147683_1_)
    {
        sndManager.stopSound(p_147683_1_);
    }

    /**
     * Returns a random sound from one or more categories
     */
    public SoundEventAccessorComposite getRandomSoundFromCategories(SoundCategory... categories)
    {
        List<SoundEventAccessorComposite> list = Lists.newArrayList();

        for (ResourceLocation resourcelocation : sndRegistry.getKeys())
        {
            SoundEventAccessorComposite soundeventaccessorcomposite = sndRegistry.getObject(resourcelocation);

            if (ArrayUtils.contains(categories, soundeventaccessorcomposite.getSoundCategory()))
            {
                list.add(soundeventaccessorcomposite);
            }
        }

        if (list.isEmpty())
        {
            return null;
        }
        else
        {
            return list.get((new Random()).nextInt(list.size()));
        }
    }

    public boolean isSoundPlaying(ISound sound)
    {
        return sndManager.isSoundPlaying(sound);
    }
}
