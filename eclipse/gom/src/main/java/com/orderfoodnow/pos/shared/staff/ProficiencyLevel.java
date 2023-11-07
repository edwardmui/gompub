package com.orderfoodnow.pos.shared.staff;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

//possible use case: configure GUI based on user proficiency level 
public enum ProficiencyLevel {
	// @formatter:off
	BEGINNER("beginner", 'B'),
	INTERMEDIATE("intermediate", 'I'),
	ADVANCED("advanced", 'A'),
	EXPERT("expert", 'E'),
	;
	// @formatter:on

	ProficiencyLevel(String displayName, Character shortHandChar) {
		this.displayName = displayName;
		this.shortHandChar = shortHandChar;
	}

	private final String displayName;
	private final Character shortHandChar;
	// to ensure shortHandChar uniqueness and easy paramId look up
	private static final Map<Character, ProficiencyLevel> shortHandCharToProficiencyLevel = new HashMap<>();

	static {
		for (ProficiencyLevel type : EnumSet.allOf(ProficiencyLevel.class)) {
			if (type.shortHandChar != null) {
				if (shortHandCharToProficiencyLevel.containsKey(type.shortHandChar)) {
					throw new RuntimeException("The paramId value of " + type.shortHandChar + " is duplicated");
				}
				shortHandCharToProficiencyLevel.put(type.shortHandChar, type);
			}
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public Character getShortHandChar() {
		return shortHandChar;
	}

	public ProficiencyLevel getEnum(Character shortHandChar) {
		return shortHandCharToProficiencyLevel.get(shortHandChar);
	}

	public static ProficiencyLevel getEnum(int ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= ProficiencyLevel.values().length ? null
				: ProficiencyLevel.values()[ordinalValue]);
	}
}
