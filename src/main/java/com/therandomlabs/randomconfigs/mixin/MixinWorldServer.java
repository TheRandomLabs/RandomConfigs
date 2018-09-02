package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.api.listener.CreateSpawnPositionListener;
import com.therandomlabs.randomconfigs.api.listener.WorldLoadListener;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(WorldServer.class)
public class MixinWorldServer {
	@Inject(method = "createSpawnPosition", at = @At(
			value = "NEW",
			target = "java/util/Random"
	))
	private void createSpawnPosition(WorldSettings worldSettings, CallbackInfo callback) {
		for(CreateSpawnPositionListener listener :
				RiftLoader.instance.getListeners(CreateSpawnPositionListener.class)) {
			listener.onCreateSpawnPosition((WorldServer) (Object) this);
		}
	}

	@Inject(method = "initialize", at = @At("HEAD"))
	private void initialize(WorldSettings worldSettings, CallbackInfo callback) {
		for(WorldLoadListener listener :
				RiftLoader.instance.getListeners(WorldLoadListener.class)) {
			listener.onWorldLoad((WorldServer) (Object) this);
		}
	}
}
