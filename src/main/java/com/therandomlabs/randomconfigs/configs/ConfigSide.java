package com.therandomlabs.randomconfigs.configs;

import com.google.gson.annotations.SerializedName;

public enum ConfigSide {
	@SerializedName("client")
	CLIENT,
	@SerializedName("server")
	SERVER,
	@SerializedName("both")
	BOTH
}
