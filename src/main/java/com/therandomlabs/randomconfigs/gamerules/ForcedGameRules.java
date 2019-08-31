package com.therandomlabs.randomconfigs.gamerules;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.therandomlabs.randomconfigs.RandomConfigs;
import net.minecraft.world.GameRules;

public class ForcedGameRules extends GameRules {
	private static final Field RULES =
			RandomConfigs.findField(GameRules.class, "rules", "field_9196");

	@SuppressWarnings("unchecked")
	public ForcedGameRules(GameRules rules, Set<String> forced) {
		try {
			final Map<GameRules.RuleKey<?>, GameRules.Rule<?>> newRules = new HashMap<>(
					(Map<GameRules.RuleKey<?>, GameRules.Rule<?>>) RULES.get(rules)
			);

			for(String key : forced) {
				final RuleKey ruleKey = new RuleKey(key);
				newRules.put(ruleKey, ForcedValue.get(newRules.get(ruleKey)));
			}

			RULES.set(this, newRules);
		} catch(IllegalAccessException ex) {
			RandomConfigs.crashReport("Failed to set forced gamerules", ex);
		}
	}
}

