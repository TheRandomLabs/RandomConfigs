package com.therandomlabs.randomconfigs.gamerules;

import java.lang.reflect.Field;
import com.mojang.brigadier.context.CommandContext;
import com.therandomlabs.randomconfigs.RandomConfigs;
import net.minecraft.world.GameRules;

public final class ForcedValue {
	public static class ForcedIntegerValue extends GameRules.IntRule {
		private final GameRules.IntRule rule;

		public ForcedIntegerValue(GameRules.IntRule rule) {
			super(getType(rule), rule.get());
			this.rule = rule;
		}

		@Override
		protected void setFromArgument(CommandContext context, String argument) {}

		@Override
		public void setFromString(String value) {}

		@Override
		public String valueToString() {
			return Integer.toString(rule.get());
		}

		@Override
		public int get() {
			return rule.get();
		}

		@Override
		protected GameRules.IntRule method_20770() {
			return this;
		}
	}

	public static class ForcedBooleanValue extends GameRules.BooleanRule {
		private final GameRules.BooleanRule rule;

		public ForcedBooleanValue(GameRules.BooleanRule rule) {
			super(getType(rule), rule.get());
			this.rule = rule;
		}

		@Override
		protected void setFromArgument(CommandContext context, String argument) {}

		@Override
		public void setFromString(String value) {}

		@Override
		public String valueToString() {
			return Boolean.toString(rule.get());
		}

		@Override
		public boolean get() {
			return rule.get();
		}

		@Override
		protected GameRules.BooleanRule method_20761() {
			return this;
		}
	}

	private static final Field TYPE =
			RandomConfigs.findField(GameRules.class, "type", "field_19417");

	private ForcedValue() {}

	public static GameRules.Rule get(GameRules.Rule rule) {
		if(rule instanceof GameRules.IntRule) {
			return new ForcedIntegerValue((GameRules.IntRule) rule);
		}

		return new ForcedBooleanValue((GameRules.BooleanRule) rule);
	}

	@SuppressWarnings("unchecked")
	private static <T extends GameRules.Rule<T>> GameRules.RuleType<T> getType(
			GameRules.Rule rule
	) {
		try {
			return (GameRules.RuleType<T>) TYPE.get(rule);
		} catch(IllegalAccessException ex) {
			RandomConfigs.crashReport("Failed to get gamerule type", ex);
		}

		return null;
	}
}
