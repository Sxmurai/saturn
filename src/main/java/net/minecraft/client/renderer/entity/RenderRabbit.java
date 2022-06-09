package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class RenderRabbit extends RenderLiving<EntityRabbit>
{
    private static final ResourceLocation BROWN = new ResourceLocation("textures/entity/rabbit/brown.png");
    private static final ResourceLocation WHITE = new ResourceLocation("textures/entity/rabbit/white.png");
    private static final ResourceLocation BLACK = new ResourceLocation("textures/entity/rabbit/black.png");
    private static final ResourceLocation GOLD = new ResourceLocation("textures/entity/rabbit/gold.png");
    private static final ResourceLocation SALT = new ResourceLocation("textures/entity/rabbit/salt.png");
    private static final ResourceLocation WHITE_SPLOTCHED = new ResourceLocation("textures/entity/rabbit/white_splotched.png");
    private static final ResourceLocation TOAST = new ResourceLocation("textures/entity/rabbit/toast.png");
    private static final ResourceLocation CAERBANNOG = new ResourceLocation("textures/entity/rabbit/caerbannog.png");

    public RenderRabbit(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn)
    {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityRabbit entity)
    {
        String s = EnumChatFormatting.getTextWithoutFormattingCodes(entity.getName());

        if (s != null && s.equals("Toast"))
        {
            return RenderRabbit.TOAST;
        }
        else
        {
            switch (entity.getRabbitType())
            {
                case 0:
                default:
                    return RenderRabbit.BROWN;

                case 1:
                    return RenderRabbit.WHITE;

                case 2:
                    return RenderRabbit.BLACK;

                case 3:
                    return RenderRabbit.WHITE_SPLOTCHED;

                case 4:
                    return RenderRabbit.GOLD;

                case 5:
                    return RenderRabbit.SALT;

                case 99:
                    return RenderRabbit.CAERBANNOG;
            }
        }
    }
}
