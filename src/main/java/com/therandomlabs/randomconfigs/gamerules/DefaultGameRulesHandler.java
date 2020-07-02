package com.therandomlabs.randomconfigs.gamerules;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.therandomlabs.randomconfigs.RandomConfigs;
import com.therandomlabs.randomconfigs.api.IMixinRule;
import com.therandomlabs.randomconfigs.api.event.world.CreateSpawnPositionCallback;
import com.therandomlabs.randomconfigs.api.event.world.WorldInitializeCallback;
import com.therandomlabs.randomconfigs.mixin.IMixinGameRules;
import com.therandomlabs.randomconfigs.mixin.IMixinLevelInfo;
import com.therandomlabs.randomconfigs.mixin.IMixinLevelProperties;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DefaultGameRulesHandler
		implements WorldInitializeCallback, CreateSpawnPositionCallback {
	@Nullable
	private static List<DefaultGameRule> defaultGameRules;

	@SuppressWarnings("NullAway")
	@Override
	public void onInitialize(ServerWorld world) {
		defaultGameRules = DefaultGameRules.get(world);

		try {
			final LevelProperties properties = DefaultGameRules.getLevelProperties(world);
			final GameRules gameRules = properties.getGameRules();

			final Set<String> forced = new HashSet<>();

			for (DefaultGameRule rule : defaultGameRules) {
				if (!rule.forced || rule.key.equals(DefaultGameRules.DIFFICULTY) ||
						rule.key.equals(DefaultGameRules.WORLD_BORDER_SIZE)) {
					continue;
				}

				forced.add(rule.key);
				setDefaultGameRule(properties, rule);
			}

			final LevelInfo info = ((IMixinLevelProperties) properties).getField_25030();
			((IMixinLevelInfo) (Object) info).setGameRules(new ForcedGameRules(gameRules, forced));
		} catch (Exception ex) {
			RandomConfigs.crashReport("Failed to set GameRules instance", ex);
		}
	}

	@SuppressWarnings("NullAway")
	@Override
	public void onCreateSpawnPosition(ServerWorld world) {
		if (world.getDimension() != DimensionType.getOverworldDimensionType()) {
			return;
		}

		final LevelProperties properties = DefaultGameRules.getLevelProperties(world);

		if (defaultGameRules == null) {
			defaultGameRules = DefaultGameRules.get(world);
		}

		for (DefaultGameRule rule : defaultGameRules) {
			if (rule.key.equals(DefaultGameRules.DIFFICULTY)) {
				try {
					properties.setDifficulty(
							Difficulty.valueOf(rule.value.toUpperCase(Locale.ENGLISH))
					);
					properties.setDifficultyLocked(rule.forced);
				} catch (IllegalArgumentException ex) {
					RandomConfigs.LOGGER.error("Invalid difficulty: {}", rule.value);
				}

				continue;
			}

			if (rule.key.equals(DefaultGameRules.WORLD_BORDER_SIZE)) {
				try {
					world.getWorldBorder().setSize(Integer.parseInt(rule.value));
				} catch (NumberFormatException ex) {
					RandomConfigs.LOGGER.error("Invalid world border size: {}", rule.value);
				}

				continue;
			}

			setDefaultGameRule(properties, rule);
		}

		defaultGameRules = null;
	}

	private static void setDefaultGameRule(LevelProperties properties, DefaultGameRule rule) {
		final GameRules rules = properties.getGameRules();
		final Optional<GameRules.Key<?>> optionalKey = ((IMixinGameRules) rules).getRules().
				keySet().stream().
				filter(key -> rule.key.equals(key.getName())).
				findFirst();
		optionalKey.ifPresent(ruleKey -> ((IMixinRule) rules.get(ruleKey)).set(rule.value));
	}
}
