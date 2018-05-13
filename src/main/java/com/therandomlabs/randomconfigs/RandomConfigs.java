package com.therandomlabs.randomconfigs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	public static final Path MC_DIR = Paths.get(".").toAbsolutePath();
	public static final Path CONFIG_DIR = Paths.get("config", MODID);

	private static Boolean clientSide;

	@Mod.EventHandler
	public static void construct(FMLConstructionEvent event) throws IOException {
		DefaultConfigs.handle();
	}

	public static Path getFile(String file) throws IOException {
		final Path path = MC_DIR.resolve(file);

		if(isParent(path, MC_DIR)) {
			throw new IllegalArgumentException("Invalid path: " + file);
		}

		return path;
	}

	public static Path getConfig(String fileName) throws IOException {
		Files.createDirectories(CONFIG_DIR);

		final Path path = CONFIG_DIR.resolve(fileName).normalize();

		if(isParent(path, CONFIG_DIR)) {
			throw new IllegalArgumentException("Invalid config path: " + fileName);
		}

		return path;
	}

	public static <T> T readJson(String jsonName, Class<T> clazz) throws IOException {
		try {
			final Path path = getConfig(jsonName + ".json");
			final String raw = StringUtils.join(Files.readAllLines(path), System.lineSeparator());

			return new Gson().fromJson(raw, clazz);
		} catch(FileNotFoundException ex) {
			return null;
		}
	}

	public static void writeJson(String jsonName, Object object) throws IOException {
		final Path path = getConfig(jsonName + ".json");
		final String json = new GsonBuilder().setPrettyPrinting().create().toJson(object).
				replaceAll(" {2}", "\t");

		Files.write(path, (json + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
	}

	public static boolean isClientSide() {
		if(clientSide == null) {
			try {
				Class.forName("net.minecraft.client.gui.GuiScreen");
				clientSide = true;
			} catch(ClassNotFoundException ex) {
				clientSide = false;
			}
		}

		return clientSide;
	}

	private static boolean isParent(Path parent, Path path) {
		while((path = path.getParent()) != null) {
			if(path.equals(parent)) {
				return true;
			}
		}

		return false;
	}
}
