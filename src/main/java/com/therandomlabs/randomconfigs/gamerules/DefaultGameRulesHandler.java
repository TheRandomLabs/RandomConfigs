package com.therandomlabs.randomconfigs.gamerules;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.therandomlabs.randomconfigs.RandomConfigs;
import com.therandomlabs.randomconfigs.api.event.world.CreateSpawnPositionCallback;
import com.therandomlabs.randomconfigs.api.event.world.WorldInitializeCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;

public final class DefaultGameRulesHandler implements
		WorldInitializeCallback, CreateSpawnPositionCallback {
	private static final Field LEVEL_PROPERTIES =
			RandomConfigs.findField(World.class, "properties", "field_9232");
	private static final Field GAME_RULES = RandomConfigs.removeFinalModifier(
			RandomConfigs.findField(LevelProperties.class, "gameRules", "field_154")
	);

	private static List<DefaultGameRule> defaultGameRules;

	@Override
	public void onInitialize(ServerWorld world) {
		defaultGameRules = DefaultGameRules.get(world);

		try {
			final MinecraftServer server = world.getServer();
			final LevelProperties properties = (LevelProperties) LEVEL_PROPERTIES.get(world);
			final GameRules gamerules = (GameRules) GAME_RULES.get(properties);

			final Set<String> forced = new HashSet<>();

			for(DefaultGameRule rule : defaultGameRules) {
				if(!rule.forced || rule.key.equals(DefaultGameRules.DIFFICULTY) ||
						rule.key.equals(DefaultGameRules.WORLD_BORDER_SIZE)) {
					continue;
				}

				forced.add(rule.key);
				gamerules.put(rule.key, rule.value, server);
			}

			GAME_RULES.set(properties, new RCGameRules(server, gamerules, forced));
		} catch(Exception ex) {
			RandomConfigs.crashReport("Failed to set GameRules instance", ex);
		}
	}

	@Override
	public void onCreateSpawnPosition(ServerWorld world) {
		if(world.dimension.getType() != DimensionType.field_13072) {
			return;
		}

		LevelProperties properties = null;

		try {
			properties = (LevelProperties) LEVEL_PROPERTIES.get(world);
		} catch(Exception ex) {
			RandomConfigs.crashReport("Failed to retrieve level properties", ex);
		}

		if(defaultGameRules == null) {
			defaultGameRules = DefaultGameRules.get(world);
		}

		for(DefaultGameRule rule : defaultGameRules) {
			if(rule.key.equals(DefaultGameRules.DIFFICULTY)) {
				try {
					properties.setDifficulty(
							Difficulty.valueOf(rule.value.toUpperCase(Locale.ENGLISH))
					);
					properties.setDifficultyLocked(rule.forced);
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

			properties.getGameRules().put(rule.key, rule.value, world.getServer());
		}

		defaultGameRules = null;
	}
}
