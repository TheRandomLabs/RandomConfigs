package com.therandomlabs.randomconfigs.api.event;

import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.server.world.ServerWorld;

public final class WorldEvent {
	@FunctionalInterface
	public interface Initialize {
		void onInitialize(ServerWorld world);
	}

	@FunctionalInterface
	public interface CreateSpawnPosition {
		void onCreateSpawnPosition(ServerWorld world);
	}

	public static final HandlerArray<Initialize> INITIALIZE = new HandlerArray<>(Initialize.class);
	public static final HandlerArray<CreateSpawnPosition> CREATE_SPAWN_POSITION =
			new HandlerArray<>(CreateSpawnPosition.class);
}
