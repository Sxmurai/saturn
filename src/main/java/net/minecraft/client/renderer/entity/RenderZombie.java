package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerVillagerArmor;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

public class RenderZombie extends RenderBiped<EntityZombie>
{
    private static final ResourceLocation zombieTextures = new ResourceLocation("textures/entity/zombie/zombie.png");
    private static final ResourceLocation zombieVillagerTextures = new ResourceLocation("textures/entity/zombie/zombie_villager.png");
    private final ModelBiped field_82434_o;
    private final ModelZombieVillager zombieVillagerModel;
    private final List<LayerRenderer<EntityZombie>> field_177121_n;
    private final List<LayerRenderer<EntityZombie>> field_177122_o;

    public RenderZombie(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelZombie(), 0.5F, 1.0F);
        LayerRenderer layerrenderer = layerRenderers.get(0);
        field_82434_o = modelBipedMain;
        zombieVillagerModel = new ModelZombieVillager();
        addLayer(new LayerHeldItem(this));
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this)
        {
            protected void initArmor()
            {
                field_177189_c = new ModelZombie(0.5F, true);
                field_177186_d = new ModelZombie(1.0F, true);
            }
        };
        addLayer(layerbipedarmor);
        field_177122_o = Lists.newArrayList(layerRenderers);

        if (layerrenderer instanceof LayerCustomHead)
        {
            removeLayer(layerrenderer);
            addLayer(new LayerCustomHead(zombieVillagerModel.bipedHead));
        }

        removeLayer(layerbipedarmor);
        addLayer(new LayerVillagerArmor(this));
        field_177121_n = Lists.newArrayList(layerRenderers);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity>) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doe
     */
    public void doRender(EntityZombie entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        func_82427_a(entity);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityZombie entity)
    {
        return entity.isVillager() ? RenderZombie.zombieVillagerTextures : RenderZombie.zombieTextures;
    }

    private void func_82427_a(EntityZombie zombie)
    {
        if (zombie.isVillager())
        {
            mainModel = zombieVillagerModel;
            layerRenderers = field_177121_n;
        }
        else
        {
            mainModel = field_82434_o;
            layerRenderers = field_177122_o;
        }

        modelBipedMain = (ModelBiped) mainModel;
    }

    protected void rotateCorpse(EntityZombie bat, float p_77043_2_, float p_77043_3_, float partialTicks)
    {
        if (bat.isConverting())
        {
            p_77043_3_ += (float)(Math.cos((double)bat.ticksExisted * 3.25D) * Math.PI * 0.25D);
        }

        super.rotateCorpse(bat, p_77043_2_, p_77043_3_, partialTicks);
    }
}
