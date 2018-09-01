package com.therandomlabs.randomconfigs;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

public final class DefaultGamerules {
	public static class DefaultGamerule {
		public String key;
		public String value;
		public boolean forced;

		public DefaultGamerule(String key, String value, boolean forced) {
			this.key = key;
			this.value = value;
			this.forced = forced;
		}
	}

	public static class DGGameRules extends GameRules {
		private static final Field RULES = RandomConfigs.findField(GameRules.class, "rules", "b");

		private final Set<String> forced;

		@SuppressWarnings("unchecked")
		public DGGameRules(MinecraftServer server, GameRules rules, Set<String> forced)
				throws Exception {
			final Map<String, GameRules.Value> localRules =
					(Map<String, GameRules.Value>) RULES.get(this);
			final Map<String, GameRules.Value> originalRules =
					(Map<String, GameRules.Value>) RULES.get(rules);

			localRules.clear();
			localRules.putAll(originalRules);

			this.forced = forced;

			for(String key : forced) {
				final GameRules.Value value = localRules.get(key);
				final BiConsumer<MinecraftServer, GameRules.Value> consumer =
						(mcServer, newValue) -> {};

				localRules.put(key, new GameRules.Value(key, value.getType(), consumer) {
					{
						super.setValue(value.getString(), server);
					}

					@Override
					public void setValue(String value, MinecraftServer server) {}
				});
			}
		}

		@Override
		public void setOrCreateGameRule(String key, String ruleValue, MinecraftServer server) {
			if(!forced.contains(key)) {
				super.setOrCreateGameRule(key, ruleValue, server);
			}
		}
	}

	public static final String MODE_OR_WORLD_TYPE_SPECIFIC = "MODE_OR_WORLD_TYPE_SPECIFIC";
	public static final String WORLD_BORDER_SIZE = "WORLD_BORDER_SIZE";
	public static final Path JSON = RandomConfigs.getJson("defaultgamerules");
	public static final List<String> DEFAULT = RandomConfigs.readLines(
			DefaultGamerules.class.getResourceAsStream(
					"/data/randomconfigs/defaultgamerules.json"
			)
	);

	private static final Field WORLD_INFO = RandomConfigs.findField(World.class, "worldInfo", "j");
	private static final Field GAMERULES = RandomConfigs.removeFinalModifier(
			RandomConfigs.findField(WorldInfo.class, "gameRules", "V")
	);

	private static List<DefaultGamerule> cachedDefaultGamerules;

	public static void onOverworldInit(World world) {
		WorldInfo worldInfo = null;

		try {
			worldInfo = (WorldInfo) WORLD_INFO.get(world);
		} catch(Exception ex) {
			RandomConfigs.handleException("Failed to retrieve world info", ex);
		}

		final int gamemode = worldInfo.getGameType().getID();
		final String type = world.getWorldType().func_211888_a();

		List<DefaultGamerule> defaultGamerules = null;

		try {
			defaultGamerules = get(gamemode, type);
		} catch(Exception ex) {
			RandomConfigs.handleException("Failed to read default gamerules", ex);
		}

		cachedDefaultGamerules = defaultGamerules;

		for(DefaultGamerule rule : defaultGamerules) {
			if(rule.key.equals(WORLD_BORDER_SIZE)) {
				world.getWorldBorder().setSize(Integer.parseInt(rule.value));
			} else {
				worldInfo.getGameRulesInstance().setOrCreateGameRule(
						rule.key,
						rule.value,
						world.getMinecraftServer()
				);
			}
		}
	}

	public static void onWorldLoad(World world) {
		List<DefaultGamerule> defaultGamerules = null;

		if(cachedDefaultGamerules != null) {
			defaultGamerules = cachedDefaultGamerules;
			cachedDefaultGamerules = null;
		} else {
			final int gamemode = world.getWorldInfo().getGameType().getID();
			final String type = world.getWorldType().func_211888_a();

			try {
				defaultGamerules = get(gamemode, type);
			} catch(Exception ex) {
				RandomConfigs.handleException("Failed to read default gamerules", ex);
			}
		}

		try {
			final MinecraftServer server = world.getMinecraftServer();
			final WorldInfo worldInfo = (WorldInfo) WORLD_INFO.get(world);
			final GameRules gamerules = (GameRules) GAMERULES.get(worldInfo);

			final Set<String> forced = new HashSet<>();

			for(DefaultGamerule rule : defaultGamerules) {
				if(rule.forced && !rule.key.equals(WORLD_BORDER_SIZE)) {
					forced.add(rule.key);
					gamerules.setOrCreateGameRule(rule.key, rule.value, server);
				}
			}

			GAMERULES.set(worldInfo, new DGGameRules(server, gamerules, forced));
		} catch(Exception ex) {
			RandomConfigs.handleException("Failed to set GameRules instance", ex);
		}
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

	public static List<DefaultGamerule> get(int gamemode, String worldType) throws IOException {
		if(!exists()) {
			create();
			return Collections.emptyList();
		}

		final JsonObject json = RandomConfigs.readJson(JSON);

		final List<DefaultGamerule> gamerules = new ArrayList<>();

		for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final String key = entry.getKey();
			final JsonElement value = entry.getValue();

			if(key.equals(WORLD_BORDER_SIZE)) {
				gamerules.add(new DefaultGamerule(WORLD_BORDER_SIZE, value.toString(), false));
				continue;
			}

			if(!value.isJsonObject()) {
				continue;
			}

			final JsonObject object = value.getAsJsonObject();

			if(key.equals(MODE_OR_WORLD_TYPE_SPECIFIC)) {
				getSpecific(gamerules, object, gamemode, worldType);
				continue;
			}

			final DefaultGamerule gamerule = get(key, object);

			if(gamerule != null) {
				gamerules.add(gamerule);
			}
		}

		return gamerules;
	}

	private static void getSpecific(List<DefaultGamerule> gamerules, JsonObject json, int gamemode,
			String worldType) {
		for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final String key = entry.getKey();

			if(!matches(key, gamemode, worldType)) {
				continue;
			}

			final JsonElement value = entry.getValue();

			if(!value.isJsonObject()) {
				continue;
			}

			final JsonObject object = value.getAsJsonObject();
			get(gamerules, object);
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

	private static void get(List<DefaultGamerule> gamerules, JsonObject json) {
		for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final String key = entry.getKey();
			final JsonElement value = entry.getValue();

			if(key.equals(WORLD_BORDER_SIZE)) {
				gamerules.add(new DefaultGamerule(WORLD_BORDER_SIZE, value.toString(), false));
				continue;
			}

			if(!value.isJsonObject()) {
				continue;
			}

			final DefaultGamerule gamerule = get(key, value.getAsJsonObject());

			if(gamerule != null) {
				gamerules.add(gamerule);
			}
		}
	}

	private static DefaultGamerule get(String key, JsonObject value) {
		if(!value.has("value") || !value.has("forced")) {
			return null;
		}

		final JsonElement forced = value.get("forced");

		if(!forced.isJsonPrimitive()) {
			return null;
		}

		final JsonPrimitive primitive = forced.getAsJsonPrimitive();

		if(!primitive.isBoolean()) {
			return null;
		}

		return new DefaultGamerule(key, value.get("value").toString(), primitive.getAsBoolean());
	}
}
