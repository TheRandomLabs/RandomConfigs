package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.api.IMixinRule;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("NullAway")
@Mixin(GameRules.Rule.class)
public class MixinRule implements IMixinRule {
	@Shadow
	@Final
	private GameRules.RuleType type;

	@SuppressWarnings("unchecked")
	public <T extends GameRules.Rule<T>> GameRules.RuleType<T> getType() {
		return type;
	}

	@Override
	public void set(String string) {
		deserialize(string);
	}

	@Shadow
	protected void deserialize(String string) {}
}
