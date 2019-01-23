package com.therandomlabs.randomconfigs.attackspeeds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.therandomlabs.randomconfigs.RandomConfigs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public final class AttackSpeeds {
	public static final Path JSON = RandomConfigs.getJson("attackspeeds");

	private static AttackSpeedConfig speeds = new AttackSpeedConfig();

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if((!speeds.enabled && RandomConfigs.RANDOMTWEAKS_LOADED) || event.getWorld().isRemote) {
			return;
		}

		final Entity entity = event.getEntity();

		if(!(entity instanceof EntityPlayer)) {
			return;
		}

		final EntityPlayer player = (EntityPlayer) event.getEntity();

		//We don't need to apply potion attributes here because it's done automatically
		final IAttributeInstance attackSpeed =
				player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);

		//If configurable attack speeds are disabled, set it to the vanilla default of 4.0
		//unless RandomTweaks is installed (see above if statement) since RandomTweaks
		//has its own configurable attack speed option
		attackSpeed.setBaseValue(speeds.enabled ? speeds.defaultAttackSpeed : 4.0);
	}

	@SubscribeEvent
	public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		if(!speeds.enabled) {
			return;
		}

		final Entity entity = event.getEntity();

		if(entity.getEntityWorld().isRemote || !(entity instanceof EntityPlayer)) {
			return;
		}

		final EntityPlayer player = (EntityPlayer) entity;
		final ItemStack stack = player.getHeldItem(player.getActiveHand());
		//1.10 compatibility
		final Item item = stack == null ? Items.AIR : stack.getItem();

		final IAttributeInstance attackSpeed =
				player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);

		final ItemAttackSpeed speed = speeds.itemAttackSpeeds.get(item);
		attackSpeed.setBaseValue(speed == null ? speeds.defaultAttackSpeed : speed.speed);
	}

	@SubscribeEvent
	public static void onPlayerAttackEntity(AttackEntityEvent event) {
		final EntityPlayer player = event.getEntityPlayer();

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

	public static void registerClientCommand() {
		if(speeds.asreloadclientCommand) {
			ClientCommandHandler.instance.registerCommand(new CommandASReload(Side.CLIENT));
		}
	}

	public static void registerCommand(FMLServerStartingEvent event) {
		if(speeds.asreloadCommand) {
			event.registerServerCommand(new CommandASReload(Side.SERVER));
		}
	}
}
