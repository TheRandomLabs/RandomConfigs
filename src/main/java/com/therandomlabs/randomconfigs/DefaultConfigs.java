package com.therandomlabs.randomconfigs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import com.google.gson.annotations.SerializedName;

public final class DefaultConfigs {
	private DefaultConfigs() {}

	public static class Config {
		public String source;
		public String destination;
		public Side side;
		public boolean forceCopy;
	}

	public enum Side {
		@SerializedName("client")
		CLIENT,
		@SerializedName("server")
		SERVER,
		@SerializedName("both")
		BOTH
	}

	public static void handle() throws IOException {
		final Config[] configs = RandomConfigs.readJson("defaultconfigs", Config[].class);

		if(configs == null) {
			return;
		}

		for(Config config : configs) {
			handle(config);
		}

		RandomConfigs.writeJson("defaultconfigs", configs);
	}

	public static void handle(Config config) throws IOException {
		if(config.side == Side.CLIENT && !RandomConfigs.isClientSide()) {
			return;
		}

		if(config.side == Side.SERVER && RandomConfigs.isClientSide()) {
			return;
		}

		final Path source = RandomConfigs.getConfig("defaultconfigs/" + config.source);
		final Path destination = RandomConfigs.getFile(config.destination);

		if(config.forceCopy || !Files.exists(destination)) {
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
		}

		config.forceCopy = false;
	}
}
