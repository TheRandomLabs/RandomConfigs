package com.therandomlabs.randomconfigs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import org.apache.commons.lang3.StringUtils;

@Mod(modid = RandomConfigs.MODID, version = RandomConfigs.VERSION,
		acceptedMinecraftVersions = RandomConfigs.ACCEPTED_MINECRAFT_VERSIONS,
		acceptableRemoteVersions = RandomConfigs.ACCEPTABLE_REMOTE_VERSIONS,
		updateJSON = RandomConfigs.UPDATE_JSON,
		certificateFingerprint = RandomConfigs.CERTIFICATE_FINGERPRINT)
public final class RandomConfigs {
	public static final String MODID = "randomconfigs";
	public static final String VERSION = "@VERSION@";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.10,1.14)";
	public static final String ACCEPTABLE_REMOTE_VERSIONS = "*";
	public static final String UPDATE_JSON =
			"https://raw.githubusercontent.com/TheRandomLabs/RandomConfigs/misc/versions.json";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	public static final boolean IS_CLIENT = FMLCommonHandler.instance().getSide().isClient();

	public static final Path MC_DIR = Paths.get(".").toAbsolutePath().normalize();
	public static final Path CONFIG_DIR = MC_DIR.resolve("config").resolve(MODID);

	public static final String NEWLINE_REGEX = "(\r\n|\r|\n)";
	public static final Pattern NEWLINE = Pattern.compile(NEWLINE_REGEX);

	@Mod.EventHandler
	public static void construct(FMLConstructionEvent event) {
		try {
			DefaultConfigs.handle();
		} catch(IOException ex) {
			handleException("Failed to handle default configs", ex);
		}

		try {
			DefaultGamerules.ensureExists();
		} catch(IOException ex) {
			handleException("Failed to handle default gamerules", ex);
		}
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
			handleException("Failed to create: " + CONFIG_DIR, ex);
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
				handleException("Failed to read file: " + path, ex);
			}
		}

		return null;
	}

	public static Path getJson(String jsonName) {
		return getConfig(jsonName + ".json");
	}

	public static JsonObject readJson(Path json) {
		final String raw = read(json);
		return raw == null ? null : new JsonParser().parse(raw).getAsJsonObject();
	}

	public static <T> T readJson(Path json, Class<T> clazz) {
		final String raw = read(json);
		return raw == null ? null : new Gson().fromJson(raw, clazz);
	}

	public static void writeJson(Path json, Object object) {
		final String raw = new GsonBuilder().setPrettyPrinting().create().toJson(object).
				replaceAll(" {2}", "\t");

		try {
			Files.write(json, (raw + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
		} catch(IOException ex) {
			handleException("Failed to write to: " + json, ex);
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

	public static void handleException(String message, Exception ex) {
		throw new ReportedException(new CrashReport(message, ex));
	}
}
