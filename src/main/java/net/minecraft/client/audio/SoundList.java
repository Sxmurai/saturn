package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import java.util.List;

public class SoundList
{
    private final List<SoundList.SoundEntry> soundList = Lists.newArrayList();

    /**
     * if true it will override all the sounds from the resourcepacks loaded before
     */
    private boolean replaceExisting;
    private SoundCategory category;

    public List<SoundList.SoundEntry> getSoundList()
    {
        return soundList;
    }

    public boolean canReplaceExisting()
    {
        return replaceExisting;
    }

    public void setReplaceExisting(boolean p_148572_1_)
    {
        replaceExisting = p_148572_1_;
    }

    public SoundCategory getSoundCategory()
    {
        return category;
    }

    public void setSoundCategory(SoundCategory soundCat)
    {
        category = soundCat;
    }

    public static class SoundEntry
    {
        private String name;
        private float volume = 1.0F;
        private float pitch = 1.0F;
        private int weight = 1;
        private SoundList.SoundEntry.Type type = SoundList.SoundEntry.Type.FILE;
        private boolean streaming = false;

        public String getSoundEntryName()
        {
            return name;
        }

        public void setSoundEntryName(String nameIn)
        {
            name = nameIn;
        }

        public float getSoundEntryVolume()
        {
            return volume;
        }

        public void setSoundEntryVolume(float volumeIn)
        {
            volume = volumeIn;
        }

        public float getSoundEntryPitch()
        {
            return pitch;
        }

        public void setSoundEntryPitch(float pitchIn)
        {
            pitch = pitchIn;
        }

        public int getSoundEntryWeight()
        {
            return weight;
        }

        public void setSoundEntryWeight(int weightIn)
        {
            weight = weightIn;
        }

        public SoundList.SoundEntry.Type getSoundEntryType()
        {
            return type;
        }

        public void setSoundEntryType(SoundList.SoundEntry.Type typeIn)
        {
            type = typeIn;
        }

        public boolean isStreaming()
        {
            return streaming;
        }

        public void setStreaming(boolean isStreaming)
        {
            streaming = isStreaming;
        }

        public static enum Type
        {
            FILE("file"),
            SOUND_EVENT("event");

            private final String field_148583_c;

            private Type(String p_i45109_3_)
            {
                field_148583_c = p_i45109_3_;
            }

            public static SoundList.SoundEntry.Type getType(String p_148580_0_)
            {
                for (SoundList.SoundEntry.Type soundlist$soundentry$type : Type.values())
                {
                    if (soundlist$soundentry$type.field_148583_c.equals(p_148580_0_))
                    {
                        return soundlist$soundentry$type;
                    }
                }

                return null;
            }
        }
    }
}
