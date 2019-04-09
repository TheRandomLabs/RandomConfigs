package com.therandomlabs.randomconfigs.gamerules;

import java.util.function.BiConsumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

public class ForcedValue extends GameRules.Value {
	public ForcedValue(String key, GameRules.Value value,
			BiConsumer<MinecraftServer, GameRules.Value> consumer, MinecraftServer server) {
		super(key, value.getType(), consumer);
		super.setValue(value.getString(), server);
	}

	@Override
	public void setValue(String value, MinecraftServer server) {}
}
