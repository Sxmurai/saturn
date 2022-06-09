package net.minecraft.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PotionEffect
{
    private static final Logger LOGGER = LogManager.getLogger();

    /** ID value of the potion this effect matches. */
    private final int potionID;

    /** The duration of the potion effect */
    private int duration;

    /** The amplifier of the potion effect */
    private int amplifier;

    /** Whether the potion is a splash potion */
    private boolean isSplashPotion;

    /** Whether the potion effect came from a beacon */
    private boolean isAmbient;

    /** True if potion effect duration is at maximum, false otherwise. */
    private boolean isPotionDurationMax;
    private boolean showParticles;

    public PotionEffect(int id, int effectDuration)
    {
        this(id, effectDuration, 0);
    }

    public PotionEffect(int id, int effectDuration, int effectAmplifier)
    {
        this(id, effectDuration, effectAmplifier, false, true);
    }

    public PotionEffect(int id, int effectDuration, int effectAmplifier, boolean ambient, boolean showParticles)
    {
        potionID = id;
        duration = effectDuration;
        amplifier = effectAmplifier;
        isAmbient = ambient;
        this.showParticles = showParticles;
    }

    public PotionEffect(PotionEffect other)
    {
        potionID = other.potionID;
        duration = other.duration;
        amplifier = other.amplifier;
        isAmbient = other.isAmbient;
        showParticles = other.showParticles;
    }

    /**
     * merges the input PotionEffect into this one if this.amplifier <= tomerge.amplifier. The duration in the supplied
     * potion effect is assumed to be greater.
     */
    public void combine(PotionEffect other)
    {
        if (potionID != other.potionID)
        {
            PotionEffect.LOGGER.warn("This method should only be called for matching effects!");
        }

        if (other.amplifier > amplifier)
        {
            amplifier = other.amplifier;
            duration = other.duration;
        }
        else if (other.amplifier == amplifier && duration < other.duration)
        {
            duration = other.duration;
        }
        else if (!other.isAmbient && isAmbient)
        {
            isAmbient = other.isAmbient;
        }

        showParticles = other.showParticles;
    }

    /**
     * Retrieve the ID of the potion this effect matches.
     */
    public int getPotionID()
    {
        return potionID;
    }

    public int getDuration()
    {
        return duration;
    }

    public int getAmplifier()
    {
        return amplifier;
    }

    /**
     * Set whether this potion is a splash potion.
     */
    public void setSplashPotion(boolean splashPotion)
    {
        isSplashPotion = splashPotion;
    }

    /**
     * Gets whether this potion effect originated from a beacon
     */
    public boolean getIsAmbient()
    {
        return isAmbient;
    }

    public boolean getIsShowParticles()
    {
        return showParticles;
    }

    public boolean onUpdate(EntityLivingBase entityIn)
    {
        if (duration > 0)
        {
            if (Potion.potionTypes[potionID].isReady(duration, amplifier))
            {
                performEffect(entityIn);
            }

            deincrementDuration();
        }

        return duration > 0;
    }

    private int deincrementDuration()
    {
        return --duration;
    }

    public void performEffect(EntityLivingBase entityIn)
    {
        if (duration > 0)
        {
            Potion.potionTypes[potionID].performEffect(entityIn, amplifier);
        }
    }

    public String getEffectName()
    {
        return Potion.potionTypes[potionID].getName();
    }

    public int hashCode()
    {
        return potionID;
    }

    public String toString()
    {
        String s = "";

        if (getAmplifier() > 0)
        {
            s = getEffectName() + " x " + (getAmplifier() + 1) + ", Duration: " + getDuration();
        }
        else
        {
            s = getEffectName() + ", Duration: " + getDuration();
        }

        if (isSplashPotion)
        {
            s = s + ", Splash: true";
        }

        if (!showParticles)
        {
            s = s + ", Particles: false";
        }

        return Potion.potionTypes[potionID].isUsable() ? "(" + s + ")" : s;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (!(p_equals_1_ instanceof PotionEffect))
        {
            return false;
        }
        else
        {
            PotionEffect potioneffect = (PotionEffect)p_equals_1_;
            return potionID == potioneffect.potionID && amplifier == potioneffect.amplifier && duration == potioneffect.duration && isSplashPotion == potioneffect.isSplashPotion && isAmbient == potioneffect.isAmbient;
        }
    }

    /**
     * Write a custom potion effect to a potion item's NBT data.
     */
    public NBTTagCompound writeCustomPotionEffectToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("Id", (byte) getPotionID());
        nbt.setByte("Amplifier", (byte) getAmplifier());
        nbt.setInteger("Duration", getDuration());
        nbt.setBoolean("Ambient", getIsAmbient());
        nbt.setBoolean("ShowParticles", getIsShowParticles());
        return nbt;
    }

    /**
     * Read a custom potion effect from a potion item's NBT data.
     */
    public static PotionEffect readCustomPotionEffectFromNBT(NBTTagCompound nbt)
    {
        int i = nbt.getByte("Id");

        if (i >= 0 && i < Potion.potionTypes.length && Potion.potionTypes[i] != null)
        {
            int j = nbt.getByte("Amplifier");
            int k = nbt.getInteger("Duration");
            boolean flag = nbt.getBoolean("Ambient");
            boolean flag1 = true;

            if (nbt.hasKey("ShowParticles", 1))
            {
                flag1 = nbt.getBoolean("ShowParticles");
            }

            return new PotionEffect(i, k, j, flag, flag1);
        }
        else
        {
            return null;
        }
    }

    /**
     * Toggle the isPotionDurationMax field.
     */
    public void setPotionDurationMax(boolean maxDuration)
    {
        isPotionDurationMax = maxDuration;
    }

    public boolean getIsPotionDurationMax()
    {
        return isPotionDurationMax;
    }
}
