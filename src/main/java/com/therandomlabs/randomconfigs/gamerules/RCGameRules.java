package com.therandomlabs.randomconfigs.gamerules;

import java.util.Set;
import net.minecraft.world.GameRules;

public class RCGameRules extends GameRules {
	private final Set<String> forced;

	public RCGameRules(GameRules rules, Set<String> forced) {
		this.rules = rules.rules;
		this.forced = forced;
	}

	@Override
	public void setOrCreateGameRule(String key, String ruleValue) {
		if(!forced.contains(key)) {
			super.setOrCreateGameRule(key, ruleValue);
		}
	}
}
