package com.therandomlabs.randomconfigs.attackspeeds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.therandomlabs.randomconfigs.RandomConfigs;
import com.therandomlabs.randomconfigs.api.event.PlayerEvent;
import com.therandomlabs.randomconfigs.api.event.WorldEvent;
import net.fabricmc.fabric.commands.CommandRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class AttackSpeeds
		implements WorldEvent.EntityAdded, PlayerEvent.Tick, PlayerEvent.AttackEntity {
	public static final Path JSON = RandomConfigs.getJson("attackspeeds");

	private static AttackSpeedConfig speeds = new AttackSpeedConfig();
	private static boolean reloaded;

	@Override
	public void onEntityAdded(ServerWorld world, Entity entity) {
		if(!(entity instanceof PlayerEntity)) {
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
		final EntityAttributeInstance attackSpeed =
				((PlayerEntity) entity).getAttributeInstance(EntityAttributes.ATTACK_SPEED);

		//If configurable attack speeds are disabled, set it to the vanilla default of 4.0
		attackSpeed.setBaseValue(speeds.enabled ? speeds.defaultAttackSpeed : 4.0);
	}

	@Override
	public void onPlayerTick(ServerPlayerEntity player) {
		if(!speeds.enabled) {
			return;
		}

		final Item item = player.getStackInHand(player.getActiveHand()).getItem();

		final EntityAttributeInstance attackSpeed =
				player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);

		final ItemAttackSpeed speed = speeds.itemAttackSpeeds.get(item);
		attackSpeed.setBaseValue(speed == null ? speeds.defaultAttackSpeed : speed.speed);
	}

	@Override
	public boolean onPlayerAttackEntity(ServerPlayerEntity player, Entity target) {
		if(player.method_7261(0.5F) == 1.0F) {
			return true;
		}

		final Item item = player.getStackInHand(player.getActiveHand()).getItem();
		final ItemAttackSpeed speed = speeds.itemAttackSpeeds.get(item);

		if(speed == null) {
			if(speeds.disableAttacksDuringAttackCooldownByDefault) {
				player.method_7350();
				return false;
			}
		} else {
			if(speed.disableAttacksDuringAttackCooldown) {
				player.method_7350();
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

	public static void registerCommand() {
		if(speeds.asreloadCommand) {
			CommandRegistry.INSTANCE.register(false, ASReloadCommand::register);
		}
	}
}
