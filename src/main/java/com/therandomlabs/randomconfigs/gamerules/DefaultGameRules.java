package com.therandomlabs.randomconfigs.gamerules;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.therandomlabs.randomconfigs.RandomConfigs;
import com.therandomlabs.randomconfigs.api.event.WorldEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;

public final class DefaultGameRules implements
		WorldEvent.Initialize, WorldEvent.CreateSpawnPosition {
	public static final String MODE_OR_WORLD_TYPE_SPECIFIC = "MODE_OR_WORLD_TYPE_SPECIFIC";
	public static final String DIFFICULTY = "DIFFICULTY";
	public static final String WORLD_BORDER_SIZE = "WORLD_BORDER_SIZE";

	public static final Path JSON = RandomConfigs.getJson("defaultgamerules");

	public static final List<String> DEFAULT = RandomConfigs.readLines(
			DefaultGameRules.class.getResourceAsStream(
					"/data/randomconfigs/defaultgamerules.json"
			)
	);

	private static final Field LEVEL_PROPERTIES =
			RandomConfigs.findField(World.class, "properties", "y");
	private static final Field GAME_RULES = RandomConfigs.removeFinalModifier(
			RandomConfigs.findField(LevelProperties.class, "gameRules", "V")
	);

	private static List<DefaultGameRule> defaultGameRules;

	@Override
	public void onInitialize(ServerWorld world) {
		defaultGameRules = get(world);

		try {
			final MinecraftServer server = world.getServer();
			final LevelProperties properties = (LevelProperties) LEVEL_PROPERTIES.get(world);
			final GameRules gamerules = (GameRules) GAME_RULES.get(properties);

			final Set<String> forced = new HashSet<>();

			for(DefaultGameRule rule : defaultGameRules) {
				if(!rule.forced || rule.key.equals(DIFFICULTY) ||
						rule.key.equals(WORLD_BORDER_SIZE)) {
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
		if(world.dimension.getType() != DimensionType.OVERWORLD) {
			return;
		}

		LevelProperties properties = null;

		try {
			properties = (LevelProperties) LEVEL_PROPERTIES.get(world);
		} catch(Exception ex) {
			RandomConfigs.crashReport("Failed to retrieve level properties", ex);
		}

		if(defaultGameRules == null) {
			defaultGameRules = get(world);
		}

		for(DefaultGameRule rule : defaultGameRules) {
			if(rule.key.equals(DIFFICULTY)) {
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

			if(rule.key.equals(WORLD_BORDER_SIZE)) {
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

	public static void create() throws IOException {
		Files.write(JSON, DEFAULT);
	}

	public static boolean exists() {
		return JSON.toFile().exists();
	}

	public static void ensureExists() throws IOException {
		if(!exists()) {
			create();
		}
	}

	@SuppressWarnings("Duplicates")
	public static List<DefaultGameRule> get(int gamemode, String worldType) throws IOException {
		if(!exists()) {
			create();
			return Collections.emptyList();
		}

		final JsonObject json = RandomConfigs.readJson(JSON);

		final List<DefaultGameRule> gameRules = new ArrayList<>();

		for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final String key = entry.getKey();
			final JsonElement value = entry.getValue();

			if(key.equals(WORLD_BORDER_SIZE)) {
				gameRules.add(new DefaultGameRule(WORLD_BORDER_SIZE, value.toString(), false));
				continue;
			}

			if(!(value instanceof JsonObject)) {
				continue;
			}

			final JsonObject object = (JsonObject) value;

			if(key.equals(MODE_OR_WORLD_TYPE_SPECIFIC)) {
				getSpecific(gameRules, object, gamemode, worldType);
				continue;
			}

			final DefaultGameRule gameRule = get(key, object);

			if(gameRule != null) {
				gameRules.add(gameRule);
			}
		}

		return gameRules;
	}

	private static void getSpecific(List<DefaultGameRule> gameRules, JsonObject json, int gamemode,
			String worldType) {
		for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final String key = entry.getKey();

			if(!matches(key, gamemode, worldType)) {
				continue;
			}

			final JsonElement value = entry.getValue();

			if(!(value instanceof JsonObject)) {
				continue;
			}

			get(gameRules, (JsonObject) value);
		}
	}

	private static boolean matches(String key, int gamemode, String worldType) {
		final GameMode gameType = GameMode.byId(gamemode);
		final String[] split = key.split(":");

		if(!split[0].isEmpty()) {
			final String[] gamemodes = split[0].split(",");
			boolean gamemodeFound = false;

			for(String mode : gamemodes) {
				if(GameMode.byName(mode, null) == gameType) {
					gamemodeFound = true;
					break;
				}
			}

			if(!gamemodeFound) {
				return false;
			}
		}

		if(split.length > 1) {
			for(String type : split[1].split(",")) {
				if(type.equals(worldType)) {
					return true;
				}
			}

			return false;
		}

		return true;
	}

	@SuppressWarnings("Duplicates")
	private static void get(List<DefaultGameRule> gameRules, JsonObject json) {
		for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final String key = entry.getKey();
			final JsonElement value = entry.getValue();

			if(key.equals(WORLD_BORDER_SIZE)) {
				gameRules.add(new DefaultGameRule(WORLD_BORDER_SIZE, value.toString(), false));
				continue;
			}

			if(!(value instanceof JsonObject)) {
				continue;
			}

			final DefaultGameRule gameRule = get(key, (JsonObject) value);

			if(gameRule != null) {
				gameRules.add(gameRule);
			}
		}
	}

	private static DefaultGameRule get(String key, JsonObject object) {
		final JsonElement value = object.get("value");

		if(!(value instanceof JsonPrimitive)) {
			return null;
		}

		final JsonElement forced = object.get("forced");

		if(!(forced instanceof JsonPrimitive)) {
			return null;
		}

		return new DefaultGameRule(
				key,
				((JsonPrimitive) value).asString(),
				(boolean) ((JsonPrimitive) forced).getValue()
		);
	}

	private static List<DefaultGameRule> get(World world) {
		try {
			return get(
					world.getLevelProperties().getGameMode().getId(),
					world.getGeneratorType().getName()
			);
		} catch(Exception ex) {
			RandomConfigs.crashReport("Failed to read default gamerules", ex);
		}

		return null;
	}
}
