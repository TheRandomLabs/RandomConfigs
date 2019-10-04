package com.therandomlabs.randomconfigs.gamerules;

import com.mojang.brigadier.context.CommandContext;
import com.therandomlabs.randomconfigs.api.IMixinRule;
import net.minecraft.world.GameRules;

public final class ForcedValue {
	public static class ForcedIntegerValue extends GameRules.IntRule {
		private final GameRules.IntRule rule;

		public ForcedIntegerValue(GameRules.IntRule rule) {
			super(((IMixinRule) rule).getType(), rule.get());
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
			super(((IMixinRule) rule).getType(), rule.get());
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

	private ForcedValue() {}

	public static GameRules.Rule get(GameRules.Rule rule) {
		if(rule instanceof GameRules.IntRule) {
			return new ForcedIntegerValue((GameRules.IntRule) rule);
		}

		return new ForcedBooleanValue((GameRules.BooleanRule) rule);
	}
}
