package com.therandomlabs.randomconfigs;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.impl.SyntaxError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.therandomlabs.randomconfigs.api.event.PlayerEvent;
import com.therandomlabs.randomconfigs.api.event.WorldEvent;
import com.therandomlabs.randomconfigs.attackspeeds.AttackSpeeds;
import com.therandomlabs.randomconfigs.configs.DefaultConfigs;
import com.therandomlabs.randomconfigs.gamerules.DefaultGameRules;
import com.therandomlabs.randomconfigs.gamerules.DefaultGameRulesHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RandomConfigs implements ModInitializer {
	public static final String MOD_ID = "randomconfigs";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final boolean IS_CLIENT =
			FabricLoader.INSTANCE.getEnvironmentHandler().getEnvironmentType() == EnvType.CLIENT;

	public static final Gson GSON = new GsonBuilder().
			setPrettyPrinting().
			disableHtmlEscaping().
			create();

	public static final Path MC_DIR = Paths.get(".").toAbsolutePath().normalize();
	public static final Path CONFIG_DIR = MC_DIR.resolve("config").resolve(MOD_ID);

	public static final String NEWLINE_REGEX = "(\r\n|\r|\n)";
	public static final Pattern NEWLINE = Pattern.compile(NEWLINE_REGEX);

	private static Field modifiers;

	@Override
	public void onInitialize() {
		try {
			DefaultConfigs.handle();
		} catch(IOException ex) {
			crashReport("Failed to handle default configs", ex);
		}

		try {
			DefaultGameRules.ensureExists();
		} catch(IOException ex) {
			crashReport("Failed to load default gamerules", ex);
		}

		final DefaultGameRulesHandler handler = new DefaultGameRulesHandler();

		WorldEvent.INITIALIZE.register(handler);
		WorldEvent.CREATE_SPAWN_POSITION.register(handler);

		final AttackSpeeds attackSpeeds = new AttackSpeeds();

		WorldEvent.ENTITY_ADDED.register(attackSpeeds);
		PlayerEvent.TICK.register(attackSpeeds);
		PlayerEvent.ATTACK_ENTITY.register(attackSpeeds);

		AttackSpeeds.registerCommand();
	}

	public static Path getFile(String file) {
		final Path path = MC_DIR.resolve(file);

		if(isParent(path, MC_DIR)) {
			throw new IllegalArgumentException("Invalid path: " + file);
		}

		return path;
	}

	public static Path getConfig(String fileName) {
		try {
			Files.createDirectories(CONFIG_DIR);
		} catch(IOException ex) {
			crashReport("Failed to create: " + CONFIG_DIR, ex);
		}

		final Path path = CONFIG_DIR.resolve(fileName).normalize();

		if(isParent(path, CONFIG_DIR)) {
			throw new IllegalArgumentException("Invalid config path: " + fileName);
		}

		return path;
	}

	public static String read(Path path) {
		try {
			return StringUtils.join(Files.readAllLines(path), System.lineSeparator());
		} catch(IOException ex) {
			if(!(ex instanceof NoSuchFileException)) {
				crashReport("Failed to read file: " + path, ex);
			}
		}

		return null;
	}

	public static Path getJson(String jsonName) {
		return getConfig(jsonName + ".json");
	}

	public static JsonObject readJson(Path json) {
		final String raw = read(json);

		if(raw != null) {
			try {
				return Jankson.builder().build().load(raw);
			} catch(SyntaxError ex) {
				crashReport("Failed to read JSON: " + json, ex);
			}
		}

		return null;
	}

	public static <T> T readJson(Path json, Class<T> clazz) {
		String raw = read(json);

		if(raw != null) {
			try {
				final Jankson jankson = Jankson.builder().build();

				if(clazz.isArray()) {
					//Shoddy workaround until I figure out how to parse arrays with Jankson
					raw = jankson.load("{\"array\":" + raw + "}").get("array").toJson();
				} else {
					raw = jankson.load(raw).toJson();
				}

				return GSON.fromJson(raw, clazz);
			} catch(SyntaxError ex) {
				crashReport("Failed to read JSON: " + json, ex);
			}
		}

		return null;
	}

	public static void writeJson(Path json, Object object) {
		final String raw = GSON.toJson(object).replaceAll(" {2}", "\t");

		try {
			Files.write(json, (raw + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
		} catch(IOException ex) {
			crashReport("Failed to write to: " + json, ex);
		}
	}

	public static boolean isParent(Path parent, Path path) {
		while((path = path.getParent()) != null) {
			if(path.equals(parent)) {
				return true;
			}
		}

		return false;
	}

	public static String readString(InputStream stream) {
		@SuppressWarnings("resource")
		final Scanner scanner = new Scanner(stream, "UTF-8").useDelimiter("\\A");
		final String string = scanner.hasNext() ? scanner.next() : "";
		scanner.close();
		return string;
	}

	public static List<String> readLines(InputStream stream) {
		return Arrays.asList(NEWLINE.split(readString(stream)));
	}

	public static void crashReport(String message, Exception ex) {
		throw new CrashException(new CrashReport(message, ex));
	}

	public static Field findField(Class<?> clazz, String name, String obfName) {
		for(Field field : clazz.getDeclaredFields()) {
			final String fieldName = field.getName();

			if(name.equals(fieldName) || obfName.equals(fieldName)) {
				field.setAccessible(true);
				return field;
			}
		}

		return null;
	}

	public static Field removeFinalModifier(Field field) {
		try {
			if(modifiers == null) {
				modifiers = Field.class.getDeclaredField("modifiers");
				modifiers.setAccessible(true);
			}

			modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		} catch(Exception ex) {
			crashReport("Failed to make " + field.getName() + " non-final", ex);
		}

		return field;
	}
}
