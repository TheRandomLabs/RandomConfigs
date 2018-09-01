package com.therandomlabs.randomconfigs.mixin;

import java.lang.reflect.Field;
import com.therandomlabs.randomconfigs.DefaultGamerules;
import com.therandomlabs.randomconfigs.RandomConfigs;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.OverworldDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(OverworldDimension.class)
public class MixinOverworldDimension {
	private static final Field WORLD = RandomConfigs.findField(Dimension.class, "world", "b");

	@Inject(method = "init", at = @At("HEAD"))
	private void init(CallbackInfo ci) {
		try {
			DefaultGamerules.onOverworldInit((World) WORLD.get(this));
		} catch(Exception ex) {
			RandomConfigs.handleException("Failed to retrieve world", ex);
		}
	}
}
