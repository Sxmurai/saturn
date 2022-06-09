package net.minecraft.util;

import java.util.Random;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

public class WeightedRandomFishable extends WeightedRandom.Item
{
    private final ItemStack returnStack;
    private float maxDamagePercent;
    private boolean enchantable;

    public WeightedRandomFishable(ItemStack returnStackIn, int itemWeightIn)
    {
        super(itemWeightIn);
        returnStack = returnStackIn;
    }

    public ItemStack getItemStack(Random random)
    {
        ItemStack itemstack = returnStack.copy();

        if (maxDamagePercent > 0.0F)
        {
            int i = (int)(maxDamagePercent * (float) returnStack.getMaxDamage());
            int j = itemstack.getMaxDamage() - random.nextInt(random.nextInt(i) + 1);

            if (j > i)
            {
                j = i;
            }

            if (j < 1)
            {
                j = 1;
            }

            itemstack.setItemDamage(j);
        }

        if (enchantable)
        {
            EnchantmentHelper.addRandomEnchantment(random, itemstack, 30);
        }

        return itemstack;
    }

    public WeightedRandomFishable setMaxDamagePercent(float maxDamagePercentIn)
    {
        maxDamagePercent = maxDamagePercentIn;
        return this;
    }

    public WeightedRandomFishable setEnchantable()
    {
        enchantable = true;
        return this;
    }
}
