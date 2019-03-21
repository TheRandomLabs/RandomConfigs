package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.api.event.world.EntityAddedCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class MixinWorldChunk {
	@Inject(method = "addEntity", at = @At("TAIL"))
	public void addEntity(Entity entity, CallbackInfo callback) {
		final World world = ((WorldChunk) (Object) this).getWorld();

		if(!world.isClient) {
			EntityAddedCallback.EVENT.invoker().onEntityAdded((ServerWorld) world, entity);
		}
	}
}
