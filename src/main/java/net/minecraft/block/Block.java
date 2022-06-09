package net.minecraft.block;

import java.util.List;
import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryNamespacedDefaultedByKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Block
{
    /** ResourceLocation for the Air block */
    private static final ResourceLocation AIR_ID = new ResourceLocation("air");
    public static final RegistryNamespacedDefaultedByKey<ResourceLocation, Block> blockRegistry = new RegistryNamespacedDefaultedByKey(Block.AIR_ID);
    public static final ObjectIntIdentityMap BLOCK_STATE_IDS = new ObjectIntIdentityMap();
    private CreativeTabs displayOnCreativeTab;
    public static final Block.SoundType soundTypeStone = new Block.SoundType("stone", 1.0F, 1.0F);

    /** the wood sound type */
    public static final Block.SoundType soundTypeWood = new Block.SoundType("wood", 1.0F, 1.0F);

    /** the gravel sound type */
    public static final Block.SoundType soundTypeGravel = new Block.SoundType("gravel", 1.0F, 1.0F);
    public static final Block.SoundType soundTypeGrass = new Block.SoundType("grass", 1.0F, 1.0F);
    public static final Block.SoundType soundTypePiston = new Block.SoundType("stone", 1.0F, 1.0F);
    public static final Block.SoundType soundTypeMetal = new Block.SoundType("stone", 1.0F, 1.5F);
    public static final Block.SoundType soundTypeGlass = new Block.SoundType("stone", 1.0F, 1.0F)
    {
        public String getBreakSound()
        {
            return "dig.glass";
        }
        public String getPlaceSound()
        {
            return "step.stone";
        }
    };
    public static final Block.SoundType soundTypeCloth = new Block.SoundType("cloth", 1.0F, 1.0F);
    public static final Block.SoundType soundTypeSand = new Block.SoundType("sand", 1.0F, 1.0F);
    public static final Block.SoundType soundTypeSnow = new Block.SoundType("snow", 1.0F, 1.0F);
    public static final Block.SoundType soundTypeLadder = new Block.SoundType("ladder", 1.0F, 1.0F)
    {
        public String getBreakSound()
        {
            return "dig.wood";
        }
    };
    public static final Block.SoundType soundTypeAnvil = new Block.SoundType("anvil", 0.3F, 1.0F)
    {
        public String getBreakSound()
        {
            return "dig.stone";
        }
        public String getPlaceSound()
        {
            return "random.anvil_land";
        }
    };
    public static final Block.SoundType SLIME_SOUND = new Block.SoundType("slime", 1.0F, 1.0F)
    {
        public String getBreakSound()
        {
            return "mob.slime.big";
        }
        public String getPlaceSound()
        {
            return "mob.slime.big";
        }
        public String getStepSound()
        {
            return "mob.slime.small";
        }
    };
    protected boolean fullBlock;

    /** How much light is subtracted for going through this block */
    protected int lightOpacity;
    protected boolean translucent;

    /** Amount of light emitted */
    protected int lightValue;

    /**
     * Flag if block should use the brightest neighbor light value as its own
     */
    protected boolean useNeighborBrightness;

    /** Indicates how many hits it takes to break a block. */
    protected float blockHardness;

    /** Indicates how much this block can resist explosions */
    protected float blockResistance;
    protected boolean enableStats;

    /**
     * Flags whether or not this block is of a type that needs random ticking. Ref-counted by ExtendedBlockStorage in
     * order to broadly cull a chunk from the random chunk update list for efficiency's sake.
     */
    protected boolean needsRandomTick;

    /** true if the Block contains a Tile Entity */
    protected boolean isBlockContainer;
    protected double minX;
    protected double minY;
    protected double minZ;
    protected double maxX;
    protected double maxY;
    protected double maxZ;

    /** Sound of stepping on the block */
    public Block.SoundType stepSound;
    public float blockParticleGravity;
    protected final Material blockMaterial;
    protected final MapColor field_181083_K;

    /**
     * Determines how much velocity is maintained while moving on top of this block
     */
    public float slipperiness;
    protected final BlockState blockState;
    private IBlockState defaultBlockState;
    private String unlocalizedName;

    public static int getIdFromBlock(Block blockIn)
    {
        return Block.blockRegistry.getIDForObject(blockIn);
    }

    /**
     * Get a unique ID for the given BlockState, containing both BlockID and metadata
     */
    public static int getStateId(IBlockState state)
    {
        Block block = state.getBlock();
        return Block.getIdFromBlock(block) + (block.getMetaFromState(state) << 12);
    }

    public static Block getBlockById(int id)
    {
        return Block.blockRegistry.getObjectById(id);
    }

    /**
     * Get a BlockState by it's ID (see getStateId)
     */
    public static IBlockState getStateById(int id)
    {
        int i = id & 4095;
        int j = id >> 12 & 15;
        return Block.getBlockById(i).getStateFromMeta(j);
    }

    public static Block getBlockFromItem(Item itemIn)
    {
        return itemIn instanceof ItemBlock ? ((ItemBlock)itemIn).getBlock() : null;
    }

    public static Block getBlockFromName(String name)
    {
        ResourceLocation resourcelocation = new ResourceLocation(name);

        if (Block.blockRegistry.containsKey(resourcelocation))
        {
            return Block.blockRegistry.getObject(resourcelocation);
        }
        else
        {
            try
            {
                return Block.blockRegistry.getObjectById(Integer.parseInt(name));
            }
            catch (NumberFormatException var3)
            {
                return null;
            }
        }
    }

    public boolean isFullBlock()
    {
        return fullBlock;
    }

    public int getLightOpacity()
    {
        return lightOpacity;
    }

    /**
     * Used in the renderer to apply ambient occlusion
     */
    public boolean isTranslucent()
    {
        return translucent;
    }

    public int getLightValue()
    {
        return lightValue;
    }

    /**
     * Should block use the brightest neighbor light value as its own
     */
    public boolean getUseNeighborBrightness()
    {
        return useNeighborBrightness;
    }

    /**
     * Get a material of block
     */
    public Material getMaterial()
    {
        return blockMaterial;
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IBlockState state)
    {
        return field_181083_K;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState();
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        if (state != null && !state.getPropertyNames().isEmpty())
        {
            throw new IllegalArgumentException("Don't know how to convert " + state + " back into data...");
        }
        else
        {
            return 0;
        }
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state;
    }

    public Block(Material p_i46399_1_, MapColor p_i46399_2_)
    {
        enableStats = true;
        stepSound = Block.soundTypeStone;
        blockParticleGravity = 1.0F;
        slipperiness = 0.6F;
        blockMaterial = p_i46399_1_;
        field_181083_K = p_i46399_2_;
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        fullBlock = isOpaqueCube();
        lightOpacity = isOpaqueCube() ? 255 : 0;
        translucent = !p_i46399_1_.blocksLight();
        blockState = createBlockState();
        setDefaultState(blockState.getBaseState());
    }

    protected Block(Material materialIn)
    {
        this(materialIn, materialIn.getMaterialMapColor());
    }

    /**
     * Sets the footstep sound for the block. Returns the object for convenience in constructing.
     */
    protected Block setStepSound(Block.SoundType sound)
    {
        stepSound = sound;
        return this;
    }

    /**
     * Sets how much light is blocked going through this block. Returns the object for convenience in constructing.
     */
    protected Block setLightOpacity(int opacity)
    {
        lightOpacity = opacity;
        return this;
    }

    /**
     * Sets the light value that the block emits. Returns resulting block instance for constructing convenience. Args:
     * level
     */
    protected Block setLightLevel(float value)
    {
        lightValue = (int)(15.0F * value);
        return this;
    }

    /**
     * Sets the the blocks resistance to explosions. Returns the object for convenience in constructing.
     */
    protected Block setResistance(float resistance)
    {
        blockResistance = resistance * 3.0F;
        return this;
    }

    /**
     * Indicate if a material is a normal solid opaque cube
     */
    public boolean isBlockNormalCube()
    {
        return blockMaterial.blocksMovement() && isFullCube();
    }

    /**
     * Used for nearly all game logic (non-rendering) purposes. Use Forge-provided isNormalCube(IBlockAccess, BlockPos)
     * instead.
     */
    public boolean isNormalCube()
    {
        return blockMaterial.isOpaque() && isFullCube() && !canProvidePower();
    }

    public boolean isVisuallyOpaque()
    {
        return blockMaterial.blocksMovement() && isFullCube();
    }

    public boolean isFullCube()
    {
        return true;
    }

    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return !blockMaterial.blocksMovement();
    }

    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
    public int getRenderType()
    {
        return 3;
    }

    /**
     * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
     */
    public boolean isReplaceable(World worldIn, BlockPos pos)
    {
        return false;
    }

    /**
     * Sets how many hits it takes to break a block.
     */
    protected Block setHardness(float hardness)
    {
        blockHardness = hardness;

        if (blockResistance < hardness * 5.0F)
        {
            blockResistance = hardness * 5.0F;
        }

        return this;
    }

    protected Block setBlockUnbreakable()
    {
        setHardness(-1.0F);
        return this;
    }

    public float getBlockHardness(World worldIn, BlockPos pos)
    {
        return blockHardness;
    }

    /**
     * Sets whether this block type will receive random update ticks
     */
    protected Block setTickRandomly(boolean shouldTick)
    {
        needsRandomTick = shouldTick;
        return this;
    }

    /**
     * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
     * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
     */
    public boolean getTickRandomly()
    {
        return needsRandomTick;
    }

    public boolean hasTileEntity()
    {
        return isBlockContainer;
    }

    protected final void setBlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
    {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public int getMixedBrightnessForBlock(IBlockAccess worldIn, BlockPos pos)
    {
        Block block = worldIn.getBlockState(pos).getBlock();
        int i = worldIn.getCombinedLight(pos, block.getLightValue());

        if (i == 0 && block instanceof BlockSlab)
        {
            pos = pos.down();
            block = worldIn.getBlockState(pos).getBlock();
            return worldIn.getCombinedLight(pos, block.getLightValue());
        }
        else
        {
            return i;
        }
    }

    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return side == EnumFacing.DOWN && minY > 0.0D || (side == EnumFacing.UP && maxY < 1.0D || (side == EnumFacing.NORTH && minZ > 0.0D || (side == EnumFacing.SOUTH && maxZ < 1.0D || (side == EnumFacing.WEST && minX > 0.0D || (side == EnumFacing.EAST && maxX < 1.0D || !worldIn.getBlockState(pos).getBlock().isOpaqueCube())))));
    }

    /**
     * Whether this Block is solid on the given Side
     */
    public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return worldIn.getBlockState(pos).getBlock().getMaterial().isSolid();
    }

    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos)
    {
        return new AxisAlignedBB((double)pos.getX() + minX, (double)pos.getY() + minY, (double)pos.getZ() + minZ, (double)pos.getX() + maxX, (double)pos.getY() + maxY, (double)pos.getZ() + maxZ);
    }

    /**
     * Add all collision boxes of this Block to the list that intersect with the given mask.
     */
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
    {
        AxisAlignedBB axisalignedbb = getCollisionBoundingBox(worldIn, pos, state);

        if (axisalignedbb != null && mask.intersectsWith(axisalignedbb))
        {
            list.add(axisalignedbb);
        }
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        return new AxisAlignedBB((double)pos.getX() + minX, (double)pos.getY() + minY, (double)pos.getZ() + minZ, (double)pos.getX() + maxX, (double)pos.getY() + maxY, (double)pos.getZ() + maxZ);
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube()
    {
        return true;
    }

    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid)
    {
        return isCollidable();
    }

    /**
     * Returns if this block is collidable (only used by Fire). Args: x, y, z
     */
    public boolean isCollidable()
    {
        return true;
    }

    /**
     * Called randomly when setTickRandomly is set to true (used by e.g. crops to grow, etc.)
     */
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        updateTick(worldIn, pos, state, random);
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    }

    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    }

    /**
     * Called when a player destroys this Block
     */
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state)
    {
    }

    /**
     * Called when a neighboring block changes.
     */
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
    }

    /**
     * How many world ticks before ticking
     */
    public int tickRate(World worldIn)
    {
        return 10;
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random)
    {
        return 1;
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Item.getItemFromBlock(this);
    }

    /**
     * Get the hardness of this Block relative to the ability of the given player
     */
    public float getPlayerRelativeBlockHardness(EntityPlayer playerIn, World worldIn, BlockPos pos)
    {
        float f = getBlockHardness(worldIn, pos);
        return f < 0.0F ? 0.0F : (!playerIn.canHarvestBlock(this) ? playerIn.getToolDigEfficiency(this) / f / 100.0F : playerIn.getToolDigEfficiency(this) / f / 30.0F);
    }

    /**
     * Spawn this Block's drops into the World as EntityItems
     */
    public final void dropBlockAsItem(World worldIn, BlockPos pos, IBlockState state, int forture)
    {
        dropBlockAsItemWithChance(worldIn, pos, state, 1.0F, forture);
    }

    /**
     * Spawns this Block's drops into the World as EntityItems.
     */
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        if (!worldIn.isRemote)
        {
            int i = quantityDroppedWithBonus(fortune, worldIn.rand);

            for (int j = 0; j < i; ++j)
            {
                if (worldIn.rand.nextFloat() <= chance)
                {
                    Item item = getItemDropped(state, worldIn.rand, fortune);

                    if (item != null)
                    {
                        Block.spawnAsEntity(worldIn, pos, new ItemStack(item, 1, damageDropped(state)));
                    }
                }
            }
        }
    }

    /**
     * Spawns the given ItemStack as an EntityItem into the World at the given position
     */
    public static void spawnAsEntity(World worldIn, BlockPos pos, ItemStack stack)
    {
        if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops"))
        {
            float f = 0.5F;
            double d0 = (double)(worldIn.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            double d1 = (double)(worldIn.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            double d2 = (double)(worldIn.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            EntityItem entityitem = new EntityItem(worldIn, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
            entityitem.setDefaultPickupDelay();
            worldIn.spawnEntityInWorld(entityitem);
        }
    }

    /**
     * Spawns the given amount of experience into the World as XP orb entities
     */
    protected void dropXpOnBlockBreak(World worldIn, BlockPos pos, int amount)
    {
        if (!worldIn.isRemote)
        {
            while (amount > 0)
            {
                int i = EntityXPOrb.getXPSplit(amount);
                amount -= i;
                worldIn.spawnEntityInWorld(new EntityXPOrb(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, i));
            }
        }
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state)
    {
        return 0;
    }

    /**
     * Returns how much this block can resist explosions from the passed in entity.
     */
    public float getExplosionResistance(Entity exploder)
    {
        return blockResistance / 5.0F;
    }

    /**
     * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit.
     */
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end)
    {
        setBlockBoundsBasedOnState(worldIn, pos);
        start = start.addVector(-pos.getX(), -pos.getY(), -pos.getZ());
        end = end.addVector(-pos.getX(), -pos.getY(), -pos.getZ());
        Vec3 vec3 = start.getIntermediateWithXValue(end, minX);
        Vec3 vec31 = start.getIntermediateWithXValue(end, maxX);
        Vec3 vec32 = start.getIntermediateWithYValue(end, minY);
        Vec3 vec33 = start.getIntermediateWithYValue(end, maxY);
        Vec3 vec34 = start.getIntermediateWithZValue(end, minZ);
        Vec3 vec35 = start.getIntermediateWithZValue(end, maxZ);

        if (!isVecInsideYZBounds(vec3))
        {
            vec3 = null;
        }

        if (!isVecInsideYZBounds(vec31))
        {
            vec31 = null;
        }

        if (!isVecInsideXZBounds(vec32))
        {
            vec32 = null;
        }

        if (!isVecInsideXZBounds(vec33))
        {
            vec33 = null;
        }

        if (!isVecInsideXYBounds(vec34))
        {
            vec34 = null;
        }

        if (!isVecInsideXYBounds(vec35))
        {
            vec35 = null;
        }

        Vec3 vec36 = null;

        if (vec3 != null && (vec36 == null || start.squareDistanceTo(vec3) < start.squareDistanceTo(vec36)))
        {
            vec36 = vec3;
        }

        if (vec31 != null && (vec36 == null || start.squareDistanceTo(vec31) < start.squareDistanceTo(vec36)))
        {
            vec36 = vec31;
        }

        if (vec32 != null && (vec36 == null || start.squareDistanceTo(vec32) < start.squareDistanceTo(vec36)))
        {
            vec36 = vec32;
        }

        if (vec33 != null && (vec36 == null || start.squareDistanceTo(vec33) < start.squareDistanceTo(vec36)))
        {
            vec36 = vec33;
        }

        if (vec34 != null && (vec36 == null || start.squareDistanceTo(vec34) < start.squareDistanceTo(vec36)))
        {
            vec36 = vec34;
        }

        if (vec35 != null && (vec36 == null || start.squareDistanceTo(vec35) < start.squareDistanceTo(vec36)))
        {
            vec36 = vec35;
        }

        if (vec36 == null)
        {
            return null;
        }
        else
        {
            EnumFacing enumfacing = null;

            if (vec36 == vec3)
            {
                enumfacing = EnumFacing.WEST;
            }

            if (vec36 == vec31)
            {
                enumfacing = EnumFacing.EAST;
            }

            if (vec36 == vec32)
            {
                enumfacing = EnumFacing.DOWN;
            }

            if (vec36 == vec33)
            {
                enumfacing = EnumFacing.UP;
            }

            if (vec36 == vec34)
            {
                enumfacing = EnumFacing.NORTH;
            }

            if (vec36 == vec35)
            {
                enumfacing = EnumFacing.SOUTH;
            }

            return new MovingObjectPosition(vec36.addVector(pos.getX(), pos.getY(), pos.getZ()), enumfacing, pos);
        }
    }

    /**
     * Checks if a vector is within the Y and Z bounds of the block.
     */
    private boolean isVecInsideYZBounds(Vec3 point)
    {
        return point != null && point.yCoord >= minY && point.yCoord <= maxY && point.zCoord >= minZ && point.zCoord <= maxZ;
    }

    /**
     * Checks if a vector is within the X and Z bounds of the block.
     */
    private boolean isVecInsideXZBounds(Vec3 point)
    {
        return point != null && point.xCoord >= minX && point.xCoord <= maxX && point.zCoord >= minZ && point.zCoord <= maxZ;
    }

    /**
     * Checks if a vector is within the X and Y bounds of the block.
     */
    private boolean isVecInsideXYBounds(Vec3 point)
    {
        return point != null && point.xCoord >= minX && point.xCoord <= maxX && point.yCoord >= minY && point.yCoord <= maxY;
    }

    /**
     * Called when this Block is destroyed by an Explosion
     */
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn)
    {
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.SOLID;
    }

    public boolean canReplace(World worldIn, BlockPos pos, EnumFacing side, ItemStack stack)
    {
        return canPlaceBlockOnSide(worldIn, pos, side);
    }

    /**
     * Check whether this Block can be placed on the given side
     */
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side)
    {
        return canPlaceBlockAt(worldIn, pos);
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos).getBlock().blockMaterial.isReplaceable();
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    /**
     * Triggered whenever an entity collides with this block (enters into the block)
     */
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn)
    {
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return getStateFromMeta(meta);
    }

    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
    }

    public Vec3 modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3 motion)
    {
        return motion;
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
    }

    /**
     * returns the block bounderies minX value
     */
    public final double getBlockBoundsMinX()
    {
        return minX;
    }

    /**
     * returns the block bounderies maxX value
     */
    public final double getBlockBoundsMaxX()
    {
        return maxX;
    }

    /**
     * returns the block bounderies minY value
     */
    public final double getBlockBoundsMinY()
    {
        return minY;
    }

    /**
     * returns the block bounderies maxY value
     */
    public final double getBlockBoundsMaxY()
    {
        return maxY;
    }

    /**
     * returns the block bounderies minZ value
     */
    public final double getBlockBoundsMinZ()
    {
        return minZ;
    }

    /**
     * returns the block bounderies maxZ value
     */
    public final double getBlockBoundsMaxZ()
    {
        return maxZ;
    }

    public int getBlockColor()
    {
        return 16777215;
    }

    public int getRenderColor(IBlockState state)
    {
        return 16777215;
    }

    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass)
    {
        return 16777215;
    }

    public final int colorMultiplier(IBlockAccess worldIn, BlockPos pos)
    {
        return colorMultiplier(worldIn, pos, 0);
    }

    public int getWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side)
    {
        return 0;
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower()
    {
        return false;
    }

    /**
     * Called When an Entity Collided with the Block
     */
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
    }

    public int getStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side)
    {
        return 0;
    }

    /**
     * Sets the block's bounds for rendering it as an item
     */
    public void setBlockBoundsForItemRender()
    {
    }

    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te)
    {
        player.triggerAchievement(StatList.mineBlockStatArray[Block.getIdFromBlock(this)]);
        player.addExhaustion(0.025F);

        if (canSilkHarvest() && EnchantmentHelper.getSilkTouchModifier(player))
        {
            ItemStack itemstack = createStackedBlock(state);

            if (itemstack != null)
            {
                Block.spawnAsEntity(worldIn, pos, itemstack);
            }
        }
        else
        {
            int i = EnchantmentHelper.getFortuneModifier(player);
            dropBlockAsItem(worldIn, pos, state, i);
        }
    }

    protected boolean canSilkHarvest()
    {
        return isFullCube() && !isBlockContainer;
    }

    protected ItemStack createStackedBlock(IBlockState state)
    {
        int i = 0;
        Item item = Item.getItemFromBlock(this);

        if (item != null && item.getHasSubtypes())
        {
            i = getMetaFromState(state);
        }

        return new ItemStack(item, 1, i);
    }

    /**
     * Get the quantity dropped based on the given fortune level
     */
    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        return quantityDropped(random);
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
    }

    public boolean func_181623_g()
    {
        return !blockMaterial.isSolid() && !blockMaterial.isLiquid();
    }

    public Block setUnlocalizedName(String name)
    {
        unlocalizedName = name;
        return this;
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName()
    {
        return StatCollector.translateToLocal(getUnlocalizedName() + ".name");
    }

    /**
     * Returns the unlocalized name of the block with "tile." appended to the front.
     */
    public String getUnlocalizedName()
    {
        return "tile." + unlocalizedName;
    }

    /**
     * Called on both Client and Server when World#addBlockEvent is called
     */
    public boolean onBlockEventReceived(World worldIn, BlockPos pos, IBlockState state, int eventID, int eventParam)
    {
        return false;
    }

    /**
     * Return the state of blocks statistics flags - if the block is counted for mined and placed.
     */
    public boolean getEnableStats()
    {
        return enableStats;
    }

    protected Block disableStats()
    {
        enableStats = false;
        return this;
    }

    public int getMobilityFlag()
    {
        return blockMaterial.getMaterialMobility();
    }

    /**
     * Returns the default ambient occlusion value based on block opacity
     */
    public float getAmbientOcclusionLightValue()
    {
        return isBlockNormalCube() ? 0.2F : 1.0F;
    }

    /**
     * Block's chance to react to a living entity falling on it.
     */
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance)
    {
        entityIn.fall(fallDistance, 1.0F);
    }

    /**
     * Called when an Entity lands on this Block. This method *must* update motionY because the entity will not do that
     * on its own
     */
    public void onLanded(World worldIn, Entity entityIn)
    {
        entityIn.motionY = 0.0D;
    }

    public Item getItem(World worldIn, BlockPos pos)
    {
        return Item.getItemFromBlock(this);
    }

    public int getDamageValue(World worldIn, BlockPos pos)
    {
        return damageDropped(worldIn.getBlockState(pos));
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        list.add(new ItemStack(itemIn, 1, 0));
    }

    /**
     * Returns the CreativeTab to display the given block on.
     */
    public CreativeTabs getCreativeTabToDisplayOn()
    {
        return displayOnCreativeTab;
    }

    public Block setCreativeTab(CreativeTabs tab)
    {
        displayOnCreativeTab = tab;
        return this;
    }

    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player)
    {
    }

    /**
     * Called similar to random ticks, but only when it is raining.
     */
    public void fillWithRain(World worldIn, BlockPos pos)
    {
    }

    /**
     * Returns true only if block is flowerPot
     */
    public boolean isFlowerPot()
    {
        return false;
    }

    public boolean requiresUpdates()
    {
        return true;
    }

    /**
     * Return whether this block can drop from an explosion.
     */
    public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return true;
    }

    public boolean isAssociatedBlock(Block other)
    {
        return this == other;
    }

    public static boolean isEqualTo(Block blockIn, Block other)
    {
        return blockIn != null && other != null && (blockIn == other || blockIn.isAssociatedBlock(other));
    }

    public boolean hasComparatorInputOverride()
    {
        return false;
    }

    public int getComparatorInputOverride(World worldIn, BlockPos pos)
    {
        return 0;
    }

    /**
     * Possibly modify the given BlockState before rendering it on an Entity (Minecarts, Endermen, ...)
     */
    public IBlockState getStateForEntityRender(IBlockState state)
    {
        return state;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this);
    }

    public BlockState getBlockState()
    {
        return blockState;
    }

    protected final void setDefaultState(IBlockState state)
    {
        defaultBlockState = state;
    }

    public final IBlockState getDefaultState()
    {
        return defaultBlockState;
    }

    /**
     * Get the OffsetType for this Block. Determines if the model is rendered slightly offset.
     */
    public Block.EnumOffsetType getOffsetType()
    {
        return Block.EnumOffsetType.NONE;
    }

    public String toString()
    {
        return "Block{" + Block.blockRegistry.getNameForObject(this) + "}";
    }

    public static void registerBlocks()
    {
        Block.registerBlock(0, Block.AIR_ID, (new BlockAir()).setUnlocalizedName("air"));
        Block.registerBlock(1, "stone", (new BlockStone()).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("stone"));
        Block.registerBlock(2, "grass", (new BlockGrass()).setHardness(0.6F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("grass"));
        Block.registerBlock(3, "dirt", (new BlockDirt()).setHardness(0.5F).setStepSound(Block.soundTypeGravel).setUnlocalizedName("dirt"));
        Block block = (new Block(Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("stonebrick").setCreativeTab(CreativeTabs.tabBlock);
        Block.registerBlock(4, "cobblestone", block);
        Block block1 = (new BlockPlanks()).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("wood");
        Block.registerBlock(5, "planks", block1);
        Block.registerBlock(6, "sapling", (new BlockSapling()).setHardness(0.0F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("sapling"));
        Block.registerBlock(7, "bedrock", (new Block(Material.rock)).setBlockUnbreakable().setResistance(6000000.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("bedrock").disableStats().setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(8, "flowing_water", (new BlockDynamicLiquid(Material.water)).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("water").disableStats());
        Block.registerBlock(9, "water", (new BlockStaticLiquid(Material.water)).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("water").disableStats());
        Block.registerBlock(10, "flowing_lava", (new BlockDynamicLiquid(Material.lava)).setHardness(100.0F).setLightLevel(1.0F).setUnlocalizedName("lava").disableStats());
        Block.registerBlock(11, "lava", (new BlockStaticLiquid(Material.lava)).setHardness(100.0F).setLightLevel(1.0F).setUnlocalizedName("lava").disableStats());
        Block.registerBlock(12, "sand", (new BlockSand()).setHardness(0.5F).setStepSound(Block.soundTypeSand).setUnlocalizedName("sand"));
        Block.registerBlock(13, "gravel", (new BlockGravel()).setHardness(0.6F).setStepSound(Block.soundTypeGravel).setUnlocalizedName("gravel"));
        Block.registerBlock(14, "gold_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("oreGold"));
        Block.registerBlock(15, "iron_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("oreIron"));
        Block.registerBlock(16, "coal_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("oreCoal"));
        Block.registerBlock(17, "log", (new BlockOldLog()).setUnlocalizedName("log"));
        Block.registerBlock(18, "leaves", (new BlockOldLeaf()).setUnlocalizedName("leaves"));
        Block.registerBlock(19, "sponge", (new BlockSponge()).setHardness(0.6F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("sponge"));
        Block.registerBlock(20, "glass", (new BlockGlass(Material.glass, false)).setHardness(0.3F).setStepSound(Block.soundTypeGlass).setUnlocalizedName("glass"));
        Block.registerBlock(21, "lapis_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("oreLapis"));
        Block.registerBlock(22, "lapis_block", (new Block(Material.iron, MapColor.lapisColor)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("blockLapis").setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(23, "dispenser", (new BlockDispenser()).setHardness(3.5F).setStepSound(Block.soundTypePiston).setUnlocalizedName("dispenser"));
        Block block2 = (new BlockSandStone()).setStepSound(Block.soundTypePiston).setHardness(0.8F).setUnlocalizedName("sandStone");
        Block.registerBlock(24, "sandstone", block2);
        Block.registerBlock(25, "noteblock", (new BlockNote()).setHardness(0.8F).setUnlocalizedName("musicBlock"));
        Block.registerBlock(26, "bed", (new BlockBed()).setStepSound(Block.soundTypeWood).setHardness(0.2F).setUnlocalizedName("bed").disableStats());
        Block.registerBlock(27, "golden_rail", (new BlockRailPowered()).setHardness(0.7F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("goldenRail"));
        Block.registerBlock(28, "detector_rail", (new BlockRailDetector()).setHardness(0.7F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("detectorRail"));
        Block.registerBlock(29, "sticky_piston", (new BlockPistonBase(true)).setUnlocalizedName("pistonStickyBase"));
        Block.registerBlock(30, "web", (new BlockWeb()).setLightOpacity(1).setHardness(4.0F).setUnlocalizedName("web"));
        Block.registerBlock(31, "tallgrass", (new BlockTallGrass()).setHardness(0.0F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("tallgrass"));
        Block.registerBlock(32, "deadbush", (new BlockDeadBush()).setHardness(0.0F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("deadbush"));
        Block.registerBlock(33, "piston", (new BlockPistonBase(false)).setUnlocalizedName("pistonBase"));
        Block.registerBlock(34, "piston_head", (new BlockPistonExtension()).setUnlocalizedName("pistonBase"));
        Block.registerBlock(35, "wool", (new BlockColored(Material.cloth)).setHardness(0.8F).setStepSound(Block.soundTypeCloth).setUnlocalizedName("cloth"));
        Block.registerBlock(36, "piston_extension", new BlockPistonMoving());
        Block.registerBlock(37, "yellow_flower", (new BlockYellowFlower()).setHardness(0.0F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("flower1"));
        Block.registerBlock(38, "red_flower", (new BlockRedFlower()).setHardness(0.0F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("flower2"));
        Block block3 = (new BlockMushroom()).setHardness(0.0F).setStepSound(Block.soundTypeGrass).setLightLevel(0.125F).setUnlocalizedName("mushroom");
        Block.registerBlock(39, "brown_mushroom", block3);
        Block block4 = (new BlockMushroom()).setHardness(0.0F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("mushroom");
        Block.registerBlock(40, "red_mushroom", block4);
        Block.registerBlock(41, "gold_block", (new Block(Material.iron, MapColor.goldColor)).setHardness(3.0F).setResistance(10.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("blockGold").setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(42, "iron_block", (new Block(Material.iron, MapColor.ironColor)).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("blockIron").setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(43, "double_stone_slab", (new BlockDoubleStoneSlab()).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("stoneSlab"));
        Block.registerBlock(44, "stone_slab", (new BlockHalfStoneSlab()).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("stoneSlab"));
        Block block5 = (new Block(Material.rock, MapColor.redColor)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("brick").setCreativeTab(CreativeTabs.tabBlock);
        Block.registerBlock(45, "brick_block", block5);
        Block.registerBlock(46, "tnt", (new BlockTNT()).setHardness(0.0F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("tnt"));
        Block.registerBlock(47, "bookshelf", (new BlockBookshelf()).setHardness(1.5F).setStepSound(Block.soundTypeWood).setUnlocalizedName("bookshelf"));
        Block.registerBlock(48, "mossy_cobblestone", (new Block(Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("stoneMoss").setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(49, "obsidian", (new BlockObsidian()).setHardness(50.0F).setResistance(2000.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("obsidian"));
        Block.registerBlock(50, "torch", (new BlockTorch()).setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setUnlocalizedName("torch"));
        Block.registerBlock(51, "fire", (new BlockFire()).setHardness(0.0F).setLightLevel(1.0F).setStepSound(Block.soundTypeCloth).setUnlocalizedName("fire").disableStats());
        Block.registerBlock(52, "mob_spawner", (new BlockMobSpawner()).setHardness(5.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("mobSpawner").disableStats());
        Block.registerBlock(53, "oak_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.OAK))).setUnlocalizedName("stairsWood"));
        Block.registerBlock(54, "chest", (new BlockChest(0)).setHardness(2.5F).setStepSound(Block.soundTypeWood).setUnlocalizedName("chest"));
        Block.registerBlock(55, "redstone_wire", (new BlockRedstoneWire()).setHardness(0.0F).setStepSound(Block.soundTypeStone).setUnlocalizedName("redstoneDust").disableStats());
        Block.registerBlock(56, "diamond_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("oreDiamond"));
        Block.registerBlock(57, "diamond_block", (new Block(Material.iron, MapColor.diamondColor)).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("blockDiamond").setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(58, "crafting_table", (new BlockWorkbench()).setHardness(2.5F).setStepSound(Block.soundTypeWood).setUnlocalizedName("workbench"));
        Block.registerBlock(59, "wheat", (new BlockCrops()).setUnlocalizedName("crops"));
        Block block6 = (new BlockFarmland()).setHardness(0.6F).setStepSound(Block.soundTypeGravel).setUnlocalizedName("farmland");
        Block.registerBlock(60, "farmland", block6);
        Block.registerBlock(61, "furnace", (new BlockFurnace(false)).setHardness(3.5F).setStepSound(Block.soundTypePiston).setUnlocalizedName("furnace").setCreativeTab(CreativeTabs.tabDecorations));
        Block.registerBlock(62, "lit_furnace", (new BlockFurnace(true)).setHardness(3.5F).setStepSound(Block.soundTypePiston).setLightLevel(0.875F).setUnlocalizedName("furnace"));
        Block.registerBlock(63, "standing_sign", (new BlockStandingSign()).setHardness(1.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("sign").disableStats());
        Block.registerBlock(64, "wooden_door", (new BlockDoor(Material.wood)).setHardness(3.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("doorOak").disableStats());
        Block.registerBlock(65, "ladder", (new BlockLadder()).setHardness(0.4F).setStepSound(Block.soundTypeLadder).setUnlocalizedName("ladder"));
        Block.registerBlock(66, "rail", (new BlockRail()).setHardness(0.7F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("rail"));
        Block.registerBlock(67, "stone_stairs", (new BlockStairs(block.getDefaultState())).setUnlocalizedName("stairsStone"));
        Block.registerBlock(68, "wall_sign", (new BlockWallSign()).setHardness(1.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("sign").disableStats());
        Block.registerBlock(69, "lever", (new BlockLever()).setHardness(0.5F).setStepSound(Block.soundTypeWood).setUnlocalizedName("lever"));
        Block.registerBlock(70, "stone_pressure_plate", (new BlockPressurePlate(Material.rock, BlockPressurePlate.Sensitivity.MOBS)).setHardness(0.5F).setStepSound(Block.soundTypePiston).setUnlocalizedName("pressurePlateStone"));
        Block.registerBlock(71, "iron_door", (new BlockDoor(Material.iron)).setHardness(5.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("doorIron").disableStats());
        Block.registerBlock(72, "wooden_pressure_plate", (new BlockPressurePlate(Material.wood, BlockPressurePlate.Sensitivity.EVERYTHING)).setHardness(0.5F).setStepSound(Block.soundTypeWood).setUnlocalizedName("pressurePlateWood"));
        Block.registerBlock(73, "redstone_ore", (new BlockRedstoneOre(false)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("oreRedstone").setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(74, "lit_redstone_ore", (new BlockRedstoneOre(true)).setLightLevel(0.625F).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("oreRedstone"));
        Block.registerBlock(75, "unlit_redstone_torch", (new BlockRedstoneTorch(false)).setHardness(0.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("notGate"));
        Block.registerBlock(76, "redstone_torch", (new BlockRedstoneTorch(true)).setHardness(0.0F).setLightLevel(0.5F).setStepSound(Block.soundTypeWood).setUnlocalizedName("notGate").setCreativeTab(CreativeTabs.tabRedstone));
        Block.registerBlock(77, "stone_button", (new BlockButtonStone()).setHardness(0.5F).setStepSound(Block.soundTypePiston).setUnlocalizedName("button"));
        Block.registerBlock(78, "snow_layer", (new BlockSnow()).setHardness(0.1F).setStepSound(Block.soundTypeSnow).setUnlocalizedName("snow").setLightOpacity(0));
        Block.registerBlock(79, "ice", (new BlockIce()).setHardness(0.5F).setLightOpacity(3).setStepSound(Block.soundTypeGlass).setUnlocalizedName("ice"));
        Block.registerBlock(80, "snow", (new BlockSnowBlock()).setHardness(0.2F).setStepSound(Block.soundTypeSnow).setUnlocalizedName("snow"));
        Block.registerBlock(81, "cactus", (new BlockCactus()).setHardness(0.4F).setStepSound(Block.soundTypeCloth).setUnlocalizedName("cactus"));
        Block.registerBlock(82, "clay", (new BlockClay()).setHardness(0.6F).setStepSound(Block.soundTypeGravel).setUnlocalizedName("clay"));
        Block.registerBlock(83, "reeds", (new BlockReed()).setHardness(0.0F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("reeds").disableStats());
        Block.registerBlock(84, "jukebox", (new BlockJukebox()).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("jukebox"));
        Block.registerBlock(85, "fence", (new BlockFence(Material.wood, BlockPlanks.EnumType.OAK.func_181070_c())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("fence"));
        Block block7 = (new BlockPumpkin()).setHardness(1.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("pumpkin");
        Block.registerBlock(86, "pumpkin", block7);
        Block.registerBlock(87, "netherrack", (new BlockNetherrack()).setHardness(0.4F).setStepSound(Block.soundTypePiston).setUnlocalizedName("hellrock"));
        Block.registerBlock(88, "soul_sand", (new BlockSoulSand()).setHardness(0.5F).setStepSound(Block.soundTypeSand).setUnlocalizedName("hellsand"));
        Block.registerBlock(89, "glowstone", (new BlockGlowstone(Material.glass)).setHardness(0.3F).setStepSound(Block.soundTypeGlass).setLightLevel(1.0F).setUnlocalizedName("lightgem"));
        Block.registerBlock(90, "portal", (new BlockPortal()).setHardness(-1.0F).setStepSound(Block.soundTypeGlass).setLightLevel(0.75F).setUnlocalizedName("portal"));
        Block.registerBlock(91, "lit_pumpkin", (new BlockPumpkin()).setHardness(1.0F).setStepSound(Block.soundTypeWood).setLightLevel(1.0F).setUnlocalizedName("litpumpkin"));
        Block.registerBlock(92, "cake", (new BlockCake()).setHardness(0.5F).setStepSound(Block.soundTypeCloth).setUnlocalizedName("cake").disableStats());
        Block.registerBlock(93, "unpowered_repeater", (new BlockRedstoneRepeater(false)).setHardness(0.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("diode").disableStats());
        Block.registerBlock(94, "powered_repeater", (new BlockRedstoneRepeater(true)).setHardness(0.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("diode").disableStats());
        Block.registerBlock(95, "stained_glass", (new BlockStainedGlass(Material.glass)).setHardness(0.3F).setStepSound(Block.soundTypeGlass).setUnlocalizedName("stainedGlass"));
        Block.registerBlock(96, "trapdoor", (new BlockTrapDoor(Material.wood)).setHardness(3.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("trapdoor").disableStats());
        Block.registerBlock(97, "monster_egg", (new BlockSilverfish()).setHardness(0.75F).setUnlocalizedName("monsterStoneEgg"));
        Block block8 = (new BlockStoneBrick()).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("stonebricksmooth");
        Block.registerBlock(98, "stonebrick", block8);
        Block.registerBlock(99, "brown_mushroom_block", (new BlockHugeMushroom(Material.wood, MapColor.dirtColor, block3)).setHardness(0.2F).setStepSound(Block.soundTypeWood).setUnlocalizedName("mushroom"));
        Block.registerBlock(100, "red_mushroom_block", (new BlockHugeMushroom(Material.wood, MapColor.redColor, block4)).setHardness(0.2F).setStepSound(Block.soundTypeWood).setUnlocalizedName("mushroom"));
        Block.registerBlock(101, "iron_bars", (new BlockPane(Material.iron, true)).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("fenceIron"));
        Block.registerBlock(102, "glass_pane", (new BlockPane(Material.glass, false)).setHardness(0.3F).setStepSound(Block.soundTypeGlass).setUnlocalizedName("thinGlass"));
        Block block9 = (new BlockMelon()).setHardness(1.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("melon");
        Block.registerBlock(103, "melon_block", block9);
        Block.registerBlock(104, "pumpkin_stem", (new BlockStem(block7)).setHardness(0.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("pumpkinStem"));
        Block.registerBlock(105, "melon_stem", (new BlockStem(block9)).setHardness(0.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("pumpkinStem"));
        Block.registerBlock(106, "vine", (new BlockVine()).setHardness(0.2F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("vine"));
        Block.registerBlock(107, "fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.OAK)).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("fenceGate"));
        Block.registerBlock(108, "brick_stairs", (new BlockStairs(block5.getDefaultState())).setUnlocalizedName("stairsBrick"));
        Block.registerBlock(109, "stone_brick_stairs", (new BlockStairs(block8.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.DEFAULT))).setUnlocalizedName("stairsStoneBrickSmooth"));
        Block.registerBlock(110, "mycelium", (new BlockMycelium()).setHardness(0.6F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("mycel"));
        Block.registerBlock(111, "waterlily", (new BlockLilyPad()).setHardness(0.0F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("waterlily"));
        Block block10 = (new BlockNetherBrick()).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("netherBrick").setCreativeTab(CreativeTabs.tabBlock);
        Block.registerBlock(112, "nether_brick", block10);
        Block.registerBlock(113, "nether_brick_fence", (new BlockFence(Material.rock, MapColor.netherrackColor)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("netherFence"));
        Block.registerBlock(114, "nether_brick_stairs", (new BlockStairs(block10.getDefaultState())).setUnlocalizedName("stairsNetherBrick"));
        Block.registerBlock(115, "nether_wart", (new BlockNetherWart()).setUnlocalizedName("netherStalk"));
        Block.registerBlock(116, "enchanting_table", (new BlockEnchantmentTable()).setHardness(5.0F).setResistance(2000.0F).setUnlocalizedName("enchantmentTable"));
        Block.registerBlock(117, "brewing_stand", (new BlockBrewingStand()).setHardness(0.5F).setLightLevel(0.125F).setUnlocalizedName("brewingStand"));
        Block.registerBlock(118, "cauldron", (new BlockCauldron()).setHardness(2.0F).setUnlocalizedName("cauldron"));
        Block.registerBlock(119, "end_portal", (new BlockEndPortal(Material.portal)).setHardness(-1.0F).setResistance(6000000.0F));
        Block.registerBlock(120, "end_portal_frame", (new BlockEndPortalFrame()).setStepSound(Block.soundTypeGlass).setLightLevel(0.125F).setHardness(-1.0F).setUnlocalizedName("endPortalFrame").setResistance(6000000.0F).setCreativeTab(CreativeTabs.tabDecorations));
        Block.registerBlock(121, "end_stone", (new Block(Material.rock, MapColor.sandColor)).setHardness(3.0F).setResistance(15.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("whiteStone").setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(122, "dragon_egg", (new BlockDragonEgg()).setHardness(3.0F).setResistance(15.0F).setStepSound(Block.soundTypePiston).setLightLevel(0.125F).setUnlocalizedName("dragonEgg"));
        Block.registerBlock(123, "redstone_lamp", (new BlockRedstoneLight(false)).setHardness(0.3F).setStepSound(Block.soundTypeGlass).setUnlocalizedName("redstoneLight").setCreativeTab(CreativeTabs.tabRedstone));
        Block.registerBlock(124, "lit_redstone_lamp", (new BlockRedstoneLight(true)).setHardness(0.3F).setStepSound(Block.soundTypeGlass).setUnlocalizedName("redstoneLight"));
        Block.registerBlock(125, "double_wooden_slab", (new BlockDoubleWoodSlab()).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("woodSlab"));
        Block.registerBlock(126, "wooden_slab", (new BlockHalfWoodSlab()).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("woodSlab"));
        Block.registerBlock(127, "cocoa", (new BlockCocoa()).setHardness(0.2F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("cocoa"));
        Block.registerBlock(128, "sandstone_stairs", (new BlockStairs(block2.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH))).setUnlocalizedName("stairsSandStone"));
        Block.registerBlock(129, "emerald_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("oreEmerald"));
        Block.registerBlock(130, "ender_chest", (new BlockEnderChest()).setHardness(22.5F).setResistance(1000.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("enderChest").setLightLevel(0.5F));
        Block.registerBlock(131, "tripwire_hook", (new BlockTripWireHook()).setUnlocalizedName("tripWireSource"));
        Block.registerBlock(132, "tripwire", (new BlockTripWire()).setUnlocalizedName("tripWire"));
        Block.registerBlock(133, "emerald_block", (new Block(Material.iron, MapColor.emeraldColor)).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("blockEmerald").setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(134, "spruce_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE))).setUnlocalizedName("stairsWoodSpruce"));
        Block.registerBlock(135, "birch_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.BIRCH))).setUnlocalizedName("stairsWoodBirch"));
        Block.registerBlock(136, "jungle_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.JUNGLE))).setUnlocalizedName("stairsWoodJungle"));
        Block.registerBlock(137, "command_block", (new BlockCommandBlock()).setBlockUnbreakable().setResistance(6000000.0F).setUnlocalizedName("commandBlock"));
        Block.registerBlock(138, "beacon", (new BlockBeacon()).setUnlocalizedName("beacon").setLightLevel(1.0F));
        Block.registerBlock(139, "cobblestone_wall", (new BlockWall(block)).setUnlocalizedName("cobbleWall"));
        Block.registerBlock(140, "flower_pot", (new BlockFlowerPot()).setHardness(0.0F).setStepSound(Block.soundTypeStone).setUnlocalizedName("flowerPot"));
        Block.registerBlock(141, "carrots", (new BlockCarrot()).setUnlocalizedName("carrots"));
        Block.registerBlock(142, "potatoes", (new BlockPotato()).setUnlocalizedName("potatoes"));
        Block.registerBlock(143, "wooden_button", (new BlockButtonWood()).setHardness(0.5F).setStepSound(Block.soundTypeWood).setUnlocalizedName("button"));
        Block.registerBlock(144, "skull", (new BlockSkull()).setHardness(1.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("skull"));
        Block.registerBlock(145, "anvil", (new BlockAnvil()).setHardness(5.0F).setStepSound(Block.soundTypeAnvil).setResistance(2000.0F).setUnlocalizedName("anvil"));
        Block.registerBlock(146, "trapped_chest", (new BlockChest(1)).setHardness(2.5F).setStepSound(Block.soundTypeWood).setUnlocalizedName("chestTrap"));
        Block.registerBlock(147, "light_weighted_pressure_plate", (new BlockPressurePlateWeighted(Material.iron, 15, MapColor.goldColor)).setHardness(0.5F).setStepSound(Block.soundTypeWood).setUnlocalizedName("weightedPlate_light"));
        Block.registerBlock(148, "heavy_weighted_pressure_plate", (new BlockPressurePlateWeighted(Material.iron, 150)).setHardness(0.5F).setStepSound(Block.soundTypeWood).setUnlocalizedName("weightedPlate_heavy"));
        Block.registerBlock(149, "unpowered_comparator", (new BlockRedstoneComparator(false)).setHardness(0.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("comparator").disableStats());
        Block.registerBlock(150, "powered_comparator", (new BlockRedstoneComparator(true)).setHardness(0.0F).setLightLevel(0.625F).setStepSound(Block.soundTypeWood).setUnlocalizedName("comparator").disableStats());
        Block.registerBlock(151, "daylight_detector", new BlockDaylightDetector(false));
        Block.registerBlock(152, "redstone_block", (new BlockCompressedPowered(Material.iron, MapColor.tntColor)).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("blockRedstone").setCreativeTab(CreativeTabs.tabRedstone));
        Block.registerBlock(153, "quartz_ore", (new BlockOre(MapColor.netherrackColor)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("netherquartz"));
        Block.registerBlock(154, "hopper", (new BlockHopper()).setHardness(3.0F).setResistance(8.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("hopper"));
        Block block11 = (new BlockQuartz()).setStepSound(Block.soundTypePiston).setHardness(0.8F).setUnlocalizedName("quartzBlock");
        Block.registerBlock(155, "quartz_block", block11);
        Block.registerBlock(156, "quartz_stairs", (new BlockStairs(block11.getDefaultState().withProperty(BlockQuartz.VARIANT, BlockQuartz.EnumType.DEFAULT))).setUnlocalizedName("stairsQuartz"));
        Block.registerBlock(157, "activator_rail", (new BlockRailPowered()).setHardness(0.7F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("activatorRail"));
        Block.registerBlock(158, "dropper", (new BlockDropper()).setHardness(3.5F).setStepSound(Block.soundTypePiston).setUnlocalizedName("dropper"));
        Block.registerBlock(159, "stained_hardened_clay", (new BlockColored(Material.rock)).setHardness(1.25F).setResistance(7.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("clayHardenedStained"));
        Block.registerBlock(160, "stained_glass_pane", (new BlockStainedGlassPane()).setHardness(0.3F).setStepSound(Block.soundTypeGlass).setUnlocalizedName("thinStainedGlass"));
        Block.registerBlock(161, "leaves2", (new BlockNewLeaf()).setUnlocalizedName("leaves"));
        Block.registerBlock(162, "log2", (new BlockNewLog()).setUnlocalizedName("log"));
        Block.registerBlock(163, "acacia_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.ACACIA))).setUnlocalizedName("stairsWoodAcacia"));
        Block.registerBlock(164, "dark_oak_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.DARK_OAK))).setUnlocalizedName("stairsWoodDarkOak"));
        Block.registerBlock(165, "slime", (new BlockSlime()).setUnlocalizedName("slime").setStepSound(Block.SLIME_SOUND));
        Block.registerBlock(166, "barrier", (new BlockBarrier()).setUnlocalizedName("barrier"));
        Block.registerBlock(167, "iron_trapdoor", (new BlockTrapDoor(Material.iron)).setHardness(5.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("ironTrapdoor").disableStats());
        Block.registerBlock(168, "prismarine", (new BlockPrismarine()).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("prismarine"));
        Block.registerBlock(169, "sea_lantern", (new BlockSeaLantern(Material.glass)).setHardness(0.3F).setStepSound(Block.soundTypeGlass).setLightLevel(1.0F).setUnlocalizedName("seaLantern"));
        Block.registerBlock(170, "hay_block", (new BlockHay()).setHardness(0.5F).setStepSound(Block.soundTypeGrass).setUnlocalizedName("hayBlock").setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(171, "carpet", (new BlockCarpet()).setHardness(0.1F).setStepSound(Block.soundTypeCloth).setUnlocalizedName("woolCarpet").setLightOpacity(0));
        Block.registerBlock(172, "hardened_clay", (new BlockHardenedClay()).setHardness(1.25F).setResistance(7.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("clayHardened"));
        Block.registerBlock(173, "coal_block", (new Block(Material.rock, MapColor.blackColor)).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("blockCoal").setCreativeTab(CreativeTabs.tabBlock));
        Block.registerBlock(174, "packed_ice", (new BlockPackedIce()).setHardness(0.5F).setStepSound(Block.soundTypeGlass).setUnlocalizedName("icePacked"));
        Block.registerBlock(175, "double_plant", new BlockDoublePlant());
        Block.registerBlock(176, "standing_banner", (new BlockBanner.BlockBannerStanding()).setHardness(1.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("banner").disableStats());
        Block.registerBlock(177, "wall_banner", (new BlockBanner.BlockBannerHanging()).setHardness(1.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("banner").disableStats());
        Block.registerBlock(178, "daylight_detector_inverted", new BlockDaylightDetector(true));
        Block block12 = (new BlockRedSandstone()).setStepSound(Block.soundTypePiston).setHardness(0.8F).setUnlocalizedName("redSandStone");
        Block.registerBlock(179, "red_sandstone", block12);
        Block.registerBlock(180, "red_sandstone_stairs", (new BlockStairs(block12.getDefaultState().withProperty(BlockRedSandstone.TYPE, BlockRedSandstone.EnumType.SMOOTH))).setUnlocalizedName("stairsRedSandStone"));
        Block.registerBlock(181, "double_stone_slab2", (new BlockDoubleStoneSlabNew()).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("stoneSlab2"));
        Block.registerBlock(182, "stone_slab2", (new BlockHalfStoneSlabNew()).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypePiston).setUnlocalizedName("stoneSlab2"));
        Block.registerBlock(183, "spruce_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.SPRUCE)).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("spruceFenceGate"));
        Block.registerBlock(184, "birch_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.BIRCH)).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("birchFenceGate"));
        Block.registerBlock(185, "jungle_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.JUNGLE)).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("jungleFenceGate"));
        Block.registerBlock(186, "dark_oak_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.DARK_OAK)).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("darkOakFenceGate"));
        Block.registerBlock(187, "acacia_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.ACACIA)).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("acaciaFenceGate"));
        Block.registerBlock(188, "spruce_fence", (new BlockFence(Material.wood, BlockPlanks.EnumType.SPRUCE.func_181070_c())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("spruceFence"));
        Block.registerBlock(189, "birch_fence", (new BlockFence(Material.wood, BlockPlanks.EnumType.BIRCH.func_181070_c())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("birchFence"));
        Block.registerBlock(190, "jungle_fence", (new BlockFence(Material.wood, BlockPlanks.EnumType.JUNGLE.func_181070_c())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("jungleFence"));
        Block.registerBlock(191, "dark_oak_fence", (new BlockFence(Material.wood, BlockPlanks.EnumType.DARK_OAK.func_181070_c())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("darkOakFence"));
        Block.registerBlock(192, "acacia_fence", (new BlockFence(Material.wood, BlockPlanks.EnumType.ACACIA.func_181070_c())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("acaciaFence"));
        Block.registerBlock(193, "spruce_door", (new BlockDoor(Material.wood)).setHardness(3.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("doorSpruce").disableStats());
        Block.registerBlock(194, "birch_door", (new BlockDoor(Material.wood)).setHardness(3.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("doorBirch").disableStats());
        Block.registerBlock(195, "jungle_door", (new BlockDoor(Material.wood)).setHardness(3.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("doorJungle").disableStats());
        Block.registerBlock(196, "acacia_door", (new BlockDoor(Material.wood)).setHardness(3.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("doorAcacia").disableStats());
        Block.registerBlock(197, "dark_oak_door", (new BlockDoor(Material.wood)).setHardness(3.0F).setStepSound(Block.soundTypeWood).setUnlocalizedName("doorDarkOak").disableStats());
        Block.blockRegistry.validateKey();

        for (Block block13 : Block.blockRegistry)
        {
            if (block13.blockMaterial == Material.air)
            {
                block13.useNeighborBrightness = false;
            }
            else
            {
                boolean flag = false;
                boolean flag1 = block13 instanceof BlockStairs;
                boolean flag2 = block13 instanceof BlockSlab;
                boolean flag3 = block13 == block6;
                boolean flag4 = block13.translucent;
                boolean flag5 = block13.lightOpacity == 0;

                if (flag1 || flag2 || flag3 || flag4 || flag5)
                {
                    flag = true;
                }

                block13.useNeighborBrightness = flag;
            }
        }

        for (Block block14 : Block.blockRegistry)
        {
            for (IBlockState iblockstate : block14.getBlockState().getValidStates())
            {
                int i = Block.blockRegistry.getIDForObject(block14) << 4 | block14.getMetaFromState(iblockstate);
                Block.BLOCK_STATE_IDS.put(iblockstate, i);
            }
        }
    }

    private static void registerBlock(int id, ResourceLocation textualID, Block block_)
    {
        Block.blockRegistry.register(id, textualID, block_);
    }

    private static void registerBlock(int id, String textualID, Block block_)
    {
        Block.registerBlock(id, new ResourceLocation(textualID), block_);
    }

    public static enum EnumOffsetType
    {
        NONE,
        XZ,
        XYZ
    }

    public static class SoundType
    {
        public final String soundName;
        public final float volume;
        public final float frequency;

        public SoundType(String name, float volume, float frequency)
        {
            soundName = name;
            this.volume = volume;
            this.frequency = frequency;
        }

        public float getVolume()
        {
            return volume;
        }

        public float getFrequency()
        {
            return frequency;
        }

        public String getBreakSound()
        {
            return "dig." + soundName;
        }

        public String getStepSound()
        {
            return "step." + soundName;
        }

        public String getPlaceSound()
        {
            return getBreakSound();
        }
    }
}
