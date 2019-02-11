package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.api.event.world.CreateSpawnPositionCallback;
import com.therandomlabs.randomconfigs.api.event.world.EntityAddedCallback;
import com.therandomlabs.randomconfigs.api.event.world.WorldInitializeCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class MixinServerWorld {
	@Inject(method = "init", at = @At("HEAD"))
	public void init(LevelInfo info, CallbackInfo callback) {
		WorldInitializeCallback.EVENT.invoker().onInitialize((ServerWorld) (Object) this);
	}

	@Inject(method = "init", at = @At(
			value = "NEW",
			target = "java/util/Random"
	))
	public void createSpawnPosition(LevelInfo info, CallbackInfo callback) {
		CreateSpawnPositionCallback.EVENT.invoker().onCreateSpawnPosition(
				(ServerWorld) (Object) this
		);
	}

	@Inject(method = "method_18209", at = @At("RETURN"))
	public void onEntityAdded(Entity entity, boolean flag,
			CallbackInfoReturnable<Boolean> callback) {
		EntityAddedCallback.EVENT.invoker().onEntityAdded((ServerWorld) (Object) this, entity);
	}
}
