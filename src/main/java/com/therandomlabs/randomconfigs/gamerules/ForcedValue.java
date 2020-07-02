package com.therandomlabs.randomconfigs.gamerules;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.world.GameRules;

public final class ForcedValue {
	public static class ForcedIntegerValue extends GameRules.IntegerValue {
		private final GameRules.IntegerValue value;

		public ForcedIntegerValue(GameRules.IntegerValue value) {
			super(value.type, value.get());
			this.value = value;
		}

		@Override
		protected void func_223555_a(CommandContext context, String argument) {}

		@Override
		public String func_223552_b() {
			return Integer.toString(value.get());
		}

		@Override
		public void func_223553_a(String value) {}

		@Override
		public int func_223557_c() {
			return value.func_223557_c();
		}

		@Override
		protected GameRules.IntegerValue func_223213_e_() {
			return this;
		}
	}

	public static class ForcedBooleanValue extends GameRules.BooleanValue {
		private final GameRules.BooleanValue value;

		public ForcedBooleanValue(GameRules.BooleanValue value) {
			super(value.type, value.get());
			this.value = value;
		}

		@Override
		protected void func_223555_a(CommandContext context, String argument) {}

		@Override
		public String func_223552_b() {
			return Boolean.toString(value.get());
		}

		@Override
		public void func_223553_a(String value) {}

		@Override
		public int func_223557_c() {
			return value.func_223557_c();
		}

		@Override
		protected GameRules.BooleanValue func_223213_e_() {
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
