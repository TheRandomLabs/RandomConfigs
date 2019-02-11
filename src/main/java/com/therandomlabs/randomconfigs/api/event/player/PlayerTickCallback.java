package com.therandomlabs.randomconfigs.api.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerTickCallback {
	Event<PlayerTickCallback> EVENT = EventFactory.createArrayBacked(
			PlayerTickCallback.class,
			listeners -> player -> {
				for(PlayerTickCallback event : listeners) {
					event.onPlayerTick(player);
				}
			}
	);

	void onPlayerTick(ServerPlayerEntity player);
}
