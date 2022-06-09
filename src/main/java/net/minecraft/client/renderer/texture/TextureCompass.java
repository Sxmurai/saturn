package net.minecraft.client.renderer.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import optifine.Config;
import shadersmod.client.ShadersTex;

public class TextureCompass extends TextureAtlasSprite
{
    /** Current compass heading in radians */
    public double currentAngle;

    /** Speed and direction of compass rotation */
    public double angleDelta;
    public static String field_176608_l;
    private static final String __OBFID = "CL_00001071";

    public TextureCompass(String iconName)
    {
        super(iconName);
        TextureCompass.field_176608_l = iconName;
    }

    public void updateAnimation()
    {
        Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft.theWorld != null && minecraft.thePlayer != null)
        {
            updateCompass(minecraft.theWorld, minecraft.thePlayer.posX, minecraft.thePlayer.posZ, minecraft.thePlayer.rotationYaw, false, false);
        }
        else
        {
            updateCompass(null, 0.0D, 0.0D, 0.0D, true, false);
        }
    }

    /**
     * Updates the compass based on the given x,z coords and camera direction
     */
    public void updateCompass(World worldIn, double p_94241_2_, double p_94241_4_, double p_94241_6_, boolean p_94241_8_, boolean p_94241_9_)
    {
        if (!framesTextureData.isEmpty())
        {
            double d0 = 0.0D;

            if (worldIn != null && !p_94241_8_)
            {
                BlockPos blockpos = worldIn.getSpawnPoint();
                double d1 = (double)blockpos.getX() - p_94241_2_;
                double d2 = (double)blockpos.getZ() - p_94241_4_;
                p_94241_6_ = p_94241_6_ % 360.0D;
                d0 = -((p_94241_6_ - 90.0D) * Math.PI / 180.0D - Math.atan2(d2, d1));

                if (!worldIn.provider.isSurfaceWorld())
                {
                    d0 = Math.random() * Math.PI * 2.0D;
                }
            }

            if (p_94241_9_)
            {
                currentAngle = d0;
            }
            else
            {
                double d3;

                for (d3 = d0 - currentAngle; d3 < -Math.PI; d3 += (Math.PI * 2D))
                {
                }

                while (d3 >= Math.PI)
                {
                    d3 -= (Math.PI * 2D);
                }

                d3 = MathHelper.clamp_double(d3, -1.0D, 1.0D);
                angleDelta += d3 * 0.1D;
                angleDelta *= 0.8D;
                currentAngle += angleDelta;
            }

            int i;

            for (i = (int)((currentAngle / (Math.PI * 2D) + 1.0D) * (double) framesTextureData.size()) % framesTextureData.size(); i < 0; i = (i + framesTextureData.size()) % framesTextureData.size())
            {
            }

            if (i != frameCounter)
            {
                frameCounter = i;

                if (Config.isShaders())
                {
                    ShadersTex.uploadTexSub((int[][]) framesTextureData.get(frameCounter), width, height, originX, originY, false, false);
                }
                else
                {
                    TextureUtil.uploadTextureMipmap((int[][]) framesTextureData.get(frameCounter), width, height, originX, originY, false, false);
                }
            }
        }
    }
}
