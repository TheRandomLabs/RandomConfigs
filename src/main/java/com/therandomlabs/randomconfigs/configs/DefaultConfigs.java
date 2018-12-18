package com.therandomlabs.randomconfigs.configs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.therandomlabs.randomconfigs.RandomConfigs;

public final class DefaultConfigs {
	public static final Path DIRECTORY = RandomConfigs.getConfig("defaultconfigs");
	public static final Path JSON = RandomConfigs.getJson("defaultconfigs");
	public static final Path OLD_JSON = RandomConfigs.getJson("defaultconfigs_old");

	private DefaultConfigs() {}

	public static void handle() throws IOException {
		final DefaultConfig[] configs = RandomConfigs.readJson(JSON, DefaultConfig[].class);

		if(configs == null) {
			Files.createDirectories(DIRECTORY);
			return;
		}

		final DefaultConfig[] oldConfigs = RandomConfigs.readJson(OLD_JSON, DefaultConfig[].class);

		Files.copy(JSON, OLD_JSON, StandardCopyOption.REPLACE_EXISTING);

		if(oldConfigs == null) {
			for(DefaultConfig config : configs) {
				handle(config, config.version);
			}
		} else {
			for(DefaultConfig config : configs) {
				int oldVersion = config.version;

				for(DefaultConfig oldConfig : oldConfigs) {
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

	public static void handle(DefaultConfig config, int oldVersion) throws IOException {
		if(config.source == null) {
			throw new NullPointerException("source");
		}

		if(config.destination == null) {
			throw new NullPointerException("destination");
		}

		if(config.side == null) {
			throw new NullPointerException("side");
		}

		if(config.side == ConfigSide.CLIENT && !RandomConfigs.IS_CLIENT) {
			return;
		}

		if(config.side == ConfigSide.SERVER && RandomConfigs.IS_CLIENT) {
			return;
		}

		final Path source = RandomConfigs.getConfig("defaultconfigs/" + config.source);
		final Path destination = RandomConfigs.getFile(config.destination);

		if(config.version != oldVersion || !destination.toFile().exists()) {
			Files.createDirectories(destination.getParent());
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
