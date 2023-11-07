package com.orderfoodnow.pos.shared;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum DishSize {
	// @formatter:off
	LARGE("Large", 'L'),
	SMALL("Small", 'S'),
	;
	// @formatter:on

	DishSize(String displayName, Character shortHandChar) {
		this.displayName = displayName;
		this.shortHandChar = shortHandChar;
	}

	private final String displayName;
	private final Character shortHandChar;
	// to ensure shortHandChar uniqueness and easy paramId look up
	private static final Map<Character, DishSize> shortHandCharToDishSize = new HashMap<>();
	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (DishSize type : EnumSet.allOf(DishSize.class)) {
			enumNames.add(type.toString());
			if (type.shortHandChar != null) {
				if (shortHandCharToDishSize.containsKey(type.shortHandChar)) {
					throw new RuntimeException("The shortHandChar value of " + type.shortHandChar + " is duplicated");
				}
				shortHandCharToDishSize.put(type.shortHandChar, type);
				enumNames.add(type.toString());
			}
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public Character getShortHandChar() {
		return shortHandChar;
	}

	public DishSize getEnum(Character shortHandChar) {
		return shortHandCharToDishSize.get(shortHandChar);
	}

	public static DishSize getEnum(int ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= DishSize.values().length ? null : DishSize.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}
}
