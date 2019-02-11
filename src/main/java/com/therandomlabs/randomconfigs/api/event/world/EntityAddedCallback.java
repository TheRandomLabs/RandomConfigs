package com.therandomlabs.randomconfigs.api.event.world;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

public interface EntityAddedCallback {
	Event<EntityAddedCallback> EVENT = EventFactory.createArrayBacked(
			EntityAddedCallback.class,
			listeners -> (world, entity) -> {
				for(EntityAddedCallback event : listeners) {
					event.onEntityAdded(world, entity);
				}
			}
	);

	void onEntityAdded(ServerWorld world, Entity entity);
}
