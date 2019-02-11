package com.therandomlabs.randomconfigs.api.event.world;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;

public interface CreateSpawnPositionCallback {
	Event<CreateSpawnPositionCallback> EVENT = EventFactory.createArrayBacked(
			CreateSpawnPositionCallback.class,
			listeners -> world -> {
				for(CreateSpawnPositionCallback event : listeners) {
					event.onCreateSpawnPosition(world);
				}
			}
	);

	void onCreateSpawnPosition(ServerWorld world);
}
