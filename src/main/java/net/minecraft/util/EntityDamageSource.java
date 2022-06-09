package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class EntityDamageSource extends DamageSource
{
    protected Entity damageSourceEntity;

    /**
     * Whether this EntityDamageSource is from an entity wearing Thorns-enchanted armor.
     */
    private boolean isThornsDamage = false;

    public EntityDamageSource(String p_i1567_1_, Entity damageSourceEntityIn)
    {
        super(p_i1567_1_);
        damageSourceEntity = damageSourceEntityIn;
    }

    /**
     * Sets this EntityDamageSource as originating from Thorns armor
     */
    public EntityDamageSource setIsThornsDamage()
    {
        isThornsDamage = true;
        return this;
    }

    public boolean getIsThornsDamage()
    {
        return isThornsDamage;
    }

    public Entity getEntity()
    {
        return damageSourceEntity;
    }

    /**
     * Gets the death message that is displayed when the player dies
     */
    public IChatComponent getDeathMessage(EntityLivingBase p_151519_1_)
    {
        ItemStack itemstack = damageSourceEntity instanceof EntityLivingBase ? ((EntityLivingBase) damageSourceEntity).getHeldItem() : null;
        String s = "death.attack." + damageType;
        String s1 = s + ".item";
        return itemstack != null && itemstack.hasDisplayName() && StatCollector.canTranslate(s1) ? new ChatComponentTranslation(s1, p_151519_1_.getDisplayName(), damageSourceEntity.getDisplayName(), itemstack.getChatComponent()): new ChatComponentTranslation(s, p_151519_1_.getDisplayName(), damageSourceEntity.getDisplayName());
    }

    /**
     * Return whether this damage source will have its damage amount scaled based on the current difficulty.
     */
    public boolean isDifficultyScaled()
    {
        return damageSourceEntity != null && damageSourceEntity instanceof EntityLivingBase && !(damageSourceEntity instanceof EntityPlayer);
    }
}
