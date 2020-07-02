package com.therandomlabs.randomconfigs.gamerules;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.world.GameRules;

public final class ForcedValue {
	public static class ForcedIntegerValue extends GameRules.IntegerValue {
		public ForcedIntegerValue(GameRules.IntegerValue value) {
			super(value.type, value.get());
		}

		@Override
		public void updateValue(CommandContext<CommandSource> context, String argument) {}

		@Override
		public void setStringValue(String value) {}

		@Override
		protected GameRules.IntegerValue getValue() {
			return this;
		}
	}

	public static class ForcedBooleanValue extends GameRules.BooleanValue {
		public ForcedBooleanValue(GameRules.BooleanValue value) {
			super(value.type, value.get());
		}

		@Override
		public void updateValue(CommandContext<CommandSource> context, String argument) {}

		@Override
		public void setStringValue(String value) {}

		@Override
		protected GameRules.BooleanValue getValue() {
			return this;
		}
	}

	private ForcedValue() {}

	public static GameRules.RuleValue get(GameRules.RuleValue value) {
		if(value instanceof GameRules.IntegerValue) {
			return new ForcedIntegerValue((GameRules.IntegerValue) value);
		}

		return new ForcedBooleanValue((GameRules.BooleanValue) value);
	}
}
