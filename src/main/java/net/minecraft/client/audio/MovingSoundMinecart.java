package net.minecraft.client.audio;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class MovingSoundMinecart extends MovingSound
{
    private final EntityMinecart minecart;
    private float distance = 0.0F;

    public MovingSoundMinecart(EntityMinecart minecartIn)
    {
        super(new ResourceLocation("minecraft:minecart.base"));
        minecart = minecartIn;
        repeat = true;
        repeatDelay = 0;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        if (minecart.isDead)
        {
            donePlaying = true;
        }
        else
        {
            xPosF = (float) minecart.posX;
            yPosF = (float) minecart.posY;
            zPosF = (float) minecart.posZ;
            float f = MathHelper.sqrt_double(minecart.motionX * minecart.motionX + minecart.motionZ * minecart.motionZ);

            if ((double)f >= 0.01D)
            {
                distance = MathHelper.clamp_float(distance + 0.0025F, 0.0F, 1.0F);
                volume = 0.0F + MathHelper.clamp_float(f, 0.0F, 0.5F) * 0.7F;
            }
            else
            {
                distance = 0.0F;
                volume = 0.0F;
            }
        }
    }
}
