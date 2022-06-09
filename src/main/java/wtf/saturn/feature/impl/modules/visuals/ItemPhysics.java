package wtf.saturn.feature.impl.modules.visuals;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;

import static org.lwjgl.opengl.GL11.glRotatef;

public class ItemPhysics extends Module {
    public ItemPhysics() {
        super("Item Physics", ModuleCategory.VISUALS, "Applies physics to items");
    }

    private double rotation;
    private long tick;

    @Override
    protected void onDeactivated() {
        super.onDeactivated();
        rotation = 0.0;
        tick = 0L;
    }

    public void renderPhysics(RenderEntityItem renderer, EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks) {
        rotation = (System.nanoTime() - tick) / 3000000.0;
        if (!mc.inGameHasFocus) {
            rotation = 0.0;
        }

        ItemStack itemstack = entity.getEntityItem();
        if (itemstack.getItem() != null) {
            RenderEntityItem.RNG.setSeed(187L);

            mc.getRenderManager().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            mc.getRenderManager().renderEngine.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);

            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1f);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();

            IBakedModel ibakedmodel = renderer.itemRenderer.getItemModelMesher().getItemModel(itemstack);
            int i = func_177077_a(entity, x, y, z, ibakedmodel);
            BlockPos blockpos = new BlockPos(entity);

            if (entity.rotationPitch > 360.0f) {
                entity.rotationPitch = 0.0f;
            }

            if (!Double.isNaN(entity.posX) && !Double.isNaN(entity.posY) && !Double.isNaN(entity.posZ) && entity.worldObj != null) {
                if (entity.onGround) {
                    if (entity.rotationPitch != 0.0f && entity.rotationPitch != 90.0f
                            && entity.rotationPitch != 180.0f && entity.rotationPitch != 270.0f) {
                        double d0 = renderer.formPositiv(entity.rotationPitch);
                        double d2 = renderer.formPositiv(entity.rotationPitch - 90.0f);
                        double d3 = renderer.formPositiv(entity.rotationPitch - 180.0f);
                        double d4 = renderer.formPositiv(entity.rotationPitch - 270.0f);

                        if (d0 <= d2 && d0 <= d3 && d0 <= d4) {
                            if (entity.rotationPitch < 0.0f) {
                                entity.rotationPitch += (float) rotation;
                            } else {
                                entity.rotationPitch -= (float) rotation;
                            }
                        }

                        if (d2 < d0 && d2 <= d3 && d2 <= d4) {
                            if (entity.rotationPitch - 90.0f < 0.0f) {
                                entity.rotationPitch += (float) rotation;
                            } else {
                                entity.rotationPitch -= (float) rotation;
                            }
                        }

                        if (d3 < d2 && d3 < d0 && d3 <= d4) {
                            if (entity.rotationPitch - 180.0f < 0.0f) {
                                entity.rotationPitch += (float) rotation;
                            } else {
                                entity.rotationPitch -= (float) rotation;
                            }
                        }

                        if (d4 < d2 && d4 < d3 && d4 < d0) {
                            if (entity.rotationPitch - 270.0f < 0.0f) {
                                entity.rotationPitch += (float) rotation;
                            } else {
                                entity.rotationPitch -= (float) rotation;
                            }
                        }
                    }
                } else {
                    BlockPos blockpos2 = new BlockPos(entity).add(0, 1, 0);

                    Material material = entity.worldObj.getBlockState(blockpos2).getBlock().getMaterial();
                    Material material2 = entity.worldObj.getBlockState(blockpos).getBlock().getMaterial();

                    if (entity.isInsideOfMaterial(Material.water) | material == Material.water | material2 == Material.water | entity.isInWater()) {
                        entity.rotationPitch += (float) (rotation / 4.0);
                    } else {
                        entity.rotationPitch += (float) (rotation * 2.0);
                    }
                }
            }

            glRotatef(entity.rotationYaw, 0.0f, 1.0f, 0.0f);
            glRotatef(entity.rotationPitch + 90.0f, 1.0f, 0.0f, 0.0f);

            for (int j = 0; j < i; ++j) {
                if (ibakedmodel.isAmbientOcclusion()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.5, 0.5, 0.5);
                    renderer.itemRenderer.renderItem(itemstack, ibakedmodel);
                    GlStateManager.popMatrix();
                } else {
                    GlStateManager.pushMatrix();

                    if (j > 0) {
                        GlStateManager.translate(0.0f, 0.0f, 0.046875f * j);
                    }

                    renderer.itemRenderer.renderItem(itemstack, ibakedmodel);
                    GlStateManager.popMatrix();
                }
            }

            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();

            mc.getRenderManager().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            mc.getRenderManager().renderEngine.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        }
    }

    private int func_177077_a(EntityItem itemIn, double p_177077_2_, double p_177077_4_, double p_177077_6_, IBakedModel p_177077_9_) {
        ItemStack itemstack = itemIn.getEntityItem();
        Item item = itemstack.getItem();

        if (item == null) {
            return 0;
        }

        final boolean flag = p_177077_9_.isAmbientOcclusion();
        final int i = itemstack.stackSize;
        final float f2 = 0.0f;

        GlStateManager.translate((float) p_177077_2_, (float) p_177077_4_ + f2 + 0.1f, (float) p_177077_6_);
        float f3 = 0.0f;

        if (flag || (mc.getRenderManager().options != null && mc.getRenderManager().options.fancyGraphics)) {
            GlStateManager.rotate(f3, 0.0f, 1.0f, 0.0f);
        }

        if (!flag) {
            f3 = -0.0f * (i - 1) * 0.5f;
            final float f4 = -0.0f * (i - 1) * 0.5f;
            final float f5 = -0.046875f * (i - 1) * 0.5f;
            GlStateManager.translate(f3, f4, f5);
        }

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        return 1;
    }
}
