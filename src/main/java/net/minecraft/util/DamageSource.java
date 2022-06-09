package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.world.Explosion;

public class DamageSource
{
    public static DamageSource inFire = (new DamageSource("inFire")).setFireDamage();
    public static DamageSource lightningBolt = new DamageSource("lightningBolt");
    public static DamageSource onFire = (new DamageSource("onFire")).setDamageBypassesArmor().setFireDamage();
    public static DamageSource lava = (new DamageSource("lava")).setFireDamage();
    public static DamageSource inWall = (new DamageSource("inWall")).setDamageBypassesArmor();
    public static DamageSource drown = (new DamageSource("drown")).setDamageBypassesArmor();
    public static DamageSource starve = (new DamageSource("starve")).setDamageBypassesArmor().setDamageIsAbsolute();
    public static DamageSource cactus = new DamageSource("cactus");
    public static DamageSource fall = (new DamageSource("fall")).setDamageBypassesArmor();
    public static DamageSource outOfWorld = (new DamageSource("outOfWorld")).setDamageBypassesArmor().setDamageAllowedInCreativeMode();
    public static DamageSource generic = (new DamageSource("generic")).setDamageBypassesArmor();
    public static DamageSource magic = (new DamageSource("magic")).setDamageBypassesArmor().setMagicDamage();
    public static DamageSource wither = (new DamageSource("wither")).setDamageBypassesArmor();
    public static DamageSource anvil = new DamageSource("anvil");
    public static DamageSource fallingBlock = new DamageSource("fallingBlock");

    /** This kind of damage can be blocked or not. */
    private boolean isUnblockable;
    private boolean isDamageAllowedInCreativeMode;

    /**
     * Whether or not the damage ignores modification by potion effects or enchantments.
     */
    private boolean damageIsAbsolute;
    private float hungerDamage = 0.3F;

    /** This kind of damage is based on fire or not. */
    private boolean fireDamage;

    /** This kind of damage is based on a projectile or not. */
    private boolean projectile;

    /**
     * Whether this damage source will have its damage amount scaled based on the current difficulty.
     */
    private boolean difficultyScaled;

    /** Whether the damage is magic based. */
    private boolean magicDamage;
    private boolean explosion;
    public String damageType;

    public static DamageSource causeMobDamage(EntityLivingBase mob)
    {
        return new EntityDamageSource("mob", mob);
    }

    /**
     * returns an EntityDamageSource of type player
     */
    public static DamageSource causePlayerDamage(EntityPlayer player)
    {
        return new EntityDamageSource("player", player);
    }

    /**
     * returns EntityDamageSourceIndirect of an arrow
     */
    public static DamageSource causeArrowDamage(EntityArrow arrow, Entity p_76353_1_)
    {
        return (new EntityDamageSourceIndirect("arrow", arrow, p_76353_1_)).setProjectile();
    }

    /**
     * returns EntityDamageSourceIndirect of a fireball
     */
    public static DamageSource causeFireballDamage(EntityFireball fireball, Entity p_76362_1_)
    {
        return p_76362_1_ == null ? (new EntityDamageSourceIndirect("onFire", fireball, fireball)).setFireDamage().setProjectile() : (new EntityDamageSourceIndirect("fireball", fireball, p_76362_1_)).setFireDamage().setProjectile();
    }

    public static DamageSource causeThrownDamage(Entity p_76356_0_, Entity p_76356_1_)
    {
        return (new EntityDamageSourceIndirect("thrown", p_76356_0_, p_76356_1_)).setProjectile();
    }

    public static DamageSource causeIndirectMagicDamage(Entity p_76354_0_, Entity p_76354_1_)
    {
        return (new EntityDamageSourceIndirect("indirectMagic", p_76354_0_, p_76354_1_)).setDamageBypassesArmor().setMagicDamage();
    }

    /**
     * Returns the EntityDamageSource of the Thorns enchantment
     */
    public static DamageSource causeThornsDamage(Entity p_92087_0_)
    {
        return (new EntityDamageSource("thorns", p_92087_0_)).setIsThornsDamage().setMagicDamage();
    }

    public static DamageSource setExplosionSource(Explosion explosionIn)
    {
        return explosionIn != null && explosionIn.getExplosivePlacedBy() != null ? (new EntityDamageSource("explosion.player", explosionIn.getExplosivePlacedBy())).setDifficultyScaled().setExplosion() : (new DamageSource("explosion")).setDifficultyScaled().setExplosion();
    }

    /**
     * Returns true if the damage is projectile based.
     */
    public boolean isProjectile()
    {
        return projectile;
    }

    /**
     * Define the damage type as projectile based.
     */
    public DamageSource setProjectile()
    {
        projectile = true;
        return this;
    }

    public boolean isExplosion()
    {
        return explosion;
    }

    public DamageSource setExplosion()
    {
        explosion = true;
        return this;
    }

    public boolean isUnblockable()
    {
        return isUnblockable;
    }

    /**
     * How much satiate(food) is consumed by this DamageSource
     */
    public float getHungerDamage()
    {
        return hungerDamage;
    }

    public boolean canHarmInCreative()
    {
        return isDamageAllowedInCreativeMode;
    }

    /**
     * Whether or not the damage ignores modification by potion effects or enchantments.
     */
    public boolean isDamageAbsolute()
    {
        return damageIsAbsolute;
    }

    protected DamageSource(String damageTypeIn)
    {
        damageType = damageTypeIn;
    }

    public Entity getSourceOfDamage()
    {
        return getEntity();
    }

    public Entity getEntity()
    {
        return null;
    }

    protected DamageSource setDamageBypassesArmor()
    {
        isUnblockable = true;
        hungerDamage = 0.0F;
        return this;
    }

    protected DamageSource setDamageAllowedInCreativeMode()
    {
        isDamageAllowedInCreativeMode = true;
        return this;
    }

    /**
     * Sets a value indicating whether the damage is absolute (ignores modification by potion effects or enchantments),
     * and also clears out hunger damage.
     */
    protected DamageSource setDamageIsAbsolute()
    {
        damageIsAbsolute = true;
        hungerDamage = 0.0F;
        return this;
    }

    /**
     * Define the damage type as fire based.
     */
    protected DamageSource setFireDamage()
    {
        fireDamage = true;
        return this;
    }

    /**
     * Gets the death message that is displayed when the player dies
     */
    public IChatComponent getDeathMessage(EntityLivingBase p_151519_1_)
    {
        EntityLivingBase entitylivingbase = p_151519_1_.func_94060_bK();
        String s = "death.attack." + damageType;
        String s1 = s + ".player";
        return entitylivingbase != null && StatCollector.canTranslate(s1) ? new ChatComponentTranslation(s1, p_151519_1_.getDisplayName(), entitylivingbase.getDisplayName()): new ChatComponentTranslation(s, p_151519_1_.getDisplayName());
    }

    /**
     * Returns true if the damage is fire based.
     */
    public boolean isFireDamage()
    {
        return fireDamage;
    }

    /**
     * Return the name of damage type.
     */
    public String getDamageType()
    {
        return damageType;
    }

    /**
     * Set whether this damage source will have its damage amount scaled based on the current difficulty.
     */
    public DamageSource setDifficultyScaled()
    {
        difficultyScaled = true;
        return this;
    }

    /**
     * Return whether this damage source will have its damage amount scaled based on the current difficulty.
     */
    public boolean isDifficultyScaled()
    {
        return difficultyScaled;
    }

    /**
     * Returns true if the damage is magic based.
     */
    public boolean isMagicDamage()
    {
        return magicDamage;
    }

    /**
     * Define the damage type as magic based.
     */
    public DamageSource setMagicDamage()
    {
        magicDamage = true;
        return this;
    }

    public boolean isCreativePlayer()
    {
        Entity entity = getEntity();
        return entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode;
    }
}
