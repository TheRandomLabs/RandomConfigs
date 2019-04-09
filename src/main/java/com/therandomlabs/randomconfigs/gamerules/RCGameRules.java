package com.therandomlabs.randomconfigs.gamerules;

import java.util.Map;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

public class RCGameRules extends GameRules {
	private final Set<String> forced;

	public RCGameRules(MinecraftServer server, GameRules rules, Set<String> forced) {
		final Map<String, Value> localRules = this.rules;
		final Map<String, Value> originalRules = rules.rules;

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
	public void setOrCreateGameRule(String key, String ruleValue, MinecraftServer server) {
		if(!forced.contains(key)) {
			super.setOrCreateGameRule(key, ruleValue, server);
		}
	}
}
