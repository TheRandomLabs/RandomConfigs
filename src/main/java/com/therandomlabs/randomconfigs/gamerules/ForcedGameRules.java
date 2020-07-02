package com.therandomlabs.randomconfigs.gamerules;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import net.minecraft.world.GameRules;

public class ForcedGameRules extends GameRules {
	public ForcedGameRules(GameRules rules, Set<String> forced) {
		this.rules = new HashMap<>(rules.rules);

		for (String key : forced) {
			final Optional<RuleKey<?>> optionalKey = this.rules.keySet().stream().
					filter(ruleKey -> ruleKey.getName().equals(key)).
					findFirst();

			if (optionalKey.isPresent()) {
				final RuleKey<?> originalKey = optionalKey.get();
				this.rules.put(originalKey, ForcedValue.get(this.rules.get(originalKey)));
			}
		}
	}
}
