package com.therandomlabs.randomconfigs.attackspeeds;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class AttackSpeedConfig {
	public Map<String, ItemAttackSpeed> attackSpeeds = new HashMap<>();
	public double defaultAttackSpeed = 4.0;
	public boolean disableAttacksDuringAttackCooldownByDefault;
	public boolean asreloadCommand = true;
	public boolean enabled;

	public transient Map<Item, ItemAttackSpeed> itemAttackSpeeds;

	public void ensureCorrect() {
		final Map<String, ItemAttackSpeed> newAttackSpeeds = new HashMap<>(attackSpeeds.size());
		itemAttackSpeeds = new HashMap<>(attackSpeeds.size());

		for(Map.Entry<String, ItemAttackSpeed> entry : attackSpeeds.entrySet()) {
			final Identifier identifier = new Identifier(entry.getKey());

			if(!Registry.ITEM.containsId(identifier)) {
				continue;
			}

			final Item item = Registry.ITEM.get(identifier);

			final ItemAttackSpeed speed = entry.getValue();
			speed.ensureCorrect();

			newAttackSpeeds.put(Registry.ITEM.getId(item).toString(), speed);
			itemAttackSpeeds.put(item, speed);
		}

		attackSpeeds = newAttackSpeeds;
	}
}
