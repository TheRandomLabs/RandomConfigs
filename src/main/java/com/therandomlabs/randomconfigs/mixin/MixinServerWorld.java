package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.api.event.WorldEvent;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class MixinServerWorld {
	@Inject(method = "init", at = @At("HEAD"))
	public void init(LevelInfo info, CallbackInfo callback) {
		for(WorldEvent.Initialize event : WorldEvent.INITIALIZE.getBackingArray()) {
			event.onInitialize((ServerWorld) (Object) this);
		}
	}

	@Inject(method = "method_14184", at = @At(
			value = "NEW",
			target = "java/util/Random"
	))
	public void createSpawnPosition(LevelInfo info, CallbackInfo callback) {
		for(WorldEvent.CreateSpawnPosition event :
				WorldEvent.CREATE_SPAWN_POSITION.getBackingArray()) {
			event.onCreateSpawnPosition((ServerWorld) (Object) this);
		}
	}

	@Inject(method = "onEntityAdded", at = @At("TAIL"))
	public void onEntityAdded(Entity entity, CallbackInfo callback) {
		for(WorldEvent.EntityAdded event : WorldEvent.ENTITY_ADDED.getBackingArray()) {
			event.onEntityAdded((ServerWorld) (Object) this, entity);
		}
	}
}
