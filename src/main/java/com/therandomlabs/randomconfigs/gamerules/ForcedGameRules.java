package com.therandomlabs.randomconfigs.gamerules;

import java.util.HashMap;
import java.util.Set;
import net.minecraft.world.GameRules;

public class ForcedGameRules extends GameRules {
	public ForcedGameRules(GameRules rules, Set<String> forced) {
		this.rules = new HashMap<>(rules.rules);

		for(String key : forced) {
			final RuleKey ruleKey = new RuleKey(key);
			this.rules.put(ruleKey, ForcedValue.get(this.rules.get(ruleKey)));
		}
	}
}
