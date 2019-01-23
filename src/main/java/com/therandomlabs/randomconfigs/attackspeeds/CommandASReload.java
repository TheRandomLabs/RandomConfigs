package com.therandomlabs.randomconfigs.attackspeeds;

import java.io.IOException;
import com.therandomlabs.randomconfigs.RandomConfigs;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

public class CommandASReload extends CommandBase {
	private final boolean isClient;

	public CommandASReload(Side side) {
		isClient = side.isClient();
	}

	@Override
	public String getName() {
		return isClient ? "asreloadclient" : "asreload";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return isClient ? "commands.asreloadclient.usage" : "/asreload";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
			throws CommandException {
		final boolean isServer = server != null && server.isDedicatedServer();

		try{
			AttackSpeeds.reload();
		} catch(IOException ex) {
			RandomConfigs.LOGGER.error("Failed to reload attack speed configuration", ex);

			if(isServer) {
				throw new CommandException(
						"Failed to reload attack speed configuration: " + ex.getMessage()
				);
			}

			throw new CommandException("commands.asreloadclient.failure", ex.getMessage());
		}

		if(isServer) {
			notifyCommandListener(sender, this, "Attack speed configuration reloaded!");
		} else {
			sender.sendMessage(new TextComponentTranslation("commands.asreloadclient.success"));
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return isClient ? 0 : 4;
	}
}
