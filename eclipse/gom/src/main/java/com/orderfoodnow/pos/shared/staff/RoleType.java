package com.orderfoodnow.pos.shared.staff;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum RoleType {
	// @formatter:off
	OWNER("Owner", "owner", 'O'),
	MANAGER("Manager", "manager", 'M'),
	CASHIER("Cashier", "cashier", 'C'),
	SERVER("Server", "server", 'S'),
	DRIVER("Driver", "driver", 'D'),
	PACKER("Packer", "packer", 'P'),
	BUSSER("Busser", "busser", 'B'),
	;
	// @formatter:on

	RoleType(String displayName, String configurationName, Character shortHandChar) {
		this.displayName = displayName;
		this.configurationName = configurationName;
		this.shortHandChar = shortHandChar;
	}

	private final String displayName;
	private final String configurationName;
	private final Character shortHandChar;
	// to ensure shortHandChar uniqueness and easy paramId look up
	private static final Map<Character, RoleType> shortHandCharToRoleType = new HashMap<>();
	static {
		for (RoleType type : EnumSet.allOf(RoleType.class)) {
			if (type.shortHandChar != null) {
				if (shortHandCharToRoleType.containsKey(type.shortHandChar)) {
					throw new RuntimeException("The paramId value of " + type.shortHandChar + " is duplicated");
				}
				shortHandCharToRoleType.put(type.shortHandChar, type);
			}
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getConfigurationName() {
		return configurationName;
	}

	public Character getShortHandChar() {
		return shortHandChar;
	}

	public static RoleType getEnum(Character shortHandChar) {
		return shortHandCharToRoleType.get(shortHandChar);
	}

	public static RoleType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= RoleType.values().length ? null : RoleType.values()[ordinalValue]);
	}
}
