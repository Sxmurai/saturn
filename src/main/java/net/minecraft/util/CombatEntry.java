package net.minecraft.util;

import net.minecraft.entity.EntityLivingBase;

public class CombatEntry
{
    private final DamageSource damageSrc;
    private final int field_94567_b;
    private final float damage;
    private final float health;
    private final String field_94566_e;
    private final float fallDistance;

    public CombatEntry(DamageSource damageSrcIn, int p_i1564_2_, float healthAmount, float damageAmount, String p_i1564_5_, float fallDistanceIn)
    {
        damageSrc = damageSrcIn;
        field_94567_b = p_i1564_2_;
        damage = damageAmount;
        health = healthAmount;
        field_94566_e = p_i1564_5_;
        fallDistance = fallDistanceIn;
    }

    /**
     * Get the DamageSource of the CombatEntry instance.
     */
    public DamageSource getDamageSrc()
    {
        return damageSrc;
    }

    public float func_94563_c()
    {
        return damage;
    }

    /**
     * Returns true if {@link net.minecraft.util.DamageSource#getEntity() damage source} is a living entity
     */
    public boolean isLivingDamageSrc()
    {
        return damageSrc.getEntity() instanceof EntityLivingBase;
    }

    public String func_94562_g()
    {
        return field_94566_e;
    }

    public IChatComponent getDamageSrcDisplayName()
    {
        return getDamageSrc().getEntity() == null ? null : getDamageSrc().getEntity().getDisplayName();
    }

    public float getDamageAmount()
    {
        return damageSrc == DamageSource.outOfWorld ? Float.MAX_VALUE : fallDistance;
    }
}
