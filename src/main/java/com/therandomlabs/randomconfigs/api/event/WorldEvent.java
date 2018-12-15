package com.therandomlabs.randomconfigs.api.event;

import net.fabricmc.fabric.util.HandlerList;
import net.minecraft.server.world.ServerWorld;

public final class WorldEvent {
	public static final HandlerList<Initialize> INITIALIZE = new HandlerList<>(Initialize.class);
	public static final HandlerList<CreateSpawnPosition> CREATE_SPAWN_POSITION =
			new HandlerList<>(CreateSpawnPosition.class);

	@FunctionalInterface
	public interface Initialize {
		void onInitialize(ServerWorld world);
	}

	@FunctionalInterface
	public interface CreateSpawnPosition {
		void onCreateSpawnPosition(ServerWorld world);
	}
}
