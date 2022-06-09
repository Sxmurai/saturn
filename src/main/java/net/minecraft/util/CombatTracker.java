package net.minecraft.util;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class CombatTracker
{
    private final List<CombatEntry> combatEntries = Lists.newArrayList();

    /** The entity tracked. */
    private final EntityLivingBase fighter;
    private int field_94555_c;
    private int field_152775_d;
    private int field_152776_e;
    private boolean field_94552_d;
    private boolean field_94553_e;
    private String field_94551_f;

    public CombatTracker(EntityLivingBase fighterIn)
    {
        fighter = fighterIn;
    }

    public void func_94545_a()
    {
        func_94542_g();

        if (fighter.isOnLadder())
        {
            Block block = fighter.worldObj.getBlockState(new BlockPos(fighter.posX, fighter.getEntityBoundingBox().minY, fighter.posZ)).getBlock();

            if (block == Blocks.ladder)
            {
                field_94551_f = "ladder";
            }
            else if (block == Blocks.vine)
            {
                field_94551_f = "vines";
            }
        }
        else if (fighter.isInWater())
        {
            field_94551_f = "water";
        }
    }

    /**
     * Adds an entry for the combat tracker
     */
    public void trackDamage(DamageSource damageSrc, float healthIn, float damageAmount)
    {
        reset();
        func_94545_a();
        CombatEntry combatentry = new CombatEntry(damageSrc, fighter.ticksExisted, healthIn, damageAmount, field_94551_f, fighter.fallDistance);
        combatEntries.add(combatentry);
        field_94555_c = fighter.ticksExisted;
        field_94553_e = true;

        if (combatentry.isLivingDamageSrc() && !field_94552_d && fighter.isEntityAlive())
        {
            field_94552_d = true;
            field_152775_d = fighter.ticksExisted;
            field_152776_e = field_152775_d;
            fighter.sendEnterCombat();
        }
    }

    public IChatComponent getDeathMessage()
    {
        if (combatEntries.size() == 0)
        {
            return new ChatComponentTranslation("death.attack.generic", fighter.getDisplayName());
        }
        else
        {
            CombatEntry combatentry = func_94544_f();
            CombatEntry combatentry1 = combatEntries.get(combatEntries.size() - 1);
            IChatComponent ichatcomponent1 = combatentry1.getDamageSrcDisplayName();
            Entity entity = combatentry1.getDamageSrc().getEntity();
            IChatComponent ichatcomponent;

            if (combatentry != null && combatentry1.getDamageSrc() == DamageSource.fall)
            {
                IChatComponent ichatcomponent2 = combatentry.getDamageSrcDisplayName();

                if (combatentry.getDamageSrc() != DamageSource.fall && combatentry.getDamageSrc() != DamageSource.outOfWorld)
                {
                    if (ichatcomponent2 != null && (ichatcomponent1 == null || !ichatcomponent2.equals(ichatcomponent1)))
                    {
                        Entity entity1 = combatentry.getDamageSrc().getEntity();
                        ItemStack itemstack1 = entity1 instanceof EntityLivingBase ? ((EntityLivingBase)entity1).getHeldItem() : null;

                        if (itemstack1 != null && itemstack1.hasDisplayName())
                        {
                            ichatcomponent = new ChatComponentTranslation("death.fell.assist.item", fighter.getDisplayName(), ichatcomponent2, itemstack1.getChatComponent());
                        }
                        else
                        {
                            ichatcomponent = new ChatComponentTranslation("death.fell.assist", fighter.getDisplayName(), ichatcomponent2);
                        }
                    }
                    else if (ichatcomponent1 != null)
                    {
                        ItemStack itemstack = entity instanceof EntityLivingBase ? ((EntityLivingBase)entity).getHeldItem() : null;

                        if (itemstack != null && itemstack.hasDisplayName())
                        {
                            ichatcomponent = new ChatComponentTranslation("death.fell.finish.item", fighter.getDisplayName(), ichatcomponent1, itemstack.getChatComponent());
                        }
                        else
                        {
                            ichatcomponent = new ChatComponentTranslation("death.fell.finish", fighter.getDisplayName(), ichatcomponent1);
                        }
                    }
                    else
                    {
                        ichatcomponent = new ChatComponentTranslation("death.fell.killer", fighter.getDisplayName());
                    }
                }
                else
                {
                    ichatcomponent = new ChatComponentTranslation("death.fell.accident." + func_94548_b(combatentry), fighter.getDisplayName());
                }
            }
            else
            {
                ichatcomponent = combatentry1.getDamageSrc().getDeathMessage(fighter);
            }

            return ichatcomponent;
        }
    }

    public EntityLivingBase func_94550_c()
    {
        EntityLivingBase entitylivingbase = null;
        EntityPlayer entityplayer = null;
        float f = 0.0F;
        float f1 = 0.0F;

        for (CombatEntry combatentry : combatEntries)
        {
            if (combatentry.getDamageSrc().getEntity() instanceof EntityPlayer && (entityplayer == null || combatentry.func_94563_c() > f1))
            {
                f1 = combatentry.func_94563_c();
                entityplayer = (EntityPlayer)combatentry.getDamageSrc().getEntity();
            }

            if (combatentry.getDamageSrc().getEntity() instanceof EntityLivingBase && (entitylivingbase == null || combatentry.func_94563_c() > f))
            {
                f = combatentry.func_94563_c();
                entitylivingbase = (EntityLivingBase)combatentry.getDamageSrc().getEntity();
            }
        }

        if (entityplayer != null && f1 >= f / 3.0F)
        {
            return entityplayer;
        }
        else
        {
            return entitylivingbase;
        }
    }

    private CombatEntry func_94544_f()
    {
        CombatEntry combatentry = null;
        CombatEntry combatentry1 = null;
        int i = 0;
        float f = 0.0F;

        for (int j = 0; j < combatEntries.size(); ++j)
        {
            CombatEntry combatentry2 = combatEntries.get(j);
            CombatEntry combatentry3 = j > 0 ? combatEntries.get(j - 1) : null;

            if ((combatentry2.getDamageSrc() == DamageSource.fall || combatentry2.getDamageSrc() == DamageSource.outOfWorld) && combatentry2.getDamageAmount() > 0.0F && (combatentry == null || combatentry2.getDamageAmount() > f))
            {
                if (j > 0)
                {
                    combatentry = combatentry3;
                }
                else
                {
                    combatentry = combatentry2;
                }

                f = combatentry2.getDamageAmount();
            }

            if (combatentry2.func_94562_g() != null && (combatentry1 == null || combatentry2.func_94563_c() > (float)i))
            {
                combatentry1 = combatentry2;
            }
        }

        if (f > 5.0F && combatentry != null)
        {
            return combatentry;
        }
        else if (i > 5 && combatentry1 != null)
        {
            return combatentry1;
        }
        else
        {
            return null;
        }
    }

    private String func_94548_b(CombatEntry p_94548_1_)
    {
        return p_94548_1_.func_94562_g() == null ? "generic" : p_94548_1_.func_94562_g();
    }

    public int func_180134_f()
    {
        return field_94552_d ? fighter.ticksExisted - field_152775_d : field_152776_e - field_152775_d;
    }

    private void func_94542_g()
    {
        field_94551_f = null;
    }

    /**
     * Resets this trackers list of combat entries
     */
    public void reset()
    {
        int i = field_94552_d ? 300 : 100;

        if (field_94553_e && (!fighter.isEntityAlive() || fighter.ticksExisted - field_94555_c > i))
        {
            boolean flag = field_94552_d;
            field_94553_e = false;
            field_94552_d = false;
            field_152776_e = fighter.ticksExisted;

            if (flag)
            {
                fighter.sendEndCombat();
            }

            combatEntries.clear();
        }
    }

    /**
     * Returns EntityLivingBase assigned for this CombatTracker
     */
    public EntityLivingBase getFighter()
    {
        return fighter;
    }
}
