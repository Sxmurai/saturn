package net.minecraft.client.audio;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class MovingSoundMinecartRiding extends MovingSound
{
    private final EntityPlayer player;
    private final EntityMinecart minecart;

    public MovingSoundMinecartRiding(EntityPlayer playerRiding, EntityMinecart minecart)
    {
        super(new ResourceLocation("minecraft:minecart.inside"));
        player = playerRiding;
        this.minecart = minecart;
        attenuationType = ISound.AttenuationType.NONE;
        repeat = true;
        repeatDelay = 0;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        if (!minecart.isDead && player.isRiding() && player.ridingEntity == minecart)
        {
            float f = MathHelper.sqrt_double(minecart.motionX * minecart.motionX + minecart.motionZ * minecart.motionZ);

            if ((double)f >= 0.01D)
            {
                volume = 0.0F + MathHelper.clamp_float(f, 0.0F, 1.0F) * 0.75F;
            }
            else
            {
                volume = 0.0F;
            }
        }
        else
        {
            donePlaying = true;
        }
    }
}
