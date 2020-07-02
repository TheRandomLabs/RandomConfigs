package com.therandomlabs.randomconfigs.mixin;

import java.util.Map;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRules.class)
public interface IMixinGameRules {
	@Accessor
	Map<GameRules.Key<?>, GameRules.Rule<?>> getRules();

	@Accessor
	void setRules(Map<GameRules.Key<?>, GameRules.Rule<?>> rules);
}
