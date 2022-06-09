package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandSetSpawnpoint extends CommandBase
{
    /**
     * Gets the name of the command
     */
    public String getCommandName()
    {
        return "spawnpoint";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    /**
     * Gets the usage string for the command.
     */
    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.spawnpoint.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 1 && args.length < 4)
        {
            throw new WrongUsageException("commands.spawnpoint.usage");
        }
        else
        {
            EntityPlayerMP entityplayermp = args.length > 0 ? CommandBase.getPlayer(sender, args[0]) : CommandBase.getCommandSenderAsPlayer(sender);
            BlockPos blockpos = args.length > 3 ? CommandBase.parseBlockPos(sender, args, 1, true) : entityplayermp.getPosition();

            if (entityplayermp.worldObj != null)
            {
                entityplayermp.setSpawnPoint(blockpos, true);
                CommandBase.notifyOperators(sender, this, "commands.spawnpoint.success", entityplayermp.getName(), Integer.valueOf(blockpos.getX()), Integer.valueOf(blockpos.getY()), Integer.valueOf(blockpos.getZ()));
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : (args.length > 1 && args.length <= 4 ? CommandBase.func_175771_a(args, 1, pos) : null);
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
