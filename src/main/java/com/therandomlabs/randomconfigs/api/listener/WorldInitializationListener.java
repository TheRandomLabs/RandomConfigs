package com.therandomlabs.randomconfigs.api.listener;

import net.minecraft.world.WorldServer;

public interface WorldInitializationListener {
	void onInitialize(WorldServer world);
}
