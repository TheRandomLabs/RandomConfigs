package com.therandomlabs.randomconfigs.gamerules;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.therandomlabs.randomconfigs.RandomConfigs;
import com.therandomlabs.randomconfigs.api.event.world.CreateSpawnPositionCallback;
import com.therandomlabs.randomconfigs.api.event.world.WorldInitializeCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;

public final class DefaultGameRulesHandler
		implements WorldInitializeCallback, CreateSpawnPositionCallback {
	private static final Field GAME_RULES = RandomConfigs.removeFinalModifier(
			RandomConfigs.findField(LevelProperties.class, "gameRules", "field_154")
	);
	private static final Method SET_FROM_STRING =
			RandomConfigs.findMethod(GameRules.Rule.class, "setFromString", "method_20777");

	private static List<DefaultGameRule> defaultGameRules;

	@Override
	public void onInitialize(ServerWorld world) {
		defaultGameRules = DefaultGameRules.get(world);

		try {
			final LevelProperties properties = world.getLevelProperties();
			final GameRules gameRules = properties.getGameRules();

			final Set<String> forced = new HashSet<>();

			for(DefaultGameRule rule : defaultGameRules) {
				if(!rule.forced || rule.key.equals(DefaultGameRules.DIFFICULTY) ||
						rule.key.equals(DefaultGameRules.WORLD_BORDER_SIZE)) {
					continue;
				}

				forced.add(rule.key);
				setDefaultGameRule(world, rule);
			}

			GAME_RULES.set(properties, new ForcedGameRules(gameRules, forced));
		} catch(Exception ex) {
			RandomConfigs.crashReport("Failed to set GameRules instance", ex);
		}
	}

	@Override
	public void onCreateSpawnPosition(ServerWorld world) {
		if(world.dimension.getType() != DimensionType.OVERWORLD) {
			return;
		}

		final LevelProperties properties = world.getLevelProperties();

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
					RandomConfigs.LOGGER.error("Invalid difficulty: {}", rule.value);
				}

				continue;
			}

			if(rule.key.equals(DefaultGameRules.WORLD_BORDER_SIZE)) {
				try {
					world.getWorldBorder().setSize(Integer.parseInt(rule.value));
				} catch(NumberFormatException ex) {
					RandomConfigs.LOGGER.error("Invalid world border size: {}", rule.value);
				}

				continue;
			}

			setDefaultGameRule(world, rule);
		}

		defaultGameRules = null;
	}

	@SuppressWarnings("unchecked")
	private static void setDefaultGameRule(ServerWorld world, DefaultGameRule rule) {
		try {
			SET_FROM_STRING.invoke(
					world.getLevelProperties().getGameRules().get(new GameRules.RuleKey(rule.key)),
					rule.value
			);
		} catch(IllegalAccessException | InvocationTargetException ex) {
			RandomConfigs.crashReport("Failed to set default gamerule", ex);
		}
	}
}
