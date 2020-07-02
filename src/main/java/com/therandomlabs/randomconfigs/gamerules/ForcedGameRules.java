package com.therandomlabs.randomconfigs.gamerules;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.therandomlabs.randomconfigs.mixin.IMixinGameRules;
import net.minecraft.world.GameRules;

public class ForcedGameRules extends GameRules {
	@SuppressWarnings("NullAway")
	public ForcedGameRules(GameRules rules, Set<String> forced) {
		final Map<GameRules.Key<?>, GameRules.Rule<?>> newRules =
				new HashMap<>(((IMixinGameRules) rules).getRules());

		for (String key : forced) {
			final Optional<Key<?>> optionalKey = newRules.keySet().stream().
					filter(ruleKey -> ruleKey.getName().equals(key)).
					findFirst();

			if (optionalKey.isPresent()) {
				final Key<?> originalKey = optionalKey.get();
				newRules.put(originalKey, ForcedValue.get(newRules.get(originalKey)));
			}
		}

		((IMixinGameRules) this).setRules(newRules);
	}
}

