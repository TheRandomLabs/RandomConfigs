package com.therandomlabs.randomconfigs.attackspeeds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mojang.brigadier.CommandDispatcher;
import com.therandomlabs.randomconfigs.RandomConfigs;
import com.therandomlabs.randomconfigs.api.event.player.PlayerAttackEntityCallback;
import com.therandomlabs.randomconfigs.api.event.player.PlayerTickCallback;
import com.therandomlabs.randomconfigs.api.event.world.EntityAddedCallback;
import com.therandomlabs.randomconfigs.api.event.world.WorldInitializeCallback;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class AttackSpeeds
		implements EntityAddedCallback, PlayerTickCallback, PlayerAttackEntityCallback,
		WorldInitializeCallback, CommandRegistrationCallback {
	public static final Path JSON = RandomConfigs.getJson("attackspeeds");

	private static AttackSpeedConfig speeds = new AttackSpeedConfig();
	private static boolean firstReload = true;

	@Override
	public void onEntityAdded(ServerWorld world, Entity entity) {
		//Temporary workaround
		if (speeds.itemAttackSpeeds == null) {
			try {
				reload();
			} catch(IOException ex) {
				RandomConfigs.crashReport("Failed to load attack speeds", ex);
			}
		}

		if(entity instanceof PlayerEntity) {
			final EntityAttributeInstance attackSpeed =
					((PlayerEntity) entity).getAttributeInstance(EntityAttributes.ATTACK_SPEED);

			//If configurable attack speeds are disabled, set it to the vanilla default of 4.0
			attackSpeed.setBaseValue(speeds.enabled ? speeds.defaultAttackSpeed : 4.0);
		}
	}

	@SuppressWarnings("NullAway")
	@Override
	public void onPlayerTick(ServerPlayerEntity player) {
		//Temporary workaround
		if(speeds.itemAttackSpeeds == null) {
			try {
				reload();
			} catch(IOException ex) {
				RandomConfigs.crashReport("Failed to load attack speeds", ex);
			}
		}

		if(!speeds.enabled) {
			return;
		}

		final Item item = player.getStackInHand(player.getActiveHand()).getItem();

		final EntityAttributeInstance attackSpeed =
				player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);

		final ItemAttackSpeed speed = speeds.itemAttackSpeeds.get(item);
		attackSpeed.setBaseValue(speed == null ? speeds.defaultAttackSpeed : speed.speed);
	}

	@SuppressWarnings("NullAway")
	@Override
	public boolean onPlayerAttackEntity(ServerPlayerEntity player, Entity target) {
		//Temporary workaround
		if(speeds.itemAttackSpeeds == null) {
			try {
				reload();
			} catch(IOException ex) {
				RandomConfigs.crashReport("Failed to load attack speeds", ex);
			}
		}
		if(player.getAttackCooldownProgress(0.5F) == 1.0F) {
			return true;
		}

		final Item item = player.getStackInHand(player.getActiveHand()).getItem();
		final ItemAttackSpeed speed = speeds.itemAttackSpeeds.get(item);

		if(speed == null) {
			if(speeds.disableAttacksDuringAttackCooldownByDefault) {
				player.resetLastAttackedTicks();
				return false;
			}
		} else {
			if(speed.disableAttacksDuringAttackCooldown) {
				player.resetLastAttackedTicks();
				return false;
			}
		}

		return true;
	}

	@Override
	public void onInitialize(ServerWorld world) {
		try {
			reload();
		} catch (IOException ex) {
			RandomConfigs.crashReport("Failed to load attack speeds", ex);
		}
	}

	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
		if (speeds.asreloadCommand) {
			ASReloadCommand.register(dispatcher);
		}
	}

	public static AttackSpeedConfig get() {
		return speeds;
	}

	@SuppressWarnings("NullAway")
	public static void reload() throws IOException {
		if (Files.exists(JSON)) {
			speeds = RandomConfigs.readJson(JSON, AttackSpeedConfig.class);

			if (firstReload) {
				firstReload = false;
			} else {
				speeds.ensureCorrect();
			}
		}

		RandomConfigs.writeJson(JSON, speeds);
	}
}
