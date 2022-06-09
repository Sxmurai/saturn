package net.minecraft.stats;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonSerializableSet;

public class AchievementList
{
    /** Is the smallest column used to display a achievement on the GUI. */
    public static int minDisplayColumn;

    /** Is the smallest row used to display a achievement on the GUI. */
    public static int minDisplayRow;

    /** Is the biggest column used to display a achievement on the GUI. */
    public static int maxDisplayColumn;

    /** Is the biggest row used to display a achievement on the GUI. */
    public static int maxDisplayRow;
    public static List<Achievement> achievementList = Lists.newArrayList();

    /** Is the 'open inventory' achievement. */
    public static Achievement openInventory = (new Achievement("achievement.openInventory", "openInventory", 0, 0, Items.book, null)).initIndependentStat().registerStat();

    /** Is the 'getting wood' achievement. */
    public static Achievement mineWood = (new Achievement("achievement.mineWood", "mineWood", 2, 1, Blocks.log, AchievementList.openInventory)).registerStat();

    /** Is the 'benchmarking' achievement. */
    public static Achievement buildWorkBench = (new Achievement("achievement.buildWorkBench", "buildWorkBench", 4, -1, Blocks.crafting_table, AchievementList.mineWood)).registerStat();

    /** Is the 'time to mine' achievement. */
    public static Achievement buildPickaxe = (new Achievement("achievement.buildPickaxe", "buildPickaxe", 4, 2, Items.wooden_pickaxe, AchievementList.buildWorkBench)).registerStat();

    /** Is the 'hot topic' achievement. */
    public static Achievement buildFurnace = (new Achievement("achievement.buildFurnace", "buildFurnace", 3, 4, Blocks.furnace, AchievementList.buildPickaxe)).registerStat();

    /** Is the 'acquire hardware' achievement. */
    public static Achievement acquireIron = (new Achievement("achievement.acquireIron", "acquireIron", 1, 4, Items.iron_ingot, AchievementList.buildFurnace)).registerStat();

    /** Is the 'time to farm' achievement. */
    public static Achievement buildHoe = (new Achievement("achievement.buildHoe", "buildHoe", 2, -3, Items.wooden_hoe, AchievementList.buildWorkBench)).registerStat();

    /** Is the 'bake bread' achievement. */
    public static Achievement makeBread = (new Achievement("achievement.makeBread", "makeBread", -1, -3, Items.bread, AchievementList.buildHoe)).registerStat();

    /** Is the 'the lie' achievement. */
    public static Achievement bakeCake = (new Achievement("achievement.bakeCake", "bakeCake", 0, -5, Items.cake, AchievementList.buildHoe)).registerStat();

    /** Is the 'getting a upgrade' achievement. */
    public static Achievement buildBetterPickaxe = (new Achievement("achievement.buildBetterPickaxe", "buildBetterPickaxe", 6, 2, Items.stone_pickaxe, AchievementList.buildPickaxe)).registerStat();

    /** Is the 'delicious fish' achievement. */
    public static Achievement cookFish = (new Achievement("achievement.cookFish", "cookFish", 2, 6, Items.cooked_fish, AchievementList.buildFurnace)).registerStat();

    /** Is the 'on a rail' achievement */
    public static Achievement onARail = (new Achievement("achievement.onARail", "onARail", 2, 3, Blocks.rail, AchievementList.acquireIron)).setSpecial().registerStat();

    /** Is the 'time to strike' achievement. */
    public static Achievement buildSword = (new Achievement("achievement.buildSword", "buildSword", 6, -1, Items.wooden_sword, AchievementList.buildWorkBench)).registerStat();

    /** Is the 'monster hunter' achievement. */
    public static Achievement killEnemy = (new Achievement("achievement.killEnemy", "killEnemy", 8, -1, Items.bone, AchievementList.buildSword)).registerStat();

    /** is the 'cow tipper' achievement. */
    public static Achievement killCow = (new Achievement("achievement.killCow", "killCow", 7, -3, Items.leather, AchievementList.buildSword)).registerStat();

    /** Is the 'when pig fly' achievement. */
    public static Achievement flyPig = (new Achievement("achievement.flyPig", "flyPig", 9, -3, Items.saddle, AchievementList.killCow)).setSpecial().registerStat();

    /** The achievement for killing a Skeleton from 50 meters aways. */
    public static Achievement snipeSkeleton = (new Achievement("achievement.snipeSkeleton", "snipeSkeleton", 7, 0, Items.bow, AchievementList.killEnemy)).setSpecial().registerStat();

    /** Is the 'DIAMONDS!' achievement */
    public static Achievement diamonds = (new Achievement("achievement.diamonds", "diamonds", -1, 5, Blocks.diamond_ore, AchievementList.acquireIron)).registerStat();
    public static Achievement diamondsToYou = (new Achievement("achievement.diamondsToYou", "diamondsToYou", -1, 2, Items.diamond, AchievementList.diamonds)).registerStat();

    /** Is the 'We Need to Go Deeper' achievement */
    public static Achievement portal = (new Achievement("achievement.portal", "portal", -1, 7, Blocks.obsidian, AchievementList.diamonds)).registerStat();

    /** Is the 'Return to Sender' achievement */
    public static Achievement ghast = (new Achievement("achievement.ghast", "ghast", -4, 8, Items.ghast_tear, AchievementList.portal)).setSpecial().registerStat();

    /** Is the 'Into Fire' achievement */
    public static Achievement blazeRod = (new Achievement("achievement.blazeRod", "blazeRod", 0, 9, Items.blaze_rod, AchievementList.portal)).registerStat();

    /** Is the 'Local Brewery' achievement */
    public static Achievement potion = (new Achievement("achievement.potion", "potion", 2, 8, Items.potionitem, AchievementList.blazeRod)).registerStat();

    /** Is the 'The End?' achievement */
    public static Achievement theEnd = (new Achievement("achievement.theEnd", "theEnd", 3, 10, Items.ender_eye, AchievementList.blazeRod)).setSpecial().registerStat();

    /** Is the 'The End.' achievement */
    public static Achievement theEnd2 = (new Achievement("achievement.theEnd2", "theEnd2", 4, 13, Blocks.dragon_egg, AchievementList.theEnd)).setSpecial().registerStat();

    /** Is the 'Enchanter' achievement */
    public static Achievement enchantments = (new Achievement("achievement.enchantments", "enchantments", -4, 4, Blocks.enchanting_table, AchievementList.diamonds)).registerStat();
    public static Achievement overkill = (new Achievement("achievement.overkill", "overkill", -4, 1, Items.diamond_sword, AchievementList.enchantments)).setSpecial().registerStat();

    /** Is the 'Librarian' achievement */
    public static Achievement bookcase = (new Achievement("achievement.bookcase", "bookcase", -3, 6, Blocks.bookshelf, AchievementList.enchantments)).registerStat();

    /** Is the 'Repopulation' achievement */
    public static Achievement breedCow = (new Achievement("achievement.breedCow", "breedCow", 7, -5, Items.wheat, AchievementList.killCow)).registerStat();

    /** Is the 'The Beginning?' achievement */
    public static Achievement spawnWither = (new Achievement("achievement.spawnWither", "spawnWither", 7, 12, new ItemStack(Items.skull, 1, 1), AchievementList.theEnd2)).registerStat();

    /** Is the 'The Beginning.' achievement */
    public static Achievement killWither = (new Achievement("achievement.killWither", "killWither", 7, 10, Items.nether_star, AchievementList.spawnWither)).registerStat();

    /** Is the 'Beaconator' achievement */
    public static Achievement fullBeacon = (new Achievement("achievement.fullBeacon", "fullBeacon", 7, 8, Blocks.beacon, AchievementList.killWither)).setSpecial().registerStat();

    /** Is the 'Adventuring Time' achievement */
    public static Achievement exploreAllBiomes = (new Achievement("achievement.exploreAllBiomes", "exploreAllBiomes", 4, 8, Items.diamond_boots, AchievementList.theEnd)).func_150953_b(JsonSerializableSet.class).setSpecial().registerStat();
    public static Achievement overpowered = (new Achievement("achievement.overpowered", "overpowered", 6, 4, new ItemStack(Items.golden_apple, 1, 1), AchievementList.buildBetterPickaxe)).setSpecial().registerStat();

    /**
     * A stub functions called to make the static initializer for this class run.
     */
    public static void init()
    {
    }
}
