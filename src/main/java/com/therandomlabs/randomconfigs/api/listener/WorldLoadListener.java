package com.therandomlabs.randomconfigs.api.listener;

import net.minecraft.world.WorldServer;

public interface WorldLoadListener {
	void onWorldLoad(WorldServer world);
}
