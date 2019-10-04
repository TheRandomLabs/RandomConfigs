package com.therandomlabs.randomconfigs.api;

import net.minecraft.world.GameRules;

public interface IMixinRule {
	<T extends GameRules.Rule<T>> GameRules.RuleType<T> getType();

	void set(String string);
}
