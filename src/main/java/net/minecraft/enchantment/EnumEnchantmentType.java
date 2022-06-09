package net.minecraft.enchantment;

import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

public enum EnumEnchantmentType
{
    ALL,
    ARMOR,
    ARMOR_FEET,
    ARMOR_LEGS,
    ARMOR_TORSO,
    ARMOR_HEAD,
    WEAPON,
    DIGGER,
    FISHING_ROD,
    BREAKABLE,
    BOW;

    /**
     * Return true if the item passed can be enchanted by a enchantment of this type.
     */
    public boolean canEnchantItem(Item p_77557_1_)
    {
        if (this == EnumEnchantmentType.ALL)
        {
            return true;
        }
        else if (this == EnumEnchantmentType.BREAKABLE && p_77557_1_.isDamageable())
        {
            return true;
        }
        else if (p_77557_1_ instanceof ItemArmor)
        {
            if (this == EnumEnchantmentType.ARMOR)
            {
                return true;
            }
            else
            {
                ItemArmor itemarmor = (ItemArmor)p_77557_1_;
                return itemarmor.armorType == 0 ? this == EnumEnchantmentType.ARMOR_HEAD : (itemarmor.armorType == 2 ? this == EnumEnchantmentType.ARMOR_LEGS : (itemarmor.armorType == 1 ? this == EnumEnchantmentType.ARMOR_TORSO : (itemarmor.armorType == 3 && this == EnumEnchantmentType.ARMOR_FEET)));
            }
        }
        else
        {
            return p_77557_1_ instanceof ItemSword ? this == EnumEnchantmentType.WEAPON : (p_77557_1_ instanceof ItemTool ? this == EnumEnchantmentType.DIGGER : (p_77557_1_ instanceof ItemBow ? this == EnumEnchantmentType.BOW : (p_77557_1_ instanceof ItemFishingRod && this == EnumEnchantmentType.FISHING_ROD)));
        }
    }
}
