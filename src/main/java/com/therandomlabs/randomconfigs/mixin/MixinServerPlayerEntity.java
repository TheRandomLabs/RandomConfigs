package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.api.event.PlayerEvent;
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
		for(PlayerEvent.Tick event : PlayerEvent.TICK.getBackingArray()) {
			event.onPlayerTick((ServerPlayerEntity) (Object) this);
		}
	}

	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	public void attack(Entity target, CallbackInfo callback) {
		for(PlayerEvent.AttackEntity event : PlayerEvent.ATTACK_ENTITY.getBackingArray()) {
			if(!event.onPlayerAttackEntity((ServerPlayerEntity) (Object) this, target)) {
				callback.cancel();
				break;
			}
		}
	}
}
