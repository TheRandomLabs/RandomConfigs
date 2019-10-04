package com.therandomlabs.randomconfigs.mixin;

import net.minecraft.world.GameRules;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelProperties.class)
public interface IMixinLevelProperties {
	@Accessor
	void setGameRules(GameRules rules);
}
