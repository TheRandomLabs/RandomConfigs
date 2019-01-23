package com.therandomlabs.randomconfigs.api.listener;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

public interface PlayerAttackEntityListener {
	boolean onPlayerAttackEntity(EntityPlayerMP player, Entity target);
}
