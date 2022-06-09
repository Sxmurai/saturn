package net.minecraft.client.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityOtherPlayerMP extends AbstractClientPlayer
{
    private boolean isItemInUse;
    private int otherPlayerMPPosRotationIncrements;
    private double otherPlayerMPX;
    private double otherPlayerMPY;
    private double otherPlayerMPZ;
    private double otherPlayerMPYaw;
    private double otherPlayerMPPitch;

    public EntityOtherPlayerMP(World worldIn, GameProfile gameProfileIn)
    {
        super(worldIn, gameProfileIn);
        stepHeight = 0.0F;
        noClip = true;
        renderOffsetY = 0.25F;
        renderDistanceWeight = 10.0D;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        return true;
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_)
    {
        otherPlayerMPX = x;
        otherPlayerMPY = y;
        otherPlayerMPZ = z;
        otherPlayerMPYaw = yaw;
        otherPlayerMPPitch = pitch;
        otherPlayerMPPosRotationIncrements = posRotationIncrements;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        renderOffsetY = 0.0F;
        super.onUpdate();
        prevLimbSwingAmount = limbSwingAmount;
        double d0 = posX - prevPosX;
        double d1 = posZ - prevPosZ;
        float f = MathHelper.sqrt_double(d0 * d0 + d1 * d1) * 4.0F;

        if (f > 1.0F)
        {
            f = 1.0F;
        }

        limbSwingAmount += (f - limbSwingAmount) * 0.4F;
        limbSwing += limbSwingAmount;

        if (!isItemInUse && isEating() && inventory.mainInventory[inventory.currentItem] != null)
        {
            ItemStack itemstack = inventory.mainInventory[inventory.currentItem];
            setItemInUse(inventory.mainInventory[inventory.currentItem], itemstack.getItem().getMaxItemUseDuration(itemstack));
            isItemInUse = true;
        }
        else if (isItemInUse && !isEating())
        {
            clearItemInUse();
            isItemInUse = false;
        }
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        if (otherPlayerMPPosRotationIncrements > 0)
        {
            double d0 = posX + (otherPlayerMPX - posX) / (double) otherPlayerMPPosRotationIncrements;
            double d1 = posY + (otherPlayerMPY - posY) / (double) otherPlayerMPPosRotationIncrements;
            double d2 = posZ + (otherPlayerMPZ - posZ) / (double) otherPlayerMPPosRotationIncrements;
            double d3;

            for (d3 = otherPlayerMPYaw - (double) rotationYaw; d3 < -180.0D; d3 += 360.0D)
            {
            }

            while (d3 >= 180.0D)
            {
                d3 -= 360.0D;
            }

            rotationYaw = (float)((double) rotationYaw + d3 / (double) otherPlayerMPPosRotationIncrements);
            rotationPitch = (float)((double) rotationPitch + (otherPlayerMPPitch - (double) rotationPitch) / (double) otherPlayerMPPosRotationIncrements);
            --otherPlayerMPPosRotationIncrements;
            setPosition(d0, d1, d2);
            setRotation(rotationYaw, rotationPitch);
        }

        prevCameraYaw = cameraYaw;
        updateArmSwingProgress();
        float f1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        float f = (float)Math.atan(-motionY * 0.20000000298023224D) * 15.0F;

        if (f1 > 0.1F)
        {
            f1 = 0.1F;
        }

        if (!onGround || getHealth() <= 0.0F)
        {
            f1 = 0.0F;
        }

        if (onGround || getHealth() <= 0.0F)
        {
            f = 0.0F;
        }

        cameraYaw += (f1 - cameraYaw) * 0.4F;
        cameraPitch += (f - cameraPitch) * 0.8F;
    }

    /**
     * Sets the held item, or an armor slot. Slot 0 is held item. Slot 1-4 is armor. Params: Item, slot
     */
    public void setCurrentItemOrArmor(int slotIn, ItemStack stack)
    {
        if (slotIn == 0)
        {
            inventory.mainInventory[inventory.currentItem] = stack;
        }
        else
        {
            inventory.armorInventory[slotIn - 1] = stack;
        }
    }

    /**
     * Send a chat message to the CommandSender
     */
    public void addChatMessage(IChatComponent component)
    {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(component);
    }

    /**
     * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
     */
    public boolean canCommandSenderUseCommand(int permLevel, String commandName)
    {
        return false;
    }

    /**
     * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
     * the coordinates 0, 0, 0
     */
    public BlockPos getPosition()
    {
        return new BlockPos(posX + 0.5D, posY + 0.5D, posZ + 0.5D);
    }
}
