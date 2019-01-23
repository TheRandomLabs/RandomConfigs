package com.therandomlabs.randomconfigs.api.event;

import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerEvent {
	@FunctionalInterface
	public interface Tick {
		void onPlayerTick(ServerPlayerEntity player);
	}

	@FunctionalInterface
	public interface AttackEntity {
		boolean onPlayerAttackEntity(ServerPlayerEntity player, Entity target);
	}

	public static final HandlerArray<Tick> TICK = new HandlerArray<>(Tick.class);
	public static final HandlerArray<AttackEntity> ATTACK_ENTITY =
			new HandlerArray<>(AttackEntity.class);
}
