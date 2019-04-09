package com.therandomlabs.randomconfigs.gamerules;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.therandomlabs.randomconfigs.RandomConfigs;
import com.therandomlabs.randomconfigs.api.listener.CreateSpawnPositionListener;
import com.therandomlabs.randomconfigs.api.listener.WorldInitializationListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldInfo;

public final class DefaultGameRulesHandler implements
		CreateSpawnPositionListener, WorldInitializationListener {
	private static final Field WORLD_INFO = RandomConfigs.findField(World.class, "worldInfo", "y");
	private static final Field GAME_RULES = RandomConfigs.removeFinalModifier(
			RandomConfigs.findField(WorldInfo.class, "gameRules", "V")
	);

	private static List<DefaultGameRule> defaultGameRules;

	@Override
	public void onInitialize(WorldServer world) {
		defaultGameRules = DefaultGameRules.get(world);

		try {
			final MinecraftServer server = world.getServer();
			final WorldInfo worldInfo = (WorldInfo) WORLD_INFO.get(world);
			final GameRules gameRules = (GameRules) GAME_RULES.get(worldInfo);

			final Set<String> forced = new HashSet<>();

			for(DefaultGameRule rule : defaultGameRules) {
				if(!rule.forced || rule.key.equals(DefaultGameRules.DIFFICULTY) ||
						rule.key.equals(DefaultGameRules.WORLD_BORDER_SIZE)) {
					continue;
				}

				forced.add(rule.key);
				gameRules.setOrCreateGameRule(rule.key, rule.value, server);
			}

			GAME_RULES.set(worldInfo, new RCGameRules(server, gameRules, forced));
		} catch(Exception ex) {
			RandomConfigs.crashReport("Failed to set GameRules instance", ex);
		}
	}

	@Override
	public void onCreateSpawnPosition(WorldServer world) {
		if(world.dimension.getType() != DimensionType.OVERWORLD) {
			return;
		}

		WorldInfo worldInfo = null;

		try {
			worldInfo = (WorldInfo) WORLD_INFO.get(world);
		} catch(Exception ex) {
			RandomConfigs.crashReport("Failed to retrieve world info", ex);
		}

		if(defaultGameRules == null) {
			defaultGameRules = DefaultGameRules.get(world);
		}

		for(DefaultGameRule rule : defaultGameRules) {
			if(rule.key.equals(DefaultGameRules.DIFFICULTY)) {
				try {
					worldInfo.setDifficulty(
							EnumDifficulty.valueOf(rule.value.toUpperCase(Locale.ENGLISH))
					);
					worldInfo.setDifficultyLocked(rule.forced);
				} catch(IllegalArgumentException ex) {
					RandomConfigs.LOGGER.error("Invalid difficulty: " + rule.value);
				}

				continue;
			}

			if(rule.key.equals(DefaultGameRules.WORLD_BORDER_SIZE)) {
				try {
					world.getWorldBorder().setSize(Integer.parseInt(rule.value));
				} catch(NumberFormatException ex) {
					RandomConfigs.LOGGER.error("Invalid world border size: " + rule.value);
				}

				continue;
			}

			worldInfo.getGameRulesInstance().setOrCreateGameRule(
					rule.key, rule.value, world.getServer()
			);
		}

		defaultGameRules = null;
	}
}
