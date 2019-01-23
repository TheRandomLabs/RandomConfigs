package com.therandomlabs.randomconfigs.api.listener;

import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;

public interface EntityAddedListener {
	void onEntityAdded(WorldServer world, Entity entity);
}
