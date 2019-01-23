package com.therandomlabs.randomconfigs.api.listener;

import net.minecraft.entity.player.EntityPlayerMP;

public interface PlayerTickListener {
	void onPlayerTick(EntityPlayerMP player);
}
