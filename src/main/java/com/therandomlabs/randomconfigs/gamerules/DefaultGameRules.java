package com.therandomlabs.randomconfigs.gamerules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DefaultGameRules {
	public static final String MODE_OR_WORLD_TYPE_SPECIFIC = "MODE_OR_WORLD_TYPE_SPECIFIC";
	public static final String DIFFICULTY = "DIFFICULTY";
	public static final String WORLD_BORDER_SIZE = "WORLD_BORDER_SIZE";

	public static final Path JSON = RandomConfigs.getJson("defaultgamerules");

	//Temporary until I can figure out how to read defaultgamerules.json in mod jar
	public static final List<String> DEFAULT = Arrays.asList(("{\n" +
			"\t/*\n" +
			"\t//Top-level gamerules are set for all game modes and world types.\n" +
			"\t\"commandBlockOutput\": {\n" +
			"\t\t//Each gamerule has two properties: the value, and whether the value should be " +
			"forced.\n" +
			"\t\t//RandomConfigs makes sure that forced gamerules are not changed.\n" +
			"\t\t\"value\": false,\n" +
			"\t\t\"forced\": false\n" +
			"\t},\n" +
			"\t\"keepInventory\": {\n" +
			"\t\t\"value\": true,\n" +
			"\t\t\"forced\": false\n" +
			"\t},\n" +
			"\t\"MODE_OR_WORLD_TYPE_SPECIFIC\": {\n" +
			"\t\t//Gamerules can be set for specific game modes and world types in the format:\n" +
			"\t\t//MODE,MODE,...:TYPE,TYPE,...\n" +
			"\t\t\"creative:flat,void\": {\n" +
			"\t\t\t\"doDaylightCycle\": {\n" +
			"\t\t\t\t\"value\": false,\n" +
			"\t\t\t\t\"forced\": true\n" +
			"\t\t\t},\n" +
			"\t\t\t\"doWeatherCycle\": {\n" +
			"\t\t\t\t\"value\": false,\n" +
			"\t\t\t\t\"forced\": true\n" +
			"\t\t\t},\n" +
			"\t\t\t\"doMobSpawning\": {\n" +
			"\t\t\t\t\"value\": false,\n" +
			"\t\t\t\t\"forced\": false\n" +
			"\t\t\t}\n" +
			"\t\t},\n" +
			"\t\t//The game mode or world type does not have to be specified.\n" +
			"\t\t//For an empty game mode, use:\n" +
			"\t\t//:TYPE,TYPE,...\n" +
			"\t\t//For example, use \":void\" to specify gamerules for all Void worlds.\n" +
			"\t\t//The following specifies gamerules for all survival worlds.\n" +
			"\t\t\"survival\": {\n" +
			"\t\t\t//This isn't really a gamerule. RandomConfigs uses this to determine the " +
			"difficulty.\n" +
			"\t\t\t\"DIFFICULTY\": {\n" +
			"\t\t\t\t//Valid values: \"peaceful\", \"easy\", \"normal\", \"hard\"\n" +
			"\t\t\t\t\"value\": \"hard\",\n" +
			"\t\t\t\t//Whether the difficulty should be locked on world creation\n" +
			"\t\t\t\t\"forced\": true\n" +
			"\t\t\t},\n" +
			"\t\t\t//This is also not an actual gamerule.\n" +
			"\t\t\t//It's used to determine the world border size in blocks from (0, 0).\n" +
			"\t\t\t\"WORLD_BORDER_SIZE\": 10000\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\t*/\n" +
			"}\n").split("\n"));

	private static List<DefaultGameRule> defaultGameRules;

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void onCreateSpawn(WorldEvent.CreateSpawnPosition event) {
		final ServerWorld world = (ServerWorld) event.getWorld();

		if(world.getDimension().getType() != DimensionType.OVERWORLD) {
			return;
		}

		defaultGameRules = get(world);

		final WorldInfo worldInfo = world.getWorldInfo();

		for(DefaultGameRule rule : defaultGameRules) {
			if(rule.key.equals(DIFFICULTY)) {
				try {
					worldInfo.setDifficulty(
							Difficulty.valueOf(rule.value.toUpperCase(Locale.ENGLISH))
					);
					worldInfo.setDifficultyLocked(rule.forced);
				} catch(IllegalArgumentException ex) {
					RandomConfigs.LOGGER.error("Invalid difficulty: {}", rule.value);
				}

				continue;
			}

			if(rule.key.equals(WORLD_BORDER_SIZE)) {
				try {
					world.getWorldBorder().setSize(Integer.parseInt(rule.value));
				} catch(NumberFormatException ex) {
					RandomConfigs.LOGGER.error("Invalid world border size: {}", rule.value);
				}

				continue;
			}

			worldInfo.gameRules.get(new GameRules.RuleKey(rule.key)).setStringValue(rule.value);
		}
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		final IWorld world = event.getWorld();

		if(world.isRemote()) {
			return;
		}

		if(defaultGameRules == null) {
			defaultGameRules = get(world);
		}

		final WorldInfo worldInfo = world.getWorldInfo();
		final Set<String> forced = new HashSet<>();

		for(DefaultGameRule rule : defaultGameRules) {
			if(!rule.forced || rule.key.equals(DefaultGameRules.DIFFICULTY) ||
					rule.key.equals(DefaultGameRules.WORLD_BORDER_SIZE)) {
				continue;
			}

			forced.add(rule.key);
			worldInfo.gameRules.get(new GameRules.RuleKey(rule.key)).setStringValue(rule.value);
		}

		worldInfo.gameRules = new ForcedGameRules(worldInfo.gameRules, forced);
		defaultGameRules = null;
	}

	public static void create() throws IOException {
		Files.write(JSON, DEFAULT);
	}

	public static boolean exists() {
		return Files.exists(JSON);
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
		RandomConfigs.writeJson(JSON, json);

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

	private static void getSpecific(
			List<DefaultGameRule> gameRules, JsonObject json, int gamemode, String worldType
	) {
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
		final GameType gameType = GameType.getByID(gamemode);
		final String[] split = key.split(":");

		if(!split[0].isEmpty()) {
			final String[] gamemodes = split[0].split(",");
			boolean gamemodeFound = false;

			for(String mode : gamemodes) {
				if(GameType.parseGameTypeWithDefault(mode, null) == gameType) {
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

	private static List<DefaultGameRule> get(IWorld world) {
		try {
			return get(
					world.getWorldInfo().getGameType().getID(),
					world.getWorldInfo().getGenerator().getName()
			);
		} catch(Exception ex) {
			RandomConfigs.crashReport("Failed to read default gamerules", ex);
		}

		return null;
	}
}
