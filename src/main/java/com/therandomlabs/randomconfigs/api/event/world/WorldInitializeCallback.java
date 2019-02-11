package com.therandomlabs.randomconfigs.api.event.world;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;

public interface WorldInitializeCallback {
	Event<WorldInitializeCallback> EVENT = EventFactory.createArrayBacked(
			WorldInitializeCallback.class,
			listeners -> world -> {
				for(WorldInitializeCallback event : listeners) {
					event.onInitialize(world);
				}
			}
	);

	void onInitialize(ServerWorld world);
}
