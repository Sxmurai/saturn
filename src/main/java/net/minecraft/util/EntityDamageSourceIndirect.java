package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class EntityDamageSourceIndirect extends EntityDamageSource
{
    private final Entity indirectEntity;

    public EntityDamageSourceIndirect(String p_i1568_1_, Entity p_i1568_2_, Entity indirectEntityIn)
    {
        super(p_i1568_1_, p_i1568_2_);
        indirectEntity = indirectEntityIn;
    }

    public Entity getSourceOfDamage()
    {
        return damageSourceEntity;
    }

    public Entity getEntity()
    {
        return indirectEntity;
    }

    /**
     * Gets the death message that is displayed when the player dies
     */
    public IChatComponent getDeathMessage(EntityLivingBase p_151519_1_)
    {
        IChatComponent ichatcomponent = indirectEntity == null ? damageSourceEntity.getDisplayName() : indirectEntity.getDisplayName();
        ItemStack itemstack = indirectEntity instanceof EntityLivingBase ? ((EntityLivingBase) indirectEntity).getHeldItem() : null;
        String s = "death.attack." + damageType;
        String s1 = s + ".item";
        return itemstack != null && itemstack.hasDisplayName() && StatCollector.canTranslate(s1) ? new ChatComponentTranslation(s1, p_151519_1_.getDisplayName(), ichatcomponent, itemstack.getChatComponent()): new ChatComponentTranslation(s, p_151519_1_.getDisplayName(), ichatcomponent);
    }
}
