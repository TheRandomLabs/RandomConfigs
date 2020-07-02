package com.therandomlabs.randomconfigs.mixin;

import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UnmodifiableLevelProperties.class)
public interface IMixinUnmodifiableLevelProperties {
	@Accessor
	SaveProperties getField_24179();
}
