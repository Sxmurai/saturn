package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.util.ResourceLocation;

public class SoundEventAccessorComposite implements ISoundEventAccessor<SoundPoolEntry>
{
    private final List<ISoundEventAccessor<SoundPoolEntry>> soundPool = Lists.newArrayList();
    private final Random rnd = new Random();
    private final ResourceLocation soundLocation;
    private final SoundCategory category;
    private final double eventPitch;
    private final double eventVolume;

    public SoundEventAccessorComposite(ResourceLocation soundLocation, double pitch, double volume, SoundCategory category)
    {
        this.soundLocation = soundLocation;
        eventVolume = volume;
        eventPitch = pitch;
        this.category = category;
    }

    public int getWeight()
    {
        int i = 0;

        for (ISoundEventAccessor<SoundPoolEntry> isoundeventaccessor : soundPool)
        {
            i += isoundeventaccessor.getWeight();
        }

        return i;
    }

    public SoundPoolEntry cloneEntry()
    {
        int i = getWeight();

        if (!soundPool.isEmpty() && i != 0)
        {
            int j = rnd.nextInt(i);

            for (ISoundEventAccessor<SoundPoolEntry> isoundeventaccessor : soundPool)
            {
                j -= isoundeventaccessor.getWeight();

                if (j < 0)
                {
                    SoundPoolEntry soundpoolentry = isoundeventaccessor.cloneEntry();
                    soundpoolentry.setPitch(soundpoolentry.getPitch() * eventPitch);
                    soundpoolentry.setVolume(soundpoolentry.getVolume() * eventVolume);
                    return soundpoolentry;
                }
            }

            return SoundHandler.missing_sound;
        }
        else
        {
            return SoundHandler.missing_sound;
        }
    }

    public void addSoundToEventPool(ISoundEventAccessor<SoundPoolEntry> sound)
    {
        soundPool.add(sound);
    }

    public ResourceLocation getSoundEventLocation()
    {
        return soundLocation;
    }

    public SoundCategory getSoundCategory()
    {
        return category;
    }
}
