package com.orderfoodnow.pos.frontend;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum FontSizeType {
	// @formatter:off
	SMALL("small", "Small"),
	MEDIUM("medium", "Medium"),
	LARGE("large", "Large"),
	;
	// @formatter:on

	FontSizeType(String actionName, String displayName) {
		this.actionName = actionName;
		this.displayName = displayName;
	}

	private final String actionName;
	private final String displayName;
	// to ensure uniqueness and easy paramId look up
	private static final Map<String, FontSizeType> actionNameToFontSizeType = new HashMap<>();

	private static final Set<String> enumNames = new HashSet<>();
	static {
		for (FontSizeType type : EnumSet.allOf(FontSizeType.class)) {
			enumNames.add(type.toString());
			if (type.actionName != null) {
				if (actionNameToFontSizeType.containsKey(type.actionName)) {
					throw new RuntimeException("The actionName value of " + type.actionName + " is duplicated");
				}
				actionNameToFontSizeType.put(type.actionName, type);
				enumNames.add(type.toString());
			}
		}
	}

	public String getActionName() {
		return actionName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static FontSizeType getEnum(String actionName) {
		return actionNameToFontSizeType.get(actionName);
	}

	public static FontSizeType getEnum(Integer ordinalValue) {
		return (ordinalValue < 0 || ordinalValue >= FontSizeType.values().length ? null
				: FontSizeType.values()[ordinalValue]);
	}

	public static Set<String> getEnumNames() {
		return enumNames;
	}

	public static void main(String[] args) {
		System.out.println("Unit testing for " + FontSizeType.class.getSimpleName());
		for (FontSizeType type : EnumSet.allOf(FontSizeType.class)) {
			System.out.println("displayName=" + type.getDisplayName());
		}

		for (String actionName : actionNameToFontSizeType.keySet()) {
			System.out.println("actionName=" + actionName + " type=" + FontSizeType.getEnum(actionName));
		}
	}
}
