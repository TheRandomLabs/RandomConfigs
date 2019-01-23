package com.therandomlabs.randomconfigs.attackspeeds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.mojang.brigadier.CommandDispatcher;
import com.therandomlabs.randomconfigs.RandomConfigs;
import com.therandomlabs.randomconfigs.api.listener.EntityAddedListener;
import com.therandomlabs.randomconfigs.api.listener.PlayerAttackEntityListener;
import com.therandomlabs.randomconfigs.api.listener.PlayerTickListener;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.world.WorldServer;
import org.dimdev.rift.listener.CommandAdder;

public final class AttackSpeeds
		implements CommandAdder, EntityAddedListener, PlayerTickListener,
		PlayerAttackEntityListener {
	public static final Path JSON = RandomConfigs.getJson("attackspeeds");

	private static AttackSpeedConfig speeds = new AttackSpeedConfig();
	private static boolean reloaded;

	@Override
	public void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
		if(speeds.asreloadCommand) {
			ASReloadCommand.register(dispatcher);
		}
	}

	@Override
	public void onEntityAdded(WorldServer world, Entity entity) {
		if(!(entity instanceof EntityPlayer)) {
			return;
		}

		if(!reloaded) {
			try {
				reload();
				reloaded = true;
			} catch(IOException ex) {
				RandomConfigs.crashReport("Failed to load attack speeds", ex);
			}
		}

		//We don't need to apply potion attributes here because it's done automatically
		final IAttributeInstance attackSpeed =
				((EntityPlayer) entity).getAttribute(SharedMonsterAttributes.ATTACK_SPEED);

		//If configurable attack speeds are disabled, set it to the vanilla default of 4.0
		attackSpeed.setBaseValue(speeds.enabled ? speeds.defaultAttackSpeed : 4.0);
	}

	@Override
	public void onPlayerTick(EntityPlayerMP player) {
		if(!speeds.enabled) {
			return;
		}

		final Item item = player.getHeldItem(player.getActiveHand()).getItem();

		final IAttributeInstance attackSpeed =
				player.getAttribute(SharedMonsterAttributes.ATTACK_SPEED);

		final ItemAttackSpeed speed = speeds.itemAttackSpeeds.get(item);
		attackSpeed.setBaseValue(speed == null ? speeds.defaultAttackSpeed : speed.speed);
	}

	@Override
	public boolean onPlayerAttackEntity(EntityPlayerMP player, Entity target) {
		if(player.getEntityWorld().isRemote || player.getCooledAttackStrength(0.5F) == 1.0F) {
			return true;
		}

		final Item item = player.getHeldItem(player.getActiveHand()).getItem();
		final ItemAttackSpeed speed = speeds.itemAttackSpeeds.get(item);

		if(speed == null) {
			if(speeds.disableAttacksDuringAttackCooldownByDefault) {
				player.resetCooldown();
				return false;
			}
		} else {
			if(speed.disableAttacksDuringAttackCooldown) {
				player.resetCooldown();
				return false;
			}
		}

		return true;
	}

	public AttackSpeedConfig get() {
		return speeds;
	}

	public static void reload() throws IOException {
		if(Files.exists(JSON)) {
			speeds = RandomConfigs.readJson(JSON, AttackSpeedConfig.class);
			speeds.ensureCorrect();
		}

		RandomConfigs.writeJson(JSON, speeds);
	}
}
