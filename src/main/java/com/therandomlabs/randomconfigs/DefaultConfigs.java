package com.therandomlabs.randomconfigs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.google.gson.annotations.SerializedName;

public final class DefaultConfigs {
	private DefaultConfigs() {}

	public static class Config {
		public String source;
		public String destination;
		public Side side;
		public int version;
	}

	public enum Side {
		@SerializedName("client")
		CLIENT,
		@SerializedName("server")
		SERVER,
		@SerializedName("both")
		BOTH
	}

	public static final Path DIRECTORY = RandomConfigs.getConfig("defaultconfigs");
	public static final Path JSON = RandomConfigs.getJson("defaultconfigs");
	public static final Path OLD_JSON = RandomConfigs.getJson("defaultconfigs_old");

	public static void handle() throws IOException {
		final Config[] configs = RandomConfigs.readJson(JSON, Config[].class);

		if(configs == null) {
			Files.createDirectories(DIRECTORY);
			return;
		}

		final Config[] oldConfigs = RandomConfigs.readJson(OLD_JSON, Config[].class);

		Files.copy(JSON, OLD_JSON, StandardCopyOption.REPLACE_EXISTING);

		if(oldConfigs == null) {
			for(Config config : configs) {
				handle(config, config.version);
			}
		} else {
			for(Config config : configs) {
				int oldVersion = config.version;

				for(Config oldConfig : oldConfigs) {
					if(Paths.get(config.source).equals(Paths.get(oldConfig.source))) {
						if(Paths.get(config.destination).equals(Paths.get(oldConfig.destination))) {
							oldVersion = oldConfig.version;
						}

						break;
					}
				}

				handle(config, oldVersion);
			}
		}

		RandomConfigs.writeJson(JSON, configs);
	}

	public static void handle(Config config, int oldVersion) throws IOException {
		if(config.source == null) {
			throw new NullPointerException("source");
		}

		if(config.destination == null) {
			throw new NullPointerException("destination");
		}

		if(config.side == null) {
			throw new NullPointerException("side");
		}

		if(config.side == Side.CLIENT && !RandomConfigs.IS_CLIENT) {
			return;
		}

		if(config.side == Side.SERVER && RandomConfigs.IS_CLIENT) {
			return;
		}

		final Path source = RandomConfigs.getConfig("defaultconfigs/" + config.source);
		final Path destination = RandomConfigs.getFile(config.destination);

		if(config.version != oldVersion || !Files.exists(destination)) {
			Files.createDirectories(destination.getParent());
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
