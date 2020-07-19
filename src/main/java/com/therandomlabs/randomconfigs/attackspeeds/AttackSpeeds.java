package com.therandomlabs.randomconfigs.attackspeeds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.therandomlabs.randomconfigs.RandomConfigs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public final class AttackSpeeds {
	public static final Path JSON = RandomConfigs.getJson("attackspeeds");

	private static AttackSpeedConfig speeds = new AttackSpeedConfig();

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if((!speeds.enabled && ModList.get().isLoaded("randomtweaks")) ||
				event.getWorld().isRemote) {
			return;
		}

		final Entity entity = event.getEntity();

		if (!(entity instanceof PlayerEntity)) {
			return;
		}

		final ModifiableAttributeInstance attackSpeed =
				((PlayerEntity) entity).getAttribute(Attributes.field_233825_h_);

		//If configurable attack speeds are disabled, set it to the vanilla default of 4.0
		//unless RandomTweaks is installed (see above if statement) since RandomTweaks
		//has its own configurable attack speed option
		attackSpeed.setBaseValue(speeds.enabled ? speeds.defaultAttackSpeed : 4.0);
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		if(!speeds.enabled) {
			return;
		}

		final Entity entity = event.getEntity();

		if (entity.getEntityWorld().isRemote || !(entity instanceof PlayerEntity)) {
			return;
		}

		final PlayerEntity player = (PlayerEntity) entity;
		final ItemStack stack = player.getHeldItem(player.getActiveHand());

		final ModifiableAttributeInstance attackSpeed =
				player.getAttribute(Attributes.field_233825_h_);

		final ItemAttackSpeed speed = speeds.itemAttackSpeeds.get(stack.getItem());
		attackSpeed.setBaseValue(speed == null ? speeds.defaultAttackSpeed : speed.speed);
	}

	@SubscribeEvent
	public void onPlayerAttackEntity(AttackEntityEvent event) {
		final PlayerEntity player = event.getPlayer();

		if(player.getEntityWorld().isRemote || player.getCooledAttackStrength(0.5F) == 1.0F) {
			return;
		}

		final Item item = player.getHeldItem(player.getActiveHand()).getItem();
		final ItemAttackSpeed speed = speeds.itemAttackSpeeds.get(item);

		if(speed == null) {
			if(speeds.disableAttacksDuringAttackCooldownByDefault) {
				player.resetCooldown();
				event.setCanceled(true);
			}
		} else {
			if(speed.disableAttacksDuringAttackCooldown) {
				player.resetCooldown();
				event.setCanceled(true);
			}
		}
	}

	public static AttackSpeedConfig get() {
		return speeds;
	}

	public static void reload() throws IOException {
		if(Files.exists(JSON)) {
			speeds = RandomConfigs.readJson(JSON, AttackSpeedConfig.class);
			speeds.ensureCorrect();
		}

		RandomConfigs.writeJson(JSON, speeds);
	}

	public static void registerClientCommand() {
		if (speeds.asreloadclientCommand) {
			//ClientCommandRegistry.instance.registerCommand(new ASReloadCommand(Side.CLIENT));
		}
	}

	public static void registerCommand(RegisterCommandsEvent event) {
		if (speeds.asreloadCommand) {
			ASReloadCommand.register(event.getDispatcher(), Dist.DEDICATED_SERVER);
		}
	}
}
