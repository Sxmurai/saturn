package net.minecraft.entity.projectile;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityFishHook extends Entity
{
    private static final List<WeightedRandomFishable> JUNK = Arrays.asList((new WeightedRandomFishable(new ItemStack(Items.leather_boots), 10)).setMaxDamagePercent(0.9F), new WeightedRandomFishable(new ItemStack(Items.leather), 10), new WeightedRandomFishable(new ItemStack(Items.bone), 10), new WeightedRandomFishable(new ItemStack(Items.potionitem), 10), new WeightedRandomFishable(new ItemStack(Items.string), 5), (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 2)).setMaxDamagePercent(0.9F), new WeightedRandomFishable(new ItemStack(Items.bowl), 10), new WeightedRandomFishable(new ItemStack(Items.stick), 5), new WeightedRandomFishable(new ItemStack(Items.dye, 10, EnumDyeColor.BLACK.getDyeDamage()), 1), new WeightedRandomFishable(new ItemStack(Blocks.tripwire_hook), 10), new WeightedRandomFishable(new ItemStack(Items.rotten_flesh), 10));
    private static final List<WeightedRandomFishable> TREASURE = Arrays.asList(new WeightedRandomFishable(new ItemStack(Blocks.waterlily), 1), new WeightedRandomFishable(new ItemStack(Items.name_tag), 1), new WeightedRandomFishable(new ItemStack(Items.saddle), 1), (new WeightedRandomFishable(new ItemStack(Items.bow), 1)).setMaxDamagePercent(0.25F).setEnchantable(), (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 1)).setMaxDamagePercent(0.25F).setEnchantable(), (new WeightedRandomFishable(new ItemStack(Items.book), 1)).setEnchantable());
    private static final List<WeightedRandomFishable> FISH = Arrays.asList(new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.COD.getMetadata()), 60), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.SALMON.getMetadata()), 25), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.CLOWNFISH.getMetadata()), 2), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.PUFFERFISH.getMetadata()), 13));
    private int xTile;
    private int yTile;
    private int zTile;
    private Block inTile;
    private boolean inGround;
    public int shake;
    public EntityPlayer angler;
    private int ticksInGround;
    private int ticksInAir;
    private int ticksCatchable;
    private int ticksCaughtDelay;
    private int ticksCatchableDelay;
    private float fishApproachAngle;
    public Entity caughtEntity;
    private int fishPosRotationIncrements;
    private double fishX;
    private double fishY;
    private double fishZ;
    private double fishYaw;
    private double fishPitch;
    private double clientMotionX;
    private double clientMotionY;
    private double clientMotionZ;

    public static List<WeightedRandomFishable> func_174855_j()
    {
        return EntityFishHook.FISH;
    }

    public EntityFishHook(World worldIn)
    {
        super(worldIn);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        setSize(0.25F, 0.25F);
        ignoreFrustumCheck = true;
    }

    public EntityFishHook(World worldIn, double x, double y, double z, EntityPlayer anglerIn)
    {
        this(worldIn);
        setPosition(x, y, z);
        ignoreFrustumCheck = true;
        angler = anglerIn;
        anglerIn.fishEntity = this;
    }

    public EntityFishHook(World worldIn, EntityPlayer fishingPlayer)
    {
        super(worldIn);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        ignoreFrustumCheck = true;
        angler = fishingPlayer;
        angler.fishEntity = this;
        setSize(0.25F, 0.25F);
        setLocationAndAngles(fishingPlayer.posX, fishingPlayer.posY + (double)fishingPlayer.getEyeHeight(), fishingPlayer.posZ, fishingPlayer.rotationYaw, fishingPlayer.rotationPitch);
        posX -= MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * 0.16F;
        posY -= 0.10000000149011612D;
        posZ -= MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * 0.16F;
        setPosition(posX, posY, posZ);
        float f = 0.4F;
        motionX = -MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI) * f;
        motionZ = MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI) * f;
        motionY = -MathHelper.sin(rotationPitch / 180.0F * (float)Math.PI) * f;
        handleHookCasting(motionX, motionY, motionZ, 1.5F, 1.0F);
    }

    protected void entityInit()
    {
    }

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     */
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(d0))
        {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    public void handleHookCasting(double p_146035_1_, double p_146035_3_, double p_146035_5_, float p_146035_7_, float p_146035_8_)
    {
        float f = MathHelper.sqrt_double(p_146035_1_ * p_146035_1_ + p_146035_3_ * p_146035_3_ + p_146035_5_ * p_146035_5_);
        p_146035_1_ = p_146035_1_ / (double)f;
        p_146035_3_ = p_146035_3_ / (double)f;
        p_146035_5_ = p_146035_5_ / (double)f;
        p_146035_1_ = p_146035_1_ + rand.nextGaussian() * 0.007499999832361937D * (double)p_146035_8_;
        p_146035_3_ = p_146035_3_ + rand.nextGaussian() * 0.007499999832361937D * (double)p_146035_8_;
        p_146035_5_ = p_146035_5_ + rand.nextGaussian() * 0.007499999832361937D * (double)p_146035_8_;
        p_146035_1_ = p_146035_1_ * (double)p_146035_7_;
        p_146035_3_ = p_146035_3_ * (double)p_146035_7_;
        p_146035_5_ = p_146035_5_ * (double)p_146035_7_;
        motionX = p_146035_1_;
        motionY = p_146035_3_;
        motionZ = p_146035_5_;
        float f1 = MathHelper.sqrt_double(p_146035_1_ * p_146035_1_ + p_146035_5_ * p_146035_5_);
        prevRotationYaw = rotationYaw = (float)(MathHelper.func_181159_b(p_146035_1_, p_146035_5_) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float)(MathHelper.func_181159_b(p_146035_3_, f1) * 180.0D / Math.PI);
        ticksInGround = 0;
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_)
    {
        fishX = x;
        fishY = y;
        fishZ = z;
        fishYaw = yaw;
        fishPitch = pitch;
        fishPosRotationIncrements = posRotationIncrements;
        motionX = clientMotionX;
        motionY = clientMotionY;
        motionZ = clientMotionZ;
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double x, double y, double z)
    {
        clientMotionX = motionX = x;
        clientMotionY = motionY = y;
        clientMotionZ = motionZ = z;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (fishPosRotationIncrements > 0)
        {
            double d7 = posX + (fishX - posX) / (double) fishPosRotationIncrements;
            double d8 = posY + (fishY - posY) / (double) fishPosRotationIncrements;
            double d9 = posZ + (fishZ - posZ) / (double) fishPosRotationIncrements;
            double d1 = MathHelper.wrapAngleTo180_double(fishYaw - (double) rotationYaw);
            rotationYaw = (float)((double) rotationYaw + d1 / (double) fishPosRotationIncrements);
            rotationPitch = (float)((double) rotationPitch + (fishPitch - (double) rotationPitch) / (double) fishPosRotationIncrements);
            --fishPosRotationIncrements;
            setPosition(d7, d8, d9);
            setRotation(rotationYaw, rotationPitch);
        }
        else
        {
            if (!worldObj.isRemote)
            {
                ItemStack itemstack = angler.getCurrentEquippedItem();

                if (angler.isDead || !angler.isEntityAlive() || itemstack == null || itemstack.getItem() != Items.fishing_rod || getDistanceSqToEntity(angler) > 1024.0D)
                {
                    setDead();
                    angler.fishEntity = null;
                    return;
                }

                if (caughtEntity != null)
                {
                    if (!caughtEntity.isDead)
                    {
                        posX = caughtEntity.posX;
                        double d17 = caughtEntity.height;
                        posY = caughtEntity.getEntityBoundingBox().minY + d17 * 0.8D;
                        posZ = caughtEntity.posZ;
                        return;
                    }

                    caughtEntity = null;
                }
            }

            if (shake > 0)
            {
                --shake;
            }

            if (inGround)
            {
                if (worldObj.getBlockState(new BlockPos(xTile, yTile, zTile)).getBlock() == inTile)
                {
                    ++ticksInGround;

                    if (ticksInGround == 1200)
                    {
                        setDead();
                    }

                    return;
                }

                inGround = false;
                motionX *= rand.nextFloat() * 0.2F;
                motionY *= rand.nextFloat() * 0.2F;
                motionZ *= rand.nextFloat() * 0.2F;
                ticksInGround = 0;
                ticksInAir = 0;
            }
            else
            {
                ++ticksInAir;
            }

            Vec3 vec31 = new Vec3(posX, posY, posZ);
            Vec3 vec3 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
            MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks(vec31, vec3);
            vec31 = new Vec3(posX, posY, posZ);
            vec3 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

            if (movingobjectposition != null)
            {
                vec3 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity1 = list.get(i);

                if (entity1.canBeCollidedWith() && (entity1 != angler || ticksInAir >= 5))
                {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f, f, f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec31, vec3);

                    if (movingobjectposition1 != null)
                    {
                        double d2 = vec31.squareDistanceTo(movingobjectposition1.hitVec);

                        if (d2 < d0 || d0 == 0.0D)
                        {
                            entity = entity1;
                            d0 = d2;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null)
            {
                if (movingobjectposition.entityHit != null)
                {
                    if (movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, angler), 0.0F))
                    {
                        caughtEntity = movingobjectposition.entityHit;
                    }
                }
                else
                {
                    inGround = true;
                }
            }

            if (!inGround)
            {
                moveEntity(motionX, motionY, motionZ);
                float f5 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
                rotationYaw = (float)(MathHelper.func_181159_b(motionX, motionZ) * 180.0D / Math.PI);

                for (rotationPitch = (float)(MathHelper.func_181159_b(motionY, f5) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F)
                {
                }

                while (rotationPitch - prevRotationPitch >= 180.0F)
                {
                    prevRotationPitch += 360.0F;
                }

                while (rotationYaw - prevRotationYaw < -180.0F)
                {
                    prevRotationYaw -= 360.0F;
                }

                while (rotationYaw - prevRotationYaw >= 180.0F)
                {
                    prevRotationYaw += 360.0F;
                }

                rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
                rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
                float f6 = 0.92F;

                if (onGround || isCollidedHorizontally)
                {
                    f6 = 0.5F;
                }

                int j = 5;
                double d10 = 0.0D;

                for (int k = 0; k < j; ++k)
                {
                    AxisAlignedBB axisalignedbb1 = getEntityBoundingBox();
                    double d3 = axisalignedbb1.maxY - axisalignedbb1.minY;
                    double d4 = axisalignedbb1.minY + d3 * (double)k / (double)j;
                    double d5 = axisalignedbb1.minY + d3 * (double)(k + 1) / (double)j;
                    AxisAlignedBB axisalignedbb2 = new AxisAlignedBB(axisalignedbb1.minX, d4, axisalignedbb1.minZ, axisalignedbb1.maxX, d5, axisalignedbb1.maxZ);

                    if (worldObj.isAABBInMaterial(axisalignedbb2, Material.water))
                    {
                        d10 += 1.0D / (double)j;
                    }
                }

                if (!worldObj.isRemote && d10 > 0.0D)
                {
                    WorldServer worldserver = (WorldServer) worldObj;
                    int l = 1;
                    BlockPos blockpos = (new BlockPos(this)).up();

                    if (rand.nextFloat() < 0.25F && worldObj.canLightningStrike(blockpos))
                    {
                        l = 2;
                    }

                    if (rand.nextFloat() < 0.5F && !worldObj.canSeeSky(blockpos))
                    {
                        --l;
                    }

                    if (ticksCatchable > 0)
                    {
                        --ticksCatchable;

                        if (ticksCatchable <= 0)
                        {
                            ticksCaughtDelay = 0;
                            ticksCatchableDelay = 0;
                        }
                    }
                    else if (ticksCatchableDelay > 0)
                    {
                        ticksCatchableDelay -= l;

                        if (ticksCatchableDelay <= 0)
                        {
                            motionY -= 0.20000000298023224D;
                            playSound("random.splash", 0.25F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                            float f8 = (float)MathHelper.floor_double(getEntityBoundingBox().minY);
                            worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX, f8 + 1.0F, posZ, (int)(1.0F + width * 20.0F), width, 0.0D, width, 0.20000000298023224D);
                            worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, posX, f8 + 1.0F, posZ, (int)(1.0F + width * 20.0F), width, 0.0D, width, 0.20000000298023224D);
                            ticksCatchable = MathHelper.getRandomIntegerInRange(rand, 10, 30);
                        }
                        else
                        {
                            fishApproachAngle = (float)((double) fishApproachAngle + rand.nextGaussian() * 4.0D);
                            float f7 = fishApproachAngle * 0.017453292F;
                            float f10 = MathHelper.sin(f7);
                            float f11 = MathHelper.cos(f7);
                            double d13 = posX + (double)(f10 * (float) ticksCatchableDelay * 0.1F);
                            double d15 = (float)MathHelper.floor_double(getEntityBoundingBox().minY) + 1.0F;
                            double d16 = posZ + (double)(f11 * (float) ticksCatchableDelay * 0.1F);
                            Block block1 = worldserver.getBlockState(new BlockPos((int)d13, (int)d15 - 1, (int)d16)).getBlock();

                            if (block1 == Blocks.water || block1 == Blocks.flowing_water)
                            {
                                if (rand.nextFloat() < 0.15F)
                                {
                                    worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, d13, d15 - 0.10000000149011612D, d16, 1, f10, 0.1D, f11, 0.0D);
                                }

                                float f3 = f10 * 0.04F;
                                float f4 = f11 * 0.04F;
                                worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, d13, d15, d16, 0, f4, 0.01D, -f3, 1.0D);
                                worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, d13, d15, d16, 0, -f4, 0.01D, f3, 1.0D);
                            }
                        }
                    }
                    else if (ticksCaughtDelay > 0)
                    {
                        ticksCaughtDelay -= l;
                        float f1 = 0.15F;

                        if (ticksCaughtDelay < 20)
                        {
                            f1 = (float)((double)f1 + (double)(20 - ticksCaughtDelay) * 0.05D);
                        }
                        else if (ticksCaughtDelay < 40)
                        {
                            f1 = (float)((double)f1 + (double)(40 - ticksCaughtDelay) * 0.02D);
                        }
                        else if (ticksCaughtDelay < 60)
                        {
                            f1 = (float)((double)f1 + (double)(60 - ticksCaughtDelay) * 0.01D);
                        }

                        if (rand.nextFloat() < f1)
                        {
                            float f9 = MathHelper.randomFloatClamp(rand, 0.0F, 360.0F) * 0.017453292F;
                            float f2 = MathHelper.randomFloatClamp(rand, 25.0F, 60.0F);
                            double d12 = posX + (double)(MathHelper.sin(f9) * f2 * 0.1F);
                            double d14 = (float)MathHelper.floor_double(getEntityBoundingBox().minY) + 1.0F;
                            double d6 = posZ + (double)(MathHelper.cos(f9) * f2 * 0.1F);
                            Block block = worldserver.getBlockState(new BlockPos((int)d12, (int)d14 - 1, (int)d6)).getBlock();

                            if (block == Blocks.water || block == Blocks.flowing_water)
                            {
                                worldserver.spawnParticle(EnumParticleTypes.WATER_SPLASH, d12, d14, d6, 2 + rand.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D);
                            }
                        }

                        if (ticksCaughtDelay <= 0)
                        {
                            fishApproachAngle = MathHelper.randomFloatClamp(rand, 0.0F, 360.0F);
                            ticksCatchableDelay = MathHelper.getRandomIntegerInRange(rand, 20, 80);
                        }
                    }
                    else
                    {
                        ticksCaughtDelay = MathHelper.getRandomIntegerInRange(rand, 100, 900);
                        ticksCaughtDelay -= EnchantmentHelper.getLureModifier(angler) * 20 * 5;
                    }

                    if (ticksCatchable > 0)
                    {
                        motionY -= (double)(rand.nextFloat() * rand.nextFloat() * rand.nextFloat()) * 0.2D;
                    }
                }

                double d11 = d10 * 2.0D - 1.0D;
                motionY += 0.03999999910593033D * d11;

                if (d10 > 0.0D)
                {
                    f6 = (float)((double)f6 * 0.9D);
                    motionY *= 0.8D;
                }

                motionX *= f6;
                motionY *= f6;
                motionZ *= f6;
                setPosition(posX, posY, posZ);
            }
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setShort("xTile", (short) xTile);
        tagCompound.setShort("yTile", (short) yTile);
        tagCompound.setShort("zTile", (short) zTile);
        ResourceLocation resourcelocation = Block.blockRegistry.getNameForObject(inTile);
        tagCompound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
        tagCompound.setByte("shake", (byte) shake);
        tagCompound.setByte("inGround", (byte)(inGround ? 1 : 0));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        xTile = tagCompund.getShort("xTile");
        yTile = tagCompund.getShort("yTile");
        zTile = tagCompund.getShort("zTile");

        if (tagCompund.hasKey("inTile", 8))
        {
            inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
        }
        else
        {
            inTile = Block.getBlockById(tagCompund.getByte("inTile") & 255);
        }

        shake = tagCompund.getByte("shake") & 255;
        inGround = tagCompund.getByte("inGround") == 1;
    }

    public int handleHookRetraction()
    {
        if (worldObj.isRemote)
        {
            return 0;
        }
        else
        {
            int i = 0;

            if (caughtEntity != null)
            {
                double d0 = angler.posX - posX;
                double d2 = angler.posY - posY;
                double d4 = angler.posZ - posZ;
                double d6 = MathHelper.sqrt_double(d0 * d0 + d2 * d2 + d4 * d4);
                double d8 = 0.1D;
                caughtEntity.motionX += d0 * d8;
                caughtEntity.motionY += d2 * d8 + (double)MathHelper.sqrt_double(d6) * 0.08D;
                caughtEntity.motionZ += d4 * d8;
                i = 3;
            }
            else if (ticksCatchable > 0)
            {
                EntityItem entityitem = new EntityItem(worldObj, posX, posY, posZ, getFishingResult());
                double d1 = angler.posX - posX;
                double d3 = angler.posY - posY;
                double d5 = angler.posZ - posZ;
                double d7 = MathHelper.sqrt_double(d1 * d1 + d3 * d3 + d5 * d5);
                double d9 = 0.1D;
                entityitem.motionX = d1 * d9;
                entityitem.motionY = d3 * d9 + (double)MathHelper.sqrt_double(d7) * 0.08D;
                entityitem.motionZ = d5 * d9;
                worldObj.spawnEntityInWorld(entityitem);
                angler.worldObj.spawnEntityInWorld(new EntityXPOrb(angler.worldObj, angler.posX, angler.posY + 0.5D, angler.posZ + 0.5D, rand.nextInt(6) + 1));
                i = 1;
            }

            if (inGround)
            {
                i = 2;
            }

            setDead();
            angler.fishEntity = null;
            return i;
        }
    }

    private ItemStack getFishingResult()
    {
        float f = worldObj.rand.nextFloat();
        int i = EnchantmentHelper.getLuckOfSeaModifier(angler);
        int j = EnchantmentHelper.getLureModifier(angler);
        float f1 = 0.1F - (float)i * 0.025F - (float)j * 0.01F;
        float f2 = 0.05F + (float)i * 0.01F - (float)j * 0.01F;
        f1 = MathHelper.clamp_float(f1, 0.0F, 1.0F);
        f2 = MathHelper.clamp_float(f2, 0.0F, 1.0F);

        if (f < f1)
        {
            angler.triggerAchievement(StatList.junkFishedStat);
            return WeightedRandom.getRandomItem(rand, EntityFishHook.JUNK).getItemStack(rand);
        }
        else
        {
            f = f - f1;

            if (f < f2)
            {
                angler.triggerAchievement(StatList.treasureFishedStat);
                return WeightedRandom.getRandomItem(rand, EntityFishHook.TREASURE).getItemStack(rand);
            }
            else
            {
                float f3 = f - f2;
                angler.triggerAchievement(StatList.fishCaughtStat);
                return WeightedRandom.getRandomItem(rand, EntityFishHook.FISH).getItemStack(rand);
            }
        }
    }

    /**
     * Will get destroyed next tick.
     */
    public void setDead()
    {
        super.setDead();

        if (angler != null)
        {
            angler.fishEntity = null;
        }
    }
}
