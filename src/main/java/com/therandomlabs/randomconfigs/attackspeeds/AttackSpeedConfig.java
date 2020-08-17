package com.therandomlabs.randomconfigs.attackspeeds;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Lifecycle;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraftforge.registries.GameData;

public final class AttackSpeedConfig {
	private static final SimpleRegistry<Item> ITEM_REGISTRY =
			GameData.getWrapper(Registry.field_239714_o_, Lifecycle.experimental());

	public Map<String, ItemAttackSpeed> attackSpeeds = new HashMap<>();
	public double defaultAttackSpeed = 4.0;
	public boolean disableAttacksDuringAttackCooldownByDefault;
	public boolean asreloadCommand = true;
	public boolean asreloadclientCommand = true;
	public boolean enabled;

	public transient Map<Item, ItemAttackSpeed> itemAttackSpeeds;

	public void ensureCorrect() {
		final Map<String, ItemAttackSpeed> newAttackSpeeds = new HashMap<>(attackSpeeds.size());
		itemAttackSpeeds = new HashMap<>(attackSpeeds.size());

		for(Map.Entry<String, ItemAttackSpeed> entry : attackSpeeds.entrySet()) {
			final Item item = ITEM_REGISTRY.getOrDefault(new ResourceLocation(entry.getKey()));

			if(item == Items.AIR) {
				continue;
			}

			final ItemAttackSpeed speed = entry.getValue();
			speed.ensureCorrect();

			newAttackSpeeds.put(item.getRegistryName().toString(), speed);
			itemAttackSpeeds.put(item, speed);
		}

		attackSpeeds = newAttackSpeeds;
	}
}
