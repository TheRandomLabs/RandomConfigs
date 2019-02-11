package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.api.event.player.PlayerAttackEntityCallback;
import com.therandomlabs.randomconfigs.api.event.player.PlayerTickCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {
	@Inject(method = "method_14226", at = @At("HEAD"))
	public void playerTick(CallbackInfo callback) {
		PlayerTickCallback.EVENT.invoker().onPlayerTick((ServerPlayerEntity) (Object) this);
	}

	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	public void attack(Entity target, CallbackInfo callback) {
		if(!PlayerAttackEntityCallback.EVENT.invoker().onPlayerAttackEntity(
				(ServerPlayerEntity) (Object) this, target
		)) {
			callback.cancel();
		}
	}
}
