package com.therandomlabs.randomconfigs.attackspeeds;

public final class ItemAttackSpeed {
	public double speed = 4.0;
	public boolean disableAttacksDuringAttackCooldown;

	public void ensureCorrect() {
		if(speed < 0.0) {
			speed = 0.0;
		} else if(speed > 1024.0) {
			speed = 1024.0;
		}
	}
}
