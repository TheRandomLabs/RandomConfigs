package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.api.listener.PlayerAttackEntityListener;
import com.therandomlabs.randomconfigs.api.listener.PlayerTickListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public class MixinEntityPlayerMP {
	@Inject(method = "playerTick", at = @At("HEAD"))
	public void playerTick(CallbackInfo callback) {
		for(PlayerTickListener listener :
				RiftLoader.instance.getListeners(PlayerTickListener.class)) {
			listener.onPlayerTick((EntityPlayerMP) (Object) this);
		}
	}

	@Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"), cancellable = true)
	public void attackTargetEntityWithCurrentItem(Entity target, CallbackInfo callback) {
		for(PlayerAttackEntityListener listener :
				RiftLoader.instance.getListeners(PlayerAttackEntityListener.class)) {
			if(!listener.onPlayerAttackEntity((EntityPlayerMP) (Object) this, target)) {
				callback.cancel();
				break;
			}
		}
	}
}
