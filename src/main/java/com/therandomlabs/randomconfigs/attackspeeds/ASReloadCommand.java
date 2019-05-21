package com.therandomlabs.randomconfigs.attackspeeds;

import java.io.IOException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.therandomlabs.randomconfigs.RandomConfigs;
import net.minecraft.command.CommandException;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

public final class ASReloadCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("asreload").
				requires(source -> source.hasPermissionLevel(4)).
				executes(context -> execute(context.getSource())));
	}

	public static int execute(ServerCommandSource source) {
		final MinecraftServer server = source.getMinecraftServer();
		final boolean isServer = server != null && server.isDedicated();

		try {
			AttackSpeeds.reload();
		} catch(IOException ex) {
			RandomConfigs.LOGGER.error("Failed to reload attack speed configuration", ex);

			if(isServer) {
				throw new CommandException(new TextComponent(
						"Failed to reload attack speed configuration: " + ex.getMessage()
				));
			}

			throw new CommandException(new TranslatableComponent(
					"commands.asreloadclient.failure", ex.getMessage()
			));
		}

		if(isServer) {
			source.sendFeedback(
					new TextComponent("Attack speed configuration reloaded!"),
					true
			);
		} else {
			source.sendFeedback(
					new TranslatableComponent("commands.asreloadclient.success"),
					true
			);
		}

		return Command.SINGLE_SUCCESS;
	}
}
