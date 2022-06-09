package net.minecraft.client.audio;

import net.minecraft.util.ResourceLocation;

public class SoundPoolEntry
{
    private final ResourceLocation location;
    private final boolean streamingSound;
    private double pitch;
    private double volume;

    public SoundPoolEntry(ResourceLocation locationIn, double pitchIn, double volumeIn, boolean streamingSoundIn)
    {
        location = locationIn;
        pitch = pitchIn;
        volume = volumeIn;
        streamingSound = streamingSoundIn;
    }

    public SoundPoolEntry(SoundPoolEntry locationIn)
    {
        location = locationIn.location;
        pitch = locationIn.pitch;
        volume = locationIn.volume;
        streamingSound = locationIn.streamingSound;
    }

    public ResourceLocation getSoundPoolEntryLocation()
    {
        return location;
    }

    public double getPitch()
    {
        return pitch;
    }

    public void setPitch(double pitchIn)
    {
        pitch = pitchIn;
    }

    public double getVolume()
    {
        return volume;
    }

    public void setVolume(double volumeIn)
    {
        volume = volumeIn;
    }

    public boolean isStreamingSound()
    {
        return streamingSound;
    }
}
