package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.api.listener.CreateSpawnPositionListener;
import com.therandomlabs.randomconfigs.api.listener.EntityAddedListener;
import com.therandomlabs.randomconfigs.api.listener.WorldInitializationListener;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldServer.class)
public class MixinWorldServer {
	@Inject(method = "initialize", at = @At("HEAD"))
	public void initialize(WorldSettings worldSettings, CallbackInfo callback) {
		for(WorldInitializationListener listener :
				RiftLoader.instance.getListeners(WorldInitializationListener.class)) {
			listener.onInitialize((WorldServer) (Object) this);
		}
	}

	@Inject(method = "createSpawnPosition", at = @At(
			value = "NEW",
			target = "java/util/Random"
	))
	public void createSpawnPosition(WorldSettings worldSettings, CallbackInfo callback) {
		for(CreateSpawnPositionListener listener :
				RiftLoader.instance.getListeners(CreateSpawnPositionListener.class)) {
			listener.onCreateSpawnPosition((WorldServer) (Object) this);
		}
	}

	@Inject(method = "onEntityAdded", at = @At("TAIL"))
	public void onEntityAdded(Entity entity, CallbackInfo callback) {
		for(EntityAddedListener listener :
				RiftLoader.instance.getListeners(EntityAddedListener.class)) {
			listener.onEntityAdded((WorldServer) (Object) this, entity);
		}
	}
}
