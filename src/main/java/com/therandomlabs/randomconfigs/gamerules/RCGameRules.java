package com.therandomlabs.randomconfigs.gamerules;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import com.therandomlabs.randomconfigs.RandomConfigs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

public class RCGameRules extends GameRules {
	private static final Field RULES =
			RandomConfigs.findField(GameRules.class, "rules", "field_9196");

	private final Set<String> forced;

	@SuppressWarnings("unchecked")
	public RCGameRules(MinecraftServer server, GameRules rules, Set<String> forced)
			throws IllegalAccessException {
		final Map<String, Value> localRules = (Map<String, Value>) RULES.get(this);
		final Map<String, Value> originalRules = (Map<String, Value>) RULES.get(rules);

		localRules.clear();
		localRules.putAll(originalRules);

		this.forced = forced;

		for(String key : forced) {
			localRules.put(key, new ForcedValue(
					key, localRules.get(key), (mcServer, newValue) -> {}, server
			));
		}
	}

	@Override
	public void put(String key, String value, MinecraftServer server) {
		if(!forced.contains(key)) {
			super.put(key, value, server);
		}
	}
}
