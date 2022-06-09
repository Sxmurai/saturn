package net.minecraft.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.world.World;

public class ItemFishFood extends ItemFood
{
    /** Indicates whether this fish is "cooked" or not. */
    private final boolean cooked;

    public ItemFishFood(boolean cooked)
    {
        super(0, 0.0F, false);
        this.cooked = cooked;
    }

    public int getHealAmount(ItemStack stack)
    {
        ItemFishFood.FishType itemfishfood$fishtype = ItemFishFood.FishType.byItemStack(stack);
        return cooked && itemfishfood$fishtype.canCook() ? itemfishfood$fishtype.getCookedHealAmount() : itemfishfood$fishtype.getUncookedHealAmount();
    }

    public float getSaturationModifier(ItemStack stack)
    {
        ItemFishFood.FishType itemfishfood$fishtype = ItemFishFood.FishType.byItemStack(stack);
        return cooked && itemfishfood$fishtype.canCook() ? itemfishfood$fishtype.getCookedSaturationModifier() : itemfishfood$fishtype.getUncookedSaturationModifier();
    }

    public String getPotionEffect(ItemStack stack)
    {
        return ItemFishFood.FishType.byItemStack(stack) == ItemFishFood.FishType.PUFFERFISH ? PotionHelper.pufferfishEffect : null;
    }

    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player)
    {
        ItemFishFood.FishType itemfishfood$fishtype = ItemFishFood.FishType.byItemStack(stack);

        if (itemfishfood$fishtype == ItemFishFood.FishType.PUFFERFISH)
        {
            player.addPotionEffect(new PotionEffect(Potion.poison.id, 1200, 3));
            player.addPotionEffect(new PotionEffect(Potion.hunger.id, 300, 2));
            player.addPotionEffect(new PotionEffect(Potion.confusion.id, 300, 1));
        }

        super.onFoodEaten(stack, worldIn, player);
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (ItemFishFood.FishType itemfishfood$fishtype : ItemFishFood.FishType.values())
        {
            if (!cooked || itemfishfood$fishtype.canCook())
            {
                subItems.add(new ItemStack(this, 1, itemfishfood$fishtype.getMetadata()));
            }
        }
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack stack)
    {
        ItemFishFood.FishType itemfishfood$fishtype = ItemFishFood.FishType.byItemStack(stack);
        return getUnlocalizedName() + "." + itemfishfood$fishtype.getUnlocalizedName() + "." + (cooked && itemfishfood$fishtype.canCook() ? "cooked" : "raw");
    }

    public static enum FishType
    {
        COD(0, "cod", 2, 0.1F, 5, 0.6F),
        SALMON(1, "salmon", 2, 0.1F, 6, 0.8F),
        CLOWNFISH(2, "clownfish", 1, 0.1F),
        PUFFERFISH(3, "pufferfish", 1, 0.1F);

        private static final Map<Integer, ItemFishFood.FishType> META_LOOKUP = Maps.newHashMap();
        private final int meta;
        private final String unlocalizedName;
        private final int uncookedHealAmount;
        private final float uncookedSaturationModifier;
        private final int cookedHealAmount;
        private final float cookedSaturationModifier;
        private boolean cookable = false;

        private FishType(int meta, String unlocalizedName, int uncookedHeal, float uncookedSaturation, int cookedHeal, float cookedSaturation)
        {
            this.meta = meta;
            this.unlocalizedName = unlocalizedName;
            uncookedHealAmount = uncookedHeal;
            uncookedSaturationModifier = uncookedSaturation;
            cookedHealAmount = cookedHeal;
            cookedSaturationModifier = cookedSaturation;
            cookable = true;
        }

        private FishType(int meta, String unlocalizedName, int uncookedHeal, float uncookedSaturation)
        {
            this.meta = meta;
            this.unlocalizedName = unlocalizedName;
            uncookedHealAmount = uncookedHeal;
            uncookedSaturationModifier = uncookedSaturation;
            cookedHealAmount = 0;
            cookedSaturationModifier = 0.0F;
            cookable = false;
        }

        public int getMetadata()
        {
            return meta;
        }

        public String getUnlocalizedName()
        {
            return unlocalizedName;
        }

        public int getUncookedHealAmount()
        {
            return uncookedHealAmount;
        }

        public float getUncookedSaturationModifier()
        {
            return uncookedSaturationModifier;
        }

        public int getCookedHealAmount()
        {
            return cookedHealAmount;
        }

        public float getCookedSaturationModifier()
        {
            return cookedSaturationModifier;
        }

        public boolean canCook()
        {
            return cookable;
        }

        public static ItemFishFood.FishType byMetadata(int meta)
        {
            ItemFishFood.FishType itemfishfood$fishtype = FishType.META_LOOKUP.get(Integer.valueOf(meta));
            return itemfishfood$fishtype == null ? FishType.COD : itemfishfood$fishtype;
        }

        public static ItemFishFood.FishType byItemStack(ItemStack stack)
        {
            return stack.getItem() instanceof ItemFishFood ? FishType.byMetadata(stack.getMetadata()) : FishType.COD;
        }

        static {
            for (ItemFishFood.FishType itemfishfood$fishtype : FishType.values())
            {
                FishType.META_LOOKUP.put(Integer.valueOf(itemfishfood$fishtype.getMetadata()), itemfishfood$fishtype);
            }
        }
    }
}
