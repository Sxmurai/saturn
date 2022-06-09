package net.minecraft.client.audio;

import net.minecraft.util.ResourceLocation;

public abstract class PositionedSound implements ISound
{
    protected final ResourceLocation positionedSoundLocation;
    protected float volume = 1.0F;
    protected float pitch = 1.0F;
    protected float xPosF;
    protected float yPosF;
    protected float zPosF;
    protected boolean repeat = false;

    /** The number of ticks between repeating the sound */
    protected int repeatDelay = 0;
    protected ISound.AttenuationType attenuationType = ISound.AttenuationType.LINEAR;

    protected PositionedSound(ResourceLocation soundResource)
    {
        positionedSoundLocation = soundResource;
    }

    public ResourceLocation getSoundLocation()
    {
        return positionedSoundLocation;
    }

    public boolean canRepeat()
    {
        return repeat;
    }

    public int getRepeatDelay()
    {
        return repeatDelay;
    }

    public float getVolume()
    {
        return volume;
    }

    public float getPitch()
    {
        return pitch;
    }

    public float getXPosF()
    {
        return xPosF;
    }

    public float getYPosF()
    {
        return yPosF;
    }

    public float getZPosF()
    {
        return zPosF;
    }

    public ISound.AttenuationType getAttenuationType()
    {
        return attenuationType;
    }
}
