package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.DefaultGamerules;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(WorldServer.class)
public class MixinWorldServer {
	@Inject(method = "initialize", at = @At("HEAD"))
	private void initialize(WorldSettings worldSettings, CallbackInfo ci) {
		DefaultGamerules.onWorldLoad((World) (Object) this);
	}
}
