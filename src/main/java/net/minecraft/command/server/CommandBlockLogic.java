package net.minecraft.command.server;

import io.netty.buffer.ByteBuf;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;

public abstract class CommandBlockLogic implements ICommandSender
{
    /** The formatting for the timestamp on commands run. */
    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss");

    /** The number of successful commands run. (used for redstone output) */
    private int successCount;
    private boolean trackOutput = true;

    /** The previously run command. */
    private IChatComponent lastOutput = null;

    /** The command stored in the command block. */
    private String commandStored = "";

    /** The custom name of the command block. (defaults to "@") */
    private String customName = "@";
    private final CommandResultStats resultStats = new CommandResultStats();

    /**
     * returns the successCount int.
     */
    public int getSuccessCount()
    {
        return successCount;
    }

    /**
     * Returns the lastOutput.
     */
    public IChatComponent getLastOutput()
    {
        return lastOutput;
    }

    /**
     * Stores data to NBT format.
     */
    public void writeDataToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setString("Command", commandStored);
        tagCompound.setInteger("SuccessCount", successCount);
        tagCompound.setString("CustomName", customName);
        tagCompound.setBoolean("TrackOutput", trackOutput);

        if (lastOutput != null && trackOutput)
        {
            tagCompound.setString("LastOutput", IChatComponent.Serializer.componentToJson(lastOutput));
        }

        resultStats.writeStatsToNBT(tagCompound);
    }

    /**
     * Reads NBT formatting and stored data into variables.
     */
    public void readDataFromNBT(NBTTagCompound nbt)
    {
        commandStored = nbt.getString("Command");
        successCount = nbt.getInteger("SuccessCount");

        if (nbt.hasKey("CustomName", 8))
        {
            customName = nbt.getString("CustomName");
        }

        if (nbt.hasKey("TrackOutput", 1))
        {
            trackOutput = nbt.getBoolean("TrackOutput");
        }

        if (nbt.hasKey("LastOutput", 8) && trackOutput)
        {
            lastOutput = IChatComponent.Serializer.jsonToComponent(nbt.getString("LastOutput"));
        }

        resultStats.readStatsFromNBT(nbt);
    }

    /**
     * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
     */
    public boolean canCommandSenderUseCommand(int permLevel, String commandName)
    {
        return permLevel <= 2;
    }

    /**
     * Sets the command.
     */
    public void setCommand(String command)
    {
        commandStored = command;
        successCount = 0;
    }

    /**
     * Returns the command of the command block.
     */
    public String getCommand()
    {
        return commandStored;
    }

    public void trigger(World worldIn)
    {
        if (worldIn.isRemote)
        {
            successCount = 0;
        }

        MinecraftServer minecraftserver = MinecraftServer.getServer();

        if (minecraftserver != null && minecraftserver.isAnvilFileSet() && minecraftserver.isCommandBlockEnabled())
        {
            ICommandManager icommandmanager = minecraftserver.getCommandManager();

            try
            {
                lastOutput = null;
                successCount = icommandmanager.executeCommand(this, commandStored);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Executing command block");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Command to be executed");
                crashreportcategory.addCrashSectionCallable("Command", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return getCommand();
                    }
                });
                crashreportcategory.addCrashSectionCallable("Name", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return getName();
                    }
                });
                throw new ReportedException(crashreport);
            }
        }
        else
        {
            successCount = 0;
        }
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getName()
    {
        return customName;
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public IChatComponent getDisplayName()
    {
        return new ChatComponentText(getName());
    }

    public void setName(String p_145754_1_)
    {
        customName = p_145754_1_;
    }

    /**
     * Send a chat message to the CommandSender
     */
    public void addChatMessage(IChatComponent component)
    {
        if (trackOutput && getEntityWorld() != null && !getEntityWorld().isRemote)
        {
            lastOutput = (new ChatComponentText("[" + CommandBlockLogic.timestampFormat.format(new Date()) + "] ")).appendSibling(component);
            updateCommand();
        }
    }

    /**
     * Returns true if the command sender should be sent feedback about executed commands
     */
    public boolean sendCommandFeedback()
    {
        MinecraftServer minecraftserver = MinecraftServer.getServer();
        return minecraftserver == null || !minecraftserver.isAnvilFileSet() || minecraftserver.worldServers[0].getGameRules().getBoolean("commandBlockOutput");
    }

    public void setCommandStat(CommandResultStats.Type type, int amount)
    {
        resultStats.func_179672_a(this, type, amount);
    }

    public abstract void updateCommand();

    public abstract int func_145751_f();

    public abstract void func_145757_a(ByteBuf p_145757_1_);

    public void setLastOutput(IChatComponent lastOutputMessage)
    {
        lastOutput = lastOutputMessage;
    }

    public void setTrackOutput(boolean shouldTrackOutput)
    {
        trackOutput = shouldTrackOutput;
    }

    public boolean shouldTrackOutput()
    {
        return trackOutput;
    }

    public boolean tryOpenEditCommandBlock(EntityPlayer playerIn)
    {
        if (!playerIn.capabilities.isCreativeMode)
        {
            return false;
        }
        else
        {
            if (playerIn.getEntityWorld().isRemote)
            {
                playerIn.openEditCommandBlock(this);
            }

            return true;
        }
    }

    public CommandResultStats getCommandResultStats()
    {
        return resultStats;
    }
}
