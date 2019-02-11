package com.therandomlabs.randomconfigs.api.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerAttackEntityCallback {
	Event<PlayerAttackEntityCallback> EVENT = EventFactory.createArrayBacked(
			PlayerAttackEntityCallback.class,
			listeners -> (player, target) -> {
				for(PlayerAttackEntityCallback event : listeners) {
					if(!event.onPlayerAttackEntity(player, target)) {
						return false;
					}
				}

				return true;
			}
	);

	boolean onPlayerAttackEntity(ServerPlayerEntity player, Entity target);
}
