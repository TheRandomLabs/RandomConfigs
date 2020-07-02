package com.therandomlabs.randomconfigs.mixin;

import net.minecraft.world.GameRules;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelInfo.class)
public interface IMixinLevelInfo {
	@Accessor
	void setGameRules(GameRules gameRules);
}
