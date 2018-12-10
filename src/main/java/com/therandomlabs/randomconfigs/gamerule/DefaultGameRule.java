package com.therandomlabs.randomconfigs.gamerule;

public class DefaultGameRule {
	public String key;
	public String value;
	public boolean forced;

	public DefaultGameRule(String key, String value, boolean forced) {
		this.key = key;
		this.value = value;
		this.forced = forced;
	}
}
