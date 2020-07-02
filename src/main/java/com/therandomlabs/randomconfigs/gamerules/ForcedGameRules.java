package com.therandomlabs.randomconfigs.gamerules;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.therandomlabs.randomconfigs.mixin.IMixinGameRules;
import net.minecraft.world.GameRules;

public class ForcedGameRules extends GameRules {
	@SuppressWarnings("NullAway")
	public ForcedGameRules(GameRules rules, Set<String> forced) {
		final Map<GameRules.RuleKey<?>, GameRules.Rule<?>> newRules =
				new HashMap<>(((IMixinGameRules) rules).getRules());

		for(String key : forced) {
			final RuleKey ruleKey = new RuleKey(key);
			newRules.put(ruleKey, ForcedValue.get(newRules.get(ruleKey)));
		}

		((IMixinGameRules) this).setRules(newRules);
	}
}

