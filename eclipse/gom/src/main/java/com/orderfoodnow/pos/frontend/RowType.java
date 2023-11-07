package com.orderfoodnow.pos.frontend;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum RowType {
	// @formatter:off
	STREET('S', "Street"),
	CITY('C', "City"),
	TABLE_NUMBER('T', "Table Number"),
	;
	// @formatter:on

	RowType(char characterCode, String displayName) {
		this.characterCode = characterCode;
		this.displayName = displayName;
	}

	private final Character characterCode;
	private final String displayName;
	// to ensure uniqueness and easy paramId look up
	private static final Map<Character, RowType> characterCodeToRowType = new HashMap<>();
	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (RowType type : EnumSet.allOf(RowType.class)) {
			enumNames.add(type.toString());
			if (type.characterCode != null) {
				if (characterCodeToRowType.containsKey(type.characterCode)) {
					throw new RuntimeException("The characterCode value of " + type.characterCode + " is duplicated");
				}
				characterCodeToRowType.put(type.characterCode, type);
				enumNames.add(type.toString());
			}
		}
	}

	public char getCharacterCode() {
		return characterCode;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static RowType getEnum(Character characterCode) {
		return characterCodeToRowType.get(characterCode);
	}

	public static RowType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= RowType.values().length ? null : RowType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + RowType.class.getSimpleName());
		for (RowType type : EnumSet.allOf(RowType.class)) {
			System.out.println("displayName=" + type.getDisplayName());
		}

		for (Character actionName : characterCodeToRowType.keySet()) {
			System.out.println("characterCode=" + actionName + " type=" + RowType.getEnum(actionName));
		}
	}
}
