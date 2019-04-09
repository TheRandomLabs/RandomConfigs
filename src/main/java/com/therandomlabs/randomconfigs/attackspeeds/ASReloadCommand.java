package com.therandomlabs.randomconfigs.attackspeeds;

import java.io.IOException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.therandomlabs.randomconfigs.RandomConfigs;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.api.distmarker.Dist;

public class ASReloadCommand {
	private ASReloadCommand() {}

	public static void register(CommandDispatcher<CommandSource> dispatcher, Dist dist) {
		dispatcher.register(Commands.literal(dist.isClient() ? "asreloadclient" : "asreload").
				requires(source -> source.hasPermissionLevel(dist.isClient() ? 0 : 4)).
				executes(context -> execute(context.getSource())));
	}

	public static int execute(CommandSource source) {
		final MinecraftServer server = source.getServer();
		final boolean isServer = server != null && server.isDedicatedServer();

		try {
			AttackSpeeds.reload();
		} catch(IOException ex) {
			RandomConfigs.LOGGER.error("Failed to reload attack speed configuration", ex);

			if(isServer) {
				throw new CommandException(new TextComponentString(
						"Failed to reload attack speed configuration: " + ex.getMessage()
				));
			}

			//noinspection NoTranslation
			throw new CommandException(new TextComponentTranslation(
					"commands.asreloadclient.failure", ex.getMessage()
			));
		}

		if(isServer) {
			source.sendFeedback(
					new TextComponentString("Attack speed configuration reloaded!"),
					true
			);
		} else {
			//noinspection NoTranslation
			source.sendFeedback(
					new TextComponentTranslation("commands.asreloadclient.success"),
					true
			);
		}

		return Command.SINGLE_SUCCESS;
	}
}
